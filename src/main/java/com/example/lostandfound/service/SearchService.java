package com.example.lostandfound.service;

import com.example.lostandfound.dto.search.SearchResultItem;
import com.example.lostandfound.entity.FoundItem;
import com.example.lostandfound.entity.LostItem;
import com.example.lostandfound.repository.FoundItemRepository;
import com.example.lostandfound.repository.LostItemRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final LostItemRepository lostItemRepository;
    private final FoundItemRepository foundItemRepository;

    /**
     * Unified search across lost and found reports.
     *
     * @param keyword  matched against item title (case-insensitive, partial)
     * @param category exact match, optional
     * @param color    exact match, optional
     * @param location partial match, optional
     * @param date     exact date match (lostDate or foundDate), optional
     * @param status   "LOST"/"RESOLVED" or "FOUND"/"RETURNED", optional
     * @param type     "LOST", "FOUND", or null/"ALL" for both
     */
    public List<SearchResultItem> search(String keyword, String category, String color,
                                          String location, LocalDate date, String status, String type) {

        List<SearchResultItem> results = new ArrayList<>();

        boolean includeLost = type == null || type.isBlank() || type.equalsIgnoreCase("LOST") || type.equalsIgnoreCase("ALL");
        boolean includeFound = type == null || type.isBlank() || type.equalsIgnoreCase("FOUND") || type.equalsIgnoreCase("ALL");

        if (includeLost) {
            Specification<LostItem> spec = buildLostSpec(keyword, category, color, location, date, status);
            results.addAll(lostItemRepository.findAll(spec).stream()
                    .map(SearchResultItem::fromLost)
                    .toList());
        }

        if (includeFound) {
            Specification<FoundItem> spec = buildFoundSpec(keyword, category, color, location, date, status);
            results.addAll(foundItemRepository.findAll(spec).stream()
                    .map(SearchResultItem::fromFound)
                    .toList());
        }

        // Most recent items first
        results.sort(Comparator.comparing(SearchResultItem::getDate,
                Comparator.nullsLast(Comparator.reverseOrder())));

        return results;
    }

    private Specification<LostItem> buildLostSpec(String keyword, String category, String color,
                                                    String location, LocalDate date, String status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(keyword)) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"));
            }
            if (StringUtils.hasText(category)) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (StringUtils.hasText(color)) {
                predicates.add(cb.equal(root.get("color"), color));
            }
            if (StringUtils.hasText(location)) {
                predicates.add(cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%"));
            }
            if (date != null) {
                predicates.add(cb.equal(root.get("lostDate"), date));
            }
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status").as(String.class), status.toUpperCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<FoundItem> buildFoundSpec(String keyword, String category, String color,
                                                       String location, LocalDate date, String status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(keyword)) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"));
            }
            if (StringUtils.hasText(category)) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (StringUtils.hasText(color)) {
                predicates.add(cb.equal(root.get("color"), color));
            }
            if (StringUtils.hasText(location)) {
                predicates.add(cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%"));
            }
            if (date != null) {
                predicates.add(cb.equal(root.get("foundDate"), date));
            }
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status").as(String.class), status.toUpperCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
