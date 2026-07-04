package com.example.lostandfound.service;

import com.example.lostandfound.dto.item.FoundItemRequest;
import com.example.lostandfound.dto.item.FoundItemResponse;
import com.example.lostandfound.entity.FoundItem;
import com.example.lostandfound.entity.User;
import com.example.lostandfound.entity.enums.FoundStatus;
import com.example.lostandfound.exception.BadRequestException;
import com.example.lostandfound.exception.ResourceNotFoundException;
import com.example.lostandfound.repository.FoundItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FoundItemService {

    private final FoundItemRepository foundItemRepository;
    private final UserService userService;
    private final FileStorageService fileStorageService;

    public FoundItemResponse create(Long userId, FoundItemRequest request, MultipartFile image) {
        User user = userService.findUserOrThrow(userId);

        FoundItem item = FoundItem.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .color(request.getColor())
                .location(request.getLocation())
                .foundDate(request.getFoundDate())
                .image(fileStorageService.store(image))
                .status(FoundStatus.FOUND)
                .user(user)
                .build();

        return FoundItemResponse.from(foundItemRepository.save(item));
    }

    public List<FoundItemResponse> getAll() {
        return foundItemRepository.findAll().stream()
                .map(FoundItemResponse::from)
                .toList();
    }

    public FoundItemResponse getById(Long id) {
        return FoundItemResponse.from(findOrThrow(id));
    }

    public FoundItemResponse update(Long id, Long requesterId, FoundItemRequest request, MultipartFile image) {
        FoundItem item = findOrThrow(id);
        assertOwner(item, requesterId);

        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setCategory(request.getCategory());
        item.setColor(request.getColor());
        item.setLocation(request.getLocation());
        item.setFoundDate(request.getFoundDate());

        if (image != null && !image.isEmpty()) {
            item.setImage(fileStorageService.store(image));
        }

        return FoundItemResponse.from(foundItemRepository.save(item));
    }

    public void delete(Long id, Long requesterId) {
        FoundItem item = findOrThrow(id);
        assertOwner(item, requesterId);
        foundItemRepository.delete(item);
    }

    public FoundItemResponse markReturned(Long id, Long requesterId) {
        FoundItem item = findOrThrow(id);
        assertOwner(item, requesterId);
        item.setStatus(FoundStatus.RETURNED);
        return FoundItemResponse.from(foundItemRepository.save(item));
    }

    private FoundItem findOrThrow(Long id) {
        return foundItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Found item not found with id: " + id));
    }

    private void assertOwner(FoundItem item, Long requesterId) {
        if (!item.getUser().getId().equals(requesterId)) {
            throw new BadRequestException("You do not have permission to modify this report");
        }
    }
}
