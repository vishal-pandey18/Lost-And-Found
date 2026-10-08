package com.lostfound.dto.request;

import com.lostfound.entity.enums.ItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ItemCreateRequest(
        @NotBlank(message = "Title is required") String title,
        String description,
        @NotNull(message = "categoryId is required") Integer categoryId,
        @NotNull(message = "itemType must be LOST or FOUND") ItemType itemType,
        String location,
        Double latitude,
        Double longitude,
        LocalDate eventDate
) {}
