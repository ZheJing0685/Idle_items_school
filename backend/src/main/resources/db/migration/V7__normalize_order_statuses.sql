ALTER TABLE orders
    MODIFY COLUMN order_status VARCHAR(30) NOT NULL DEFAULT 'PENDING_PAYMENT';

UPDATE orders
SET order_status = CASE order_status
    WHEN '0' THEN 'PENDING_PAYMENT'
    WHEN '1' THEN 'PENDING_SHIPMENT'
    WHEN '2' THEN 'SHIPPED'
    WHEN '3' THEN 'COMPLETED'
    WHEN '4' THEN 'CANCELLED'
    WHEN '5' THEN 'REFUND_REQUESTED'
    WHEN '6' THEN 'REFUNDED'
    WHEN 'PAID' THEN 'PENDING_SHIPMENT'
    WHEN 'DELIVERED' THEN 'COMPLETED'
    ELSE order_status
END;

UPDATE orders
SET complete_time = COALESCE(complete_time, deliver_time, updated_at, created_at)
WHERE order_status = 'COMPLETED'
  AND complete_time IS NULL;
