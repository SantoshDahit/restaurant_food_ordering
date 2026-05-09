-- =====================================================================
-- V3__add_kiosk_code_to_restaurant.sql
-- Add a short, public-facing kiosk_code to the restaurant table.
-- Backfill any pre-existing rows with a generated 6-char code so the
-- NOT NULL UNIQUE constraint can be applied.
-- =====================================================================

ALTER TABLE restaurant
    ADD COLUMN kiosk_code VARCHAR(20) NULL UNIQUE AFTER code;

UPDATE restaurant
SET kiosk_code = UPPER(SUBSTRING(REPLACE(UUID(), '-', ''), 1, 6))
WHERE kiosk_code IS NULL;

ALTER TABLE restaurant
    MODIFY COLUMN kiosk_code VARCHAR(20) NOT NULL;
