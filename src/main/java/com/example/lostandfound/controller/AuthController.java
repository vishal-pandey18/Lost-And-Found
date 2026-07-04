package com.example.lostandfound.controller;

import com.example.lostandfound.dto.auth.LoginRequest;
import com.example.lostandfound.dto.auth.RegisterRequest;
import com.example.lostandfound.dto.user.UserResponse;
import com.example.lostandfound.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        // Note: no JWT/session yet (see roadmap "Future Enhancements").
        // The frontend should hold onto the returned user id and send it
        // as a request param (e.g. userId=) on subsequent write requests.
        return ResponseEntity.ok(userService.login(request));
    }
}
