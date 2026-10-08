package com.lostfound.dto.response;

import com.lostfound.entity.UserSubscription;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record SubscriptionResponse(
        Long id,
        Integer categoryId,
        String categoryName,
        String keyword,
        LocalDateTime createdAt
) {
    public static SubscriptionResponse from(UserSubscription s) {
        return SubscriptionResponse.builder()
                .id(s.getId())
                .categoryId(s.getCategory() != null ? s.getCategory().getId() : null)
                .categoryName(s.getCategory() != null ? s.getCategory().getName() : null)
                .keyword(s.getKeyword())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
