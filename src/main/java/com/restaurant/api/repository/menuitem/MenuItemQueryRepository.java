package com.restaurant.api.repository.menuitem;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.restaurant.api.constant.ItemAvailability;
import com.restaurant.api.dto.MenuItemDto;
import com.restaurant.api.entity.MenuItem;
import com.restaurant.api.entity.QMenuItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MenuItemQueryRepository {
    private final JPAQueryFactory queryFactory;
    private static final QMenuItem menuItem = QMenuItem.menuItem;

    public Page<MenuItem> search(MenuItemDto.SearchRequest searchRequest, Pageable pageable) {
        List<MenuItem> result = queryFactory
                .selectFrom(menuItem)
                .where(
                        eqRestaurantCode(searchRequest.restaurantCode()),
                        eqCategoryCode(searchRequest.categoryCode()),
                        eqAvailability(searchRequest.availability()),
                        eqIsFeatured(searchRequest.isFeatured()),
                        eqIsVeg(searchRequest.isVeg()),
                        menuItem.deletedAt.isNull()
                )
                .orderBy(menuItem.sortOrder.asc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        Long count = queryFactory.select(com.querydsl.core.types.dsl.Wildcard.count)
                .from(menuItem)
                .where(
                        eqRestaurantCode(searchRequest.restaurantCode()),
                        eqCategoryCode(searchRequest.categoryCode()),
                        eqAvailability(searchRequest.availability()),
                        eqIsFeatured(searchRequest.isFeatured()),
                        eqIsVeg(searchRequest.isVeg()),
                        menuItem.deletedAt.isNull()
                )
                .fetchOne();

        return new PageImpl<>(result, pageable, count != null ? count : 0L);
    }

    private BooleanExpression eqRestaurantCode(String restaurantCode) {
        if (!StringUtils.hasText(restaurantCode)) return null;
        return menuItem.restaurantCode.eq(restaurantCode);
    }

    private BooleanExpression eqCategoryCode(String categoryCode) {
        if (!StringUtils.hasText(categoryCode)) return null;
        return menuItem.categoryCode.eq(categoryCode);
    }

    private BooleanExpression eqAvailability(ItemAvailability availability) {
        if (availability == null) return null;
        return menuItem.availability.eq(availability);
    }

    private BooleanExpression eqIsFeatured(Boolean isFeatured) {
        if (isFeatured == null) return null;
        return menuItem.isFeatured.eq(isFeatured);
    }

    private BooleanExpression eqIsVeg(Boolean isVeg) {
        if (isVeg == null) return null;
        return menuItem.isVeg.eq(isVeg);
    }
}
