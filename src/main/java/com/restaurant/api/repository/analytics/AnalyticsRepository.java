package com.restaurant.api.repository.analytics;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Read-only analytics queries against the orders + order_item tables.
 * Native SQL keeps the date-grouping and joins clear; results land as
 * Object[] rows that the service converts into DTOs.
 */
@Repository
@RequiredArgsConstructor
public class AnalyticsRepository {
    private final EntityManager em;

    /**
     * Per-day order count + revenue for the restaurant between [from, to]
     * (inclusive). Excludes cancelled and soft-deleted orders.
     * Returns rows: [java.sql.Date day, Long orderCount, BigDecimal revenue].
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> revenueByDay(String restaurantCode, LocalDate from, LocalDate to) {
        return em.createNativeQuery("""
                SELECT DATE(o.created_at) AS day,
                       COUNT(*)            AS order_count,
                       COALESCE(SUM(o.total_amount), 0) AS revenue
                FROM orders o
                WHERE o.restaurant_code = :restaurantCode
                  AND o.created_at >= :from
                  AND o.created_at <  :toExclusive
                  AND o.status <> 'CANCELLED'
                  AND o.deleted_at IS NULL
                GROUP BY DATE(o.created_at)
                ORDER BY DATE(o.created_at)
                """)
                .setParameter("restaurantCode", restaurantCode)
                .setParameter("from", from.atStartOfDay())
                .setParameter("toExclusive", to.plusDays(1).atStartOfDay())
                .getResultList();
    }

    /**
     * Top selling menu items in the window. Aggregated quantity + revenue.
     * Returns rows: [String menuItemCode, String menuItemName, Long qty, BigDecimal revenue].
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> topItems(String restaurantCode, LocalDate from, LocalDate to, int limit) {
        return em.createNativeQuery("""
                SELECT oi.menu_item_code,
                       mi.name AS menu_item_name,
                       COALESCE(SUM(oi.quantity), 0)   AS qty,
                       COALESCE(SUM(oi.total_price), 0) AS revenue
                FROM order_item oi
                JOIN orders o     ON o.code = oi.order_code
                LEFT JOIN menu_item mi ON mi.code = oi.menu_item_code
                WHERE o.restaurant_code = :restaurantCode
                  AND o.created_at >= :from
                  AND o.created_at <  :toExclusive
                  AND o.status <> 'CANCELLED'
                  AND o.deleted_at IS NULL
                GROUP BY oi.menu_item_code, mi.name
                ORDER BY qty DESC, revenue DESC
                LIMIT :lim
                """)
                .setParameter("restaurantCode", restaurantCode)
                .setParameter("from", from.atStartOfDay())
                .setParameter("toExclusive", to.plusDays(1).atStartOfDay())
                .setParameter("lim", limit)
                .getResultList();
    }

    /** Net revenue + total orders across the window (single-row aggregate). */
    public Object[] totals(String restaurantCode, LocalDate from, LocalDate to) {
        Object result = em.createNativeQuery("""
                SELECT COUNT(*) AS order_count,
                       COALESCE(SUM(o.total_amount), 0) AS revenue
                FROM orders o
                WHERE o.restaurant_code = :restaurantCode
                  AND o.created_at >= :from
                  AND o.created_at <  :toExclusive
                  AND o.status <> 'CANCELLED'
                  AND o.deleted_at IS NULL
                """)
                .setParameter("restaurantCode", restaurantCode)
                .setParameter("from", from.atStartOfDay())
                .setParameter("toExclusive", to.plusDays(1).atStartOfDay())
                .getSingleResult();
        return result instanceof Object[] arr
                ? arr
                : new Object[] { 0L, BigDecimal.ZERO };
    }
}
