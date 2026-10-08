package com.lostfound.service;

import com.lostfound.dto.request.ItemCreateRequest;
import com.lostfound.dto.request.ItemUpdateRequest;
import com.lostfound.dto.response.ItemResponse;
import com.lostfound.entity.Category;
import com.lostfound.entity.Item;
import com.lostfound.entity.User;
import com.lostfound.entity.enums.ItemStatus;
import com.lostfound.entity.enums.ItemType;
import com.lostfound.entity.enums.Role;
import com.lostfound.exception.ResourceNotFoundException;
import com.lostfound.exception.UnauthorizedActionException;
import com.lostfound.repository.CategoryRepository;
import com.lostfound.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ItemServiceTest {

    @Mock private ItemRepository itemRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private FileStorageService fileStorageService;
    @Mock private NotificationService notificationService;

    @InjectMocks private ItemService itemService;

    private User reporter;
    private Category category;

    @BeforeEach
    void setUp() {
        reporter = User.builder().id(UUID.randomUUID()).name("Asha").role(Role.USER).build();
        category = Category.builder().id(1).name("Electronics").build();
    }

    @Test
    void reportingAFoundItemTriggersMatchNotifications() {
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> {
            Item i = inv.getArgument(0);
            i.setId(UUID.randomUUID());
            return i;
        });

        ItemResponse response = itemService.createItem(
                new ItemCreateRequest("Found black earphones", "Near the library",
                        1, ItemType.FOUND, "Library", null, null, null),
                reporter);

        assertThat(response.itemType()).isEqualTo(ItemType.FOUND);
        assertThat(response.status()).isEqualTo(ItemStatus.OPEN);
        verify(notificationService).notifyMatchesForFoundItem(any(Item.class));
    }

    @Test
    void reportingALostItemDoesNotTriggerMatchNotifications() {
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> {
            Item i = inv.getArgument(0);
            i.setId(UUID.randomUUID());
            return i;
        });

        itemService.createItem(
                new ItemCreateRequest("Lost my phone", "Silver, cracked screen",
                        1, ItemType.LOST, "Canteen", null, null, null),
                reporter);

        // Matching runs when a FOUND item appears, not when a LOST report is filed.
        verify(notificationService, never()).notifyMatchesForFoundItem(any());
    }

    @Test
    void creatingAnItemWithUnknownCategoryFails() {
        when(categoryRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.createItem(
                new ItemCreateRequest("Something", null, 999, ItemType.LOST, null, null, null, null),
                reporter))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found");
    }

    @Test
    void nonOwnerCannotUpdateSomeoneElsesItem() {
        User stranger = User.builder().id(UUID.randomUUID()).name("Stranger").role(Role.USER).build();
        Item item = Item.builder()
                .id(UUID.randomUUID()).title("Wallet").category(category)
                .itemType(ItemType.FOUND).status(ItemStatus.OPEN).reportedBy(reporter).build();

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> itemService.updateItem(
                item.getId(),
                new ItemUpdateRequest("Hijacked title", null, null, null, null, null, null, null),
                stranger))
                .isInstanceOf(UnauthorizedActionException.class);

        assertThat(item.getTitle()).isEqualTo("Wallet");
    }

    @Test
    void adminCanUpdateAnyItem() {
        User admin = User.builder().id(UUID.randomUUID()).name("Admin").role(Role.ADMIN).build();
        Item item = Item.builder()
                .id(UUID.randomUUID()).title("Wallet").category(category)
                .itemType(ItemType.FOUND).status(ItemStatus.OPEN).reportedBy(reporter).build();

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        itemService.updateItem(item.getId(),
                new ItemUpdateRequest("Moderated title", null, null, null, null, null, null, null),
                admin);

        assertThat(item.getTitle()).isEqualTo("Moderated title");
    }

    @Test
    void deleteIsASoftDelete() {
        Item item = Item.builder()
                .id(UUID.randomUUID()).title("Wallet").category(category)
                .itemType(ItemType.FOUND).status(ItemStatus.OPEN).reportedBy(reporter).build();

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        itemService.deleteItem(item.getId(), reporter);

        assertThat(item.isDeleted()).isTrue();
        verify(itemRepository, never()).delete(any(Item.class));
    }

    @Test
    void softDeletedItemsAreTreatedAsMissing() {
        Item item = Item.builder()
                .id(UUID.randomUUID()).title("Gone").category(category)
                .itemType(ItemType.FOUND).status(ItemStatus.OPEN).reportedBy(reporter)
                .deleted(true).build();

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> itemService.getItemById(item.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
