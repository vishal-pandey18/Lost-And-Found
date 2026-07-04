package com.example.lostandfound.repository;

import com.example.lostandfound.entity.FoundItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FoundItemRepository extends JpaRepository<FoundItem, Long>, JpaSpecificationExecutor<FoundItem> {
    long countByStatus(com.example.lostandfound.entity.enums.FoundStatus status);
}
