package com.restaurant.api.repository.user;

import com.restaurant.api.constant.UserRole;
import com.restaurant.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<User, String> {
    Optional<User> findByCodeAndDeletedAtIsNull(String code);
    Optional<User> findByEmailAndDeletedAtIsNull(String email);
    boolean existsByEmailAndDeletedAtIsNull(String email);
    long countByRoleAndDeletedAtIsNull(UserRole role);
    long countByDeletedAtIsNull();
}
