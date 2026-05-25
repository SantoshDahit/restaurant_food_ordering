package com.restaurant.api.repository.emailverification;

import com.restaurant.api.entity.EmailVerification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EmailVerificationRepositoryImpl implements EmailVerificationRepository {
    private final EmailVerificationJpaRepository emailVerificationJpaRepository;

    @Override
    public Optional<EmailVerification> findByCode(String code) {
        return emailVerificationJpaRepository.findByCodeAndDeletedAtIsNull(code);
    }

    @Override
    public EmailVerification save(EmailVerification emailVerification) {
        return emailVerificationJpaRepository.save(emailVerification);
    }
}
