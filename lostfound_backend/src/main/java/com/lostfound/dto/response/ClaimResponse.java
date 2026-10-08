package com.lostfound.dto.response;

import com.lostfound.entity.Claim;
import com.lostfound.entity.enums.ClaimStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ClaimResponse(
        UUID id,
        UUID itemId,
        String itemTitle,
        UUID claimedById,
        String claimedByName,
        String proofDescription,
        ClaimStatus status,
        String reviewedByName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ClaimResponse from(Claim claim) {
        return ClaimResponse.builder()
                .id(claim.getId())
                .itemId(claim.getItem().getId())
                .itemTitle(claim.getItem().getTitle())
                .claimedById(claim.getClaimedBy().getId())
                .claimedByName(claim.getClaimedBy().getName())
                .proofDescription(claim.getProofDescription())
                .status(claim.getStatus())
                .reviewedByName(claim.getReviewedBy() != null ? claim.getReviewedBy().getName() : null)
                .createdAt(claim.getCreatedAt())
                .updatedAt(claim.getUpdatedAt())
                .build();
    }
}
