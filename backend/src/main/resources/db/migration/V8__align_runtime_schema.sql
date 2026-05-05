ALTER TABLE users
    MODIFY COLUMN role VARCHAR(20) NULL,
    MODIFY COLUMN status VARCHAR(20) NULL;

UPDATE users
SET role = CASE role
    WHEN '0' THEN 'STUDENT'
    WHEN '1' THEN 'STUDENT'
    WHEN '2' THEN 'ADMIN'
    WHEN '3' THEN 'ADMIN'
    ELSE role
END;

UPDATE users
SET role = 'STUDENT'
WHERE role IS NULL
   OR role = ''
   OR role NOT IN ('STUDENT', 'ADMIN');

UPDATE users
SET status = CASE status
    WHEN '0' THEN 'DISABLED'
    WHEN '1' THEN 'ACTIVE'
    ELSE status
END;

UPDATE users
SET status = 'ACTIVE'
WHERE status IS NULL
   OR status = ''
   OR status NOT IN ('ACTIVE', 'DISABLED');

ALTER TABLE users
    MODIFY COLUMN role ENUM('STUDENT', 'ADMIN') NULL DEFAULT 'STUDENT',
    MODIFY COLUMN status ENUM('ACTIVE', 'DISABLED') NULL DEFAULT 'ACTIVE';

SET @has_items_item_condition := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'items'
      AND COLUMN_NAME = 'item_condition'
);
SET @items_item_condition_add_sql := IF(
    @has_items_item_condition = 0,
    "ALTER TABLE items ADD COLUMN item_condition VARCHAR(16) NULL AFTER original_price",
    "SELECT 1"
);
PREPARE stmt_items_item_condition_add FROM @items_item_condition_add_sql;
EXECUTE stmt_items_item_condition_add;
DEALLOCATE PREPARE stmt_items_item_condition_add;

SET @has_items_contact_name := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'items'
      AND COLUMN_NAME = 'contact_name'
);
SET @items_contact_name_add_sql := IF(
    @has_items_contact_name = 0,
    "ALTER TABLE items ADD COLUMN contact_name VARCHAR(50) NULL AFTER tags",
    "SELECT 1"
);
PREPARE stmt_items_contact_name_add FROM @items_contact_name_add_sql;
EXECUTE stmt_items_contact_name_add;
DEALLOCATE PREPARE stmt_items_contact_name_add;

SET @has_items_contact_phone := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'items'
      AND COLUMN_NAME = 'contact_phone'
);
SET @items_contact_phone_add_sql := IF(
    @has_items_contact_phone = 0,
    "ALTER TABLE items ADD COLUMN contact_phone VARCHAR(20) NULL AFTER contact_name",
    "SELECT 1"
);
PREPARE stmt_items_contact_phone_add FROM @items_contact_phone_add_sql;
EXECUTE stmt_items_contact_phone_add;
DEALLOCATE PREPARE stmt_items_contact_phone_add;

SET @has_items_condition := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'items'
      AND COLUMN_NAME = 'condition'
);

SET @items_condition_sql := IF(
    @has_items_condition > 0,
    "UPDATE items
     SET item_condition = CASE `condition`
         WHEN 1 THEN 'NEW'
         WHEN 2 THEN 'LIKE_NEW'
         WHEN 3 THEN 'GOOD'
         WHEN 4 THEN 'FAIR'
         WHEN 5 THEN 'POOR'
         WHEN '1' THEN 'NEW'
         WHEN '2' THEN 'LIKE_NEW'
         WHEN '3' THEN 'GOOD'
         WHEN '4' THEN 'FAIR'
         WHEN '5' THEN 'POOR'
         ELSE item_condition
     END
     WHERE item_condition IS NULL OR item_condition = ''",
    "SELECT 1"
);
PREPARE stmt_items_condition FROM @items_condition_sql;
EXECUTE stmt_items_condition;
DEALLOCATE PREPARE stmt_items_condition;

UPDATE items
SET item_condition = 'GOOD'
WHERE item_condition IS NULL
   OR item_condition = ''
   OR item_condition NOT IN ('NEW', 'LIKE_NEW', 'GOOD', 'FAIR', 'POOR');

ALTER TABLE items
    MODIFY COLUMN status VARCHAR(16) NOT NULL;

UPDATE items
SET status = CASE status
    WHEN '0' THEN 'DRAFT'
    WHEN '1' THEN 'ON_SALE'
    WHEN '2' THEN 'SOLD'
    WHEN '3' THEN 'OFF_SHELF'
    WHEN '4' THEN 'PENDING'
    WHEN '5' THEN 'REJECTED'
    ELSE status
END;

UPDATE items
SET status = 'PENDING'
WHERE status IS NULL
   OR status = ''
   OR status NOT IN ('DRAFT', 'PENDING', 'ON_SALE', 'SOLD', 'OFF_SHELF', 'REJECTED');

