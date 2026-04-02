package com.restaurant.api.entity;

import com.restaurant.api.constant.OrderStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "order_item")
@EntityListeners(AuditingEntityListener.class)
public class OrderItem {

    @Id
    @Column(name = "code")
    private String code;

    @Column(name = "order_code", nullable = false)
    private String orderCode;

    @Column(name = "menu_item_code", nullable = false)
    private String menuItemCode;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "spice_level", length = 20)
    private String spiceLevel;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public OrderItem( String orderCode, String menuItemCode,
                     Integer quantity, BigDecimal unitPrice, BigDecimal discountAmount,
                     String spiceLevel, String notes) {
        this.code = UUID.randomUUID().toString();
        this.orderCode = orderCode;
        this.menuItemCode = menuItemCode;
        this.quantity = quantity != null ? quantity : 1;
        this.unitPrice = unitPrice;
        this.discountAmount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(this.quantity))
                .subtract(this.discountAmount);
        this.spiceLevel = spiceLevel;
        this.notes = notes;
    }

    public void update(Integer quantity, String spiceLevel, String notes) {
        if (quantity != null) {
            this.quantity = quantity;
            this.totalPrice = this.unitPrice.multiply(BigDecimal.valueOf(quantity))
                    .subtract(this.discountAmount);
        }
        if (spiceLevel != null) this.spiceLevel = spiceLevel;
        if (notes != null) this.notes = notes;
    }

    public void updateStatus(OrderStatus status) {
        this.status = status;
    }
}
