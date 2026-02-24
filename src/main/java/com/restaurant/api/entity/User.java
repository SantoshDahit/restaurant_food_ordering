package com.restaurant.api.entity;

import com.restaurant.api.common.BaseFullEntity;
import com.restaurant.api.constant.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "user")
public class User extends BaseFullEntity {

    @Id
    @Column(name = "code")
    private String code;

    @Column(name = "restaurant_code")
    private String restaurantCode;

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

    public User(String code, String restaurantCode, String fullName, String email,
                String phone, String passwordHash, UserRole role) {
        this.code = code;
        this.restaurantCode = restaurantCode;
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

    public void updateRestaurantCode(String restaurantCode) {
        if (restaurantCode != null && !restaurantCode.isBlank()) {
            this.restaurantCode = restaurantCode;
        }
    }

    public void updatePassword(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void deactivate() {
        this.isActive = false;
        this.softDelete();
    }
}
