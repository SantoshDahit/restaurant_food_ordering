-- PHONEPAY was a never-implemented placeholder and is redundant now that FONEPAY
-- (the real Fonepay dynamic-QR rail) exists. No rows use it, so drop it from the
-- payment_method ENUM on both tables that store it.

ALTER TABLE payment
    MODIFY COLUMN payment_method
        ENUM ('CASH','POS','ESEWA','KHALTI','IBANK','FONEPAY') NOT NULL;

ALTER TABLE receipt
    MODIFY COLUMN payment_method
        ENUM ('CASH','POS','ESEWA','KHALTI','IBANK','FONEPAY') NOT NULL;
