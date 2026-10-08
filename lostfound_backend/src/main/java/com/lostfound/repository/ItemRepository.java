package com.lostfound.repository;

import com.lostfound.entity.Item;
import com.lostfound.entity.enums.ItemStatus;
import com.lostfound.entity.enums.ItemType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ItemRepository extends JpaRepository<Item, UUID> {

    @Query("""
            SELECT i FROM Item i
            WHERE i.deleted = false
            AND (:itemType IS NULL OR i.itemType = :itemType)
            AND (:status IS NULL OR i.status = :status)
            AND (:categoryId IS NULL OR i.category.id = :categoryId)
            ORDER BY i.createdAt DESC
            """)
    Page<Item> search(
            @Param("itemType") ItemType itemType,
            @Param("status") ItemStatus status,
            @Param("categoryId") Integer categoryId,
            Pageable pageable
    );

    // Matching engine (Phase 5): find still-open LOST reports in the same
    // category whose title/description mentions the given keyword.
    @Query("""
            SELECT i FROM Item i
            WHERE i.deleted = false
            AND i.itemType = com.lostfound.entity.enums.ItemType.LOST
            AND i.status = com.lostfound.entity.enums.ItemStatus.OPEN
            AND i.category.id = :categoryId
            AND (LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(COALESCE(i.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    List<Item> findMatchingLostItems(
            @Param("categoryId") Integer categoryId,
            @Param("keyword") String keyword
    );
}
