package com.restaurant.api.entity;

import com.restaurant.api.constant.EmailVerificationPurpose;
import com.restaurant.api.constant.EmailVerificationStatus;
import com.restaurant.api.entity.base.BaseFullTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "email_verification")
public class EmailVerification extends BaseFullTimeEntity {

    private static final int EXPIRY_MINUTES = 10;

    @Id
    @Column(name = "code")
    private String code;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "pin", nullable = false, length = 10)
    private String pin;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false, length = 40)
    private EmailVerificationPurpose purpose;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EmailVerificationStatus status;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    public EmailVerification(String email, String pin, EmailVerificationPurpose purpose) {
        this.code = UUID.randomUUID().toString();
        this.email = email;
        this.pin = pin;
        this.purpose = purpose;
        this.status = EmailVerificationStatus.PENDING;
        this.expiredAt = LocalDateTime.now().plusMinutes(EXPIRY_MINUTES);
    }

    public void markVerified() {
        this.status = EmailVerificationStatus.VERIFIED;
        this.verifiedAt = LocalDateTime.now();
    }

    public void markUsed() {
        this.status = EmailVerificationStatus.USED;
    }

    public void markExpired() {
        this.status = EmailVerificationStatus.EXPIRED;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiredAt);
    }
}
