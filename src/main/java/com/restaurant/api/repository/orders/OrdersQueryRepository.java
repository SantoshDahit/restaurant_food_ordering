package com.restaurant.api.repository.orders;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.restaurant.api.constant.OrderStatus;
import com.restaurant.api.constant.OrderType;
import com.restaurant.api.dto.OrdersDto;
import com.restaurant.api.entity.Orders;
import com.restaurant.api.entity.QOrders;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrdersQueryRepository {
    private final JPAQueryFactory queryFactory;
    private static final QOrders orders = QOrders.orders;

    public Page<Orders> search(OrdersDto.SearchRequest searchRequest, Pageable pageable) {
        List<Orders> result = queryFactory
                .selectFrom(orders)
                .where(
                        eqRestaurantCode(searchRequest.restaurantCode()),
                        eqStatus(searchRequest.status()),
                        eqOrderType(searchRequest.orderType()),
                        eqTableCode(searchRequest.tableCode()),
                        orders.deletedAt.isNull()
                )
                .orderBy(orders.createdAt.desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        Long count = queryFactory.select(com.querydsl.core.types.dsl.Wildcard.count)
                .from(orders)
                .where(
                        eqRestaurantCode(searchRequest.restaurantCode()),
                        eqStatus(searchRequest.status()),
                        eqOrderType(searchRequest.orderType()),
                        eqTableCode(searchRequest.tableCode()),
                        orders.deletedAt.isNull()
                )
                .fetchOne();

        return new PageImpl<>(result, pageable, count != null ? count : 0L);
    }

    private BooleanExpression eqRestaurantCode(String restaurantCode) {
        if (!StringUtils.hasText(restaurantCode)) return null;
        return orders.restaurantCode.eq(restaurantCode);
    }

    private BooleanExpression eqStatus(OrderStatus status) {
        if (status == null) return null;
        return orders.status.eq(status);
    }

    private BooleanExpression eqOrderType(OrderType orderType) {
        if (orderType == null) return null;
        return orders.orderType.eq(orderType);
    }

    private BooleanExpression eqTableCode(String tableCode) {
        if (!StringUtils.hasText(tableCode)) return null;
        return orders.tableCode.eq(tableCode);
    }
}
