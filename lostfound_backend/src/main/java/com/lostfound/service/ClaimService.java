package com.lostfound.service;

import com.lostfound.dto.request.ClaimCreateRequest;
import com.lostfound.dto.request.ClaimReviewRequest;
import com.lostfound.dto.response.ClaimResponse;
import com.lostfound.entity.Claim;
import com.lostfound.entity.Item;
import com.lostfound.entity.User;
import com.lostfound.entity.enums.ClaimStatus;
import com.lostfound.entity.enums.ItemStatus;
import com.lostfound.entity.enums.ItemType;
import com.lostfound.entity.enums.Role;
import com.lostfound.exception.InvalidStateException;
import com.lostfound.exception.ResourceNotFoundException;
import com.lostfound.exception.UnauthorizedActionException;
import com.lostfound.repository.ClaimRepository;
import com.lostfound.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final ItemRepository itemRepository;
    private final NotificationService notificationService;

    @Transactional
    public ClaimResponse createClaim(UUID itemId, ClaimCreateRequest request, User currentUser) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemId));

        if (item.isDeleted()) {
            throw new ResourceNotFoundException("Item not found: " + itemId);
        }
        if (item.getItemType() != ItemType.FOUND) {
            throw new InvalidStateException("Only FOUND items can be claimed");
        }
        if (item.getStatus() == ItemStatus.RESOLVED || item.getStatus() == ItemStatus.CLOSED) {
            throw new InvalidStateException("This item is no longer open for claims (status: " + item.getStatus() + ")");
        }
        if (item.getReportedBy().getId().equals(currentUser.getId())) {
            throw new InvalidStateException("You cannot claim an item you reported yourself");
        }
        if (claimRepository.existsByItemIdAndClaimedByIdAndStatus(itemId, currentUser.getId(), ClaimStatus.PENDING)) {
            throw new InvalidStateException("You already have a pending claim on this item");
        }

        Claim claim = Claim.builder()
                .item(item)
                .claimedBy(currentUser)
                .proofDescription(request.proofDescription())
                .status(ClaimStatus.PENDING)
                .build();

        Claim saved = claimRepository.save(claim);

        // Item now has at least one claim under review.
        if (item.getStatus() == ItemStatus.OPEN) {
            item.setStatus(ItemStatus.CLAIMED);
        }

        notificationService.notifyItemOwnerOfNewClaim(saved);
        log.info("User {} claimed item {} (claim {})", currentUser.getId(), itemId, saved.getId());

        return ClaimResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<ClaimResponse> getClaimsByStatus(ClaimStatus status, Pageable pageable) {
        Page<Claim> claims = (status != null)
                ? claimRepository.findByStatus(status, pageable)
                : claimRepository.findAll(pageable);
        return claims.map(ClaimResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<ClaimResponse> getMyClaims(User currentUser, Pageable pageable) {
        return claimRepository.findByClaimedByIdOrderByCreatedAtDesc(currentUser.getId(), pageable)
                .map(ClaimResponse::from);
    }

    @Transactional(readOnly = true)
    public ClaimResponse getClaimById(UUID claimId, User currentUser) {
        Claim claim = findClaimOrThrow(claimId);
        assertOwnerOrAdmin(claim, currentUser);
        return ClaimResponse.from(claim);
    }

    @Transactional
    public ClaimResponse reviewClaim(UUID claimId, ClaimReviewRequest request, User adminUser) {
        Claim claim = findClaimOrThrow(claimId);

        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new InvalidStateException("This claim has already been reviewed (status: " + claim.getStatus() + ")");
        }
        if (request.status() == ClaimStatus.PENDING) {
            throw new InvalidStateException("Review decision must be APPROVED or REJECTED, not PENDING");
        }

        claim.setReviewedBy(adminUser);
        Item item = claim.getItem();

        if (request.status() == ClaimStatus.APPROVED) {
            claim.setStatus(ClaimStatus.APPROVED);
            item.setStatus(ItemStatus.RESOLVED);

            // Competing claims on the same item are now moot.
            List<Claim> otherPending = claimRepository.findByItemIdAndStatus(item.getId(), ClaimStatus.PENDING);
            for (Claim other : otherPending) {
                if (!other.getId().equals(claim.getId())) {
                    other.setStatus(ClaimStatus.REJECTED);
                    other.setReviewedBy(adminUser);
                    notificationService.notifyClaimantOfDecision(other);
                }
            }
        } else {
            claim.setStatus(ClaimStatus.REJECTED);

            // If nothing else is pending, the item becomes discoverable again.
            List<Claim> stillPending = claimRepository.findByItemIdAndStatus(item.getId(), ClaimStatus.PENDING);
            boolean anyOtherPending = stillPending.stream().anyMatch(c -> !c.getId().equals(claim.getId()));
            if (!anyOtherPending && item.getStatus() == ItemStatus.CLAIMED) {
                item.setStatus(ItemStatus.OPEN);
            }
        }

        notificationService.notifyClaimantOfDecision(claim);
        log.info("Claim {} reviewed as {} by admin {}", claimId, claim.getStatus(), adminUser.getId());

        return ClaimResponse.from(claim);
    }

    private Claim findClaimOrThrow(UUID claimId) {
        return claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found: " + claimId));
    }

    private void assertOwnerOrAdmin(Claim claim, User currentUser) {
        boolean isOwner = claim.getClaimedBy().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new UnauthorizedActionException("You do not have permission to view this claim");
        }
    }
}
