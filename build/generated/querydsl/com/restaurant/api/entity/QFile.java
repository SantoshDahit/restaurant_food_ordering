package com.restaurant.api.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QFile is a Querydsl query type for File
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFile extends EntityPathBase<File> {

    private static final long serialVersionUID = -2055063857L;

    public static final QFile file = new QFile("file");

    public final StringPath code = createString("code");

    public final DateTimePath<java.time.LocalDateTime> createAt = createDateTime("createAt", java.time.LocalDateTime.class);

    public final BooleanPath isSuccess = createBoolean("isSuccess");

    public final EnumPath<com.restaurant.api.constant.FileType> type = createEnum("type", com.restaurant.api.constant.FileType.class);

    public final DateTimePath<java.time.LocalDateTime> updateAt = createDateTime("updateAt", java.time.LocalDateTime.class);

    public final StringPath url = createString("url");

    public QFile(String variable) {
        super(File.class, forVariable(variable));
    }

    public QFile(Path<? extends File> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFile(PathMetadata metadata) {
        super(File.class, metadata);
    }

}

