-- ---------------------------------------------------------------------
-- V7 — Platform super-admin account
-- ---------------------------------------------------------------------
-- Login: admin@platform.np / password123
-- The ADMIN role lets this user access /v1/admin/** endpoints (cross-restaurant
-- platform views). It is NOT linked to any restaurant.
-- ---------------------------------------------------------------------

INSERT INTO user (code, full_name, email, phone, password_hash, role, is_active, created_at, updated_at)
SELECT UUID(), 'Platform Admin', 'admin@platform.np', '9800000000',
       '$2a$10$wEDsyK40EAAp7z8Au6DXouR2LQp4rPK1Qg9uIxlk9krb8YfnnADdO', 'ADMIN', 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM user WHERE email = 'admin@platform.np');