ALTER TABLE items
    MODIFY COLUMN item_condition ENUM('NEW', 'LIKE_NEW', 'GOOD', 'FAIR', 'POOR') NULL DEFAULT 'GOOD',
    MODIFY COLUMN status ENUM('DRAFT', 'PENDING', 'ON_SALE', 'SOLD', 'OFF_SHELF', 'REJECTED') NOT NULL DEFAULT 'PENDING';

ALTER TABLE orders
    MODIFY COLUMN order_status VARCHAR(30) NOT NULL DEFAULT 'PENDING_PAYMENT',
    MODIFY COLUMN payment_method VARCHAR(50) NULL;

UPDATE orders
SET order_status = CASE order_status
    WHEN 'PAID' THEN 'PENDING_SHIPMENT'
    WHEN 'DELIVERED' THEN 'COMPLETED'
    ELSE order_status
END;

UPDATE orders
SET payment_method = CASE payment_method
    WHEN '0' THEN 'OFFLINE'
    WHEN '1' THEN 'WECHAT_PAY'
    WHEN '2' THEN 'ALIPAY'
    WHEN '微信支付' THEN 'WECHAT_PAY'
    WHEN '支付宝' THEN 'ALIPAY'
    WHEN '线下交易' THEN 'OFFLINE'
    ELSE payment_method
END
WHERE payment_method IS NOT NULL;

ALTER TABLE verification_records
    MODIFY COLUMN status VARCHAR(20) NOT NULL;

SET @has_verification_school := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'verification_records'
      AND COLUMN_NAME = 'school'
);
SET @verification_school_add_sql := IF(
    @has_verification_school = 0,
    "ALTER TABLE verification_records ADD COLUMN school VARCHAR(100) NULL",
    "SELECT 1"
);
PREPARE stmt_verification_school_add FROM @verification_school_add_sql;
EXECUTE stmt_verification_school_add;
DEALLOCATE PREPARE stmt_verification_school_add;

SET @has_verification_student_card := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'verification_records'
      AND COLUMN_NAME = 'student_card'
);
SET @verification_student_card_add_sql := IF(
    @has_verification_student_card = 0,
    "ALTER TABLE verification_records ADD COLUMN student_card VARCHAR(255) NULL",
    "SELECT 1"
);
PREPARE stmt_verification_student_card_add FROM @verification_student_card_add_sql;
EXECUTE stmt_verification_student_card_add;
DEALLOCATE PREPARE stmt_verification_student_card_add;

SET @has_verification_teacher_card := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'verification_records'
      AND COLUMN_NAME = 'teacher_card'
);
SET @verification_teacher_card_add_sql := IF(
    @has_verification_teacher_card = 0,
    "ALTER TABLE verification_records ADD COLUMN teacher_card VARCHAR(255) NULL",
    "SELECT 1"
);
PREPARE stmt_verification_teacher_card_add FROM @verification_teacher_card_add_sql;
EXECUTE stmt_verification_teacher_card_add;
DEALLOCATE PREPARE stmt_verification_teacher_card_add;

SET @has_verification_id_card_front := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'verification_records'
      AND COLUMN_NAME = 'id_card_front'
);
SET @verification_id_card_front_add_sql := IF(
    @has_verification_id_card_front = 0,
    "ALTER TABLE verification_records ADD COLUMN id_card_front VARCHAR(255) NULL",
    "SELECT 1"
);
PREPARE stmt_verification_id_card_front_add FROM @verification_id_card_front_add_sql;
EXECUTE stmt_verification_id_card_front_add;
DEALLOCATE PREPARE stmt_verification_id_card_front_add;

SET @has_verification_id_card_back := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'verification_records'
      AND COLUMN_NAME = 'id_card_back'
);
SET @verification_id_card_back_add_sql := IF(
    @has_verification_id_card_back = 0,
    "ALTER TABLE verification_records ADD COLUMN id_card_back VARCHAR(255) NULL",
    "SELECT 1"
);
PREPARE stmt_verification_id_card_back_add FROM @verification_id_card_back_add_sql;
EXECUTE stmt_verification_id_card_back_add;
DEALLOCATE PREPARE stmt_verification_id_card_back_add;

SET @has_verification_type := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'verification_records'
      AND COLUMN_NAME = 'type'
);
SET @verification_type_add_sql := IF(
    @has_verification_type = 0,
    "ALTER TABLE verification_records ADD COLUMN type VARCHAR(20) NULL",
    "SELECT 1"
);
PREPARE stmt_verification_type_add FROM @verification_type_add_sql;
EXECUTE stmt_verification_type_add;
DEALLOCATE PREPARE stmt_verification_type_add;

