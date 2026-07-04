package com.example.lostandfound.dto.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class FoundItemRequest {

    @NotBlank(message = "title is required")
    private String title;

    private String description;
    private String category;
    private String color;
    private String location;

    @NotNull(message = "foundDate is required")
    private LocalDate foundDate;
}
