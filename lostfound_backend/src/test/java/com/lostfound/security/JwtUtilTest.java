package com.lostfound.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String SECRET = "test-secret-that-is-long-enough-for-hmac-sha256-at-least-32-bytes";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, 900_000L, 604_800_000L);
    }

    @Test
    void generatesAccessTokenCarryingEmailAndUserId() {
        UUID userId = UUID.randomUUID();
        String token = jwtUtil.generateAccessToken(userId, "asha@example.com");

        assertThat(jwtUtil.extractEmail(token)).isEqualTo("asha@example.com");
        assertThat(jwtUtil.extractUserId(token)).isEqualTo(userId);
        assertThat(jwtUtil.isTokenValid(token, "asha@example.com")).isTrue();
    }

    @Test
    void accessTokenIsNotTreatedAsRefreshToken() {
        String access = jwtUtil.generateAccessToken(UUID.randomUUID(), "asha@example.com");
        assertThat(jwtUtil.isRefreshToken(access)).isFalse();
    }

    @Test
    void refreshTokenIsMarkedAsRefresh() {
        String refresh = jwtUtil.generateRefreshToken(UUID.randomUUID(), "asha@example.com");
        assertThat(jwtUtil.isRefreshToken(refresh)).isTrue();
    }

    @Test
    void tokenIsInvalidForADifferentEmail() {
        String token = jwtUtil.generateAccessToken(UUID.randomUUID(), "asha@example.com");
        assertThat(jwtUtil.isTokenValid(token, "someone.else@example.com")).isFalse();
    }

    @Test
    void expiredTokenIsInvalid() {
        JwtUtil shortLived = new JwtUtil(SECRET, -1000L, -1000L);
        String token = shortLived.generateAccessToken(UUID.randomUUID(), "asha@example.com");
        assertThat(shortLived.isTokenValid(token, "asha@example.com")).isFalse();
    }
}
