package com.restaurant.api.service.facade;

import com.restaurant.api.annotation.Facade;
import com.restaurant.api.dto.EmailVerificationDto;
import com.restaurant.api.entity.EmailVerification;
import com.restaurant.api.mapper.EmailVerificationMapper;
import com.restaurant.api.service.EmailVerificationService;
import com.restaurant.api.service.email.EmailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Facade
@RequiredArgsConstructor
public class EmailVerificationFacade {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int PIN_MIN = 100000;
    private static final int PIN_RANGE = 900000; // 100000..999999 (6 digits)

    private final EmailVerificationService emailVerificationService;
    private final EmailVerificationMapper emailVerificationMapper;
    private final EmailSender emailSender;

    @Transactional
    public EmailVerificationDto.Response send(EmailVerificationDto.SendRequest request) {
        String email = request.email().trim().toLowerCase();
        String pin = generatePin();

        EmailVerification verification = emailVerificationService.create(email, pin, request.purpose());

        String subject = "RestaurantOS verification code";
        String body = String.format(
                "Your RestaurantOS verification code is: %s%n%nThis code expires in 10 minutes.",
                pin
        );
        emailSender.sendEmail(email, subject, body);

        return emailVerificationMapper.toResponse(verification);
    }

    @Transactional
    public EmailVerificationDto.Response verify(EmailVerificationDto.VerifyRequest request) {
        EmailVerification verification = emailVerificationService.verifyPin(
                request.code(),
                request.email().trim().toLowerCase(),
                request.pin(),
                request.purpose()
        );
        return emailVerificationMapper.toResponse(verification);
    }

    private String generatePin() {
        return String.valueOf(PIN_MIN + RANDOM.nextInt(PIN_RANGE));
    }
}
