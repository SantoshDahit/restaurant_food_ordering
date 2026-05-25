package com.restaurant.api.dto;

import com.restaurant.api.constant.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

public class UserDto {

    public record RegisterRequest(
            @NotBlank String fullName,
            @Email @NotBlank String email,
            String phone,
            @NotBlank String password,
            @NotBlank String emailVerificationCode
    ) {}

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {}

    public record PatchRequest(
            String fullName,
            String phone,
            String fileCode
    ) {}

    public record SearchRequest(
            UserRole role,
            String fullName
    ) {}

    @Getter
    public static class Response {
        private String code;
        private String fullName;
        private String email;
        private String phone;
        private UserRole role;
        private String fileCode;
        private Boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Getter
    @RequiredArgsConstructor
    public static class LoginResponse {
        private String accessToken;
        private String refreshToken;
        private Response user;

        public LoginResponse(String accessToken, String refreshToken, Response user) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.user = user;
        }
    }
}
