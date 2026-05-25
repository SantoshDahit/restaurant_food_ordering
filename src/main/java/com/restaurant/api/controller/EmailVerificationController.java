package com.restaurant.api.controller;

import com.restaurant.api.dto.EmailVerificationDto;
import com.restaurant.api.service.facade.EmailVerificationFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/email-verifications")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationFacade emailVerificationFacade;

    /**
     * Step 1: send a 6-digit PIN to the email. Creates a PENDING record.
     */
    @PostMapping
    public EmailVerificationDto.Response send(@Valid @RequestBody EmailVerificationDto.SendRequest request) {
        return emailVerificationFacade.send(request);
    }

    /**
     * Step 2: verify the PIN. Transitions PENDING -> VERIFIED.
     * The returned `code` is then sent along with the register request as `emailVerificationCode`.
     */
    @PostMapping("/verify")
    public EmailVerificationDto.Response verify(@Valid @RequestBody EmailVerificationDto.VerifyRequest request) {
        return emailVerificationFacade.verify(request);
    }
}
