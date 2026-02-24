package com.restaurant.api.repository.payroll;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.restaurant.api.constant.SalaryStatus;
import com.restaurant.api.dto.PayrollDto;
import com.restaurant.api.entity.Payroll;
import com.restaurant.api.entity.QPayroll;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PayrollQueryRepository {
    private final JPAQueryFactory queryFactory;
    private static final QPayroll payroll = QPayroll.payroll;

    public Page<Payroll> search(PayrollDto.SearchRequest searchRequest, Pageable pageable) {
        List<Payroll> result = queryFactory
                .selectFrom(payroll)
                .where(
                        eqRestaurantCode(searchRequest.restaurantCode()),
                        eqEmployeeCode(searchRequest.employeeCode()),
                        eqStatus(searchRequest.status())
                )
                .orderBy(payroll.createAt.desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        Long count = queryFactory.select(com.querydsl.core.types.dsl.Wildcard.count)
                .from(payroll)
                .where(
                        eqRestaurantCode(searchRequest.restaurantCode()),
                        eqEmployeeCode(searchRequest.employeeCode()),
                        eqStatus(searchRequest.status())
                )
                .fetchOne();

        return new PageImpl<>(result, pageable, count != null ? count : 0L);
    }

    private BooleanExpression eqRestaurantCode(String restaurantCode) {
        if (!StringUtils.hasText(restaurantCode)) return null;
        return payroll.restaurantCode.eq(restaurantCode);
    }

    private BooleanExpression eqEmployeeCode(String employeeCode) {
        if (!StringUtils.hasText(employeeCode)) return null;
        return payroll.employeeCode.eq(employeeCode);
    }

    private BooleanExpression eqStatus(SalaryStatus status) {
        if (status == null) return null;
        return payroll.status.eq(status);
    }
}
