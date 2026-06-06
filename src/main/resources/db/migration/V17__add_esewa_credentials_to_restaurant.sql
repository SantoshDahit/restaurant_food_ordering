-- Per-restaurant eSewa ePay merchant credentials, in their own table/domain
-- (one row per restaurant) for direct-to-merchant settlement: payments go
-- straight to each tenant's own eSewa merchant account via their own credentials.
-- Mirrors restaurant_fonepay.
--
-- product_code is a public-ish merchant identifier; secret_key is SENSITIVE and
-- stored ENCRYPTED (AES-256-GCM, base64 of iv+ciphertext) — hence the wide
-- VARCHAR. Never store it plain. The environment URLs stay global in config.

CREATE TABLE restaurant_esewa
(
    restaurant_code VARCHAR(255) NOT NULL,
    product_code    VARCHAR(255) NOT NULL,
    secret_key      VARCHAR(512) NOT NULL,
    enabled         BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      DATETIME     NOT NULL,
    updated_at      DATETIME     NULL,

    PRIMARY KEY (restaurant_code),
    CONSTRAINT fk_restaurant_esewa_restaurant
        FOREIGN KEY (restaurant_code) REFERENCES restaurant (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
