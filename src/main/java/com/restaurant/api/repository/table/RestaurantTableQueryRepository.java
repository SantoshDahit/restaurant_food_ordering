package com.restaurant.api.repository.table;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.restaurant.api.constant.TableStatus;
import com.restaurant.api.dto.RestaurantTableDto;
import com.restaurant.api.entity.QRestaurantTable;
import com.restaurant.api.entity.RestaurantTable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RestaurantTableQueryRepository {
    private final JPAQueryFactory queryFactory;
    private static final QRestaurantTable restaurantTable = QRestaurantTable.restaurantTable;

    public Page<RestaurantTable> search(RestaurantTableDto.SearchRequest searchRequest, Pageable pageable) {
        List<RestaurantTable> result = queryFactory
                .selectFrom(restaurantTable)
                .where(
                        eqRestaurantCode(searchRequest.restaurantCode()),
                        eqStatus(searchRequest.status()),
                        restaurantTable.deleteAt.isNull()
                )
                .orderBy(restaurantTable.tableNumber.asc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        Long count = queryFactory.select(com.querydsl.core.types.dsl.Wildcard.count)
                .from(restaurantTable)
                .where(
                        eqRestaurantCode(searchRequest.restaurantCode()),
                        eqStatus(searchRequest.status()),
                        restaurantTable.deleteAt.isNull()
                )
                .fetchOne();

        return new PageImpl<>(result, pageable, count != null ? count : 0L);
    }

    private BooleanExpression eqRestaurantCode(String restaurantCode) {
        if (!StringUtils.hasText(restaurantCode)) return null;
        return restaurantTable.restaurantCode.eq(restaurantCode);
    }

    private BooleanExpression eqStatus(TableStatus status) {
        if (status == null) return null;
        return restaurantTable.status.eq(status);
    }
}
