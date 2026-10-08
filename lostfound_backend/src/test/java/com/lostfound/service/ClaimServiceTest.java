package com.lostfound.service;

import com.lostfound.dto.request.ClaimCreateRequest;
import com.lostfound.dto.request.ClaimReviewRequest;
import com.lostfound.dto.response.ClaimResponse;
import com.lostfound.entity.Category;
import com.lostfound.entity.Claim;
import com.lostfound.entity.Item;
import com.lostfound.entity.User;
import com.lostfound.entity.enums.ClaimStatus;
import com.lostfound.entity.enums.ItemStatus;
import com.lostfound.entity.enums.ItemType;
import com.lostfound.entity.enums.Role;
import com.lostfound.exception.InvalidStateException;
import com.lostfound.repository.ClaimRepository;
import com.lostfound.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ClaimServiceTest {

    @Mock private ClaimRepository claimRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks private ClaimService claimService;

    private User finder;
    private User claimant;
    private Category category;
    private Item foundItem;

    @BeforeEach
    void setUp() {
        finder = User.builder().id(UUID.randomUUID()).name("Finder").email("finder@example.com").role(Role.USER).build();
        claimant = User.builder().id(UUID.randomUUID()).name("Claimant").email("claimant@example.com").role(Role.USER).build();
        category = Category.builder().id(1).name("Electronics").build();

        foundItem = Item.builder()
                .id(UUID.randomUUID())
                .title("Black wallet")
                .category(category)
                .itemType(ItemType.FOUND)
                .status(ItemStatus.OPEN)
                .reportedBy(finder)
                .build();
    }

    @Test
    void createClaimMovesOpenItemToClaimedAndNotifiesOwner() {
        when(itemRepository.findById(foundItem.getId())).thenReturn(Optional.of(foundItem));
        when(claimRepository.existsByItemIdAndClaimedByIdAndStatus(any(), any(), any())).thenReturn(false);
        when(claimRepository.save(any(Claim.class))).thenAnswer(inv -> {
            Claim c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        ClaimResponse response = claimService.createClaim(
                foundItem.getId(),
                new ClaimCreateRequest("It has my initials scratched inside the flap"),
                claimant);

        assertThat(response.status()).isEqualTo(ClaimStatus.PENDING);
        assertThat(foundItem.getStatus()).isEqualTo(ItemStatus.CLAIMED);
        verify(notificationService).notifyItemOwnerOfNewClaim(any(Claim.class));
    }

    @Test
    void cannotClaimYourOwnReportedItem() {
        when(itemRepository.findById(foundItem.getId())).thenReturn(Optional.of(foundItem));

        assertThatThrownBy(() -> claimService.createClaim(
                foundItem.getId(),
                new ClaimCreateRequest("This is actually mine, I promise"),
                finder))
                .isInstanceOf(InvalidStateException.class)
                .hasMessageContaining("cannot claim an item you reported");

        verify(claimRepository, never()).save(any());
    }

    @Test
    void cannotClaimALostItem() {
        Item lostItem = Item.builder()
                .id(UUID.randomUUID())
                .title("Lost keys")
                .category(category)
                .itemType(ItemType.LOST)
                .status(ItemStatus.OPEN)
                .reportedBy(finder)
                .build();
        when(itemRepository.findById(lostItem.getId())).thenReturn(Optional.of(lostItem));

        assertThatThrownBy(() -> claimService.createClaim(
                lostItem.getId(),
                new ClaimCreateRequest("I think these are the keys I lost"),
                claimant))
                .isInstanceOf(InvalidStateException.class)
                .hasMessageContaining("Only FOUND items");
    }

    @Test
    void cannotClaimAnAlreadyResolvedItem() {
        foundItem.setStatus(ItemStatus.RESOLVED);
        when(itemRepository.findById(foundItem.getId())).thenReturn(Optional.of(foundItem));

        assertThatThrownBy(() -> claimService.createClaim(
                foundItem.getId(),
                new ClaimCreateRequest("Pretty sure that one is mine actually"),
                claimant))
                .isInstanceOf(InvalidStateException.class)
                .hasMessageContaining("no longer open for claims");
    }

    @Test
    void cannotSubmitTwoPendingClaimsOnTheSameItem() {
        when(itemRepository.findById(foundItem.getId())).thenReturn(Optional.of(foundItem));
        when(claimRepository.existsByItemIdAndClaimedByIdAndStatus(
                foundItem.getId(), claimant.getId(), ClaimStatus.PENDING)).thenReturn(true);

        assertThatThrownBy(() -> claimService.createClaim(
                foundItem.getId(),
                new ClaimCreateRequest("Following up on my earlier claim here"),
                claimant))
                .isInstanceOf(InvalidStateException.class)
                .hasMessageContaining("already have a pending claim");
    }

    @Test
    void approvingAClaimResolvesItemAndRejectsCompetingClaims() {
        User admin = User.builder().id(UUID.randomUUID()).name("Admin").role(Role.ADMIN).build();
        User otherClaimant = User.builder().id(UUID.randomUUID()).name("Other").role(Role.USER).build();

        foundItem.setStatus(ItemStatus.CLAIMED);

        Claim winning = Claim.builder()
                .id(UUID.randomUUID()).item(foundItem).claimedBy(claimant)
                .status(ClaimStatus.PENDING).build();
        Claim competing = Claim.builder()
                .id(UUID.randomUUID()).item(foundItem).claimedBy(otherClaimant)
                .status(ClaimStatus.PENDING).build();

        when(claimRepository.findById(winning.getId())).thenReturn(Optional.of(winning));
        when(claimRepository.findByItemIdAndStatus(foundItem.getId(), ClaimStatus.PENDING))
                .thenReturn(List.of(winning, competing));

        ClaimResponse response = claimService.reviewClaim(
                winning.getId(), new ClaimReviewRequest(ClaimStatus.APPROVED), admin);

        assertThat(response.status()).isEqualTo(ClaimStatus.APPROVED);
        assertThat(foundItem.getStatus()).isEqualTo(ItemStatus.RESOLVED);
        assertThat(competing.getStatus()).isEqualTo(ClaimStatus.REJECTED);
        assertThat(competing.getReviewedBy()).isEqualTo(admin);
    }

    @Test
    void rejectingTheOnlyClaimReopensTheItem() {
        User admin = User.builder().id(UUID.randomUUID()).name("Admin").role(Role.ADMIN).build();
        foundItem.setStatus(ItemStatus.CLAIMED);

        Claim onlyClaim = Claim.builder()
                .id(UUID.randomUUID()).item(foundItem).claimedBy(claimant)
                .status(ClaimStatus.PENDING).build();

        when(claimRepository.findById(onlyClaim.getId())).thenReturn(Optional.of(onlyClaim));
        when(claimRepository.findByItemIdAndStatus(foundItem.getId(), ClaimStatus.PENDING))
                .thenReturn(List.of(onlyClaim));

        claimService.reviewClaim(onlyClaim.getId(), new ClaimReviewRequest(ClaimStatus.REJECTED), admin);

        assertThat(onlyClaim.getStatus()).isEqualTo(ClaimStatus.REJECTED);
        assertThat(foundItem.getStatus()).isEqualTo(ItemStatus.OPEN);
    }

    @Test
    void cannotReviewAnAlreadyDecidedClaim() {
        User admin = User.builder().id(UUID.randomUUID()).name("Admin").role(Role.ADMIN).build();
        Claim decided = Claim.builder()
                .id(UUID.randomUUID()).item(foundItem).claimedBy(claimant)
                .status(ClaimStatus.APPROVED).build();

        when(claimRepository.findById(decided.getId())).thenReturn(Optional.of(decided));

        assertThatThrownBy(() -> claimService.reviewClaim(
                decided.getId(), new ClaimReviewRequest(ClaimStatus.REJECTED), admin))
                .isInstanceOf(InvalidStateException.class)
                .hasMessageContaining("already been reviewed");
    }
}
