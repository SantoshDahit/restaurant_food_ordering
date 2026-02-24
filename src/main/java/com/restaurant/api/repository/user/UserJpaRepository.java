package com.restaurant.api.repository.user;

import com.restaurant.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<User, String> {
    Optional<User> findByCodeAndDeleteAtIsNull(String code);
    Optional<User> findByEmailAndDeleteAtIsNull(String email);
    boolean existsByEmailAndDeleteAtIsNull(String email);
}
