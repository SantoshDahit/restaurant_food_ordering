package com.restaurant.api.service.facade;

import com.restaurant.api.annotation.Facade;
import com.restaurant.api.constant.OrderStatus;
import com.restaurant.api.constant.TableStatus;
import com.restaurant.api.dto.OrdersDto;
import com.restaurant.api.entity.MenuItem;
import com.restaurant.api.entity.OrderItem;
import com.restaurant.api.entity.Orders;
import com.restaurant.api.entity.Restaurant;
import com.restaurant.api.entity.RestaurantTable;
import com.restaurant.api.entity.User;
import com.restaurant.api.mapper.OrderItemMapper;
import com.restaurant.api.mapper.OrdersMapper;
import com.restaurant.api.repository.menuitem.MenuItemRepository;
import com.restaurant.api.repository.restaurant.RestaurantRepository;
import com.restaurant.api.repository.table.RestaurantTableRepository;
import com.restaurant.api.repository.user.UserRepository;
import com.restaurant.api.service.OrderItemService;
import com.restaurant.api.service.OrdersService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Facade
@RequiredArgsConstructor
public class OrdersFacade {
    private final OrdersService ordersService;
    private final OrderItemService orderItemService;
    private final OrdersMapper ordersMapper;
    private final OrderItemMapper orderItemMapper;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantTableRepository restaurantTableRepository;
    private final UserRepository userRepository;
    private final MenuItemRepository menuItemRepository;

    @Transactional
    public OrdersDto.Response create(OrdersDto.CreateRequest request) {
        Orders orders = ordersService.create(request);
        markTableOccupied(orders.getTableCode());
        return ordersMapper.toResponse(orders);
    }

    @Transactional(readOnly = true)
    public OrdersDto.Response getByCode(String code) {
        Orders orders = ordersService.getByCode(code);
        return ordersMapper.toResponse(orders);
    }

    @Transactional(readOnly = true)
    public OrdersDto.DetailResponse getDetail(String code) {
        Orders orders = ordersService.getByCode(code);

        String restaurantName = restaurantRepository.findByCode(orders.getRestaurantCode())
                .map(Restaurant::getName)
                .orElse(null);

        String tableNumber = orders.getTableCode() == null ? null
                : restaurantTableRepository.findByCode(orders.getTableCode())
                        .map(RestaurantTable::getTableNumber)
                        .orElse(null);

        String waiterName = orders.getWaiterCode() == null ? null
                : userRepository.findByCode(orders.getWaiterCode())
                        .map(User::getFullName)
                        .orElse(null);

        List<OrderItem> items = orderItemService.findAllByOrderCode(code);
        List<OrdersDto.OrderItemDetail> itemDetails = items.stream()
                .map(item -> orderItemMapper.toDetail(item, resolveMenuItemName(item.getMenuItemCode())))
                .toList();

        return ordersMapper.toDetailResponse(orders, restaurantName, tableNumber, waiterName, itemDetails);
    }

    private String resolveMenuItemName(String menuItemCode) {
        return menuItemRepository.findByCode(menuItemCode)
                .map(MenuItem::getName)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Page<OrdersDto.Response> search(OrdersDto.SearchRequest request, Pageable pageable) {
        return ordersService.search(request, pageable)
                .map(ordersMapper::toResponse);
    }

    @Transactional
    public OrdersDto.Response updateStatus(String code, OrdersDto.StatusUpdateRequest request) {
        Orders orders = ordersService.updateStatus(code, request);
        orderItemService.updateStatusByOrderCode(code, request.status());
        if (isTerminalStatus(request.status())) {
            freeTableIfOccupied(orders.getTableCode());
        }
        return ordersMapper.toResponse(orders);
    }

    @Transactional
    public void delete(String code) {
        Orders orders = ordersService.getByCode(code);
        ordersService.delete(code);
        freeTableIfOccupied(orders.getTableCode());
    }

    private void markTableOccupied(String tableCode) {
        if (tableCode == null) return;
        restaurantTableRepository.findByCode(tableCode).ifPresent(table -> {
            if (table.getStatus() != TableStatus.OCCUPIED) {
                table.updateStatus(TableStatus.OCCUPIED);
                restaurantTableRepository.save(table);
            }
        });
    }

    private void freeTableIfOccupied(String tableCode) {
        if (tableCode == null) return;
        restaurantTableRepository.findByCode(tableCode).ifPresent(table -> {
            if (table.getStatus() == TableStatus.OCCUPIED) {
                table.updateStatus(TableStatus.AVAILABLE);
                restaurantTableRepository.save(table);
            }
        });
    }

    private boolean isTerminalStatus(OrderStatus status) {
        return status == OrderStatus.COMPLETED || status == OrderStatus.CANCELLED;
    }
}
