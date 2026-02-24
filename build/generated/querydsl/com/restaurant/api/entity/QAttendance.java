package com.restaurant.api.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAttendance is a Querydsl query type for Attendance
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAttendance extends EntityPathBase<Attendance> {

    private static final long serialVersionUID = -862283268L;

    public static final QAttendance attendance = new QAttendance("attendance");

    public final DatePath<java.time.LocalDate> attendanceDate = createDate("attendanceDate", java.time.LocalDate.class);

    public final StringPath checkInTime = createString("checkInTime");

    public final StringPath checkOutTime = createString("checkOutTime");

    public final StringPath code = createString("code");

    public final DateTimePath<java.time.LocalDateTime> createAt = createDateTime("createAt", java.time.LocalDateTime.class);

    public final StringPath employeeCode = createString("employeeCode");

    public final StringPath notes = createString("notes");

    public final NumberPath<java.math.BigDecimal> overtimeHours = createNumber("overtimeHours", java.math.BigDecimal.class);

    public final StringPath restaurantCode = createString("restaurantCode");

    public final EnumPath<com.restaurant.api.constant.AttendanceStatus> status = createEnum("status", com.restaurant.api.constant.AttendanceStatus.class);

    public final NumberPath<java.math.BigDecimal> workedHours = createNumber("workedHours", java.math.BigDecimal.class);

    public QAttendance(String variable) {
        super(Attendance.class, forVariable(variable));
    }

    public QAttendance(Path<? extends Attendance> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAttendance(PathMetadata metadata) {
        super(Attendance.class, metadata);
    }

}

