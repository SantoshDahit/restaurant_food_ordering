package com.restaurant.api.entity;

import com.restaurant.api.constant.ItemAvailability;
import com.restaurant.api.entity.base.BaseFullTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "menu_item")
public class MenuItem extends BaseFullTimeEntity {

    @Id
    @Column(name = "code")
    private String code;

    @Column(name = "restaurant_code", nullable = false)
    private String restaurantCode;

    @Column(name = "category_code")
    private String categoryCode;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "discount_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Column(name = "file_code")
    private String fileCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability", nullable = false)
    private ItemAvailability availability = ItemAvailability.AVAILABLE;

    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;

    @Column(name = "is_veg", nullable = false)
    private Boolean isVeg = false;

    @Column(name = "prep_time_minutes", nullable = false)
    private Integer prepTimeMinutes = 15;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    public MenuItem(String code, String restaurantCode, String categoryCode, String name,
                    String description, BigDecimal price, BigDecimal discountPercent,
                    String fileCode, Boolean isVeg, Integer prepTimeMinutes, Integer sortOrder,
                    ItemAvailability availability) {
        this.code = code;
        this.restaurantCode = restaurantCode;
        this.categoryCode = categoryCode;
        this.name = name;
        this.description = description;
        this.price = price;
        this.discountPercent = discountPercent != null ? discountPercent : BigDecimal.ZERO;
        this.fileCode = fileCode;
        this.isVeg = isVeg != null ? isVeg : false;
        this.prepTimeMinutes = prepTimeMinutes != null ? prepTimeMinutes : 15;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
        if (availability != null) this.availability = availability;
    }

    public void update(String name, String description, BigDecimal price, BigDecimal discountPercent,
                       String fileCode, ItemAvailability availability, Boolean isFeatured,
                       Boolean isVeg, Integer prepTimeMinutes, Integer sortOrder, String categoryCode) {
        if (name != null) this.name = name;
        if (description != null) this.description = description;
        if (price != null) this.price = price;
        if (discountPercent != null) this.discountPercent = discountPercent;
        if (fileCode != null) this.fileCode = fileCode;
        if (availability != null) this.availability = availability;
        if (isFeatured != null) this.isFeatured = isFeatured;
        if (isVeg != null) this.isVeg = isVeg;
        if (prepTimeMinutes != null) this.prepTimeMinutes = prepTimeMinutes;
        if (sortOrder != null) this.sortOrder = sortOrder;
        if (categoryCode != null) this.categoryCode = categoryCode;
    }

    public void delete() {
        this.softDelete();
    }
}
