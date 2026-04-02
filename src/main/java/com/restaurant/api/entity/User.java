package com.restaurant.api.entity;

import com.restaurant.api.constant.UserRole;
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
@Table(name = "user")
public class User extends BaseFullTimeEntity {

    @Id
    @Column(name = "code")
    private String code;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(name = "email", unique = true, length = 150)
    private String email;

    @Column(name = "phone", unique = true, length = 20)
    private String phone;

    @Column(name = "password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Column(name = "file_code")
    private String fileCode;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public User(String fullName, String email, String phone, String passwordHash, UserRole role) {
        this.code = UUID.randomUUID().toString();
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public void update(String fullName, String phone, String fileCode) {
        if (fullName != null) this.fullName = fullName;
        if (phone != null) this.phone = phone;
        if (fileCode != null) this.fileCode = fileCode;
    }

    public void updatePassword(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void deactivate() {
        this.isActive = false;
        this.softDelete();
    }
}
