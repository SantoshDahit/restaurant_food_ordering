package com.restaurant.api.entity;

import com.restaurant.api.entity.base.BaseFullTimeEntity;
import com.restaurant.api.constant.OrderStatus;
import com.restaurant.api.constant.OrderType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "orders")
public class Orders extends BaseFullTimeEntity {

    @Id
    @Column(name = "code")
    private String code;

    @Column(name = "restaurant_code", nullable = false)
    private String restaurantCode;

    @Column(name = "table_code")
    private String tableCode;

    @Column(name = "waiter_code")
    private String waiterCode;

    @Column(name = "order_number", nullable = false, unique = true, length = 30)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType = OrderType.DINE_IN;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "special_notes", columnDefinition = "TEXT")
    private String specialNotes;

    @Column(name = "device_type", length = 30)
    private String deviceType;

    public Orders(String restaurantCode, String tableCode, String waiterCode,
                  String orderNumber, OrderType orderType, String specialNotes, String deviceType) {
        this.code = UUID.randomUUID().toString();
        this.restaurantCode = restaurantCode;
        this.tableCode = tableCode;
        this.waiterCode = waiterCode;
        this.orderNumber = orderNumber;
        this.orderType = orderType != null ? orderType : OrderType.DINE_IN;
        this.specialNotes = specialNotes;
        this.deviceType = deviceType;
    }

    public void updateStatus(OrderStatus status) {
        this.status = status;
    }

    public void updateAmounts(BigDecimal subtotal, BigDecimal discountAmount,
                               BigDecimal taxAmount, BigDecimal totalAmount) {
        this.subtotal = subtotal;
        this.discountAmount = discountAmount;
        this.taxAmount = taxAmount;
        this.totalAmount = totalAmount;
    }

    public void cancel() {
        this.status = OrderStatus.CANCELLED;
        this.softDelete();
    }
}
