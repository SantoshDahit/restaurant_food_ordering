package com.restaurant.api.entity;

import com.restaurant.api.common.BaseFullEntity;
import com.restaurant.api.constant.MenuCategoryType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "menu_category")
public class MenuCategory extends BaseFullEntity {

    @Id
    @Column(name = "code")
    private String code;

    @Column(name = "restaurant_code", nullable = false)
    private String restaurantCode;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_type")
    private MenuCategoryType categoryType;

    @Column(name = "file_code")
    private String fileCode;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public MenuCategory(String code, String restaurantCode, String name,
                        MenuCategoryType categoryType, Integer sortOrder) {
        this.code = code;
        this.restaurantCode = restaurantCode;
        this.name = name;
        this.categoryType = categoryType;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }

    public void update(String name, MenuCategoryType categoryType, String fileCode, Integer sortOrder) {
        if (name != null) this.name = name;
        if (categoryType != null) this.categoryType = categoryType;
        if (fileCode != null) this.fileCode = fileCode;
        if (sortOrder != null) this.sortOrder = sortOrder;
    }

    public void deactivate() {
        this.isActive = false;
        this.softDelete();
    }
}
