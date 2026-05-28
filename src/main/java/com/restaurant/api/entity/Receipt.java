package com.restaurant.api.entity;

import com.restaurant.api.constant.PaymentMethod;
import com.restaurant.api.constant.PaymentStatus;
import com.restaurant.api.entity.base.BaseFullTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Immutable record of a payment receipt. Issued at payment time and stores
 * snapshots of order/restaurant data so the printed receipt stays accurate
 * even if the source order is later edited or soft-deleted.
 *
 * receipt_number is the 3-digit customer-facing ticket (100-999) that resets
 * each business day per restaurant. The system-wide unique identifier is
 * `code`.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "receipt")
public class Receipt extends BaseFullTimeEntity {

    @Id
    @Column(name = "code")
    private String code;

    @Column(name = "receipt_number", nullable = false)
    private Integer receiptNumber;

    @Column(name = "business_date", nullable = false)
    private LocalDate businessDate;

    @Column(name = "restaurant_code", nullable = false)
    private String restaurantCode;

    @Column(name = "order_code", nullable = false)
    private String orderCode;

    @Column(name = "payment_code", nullable = false)
    private String paymentCode;

    @Column(name = "subtotal", nullable = false)
    private BigDecimal subtotal;

    @Column(name = "discount_amount", nullable = false)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 30)
    private PaymentStatus paymentStatus;

    @Column(name = "gateway_provider", length = 30)
    private String gatewayProvider;

    @Column(name = "gateway_transaction_id", length = 120)
    private String gatewayTransactionId;

    @Column(name = "gateway_response_raw", columnDefinition = "TEXT")
    private String gatewayResponseRaw;

    @Column(name = "restaurant_name_snapshot", nullable = false, length = 255)
    private String restaurantNameSnapshot;

    @Column(name = "order_number_snapshot", nullable = false, length = 50)
    private String orderNumberSnapshot;

    @Column(name = "table_number_snapshot", length = 20)
    private String tableNumberSnapshot;

    @Column(name = "items_json", nullable = false, columnDefinition = "LONGTEXT")
    private String itemsJson;

    @Column(name = "customer_name", length = 120)
    private String customerName;

    @Column(name = "customer_email", length = 255)
    private String customerEmail;

    @Column(name = "customer_phone", length = 40)
    private String customerPhone;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    public Receipt(
            String code,
            Integer receiptNumber,
            LocalDate businessDate,
            String restaurantCode,
            String orderCode,
            String paymentCode,
            BigDecimal subtotal,
            BigDecimal discountAmount,
            BigDecimal taxAmount,
            BigDecimal totalAmount,
            PaymentMethod paymentMethod,
            PaymentStatus paymentStatus,
            String gatewayProvider,
            String gatewayTransactionId,
            String gatewayResponseRaw,
            String restaurantNameSnapshot,
            String orderNumberSnapshot,
            String tableNumberSnapshot,
            String itemsJson,
            String customerName,
            String customerEmail,
            String customerPhone,
            String notes
    ) {
        this.code = code;
        this.receiptNumber = receiptNumber;
        this.businessDate = businessDate;
        this.restaurantCode = restaurantCode;
        this.orderCode = orderCode;
        this.paymentCode = paymentCode;
        this.subtotal = subtotal;
        this.discountAmount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        this.taxAmount = taxAmount != null ? taxAmount : BigDecimal.ZERO;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.gatewayProvider = gatewayProvider;
        this.gatewayTransactionId = gatewayTransactionId;
        this.gatewayResponseRaw = gatewayResponseRaw;
        this.restaurantNameSnapshot = restaurantNameSnapshot;
        this.orderNumberSnapshot = orderNumberSnapshot;
        this.tableNumberSnapshot = tableNumberSnapshot;
        this.itemsJson = itemsJson;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.notes = notes;
        this.issuedAt = LocalDateTime.now();
    }
}
