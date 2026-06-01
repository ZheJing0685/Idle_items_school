-- =========================================
-- 标记users表中未使用的字段为deprecated
-- 这些字段在代码中未被写入，保留但标记为废弃
-- =========================================

-- 标记未使用的字段为deprecated
ALTER TABLE users
    MODIFY COLUMN create_by BIGINT NULL COMMENT '[DEPRECATED] 已废弃，不再使用',
    MODIFY COLUMN update_by BIGINT NULL COMMENT '[DEPRECATED] 已废弃，不再使用',
    MODIFY COLUMN last_login_ip VARCHAR(50) NULL COMMENT '[DEPRECATED] 已废弃，不再使用',
    MODIFY COLUMN total_sales INT DEFAULT 0 COMMENT '[DEPRECATED] 已废弃，不再使用',
    MODIFY COLUMN total_purchases INT DEFAULT 0 COMMENT '[DEPRECATED] 已废弃，不再使用';

-- 标记items表中未使用的字段为deprecated
ALTER TABLE items
    MODIFY COLUMN purchase_date DATETIME NULL COMMENT '[DEPRECATED] 已废弃，不再使用';

-- 标记chat_messages表中未使用的字段为deprecated
ALTER TABLE chat_messages
    MODIFY COLUMN is_anonymous BOOLEAN DEFAULT FALSE COMMENT '[DEPRECATED] 已废弃，不再使用';
