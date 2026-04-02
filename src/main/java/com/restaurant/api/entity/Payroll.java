package com.restaurant.api.entity;

import com.restaurant.api.constant.SalaryStatus;
import com.restaurant.api.entity.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "payroll")
@EntityListeners(AuditingEntityListener.class)
public class Payroll extends BaseTimeEntity {

    @Id
    @Column(name = "code")
    private String code;

    @Column(name = "restaurant_code", nullable = false)
    private String restaurantCode;

    @Column(name = "employee_code", nullable = false)
    private String employeeCode;

    @Column(name = "pay_period_start", nullable = false)
    private LocalDate payPeriodStart;

    @Column(name = "pay_period_end", nullable = false)
    private LocalDate payPeriodEnd;

    @Column(name = "overtime_pay", nullable = false, precision = 12, scale = 2)
    private BigDecimal overtimePay = BigDecimal.ZERO;

    @Column(name = "bonus", nullable = false, precision = 12, scale = 2)
    private BigDecimal bonus = BigDecimal.ZERO;

    @Column(name = "deductions", nullable = false, precision = 12, scale = 2)
    private BigDecimal deductions = BigDecimal.ZERO;

    @Column(name = "net_salary", nullable = false, precision = 12, scale = 2)
    private BigDecimal netSalary;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SalaryStatus status = SalaryStatus.PENDING;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    public Payroll(String restaurantCode, String employeeCode,
                   LocalDate payPeriodStart, LocalDate payPeriodEnd,
                   BigDecimal overtimePay, BigDecimal bonus, BigDecimal deductions, BigDecimal netSalary) {
        this.code = UUID.randomUUID().toString();
        this.restaurantCode = restaurantCode;
        this.employeeCode = employeeCode;
        this.payPeriodStart = payPeriodStart;
        this.payPeriodEnd = payPeriodEnd;
        this.overtimePay = overtimePay != null ? overtimePay : BigDecimal.ZERO;
        this.bonus = bonus != null ? bonus : BigDecimal.ZERO;
        this.deductions = deductions != null ? deductions : BigDecimal.ZERO;
        this.netSalary = netSalary;
    }

    public void markPaid() {
        this.status = SalaryStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    public void updateStatus(SalaryStatus status) {
        this.status = status;
        if (status == SalaryStatus.PAID) {
            this.paidAt = LocalDateTime.now();
        }
    }
}
