package com.restaurant.api.entity;

import com.restaurant.api.common.BaseFullEntity;
import com.restaurant.api.constant.TableStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "restaurant_table")
public class RestaurantTable extends BaseFullEntity {

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

    public RestaurantTable(String code, String restaurantCode, String tableNumber, Integer capacity) {
        this.code = code;
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
