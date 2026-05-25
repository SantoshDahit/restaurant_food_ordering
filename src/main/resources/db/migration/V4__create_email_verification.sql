-- =====================================================================
-- V4__create_email_verification.sql
-- Email verification PINs issued during sign-up / password reset.
-- 3-state flow: PENDING -> VERIFIED -> USED (or EXPIRED on timeout).
-- =====================================================================

CREATE TABLE email_verification (
    code         VARCHAR(255)                                                NOT NULL,
    email        VARCHAR(150)                                                NOT NULL,
    pin          VARCHAR(10)                                                 NOT NULL,
    purpose      ENUM ('JOIN', 'PASSWORD_RESET', 'CHANGE_EMAIL')             NOT NULL,
    status       ENUM ('PENDING', 'VERIFIED', 'USED', 'EXPIRED')             NOT NULL DEFAULT 'PENDING',
    verified_at  DATETIME                                                    NULL,
    expired_at   DATETIME                                                    NOT NULL,
    created_at   DATETIME                                                    NULL,
    updated_at   DATETIME                                                    NULL,
    deleted_at   DATETIME                                                    NULL,

    PRIMARY KEY (code),
    INDEX idx_email_verification_email (email),
    INDEX idx_email_verification_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
