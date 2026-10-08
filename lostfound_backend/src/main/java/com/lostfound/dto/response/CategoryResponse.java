package com.lostfound.dto.response;

import com.lostfound.entity.Category;
import lombok.Builder;

@Builder
public record CategoryResponse(Integer id, String name, String description, String iconUrl) {
    public static CategoryResponse from(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .iconUrl(category.getIconUrl())
                .build();
    }
}
