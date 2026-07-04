package com.example.lostandfound.dto.search;

import com.example.lostandfound.entity.FoundItem;
import com.example.lostandfound.entity.LostItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * A normalized shape so the search results page can render lost and
 * found items side by side with the same item card component.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultItem {
    private Long id;
    private String type;       // "LOST" or "FOUND"
    private String title;
    private String description;
    private String category;
    private String color;
    private String location;
    private LocalDate date;
    private String image;
    private String status;
    private Long userId;
    private String reporterName;

    public static SearchResultItem fromLost(LostItem item) {
        return new SearchResultItem(
                item.getId(), "LOST", item.getTitle(), item.getDescription(),
                item.getCategory(), item.getColor(), item.getLocation(),
                item.getLostDate(), item.getImage(), item.getStatus().name(),
                item.getUser().getId(), item.getUser().getName()
        );
    }

    public static SearchResultItem fromFound(FoundItem item) {
        return new SearchResultItem(
                item.getId(), "FOUND", item.getTitle(), item.getDescription(),
                item.getCategory(), item.getColor(), item.getLocation(),
                item.getFoundDate(), item.getImage(), item.getStatus().name(),
                item.getUser().getId(), item.getUser().getName()
        );
    }
}
