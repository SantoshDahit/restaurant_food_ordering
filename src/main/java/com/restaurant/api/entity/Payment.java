package com.restaurant.api.entity;

import com.restaurant.api.constant.PaymentMethod;
import com.restaurant.api.constant.PaymentStatus;
import com.restaurant.api.entity.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "payment")
@EntityListeners(AuditingEntityListener.class)
public class Payment extends BaseTimeEntity {

    @Id
    @Column(name = "code")
    private String code;

    @Column(name = "restaurant_code", nullable = false)
    private String restaurantCode;

    @Column(name = "order_code", nullable = false)
    private String orderCode;

    @Column(name = "processed_by")
    private String processedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "transaction_ref", length = 200)
    private String transactionRef;

    @Column(name = "receipt_number", length = 100)
    private String receiptNumber;

    @Column(name = "refunded_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    public Payment(String restaurantCode, String orderCode, String processedBy,
                   PaymentMethod paymentMethod, BigDecimal amount, String transactionRef, String receiptNumber) {
        this.code = UUID.randomUUID().toString();
        this.restaurantCode = restaurantCode;
        this.orderCode = orderCode;
        this.processedBy = processedBy;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.transactionRef = transactionRef;
        this.receiptNumber = receiptNumber;
    }

    public void complete() {
        this.status = PaymentStatus.COMPLETED;
        this.processedAt = LocalDateTime.now();
    }

    public void fail() {
        this.status = PaymentStatus.FAILED;
    }

    public void refund(BigDecimal refundedAmount) {
        this.status = PaymentStatus.REFUNDED;
        this.refundedAmount = refundedAmount;
    }

    public void updateStatus(PaymentStatus status) {
        this.status = status;
        if (status == PaymentStatus.COMPLETED) {
            this.processedAt = LocalDateTime.now();
        }
    }
}
