package com.lostfound.dto.response;

import lombok.Builder;

@Builder
public record AuthResponse(String accessToken, String refreshToken, String tokenType) {
    public static AuthResponse of(String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }
}
