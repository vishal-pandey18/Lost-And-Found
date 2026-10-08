package com.lostfound.dto.response;

import com.lostfound.entity.Notification;
import com.lostfound.entity.enums.NotificationType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record NotificationResponse(
        UUID id,
        String title,
        String message,
        NotificationType type,
        UUID itemId,
        boolean read,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType())
                .itemId(n.getItem() != null ? n.getItem().getId() : null)
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
