package com.lostfound.dto.request;

import com.lostfound.entity.enums.ClaimStatus;
import jakarta.validation.constraints.NotNull;

public record ClaimReviewRequest(
        @NotNull(message = "status is required (APPROVED or REJECTED)") ClaimStatus status
) {}
