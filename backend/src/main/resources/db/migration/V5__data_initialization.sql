-- 数据初始化

-- 为现有记录设置默认值

-- users表
UPDATE users SET is_deleted = 0 WHERE is_deleted IS NULL;
UPDATE users SET create_by = id WHERE create_by IS NULL;
UPDATE users SET update_by = id WHERE update_by IS NULL;
UPDATE users SET login_count = 0 WHERE login_count IS NULL;
UPDATE users SET credit_score = 100 WHERE credit_score IS NULL;
UPDATE users SET total_transactions = 0 WHERE total_transactions IS NULL;
UPDATE users SET total_sales = 0 WHERE total_sales IS NULL;
UPDATE users SET total_purchases = 0 WHERE total_purchases IS NULL;

-- items表
UPDATE items SET is_deleted = 0 WHERE is_deleted IS NULL;
UPDATE items SET create_by = user_id WHERE create_by IS NULL;
UPDATE items SET update_by = user_id WHERE update_by IS NULL;
UPDATE items SET is_bargain_allowed = 1 WHERE is_bargain_allowed IS NULL;
UPDATE items SET contact_type = 1 WHERE contact_type IS NULL;
UPDATE items SET is_recommended = 0 WHERE is_recommended IS NULL;
UPDATE items SET weight = 0 WHERE weight IS NULL;
UPDATE items SET delivery_method = 1 WHERE delivery_method IS NULL;

-- orders表
UPDATE orders SET is_deleted = 0 WHERE is_deleted IS NULL;
UPDATE orders SET create_by = buyer_id WHERE create_by IS NULL;
UPDATE orders SET update_by = buyer_id WHERE update_by IS NULL;
UPDATE orders SET total_amount = price WHERE total_amount IS NULL;
UPDATE orders SET shipping_fee = 0.00 WHERE shipping_fee IS NULL;
UPDATE orders SET discount_amount = 0.00 WHERE discount_amount IS NULL;
UPDATE orders SET pay_amount = total_amount WHERE pay_amount IS NULL;
UPDATE orders SET payment_status = 0 WHERE payment_status IS NULL;
UPDATE orders SET shipping_status = 0 WHERE shipping_status IS NULL;
UPDATE orders SET source = 1 WHERE source IS NULL;

-- categories表
UPDATE categories SET is_deleted = 0 WHERE is_deleted IS NULL;
UPDATE categories SET create_by = 1 WHERE create_by IS NULL;
UPDATE categories SET update_by = 1 WHERE update_by IS NULL;
UPDATE categories SET level = 1 WHERE level IS NULL;
UPDATE categories SET is_show = 1 WHERE is_show IS NULL;
UPDATE categories SET item_count = 0 WHERE item_count IS NULL;

-- verification_records表
UPDATE verification_records SET is_deleted = 0 WHERE is_deleted IS NULL;
UPDATE verification_records SET create_by = user_id WHERE create_by IS NULL;
UPDATE verification_records SET update_by = user_id WHERE update_by IS NULL;
UPDATE verification_records SET submit_count = 1 WHERE submit_count IS NULL;
UPDATE verification_records SET auto_approved = 0 WHERE auto_approved IS NULL;
UPDATE verification_records SET risk_level = 0 WHERE risk_level IS NULL;

-- reviews表
UPDATE reviews SET is_deleted = 0 WHERE is_deleted IS NULL;
UPDATE reviews SET create_by = reviewer_id WHERE create_by IS NULL;
UPDATE reviews SET update_by = reviewer_id WHERE update_by IS NULL;
UPDATE reviews SET is_show = 1 WHERE is_show IS NULL;
UPDATE reviews SET helpful_count = 0 WHERE helpful_count IS NULL;
UPDATE reviews SET report_count = 0 WHERE report_count IS NULL;
UPDATE reviews SET is_reported = 0 WHERE is_reported IS NULL;
UPDATE reviews SET check_status = 0 WHERE check_status IS NULL;

-- favorites表
UPDATE favorites SET is_deleted = 0 WHERE is_deleted IS NULL;
UPDATE favorites SET notify_when_price_drop = 0 WHERE notify_when_price_drop IS NULL;

