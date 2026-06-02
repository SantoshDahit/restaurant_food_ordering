package com.restaurant.api.service;

import com.restaurant.api.common.UuidUtil;
import com.restaurant.api.constant.OrderStatus;
import com.restaurant.api.dto.OrdersDto;
import com.restaurant.api.entity.OrderItem;
import com.restaurant.api.entity.Orders;
import com.restaurant.api.exception.ApiException;
import com.restaurant.api.exception.ErrorCode;
import com.restaurant.api.repository.orderitem.OrderItemRepository;
import com.restaurant.api.repository.orders.OrdersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdersService {
    private final OrdersRepository ordersRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional(readOnly = true)
    public Orders getByCode(String code) {
        return ordersRepository.findByCode(code)
                .orElseThrow(() -> new ApiException(ErrorCode.ORDER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Orders getByOrderNumber(String orderNumber) {
        return ordersRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ApiException(ErrorCode.ORDER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Page<Orders> search(OrdersDto.SearchRequest searchRequest, Pageable pageable) {
        return ordersRepository.search(searchRequest, pageable);
    }

    /**
     * Assign tickets to any orders still missing one (pre-V12 rows). Idempotent.
     * Tickets land in the order's original business day so sequences stay coherent.
     */
    @Transactional
    public int backfillTickets() {
        java.util.List<Orders> orphans = ordersRepository.findAllWithoutTicket();
        // Process in created_at order so older orders get the lower numbers per day.
        orphans.sort(java.util.Comparator.comparing(Orders::getCreatedAt));
        int issued = 0;
        for (Orders o : orphans) {
            java.time.LocalDate bd = o.getCreatedAt() != null
                    ? o.getCreatedAt().toLocalDate() : java.time.LocalDate.now();
            int next = nextTicketNumber(o.getRestaurantCode(), bd);
            o.assignTicket(next, bd);
            ordersRepository.save(o);
            issued++;
        }
        return issued;
    }

    /** Statuses that mean an order is still "live" and keeps its table occupied. */
    private static final List<OrderStatus> ACTIVE_STATUSES = List.of(
            OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.PREPARING,
            OrderStatus.READY, OrderStatus.SERVED);

    @Transactional
    public Orders create(OrdersDto.CreateRequest request) {
        // One open order per table: reject a new order while the table still has
        // an active (un-completed) order. Tableless orders (kiosk/takeaway) skip this.
        if (request.tableCode() != null
                && !ordersRepository.findActiveByTableCode(request.tableCode(), ACTIVE_STATUSES).isEmpty()) {
            throw new ApiException(ErrorCode.TABLE_HAS_ACTIVE_ORDER);
        }

        String orderNumber = "ORD-" + System.currentTimeMillis();
        Orders orders = new Orders(
                request.restaurantCode(),
                request.tableCode(),
                request.waiterCode(),
                orderNumber,
                request.orderType(),
                request.specialNotes(),
                request.deviceType()
        );
        java.time.LocalDate today = java.time.LocalDate.now();
        orders.assignTicket(nextTicketNumber(request.restaurantCode(), today), today);
        return ordersRepository.save(orders);
    }

    private static final int TICKET_MIN = 100;
    private static final int TICKET_MAX = 999;

    /** Compute the next ticket number for this restaurant on this business day. */
    public int nextTicketNumber(String restaurantCode, java.time.LocalDate businessDate) {
        int max = ordersRepository.findMaxTicketNumber(restaurantCode, businessDate).orElse(TICKET_MIN - 1);
        int next = max + 1;
        if (next > TICKET_MAX) next = TICKET_MIN; // wrap; UNIQUE-like enforcement happens in app code
        return next;
    }

    @Transactional
    public Orders updateStatus(String code, OrdersDto.StatusUpdateRequest request) {
        Orders orders = getByCode(code);
        orders.updateStatus(request.status());
        return ordersRepository.save(orders);
    }

    @Transactional
    public void delete(String code) {
        Orders orders = getByCode(code);
        orders.cancel();
        ordersRepository.save(orders);
    }

    /**
     * Cancel an unpaid order (failed/abandoned gateway payment) and release its
     * ticket. Only acts on PENDING orders — anything already CONFIRMED/PREPARING
     * or CANCELLED is left untouched. Returns the cancelled order, or null if
     * nothing was changed.
     */
    @Transactional
    public Orders cancelUnpaid(String code) {
        Orders orders = getByCode(code);
        if (orders.getStatus() != OrderStatus.PENDING) {
            return null;
        }
        orders.cancelUnpaid();
        return ordersRepository.save(orders);
    }

    @Transactional
    public Orders recalculateTotals(String orderCode, List<OrderItem> items) {
        Orders orders = getByCode(orderCode);
        BigDecimal subtotal = items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        orders.updateAmounts(subtotal, orders.getDiscountAmount(), orders.getTaxAmount(), subtotal);
        return ordersRepository.save(orders);
    }
}
