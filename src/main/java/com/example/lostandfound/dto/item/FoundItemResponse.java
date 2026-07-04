package com.example.lostandfound.dto.item;

import com.example.lostandfound.entity.FoundItem;
import com.example.lostandfound.entity.enums.FoundStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoundItemResponse {
    private Long id;
    private String title;
    private String description;
    private String category;
    private String color;
    private String location;
    private LocalDate foundDate;
    private String image;
    private FoundStatus status;
    private Long userId;
    private String reporterName;
    private LocalDateTime createdAt;

    public static FoundItemResponse from(FoundItem item) {
        return new FoundItemResponse(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getCategory(),
                item.getColor(),
                item.getLocation(),
                item.getFoundDate(),
                item.getImage(),
                item.getStatus(),
                item.getUser().getId(),
                item.getUser().getName(),
                item.getCreatedAt()
        );
    }
}
