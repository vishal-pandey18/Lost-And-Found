package com.lostfound.controller;

import com.lostfound.dto.request.ClaimCreateRequest;
import com.lostfound.dto.request.ClaimReviewRequest;
import com.lostfound.dto.response.ClaimResponse;
import com.lostfound.entity.User;
import com.lostfound.entity.enums.ClaimStatus;
import com.lostfound.security.CurrentUserProvider;
import com.lostfound.security.UserPrincipal;
import com.lostfound.service.ClaimService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Claims")
@RestController
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;
    private final CurrentUserProvider currentUserProvider;

    // Nested under the item because a claim only makes sense in that context.
    @PostMapping("/api/v1/items/{itemId}/claims")
    public ResponseEntity<ClaimResponse> createClaim(
            @PathVariable UUID itemId,
            @Valid @RequestBody ClaimCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User currentUser = currentUserProvider.resolve(principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(claimService.createClaim(itemId, request, currentUser));
    }

    // Admin moderation queue.
    @GetMapping("/api/v1/claims")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<ClaimResponse> getClaims(
            @RequestParam(required = false) ClaimStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return claimService.getClaimsByStatus(status, pageable);
    }

    // A user's own claim history.
    @GetMapping("/api/v1/claims/mine")
    public Page<ClaimResponse> getMyClaims(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        User currentUser = currentUserProvider.resolve(principal);
        return claimService.getMyClaims(currentUser, pageable);
    }

    // Claimant or admin - ownership checked in the service.
    @GetMapping("/api/v1/claims/{id}")
    public ClaimResponse getClaim(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User currentUser = currentUserProvider.resolve(principal);
        return claimService.getClaimById(id, currentUser);
    }

    // Approving auto-resolves the item and auto-rejects competing claims.
    @PatchMapping("/api/v1/claims/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ClaimResponse reviewClaim(
            @PathVariable UUID id,
            @Valid @RequestBody ClaimReviewRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User adminUser = currentUserProvider.resolve(principal);
        return claimService.reviewClaim(id, request, adminUser);
    }
}
