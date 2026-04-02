package com.restaurant.api.dto;

import com.restaurant.api.constant.OrderStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderItemDto {

    public record CreateRequest(
            @NotBlank String menuItemCode,
            @NotNull @Min(1) Integer quantity,
            BigDecimal discountAmount,
            String spiceLevel,
            String notes
    ) {}

    public record PatchRequest(
            Integer quantity,
            String spiceLevel,
            String notes
    ) {}

    @Getter
    public static class Response {
        private String code;
        private String orderCode;
        private String menuItemCode;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal discountAmount;
        private BigDecimal totalPrice;
        private String spiceLevel;
        private String notes;
        private OrderStatus status;
        private LocalDateTime createdAt;
    }
}
