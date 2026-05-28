-- =====================================================================
-- V12__add_ticket_number_to_orders.sql
-- Move the customer-facing daily ticket onto the order itself (instead of
-- only living on the receipt). Tickets are assigned at order creation so
-- that dine-in / waiter / kiosk orders display a ticket even before a
-- payment / receipt exists.
--
-- The receipt's receipt_number continues to mirror the order's ticket
-- (one customer-facing number per order, printed on the slip).
-- =====================================================================

ALTER TABLE orders
    ADD COLUMN ticket_number INT NULL AFTER order_number,
    ADD COLUMN business_date DATE NULL AFTER ticket_number,
    ADD INDEX idx_orders_restaurant_business_date (restaurant_code, business_date);
