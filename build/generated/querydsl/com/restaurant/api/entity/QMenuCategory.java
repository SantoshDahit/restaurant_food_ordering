package com.restaurant.api.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMenuCategory is a Querydsl query type for MenuCategory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMenuCategory extends EntityPathBase<MenuCategory> {

    private static final long serialVersionUID = 1279370704L;

    public static final QMenuCategory menuCategory = new QMenuCategory("menuCategory");

    public final com.restaurant.api.common.QBaseFullEntity _super = new com.restaurant.api.common.QBaseFullEntity(this);

    public final EnumPath<com.restaurant.api.constant.MenuCategoryType> categoryType = createEnum("categoryType", com.restaurant.api.constant.MenuCategoryType.class);

    public final StringPath code = createString("code");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createAt = _super.createAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deleteAt = _super.deleteAt;

    public final StringPath fileCode = createString("fileCode");

    public final BooleanPath isActive = createBoolean("isActive");

    public final StringPath name = createString("name");

    public final StringPath restaurantCode = createString("restaurantCode");

    public final NumberPath<Integer> sortOrder = createNumber("sortOrder", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateAt = _super.updateAt;

    public QMenuCategory(String variable) {
        super(MenuCategory.class, forVariable(variable));
    }

    public QMenuCategory(Path<? extends MenuCategory> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMenuCategory(PathMetadata metadata) {
        super(MenuCategory.class, metadata);
    }

}

