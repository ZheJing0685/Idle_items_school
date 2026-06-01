-- =========================================
-- 清理无效/冗余索引
-- =========================================

-- 1. items表 - 移除很少使用的索引
DROP INDEX idx_items_view_count ON items;
DROP INDEX idx_items_status_price ON items;

-- 2. orders表 - 移除很少使用的索引
DROP INDEX idx_orders_item_id ON orders;

-- 3. items表 - 添加全文索引优化搜索
ALTER TABLE items ADD FULLTEXT INDEX idx_items_fulltext(title, description);
