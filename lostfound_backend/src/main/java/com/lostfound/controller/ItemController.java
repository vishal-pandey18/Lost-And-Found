package com.lostfound.controller;

import com.lostfound.dto.request.ItemCreateRequest;
import com.lostfound.dto.request.ItemUpdateRequest;
import com.lostfound.dto.response.ItemResponse;
import com.lostfound.entity.User;
import com.lostfound.entity.enums.ItemStatus;
import com.lostfound.entity.enums.ItemType;
import com.lostfound.security.CurrentUserProvider;
import com.lostfound.security.UserPrincipal;
import com.lostfound.service.ItemService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Tag(name = "Items")
@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    public ResponseEntity<ItemResponse> createItem(
            @Valid @RequestBody ItemCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User currentUser = currentUserProvider.resolve(principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.createItem(request, currentUser));
    }

    @GetMapping
    public Page<ItemResponse> searchItems(
            @RequestParam(required = false) ItemType itemType,
            @RequestParam(required = false) ItemStatus status,
            @RequestParam(required = false) Integer categoryId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return itemService.searchItems(itemType, status, categoryId, pageable);
    }

    @GetMapping("/{id}")
    public ItemResponse getItem(@PathVariable UUID id) {
        return itemService.getItemById(id);
    }

    @PatchMapping("/{id}")
    public ItemResponse updateItem(
            @PathVariable UUID id,
            @RequestBody ItemUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User currentUser = currentUserProvider.resolve(principal);
        return itemService.updateItem(id, request, currentUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User currentUser = currentUserProvider.resolve(principal);
        itemService.deleteItem(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/images", consumes = "multipart/form-data")
    public ItemResponse uploadImage(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User currentUser = currentUserProvider.resolve(principal);
        return itemService.addImage(id, file, currentUser);
    }
}
