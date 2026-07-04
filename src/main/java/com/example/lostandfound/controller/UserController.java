package com.example.lostandfound.controller;

import com.example.lostandfound.dto.user.UpdateProfileRequest;
import com.example.lostandfound.dto.user.UserResponse;
import com.example.lostandfound.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public UserResponse getProfile(@PathVariable Long id) {
        return userService.getProfile(id);
    }

    @PutMapping("/{id}")
    public UserResponse updateProfile(@PathVariable Long id, @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(id, request);
    }
}
