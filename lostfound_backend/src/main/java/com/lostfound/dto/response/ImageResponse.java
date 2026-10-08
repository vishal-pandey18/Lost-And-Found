package com.lostfound.dto.response;

import com.lostfound.entity.ItemImage;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ImageResponse(Long id, String imageUrl, LocalDateTime uploadedAt) {
    public static ImageResponse from(ItemImage image) {
        return ImageResponse.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .uploadedAt(image.getUploadedAt())
                .build();
    }
}
