package com.example.lostandfound.controller;

import com.example.lostandfound.dto.search.SearchResultItem;
import com.example.lostandfound.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    // GET /search?keyword=wallet&category=Bags&color=Black&location=Library&date=2026-07-01&status=LOST&type=LOST
    // All params optional - omit any you don't need to filter by.
    @GetMapping("/search")
    public List<SearchResultItem> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {
        return searchService.search(keyword, category, color, location, date, status, type);
    }
}
