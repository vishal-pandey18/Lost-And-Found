package com.example.lostandfound.controller;

import com.example.lostandfound.dto.item.LostItemRequest;
import com.example.lostandfound.dto.item.LostItemResponse;
import com.example.lostandfound.service.LostItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/lost")
@RequiredArgsConstructor
public class LostItemController {

    private final LostItemService lostItemService;

    // multipart/form-data: fields (title, description, category, color,
    // location, lostDate) + optional "image" file part
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<LostItemResponse> create(
            @RequestParam Long userId,
            @Valid @ModelAttribute LostItemRequest request,
            @RequestParam(required = false) MultipartFile image) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lostItemService.create(userId, request, image));
    }

    @GetMapping
    public List<LostItemResponse> getAll() {
        return lostItemService.getAll();
    }

    @GetMapping("/{id}")
    public LostItemResponse getById(@PathVariable Long id) {
        return lostItemService.getById(id);
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public LostItemResponse update(
            @PathVariable Long id,
            @RequestParam Long userId,
            @Valid @ModelAttribute LostItemRequest request,
            @RequestParam(required = false) MultipartFile image) {
        return lostItemService.update(id, userId, request, image);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam Long userId) {
        lostItemService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/resolve")
    public LostItemResponse markResolved(@PathVariable Long id, @RequestParam Long userId) {
        return lostItemService.markResolved(id, userId);
    }
}
