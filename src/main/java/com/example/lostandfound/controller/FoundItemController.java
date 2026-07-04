package com.example.lostandfound.controller;

import com.example.lostandfound.dto.item.FoundItemRequest;
import com.example.lostandfound.dto.item.FoundItemResponse;
import com.example.lostandfound.service.FoundItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/found")
@RequiredArgsConstructor
public class FoundItemController {

    private final FoundItemService foundItemService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<FoundItemResponse> create(
            @RequestParam Long userId,
            @Valid @ModelAttribute FoundItemRequest request,
            @RequestParam(required = false) MultipartFile image) {
        return ResponseEntity.status(HttpStatus.CREATED).body(foundItemService.create(userId, request, image));
    }

    @GetMapping
    public List<FoundItemResponse> getAll() {
        return foundItemService.getAll();
    }

    @GetMapping("/{id}")
    public FoundItemResponse getById(@PathVariable Long id) {
        return foundItemService.getById(id);
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public FoundItemResponse update(
            @PathVariable Long id,
            @RequestParam Long userId,
            @Valid @ModelAttribute FoundItemRequest request,
            @RequestParam(required = false) MultipartFile image) {
        return foundItemService.update(id, userId, request, image);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam Long userId) {
        foundItemService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/return")
    public FoundItemResponse markReturned(@PathVariable Long id, @RequestParam Long userId) {
        return foundItemService.markReturned(id, userId);
    }
}
