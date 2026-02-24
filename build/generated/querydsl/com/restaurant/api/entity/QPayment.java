package com.restaurant.api.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPayment is a Querydsl query type for Payment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPayment extends EntityPathBase<Payment> {

    private static final long serialVersionUID = -1875113005L;

    public static final QPayment payment = new QPayment("payment");

    public final NumberPath<java.math.BigDecimal> amount = createNumber("amount", java.math.BigDecimal.class);

    public final StringPath code = createString("code");

    public final DateTimePath<java.time.LocalDateTime> createAt = createDateTime("createAt", java.time.LocalDateTime.class);

    public final StringPath orderCode = createString("orderCode");

    public final EnumPath<com.restaurant.api.constant.PaymentMethod> paymentMethod = createEnum("paymentMethod", com.restaurant.api.constant.PaymentMethod.class);

    public final DateTimePath<java.time.LocalDateTime> processedAt = createDateTime("processedAt", java.time.LocalDateTime.class);

    public final StringPath processedBy = createString("processedBy");

    public final StringPath receiptNumber = createString("receiptNumber");

    public final NumberPath<java.math.BigDecimal> refundedAmount = createNumber("refundedAmount", java.math.BigDecimal.class);

    public final StringPath restaurantCode = createString("restaurantCode");

    public final EnumPath<com.restaurant.api.constant.PaymentStatus> status = createEnum("status", com.restaurant.api.constant.PaymentStatus.class);

    public final StringPath transactionRef = createString("transactionRef");

    public final DateTimePath<java.time.LocalDateTime> updateAt = createDateTime("updateAt", java.time.LocalDateTime.class);

    public QPayment(String variable) {
        super(Payment.class, forVariable(variable));
    }

    public QPayment(Path<? extends Payment> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPayment(PathMetadata metadata) {
        super(Payment.class, metadata);
    }

}

