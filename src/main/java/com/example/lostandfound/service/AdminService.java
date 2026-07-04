package com.example.lostandfound.service;

import com.example.lostandfound.dto.admin.DashboardStatsResponse;
import com.example.lostandfound.dto.user.UserResponse;
import com.example.lostandfound.entity.enums.FoundStatus;
import com.example.lostandfound.entity.enums.LostStatus;
import com.example.lostandfound.exception.ResourceNotFoundException;
import com.example.lostandfound.repository.FoundItemRepository;
import com.example.lostandfound.repository.LostItemRepository;
import com.example.lostandfound.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final LostItemRepository lostItemRepository;
    private final FoundItemRepository foundItemRepository;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        userRepository.deleteById(userId);
    }

    public void deleteLostItem(Long id) {
        if (!lostItemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Lost item not found with id: " + id);
        }
        lostItemRepository.deleteById(id);
    }

    public void deleteFoundItem(Long id) {
        if (!foundItemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Found item not found with id: " + id);
        }
        foundItemRepository.deleteById(id);
    }

    public DashboardStatsResponse getDashboardStats() {
        return new DashboardStatsResponse(
                userRepository.count(),
                lostItemRepository.count(),
                foundItemRepository.count(),
                lostItemRepository.countByStatus(LostStatus.RESOLVED),
                foundItemRepository.countByStatus(FoundStatus.RETURNED)
        );
    }
}
