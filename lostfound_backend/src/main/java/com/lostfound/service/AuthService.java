package com.lostfound.service;

import com.lostfound.dto.request.LoginRequest;
import com.lostfound.dto.request.RefreshRequest;
import com.lostfound.dto.request.RegisterRequest;
import com.lostfound.dto.response.AuthResponse;
import com.lostfound.entity.User;
import com.lostfound.entity.enums.Role;
import com.lostfound.exception.DuplicateResourceException;
import com.lostfound.repository.UserRepository;
import com.lostfound.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .role(Role.USER)
                .verified(false)
                .build();

        User saved = userRepository.save(user);
        log.info("Registered new user {}", saved.getId());
        return issueTokens(saved);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        log.debug("User {} logged in", user.getId());
        return issueTokens(user);
    }

    public AuthResponse refresh(RefreshRequest request) {
        String token = request.refreshToken();

        if (!jwtUtil.isRefreshToken(token)) {
            throw new BadCredentialsException("Provided token is not a valid refresh token");
        }

        String email = jwtUtil.extractEmail(token);

        if (!jwtUtil.isTokenValid(token, email)) {
            throw new BadCredentialsException("Refresh token is invalid or expired, please log in again");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("User no longer exists"));

        // Only a new access token; the refresh token stays valid until it expires.
        // (Rotating + revoking refresh tokens via DB/Redis is a good v2 upgrade.)
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());
        return AuthResponse.of(newAccessToken, token);
    }

    private AuthResponse issueTokens(User user) {
        return AuthResponse.of(
                jwtUtil.generateAccessToken(user.getId(), user.getEmail()),
                jwtUtil.generateRefreshToken(user.getId(), user.getEmail())
        );
    }
}
