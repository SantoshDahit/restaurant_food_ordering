package com.restaurant.api.repository.orderitem;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.restaurant.api.entity.OrderItem;
import com.restaurant.api.entity.QOrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderItemQueryRepository {
    private final JPAQueryFactory queryFactory;
    private static final QOrderItem orderItem = QOrderItem.orderItem;

    public List<OrderItem> findAllByOrderCode(String orderCode) {
        return queryFactory
                .selectFrom(orderItem)
                .where(eqOrderCode(orderCode))
                .orderBy(orderItem.createdAt.asc())
                .fetch();
    }

    private BooleanExpression eqOrderCode(String orderCode) {
        if (!StringUtils.hasText(orderCode)) return null;
        return orderItem.orderCode.eq(orderCode);
    }
}
