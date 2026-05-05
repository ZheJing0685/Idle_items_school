-- 添加核心字段（高优先级）

-- 为所有表添加逻辑删除字段和审计字段

-- users表
ALTER TABLE users ADD COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN create_by BIGINT;
ALTER TABLE users ADD COLUMN update_by BIGINT;
ALTER TABLE users ADD COLUMN last_login_time DATETIME;
ALTER TABLE users ADD COLUMN last_login_ip VARCHAR(50);
ALTER TABLE users ADD COLUMN login_count INT NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN credit_score INT NOT NULL DEFAULT 100;
ALTER TABLE users ADD COLUMN total_transactions INT NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN total_sales INT NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN total_purchases INT NOT NULL DEFAULT 0;

-- 添加users表索引
CREATE INDEX IDX_users_role_status_verified ON users (role, status, verified);
CREATE INDEX IDX_users_last_login_time ON users (last_login_time);
CREATE INDEX IDX_users_credit_score ON users (credit_score);

-- items表
ALTER TABLE items ADD COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE items ADD COLUMN create_by BIGINT;
ALTER TABLE items ADD COLUMN update_by BIGINT;

-- 添加items表索引
CREATE INDEX IDX_items_status_price ON items (status, price);

-- orders表
ALTER TABLE orders ADD COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE orders ADD COLUMN create_by BIGINT;
ALTER TABLE orders ADD COLUMN update_by BIGINT;
ALTER TABLE orders ADD COLUMN total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00;
ALTER TABLE orders ADD COLUMN shipping_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00;
ALTER TABLE orders ADD COLUMN discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00;
ALTER TABLE orders ADD COLUMN pay_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00;
ALTER TABLE orders ADD COLUMN transaction_id VARCHAR(100);
ALTER TABLE orders ADD COLUMN payment_status TINYINT NOT NULL DEFAULT 0;
ALTER TABLE orders ADD COLUMN shipping_status TINYINT NOT NULL DEFAULT 0;
ALTER TABLE orders ADD COLUMN tracking_no VARCHAR(50);
ALTER TABLE orders ADD COLUMN shipping_company VARCHAR(50);

-- 添加orders表索引
CREATE INDEX IDX_orders_buyer_id_order_status ON orders (buyer_id, order_status);
CREATE INDEX IDX_orders_seller_id_order_status ON orders (seller_id, order_status);
CREATE INDEX IDX_orders_payment_time ON orders (payment_time);
CREATE INDEX IDX_orders_ship_time ON orders (ship_time);
CREATE INDEX IDX_orders_deliver_time ON orders (deliver_time);

-- categories表
ALTER TABLE categories ADD COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE categories ADD COLUMN create_by BIGINT;
ALTER TABLE categories ADD COLUMN update_by BIGINT;

-- verification_records表
ALTER TABLE verification_records ADD COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE verification_records ADD COLUMN create_by BIGINT;
ALTER TABLE verification_records ADD COLUMN update_by BIGINT;

-- reviews表
ALTER TABLE reviews ADD COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE reviews ADD COLUMN create_by BIGINT;
ALTER TABLE reviews ADD COLUMN update_by BIGINT;

-- favorites表
ALTER TABLE favorites ADD COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0;

-- chats表
ALTER TABLE chats ADD COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE chats ADD COLUMN create_by BIGINT;
ALTER TABLE chats ADD COLUMN update_by BIGINT;

-- chat_messages表
ALTER TABLE chat_messages ADD COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE chat_messages ADD COLUMN create_by BIGINT;
ALTER TABLE chat_messages ADD COLUMN update_by BIGINT;

-- disputes表
ALTER TABLE disputes ADD COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE disputes ADD COLUMN create_by BIGINT;
ALTER TABLE disputes ADD COLUMN update_by BIGINT;
ALTER TABLE disputes ADD COLUMN dispute_no VARCHAR(50) NOT NULL;

-- 添加disputes表索引
CREATE UNIQUE INDEX UK_disputes_dispute_no ON disputes (dispute_no);

-- item_images表
ALTER TABLE item_images ADD COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE item_images ADD COLUMN create_by BIGINT;
ALTER TABLE item_images ADD COLUMN update_by BIGINT;
ALTER TABLE item_images ADD COLUMN updated_at DATETIME;

-- item_tags表
ALTER TABLE item_tags ADD COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE item_tags ADD COLUMN create_by BIGINT;

-- admin_logs表
ALTER TABLE admin_logs ADD COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0;

-- image_analysis表
ALTER TABLE image_analysis ADD COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE image_analysis ADD COLUMN create_by BIGINT;
ALTER TABLE image_analysis ADD COLUMN update_by BIGINT;
ALTER TABLE image_analysis ADD COLUMN updated_at DATETIME;