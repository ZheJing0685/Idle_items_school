-- =============================================================================
-- 添加复合索引以优化查询性能
-- 解决 N+1 查询、文件排序和全表扫描问题
-- =============================================================================

-- 保存当前 sql_mode
SET @err_mode = @@sql_mode;
SET sql_mode = '';

-- ===== 1. chat_messages 表 - 优化消息列表查询 =====
-- 主要查询: findByChatIdOrderByCreatedAtAsc
-- 现有索引: idx_chat_messages_chat_id (单列)
-- 问题: 缺少 (chat_id, created_at) 复合索引导致文件排序 (filesort)
-- 说明: 此索引覆盖聊天消息的查询+排序，消除 filesort
CREATE INDEX idx_chatmessages_chat_created
    ON chat_messages(chat_id, created_at);

-- ===== 2. chats 表 - 优化用户会话列表查询 =====
-- 主要查询: findByBuyerIdOrSellerId
-- 问题: OR 条件无法高效使用单列索引
-- 说明: 为 buyer_id 和 seller_id 添加复合索引 + updatedAt 排序
CREATE INDEX idx_chats_buyer_updated
    ON chats(buyer_id, updated_at DESC);
CREATE INDEX idx_chats_seller_updated
    ON chats(seller_id, updated_at DESC);

-- ===== 3. users 表 - 补齐筛选字段索引 =====
-- 主要查询: 按认证状态筛选用户
-- 说明: V9 已添加 status/role 索引，补充 verified 索引
CREATE INDEX idx_users_verified
    ON users(verified);

-- ===== 4. item_images 表 - 优化图片获取查询 =====
-- 主要查询: 按物品 ID 查询图片并按排序字段排列
-- 说明: 避免按 item_id 查询后再排序
CREATE INDEX idx_itemimages_item_sort
    ON item_images(item_id, sort_order);

-- ===== 5. item_tags 表 - 优化标签查询 =====
-- 主要查询: 按物品查询标签
CREATE INDEX idx_itemtags_item_tag
    ON item_tags(item_id, tag_name);

-- ===== 6. notifications 表 - 优化通知列表查询 =====
-- 主要查询: 按用户查询未读通知，按时间排序
-- 说明: 覆盖通知查询中最常见的过滤条件
CREATE INDEX idx_notifications_user_read_created
    ON notifications(user_id, is_read, created_at DESC);

-- ===== 7. favorites 表 - 优化用户收藏列表查询 =====
-- 主要查询: 按用户查询收藏列表
-- 说明: 现有 UK 索引 (user_id, item_id)，补充按时间排序的索引
CREATE INDEX idx_favorites_user_created
    ON favorites(user_id, created_at DESC);

-- ===== 8. reviews 表 - 优化评价查询 =====
-- 主要查询: 按物品/卖家查询评价
-- 说明: 补充复合索引提升评价列表性能
CREATE INDEX idx_reviews_item_created
    ON reviews(item_id, created_at DESC);
CREATE INDEX idx_reviews_reviewed_user_created
    ON reviews(reviewed_user_id, created_at DESC);

-- ===== 9. image_analysis 表 - 优化图片分析记录查询 =====
CREATE INDEX idx_imageanalysis_item_created
    ON image_analysis(item_id, created_at DESC);

-- ===== 10. verification_records 表 - 优化认证记录查询 =====
CREATE INDEX idx_verification_user_created
    ON verification_records(user_id, created_at DESC);

-- 恢复 sql_mode
SET sql_mode = @err_mode;