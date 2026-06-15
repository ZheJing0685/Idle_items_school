-- =========================================
-- 为 categories 表添加减碳量字段
-- =========================================

ALTER TABLE categories
    ADD COLUMN carbon_saving_kg DECIMAL(10,2) DEFAULT 0.00 COMMENT '每件物品减碳量(kg)';
