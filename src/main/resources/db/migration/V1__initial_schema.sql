-- =====================================================================
-- V1__initial_schema.sql
-- Restaurant Food Ordering System — Full DDL
-- =====================================================================

-- ─────────────────────────────────────────────────────────────────────
-- 1. file
-- ─────────────────────────────────────────────────────────────────────
CREATE TABLE file
(
    code       VARCHAR(255)                    NOT NULL,
    type       ENUM ('IMAGE','PDF','DOCUMENT') NOT NULL,
    url        VARCHAR(1000)                   NULL,
    is_success TINYINT(1)                      NOT NULL DEFAULT 0,
    create_at  DATETIME                        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_at  DATETIME                        NULL,

    PRIMARY KEY (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────────────
-- 2. user
-- ─────────────────────────────────────────────────────────────────────
CREATE TABLE user
(
    code            VARCHAR(255)            NOT NULL,
    restaurant_code VARCHAR(255)            NULL,
    full_name       VARCHAR(200)            NOT NULL,
    email           VARCHAR(150)            NULL,
    phone           VARCHAR(20)             NULL,
    password_hash   VARCHAR(255)            NULL,
    role            ENUM ('ADMIN','MANAGER') NOT NULL,
    file_code       VARCHAR(255)            NULL,
    is_active       TINYINT(1)              NOT NULL DEFAULT 1,
    create_at       DATETIME                NULL,
    update_at       DATETIME                NULL,
    delete_at       DATETIME                NULL,

    PRIMARY KEY (code),
    UNIQUE KEY uk_user_email (email),
    UNIQUE KEY uk_user_phone (phone),
    CONSTRAINT fk_user_file FOREIGN KEY (file_code) REFERENCES file (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────────────
-- 3. restaurant
-- ─────────────────────────────────────────────────────────────────────
CREATE TABLE restaurant
(
    code            VARCHAR(255) NOT NULL,
    user_code       VARCHAR(255) NULL,
    name            VARCHAR(255) NOT NULL,
    address         VARCHAR(255) NOT NULL,
    business_number VARCHAR(255) NOT NULL,
    phone           VARCHAR(20)  NULL,
    email           VARCHAR(150) NULL,
    currency        VARCHAR(10)  NOT NULL DEFAULT 'NPR',
    file_code       VARCHAR(255) NULL,
    is_active       TINYINT(1)   NOT NULL DEFAULT 1,
    create_at       DATETIME     NULL,
    update_at       DATETIME     NULL,
    delete_at       DATETIME     NULL,

    PRIMARY KEY (code),
    CONSTRAINT fk_restaurant_user FOREIGN KEY (user_code)  REFERENCES user (code),
    CONSTRAINT fk_restaurant_file FOREIGN KEY (file_code)  REFERENCES file (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────────────
-- 4. restaurant_table
-- ─────────────────────────────────────────────────────────────────────
CREATE TABLE restaurant_table
(
    code            VARCHAR(255)                                        NOT NULL,
    restaurant_code VARCHAR(255)                                        NOT NULL,
    table_number    VARCHAR(20)                                         NOT NULL,
    capacity        INT                                                 NOT NULL DEFAULT 4,
    status          ENUM ('AVAILABLE','OCCUPIED','RESERVED','CLEANING') NOT NULL DEFAULT 'AVAILABLE',
    qr_code_url     VARCHAR(1000)                                       NULL,
    qr_code_token   VARCHAR(100)                                        NULL,
    is_active       TINYINT(1)                                          NOT NULL DEFAULT 1,
    create_at       DATETIME                                            NULL,
    update_at       DATETIME                                            NULL,
    delete_at       DATETIME                                            NULL,

    PRIMARY KEY (code),
    UNIQUE KEY uk_table_qr_token (qr_code_token),
    CONSTRAINT fk_table_restaurant FOREIGN KEY (restaurant_code) REFERENCES restaurant (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────────────
-- 5. menu_category
-- ─────────────────────────────────────────────────────────────────────
CREATE TABLE menu_category
(
    code            VARCHAR(255)                                                              NOT NULL,
    restaurant_code VARCHAR(255)                                                              NOT NULL,
    name            VARCHAR(100)                                                              NOT NULL,
    category_type   ENUM ('VEG','NON_VEG','DRINKS','SPECIALS','DESSERTS','APPETIZERS','SIDES') NULL,
    file_code       VARCHAR(255)                                                              NULL,
    sort_order      INT                                                                       NOT NULL DEFAULT 0,
    is_active       TINYINT(1)                                                                NOT NULL DEFAULT 1,
    create_at       DATETIME                                                                  NULL,
    update_at       DATETIME                                                                  NULL,
    delete_at       DATETIME                                                                  NULL,

    PRIMARY KEY (code),
    CONSTRAINT fk_category_restaurant FOREIGN KEY (restaurant_code) REFERENCES restaurant (code),
    CONSTRAINT fk_category_file       FOREIGN KEY (file_code)       REFERENCES file (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────────────
-- 6. menu_item
-- ─────────────────────────────────────────────────────────────────────
CREATE TABLE menu_item
(
    code              VARCHAR(255)                               NOT NULL,
    restaurant_code   VARCHAR(255)                               NOT NULL,
    category_code     VARCHAR(255)                               NULL,
    name              VARCHAR(200)                               NOT NULL,
    description       TEXT                                       NULL,
    price             DECIMAL(10, 2)                             NOT NULL,
    discount_percent  DECIMAL(5, 2)                              NOT NULL DEFAULT 0.00,
    file_code         VARCHAR(255)                               NULL,
    availability      ENUM ('AVAILABLE','OUT_OF_STOCK','HIDDEN') NOT NULL DEFAULT 'AVAILABLE',
    is_featured       TINYINT(1)                                 NOT NULL DEFAULT 0,
    is_veg            TINYINT(1)                                 NOT NULL DEFAULT 0,
    prep_time_minutes INT                                        NOT NULL DEFAULT 15,
    sort_order        INT                                        NOT NULL DEFAULT 0,
    create_at         DATETIME                                   NULL,
    update_at         DATETIME                                   NULL,
    delete_at         DATETIME                                   NULL,

    PRIMARY KEY (code),
    CONSTRAINT fk_item_restaurant FOREIGN KEY (restaurant_code) REFERENCES restaurant (code),
    CONSTRAINT fk_item_category   FOREIGN KEY (category_code)   REFERENCES menu_category (code),
    CONSTRAINT fk_item_file       FOREIGN KEY (file_code)       REFERENCES file (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────────────
-- 7. orders
-- ─────────────────────────────────────────────────────────────────────
CREATE TABLE orders
(
    code            VARCHAR(255)                                                                    NOT NULL,
    restaurant_code VARCHAR(255)                                                                    NOT NULL,
    table_code      VARCHAR(255)                                                                    NULL,
    waiter_code     VARCHAR(255)                                                                    NULL,
    order_number    VARCHAR(30)                                                                     NOT NULL,
    order_type      ENUM ('DINE_IN','TAKEAWAY','QR_ORDER','KIOSK')                                 NOT NULL DEFAULT 'DINE_IN',
    status          ENUM ('PENDING','CONFIRMED','PREPARING','READY','COMPLETED','CANCELLED')        NOT NULL DEFAULT 'PENDING',
    subtotal        DECIMAL(12, 2)                                                                  NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(12, 2)                                                                  NOT NULL DEFAULT 0.00,
    tax_amount      DECIMAL(12, 2)                                                                  NOT NULL DEFAULT 0.00,
    total_amount    DECIMAL(12, 2)                                                                  NOT NULL DEFAULT 0.00,
    special_notes   TEXT                                                                            NULL,
    device_type     VARCHAR(30)                                                                     NULL,
    create_at       DATETIME                                                                        NULL,
    update_at       DATETIME                                                                        NULL,
    delete_at       DATETIME                                                                        NULL,

    PRIMARY KEY (code),
    UNIQUE KEY uk_order_number (order_number),
    CONSTRAINT fk_order_restaurant FOREIGN KEY (restaurant_code) REFERENCES restaurant (code),
    CONSTRAINT fk_order_table      FOREIGN KEY (table_code)      REFERENCES restaurant_table (code),
    CONSTRAINT fk_order_waiter     FOREIGN KEY (waiter_code)     REFERENCES user (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────────────
-- 8. order_item
-- ─────────────────────────────────────────────────────────────────────
CREATE TABLE order_item
(
    code            VARCHAR(255)                                                             NOT NULL,
    order_code      VARCHAR(255)                                                             NOT NULL,
    menu_item_code  VARCHAR(255)                                                             NOT NULL,
    quantity        INT                                                                      NOT NULL DEFAULT 1,
    unit_price      DECIMAL(10, 2)                                                           NOT NULL,
    discount_amount DECIMAL(10, 2)                                                           NOT NULL DEFAULT 0.00,
    total_price     DECIMAL(10, 2)                                                           NOT NULL,
    spice_level     VARCHAR(20)                                                              NULL,
    notes           TEXT                                                                     NULL,
    status          ENUM ('PENDING','CONFIRMED','PREPARING','READY','COMPLETED','CANCELLED') NOT NULL DEFAULT 'PENDING',
    create_at       DATETIME                                                                 NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (code),
    CONSTRAINT fk_order_item_order     FOREIGN KEY (order_code)     REFERENCES orders (code),
    CONSTRAINT fk_order_item_menu_item FOREIGN KEY (menu_item_code) REFERENCES menu_item (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────────────
-- 9. payment
-- ─────────────────────────────────────────────────────────────────────
CREATE TABLE payment
(
    code            VARCHAR(255)                                        NOT NULL,
    restaurant_code VARCHAR(255)                                        NOT NULL,
    order_code      VARCHAR(255)                                        NOT NULL,
    processed_by    VARCHAR(255)                                        NULL,
    payment_method  ENUM ('CASH','POS','ESEWA','KHALTI','PHONEPAY','IBANK') NOT NULL,
    amount          DECIMAL(12, 2)                                      NOT NULL,
    status          ENUM ('PENDING','COMPLETED','FAILED','REFUNDED')    NOT NULL DEFAULT 'PENDING',
    transaction_ref VARCHAR(200)                                        NULL,
    receipt_number  VARCHAR(100)                                        NULL,
    refunded_amount DECIMAL(12, 2)                                      NOT NULL DEFAULT 0.00,
    processed_at    DATETIME                                            NULL,
    create_at       DATETIME                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_at       DATETIME                                            NULL,

    PRIMARY KEY (code),
    CONSTRAINT fk_payment_restaurant FOREIGN KEY (restaurant_code) REFERENCES restaurant (code),
    CONSTRAINT fk_payment_order      FOREIGN KEY (order_code)      REFERENCES orders (code),
    CONSTRAINT fk_payment_user       FOREIGN KEY (processed_by)    REFERENCES user (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────────────
-- 10. employee
-- ─────────────────────────────────────────────────────────────────────
CREATE TABLE employee
(
    code            VARCHAR(255)   NOT NULL,
    restaurant_code VARCHAR(255)   NOT NULL,
    file_code       VARCHAR(255)   NULL,
    full_name       VARCHAR(200)   NOT NULL,
    phone           VARCHAR(20)    NULL,
    join_date       DATE           NOT NULL,
    base_salary     DECIMAL(12, 2) NOT NULL,
    bank_account    VARCHAR(50)    NULL,
    bank_name       VARCHAR(100)   NULL,
    is_active       TINYINT(1)     NOT NULL DEFAULT 1,
    create_at       DATETIME       NULL,
    update_at       DATETIME       NULL,
    delete_at       DATETIME       NULL,

    PRIMARY KEY (code),
    CONSTRAINT fk_employee_restaurant FOREIGN KEY (restaurant_code) REFERENCES restaurant (code),
    CONSTRAINT fk_employee_file       FOREIGN KEY (file_code)       REFERENCES file (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────────────
-- 11. attendance
-- ─────────────────────────────────────────────────────────────────────
CREATE TABLE attendance
(
    code            VARCHAR(255)                                           NOT NULL,
    employee_code   VARCHAR(255)                                           NOT NULL,
    restaurant_code VARCHAR(255)                                           NOT NULL,
    attendance_date DATE                                                   NOT NULL,
    status          ENUM ('PRESENT','ABSENT','HALF_DAY','LEAVE','HOLIDAY') NOT NULL DEFAULT 'PRESENT',
    check_in_time   VARCHAR(10)                                            NULL,
    check_out_time  VARCHAR(10)                                            NULL,
    worked_hours    DECIMAL(4, 2)                                          NULL,
    overtime_hours  DECIMAL(4, 2)                                          NOT NULL DEFAULT 0.00,
    notes           TEXT                                                   NULL,
    create_at       DATETIME                                               NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (code),
    CONSTRAINT fk_attendance_employee   FOREIGN KEY (employee_code)   REFERENCES employee (code),
    CONSTRAINT fk_attendance_restaurant FOREIGN KEY (restaurant_code) REFERENCES restaurant (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────────────
-- 12. payroll
-- ─────────────────────────────────────────────────────────────────────
CREATE TABLE payroll
(
    code             VARCHAR(255)                   NOT NULL,
    restaurant_code  VARCHAR(255)                   NOT NULL,
    employee_code    VARCHAR(255)                   NOT NULL,
    pay_period_start DATE                           NOT NULL,
    pay_period_end   DATE                           NOT NULL,
    overtime_pay     DECIMAL(12, 2)                 NOT NULL DEFAULT 0.00,
    bonus            DECIMAL(12, 2)                 NOT NULL DEFAULT 0.00,
    deductions       DECIMAL(12, 2)                 NOT NULL DEFAULT 0.00,
    net_salary       DECIMAL(12, 2)                 NOT NULL,
    status           ENUM ('PENDING','PAID','ON_HOLD') NOT NULL DEFAULT 'PENDING',
    paid_at          DATETIME                       NULL,
    create_at        DATETIME                       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_at        DATETIME                       NULL,

    PRIMARY KEY (code),
    CONSTRAINT fk_payroll_restaurant FOREIGN KEY (restaurant_code) REFERENCES restaurant (code),
    CONSTRAINT fk_payroll_employee   FOREIGN KEY (employee_code)   REFERENCES employee (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
