package com.restaurant.api.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = -2054607586L;

    public static final QUser user = new QUser("user");

    public final com.restaurant.api.common.QBaseFullEntity _super = new com.restaurant.api.common.QBaseFullEntity(this);

    public final StringPath code = createString("code");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createAt = _super.createAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deleteAt = _super.deleteAt;

    public final StringPath email = createString("email");

    public final StringPath fileCode = createString("fileCode");

    public final StringPath fullName = createString("fullName");

    public final BooleanPath isActive = createBoolean("isActive");

    public final StringPath passwordHash = createString("passwordHash");

    public final StringPath phone = createString("phone");

    public final StringPath restaurantCode = createString("restaurantCode");

    public final EnumPath<com.restaurant.api.constant.UserRole> role = createEnum("role", com.restaurant.api.constant.UserRole.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateAt = _super.updateAt;

    public QUser(String variable) {
        super(User.class, forVariable(variable));
    }

    public QUser(Path<? extends User> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUser(PathMetadata metadata) {
        super(User.class, metadata);
    }

}

