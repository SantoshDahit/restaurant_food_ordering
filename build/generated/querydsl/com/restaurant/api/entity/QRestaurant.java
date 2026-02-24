package com.restaurant.api.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QRestaurant is a Querydsl query type for Restaurant
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRestaurant extends EntityPathBase<Restaurant> {

    private static final long serialVersionUID = -237174192L;

    public static final QRestaurant restaurant = new QRestaurant("restaurant");

    public final com.restaurant.api.common.QBaseFullEntity _super = new com.restaurant.api.common.QBaseFullEntity(this);

    public final StringPath address = createString("address");

    public final StringPath businessNumber = createString("businessNumber");

    public final StringPath code = createString("code");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createAt = _super.createAt;

    public final StringPath currency = createString("currency");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deleteAt = _super.deleteAt;

    public final StringPath email = createString("email");

    public final StringPath fileCode = createString("fileCode");

    public final BooleanPath isActive = createBoolean("isActive");

    public final StringPath name = createString("name");

    public final StringPath phone = createString("phone");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateAt = _super.updateAt;

    public final StringPath userCode = createString("userCode");

    public QRestaurant(String variable) {
        super(Restaurant.class, forVariable(variable));
    }

    public QRestaurant(Path<? extends Restaurant> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRestaurant(PathMetadata metadata) {
        super(Restaurant.class, metadata);
    }

}

