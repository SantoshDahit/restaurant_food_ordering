package com.restaurant.api.repository.attendance;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.restaurant.api.constant.AttendanceStatus;
import com.restaurant.api.dto.AttendanceDto;
import com.restaurant.api.entity.Attendance;
import com.restaurant.api.entity.QAttendance;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AttendanceQueryRepository {
    private final JPAQueryFactory queryFactory;
    private static final QAttendance attendance = QAttendance.attendance;

    public Page<Attendance> search(AttendanceDto.SearchRequest searchRequest, Pageable pageable) {
        List<Attendance> result = queryFactory
                .selectFrom(attendance)
                .where(
                        eqRestaurantCode(searchRequest.restaurantCode()),
                        eqEmployeeCode(searchRequest.employeeCode()),
                        goeAttendanceDateFrom(searchRequest.dateFrom()),
                        loeAttendanceDateTo(searchRequest.dateTo()),
                        eqStatus(searchRequest.status())
                )
                .orderBy(attendance.attendanceDate.desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        Long count = queryFactory.select(com.querydsl.core.types.dsl.Wildcard.count)
                .from(attendance)
                .where(
                        eqRestaurantCode(searchRequest.restaurantCode()),
                        eqEmployeeCode(searchRequest.employeeCode()),
                        goeAttendanceDateFrom(searchRequest.dateFrom()),
                        loeAttendanceDateTo(searchRequest.dateTo()),
                        eqStatus(searchRequest.status())
                )
                .fetchOne();

        return new PageImpl<>(result, pageable, count != null ? count : 0L);
    }

    private BooleanExpression eqRestaurantCode(String restaurantCode) {
        if (!StringUtils.hasText(restaurantCode)) return null;
        return attendance.restaurantCode.eq(restaurantCode);
    }

    private BooleanExpression eqEmployeeCode(String employeeCode) {
        if (!StringUtils.hasText(employeeCode)) return null;
        return attendance.employeeCode.eq(employeeCode);
    }

    private BooleanExpression goeAttendanceDateFrom(LocalDate dateFrom) {
        if (dateFrom == null) return null;
        return attendance.attendanceDate.goe(dateFrom);
    }

    private BooleanExpression loeAttendanceDateTo(LocalDate dateTo) {
        if (dateTo == null) return null;
        return attendance.attendanceDate.loe(dateTo);
    }

    private BooleanExpression eqStatus(AttendanceStatus status) {
        if (status == null) return null;
        return attendance.status.eq(status);
    }
}
