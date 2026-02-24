package com.restaurant.api.common;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBaseFullEntity is a Querydsl query type for BaseFullEntity
 */
@Generated("com.querydsl.codegen.DefaultSupertypeSerializer")
public class QBaseFullEntity extends EntityPathBase<BaseFullEntity> {

    private static final long serialVersionUID = 333888398L;

    public static final QBaseFullEntity baseFullEntity = new QBaseFullEntity("baseFullEntity");

    public final DateTimePath<java.time.LocalDateTime> createAt = createDateTime("createAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> deleteAt = createDateTime("deleteAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> updateAt = createDateTime("updateAt", java.time.LocalDateTime.class);

    public QBaseFullEntity(String variable) {
        super(BaseFullEntity.class, forVariable(variable));
    }

    public QBaseFullEntity(Path<? extends BaseFullEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBaseFullEntity(PathMetadata metadata) {
        super(BaseFullEntity.class, metadata);
    }

}

