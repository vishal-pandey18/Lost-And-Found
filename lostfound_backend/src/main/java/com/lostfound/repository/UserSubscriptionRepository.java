package com.lostfound.repository;

import com.lostfound.entity.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {

    List<UserSubscription> findByUserId(UUID userId);

    // Subscriptions that could match a found item: either the category matches,
    // or the subscription is category-agnostic (keyword-only).
    @Query("""
            SELECT s FROM UserSubscription s
            WHERE s.category IS NULL OR s.category.id = :categoryId
            """)
    List<UserSubscription> findPotentialMatches(@Param("categoryId") Integer categoryId);
}
