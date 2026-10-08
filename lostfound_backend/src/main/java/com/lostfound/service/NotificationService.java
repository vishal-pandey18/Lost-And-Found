package com.lostfound.service;

import com.lostfound.dto.response.NotificationResponse;
import com.lostfound.entity.Claim;
import com.lostfound.entity.Item;
import com.lostfound.entity.Notification;
import com.lostfound.entity.User;
import com.lostfound.entity.UserSubscription;
import com.lostfound.entity.enums.ClaimStatus;
import com.lostfound.entity.enums.NotificationType;
import com.lostfound.exception.ResourceNotFoundException;
import com.lostfound.repository.ItemRepository;
import com.lostfound.repository.NotificationRepository;
import com.lostfound.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserSubscriptionRepository subscriptionRepository;
    private final ItemRepository itemRepository;

    // Words shorter than this are too generic to match usefully ("a", "the", "bag" is fine but "of" isn't)
    private static final int MIN_KEYWORD_LENGTH = 4;
    private static final int MAX_KEYWORDS = 6;

    // ---------------------------------------------------------------
    // Matching: called when a FOUND item is reported
    // ---------------------------------------------------------------

    /**
     * Notifies only people who plausibly care about this found item:
     *   1. users whose still-open LOST report matches it (same category + shared keyword)
     *   2. users who subscribed to that category and/or keyword
     *
     * Deliberately NOT a broadcast to all users - that doesn't scale and reads as spam.
     */
    @Transactional
    public int notifyMatchesForFoundItem(Item foundItem) {
        Set<UUID> notifiedUserIds = new HashSet<>();
        List<Notification> toSave = new ArrayList<>();

        List<String> keywords = extractKeywords(foundItem);
        Integer categoryId = foundItem.getCategory().getId();
        UUID reporterId = foundItem.getReportedBy().getId();

        // 1. Matching open LOST reports
        Map<UUID, Item> matchedLostItems = new HashMap<>();
        for (String keyword : keywords) {
            for (Item lostItem : itemRepository.findMatchingLostItems(categoryId, keyword)) {
                matchedLostItems.putIfAbsent(lostItem.getId(), lostItem);
            }
        }

        for (Item lostItem : matchedLostItems.values()) {
            UUID ownerId = lostItem.getReportedBy().getId();
            if (ownerId.equals(reporterId) || notifiedUserIds.contains(ownerId)) {
                continue; // don't notify the finder about their own report, or twice
            }
            notifiedUserIds.add(ownerId);
            toSave.add(Notification.builder()
                    .user(lostItem.getReportedBy())
                    .item(foundItem)
                    .type(NotificationType.NEW_MATCH)
                    .title("Possible match for your lost item")
                    .message("Someone reported a found item that may match \"" + lostItem.getTitle()
                            + "\": " + foundItem.getTitle()
                            + (foundItem.getLocation() != null ? " (found near " + foundItem.getLocation() + ")" : ""))
                    .build());
        }

        // 2. Subscribers to this category / keyword
        for (UserSubscription sub : subscriptionRepository.findPotentialMatches(categoryId)) {
            UUID subscriberId = sub.getUser().getId();
            if (subscriberId.equals(reporterId) || notifiedUserIds.contains(subscriberId)) {
                continue;
            }
            if (!subscriptionMatches(sub, foundItem, keywords)) {
                continue;
            }
            notifiedUserIds.add(subscriberId);
            toSave.add(Notification.builder()
                    .user(sub.getUser())
                    .item(foundItem)
                    .type(NotificationType.NEW_MATCH)
                    .title("New found item you're watching for")
                    .message("A found item matching your alerts was reported: " + foundItem.getTitle())
                    .build());
        }

        if (!toSave.isEmpty()) {
            notificationRepository.saveAll(toSave);
        }
        log.info("Found item {} matched {} user(s) for notification", foundItem.getId(), toSave.size());
        return toSave.size();
    }

    private boolean subscriptionMatches(UserSubscription sub, Item foundItem, List<String> itemKeywords) {
        boolean categoryOk = sub.getCategory() == null
                || sub.getCategory().getId().equals(foundItem.getCategory().getId());
        if (!categoryOk) {
            return false;
        }
        if (sub.getKeyword() == null || sub.getKeyword().isBlank()) {
            return true; // category-only subscription
        }
        String needle = sub.getKeyword().toLowerCase();
        String haystack = (foundItem.getTitle() + " "
                + (foundItem.getDescription() == null ? "" : foundItem.getDescription())).toLowerCase();
        return haystack.contains(needle) || itemKeywords.contains(needle);
    }

    /**
     * Splits the found item's title/description into candidate keywords.
     * Simple word-based matching; upgrading this to embeddings + pgvector
     * is the natural next step for much better matching.
     */
    private List<String> extractKeywords(Item item) {
        String text = item.getTitle() + " " + (item.getDescription() == null ? "" : item.getDescription());
        return Arrays.stream(text.toLowerCase().split("[^a-z0-9]+"))
                .filter(w -> w.length() >= MIN_KEYWORD_LENGTH)
                .distinct()
                .limit(MAX_KEYWORDS)
                .toList();
    }

    // ---------------------------------------------------------------
    // Claim-related notifications
    // ---------------------------------------------------------------

    @Transactional
    public void notifyItemOwnerOfNewClaim(Claim claim) {
        User itemOwner = claim.getItem().getReportedBy();
        if (itemOwner.getId().equals(claim.getClaimedBy().getId())) {
            return;
        }
        notificationRepository.save(Notification.builder()
                .user(itemOwner)
                .item(claim.getItem())
                .type(NotificationType.CLAIM_UPDATE)
                .title("Someone claimed an item you found")
                .message(claim.getClaimedBy().getName() + " submitted a claim on \""
                        + claim.getItem().getTitle() + "\". An admin will review it.")
                .build());
    }

    @Transactional
    public void notifyClaimantOfDecision(Claim claim) {
        boolean approved = claim.getStatus() == ClaimStatus.APPROVED;
        notificationRepository.save(Notification.builder()
                .user(claim.getClaimedBy())
                .item(claim.getItem())
                .type(NotificationType.CLAIM_UPDATE)
                .title(approved ? "Your claim was approved" : "Your claim was not approved")
                .message(approved
                        ? "Your claim on \"" + claim.getItem().getTitle()
                            + "\" was approved. Contact the finder to arrange collection."
                        : "Your claim on \"" + claim.getItem().getTitle()
                            + "\" was rejected. If you believe this is a mistake, you can submit a new claim with more detail.")
                .build());
    }

    // ---------------------------------------------------------------
    // Inbox
    // ---------------------------------------------------------------

    public Page<NotificationResponse> getMyNotifications(User user, boolean unreadOnly, Pageable pageable) {
        Page<Notification> page = unreadOnly
                ? notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(user.getId(), pageable)
                : notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
        return page.map(NotificationResponse::from);
    }

    public long getUnreadCount(User user) {
        return notificationRepository.countByUserIdAndReadFalse(user.getId());
    }

    @Transactional
    public NotificationResponse markAsRead(UUID notificationId, User user) {
        Notification notification = notificationRepository
                .findByIdAndUserId(notificationId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));
        notification.setRead(true);
        return NotificationResponse.from(notification);
    }

    @Transactional
    public int markAllAsRead(User user) {
        return notificationRepository.markAllAsRead(user.getId());
    }
}
