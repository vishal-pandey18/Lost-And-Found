package com.lostfound.repository;

import com.lostfound.entity.Claim;
import com.lostfound.entity.enums.ClaimStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClaimRepository extends JpaRepository<Claim, UUID> {

    Page<Claim> findByStatus(ClaimStatus status, Pageable pageable);

    List<Claim> findByItemIdAndStatus(UUID itemId, ClaimStatus status);

    Page<Claim> findByClaimedByIdOrderByCreatedAtDesc(UUID claimedById, Pageable pageable);

    boolean existsByItemIdAndClaimedByIdAndStatus(UUID itemId, UUID claimedById, ClaimStatus status);
}
