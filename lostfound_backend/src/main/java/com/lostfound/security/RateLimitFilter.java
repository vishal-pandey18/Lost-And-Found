package com.lostfound.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Simple in-memory sliding-window rate limiter for write endpoints, to stop
// someone flooding the app with junk item/claim reports.
//
// Deliberately dependency-free (no Bucket4j) and per-instance: state lives in
// this JVM only. If you ever run more than one instance, move this to Redis so
// the limit is shared - noted as a v2 upgrade.
@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

    private final boolean enabled;
    private final int maxRequestsPerMinute;

    private final Map<String, Deque<Instant>> hits = new ConcurrentHashMap<>();
    private static final Duration WINDOW = Duration.ofMinutes(1);

    public RateLimitFilter(
            @Value("${app.rate-limit.enabled:true}") boolean enabled,
            @Value("${app.rate-limit.max-requests-per-minute:15}") int maxRequestsPerMinute
    ) {
        this.enabled = enabled;
        this.maxRequestsPerMinute = maxRequestsPerMinute;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (!enabled || !isRateLimitedRoute(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = clientKey(request);
        if (isOverLimit(key)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"status\":429,\"error\":\"Too Many Requests\","
                            + "\"message\":\"Slow down - too many submissions. Try again in a minute.\"}"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRateLimitedRoute(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/items")
                || path.startsWith("/api/v1/claims")
                || path.startsWith("/api/v1/auth/register");
    }

    private String clientKey(HttpServletRequest request) {
        // Prefer the authenticated user; fall back to IP for anonymous routes.
        Object principal = (org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication() != null)
                ? org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getPrincipal()
                : null;

        if (principal instanceof UserPrincipal up) {
            return "user:" + up.getId();
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        String ip = (forwarded != null && !forwarded.isBlank())
                ? forwarded.split(",")[0].trim()
                : request.getRemoteAddr();
        return "ip:" + ip;
    }

    private boolean isOverLimit(String key) {
        Instant now = Instant.now();
        Deque<Instant> timestamps = hits.computeIfAbsent(key, k -> new ArrayDeque<>());

        synchronized (timestamps) {
            // Drop anything outside the current window.
            while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(now.minus(WINDOW))) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= maxRequestsPerMinute) {
                return true;
            }
            timestamps.addLast(now);
            return false;
        }
    }
}
