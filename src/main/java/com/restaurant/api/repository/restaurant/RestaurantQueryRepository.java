package com.restaurant.api.repository.restaurant;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.restaurant.api.dto.RestaurantDto;
import com.restaurant.api.entity.QRestaurant;
import com.restaurant.api.entity.Restaurant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RestaurantQueryRepository {
    private final JPAQueryFactory queryFactory;
    private static final QRestaurant restaurant = QRestaurant.restaurant;

    public Page<Restaurant> search(RestaurantDto.SearchRequest searchRequest, Pageable pageable) {
        List<Restaurant> result = queryFactory
                .selectFrom(restaurant)
                .where(
                        containsName(searchRequest.name()),
                        restaurant.deletedAt.isNull()
                )
                .orderBy(restaurant.name.asc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        Long count = queryFactory.select(com.querydsl.core.types.dsl.Wildcard.count)
                .from(restaurant)
                .where(
                        containsName(searchRequest.name()),
                        restaurant.deletedAt.isNull()
                )
                .fetchOne();

        return new PageImpl<>(result, pageable, count != null ? count : 0L);
    }

    private BooleanExpression containsName(String name) {
        if (!StringUtils.hasText(name)) return null;
        return restaurant.name.containsIgnoreCase(name);
    }
}
