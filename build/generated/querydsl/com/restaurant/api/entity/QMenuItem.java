package com.restaurant.api.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMenuItem is a Querydsl query type for MenuItem
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMenuItem extends EntityPathBase<MenuItem> {

    private static final long serialVersionUID = 14261477L;

    public static final QMenuItem menuItem = new QMenuItem("menuItem");

    public final com.restaurant.api.common.QBaseFullEntity _super = new com.restaurant.api.common.QBaseFullEntity(this);

    public final EnumPath<com.restaurant.api.constant.ItemAvailability> availability = createEnum("availability", com.restaurant.api.constant.ItemAvailability.class);

    public final StringPath categoryCode = createString("categoryCode");

    public final StringPath code = createString("code");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createAt = _super.createAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deleteAt = _super.deleteAt;

    public final StringPath description = createString("description");

    public final NumberPath<java.math.BigDecimal> discountPercent = createNumber("discountPercent", java.math.BigDecimal.class);

    public final StringPath fileCode = createString("fileCode");

    public final BooleanPath isFeatured = createBoolean("isFeatured");

    public final BooleanPath isVeg = createBoolean("isVeg");

    public final StringPath name = createString("name");

    public final NumberPath<Integer> prepTimeMinutes = createNumber("prepTimeMinutes", Integer.class);

    public final NumberPath<java.math.BigDecimal> price = createNumber("price", java.math.BigDecimal.class);

    public final StringPath restaurantCode = createString("restaurantCode");

    public final NumberPath<Integer> sortOrder = createNumber("sortOrder", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateAt = _super.updateAt;

    public QMenuItem(String variable) {
        super(MenuItem.class, forVariable(variable));
    }

    public QMenuItem(Path<? extends MenuItem> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMenuItem(PathMetadata metadata) {
        super(MenuItem.class, metadata);
    }

}