SET @has_verification_school_name := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'verification_records'
      AND COLUMN_NAME = 'school_name'
);

SET @verification_school_sql := IF(
    @has_verification_school_name > 0,
    "UPDATE verification_records
     SET school = COALESCE(school, school_name)
     WHERE school IS NULL OR school = ''",
    "SELECT 1"
);
PREPARE stmt_verification_school FROM @verification_school_sql;
EXECUTE stmt_verification_school;
DEALLOCATE PREPARE stmt_verification_school;

SET @has_verification_student_card_image := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'verification_records'
      AND COLUMN_NAME = 'student_card_image'
);

SET @verification_student_card_sql := IF(
    @has_verification_student_card_image > 0,
    "UPDATE verification_records
     SET student_card = COALESCE(student_card, student_card_image)
     WHERE student_card IS NULL OR student_card = ''",
    "SELECT 1"
);
PREPARE stmt_verification_student_card FROM @verification_student_card_sql;
EXECUTE stmt_verification_student_card;
DEALLOCATE PREPARE stmt_verification_student_card;

UPDATE verification_records
SET status = CASE status
    WHEN '0' THEN 'PENDING'
    WHEN '1' THEN 'APPROVED'
    WHEN '2' THEN 'REJECTED'
    ELSE status
END;

UPDATE verification_records
SET status = 'PENDING'
WHERE status IS NULL
   OR status = ''
   OR status NOT IN ('PENDING', 'APPROVED', 'REJECTED');

UPDATE verification_records
SET type = CASE
    WHEN type = '0' THEN 'ID_CARD'
    WHEN type = '1' THEN 'STUDENT_CARD'
    WHEN type = '2' THEN 'TEACHER_CARD'
    WHEN type IN ('ID_CARD', 'STUDENT_CARD', 'TEACHER_CARD') THEN type
    WHEN teacher_card IS NOT NULL AND teacher_card <> '' THEN 'TEACHER_CARD'
    WHEN student_card IS NOT NULL AND student_card <> '' THEN 'STUDENT_CARD'
    ELSE 'ID_CARD'
END;

ALTER TABLE verification_records
    MODIFY COLUMN status ENUM('PENDING', 'APPROVED', 'REJECTED') NULL DEFAULT 'PENDING',
    MODIFY COLUMN type ENUM('ID_CARD', 'STUDENT_CARD', 'TEACHER_CARD') NOT NULL;

ALTER TABLE chat_messages
    MODIFY COLUMN message_type VARCHAR(20) NOT NULL;

UPDATE chat_messages
SET message_type = CASE message_type
    WHEN '1' THEN 'TEXT'
    WHEN '2' THEN 'IMAGE'
    WHEN '3' THEN 'SYSTEM'
    WHEN 'FILE' THEN 'SYSTEM'
    ELSE message_type
END;

UPDATE chat_messages
SET message_type = 'TEXT'
WHERE message_type IS NULL
   OR message_type = ''
   OR message_type NOT IN ('TEXT', 'IMAGE', 'SYSTEM');

ALTER TABLE chat_messages
    MODIFY COLUMN message_type ENUM('TEXT', 'IMAGE', 'SYSTEM') NULL DEFAULT 'TEXT';

ALTER TABLE disputes
    MODIFY COLUMN dispute_status VARCHAR(20) NOT NULL;

UPDATE disputes
SET dispute_status = CASE dispute_status
    WHEN '0' THEN 'PENDING'
    WHEN '1' THEN 'PROCESSING'
    WHEN '2' THEN 'RESOLVED'
    WHEN '3' THEN 'CLOSED'
    ELSE dispute_status
END;

UPDATE disputes
SET dispute_status = 'PENDING'
WHERE dispute_status IS NULL
   OR dispute_status = ''
   OR dispute_status NOT IN ('PENDING', 'PROCESSING', 'RESOLVED', 'CLOSED');

ALTER TABLE disputes
    MODIFY COLUMN dispute_status ENUM('PENDING', 'PROCESSING', 'RESOLVED', 'CLOSED') NOT NULL DEFAULT 'PENDING';

ALTER TABLE image_analysis
    MODIFY COLUMN status VARCHAR(20) NOT NULL;

UPDATE image_analysis
SET status = CASE status
    WHEN '0' THEN 'PENDING'
    WHEN '1' THEN 'PENDING'
    WHEN '2' THEN 'SUCCESS'
    WHEN '3' THEN 'FAILED'
    ELSE status
END;

UPDATE image_analysis
SET status = 'PENDING'
WHERE status IS NULL
   OR status = ''
   OR status NOT IN ('PENDING', 'SUCCESS', 'FAILED');

ALTER TABLE image_analysis
    MODIFY COLUMN status ENUM('PENDING', 'SUCCESS', 'FAILED') NOT NULL DEFAULT 'PENDING';
