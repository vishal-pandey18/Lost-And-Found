package com.lostfound.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClaimCreateRequest(
        @NotBlank(message = "Please describe why this item belongs to you")
        @Size(min = 10, message = "Proof description should be at least 10 characters - vague claims are hard to review")
        String proofDescription
) {}
