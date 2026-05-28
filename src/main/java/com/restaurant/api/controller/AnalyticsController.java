package com.restaurant.api.controller;

import com.restaurant.api.dto.AnalyticsDto;
import com.restaurant.api.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    @GetMapping("/revenue")
    public AnalyticsDto.RevenueSeries revenue(
            @RequestParam String restaurantCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return analyticsService.revenueSeries(restaurantCode, from, to);
    }

    @GetMapping("/top-items")
    public List<AnalyticsDto.TopItem> topItems(
            @RequestParam String restaurantCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false, defaultValue = "5") Integer limit
    ) {
        return analyticsService.topItems(restaurantCode, from, to, limit);
    }
}
