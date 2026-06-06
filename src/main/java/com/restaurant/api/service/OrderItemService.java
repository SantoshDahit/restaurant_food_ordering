package com.restaurant.api.service;

import com.restaurant.api.common.UuidUtil;
import com.restaurant.api.constant.OrderStatus;
import com.restaurant.api.dto.OrderItemDto;
import com.restaurant.api.entity.MenuItem;
import com.restaurant.api.entity.OrderItem;
import com.restaurant.api.exception.ApiException;
import com.restaurant.api.exception.ErrorCode;
import com.restaurant.api.repository.menuitem.MenuItemRepository;
import com.restaurant.api.repository.orderitem.OrderItemRepository;
import com.restaurant.api.repository.orders.OrdersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OrderItemService {
    private final OrderItemRepository orderItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrdersRepository ordersRepository;

    @Transactional(readOnly = true)
    public OrderItem findByCode(String code) {
        return orderItemRepository.findByCode(code)
                .orElseThrow(() -> new ApiException(ErrorCode.ORDER_ITEM_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<OrderItem> findAllByOrderCode(String orderCode) {
        return orderItemRepository.findByOrderCode(orderCode);
    }

    @Transactional
    public OrderItem create(String orderCode, OrderItemDto.CreateRequest request) {
        MenuItem menuItem = menuItemRepository.findByCode(request.menuItemCode())
                .orElseThrow(() -> new ApiException(ErrorCode.MENU_ITEM_NOT_FOUND));

        int quantity = request.quantity() != null ? request.quantity() : 1;

        // Merge into an identical line that's still being prepared (same item +
        // same spice level + same notes) so a repeat "add to order" bumps the
        // quantity instead of creating a duplicate row — which would otherwise
        // show as two lines on the receipt. An already-served line is left alone
        // so a genuinely new round stays a separate (re-cooked) line.
        OrderItem existing = orderItemRepository.findByOrderCode(orderCode).stream()
                .filter(i -> i.getMenuItemCode().equals(request.menuItemCode())
                        && Objects.equals(i.getSpiceLevel(), request.spiceLevel())
                        && Objects.equals(i.getNotes(), request.notes())
                        && isMergeable(i.getStatus()))
                .findFirst()
                .orElse(null);
        if (existing != null) {
            existing.increaseQuantity(quantity);
            return orderItemRepository.save(existing);
        }

        OrderItem orderItem = new OrderItem(
                orderCode,
                request.menuItemCode(),
                quantity,
                menuItem.getPrice(),
                request.discountAmount(),
                request.spiceLevel(),
                request.notes()
        );
        return orderItemRepository.save(orderItem);
    }

    /** A line can absorb more quantity only while it hasn't been served yet. */
    private boolean isMergeable(OrderStatus status) {
        return status == OrderStatus.PENDING
                || status == OrderStatus.CONFIRMED
                || status == OrderStatus.PREPARING;
    }

    @Transactional
    public OrderItem update(String code, OrderItemDto.PatchRequest request) {
        OrderItem orderItem = findByCode(code);
        orderItem.update(request.quantity(), request.spiceLevel(), request.notes());
        return orderItemRepository.save(orderItem);
    }

    @Transactional
    public void delete(String code) {
        orderItemRepository.delete(code);
    }

    @Transactional
    public void updateStatusByOrderCode(String orderCode, OrderStatus status) {
        List<OrderItem> items = findAllByOrderCode(orderCode);
        items.forEach(item -> {
            item.updateStatus(status);
            orderItemRepository.save(item);
        });
    }
}
