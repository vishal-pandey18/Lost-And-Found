package com.example.lostandfound.dto.user;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String name;

    @Email(message = "email must be valid")
    private String email;

    private String phone;

    // Optional - only updated if provided
    private String newPassword;
}
