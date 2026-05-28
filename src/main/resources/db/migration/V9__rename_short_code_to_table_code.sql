-- =====================================================================
-- V9__rename_short_code_to_table_code.sql
-- Rename restaurant_table.short_code → table_code for clarity.
-- =====================================================================

ALTER TABLE restaurant_table
    CHANGE COLUMN short_code table_code VARCHAR(20) NOT NULL;
