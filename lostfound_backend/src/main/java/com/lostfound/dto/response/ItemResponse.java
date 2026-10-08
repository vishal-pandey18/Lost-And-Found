package com.lostfound.dto.response;

import com.lostfound.entity.Item;
import com.lostfound.entity.enums.ItemStatus;
import com.lostfound.entity.enums.ItemType;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record ItemResponse(
        UUID id,
        String title,
        String description,
        CategoryResponse category,
        ItemType itemType,
        ItemStatus status,
        String location,
        Double latitude,
        Double longitude,
        LocalDate eventDate,
        UUID reportedById,
        String reportedByName,
        List<ImageResponse> images,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ItemResponse from(Item item) {
        return ItemResponse.builder()
                .id(item.getId())
                .title(item.getTitle())
                .description(item.getDescription())
                .category(CategoryResponse.from(item.getCategory()))
                .itemType(item.getItemType())
                .status(item.getStatus())
                .location(item.getLocation())
                .latitude(item.getLatitude())
                .longitude(item.getLongitude())
                .eventDate(item.getEventDate())
                .reportedById(item.getReportedBy().getId())
                .reportedByName(item.getReportedBy().getName())
                .images(item.getImages().stream().map(ImageResponse::from).toList())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
