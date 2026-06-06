package com.restaurant.api.dto;

import com.restaurant.api.constant.PaymentMethod;
import com.restaurant.api.constant.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

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

    /** Kick off an eSewa payment. The frontend supplies its own return URLs. */
    public record EsewaInitiateRequest(
            @NotBlank String restaurantCode,
            @NotBlank String orderCode,
            @NotNull BigDecimal amount,
            @NotBlank String successUrl,
            @NotBlank String failureUrl
    ) {}

    /** eSewa redirect-back payload (base64 JSON eSewa appends as ?data=). */
    public record EsewaVerifyRequest(
            @NotBlank String data
    ) {}

    /** Roll back an order whose eSewa payment failed or was cancelled. */
    public record EsewaCancelRequest(
            @NotBlank String orderCode
    ) {}

    /** Start a Fonepay dynamic-QR payment for an order. */
    public record FonepayInitiateRequest(
            @NotBlank String restaurantCode,
            @NotBlank String orderCode
    ) {}

    /** Poll the settled status of a Fonepay payment by its PRN (= payment code). */
    public record FonepayVerifyRequest(
            @NotBlank String prn
    ) {}

    /** The QR the frontend renders, plus the PRN to poll for completion. */
    @Getter
    public static class FonepayInitiateResponse {
        private final String paymentCode;
        private final String prn;
        private final String qrMessage;
        private final String websocketUrl;
        private final BigDecimal amount;

        public FonepayInitiateResponse(String paymentCode, String prn, String qrMessage,
                                       String websocketUrl, BigDecimal amount) {
            this.paymentCode = paymentCode;
            this.prn = prn;
            this.qrMessage = qrMessage;
            this.websocketUrl = websocketUrl;
            this.amount = amount;
        }
    }

    /**
     * Everything the browser needs to POST the customer over to eSewa's
     * hosted form: the target URL plus the exact (signed) field set.
     */
    @Getter
    public static class EsewaInitiateResponse {
        private final String paymentCode;
        private final String formUrl;
        private final Map<String, String> fields;

        public EsewaInitiateResponse(String paymentCode, String formUrl, Map<String, String> fields) {
            this.paymentCode = paymentCode;
            this.formUrl = formUrl;
            this.fields = fields;
        }
    }

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
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
