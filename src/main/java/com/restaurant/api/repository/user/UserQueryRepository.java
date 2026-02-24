package com.restaurant.api.repository.user;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.restaurant.api.constant.UserRole;
import com.restaurant.api.dto.UserDto;
import com.restaurant.api.entity.QUser;
import com.restaurant.api.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserQueryRepository {
    private final JPAQueryFactory queryFactory;
    private static final QUser user = QUser.user;

    public Page<User> search(UserDto.SearchRequest searchRequest, Pageable pageable) {
        List<User> result = queryFactory
                .selectFrom(user)
                .where(
                        eqRestaurantCode(searchRequest.restaurantCode()),
                        eqRole(searchRequest.role()),
                        containsFullName(searchRequest.fullName()),
                        user.deleteAt.isNull()
                )
                .orderBy(user.createAt.desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        Long count = queryFactory.select(com.querydsl.core.types.dsl.Wildcard.count)
                .from(user)
                .where(
                        eqRestaurantCode(searchRequest.restaurantCode()),
                        eqRole(searchRequest.role()),
                        containsFullName(searchRequest.fullName()),
                        user.deleteAt.isNull()
                )
                .fetchOne();

        return new PageImpl<>(result, pageable, count != null ? count : 0L);
    }

    private BooleanExpression eqRestaurantCode(String restaurantCode) {
        if (!StringUtils.hasText(restaurantCode)) return null;
        return user.restaurantCode.eq(restaurantCode);
    }

    private BooleanExpression eqRole(UserRole role) {
        if (role == null) return null;
        return user.role.eq(role);
    }

    private BooleanExpression containsFullName(String fullName) {
        if (!StringUtils.hasText(fullName)) return null;
        return user.fullName.containsIgnoreCase(fullName);
    }
}
