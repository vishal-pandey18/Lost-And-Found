package com.lostfound.dto.request;

// At least one of categoryId / keyword must be provided - validated in the service.
public record SubscriptionCreateRequest(
        Integer categoryId,
        String keyword
) {}
