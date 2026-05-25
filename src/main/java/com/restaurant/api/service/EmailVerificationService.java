package com.restaurant.api.service;

import com.restaurant.api.constant.EmailVerificationPurpose;
import com.restaurant.api.constant.EmailVerificationStatus;
import com.restaurant.api.entity.EmailVerification;
import com.restaurant.api.exception.ApiException;
import com.restaurant.api.exception.ErrorCode;
import com.restaurant.api.repository.emailverification.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;

    @Transactional(readOnly = true)
    public EmailVerification getByCode(String code) {
        return emailVerificationRepository.findByCode(code)
                .orElseThrow(() -> new ApiException(ErrorCode.EMAIL_VERIFICATION_NOT_FOUND));
    }

    @Transactional
    public EmailVerification create(String email, String pin, EmailVerificationPurpose purpose) {
        EmailVerification verification = new EmailVerification(email, pin, purpose);
        return emailVerificationRepository.save(verification);
    }

    /**
     * Verify the supplied PIN matches the stored record. Transitions PENDING → VERIFIED.
     * Called by the second step of the sign-up flow (after user types the PIN).
     */
    @Transactional
    public EmailVerification verifyPin(String code, String email, String pin, EmailVerificationPurpose purpose) {
        EmailVerification verification = getByCode(code);

        if (verification.isExpired()) {
            verification.markExpired();
            emailVerificationRepository.save(verification);
            throw new ApiException(ErrorCode.EMAIL_VERIFICATION_EXPIRED);
        }
        if (!verification.getEmail().equalsIgnoreCase(email)) {
            throw new ApiException(ErrorCode.EMAIL_VERIFICATION_EMAIL_MISMATCH);
        }
        if (!verification.getPurpose().equals(purpose)) {
            throw new ApiException(ErrorCode.EMAIL_VERIFICATION_PURPOSE_MISMATCH);
        }
        if (!verification.getPin().equals(pin)) {
            throw new ApiException(ErrorCode.EMAIL_VERIFICATION_PIN_MISMATCH);
        }

        verification.markVerified();
        return emailVerificationRepository.save(verification);
    }

    /**
     * Final consumption step: an authenticated action (e.g. AuthFacade.register) confirms
     * this verification was used and locks it. Transitions VERIFIED → USED. Throws if the
     * record is not VERIFIED or fields do not match.
     */
    @Transactional
    public void markUsed(String code, String email, EmailVerificationPurpose purpose) {
        EmailVerification verification = getByCode(code);

        if (verification.getStatus() == EmailVerificationStatus.USED) {
            throw new ApiException(ErrorCode.EMAIL_VERIFICATION_ALREADY_USED);
        }
        if (verification.getStatus() != EmailVerificationStatus.VERIFIED) {
            throw new ApiException(ErrorCode.EMAIL_VERIFICATION_NOT_VERIFIED);
        }
        if (!verification.getEmail().equalsIgnoreCase(email)) {
            throw new ApiException(ErrorCode.EMAIL_VERIFICATION_EMAIL_MISMATCH);
        }
        if (!verification.getPurpose().equals(purpose)) {
            throw new ApiException(ErrorCode.EMAIL_VERIFICATION_PURPOSE_MISMATCH);
        }

        verification.markUsed();
        emailVerificationRepository.save(verification);
    }
}
