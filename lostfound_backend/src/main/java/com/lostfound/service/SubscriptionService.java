package com.lostfound.service;

import com.lostfound.dto.request.SubscriptionCreateRequest;
import com.lostfound.dto.response.SubscriptionResponse;
import com.lostfound.entity.Category;
import com.lostfound.entity.User;
import com.lostfound.entity.UserSubscription;
import com.lostfound.exception.ResourceNotFoundException;
import com.lostfound.exception.UnauthorizedActionException;
import com.lostfound.repository.CategoryRepository;
import com.lostfound.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final UserSubscriptionRepository subscriptionRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public SubscriptionResponse create(SubscriptionCreateRequest request, User currentUser) {
        boolean hasCategory = request.categoryId() != null;
        boolean hasKeyword = request.keyword() != null && !request.keyword().isBlank();

        if (!hasCategory && !hasKeyword) {
            throw new IllegalArgumentException("Provide a categoryId, a keyword, or both");
        }

        Category category = null;
        if (hasCategory) {
            category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.categoryId()));
        }

        UserSubscription subscription = UserSubscription.builder()
                .user(currentUser)
                .category(category)
                .keyword(hasKeyword ? request.keyword().trim() : null)
                .build();

        return SubscriptionResponse.from(subscriptionRepository.save(subscription));
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getMySubscriptions(User currentUser) {
        return subscriptionRepository.findByUserId(currentUser.getId()).stream()
                .map(SubscriptionResponse::from)
                .toList();
    }

    @Transactional
    public void delete(Long id, User currentUser) {
        UserSubscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found: " + id));

        if (!subscription.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("You can only remove your own alerts");
        }
        subscriptionRepository.delete(subscription);
    }
}
