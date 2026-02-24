package com.restaurant.api.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QEmployee is a Querydsl query type for Employee
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEmployee extends EntityPathBase<Employee> {

    private static final long serialVersionUID = 1811826305L;

    public static final QEmployee employee = new QEmployee("employee");

    public final com.restaurant.api.common.QBaseFullEntity _super = new com.restaurant.api.common.QBaseFullEntity(this);

    public final StringPath bankAccount = createString("bankAccount");

    public final StringPath bankName = createString("bankName");

    public final NumberPath<java.math.BigDecimal> baseSalary = createNumber("baseSalary", java.math.BigDecimal.class);

    public final StringPath code = createString("code");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createAt = _super.createAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deleteAt = _super.deleteAt;

    public final StringPath fileCode = createString("fileCode");

    public final StringPath fullName = createString("fullName");

    public final BooleanPath isActive = createBoolean("isActive");

    public final DatePath<java.time.LocalDate> joinDate = createDate("joinDate", java.time.LocalDate.class);

    public final StringPath phone = createString("phone");

    public final StringPath restaurantCode = createString("restaurantCode");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateAt = _super.updateAt;

    public QEmployee(String variable) {
        super(Employee.class, forVariable(variable));
    }

    public QEmployee(Path<? extends Employee> path) {
        super(path.getType(), path.getMetadata());
    }

    public QEmployee(PathMetadata metadata) {
        super(Employee.class, metadata);
    }

}

