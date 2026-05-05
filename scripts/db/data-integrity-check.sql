-- ============================================================
-- Data Integrity Check Script
-- Usage: mysql -u idle_items_monitor -p idle_items_school < data-integrity-check.sql
-- ============================================================

SET @db_name = 'idle_items_school';
SET @check_time = NOW();

SELECT '========================================' AS '=== Data Integrity Check ===';
SELECT @check_time AS 'Check Time';
SELECT '' AS '';

-- ============================================================
-- Section 1: Orphaned Records
-- ============================================================
SELECT '=== Section 1: Orphaned Records ===' AS '';

-- 1a: Items referencing non-existent users
SELECT '1a: Items with non-existent seller' AS 'Check';
SELECT CONCAT('FOUND ', COUNT(*), ' orphaned item(s)') AS 'Result'
FROM items i LEFT JOIN users u ON i.seller_id = u.id WHERE u.id IS NULL
UNION ALL
SELECT IF(COUNT(*) = 0, 'PASS', 'FAIL') FROM items i LEFT JOIN users u ON i.seller_id = u.id WHERE u.id IS NULL;

-- 1b: Orders referencing non-existent items
SELECT '1b: Orders with non-existent item' AS 'Check';
SELECT CONCAT('FOUND ', COUNT(*), ' orphaned order(s)') AS 'Result'
FROM orders o LEFT JOIN items i ON o.item_id = i.id WHERE i.id IS NULL
UNION ALL
SELECT IF(COUNT(*) = 0, 'PASS', 'FAIL') FROM orders o LEFT JOIN items i ON o.item_id = i.id WHERE i.id IS NULL;

-- 1c: Reviews referencing non-existent orders
SELECT '1c: Reviews with non-existent order' AS 'Check';
SELECT CONCAT('FOUND ', COUNT(*), ' orphaned review(s)') AS 'Result'
FROM reviews r LEFT JOIN orders o ON r.order_id = o.id WHERE o.id IS NULL
UNION ALL
SELECT IF(COUNT(*) = 0, 'PASS', 'FAIL') FROM reviews r LEFT JOIN orders o ON r.order_id = o.id WHERE o.id IS NULL;

-- 1d: Favorites with non-existent items
SELECT '1d: Favorites with non-existent item' AS 'Check';
SELECT CONCAT('FOUND ', COUNT(*), ' orphaned favorite(s)') AS 'Result'
FROM favorites f LEFT JOIN items i ON f.item_id = i.id WHERE i.id IS NULL
UNION ALL
SELECT IF(COUNT(*) = 0, 'PASS', 'FAIL') FROM favorites f LEFT JOIN items i ON f.item_id = i.id WHERE i.id IS NULL;

-- 1e: Chat messages with non-existent chat
SELECT '1e: Chat messages with non-existent chat' AS 'Check';
SELECT CONCAT('FOUND ', COUNT(*), ' orphaned message(s)') AS 'Result'
FROM chat_messages cm LEFT JOIN chats c ON cm.chat_id = c.id WHERE c.id IS NULL
UNION ALL
SELECT IF(COUNT(*) = 0, 'PASS', 'FAIL') FROM chat_messages cm LEFT JOIN chats c ON cm.chat_id = c.id WHERE c.id IS NULL;

-- 1f: Item images with non-existent items
SELECT '1f: Item images with non-existent item' AS 'Check';
SELECT CONCAT('FOUND ', COUNT(*), ' orphaned image(s)') AS 'Result'
FROM item_images ii LEFT JOIN items i ON ii.item_id = i.id WHERE i.id IS NULL
UNION ALL
SELECT IF(COUNT(*) = 0, 'PASS', 'FAIL') FROM item_images ii LEFT JOIN items i ON ii.item_id = i.id WHERE i.id IS NULL;

SELECT '' AS '';

-- ============================================================
-- Section 2: Counter Consistency
-- ============================================================
SELECT '=== Section 2: Counter Consistency ===' AS '';

-- 2a: Favorite count mismatch
SELECT '2a: Favorite count mismatch' AS 'Check';
SELECT CONCAT('FOUND ', COUNT(*), ' item(s) with mismatched favorite_count') AS 'Result'
FROM items i
WHERE i.favorite_count != (SELECT COUNT(*) FROM favorites f WHERE f.item_id = i.id)
UNION ALL
SELECT IF(COUNT(*) = 0, 'PASS', 'FAIL')
FROM items i
WHERE i.favorite_count != (SELECT COUNT(*) FROM favorites f WHERE f.item_id = i.id);

-- 2b: Image count mismatch
SELECT '2b: Image count mismatch' AS 'Check';
SELECT CONCAT('FOUND ', COUNT(*), ' item(s) with mismatched image_count') AS 'Result'
FROM items i
WHERE i.image_count != (SELECT COUNT(*) FROM item_images ii WHERE ii.item_id = i.id)
UNION ALL
SELECT IF(COUNT(*) = 0, 'PASS', 'FAIL')
FROM items i
WHERE i.image_count != (SELECT COUNT(*) FROM item_images ii WHERE ii.item_id = i.id);

-- 2c: Unread message count mismatch
SELECT '2c: Unread message count mismatch' AS 'Check';
SELECT CONCAT('FOUND ', COUNT(*), ' chat(s) with mismatched unread_count') AS 'Result'
FROM chats c
WHERE c.unread_count != (SELECT COUNT(*) FROM chat_messages cm WHERE cm.chat_id = c.id AND cm.is_read = 0)
UNION ALL
SELECT IF(COUNT(*) = 0, 'PASS', 'FAIL')
FROM chats c
WHERE c.unread_count != (SELECT COUNT(*) FROM chat_messages cm WHERE cm.chat_id = c.id AND cm.is_read = 0);

