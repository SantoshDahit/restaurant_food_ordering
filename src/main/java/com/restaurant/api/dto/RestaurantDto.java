package com.restaurant.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.time.LocalDateTime;

public class RestaurantDto {

    public record CreateRequest(
            String code,
            @NotBlank String name,
            @NotBlank String address,
            @NotBlank String businessNumber,
            String phone,
            String email,
            String currency,
            String fileCode
    ) {}

    public record PatchRequest(
            String name,
            String address,
            String phone,
            String email,
            String currency,
            String fileCode
    ) {}

    public record SearchRequest(
            String name
    ) {}

    @Getter
    public static class Response {
        private String code;
        private String userCode;
        private String name;
        private String address;
        private String businessNumber;
        private String phone;
        private String email;
        private String currency;
        private String fileCode;
        private Boolean isActive;
        private LocalDateTime createAt;
        private LocalDateTime updateAt;
    }
}
