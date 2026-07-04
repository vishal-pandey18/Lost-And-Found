package com.example.lostandfound.dto.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LostItemRequest {

    @NotBlank(message = "title is required")
    private String title;

    private String description;
    private String category;
    private String color;
    private String location;

    @NotNull(message = "lostDate is required")
    private LocalDate lostDate;
}
