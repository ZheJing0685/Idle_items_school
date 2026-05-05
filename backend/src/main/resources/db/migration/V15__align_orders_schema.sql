-- ============================================================
-- V15: Align orders table schema with Order entity (idempotent)
-- All columns may already exist from V1/V2/V10. Use
-- information_schema to check before adding/modifying.
-- ============================================================

-- 1. Ensure deliver_time exists (V1 created it, but check anyway)
SET @deliver_time_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'deliver_time'
);
SET @deliver_time_sql := IF(
    @deliver_time_exists = 0,
    'ALTER TABLE orders ADD COLUMN deliver_time DATETIME NULL COMMENT "确认收货时间" AFTER ship_time',
    'SELECT 1 AS deliver_time_exists'
);
PREPARE stmt FROM @deliver_time_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. Ensure tracking_number exists (V10 may have added it)
SET @tracking_number_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'tracking_number'
);
SET @tracking_number_sql := IF(
    @tracking_number_exists = 0,
    'ALTER TABLE orders ADD COLUMN tracking_number VARCHAR(100) NULL COMMENT "快递单号" AFTER ship_time',
    'SELECT 1 AS tracking_number_exists'
);
PREPARE stmt FROM @tracking_number_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. Ensure shipping_company exists (V2 created it as VARCHAR(50))
SET @shipping_company_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'shipping_company'
);
SET @shipping_company_add_sql := IF(
    @shipping_company_exists = 0,
    'ALTER TABLE orders ADD COLUMN shipping_company VARCHAR(100) NULL COMMENT "快递公司" AFTER tracking_number',
    'ALTER TABLE orders MODIFY COLUMN shipping_company VARCHAR(100) COMMENT "快递公司"'
);
PREPARE stmt FROM @shipping_company_add_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. Align VARCHAR lengths
ALTER TABLE orders
    MODIFY COLUMN item_title VARCHAR(200) NOT NULL COMMENT '物品标题',
    MODIFY COLUMN buyer_address VARCHAR(500) NULL COMMENT '买家收货地址',
    MODIFY COLUMN cancel_reason VARCHAR(500) NULL COMMENT '取消原因',
    MODIFY COLUMN refund_reason VARCHAR(500) NULL COMMENT '退款原因';
