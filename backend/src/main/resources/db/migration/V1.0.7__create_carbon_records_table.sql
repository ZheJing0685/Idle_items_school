-- =========================================
-- 创建碳减排记录表
-- =========================================

CREATE TABLE carbon_records (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    order_id        BIGINT          NOT NULL COMMENT '订单ID',
    item_id         BIGINT          NOT NULL COMMENT '物品ID',
    buyer_id        BIGINT          NOT NULL COMMENT '买家ID',
    seller_id       BIGINT          NOT NULL COMMENT '卖家ID',
    category_id     BIGINT          NULL     COMMENT '分类ID',
    carbon_saving_kg DECIMAL(10,2)  NOT NULL DEFAULT 0.00 COMMENT '减碳量(kg)',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX idx_carbon_records_order_id (order_id),
    INDEX idx_carbon_records_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='碳减排记录表';
