package com.restaurant.api.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QRestaurantTable is a Querydsl query type for RestaurantTable
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRestaurantTable extends EntityPathBase<RestaurantTable> {

    private static final long serialVersionUID = -1488635042L;

    public static final QRestaurantTable restaurantTable = new QRestaurantTable("restaurantTable");

    public final com.restaurant.api.common.QBaseFullEntity _super = new com.restaurant.api.common.QBaseFullEntity(this);

    public final NumberPath<Integer> capacity = createNumber("capacity", Integer.class);

    public final StringPath code = createString("code");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createAt = _super.createAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deleteAt = _super.deleteAt;

    public final BooleanPath isActive = createBoolean("isActive");

    public final StringPath qrCodeToken = createString("qrCodeToken");

    public final StringPath qrCodeUrl = createString("qrCodeUrl");

    public final StringPath restaurantCode = createString("restaurantCode");

    public final EnumPath<com.restaurant.api.constant.TableStatus> status = createEnum("status", com.restaurant.api.constant.TableStatus.class);

    public final StringPath tableNumber = createString("tableNumber");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateAt = _super.updateAt;

    public QRestaurantTable(String variable) {
        super(RestaurantTable.class, forVariable(variable));
    }

    public QRestaurantTable(Path<? extends RestaurantTable> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRestaurantTable(PathMetadata metadata) {
        super(RestaurantTable.class, metadata);
    }

}

