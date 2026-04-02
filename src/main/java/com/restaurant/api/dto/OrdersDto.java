package com.restaurant.api.dto;

import com.restaurant.api.constant.OrderStatus;
import com.restaurant.api.constant.OrderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrdersDto {

    public record CreateRequest(
            @NotBlank String restaurantCode,
            String tableCode,
            String waiterCode,
            @NotNull OrderType orderType,
            String specialNotes,
            String deviceType
    ) {}

    public record StatusUpdateRequest(
            @NotNull OrderStatus status
    ) {}

    public record SearchRequest(
            String restaurantCode,
            OrderStatus status,
            OrderType orderType,
            String tableCode
    ) {}

    @Getter
    public static class Response {
        private String code;
        private String restaurantCode;
        private String tableCode;
        private String waiterCode;
        private String orderNumber;
        private OrderType orderType;
        private OrderStatus status;
        private BigDecimal subtotal;
        private BigDecimal discountAmount;
        private BigDecimal taxAmount;
        private BigDecimal totalAmount;
        private String specialNotes;
        private String deviceType;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
