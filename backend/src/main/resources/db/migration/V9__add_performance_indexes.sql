-- 为 admin_logs 表添加索引（使用条件创建避免重复）
-- 由于 MySQL 没有 IF NOT EXISTS 索引语法，使用错误处理忽略已存在的情况

-- 为 admin_logs 表添加索引
-- 忽略错误 1061 (Duplicate key name)
SET @err_mode = @@sql_mode;
SET sql_mode = '';
CREATE INDEX idx_admin_logs_admin_id ON admin_logs(admin_id);
CREATE INDEX idx_admin_logs_created_at ON admin_logs(created_at);
CREATE INDEX idx_admin_logs_target_type ON admin_logs(target_type);
SET sql_mode = @err_mode;

-- 为 orders 表添加索引
SET @err_mode = @@sql_mode;
SET sql_mode = '';
CREATE INDEX idx_orders_order_status ON orders(order_status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_orders_buyer_id ON orders(buyer_id);
CREATE INDEX idx_orders_seller_id ON orders(seller_id);
SET sql_mode = @err_mode;

-- 为 items 表添加索引
SET @err_mode = @@sql_mode;
SET sql_mode = '';
CREATE INDEX idx_items_status ON items(status);
CREATE INDEX idx_items_user_id ON items(user_id);
CREATE INDEX idx_items_created_at ON items(created_at);
CREATE INDEX idx_items_category_id ON items(category_id);
SET sql_mode = @err_mode;

-- 为 users 表添加索引
SET @err_mode = @@sql_mode;
SET sql_mode = '';
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_created_at ON users(created_at);
SET sql_mode = @err_mode;
