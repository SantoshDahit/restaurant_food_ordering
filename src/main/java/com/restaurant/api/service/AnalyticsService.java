package com.restaurant.api.service;

import com.restaurant.api.dto.AnalyticsDto;
import com.restaurant.api.repository.analytics.AnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
    private static final int DEFAULT_TOP_ITEMS = 5;
    private static final int MAX_TOP_ITEMS = 50;

    private final AnalyticsRepository analyticsRepository;

    @Transactional(readOnly = true)
    public AnalyticsDto.RevenueSeries revenueSeries(String restaurantCode, LocalDate from, LocalDate to) {
        // Group raw rows by their day for quick lookup, then walk every day in the window
        // so the chart has a point even on zero-order days.
        List<Object[]> rows = analyticsRepository.revenueByDay(restaurantCode, from, to);
        Map<LocalDate, Object[]> byDay = new HashMap<>();
        for (Object[] row : rows) {
            LocalDate day = ((Date) row[0]).toLocalDate();
            byDay.put(day, row);
        }

        List<AnalyticsDto.RevenuePoint> points = new ArrayList<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            Object[] row = byDay.get(d);
            long orderCount = row == null ? 0L : ((Number) row[1]).longValue();
            BigDecimal revenue = row == null ? BigDecimal.ZERO : (BigDecimal) row[2];
            points.add(new AnalyticsDto.RevenuePoint(d, orderCount, revenue));
        }

        Object[] totals = analyticsRepository.totals(restaurantCode, from, to);
        long totalOrders = totals[0] == null ? 0L : ((Number) totals[0]).longValue();
        BigDecimal totalRevenue = totals[1] == null ? BigDecimal.ZERO : (BigDecimal) totals[1];

        return new AnalyticsDto.RevenueSeries(from, to, totalRevenue, totalOrders, points);
    }

    @Transactional(readOnly = true)
    public List<AnalyticsDto.TopItem> topItems(String restaurantCode, LocalDate from, LocalDate to, Integer limit) {
        int n = limit == null ? DEFAULT_TOP_ITEMS : Math.min(Math.max(limit, 1), MAX_TOP_ITEMS);
        List<Object[]> rows = analyticsRepository.topItems(restaurantCode, from, to, n);
        List<AnalyticsDto.TopItem> out = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            String code = (String) row[0];
            String name = (String) row[1];
            long qty = ((Number) row[2]).longValue();
            BigDecimal revenue = (BigDecimal) row[3];
            out.add(new AnalyticsDto.TopItem(code, name, qty, revenue));
        }
        return out;
    }
}
