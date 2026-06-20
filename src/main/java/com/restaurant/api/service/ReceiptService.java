package com.restaurant.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.api.common.UuidUtil;
import com.restaurant.api.entity.OrderItem;
import com.restaurant.api.entity.Orders;
import com.restaurant.api.entity.Payment;
import com.restaurant.api.entity.Receipt;
import com.restaurant.api.entity.Restaurant;
import com.restaurant.api.entity.RestaurantTable;
import com.restaurant.api.exception.ApiException;
import com.restaurant.api.exception.ErrorCode;
import com.restaurant.api.repository.menuitem.MenuItemRepository;
import com.restaurant.api.repository.payment.PaymentRepository;
import com.restaurant.api.repository.orders.OrdersRepository;
import com.restaurant.api.repository.receipt.ReceiptRepository;
import com.restaurant.api.repository.restaurant.RestaurantRepository;
import com.restaurant.api.repository.table.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReceiptService {
    private static final int RECEIPT_NUMBER_MIN = 100;
    private static final int RECEIPT_NUMBER_MAX = 999;

    private final ReceiptRepository receiptRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantTableRepository restaurantTableRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderItemService orderItemService;
    private final PaymentRepository paymentRepository;
    private final OrdersRepository ordersRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public Optional<Receipt> findByOrderCode(String orderCode) {
        return receiptRepository.findByOrderCode(orderCode);
    }

    @Transactional(readOnly = true)
    public Receipt getByCode(String code) {
        return receiptRepository.findByCode(code)
                .orElseThrow(() -> new ApiException(ErrorCode.RECEIPT_NOT_FOUND));
    }

    /**
     * Issue a new receipt for the given payment + order. Idempotent: if a receipt
     * already exists for the order, returns the existing one without re-issuing.
     */
    @Transactional
    public Receipt issue(Payment payment, Orders order) {
        Optional<Receipt> existing = receiptRepository.findByOrderCode(order.getCode());
        if (existing.isPresent()) return existing.get();

        Restaurant restaurant = restaurantRepository.findByCode(order.getRestaurantCode())
                .orElseThrow(() -> new ApiException(ErrorCode.RESTAURANT_NOT_FOUND));

        String tableNumber = order.getTableCode() == null ? null
                : restaurantTableRepository.findByCode(order.getTableCode())
                        .map(RestaurantTable::getTableNumber)
                        .orElse(null);

        List<OrderItem> items = orderItemService.findAllByOrderCode(order.getCode());
        String itemsJson = serializeItems(items);

        // The receipt mirrors the order's customer-facing ticket — one shared
        // number across order detail, tracking, pickup board, and printed slip.
        // Fall back to the legacy compute path if a pre-V12 order doesn't have one.
        Integer ticket = order.getTicketNumber();
        LocalDate businessDate = order.getBusinessDate() != null
                ? order.getBusinessDate()
                : (payment.getCreatedAt() != null ? payment.getCreatedAt().toLocalDate() : LocalDate.now());

        // Use the order's ticket as the receipt number when it's still free for
        // the day; otherwise (ticket reuse, or two orders sharing a number) take
        // the next free number. Pre-checking avoids the daily unique-key clash
        // deterministically — no catch-and-retry inside the transaction.
        int receiptNumber = (ticket != null && !receiptRepository.existsDaily(restaurant.getCode(), businessDate, ticket))
                ? ticket
                : nextReceiptNumber(restaurant.getCode(), businessDate);

        Receipt receipt = new Receipt(
                UuidUtil.generate(),
                receiptNumber,
                businessDate,
                restaurant.getCode(),
                order.getCode(),
                payment.getCode(),
                order.getSubtotal(),
                order.getDiscountAmount(),
                order.getTaxAmount(),
                order.getTotalAmount(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                null,                             // gatewayProvider (populated by gateway integration)
                payment.getTransactionRef(),
                null,                             // gatewayResponseRaw
                restaurant.getName(),
                order.getOrderNumber(),
                tableNumber,
                itemsJson,
                null, null, null,                 // customer name/email/phone — wired in later
                null
        );
        return receiptRepository.save(receipt);
    }

    /**
     * Issue receipts for any payments that don't have one yet. Idempotent: safe
     * to run on every startup. New tickets land in the payment's original
     * business day, so daily sequences stay coherent.
     *
     * @return count of receipts issued in this pass
     */
    @Transactional
    public int backfillMissing() {
        List<com.restaurant.api.entity.Payment> orphans = paymentRepository.findAllWithoutReceipts();
        int issued = 0;
        for (com.restaurant.api.entity.Payment p : orphans) {
            try {
                com.restaurant.api.entity.Orders o = ordersRepository.findByCode(p.getOrderCode()).orElse(null);
                if (o == null) continue;
                issue(p, o);
                issued++;
            } catch (Exception ignore) { /* keep going — one bad row shouldn't break the rest */ }
        }
        return issued;
    }

    private int nextReceiptNumber(String restaurantCode, LocalDate businessDate) {
        int max = receiptRepository.findMaxReceiptNumber(restaurantCode, businessDate)
                .orElse(RECEIPT_NUMBER_MIN - 1);
        int next = max + 1;
        if (next > RECEIPT_NUMBER_MAX) {
            // Day exceeded the 900-slot window. For now, wrap and rely on retry/unique to
            // surface — refactor when a restaurant actually crosses this threshold.
            next = RECEIPT_NUMBER_MIN;
        }
        return next;
    }

    private String serializeItems(List<OrderItem> items) {
        List<Map<String, Object>> payload = items.stream()
                .map(item -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("menuItemCode", item.getMenuItemCode());
                    m.put("name", menuItemRepository.findByCode(item.getMenuItemCode())
                            .map(mi -> mi.getName()).orElse(null));
                    m.put("quantity", item.getQuantity());
                    m.put("unitPrice", item.getUnitPrice());
                    m.put("totalPrice", item.getTotalPrice());
                    m.put("notes", item.getNotes());
                    return m;
                })
                .toList();
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
