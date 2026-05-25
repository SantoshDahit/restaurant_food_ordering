package com.restaurant.api.dto;

import com.restaurant.api.constant.EmailVerificationPurpose;
import com.restaurant.api.constant.EmailVerificationStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

import java.time.LocalDateTime;

public class EmailVerificationDto {

    public record SendRequest(
            @Email @NotBlank String email,
            @NotNull EmailVerificationPurpose purpose
    ) {}

    public record VerifyRequest(
            @NotBlank String code,
            @Email @NotBlank String email,
            @NotBlank @Pattern(regexp = "\\d{6}", message = "PIN must be 6 digits") String pin,
            @NotNull EmailVerificationPurpose purpose
    ) {}

    @Getter
    public static class Response {
        private String code;
        private String email;
        private EmailVerificationPurpose purpose;
        private EmailVerificationStatus status;
        private LocalDateTime verifiedAt;
        private LocalDateTime expiredAt;
        private LocalDateTime createdAt;
    }
}
