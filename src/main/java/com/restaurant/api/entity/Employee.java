package com.restaurant.api.entity;

import com.restaurant.api.entity.base.BaseFullTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "employee")
public class Employee extends BaseFullTimeEntity {

    @Id
    @Column(name = "code")
    private String code;

    @Column(name = "restaurant_code", nullable = false)
    private String restaurantCode;

    @Column(name = "file_code")
    private String fileCode;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "join_date", nullable = false)
    private LocalDate joinDate;

    @Column(name = "base_salary", nullable = false, precision = 12, scale = 2)
    private BigDecimal baseSalary;

    @Column(name = "bank_account", length = 50)
    private String bankAccount;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public Employee(String code, String restaurantCode, String fullName, String phone,
                    LocalDate joinDate, BigDecimal baseSalary, String bankAccount, String bankName) {
        this.code = code;
        this.restaurantCode = restaurantCode;
        this.fullName = fullName;
        this.phone = phone;
        this.joinDate = joinDate;
        this.baseSalary = baseSalary;
        this.bankAccount = bankAccount;
        this.bankName = bankName;
    }

    public void update(String fullName, String phone, BigDecimal baseSalary,
                       String bankAccount, String bankName, String fileCode) {
        if (fullName != null) this.fullName = fullName;
        if (phone != null) this.phone = phone;
        if (baseSalary != null) this.baseSalary = baseSalary;
        if (bankAccount != null) this.bankAccount = bankAccount;
        if (bankName != null) this.bankName = bankName;
        if (fileCode != null) this.fileCode = fileCode;
    }

    public void deactivate() {
        this.isActive = false;
        this.softDelete();
    }
}
