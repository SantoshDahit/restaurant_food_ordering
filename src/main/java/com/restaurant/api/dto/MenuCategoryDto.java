package com.restaurant.api.dto;

import com.restaurant.api.constant.MenuCategoryType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.time.LocalDateTime;

public class MenuCategoryDto {

    public record CreateRequest(
            @NotBlank String restaurantCode,
            @NotBlank String name,
            MenuCategoryType categoryType,
            String fileCode,
            Integer sortOrder
    ) {}

    public record PatchRequest(
            String name,
            MenuCategoryType categoryType,
            String fileCode,
            Integer sortOrder
    ) {}

    public record SearchRequest(
            String restaurantCode,
            MenuCategoryType categoryType
    ) {}

    @Getter
    public static class Response {
        private String code;
        private String restaurantCode;
        private String name;
        private MenuCategoryType categoryType;
        private String fileCode;
        private Integer sortOrder;
        private Boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