-- chats表
UPDATE chats SET is_deleted = 0 WHERE is_deleted IS NULL;
UPDATE chats SET create_by = buyer_id WHERE create_by IS NULL;
UPDATE chats SET update_by = buyer_id WHERE update_by IS NULL;
UPDATE chats SET buyer_unread_count = 0 WHERE buyer_unread_count IS NULL;
UPDATE chats SET seller_unread_count = 0 WHERE seller_unread_count IS NULL;
UPDATE chats SET is_blocked = 0 WHERE is_blocked IS NULL;
UPDATE chats SET is_muted = 0 WHERE is_muted IS NULL;
UPDATE chats SET chat_status = 1 WHERE chat_status IS NULL;

-- chat_messages表
UPDATE chat_messages SET is_deleted = 0 WHERE is_deleted IS NULL;
UPDATE chat_messages SET create_by = sender_id WHERE create_by IS NULL;
UPDATE chat_messages SET update_by = sender_id WHERE update_by IS NULL;
UPDATE chat_messages SET message_status = 1 WHERE message_status IS NULL;
UPDATE chat_messages SET is_recalled = 0 WHERE is_recalled IS NULL;
UPDATE chat_messages SET is_deleted_by_sender = 0 WHERE is_deleted_by_sender IS NULL;
UPDATE chat_messages SET is_deleted_by_receiver = 0 WHERE is_deleted_by_receiver IS NULL;

-- disputes表
UPDATE disputes SET is_deleted = 0 WHERE is_deleted IS NULL;
UPDATE disputes SET create_by = applicant_id WHERE create_by IS NULL;
UPDATE disputes SET update_by = applicant_id WHERE update_by IS NULL;
UPDATE disputes SET dispute_no = CONCAT('DIS', id) WHERE dispute_no IS NULL;
UPDATE disputes SET dispute_type = 1 WHERE dispute_type IS NULL;
UPDATE disputes SET is_urgent = 0 WHERE is_urgent IS NULL;
UPDATE disputes SET priority = 1 WHERE priority IS NULL;
UPDATE disputes SET is_escalated = 0 WHERE is_escalated IS NULL;

-- item_images表
UPDATE item_images SET is_deleted = 0 WHERE is_deleted IS NULL;
UPDATE item_images SET create_by = (SELECT user_id FROM items WHERE items.id = item_images.item_id) WHERE create_by IS NULL;
UPDATE item_images SET update_by = create_by WHERE update_by IS NULL;
UPDATE item_images SET updated_at = created_at WHERE updated_at IS NULL;
UPDATE item_images SET storage_type = 1 WHERE storage_type IS NULL;
UPDATE item_images SET is_compressed = 0 WHERE is_compressed IS NULL;
UPDATE item_images SET is_watermarked = 0 WHERE is_watermarked IS NULL;
UPDATE item_images SET ai_analysis_status = 0 WHERE ai_analysis_status IS NULL;

-- item_tags表
UPDATE item_tags SET is_deleted = 0 WHERE is_deleted IS NULL;
UPDATE item_tags SET create_by = (SELECT user_id FROM items WHERE items.id = item_tags.item_id) WHERE create_by IS NULL;
UPDATE item_tags SET tag_type = 1 WHERE tag_type IS NULL;
UPDATE item_tags SET weight = 1 WHERE weight IS NULL;

-- admin_logs表
UPDATE admin_logs SET is_deleted = 0 WHERE is_deleted IS NULL;
UPDATE admin_logs SET log_type = 1 WHERE log_type IS NULL;
UPDATE admin_logs SET log_level = 1 WHERE log_level IS NULL;
UPDATE admin_logs SET status = 1 WHERE status IS NULL;

-- image_analysis表
UPDATE image_analysis SET is_deleted = 0 WHERE is_deleted IS NULL;
UPDATE image_analysis SET create_by = 1 WHERE create_by IS NULL;
UPDATE image_analysis SET update_by = 1 WHERE update_by IS NULL;
UPDATE image_analysis SET updated_at = created_at WHERE updated_at IS NULL;
UPDATE image_analysis SET analysis_type = 1 WHERE analysis_type IS NULL;
UPDATE image_analysis SET is_manual_reviewed = 0 WHERE is_manual_reviewed IS NULL;
UPDATE image_analysis SET is_used_for_training = 0 WHERE is_used_for_training IS NULL;

