package com.restaurant.api.dto;

import com.restaurant.api.constant.PaymentMethod;
import com.restaurant.api.constant.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentDto {

    public record CreateRequest(
            @NotBlank String restaurantCode,
            @NotBlank String orderCode,
            String processedBy,
            @NotNull PaymentMethod paymentMethod,
            @NotNull BigDecimal amount,
            String transactionRef,
            String receiptNumber
    ) {}

    public record StatusUpdateRequest(
            @NotNull PaymentStatus status,
            BigDecimal refundedAmount
    ) {}

    public record SearchRequest(
            String restaurantCode,
            PaymentStatus status,
            PaymentMethod paymentMethod
    ) {}

    @Getter
    public static class Response {
        private String code;
        private String restaurantCode;
        private String orderCode;
        private String processedBy;
        private PaymentMethod paymentMethod;
        private BigDecimal amount;
        private PaymentStatus status;
        private String transactionRef;
        private String receiptNumber;
        private BigDecimal refundedAmount;
        private LocalDateTime processedAt;
        private LocalDateTime createAt;
        private LocalDateTime updateAt;
    }
}
