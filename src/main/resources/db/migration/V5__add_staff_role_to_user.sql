-- =====================================================================
-- V5__add_staff_role_to_user.sql
-- Add STAFF as a valid value for the user.role enum.
-- The Java UserRole enum already contains STAFF; this brings the DB into
-- sync so saving a STAFF user no longer fails the column constraint.
-- =====================================================================

ALTER TABLE user
    MODIFY COLUMN role ENUM ('ADMIN', 'MANAGER', 'STAFF') NOT NULL;
