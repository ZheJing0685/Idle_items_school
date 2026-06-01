-- =========================================
-- 为items表添加乐观锁版本号字段
-- =========================================

-- 添加version字段用于乐观锁
ALTER TABLE items
    ADD COLUMN version BIGINT DEFAULT 0 COMMENT '乐观锁版本号' AFTER is_deleted;
