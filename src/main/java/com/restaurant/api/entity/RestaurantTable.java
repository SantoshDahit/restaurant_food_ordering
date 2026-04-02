package com.restaurant.api.entity;

import com.restaurant.api.entity.base.BaseFullTimeEntity;
import com.restaurant.api.constant.TableStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "restaurant_table")
public class RestaurantTable extends BaseFullTimeEntity {

    @Id
    @Column(name = "code")
    private String code;

    @Column(name = "restaurant_code", nullable = false)
    private String restaurantCode;

    @Column(name = "table_number", nullable = false, length = 20)
    private String tableNumber;

    @Column(name = "capacity", nullable = false)
    private Integer capacity = 4;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TableStatus status = TableStatus.AVAILABLE;

    @Column(name = "qr_code_url")
    private String qrCodeUrl;

    @Column(name = "qr_code_token", unique = true, length = 100)
    private String qrCodeToken;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public RestaurantTable(String restaurantCode, String tableNumber, Integer capacity) {
        this.code = UUID.randomUUID().toString();
        this.restaurantCode = restaurantCode;
        this.tableNumber = tableNumber;
        this.capacity = capacity != null ? capacity : 4;
    }

    public void update(String tableNumber, Integer capacity, TableStatus status) {
        if (tableNumber != null) this.tableNumber = tableNumber;
        if (capacity != null) this.capacity = capacity;
        if (status != null) this.status = status;
    }

    public void updateQrCode(String qrCodeToken, String qrCodeUrl) {
        this.qrCodeToken = qrCodeToken;
        this.qrCodeUrl = qrCodeUrl;
    }

    public void updateStatus(TableStatus status) {
        this.status = status;
    }

    public void deactivate() {
        this.isActive = false;
        this.softDelete();
    }
}
