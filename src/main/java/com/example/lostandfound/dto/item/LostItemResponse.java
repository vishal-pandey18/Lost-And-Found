package com.example.lostandfound.dto.item;

import com.example.lostandfound.entity.LostItem;
import com.example.lostandfound.entity.enums.LostStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LostItemResponse {
    private Long id;
    private String title;
    private String description;
    private String category;
    private String color;
    private String location;
    private LocalDate lostDate;
    private String image;
    private LostStatus status;
    private Long userId;
    private String reporterName;
    private LocalDateTime createdAt;

    public static LostItemResponse from(LostItem item) {
        return new LostItemResponse(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getCategory(),
                item.getColor(),
                item.getLocation(),
                item.getLostDate(),
                item.getImage(),
                item.getStatus(),
                item.getUser().getId(),
                item.getUser().getName(),
                item.getCreatedAt()
        );
    }
}
