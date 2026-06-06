-- IBANK ("internet banking") was a never-implemented simulated placeholder with
-- no real gateway. Bank-app users can already pay via the interoperable Fonepay
-- QR, so it is redundant. No rows use it, so drop it from the payment_method
-- ENUM on both tables that store it.

ALTER TABLE payment
    MODIFY COLUMN payment_method
        ENUM ('CASH','POS','ESEWA','KHALTI','FONEPAY') NOT NULL;

ALTER TABLE receipt
    MODIFY COLUMN payment_method
        ENUM ('CASH','POS','ESEWA','KHALTI','FONEPAY') NOT NULL;
