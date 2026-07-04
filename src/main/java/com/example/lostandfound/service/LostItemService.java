package com.example.lostandfound.service;

import com.example.lostandfound.dto.item.LostItemRequest;
import com.example.lostandfound.dto.item.LostItemResponse;
import com.example.lostandfound.entity.LostItem;
import com.example.lostandfound.entity.User;
import com.example.lostandfound.entity.enums.LostStatus;
import com.example.lostandfound.exception.BadRequestException;
import com.example.lostandfound.exception.ResourceNotFoundException;
import com.example.lostandfound.repository.LostItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LostItemService {

    private final LostItemRepository lostItemRepository;
    private final UserService userService;
    private final FileStorageService fileStorageService;

    public LostItemResponse create(Long userId, LostItemRequest request, MultipartFile image) {
        User user = userService.findUserOrThrow(userId);

        LostItem item = LostItem.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .color(request.getColor())
                .location(request.getLocation())
                .lostDate(request.getLostDate())
                .image(fileStorageService.store(image))
                .status(LostStatus.LOST)
                .user(user)
                .build();

        return LostItemResponse.from(lostItemRepository.save(item));
    }

    public List<LostItemResponse> getAll() {
        return lostItemRepository.findAll().stream()
                .map(LostItemResponse::from)
                .toList();
    }

    public LostItemResponse getById(Long id) {
        return LostItemResponse.from(findOrThrow(id));
    }

    public LostItemResponse update(Long id, Long requesterId, LostItemRequest request, MultipartFile image) {
        LostItem item = findOrThrow(id);
        assertOwner(item, requesterId);

        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setCategory(request.getCategory());
        item.setColor(request.getColor());
        item.setLocation(request.getLocation());
        item.setLostDate(request.getLostDate());

        if (image != null && !image.isEmpty()) {
            item.setImage(fileStorageService.store(image));
        }

        return LostItemResponse.from(lostItemRepository.save(item));
    }

    public void delete(Long id, Long requesterId) {
        LostItem item = findOrThrow(id);
        assertOwner(item, requesterId);
        lostItemRepository.delete(item);
    }

    public LostItemResponse markResolved(Long id, Long requesterId) {
        LostItem item = findOrThrow(id);
        assertOwner(item, requesterId);
        item.setStatus(LostStatus.RESOLVED);
        return LostItemResponse.from(lostItemRepository.save(item));
    }

    private LostItem findOrThrow(Long id) {
        return lostItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lost item not found with id: " + id));
    }

    private void assertOwner(LostItem item, Long requesterId) {
        if (!item.getUser().getId().equals(requesterId)) {
            throw new BadRequestException("You do not have permission to modify this report");
        }
    }
}
