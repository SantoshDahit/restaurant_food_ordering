package com.restaurant.api.dto;

import com.restaurant.api.constant.ItemAvailability;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MenuItemDto {

    public record CreateRequest(
            @NotBlank String restaurantCode,
            String categoryCode,
            @NotBlank String name,
            String description,
            @NotNull BigDecimal price,
            BigDecimal discountPercent,
            String fileCode,
            Boolean isVeg,
            Integer prepTimeMinutes,
            Integer sortOrder
    ) {}

    public record PatchRequest(
            String categoryCode,
            String name,
            String description,
            BigDecimal price,
            BigDecimal discountPercent,
            String fileCode,
            ItemAvailability availability,
            Boolean isFeatured,
            Boolean isVeg,
            Integer prepTimeMinutes,
            Integer sortOrder
    ) {}

    public record SearchRequest(
            String restaurantCode,
            String categoryCode,
            ItemAvailability availability,
            Boolean isFeatured,
            Boolean isVeg
    ) {}

    @Getter
    public static class Response {
        private String code;
        private String restaurantCode;
        private String categoryCode;
        private String name;
        private String description;
        private BigDecimal price;
        private BigDecimal discountPercent;
        private String fileCode;
        private ItemAvailability availability;
        private Boolean isFeatured;
        private Boolean isVeg;
        private Integer prepTimeMinutes;
        private Integer sortOrder;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
