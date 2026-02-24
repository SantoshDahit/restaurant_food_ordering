package com.restaurant.api.repository.employee;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.restaurant.api.dto.EmployeeDto;
import com.restaurant.api.entity.Employee;
import com.restaurant.api.entity.QEmployee;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class EmployeeQueryRepository {
    private final JPAQueryFactory queryFactory;
    private static final QEmployee employee = QEmployee.employee;

    public Page<Employee> search(EmployeeDto.SearchRequest searchRequest, Pageable pageable) {
        List<Employee> result = queryFactory
                .selectFrom(employee)
                .where(
                        eqRestaurantCode(searchRequest.restaurantCode()),
                        containsFullName(searchRequest.fullName()),
                        eqIsActive(searchRequest.isActive()),
                        employee.deleteAt.isNull()
                )
                .orderBy(employee.fullName.asc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        Long count = queryFactory.select(com.querydsl.core.types.dsl.Wildcard.count)
                .from(employee)
                .where(
                        eqRestaurantCode(searchRequest.restaurantCode()),
                        containsFullName(searchRequest.fullName()),
                        eqIsActive(searchRequest.isActive()),
                        employee.deleteAt.isNull()
                )
                .fetchOne();

        return new PageImpl<>(result, pageable, count != null ? count : 0L);
    }

    private BooleanExpression eqRestaurantCode(String restaurantCode) {
        if (!StringUtils.hasText(restaurantCode)) return null;
        return employee.restaurantCode.eq(restaurantCode);
    }

    private BooleanExpression containsFullName(String fullName) {
        if (!StringUtils.hasText(fullName)) return null;
        return employee.fullName.containsIgnoreCase(fullName);
    }

    private BooleanExpression eqIsActive(Boolean isActive) {
        if (isActive == null) return null;
        return employee.isActive.eq(isActive);
    }
}
