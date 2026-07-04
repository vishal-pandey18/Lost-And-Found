package com.example.lostandfound.repository;

import com.example.lostandfound.entity.LostItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LostItemRepository extends JpaRepository<LostItem, Long>, JpaSpecificationExecutor<LostItem> {
    long countByStatus(com.example.lostandfound.entity.enums.LostStatus status);
}
