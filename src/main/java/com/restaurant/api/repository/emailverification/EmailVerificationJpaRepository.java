package com.restaurant.api.repository.emailverification;

import com.restaurant.api.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationJpaRepository extends JpaRepository<EmailVerification, String> {
    Optional<EmailVerification> findByCodeAndDeletedAtIsNull(String code);
}
