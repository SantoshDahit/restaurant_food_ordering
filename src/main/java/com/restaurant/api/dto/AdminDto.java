package com.restaurant.api.dto;

import com.restaurant.api.constant.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AdminDto {

    @Getter
    @AllArgsConstructor
    public static class PlatformStats {
        private long totalRestaurants;
        private long activeRestaurants;
        private long totalUsers;
        private long totalManagers;
        private long totalStaff;
        private long totalOrders;
        private long ordersToday;
        private BigDecimal totalRevenue;
        private BigDecimal revenueToday;
    }

    @Getter
    @AllArgsConstructor
    public static class RestaurantOverview {
        private long totalOrders;
        private BigDecimal totalRevenue;
        private long activeStaffCount;
        private LocalDateTime lastOrderAt;
        private List<AnalyticsDto.TopItem> topItems;
    }

    public record RoleChangeRequest(@NotNull UserRole role) {}

    public record ActiveChangeRequest(@NotNull Boolean active) {}
}
