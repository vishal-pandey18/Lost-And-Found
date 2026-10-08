package com.lostfound.service;

import com.lostfound.dto.request.ItemCreateRequest;
import com.lostfound.dto.request.ItemUpdateRequest;
import com.lostfound.dto.response.ItemResponse;
import com.lostfound.entity.Category;
import com.lostfound.entity.Item;
import com.lostfound.entity.ItemImage;
import com.lostfound.entity.User;
import com.lostfound.entity.enums.ItemStatus;
import com.lostfound.entity.enums.ItemType;
import com.lostfound.entity.enums.Role;
import com.lostfound.exception.ResourceNotFoundException;
import com.lostfound.exception.UnauthorizedActionException;
import com.lostfound.repository.CategoryRepository;
import com.lostfound.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;

    @Transactional
    public ItemResponse createItem(ItemCreateRequest request, User currentUser) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.categoryId()));

        Item item = Item.builder()
                .title(request.title())
                .description(request.description())
                .category(category)
                .itemType(request.itemType())
                .status(ItemStatus.OPEN)
                .location(request.location())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .eventDate(request.eventDate())
                .reportedBy(currentUser)
                .build();

        Item saved = itemRepository.save(item);
        log.info("User {} reported {} item {}", currentUser.getId(), saved.getItemType(), saved.getId());

        // Phase 5: a new FOUND report may match existing open LOST reports.
        // Notifications go only to plausibly-interested users, never all users.
        if (saved.getItemType() == ItemType.FOUND) {
            notificationService.notifyMatchesForFoundItem(saved);
        }

        return ItemResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<ItemResponse> searchItems(ItemType itemType, ItemStatus status, Integer categoryId, Pageable pageable) {
        return itemRepository.search(itemType, status, categoryId, pageable)
                .map(ItemResponse::from);
    }

    @Transactional(readOnly = true)
    public ItemResponse getItemById(UUID id) {
        return ItemResponse.from(findActiveItemOrThrow(id));
    }

    @Transactional
    public ItemResponse updateItem(UUID id, ItemUpdateRequest request, User currentUser) {
        Item item = findActiveItemOrThrow(id);
        assertOwnerOrAdmin(item, currentUser);

        if (request.title() != null) item.setTitle(request.title());
        if (request.description() != null) item.setDescription(request.description());
        if (request.status() != null) item.setStatus(request.status());
        if (request.location() != null) item.setLocation(request.location());
        if (request.latitude() != null) item.setLatitude(request.latitude());
        if (request.longitude() != null) item.setLongitude(request.longitude());
        if (request.eventDate() != null) item.setEventDate(request.eventDate());

        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.categoryId()));
            item.setCategory(category);
        }

        // Managed entity: Hibernate flushes these changes on commit.
        return ItemResponse.from(item);
    }

    @Transactional
    public void deleteItem(UUID id, User currentUser) {
        Item item = findActiveItemOrThrow(id);
        assertOwnerOrAdmin(item, currentUser);
        item.setDeleted(true); // soft delete keeps history/audit intact
        log.info("Item {} soft-deleted by user {}", id, currentUser.getId());
    }

    @Transactional
    public ItemResponse addImage(UUID itemId, MultipartFile file, User currentUser) {
        Item item = findActiveItemOrThrow(itemId);
        assertOwnerOrAdmin(item, currentUser);

        String imageUrl = fileStorageService.store(file);
        item.addImage(ItemImage.builder().imageUrl(imageUrl).build());

        return ItemResponse.from(item);
    }

    private Item findActiveItemOrThrow(UUID id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + id));
        if (item.isDeleted()) {
            throw new ResourceNotFoundException("Item not found: " + id);
        }
        return item;
    }

    private void assertOwnerOrAdmin(Item item, User currentUser) {
        boolean isOwner = item.getReportedBy().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new UnauthorizedActionException("You do not have permission to modify this item");
        }
    }
}
