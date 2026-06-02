-- Add SERVED to the order lifecycle (READY → SERVED → COMPLETED).
-- SERVED = food delivered, customer still seated (order stays active, table
-- stays occupied); COMPLETED closes the order and frees the table.
-- Both orders.status and order_item.status are MySQL ENUMs, so the allowed
-- value set must be widened explicitly.

ALTER TABLE orders
    MODIFY COLUMN status
        ENUM ('PENDING','CONFIRMED','PREPARING','READY','SERVED','COMPLETED','CANCELLED')
        NOT NULL DEFAULT 'PENDING';

ALTER TABLE order_item
    MODIFY COLUMN status
        ENUM ('PENDING','CONFIRMED','PREPARING','READY','SERVED','COMPLETED','CANCELLED')
        NOT NULL DEFAULT 'PENDING';
