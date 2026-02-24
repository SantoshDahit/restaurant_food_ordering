package com.restaurant.api.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QOrders is a Querydsl query type for Orders
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOrders extends EntityPathBase<Orders> {

    private static final long serialVersionUID = 1034328952L;

    public static final QOrders orders = new QOrders("orders");

    public final com.restaurant.api.common.QBaseFullEntity _super = new com.restaurant.api.common.QBaseFullEntity(this);

    public final StringPath code = createString("code");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createAt = _super.createAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deleteAt = _super.deleteAt;

    public final StringPath deviceType = createString("deviceType");

    public final NumberPath<java.math.BigDecimal> discountAmount = createNumber("discountAmount", java.math.BigDecimal.class);

    public final StringPath orderNumber = createString("orderNumber");

    public final EnumPath<com.restaurant.api.constant.OrderType> orderType = createEnum("orderType", com.restaurant.api.constant.OrderType.class);

    public final StringPath restaurantCode = createString("restaurantCode");

    public final StringPath specialNotes = createString("specialNotes");

    public final EnumPath<com.restaurant.api.constant.OrderStatus> status = createEnum("status", com.restaurant.api.constant.OrderStatus.class);

    public final NumberPath<java.math.BigDecimal> subtotal = createNumber("subtotal", java.math.BigDecimal.class);

    public final StringPath tableCode = createString("tableCode");

    public final NumberPath<java.math.BigDecimal> taxAmount = createNumber("taxAmount", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> totalAmount = createNumber("totalAmount", java.math.BigDecimal.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateAt = _super.updateAt;

    public final StringPath waiterCode = createString("waiterCode");

    public QOrders(String variable) {
        super(Orders.class, forVariable(variable));
    }

    public QOrders(Path<? extends Orders> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOrders(PathMetadata metadata) {
        super(Orders.class, metadata);
    }

}

