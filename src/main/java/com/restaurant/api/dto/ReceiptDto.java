package com.restaurant.api.dto;

import com.restaurant.api.constant.PaymentMethod;
import com.restaurant.api.constant.PaymentStatus;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReceiptDto {

    @Getter
    public static class Response {
        private String code;
        private Integer receiptNumber;
        private LocalDate businessDate;

        private String restaurantCode;
        private String orderCode;
        private String paymentCode;

        private BigDecimal subtotal;
        private BigDecimal discountAmount;
        private BigDecimal taxAmount;
        private BigDecimal totalAmount;

        private PaymentMethod paymentMethod;
        private PaymentStatus paymentStatus;

        private String gatewayProvider;
        private String gatewayTransactionId;

        private String restaurantNameSnapshot;
        /** Live restaurant info (looked up at render time — receipt header needs these for print). */
        private String restaurantAddress;
        private String restaurantPhone;
        private String restaurantBusinessNumber;
        private String orderNumberSnapshot;
        private String tableNumberSnapshot;
        private String itemsJson;

        public void setRestaurantAddress(String v) { this.restaurantAddress = v; }
        public void setRestaurantPhone(String v) { this.restaurantPhone = v; }
        public void setRestaurantBusinessNumber(String v) { this.restaurantBusinessNumber = v; }

        private String customerName;
        private String customerEmail;
        private String customerPhone;

        private String notes;
        private LocalDateTime issuedAt;
    }
}
