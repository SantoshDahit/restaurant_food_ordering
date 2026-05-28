package com.restaurant.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class AnalyticsDto {

    /** One point per day in the requested window. */
    public record RevenuePoint(
            LocalDate date,
            long orderCount,
            BigDecimal revenue
    ) {}

    public record RevenueSeries(
            LocalDate from,
            LocalDate to,
            BigDecimal totalRevenue,
            long totalOrders,
            List<RevenuePoint> points
    ) {}

    /** Top-selling menu item over the requested window. */
    public record TopItem(
            String menuItemCode,
            String menuItemName,
            long quantity,
            BigDecimal revenue
    ) {}
}
