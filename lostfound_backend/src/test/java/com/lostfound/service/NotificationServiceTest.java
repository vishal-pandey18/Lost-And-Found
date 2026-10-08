package com.lostfound.service;

import com.lostfound.entity.Category;
import com.lostfound.entity.Item;
import com.lostfound.entity.Notification;
import com.lostfound.entity.User;
import com.lostfound.entity.UserSubscription;
import com.lostfound.entity.enums.ItemStatus;
import com.lostfound.entity.enums.ItemType;
import com.lostfound.entity.enums.Role;
import com.lostfound.repository.ItemRepository;
import com.lostfound.repository.NotificationRepository;
import com.lostfound.repository.UserSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private UserSubscriptionRepository subscriptionRepository;
    @Mock private ItemRepository itemRepository;

    @InjectMocks private NotificationService notificationService;

    private User finder;
    private User loser;
    private Category electronics;
    private Item foundItem;

    @BeforeEach
    void setUp() {
        finder = User.builder().id(UUID.randomUUID()).name("Finder").role(Role.USER).build();
        loser = User.builder().id(UUID.randomUUID()).name("Loser").role(Role.USER).build();
        electronics = Category.builder().id(1).name("Electronics").build();

        foundItem = Item.builder()
                .id(UUID.randomUUID())
                .title("Found black earphones")
                .description("Boat brand, near library")
                .category(electronics)
                .itemType(ItemType.FOUND)
                .status(ItemStatus.OPEN)
                .reportedBy(finder)
                .build();
    }

    @Test
    void notifiesTheOwnerOfAMatchingLostReport() {
        Item lostItem = Item.builder()
                .id(UUID.randomUUID())
                .title("Lost my earphones")
                .category(electronics)
                .itemType(ItemType.LOST)
                .status(ItemStatus.OPEN)
                .reportedBy(loser)
                .build();

        when(itemRepository.findMatchingLostItems(anyInt(), anyString())).thenReturn(List.of(lostItem));
        when(subscriptionRepository.findPotentialMatches(anyInt())).thenReturn(List.of());

        int count = notificationService.notifyMatchesForFoundItem(foundItem);

        assertThat(count).isEqualTo(1);

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).getUser()).isEqualTo(loser);
    }

    @Test
    void doesNotNotifyTheFinderAboutTheirOwnReport() {
        // The finder also happens to have an open LOST report in this category
        Item finderOwnLostItem = Item.builder()
                .id(UUID.randomUUID())
                .title("Lost earphones")
                .category(electronics)
                .itemType(ItemType.LOST)
                .status(ItemStatus.OPEN)
                .reportedBy(finder)
                .build();

        when(itemRepository.findMatchingLostItems(anyInt(), anyString())).thenReturn(List.of(finderOwnLostItem));
        when(subscriptionRepository.findPotentialMatches(anyInt())).thenReturn(List.of());

        int count = notificationService.notifyMatchesForFoundItem(foundItem);

        assertThat(count).isZero();
        verify(notificationRepository, never()).saveAll(any());
    }

    @Test
    void notifiesCategorySubscribers() {
        User watcher = User.builder().id(UUID.randomUUID()).name("Watcher").role(Role.USER).build();
        UserSubscription sub = UserSubscription.builder()
                .id(1L).user(watcher).category(electronics).build();

        when(itemRepository.findMatchingLostItems(anyInt(), anyString())).thenReturn(List.of());
        when(subscriptionRepository.findPotentialMatches(anyInt())).thenReturn(List.of(sub));

        int count = notificationService.notifyMatchesForFoundItem(foundItem);

        assertThat(count).isEqualTo(1);
    }

    @Test
    void keywordSubscriptionOnlyFiresWhenTheKeywordActuallyMatches() {
        User watcher = User.builder().id(UUID.randomUUID()).name("Watcher").role(Role.USER).build();
        UserSubscription nonMatching = UserSubscription.builder()
                .id(1L).user(watcher).category(electronics).keyword("umbrella").build();

        when(itemRepository.findMatchingLostItems(anyInt(), anyString())).thenReturn(List.of());
        when(subscriptionRepository.findPotentialMatches(anyInt())).thenReturn(List.of(nonMatching));

        int count = notificationService.notifyMatchesForFoundItem(foundItem);

        assertThat(count).isZero();
    }

    @Test
    void eachUserIsNotifiedAtMostOncePerFoundItem() {
        // Same user has a matching lost report AND a matching subscription
        Item lostItem = Item.builder()
                .id(UUID.randomUUID())
                .title("Lost earphones")
                .category(electronics)
                .itemType(ItemType.LOST)
                .status(ItemStatus.OPEN)
                .reportedBy(loser)
                .build();
        UserSubscription sub = UserSubscription.builder()
                .id(1L).user(loser).category(electronics).build();

        when(itemRepository.findMatchingLostItems(anyInt(), anyString())).thenReturn(List.of(lostItem));
        when(subscriptionRepository.findPotentialMatches(anyInt())).thenReturn(List.of(sub));

        int count = notificationService.notifyMatchesForFoundItem(foundItem);

        assertThat(count).isEqualTo(1);
    }
}
