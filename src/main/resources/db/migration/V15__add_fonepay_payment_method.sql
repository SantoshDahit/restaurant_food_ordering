-- Fonepay dynamic-QR is its own payment rail (distinct from the eSewa ePay
-- redirect). Add FONEPAY to the payment_method ENUM on both tables that store it.

ALTER TABLE payment
    MODIFY COLUMN payment_method
        ENUM ('CASH','POS','ESEWA','KHALTI','PHONEPAY','IBANK','FONEPAY') NOT NULL;

ALTER TABLE receipt
    MODIFY COLUMN payment_method
        ENUM ('CASH','POS','ESEWA','KHALTI','PHONEPAY','IBANK','FONEPAY') NOT NULL;
