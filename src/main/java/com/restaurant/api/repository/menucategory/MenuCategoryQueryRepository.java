package com.restaurant.api.repository.menucategory;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import com.restaurant.api.constant.MenuCategoryType;
import com.restaurant.api.dto.MenuCategoryDto;
import com.restaurant.api.entity.MenuCategory;
import com.restaurant.api.entity.QMenuCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MenuCategoryQueryRepository {
    private final JPAQueryFactory queryFactory;
    private static final QMenuCategory menuCategory = QMenuCategory.menuCategory;

    public Page<MenuCategory> search(MenuCategoryDto.SearchRequest searchRequest, Pageable pageable) {
        List<MenuCategory> result = queryFactory
                .selectFrom(menuCategory)
                .where(
                        eqRestaurantCode(searchRequest.restaurantCode()),
                        eqCategoryType(searchRequest.categoryType()),
                        menuCategory.deletedAt.isNull()
                )
                .orderBy(menuCategory.sortOrder.asc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        Long count = queryFactory.select(com.querydsl.core.types.dsl.Wildcard.count)
                .from(menuCategory)
                .where(
                        eqRestaurantCode(searchRequest.restaurantCode()),
                        eqCategoryType(searchRequest.categoryType()),
                        menuCategory.deletedAt.isNull()
                )
                .fetchOne();

        return new PageImpl<>(result, pageable, count != null ? count : 0L);
    }

    private BooleanExpression eqRestaurantCode(String restaurantCode) {
        if (!StringUtils.hasText(restaurantCode)) return null;
        return menuCategory.restaurantCode.eq(restaurantCode);
    }

    private BooleanExpression eqCategoryType(MenuCategoryType categoryType) {
        if (categoryType == null) return null;
        return menuCategory.categoryType.eq(categoryType);
    }
}
