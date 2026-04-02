package com.restaurant.api.entity;

import com.restaurant.api.entity.base.BaseFullTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "restaurant")
public class Restaurant extends BaseFullTimeEntity {

    @Id
    @Column(name = "code")
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_code")
    private User user;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "business_number", nullable = false, length = 255)
    private String businessNumber;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency = "NPR";

    @Column(name = "file_code")
    private String fileCode;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public Restaurant(User user, String name, String address, String businessNumber,
                      String phone, String email, String currency) {
        this.code = UUID.randomUUID().toString();
        this.user = user;
        this.name = name;
        this.address = address;
        this.businessNumber = businessNumber;
        this.phone = phone;
        this.email = email;
        this.currency = currency != null ? currency : "NPR";
    }

    public void update(String name, String address, String phone, String email, String currency, String fileCode) {
        if (name != null) this.name = name;
        if (address != null) this.address = address;
        if (phone != null) this.phone = phone;
        if (email != null) this.email = email;
        if (currency != null) this.currency = currency;
        if (fileCode != null) this.fileCode = fileCode;
    }

    public void deactivate() {
        this.isActive = false;
        this.softDelete();
    }
}
