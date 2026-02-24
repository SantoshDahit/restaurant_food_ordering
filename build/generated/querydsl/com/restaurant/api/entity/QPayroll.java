package com.restaurant.api.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPayroll is a Querydsl query type for Payroll
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPayroll extends EntityPathBase<Payroll> {

    private static final long serialVersionUID = -1874954510L;

    public static final QPayroll payroll = new QPayroll("payroll");

    public final NumberPath<java.math.BigDecimal> bonus = createNumber("bonus", java.math.BigDecimal.class);

    public final StringPath code = createString("code");

    public final DateTimePath<java.time.LocalDateTime> createAt = createDateTime("createAt", java.time.LocalDateTime.class);

    public final NumberPath<java.math.BigDecimal> deductions = createNumber("deductions", java.math.BigDecimal.class);

    public final StringPath employeeCode = createString("employeeCode");

    public final NumberPath<java.math.BigDecimal> netSalary = createNumber("netSalary", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> overtimePay = createNumber("overtimePay", java.math.BigDecimal.class);

    public final DateTimePath<java.time.LocalDateTime> paidAt = createDateTime("paidAt", java.time.LocalDateTime.class);

    public final DatePath<java.time.LocalDate> payPeriodEnd = createDate("payPeriodEnd", java.time.LocalDate.class);

    public final DatePath<java.time.LocalDate> payPeriodStart = createDate("payPeriodStart", java.time.LocalDate.class);

    public final StringPath restaurantCode = createString("restaurantCode");

    public final EnumPath<com.restaurant.api.constant.SalaryStatus> status = createEnum("status", com.restaurant.api.constant.SalaryStatus.class);

    public final DateTimePath<java.time.LocalDateTime> updateAt = createDateTime("updateAt", java.time.LocalDateTime.class);

    public QPayroll(String variable) {
        super(Payroll.class, forVariable(variable));
    }

    public QPayroll(Path<? extends Payroll> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPayroll(PathMetadata metadata) {
        super(Payroll.class, metadata);
    }

}

