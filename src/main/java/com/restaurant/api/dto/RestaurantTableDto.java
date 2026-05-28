package com.restaurant.api.dto;

import com.restaurant.api.constant.TableStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;

public class RestaurantTableDto {

    public record CreateRequest(
            @NotBlank String restaurantCode,
            @NotBlank String tableNumber,
            Integer capacity
    ) {}

    public record PatchRequest(
            String tableNumber,
            Integer capacity,
            TableStatus status
    ) {}

    public record SearchRequest(
            @NotBlank String restaurantCode,
            TableStatus status
    ) {}

    @Getter
    public static class Response {
        private String code;
        private String tableCode;
        private String restaurantCode;
        private String tableNumber;
        private Integer capacity;
        private TableStatus status;
        private String qrCodeUrl;
        private String qrCodeToken;
        private Boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
