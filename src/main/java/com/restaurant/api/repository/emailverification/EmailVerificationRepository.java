package com.restaurant.api.repository.emailverification;

import com.restaurant.api.entity.EmailVerification;

import java.util.Optional;

public interface EmailVerificationRepository {
    Optional<EmailVerification> findByCode(String code);
    EmailVerification save(EmailVerification emailVerification);
}
