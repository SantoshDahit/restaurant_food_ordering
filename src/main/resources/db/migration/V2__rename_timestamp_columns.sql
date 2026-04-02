-- =====================================================================
-- V2__rename_timestamp_columns.sql
-- Rename timestamp columns to match naming convention (created_at, updated_at, deleted_at)
-- =====================================================================

-- file
ALTER TABLE file CHANGE COLUMN create_at created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE file CHANGE COLUMN update_at updated_at DATETIME NULL;

-- user
ALTER TABLE user CHANGE COLUMN create_at created_at DATETIME NULL;
ALTER TABLE user CHANGE COLUMN update_at updated_at DATETIME NULL;
ALTER TABLE user CHANGE COLUMN delete_at deleted_at DATETIME NULL;

-- restaurant
ALTER TABLE restaurant CHANGE COLUMN create_at created_at DATETIME NULL;
ALTER TABLE restaurant CHANGE COLUMN update_at updated_at DATETIME NULL;
ALTER TABLE restaurant CHANGE COLUMN delete_at deleted_at DATETIME NULL;

-- restaurant_table
ALTER TABLE restaurant_table CHANGE COLUMN create_at created_at DATETIME NULL;
ALTER TABLE restaurant_table CHANGE COLUMN update_at updated_at DATETIME NULL;
ALTER TABLE restaurant_table CHANGE COLUMN delete_at deleted_at DATETIME NULL;

-- menu_category
ALTER TABLE menu_category CHANGE COLUMN create_at created_at DATETIME NULL;
ALTER TABLE menu_category CHANGE COLUMN update_at updated_at DATETIME NULL;
ALTER TABLE menu_category CHANGE COLUMN delete_at deleted_at DATETIME NULL;

-- menu_item
ALTER TABLE menu_item CHANGE COLUMN create_at created_at DATETIME NULL;
ALTER TABLE menu_item CHANGE COLUMN update_at updated_at DATETIME NULL;
ALTER TABLE menu_item CHANGE COLUMN delete_at deleted_at DATETIME NULL;

-- orders
ALTER TABLE orders CHANGE COLUMN create_at created_at DATETIME NULL;
ALTER TABLE orders CHANGE COLUMN update_at updated_at DATETIME NULL;
ALTER TABLE orders CHANGE COLUMN delete_at deleted_at DATETIME NULL;

-- order_item
ALTER TABLE order_item CHANGE COLUMN create_at created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- payment
ALTER TABLE payment CHANGE COLUMN create_at created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE payment CHANGE COLUMN update_at updated_at DATETIME NULL;

-- employee
ALTER TABLE employee CHANGE COLUMN create_at created_at DATETIME NULL;
ALTER TABLE employee CHANGE COLUMN update_at updated_at DATETIME NULL;
ALTER TABLE employee CHANGE COLUMN delete_at deleted_at DATETIME NULL;

-- attendance
ALTER TABLE attendance CHANGE COLUMN create_at created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- payroll
ALTER TABLE payroll CHANGE COLUMN create_at created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE payroll CHANGE COLUMN update_at updated_at DATETIME NULL;
