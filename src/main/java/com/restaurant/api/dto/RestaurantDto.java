package com.restaurant.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

public class RestaurantDto {

    public record CreateRequest(
            @Schema(description = "Restaurant name") @NotBlank String name,
            @Schema(description = "Address") @NotBlank String address,
            @Schema(description = "Business registration number") @NotBlank String businessNumber,
            @Schema(description = "Owner user code") @NotBlank String userCode,
            @Schema(description = "Phone number") String phone,
            @Schema(description = "Email address") String email,
            @Schema(description = "Currency code e.g. KRW, USD") String currency,
            @Schema(description = "File code for logo image") String fileCode
    ) {}

    public record PatchRequest(
            String name,
            String address,
            String businessNumber,
            String phone,
            String email,
            String currency,
            String fileCode
    ) {}

    public record SearchRequest(
            String name
    ) {}

    /** Plaintext Fonepay credentials submitted from the restaurant's admin settings. */
    public record FonepayCredentialsRequest(
            @NotBlank String merchantCode,
            @NotBlank String username,
            @NotBlank String password,
            @NotBlank String secretKey,
            boolean enabled
    ) {}

    /** Plaintext eSewa credentials submitted from the restaurant's admin settings. */
    public record EsewaCredentialsRequest(
            @NotBlank String productCode,
            @NotBlank String secretKey,
            boolean enabled
    ) {}

    @Getter
    @Setter
    public static class Response {
        private String code;
        private String kioskCode;
        private String userCode;
        private String name;
        private String address;
        private String businessNumber;
        private String phone;
        private String email;
        private String currency;
        private String fileCode;
        private Boolean isActive;
        // Payment config flags only — credentials/secrets are NEVER exposed here.
        private Boolean fonepayEnabled;
        private Boolean fonepayConfigured;
        private Boolean esewaEnabled;
        private Boolean esewaConfigured;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
