package com.lostfound.controller;

import com.lostfound.dto.request.SubscriptionCreateRequest;
import com.lostfound.dto.response.SubscriptionResponse;
import com.lostfound.entity.User;
import com.lostfound.security.CurrentUserProvider;
import com.lostfound.security.UserPrincipal;
import com.lostfound.service.SubscriptionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Alert subscriptions")
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    public ResponseEntity<SubscriptionResponse> create(
            @Valid @RequestBody SubscriptionCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User currentUser = currentUserProvider.resolve(principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(subscriptionService.create(request, currentUser));
    }

    @GetMapping
    public List<SubscriptionResponse> getMySubscriptions(@AuthenticationPrincipal UserPrincipal principal) {
        User currentUser = currentUserProvider.resolve(principal);
        return subscriptionService.getMySubscriptions(currentUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User currentUser = currentUserProvider.resolve(principal);
        subscriptionService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
