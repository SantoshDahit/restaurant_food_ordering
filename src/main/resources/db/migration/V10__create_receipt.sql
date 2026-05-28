-- =====================================================================
-- V10__create_receipt.sql
-- Immutable receipt records. Issued at payment time. Stores snapshots of
-- the order/restaurant/items so the receipt remains accurate even if the
-- source data is later edited or soft-deleted. Gateway columns are nullable
-- and populated once real payment gateways (eSewa/Khalti/etc.) are wired.
--
-- receipt_number is a customer-facing 3-digit ticket (100-999) that resets
-- every business day per restaurant — the number the customer reads off
-- their printed slip to pick up their order. Combined with business_date
-- and restaurant_code it's unique. `code` remains the system-wide UUID.
-- =====================================================================

CREATE TABLE receipt (
    code                       VARCHAR(36) PRIMARY KEY,

    -- Customer-facing ticket: 100-999, per restaurant per day
    receipt_number             INT NOT NULL,
    business_date              DATE NOT NULL,

    -- FKs (soft references — match how other entities relate)
    restaurant_code            VARCHAR(36) NOT NULL,
    order_code                 VARCHAR(36) NOT NULL,
    payment_code               VARCHAR(36) NOT NULL,

    -- Totals (snapshot)
    subtotal                   DECIMAL(12, 2) NOT NULL,
    discount_amount            DECIMAL(12, 2) NOT NULL DEFAULT 0,
    tax_amount                 DECIMAL(12, 2) NOT NULL DEFAULT 0,
    total_amount               DECIMAL(12, 2) NOT NULL,

    -- Payment (snapshot) — must match the ENUM types on the payment table
    payment_method             ENUM ('CASH','POS','ESEWA','KHALTI','PHONEPAY','IBANK') NOT NULL,
    payment_status             ENUM ('PENDING','COMPLETED','FAILED','REFUNDED') NOT NULL,

    -- Gateway integration (nullable until a real gateway is wired)
    gateway_provider           VARCHAR(30) NULL,
    gateway_transaction_id     VARCHAR(120) NULL,
    gateway_response_raw       TEXT NULL,

    -- Display snapshots (so we can render without joins / after edits)
    restaurant_name_snapshot   VARCHAR(255) NOT NULL,
    order_number_snapshot      VARCHAR(50) NOT NULL,
    table_number_snapshot      VARCHAR(20) NULL,
    items_json                 LONGTEXT NOT NULL,

    -- Customer contact (for digital receipt delivery — optional)
    customer_name              VARCHAR(120) NULL,
    customer_email             VARCHAR(255) NULL,
    customer_phone             VARCHAR(40) NULL,

    notes                      TEXT NULL,
    issued_at                  DATETIME NOT NULL,

    -- Audit (matches BaseFullEntity)
    created_at                 DATETIME NOT NULL,
    updated_at                 DATETIME NOT NULL,
    deleted_at                 DATETIME NULL,

    UNIQUE KEY uk_receipt_daily (restaurant_code, business_date, receipt_number),
    INDEX idx_receipt_order (order_code),
    INDEX idx_receipt_payment (payment_code),
    INDEX idx_receipt_restaurant_date (restaurant_code, business_date),
    INDEX idx_receipt_issued_at (issued_at)
);
