-- V19: 扩展系统配置表，创建数据字典表

-- 1. 扩展system_configs表（检查列是否存在后再添加）
SET @dbname = DATABASE();

-- 检查并添加 config_type 列
SELECT COUNT(*) INTO @exist FROM information_schema.columns WHERE table_schema = @dbname AND table_name = 'system_configs' AND column_name = 'config_type';
SET @sql = IF(@exist = 0, 'ALTER TABLE system_configs ADD COLUMN config_type INT NOT NULL DEFAULT 1 COMMENT ''配置类型：1=文本，2=数字，3=布尔，4=JSON'' AFTER config_value', 'SELECT ''config_type column already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 is_editable 列
SELECT COUNT(*) INTO @exist FROM information_schema.columns WHERE table_schema = @dbname AND table_name = 'system_configs' AND column_name = 'is_editable';
SET @sql = IF(@exist = 0, 'ALTER TABLE system_configs ADD COLUMN is_editable TINYINT(1) NOT NULL DEFAULT 1 COMMENT ''是否可编辑：0=不可编辑，1=可编辑'' AFTER description', 'SELECT ''is_editable column already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 group_name 列
SELECT COUNT(*) INTO @exist FROM information_schema.columns WHERE table_schema = @dbname AND table_name = 'system_configs' AND column_name = 'group_name';
SET @sql = IF(@exist = 0, 'ALTER TABLE system_configs ADD COLUMN group_name VARCHAR(50) DEFAULT ''general'' COMMENT ''配置分组'' AFTER is_editable', 'SELECT ''group_name column already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 sort_order 列
SELECT COUNT(*) INTO @exist FROM information_schema.columns WHERE table_schema = @dbname AND table_name = 'system_configs' AND column_name = 'sort_order';
SET @sql = IF(@exist = 0, 'ALTER TABLE system_configs ADD COLUMN sort_order INT DEFAULT 0 COMMENT ''排序'' AFTER group_name', 'SELECT ''sort_order column already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 创建数据字典类型表
CREATE TABLE IF NOT EXISTS dict_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type_code VARCHAR(50) NOT NULL UNIQUE COMMENT '字典类型编码',
    type_name VARCHAR(100) NOT NULL COMMENT '字典类型名称',
    description VARCHAR(500) COMMENT '描述',
    is_system TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否系统内置：0=否，1=是',
    status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_type_code (type_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据字典类型表';

-- 3. 创建数据字典项表
CREATE TABLE IF NOT EXISTS dict_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type_code VARCHAR(50) NOT NULL COMMENT '字典类型编码',
    item_value VARCHAR(100) NOT NULL COMMENT '字典项值',
    item_label VARCHAR(200) NOT NULL COMMENT '字典项标签',
    item_label_en VARCHAR(200) COMMENT '字典项英文标签',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
    css_class VARCHAR(100) COMMENT 'CSS类名',
    extra_data VARCHAR(500) COMMENT '扩展数据（JSON格式）',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_type_value (type_code, item_value),
    INDEX idx_type_code (type_code),
    INDEX idx_status (status),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据字典项表';

-- 4. 插入字典类型数据
INSERT IGNORE INTO dict_types (type_code, type_name, description, is_system) VALUES
('ITEM_CONDITION', '物品成色', '物品的新旧程度', 1),
('DELIVERY_METHOD', '配送方式', '物品的配送方式', 1),
('ITEM_STATUS', '物品状态', '物品的当前状态', 1),
('ORDER_STATUS', '订单状态', '订单的当前状态', 1),
('CONTACT_TYPE', '联系方式类型', '用户联系方式类型', 1),
('PAYMENT_METHOD', '支付方式', '订单支付方式', 1),
('PAYMENT_STATUS', '支付状态', '订单支付状态', 1),
('SHIPPING_STATUS', '配送状态', '订单配送状态', 1),
('USER_ROLE', '用户角色', '系统用户角色', 1),
('VERIFICATION_STATUS', '认证状态', '实名认证状态', 1),
('VERIFICATION_TYPE', '认证类型', '实名认证类型', 1),
('CATEGORY_FEEDBACK_TYPE', '分类反馈类型', '分类反馈的类型', 1);

-- 5. 插入物品成色字典数据
INSERT IGNORE INTO dict_items (type_code, item_value, item_label, sort_order, css_class) VALUES
('ITEM_CONDITION', 'NEW', '全新', 1, 'condition-new'),
('ITEM_CONDITION', 'LIKE_NEW', '九成新', 2, 'condition-like-new'),
('ITEM_CONDITION', 'GOOD', '八成新', 3, 'condition-good'),
('ITEM_CONDITION', 'FAIR', '七成新', 4, 'condition-fair'),
('ITEM_CONDITION', 'POOR', '六成新及以下', 5, 'condition-poor');

-- 6. 插入配送方式字典数据
INSERT IGNORE INTO dict_items (type_code, item_value, item_label, sort_order, css_class) VALUES
('DELIVERY_METHOD', 'PICKUP', '自提', 1, 'delivery-pickup'),
('DELIVERY_METHOD', 'LOCAL_DELIVERY', '校内配送', 2, 'delivery-local'),
('DELIVERY_METHOD', 'EXPRESS', '快递配送', 3, 'delivery-express');

-- 7. 插入物品状态字典数据
INSERT IGNORE INTO dict_items (type_code, item_value, item_label, sort_order, css_class) VALUES
('ITEM_STATUS', 'DRAFT', '草稿', 1, 'status-draft'),
('ITEM_STATUS', 'PENDING', '待审核', 2, 'status-pending'),
('ITEM_STATUS', 'ON_SALE', '在售', 3, 'status-on-sale'),
('ITEM_STATUS', 'SOLD', '已售', 4, 'status-sold'),
('ITEM_STATUS', 'OFF_SHELF', '已下架', 5, 'status-off-shelf'),
('ITEM_STATUS', 'REJECTED', '已驳回', 6, 'status-rejected');

-- 8. 插入订单状态字典数据
INSERT IGNORE INTO dict_items (type_code, item_value, item_label, sort_order, css_class) VALUES
('ORDER_STATUS', 'PENDING_PAYMENT', '待支付', 1, 'status-pending-payment'),
('ORDER_STATUS', 'PENDING_SHIPMENT', '待发货', 2, 'status-pending-shipment'),
('ORDER_STATUS', 'SHIPPED', '已发货', 3, 'status-shipped'),
('ORDER_STATUS', 'COMPLETED', '已完成', 4, 'status-completed'),
('ORDER_STATUS', 'CANCELLED', '已取消', 5, 'status-cancelled'),
('ORDER_STATUS', 'REFUND_REQUESTED', '退款申请中', 6, 'status-refund-requested'),
('ORDER_STATUS', 'REFUNDED', '已退款', 7, 'status-refunded');

-- 9. 插入联系方式类型字典数据
INSERT IGNORE INTO dict_items (type_code, item_value, item_label, sort_order, css_class) VALUES
('CONTACT_TYPE', 'PLATFORM', '平台内', 1, 'contact-platform'),
('CONTACT_TYPE', 'WECHAT', '微信', 2, 'contact-wechat'),
('CONTACT_TYPE', 'QQ', 'QQ', 3, 'contact-qq');

-- 10. 插入支付方式字典数据
INSERT IGNORE INTO dict_items (type_code, item_value, item_label, sort_order, css_class) VALUES
('PAYMENT_METHOD', 'OFFLINE', '线下支付', 1, 'payment-offline'),
('PAYMENT_METHOD', 'WECHAT', '微信支付', 2, 'payment-wechat'),
('PAYMENT_METHOD', 'ALIPAY', '支付宝', 3, 'payment-alipay');

-- 11. 插入支付状态字典数据
INSERT IGNORE INTO dict_items (type_code, item_value, item_label, sort_order, css_class) VALUES
('PAYMENT_STATUS', 'UNPAID', '未支付', 1, 'status-unpaid'),
('PAYMENT_STATUS', 'PAID', '已支付', 2, 'status-paid'),
('PAYMENT_STATUS', 'REFUNDED', '已退款', 3, 'status-refunded');

-- 12. 插入配送状态字典数据
INSERT IGNORE INTO dict_items (type_code, item_value, item_label, sort_order, css_class) VALUES
('SHIPPING_STATUS', 'PENDING', '待发货', 1, 'status-pending'),
('SHIPPING_STATUS', 'SHIPPED', '已发货', 2, 'status-shipped'),
('SHIPPING_STATUS', 'DELIVERED', '已送达', 3, 'status-delivered'),
('SHIPPING_STATUS', 'ACCEPTED', '已收货', 4, 'status-accepted');

-- 13. 插入用户角色字典数据
INSERT IGNORE INTO dict_items (type_code, item_value, item_label, sort_order, css_class) VALUES
('USER_ROLE', 'USER', '普通用户', 1, 'role-user'),
('USER_ROLE', 'ADMIN', '管理员', 2, 'role-admin');

-- 14. 插入认证状态字典数据
INSERT IGNORE INTO dict_items (type_code, item_value, item_label, sort_order, css_class) VALUES
('VERIFICATION_STATUS', 'PENDING', '待审核', 1, 'status-pending'),
('VERIFICATION_STATUS', 'APPROVED', '已通过', 2, 'status-approved'),
('VERIFICATION_STATUS', 'REJECTED', '已拒绝', 3, 'status-rejected');

-- 15. 插入认证类型字典数据
INSERT IGNORE INTO dict_items (type_code, item_value, item_label, sort_order, css_class) VALUES
('VERIFICATION_TYPE', 'ID_CARD', '身份证认证', 1, 'type-id-card'),
('VERIFICATION_TYPE', 'STUDENT_CARD', '学生证认证', 2, 'type-student-card'),
('VERIFICATION_TYPE', 'TEACHER_CARD', '教师证认证', 3, 'type-teacher-card');

-- 16. 插入分类反馈类型字典数据
INSERT IGNORE INTO dict_items (type_code, item_value, item_label, sort_order, css_class) VALUES
('CATEGORY_FEEDBACK_TYPE', 'INVALID', '无效分类', 1, 'type-invalid'),
('CATEGORY_FEEDBACK_TYPE', 'MISSING', '缺少分类', 2, 'type-missing'),
('CATEGORY_FEEDBACK_TYPE', 'OTHER', '其他', 3, 'type-other');

-- 17. 添加系统配置项（使用 INSERT IGNORE 避免重复）
INSERT IGNORE INTO system_configs (config_key, config_value, config_type, description, is_editable, group_name, sort_order) VALUES
('file_max_size', '5242880', 2, '最大文件大小（字节）', 1, 'file', 1),
('file_allowed_types', 'jpg,png,webp', 1, '允许的文件类型', 1, 'file', 2),
('file_max_width', '1920', 2, '图片最大宽度', 1, 'file', 3),
('file_default_quality', '0.8', 1, '图片默认质量', 1, 'file', 4),
('file_watermark_opacity', '0.3', 1, '水印透明度', 1, 'file', 5),
('file_watermark_text', 'Idle Items School', 1, '水印文字', 1, 'file', 6),
('order_timeout_minutes', '30', 2, '订单超时时间（分钟）', 1, 'order', 1),
('order_timeout_check_interval', '300000', 2, '订单超时检查间隔（毫秒）', 1, 'order', 2),
('cache_dict_ttl', '86400', 2, '字典缓存过期时间（秒）', 1, 'cache', 1),
('cache_config_ttl', '3600', 2, '配置缓存过期时间（秒）', 1, 'cache', 2),
('site_registration_bonus', '100', 2, '注册赠送积分', 1, 'promotion', 1),
('site_first_order_discount', '10', 2, '首单立减金额', 1, 'promotion', 2),
('pagination_default_size', '20', 2, '默认分页大小', 1, 'general', 1),
('jwt_expiration', '3600000', 2, 'JWT过期时间（毫秒）', 1, 'security', 1);