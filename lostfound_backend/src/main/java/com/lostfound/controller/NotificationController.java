package com.lostfound.controller;

import com.lostfound.dto.response.NotificationResponse;
import com.lostfound.entity.User;
import com.lostfound.security.CurrentUserProvider;
import com.lostfound.security.UserPrincipal;
import com.lostfound.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Tag(name = "Notifications")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public Page<NotificationResponse> getMyNotifications(
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        User currentUser = currentUserProvider.resolve(principal);
        return notificationService.getMyNotifications(currentUser, unreadOnly, pageable);
    }

    @GetMapping("/unread-count")
    public Map<String, Long> getUnreadCount(@AuthenticationPrincipal UserPrincipal principal) {
        User currentUser = currentUserProvider.resolve(principal);
        return Map.of("unreadCount", notificationService.getUnreadCount(currentUser));
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markAsRead(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User currentUser = currentUserProvider.resolve(principal);
        return notificationService.markAsRead(id, currentUser);
    }

    @PatchMapping("/read-all")
    public Map<String, Integer> markAllAsRead(@AuthenticationPrincipal UserPrincipal principal) {
        User currentUser = currentUserProvider.resolve(principal);
        return Map.of("markedAsRead", notificationService.markAllAsRead(currentUser));
    }
}
