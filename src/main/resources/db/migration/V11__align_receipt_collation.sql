-- =====================================================================
-- V11__align_receipt_collation.sql
-- V10 was created with the MySQL 8 default collation (utf8mb4_0900_ai_ci)
-- while every existing table uses utf8mb4_unicode_ci. Joins between
-- receipt.order_code and payment.order_code therefore fail with
-- "Illegal mix of collations". Align the entire receipt table.
-- =====================================================================

ALTER TABLE receipt CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