SELECT '' AS '';

-- ============================================================
-- Section 3: State Consistency
-- ============================================================
SELECT '=== Section 3: State Consistency ===' AS '';

-- 3a: Items still on sale but have completed orders
SELECT '3a: Items on sale with completed orders' AS 'Check';
SELECT CONCAT('FOUND ', COUNT(*), ' item(s) on sale but have completed orders') AS 'Result'
FROM items i
JOIN orders o ON o.item_id = i.id
WHERE i.status = 'ON_SALE' AND o.order_status IN ('COMPLETED', 'SHIPPED')
UNION ALL
SELECT IF(COUNT(*) = 0, 'PASS', 'FAIL')
FROM items i
JOIN orders o ON o.item_id = i.id
WHERE i.status = 'ON_SALE' AND o.order_status IN ('COMPLETED', 'SHIPPED');

-- 3b: Items marked as sold but orders are cancelled
SELECT '3b: Items sold but orders cancelled' AS 'Check';
SELECT CONCAT('FOUND ', COUNT(*), ' item(s) sold but all orders cancelled') AS 'Result'
FROM items i
WHERE i.status = 'SOLD' AND NOT EXISTS (
    SELECT 1 FROM orders o
    WHERE o.item_id = i.id AND o.order_status NOT IN ('CANCELLED', 'REFUNDED')
)
UNION ALL
SELECT IF(COUNT(*) = 0, 'PASS', 'FAIL')
FROM items i
WHERE i.status = 'SOLD' AND NOT EXISTS (
    SELECT 1 FROM orders o
    WHERE o.item_id = i.id AND o.order_status NOT IN ('CANCELLED', 'REFUNDED')
);

-- 3c: Multiple active orders for the same item
SELECT '3c: Multiple active orders for same item' AS 'Check';
SELECT CONCAT('FOUND ', COUNT(*), ' item(s) with multiple active orders') AS 'Result'
FROM (
    SELECT item_id, COUNT(*) as cnt
    FROM orders
    WHERE order_status NOT IN ('CANCELLED', 'REFUNDED', 'COMPLETED')
    GROUP BY item_id
    HAVING cnt > 1
) multi
UNION ALL
SELECT IF(COUNT(*) = 0, 'PASS', 'FAIL')
FROM (
    SELECT item_id, COUNT(*) as cnt
    FROM orders
    WHERE order_status NOT IN ('CANCELLED', 'REFUNDED', 'COMPLETED')
    GROUP BY item_id
    HAVING cnt > 1
) multi;

SELECT '' AS '';

-- ============================================================
-- Section 4: Required Field Validation
-- ============================================================
SELECT '=== Section 4: Required Field Validation ===' AS '';

-- 4a: Users with null usernames or emails
SELECT '4a: Users with null username or email' AS 'Check';
SELECT CONCAT('FOUND ', COUNT(*), ' user(s) with null required fields') AS 'Result'
FROM users WHERE username IS NULL OR email IS NULL
UNION ALL
SELECT IF(COUNT(*) = 0, 'PASS', 'FAIL')
FROM users WHERE username IS NULL OR email IS NULL;

-- 4b: Items with negative prices
SELECT '4b: Items with negative prices' AS 'Check';
SELECT CONCAT('FOUND ', COUNT(*), ' item(s) with negative prices') AS 'Result'
FROM items WHERE price < 0
UNION ALL
SELECT IF(COUNT(*) = 0, 'PASS', 'FAIL')
FROM items WHERE price < 0;

-- 4c: Orders with negative amounts
SELECT '4c: Orders with negative amounts' AS 'Check';
SELECT CONCAT('FOUND ', COUNT(*), ' order(s) with negative amounts') AS 'Result'
FROM orders WHERE price < 0
UNION ALL
SELECT IF(COUNT(*) = 0, 'PASS', 'FAIL')
FROM orders WHERE price < 0;

SELECT '' AS '';

-- ============================================================
-- Section 5: Index & Table Status
-- ============================================================
SELECT '=== Section 5: Index & Table Status ===' AS '';

-- 5a: Table size and row count
SELECT '5a: Table size analysis' AS 'Check';
SELECT
    TABLE_NAME AS 'Table',
    ROUND(((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024), 2) AS 'Size (MB)',
    TABLE_ROWS AS 'Approx Rows',
    ENGINE AS 'Engine'
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = @db_name AND TABLE_TYPE = 'BASE TABLE'
ORDER BY (DATA_LENGTH + INDEX_LENGTH) DESC;

-- 5b: Index usage statistics
SELECT '5b: Index usage statistics' AS 'Check';
SELECT
    TABLE_NAME AS 'Table',
    INDEX_NAME AS 'Index',
    ROUND(COALESCE(SEQ_IN_INDEX, 0), 0) AS 'Col Position',
    COALESCE(CARDINALITY, 0) AS 'Cardinality',
    COALESCE(NULLABLE, '') AS 'Nullable',
    COALESCE(INDEX_TYPE, '') AS 'Type'
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = @db_name
ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;

-- 5c: Low cardinality indexes (may indicate poor index choices)
SELECT '5c: Indexes with very low cardinality' AS 'Check';
SELECT
    TABLE_NAME AS 'Table',
    INDEX_NAME AS 'Index',
    CARDINALITY AS 'Cardinality'
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = @db_name
    AND CARDINALITY > 0
    AND CARDINALITY < 10
    AND SEQ_IN_INDEX = 1
ORDER BY CARDINALITY;

SELECT '' AS '';
SELECT '========================================' AS '=== Data Integrity Check Completed ===';
