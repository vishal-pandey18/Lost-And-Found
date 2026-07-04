package com.example.lostandfound.controller;

import com.example.lostandfound.dto.admin.DashboardStatsResponse;
import com.example.lostandfound.dto.user.UserResponse;
import com.example.lostandfound.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * NOTE: These endpoints assume the caller has already been verified as an
 * admin on the frontend (role check after login). Once JWT/Spring Security
 * is added (see roadmap), lock this whole controller down with
 * @PreAuthorize("hasRole('ADMIN')") instead.
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public List<UserResponse> getAllUsers() {
        return adminService.getAllUsers();
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/lost/{id}")
    public ResponseEntity<Void> deleteLostItem(@PathVariable Long id) {
        adminService.deleteLostItem(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/found/{id}")
    public ResponseEntity<Void> deleteFoundItem(@PathVariable Long id) {
        adminService.deleteFoundItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/dashboard")
    public DashboardStatsResponse getDashboardStats() {
        return adminService.getDashboardStats();
    }
}
