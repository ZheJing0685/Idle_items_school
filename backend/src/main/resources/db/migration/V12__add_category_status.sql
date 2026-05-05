-- Add status column to categories table
ALTER TABLE categories
ADD COLUMN `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用';