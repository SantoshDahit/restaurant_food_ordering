package com.restaurant.api.repository.payment;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.restaurant.api.constant.PaymentMethod;
import com.restaurant.api.constant.PaymentStatus;
import com.restaurant.api.dto.PaymentDto;
import com.restaurant.api.entity.Payment;
import com.restaurant.api.entity.QPayment;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PaymentQueryRepository {
    private final JPAQueryFactory queryFactory;
    private static final QPayment payment = QPayment.payment;

    public Page<Payment> search(PaymentDto.SearchRequest searchRequest, Pageable pageable) {
        List<Payment> result = queryFactory
                .selectFrom(payment)
                .where(
                        eqRestaurantCode(searchRequest.restaurantCode()),
                        eqStatus(searchRequest.status()),
                        eqPaymentMethod(searchRequest.paymentMethod())
                )
                .orderBy(payment.createdAt.desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        Long count = queryFactory.select(com.querydsl.core.types.dsl.Wildcard.count)
                .from(payment)
                .where(
                        eqRestaurantCode(searchRequest.restaurantCode()),
                        eqStatus(searchRequest.status()),
                        eqPaymentMethod(searchRequest.paymentMethod())
                )
                .fetchOne();

        return new PageImpl<>(result, pageable, count != null ? count : 0L);
    }

    private BooleanExpression eqRestaurantCode(String restaurantCode) {
        if (!StringUtils.hasText(restaurantCode)) return null;
        return payment.restaurantCode.eq(restaurantCode);
    }

    private BooleanExpression eqStatus(PaymentStatus status) {
        if (status == null) return null;
        return payment.status.eq(status);
    }

    private BooleanExpression eqPaymentMethod(PaymentMethod paymentMethod) {
        if (paymentMethod == null) return null;
        return payment.paymentMethod.eq(paymentMethod);
    }
}
