package com.lostfound.dto.request;

import com.lostfound.entity.enums.ItemStatus;

import java.time.LocalDate;

// All fields optional - only non-null fields are applied (PATCH semantics)
public record ItemUpdateRequest(
        String title,
        String description,
        Integer categoryId,
        ItemStatus status,
        String location,
        Double latitude,
        Double longitude,
        LocalDate eventDate
) {}