-- 初始化系统配置数据
INSERT IGNORE INTO system_configs (config_key, config_value, config_type, description, is_editable) VALUES
('site_name', '闲置物品校园交易平台', 1, '网站名称', 1),
('site_description', '安全高效的校园闲置交易平台，促进资源循环', 1, '网站描述', 1),
('site_keywords', '校园,闲置,交易,二手', 1, '网站关键词', 1),
('max_file_size', '5242880', 2, '最大文件大小（字节）', 1),
('allowed_file_types', 'jpg,png,webp', 1, '允许的文件类型', 1),
('page_size', '10', 2, '分页大小', 1),
('jwt_expiration', '3600000', 2, 'JWT过期时间（毫秒）', 1),
('default_avatar', '/uploads/avatars/default.png', 1, '默认头像', 1),
('max_images_per_item', '10', 2, '每个商品最大图片数', 1),
('auto_verify_enabled', 'false', 1, '是否启用自动验证', 1);

-- 初始化分类数据
INSERT IGNORE INTO categories (name, description, parent_id, sort_order, icon, level, is_show) VALUES
('数码产品', '手机、电脑、平板等数码产品', NULL, 1, '📱', 1, 1),
('服装鞋包', '衣服、鞋子、包包等', NULL, 2, '👔', 1, 1),
('图书文具', '书籍、文具、教材等', NULL, 3, '📚', 1, 1),
('运动户外', '运动器材、户外装备等', NULL, 4, '⚽', 1, 1),
('生活用品', '日常用品、家居用品等', NULL, 5, '🏠', 1, 1),
('其他', '其他闲置物品', NULL, 6, '📦', 1, 1),
('手机', '智能手机', 1, 1, '📱', 2, 1),
('电脑', '笔记本、台式机等', 1, 2, '💻', 2, 1),
('平板', '平板电脑', 1, 3, '�平板', 2, 1),
('配件', '数码配件', 1, 4, '🔌', 2, 1),
('上衣', '各种上衣', 2, 1, '👕', 2, 1),
('裤子', '各种裤子', 2, 2, '👖', 2, 1),
('鞋子', '各种鞋子', 2, 3, '👟', 2, 1),
('包包', '各种包包', 2, 4, '👜', 2, 1),
('教材', '教材书籍', 3, 1, '📖', 2, 1),
('课外书', '课外书籍', 3, 2, '📚', 2, 1),
('文具', '文具用品', 3, 3, '✏️', 2, 1),
('体育器材', '各种体育器材', 4, 1, '🏀', 2, 1),
('户外装备', '户外装备', 4, 2, '⛺', 2, 1),
('运动服饰', '运动服饰', 4, 3, '运动服', 2, 1),
('家居用品', '家居用品', 5, 1, '🏠', 2, 1),
('日常用品', '日常用品', 5, 2, '🧺', 2, 1),
('电子产品', '其他电子产品', 6, 1, '📱', 2, 1),
('其他物品', '其他闲置物品', 6, 2, '📦', 2, 1);

-- ==========================================================================
-- 初始化管理员用户（如果不存在）
-- ==========================================================================
-- !! 安全警告 !!
-- 以下密码哈希对应明文密码 'admin123'，仅用于开发/测试环境初始化。
-- 部署到生产环境前，必须：
--   1. 使用 BCrypt 生成新的密码哈希替换下方哈希值
--   2. 或在首次登录后立即修改管理员密码
--   3. 确保通过环境变量或密钥管理服务注入凭据
-- ==========================================================================
INSERT IGNORE INTO users (username, password, email, phone, nickname, avatar, role, status, verified) VALUES
('admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'admin@example.com', '13800138000', '管理员', '/uploads/avatars/admin.png', 3, 1, 1);

-- 初始化分类路径（简化版）
UPDATE categories
SET path = CAST(id AS CHAR);

-- 初始化商品分类计数
UPDATE categories c
SET item_count = 0;