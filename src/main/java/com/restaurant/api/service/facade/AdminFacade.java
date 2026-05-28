package com.restaurant.api.service.facade;

import com.restaurant.api.annotation.Facade;
import com.restaurant.api.constant.PaymentStatus;
import com.restaurant.api.constant.UserRole;
import com.restaurant.api.dto.AdminDto;
import com.restaurant.api.dto.AnalyticsDto;
import com.restaurant.api.dto.UserDto;
import com.restaurant.api.entity.Orders;
import com.restaurant.api.entity.User;
import com.restaurant.api.mapper.UserMapper;
import com.restaurant.api.repository.employee.EmployeeJpaRepository;
import com.restaurant.api.repository.orders.OrdersJpaRepository;
import com.restaurant.api.repository.payment.PaymentJpaRepository;
import com.restaurant.api.repository.restaurant.RestaurantJpaRepository;
import com.restaurant.api.repository.user.UserJpaRepository;
import com.restaurant.api.service.AnalyticsService;
import com.restaurant.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Facade
@RequiredArgsConstructor
public class AdminFacade {
    private final UserJpaRepository userJpaRepository;
    private final RestaurantJpaRepository restaurantJpaRepository;
    private final OrdersJpaRepository ordersJpaRepository;
    private final PaymentJpaRepository paymentJpaRepository;
    private final EmployeeJpaRepository employeeJpaRepository;
    private final AnalyticsService analyticsService;
    private final UserService userService;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public AdminDto.PlatformStats stats() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        BigDecimal totalRevenue = paymentJpaRepository.sumNetAmountByStatus(PaymentStatus.COMPLETED);
        BigDecimal revenueToday = paymentJpaRepository.sumNetAmountByStatusSince(PaymentStatus.COMPLETED, startOfDay);
        return new AdminDto.PlatformStats(
                restaurantJpaRepository.countByDeletedAtIsNull(),
                restaurantJpaRepository.countByIsActiveAndDeletedAtIsNull(true),
                userJpaRepository.countByDeletedAtIsNull(),
                userJpaRepository.countByRoleAndDeletedAtIsNull(UserRole.MANAGER),
                userJpaRepository.countByRoleAndDeletedAtIsNull(UserRole.STAFF),
                ordersJpaRepository.countByDeletedAtIsNull(),
                ordersJpaRepository.countByCreatedAtAfterAndDeletedAtIsNull(startOfDay),
                totalRevenue == null ? BigDecimal.ZERO : totalRevenue,
                revenueToday == null ? BigDecimal.ZERO : revenueToday
        );
    }

    @Transactional(readOnly = true)
    public AdminDto.RestaurantOverview restaurantOverview(String restaurantCode) {
        BigDecimal revenue = paymentJpaRepository.sumNetAmountByStatusAndRestaurant(PaymentStatus.COMPLETED, restaurantCode);
        long staffCount = employeeJpaRepository.countByRestaurantCodeAndIsActiveAndDeletedAtIsNull(restaurantCode, true);
        LocalDateTime lastOrderAt = ordersJpaRepository
                .findFirstByRestaurantCodeAndDeletedAtIsNullOrderByCreatedAtDesc(restaurantCode)
                .map(Orders::getCreatedAt)
                .orElse(null);
        // Use a generous window so admins effectively see "all time" top sellers.
        List<AnalyticsDto.TopItem> topItems = analyticsService.topItems(
                restaurantCode, LocalDate.of(2000, 1, 1), LocalDate.now(), 5);
        return new AdminDto.RestaurantOverview(
                ordersJpaRepository.countByRestaurantCodeAndDeletedAtIsNull(restaurantCode),
                revenue == null ? BigDecimal.ZERO : revenue,
                staffCount,
                lastOrderAt,
                topItems
        );
    }

    @Transactional
    public UserDto.Response changeUserRole(String userCode, AdminDto.RoleChangeRequest request) {
        User user = userService.changeRole(userCode, request.role());
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserDto.Response setUserActive(String userCode, AdminDto.ActiveChangeRequest request) {
        User user = userService.setActive(userCode, request.active());
        return userMapper.toResponse(user);
    }
}
