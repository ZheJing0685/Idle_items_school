-- 添加cancel_time、refund_result、refund_admin_id列到orders表
ALTER TABLE orders ADD COLUMN cancel_time DATETIME AFTER cancel_reason;
ALTER TABLE orders ADD COLUMN refund_result VARCHAR(50) AFTER refund_amount;
ALTER TABLE orders ADD COLUMN refund_admin_id BIGINT AFTER refund_result;
