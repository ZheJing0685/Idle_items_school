-- =========================================
-- 添加缺失的复合索引优化查询性能
-- =========================================

-- 1. chat_messages表 - 添加复合索引优化聊天消息查询
CREATE INDEX idx_chat_messages_chat_created ON chat_messages(chat_id, created_at DESC);
CREATE INDEX idx_chat_messages_receiver_read ON chat_messages(chat_id, receiver_id, is_read);

-- 2. reviews表 - 添加复合索引优化评价查询
CREATE INDEX idx_reviews_order_reviewer ON reviews(order_id, reviewer_id);
CREATE INDEX idx_reviews_user_rating ON reviews(reviewed_user_id, rating);

-- 3. favorites表 - 添加复合索引优化收藏查询
CREATE INDEX idx_favorites_user_item ON favorites(user_id, item_id);

-- 4. notifications表 - 添加复合索引优化通知查询
CREATE INDEX idx_notifications_user_read ON notifications(user_id, is_read, is_deleted);

-- 5. disputes表 - 添加复合索引优化纠纷查询
CREATE INDEX idx_disputes_order_status ON disputes(order_id, dispute_status);
CREATE INDEX idx_disputes_status_urgent ON disputes(dispute_status, is_urgent);
