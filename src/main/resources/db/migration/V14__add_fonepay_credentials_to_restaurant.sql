-- Per-restaurant Fonepay merchant credentials, in their own table/domain
-- (one row per restaurant) for direct-to-merchant settlement: payments go
-- straight to each tenant's own bank account via their own credentials.
--
-- merchant_code is a public-ish identifier; username, password and secret_key
-- are SENSITIVE and stored ENCRYPTED (AES-256-GCM, base64 of iv+ciphertext) —
-- hence the wide VARCHARs. Never store them plain.

CREATE TABLE restaurant_fonepay
(
    restaurant_code VARCHAR(255) NOT NULL,
    merchant_code   VARCHAR(255) NOT NULL,
    username        VARCHAR(512) NOT NULL,
    password        VARCHAR(512) NOT NULL,
    secret_key      VARCHAR(512) NOT NULL,
    enabled         BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      DATETIME     NOT NULL,
    updated_at      DATETIME     NULL,

    PRIMARY KEY (restaurant_code),
    CONSTRAINT fk_restaurant_fonepay_restaurant
        FOREIGN KEY (restaurant_code) REFERENCES restaurant (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
