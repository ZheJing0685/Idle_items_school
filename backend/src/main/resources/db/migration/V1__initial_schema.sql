-- 初始表结构

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    avatar VARCHAR(255),
    role TINYINT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    verified TINYINT NOT NULL DEFAULT 0,
    student_id VARCHAR(20),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY UK_users_username (username),
    UNIQUE KEY UK_users_email (email),
    UNIQUE KEY UK_users_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建分类表
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    parent_id BIGINT,
    sort_order INT NOT NULL DEFAULT 0,
    icon VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    PRIMARY KEY (id),
    KEY IDX_categories_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建商品表
CREATE TABLE IF NOT EXISTS items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    original_price DECIMAL(10,2),
    `condition` TINYINT NOT NULL DEFAULT 1,
    status TINYINT NOT NULL DEFAULT 1,
    view_count INT NOT NULL DEFAULT 0,
    favorite_count INT NOT NULL DEFAULT 0,
    reject_reason VARCHAR(500),
    location VARCHAR(100) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    PRIMARY KEY (id),
    KEY IDX_items_user_id (user_id),
    KEY IDX_items_category_id (category_id),
    KEY IDX_items_status (status),
    CONSTRAINT FK_items_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT FK_items_category_id FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建订单表
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_no VARCHAR(50) NOT NULL,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    item_title VARCHAR(100) NOT NULL,
    item_image VARCHAR(255) NOT NULL,
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    order_status TINYINT NOT NULL DEFAULT 0,
    buyer_address VARCHAR(200),
    buyer_phone VARCHAR(20),
    buyer_name VARCHAR(50),
    payment_method TINYINT,
    payment_time DATETIME,
    ship_time DATETIME,
    deliver_time DATETIME,
    complete_time DATETIME,
    cancel_reason VARCHAR(500),
    refund_reason VARCHAR(500),
    refund_time DATETIME,
    refund_amount DECIMAL(10,2),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY UK_orders_order_no (order_no),
    KEY IDX_orders_buyer_id (buyer_id),
    KEY IDX_orders_seller_id (seller_id),
    KEY IDX_orders_item_id (item_id),
    KEY IDX_orders_order_status (order_status),
    CONSTRAINT FK_orders_buyer_id FOREIGN KEY (buyer_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT FK_orders_seller_id FOREIGN KEY (seller_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT FK_orders_item_id FOREIGN KEY (item_id) REFERENCES items (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建实名认证记录表
CREATE TABLE IF NOT EXISTS verification_records (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    student_id VARCHAR(20) NOT NULL,
    id_card VARCHAR(20) NOT NULL,
    student_card_image VARCHAR(255) NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    reject_reason VARCHAR(500),
    reviewer_id BIGINT,
    reviewed_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    PRIMARY KEY (id),
    KEY IDX_verification_records_user_id (user_id),
    KEY IDX_verification_records_status (status),
    CONSTRAINT FK_verification_records_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建评价表
CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    reviewed_user_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    rating TINYINT NOT NULL DEFAULT 5,
    content TEXT NOT NULL,
    images VARCHAR(1000),
    is_anonymous TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    PRIMARY KEY (id),
    KEY IDX_reviews_order_id (order_id),
    KEY IDX_reviews_reviewer_id (reviewer_id),
    KEY IDX_reviews_reviewed_user_id (reviewed_user_id),
    KEY IDX_reviews_item_id (item_id),
    CONSTRAINT FK_reviews_order_id FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT FK_reviews_reviewer_id FOREIGN KEY (reviewer_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT FK_reviews_reviewed_user_id FOREIGN KEY (reviewed_user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT FK_reviews_item_id FOREIGN KEY (item_id) REFERENCES items (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建收藏表
CREATE TABLE IF NOT EXISTS favorites (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY UK_favorites_user_item (user_id, item_id),
    KEY IDX_favorites_user_id (user_id),
    KEY IDX_favorites_item_id (item_id),
    CONSTRAINT FK_favorites_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT FK_favorites_item_id FOREIGN KEY (item_id) REFERENCES items (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建聊天表
CREATE TABLE IF NOT EXISTS chats (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT,
    item_id BIGINT,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    PRIMARY KEY (id),
    KEY IDX_chats_order_id (order_id),
    KEY IDX_chats_item_id (item_id),
    KEY IDX_chats_buyer_id (buyer_id),
    KEY IDX_chats_seller_id (seller_id),
    CONSTRAINT FK_chats_order_id FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT FK_chats_item_id FOREIGN KEY (item_id) REFERENCES items (id) ON DELETE CASCADE,
    CONSTRAINT FK_chats_buyer_id FOREIGN KEY (buyer_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT FK_chats_seller_id FOREIGN KEY (seller_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建聊天消息表
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT NOT NULL AUTO_INCREMENT,
    chat_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    message_type TINYINT NOT NULL DEFAULT 1,
    content VARCHAR(1000),
    is_anonymous TINYINT(1) NOT NULL DEFAULT 0,
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    read_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY IDX_chat_messages_chat_id (chat_id),
    KEY IDX_chat_messages_sender_id (sender_id),
    KEY IDX_chat_messages_receiver_id (receiver_id),
    KEY IDX_chat_messages_is_read (is_read),
    CONSTRAINT FK_chat_messages_chat_id FOREIGN KEY (chat_id) REFERENCES chats (id) ON DELETE CASCADE,
    CONSTRAINT FK_chat_messages_sender_id FOREIGN KEY (sender_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT FK_chat_messages_receiver_id FOREIGN KEY (receiver_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建纠纷表
CREATE TABLE IF NOT EXISTS disputes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    applicant_id BIGINT NOT NULL,
    respondent_id BIGINT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    evidence_images VARCHAR(1000),
    dispute_status TINYINT NOT NULL DEFAULT 0,
    handler_id BIGINT,
    result TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    PRIMARY KEY (id),
    KEY IDX_disputes_order_id (order_id),
    KEY IDX_disputes_applicant_id (applicant_id),
    KEY IDX_disputes_respondent_id (respondent_id),
    KEY IDX_disputes_dispute_status (dispute_status),
    CONSTRAINT FK_disputes_order_id FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT FK_disputes_applicant_id FOREIGN KEY (applicant_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT FK_disputes_respondent_id FOREIGN KEY (respondent_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建商品图片表
CREATE TABLE IF NOT EXISTS item_images (
    id BIGINT NOT NULL AUTO_INCREMENT,
    item_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    thumbnail_url VARCHAR(255) NOT NULL,
    is_cover TINYINT(1) NOT NULL DEFAULT 0,
    sort_order INT NOT NULL DEFAULT 0,
    width INT NOT NULL DEFAULT 0,
    height INT NOT NULL DEFAULT 0,
    file_size BIGINT NOT NULL DEFAULT 0,
    format VARCHAR(10) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY IDX_item_images_item_id (item_id),
    KEY IDX_item_images_is_cover (is_cover),
    CONSTRAINT FK_item_images_item_id FOREIGN KEY (item_id) REFERENCES items (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建商品标签表
CREATE TABLE IF NOT EXISTS item_tags (
    id BIGINT NOT NULL AUTO_INCREMENT,
    item_id BIGINT NOT NULL,
    tag_name VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY IDX_item_tags_item_id (item_id),
    KEY IDX_item_tags_tag_name (tag_name),
    CONSTRAINT FK_item_tags_item_id FOREIGN KEY (item_id) REFERENCES items (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建管理员日志表
CREATE TABLE IF NOT EXISTS admin_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    admin_id BIGINT NOT NULL,
    operation VARCHAR(100) NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id BIGINT,
    details TEXT,
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY IDX_admin_logs_admin_id (admin_id),
    KEY IDX_admin_logs_operation (operation),
    CONSTRAINT FK_admin_logs_admin_id FOREIGN KEY (admin_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建图片分析表
CREATE TABLE IF NOT EXISTS image_analysis (
    id BIGINT NOT NULL AUTO_INCREMENT,
    image_url VARCHAR(255) NOT NULL,
    item_id BIGINT,
    analysis_result TEXT,
    item_type VARCHAR(50),
    brand VARCHAR(100),
    color VARCHAR(50),
    confidence DECIMAL(5,4),
    status TINYINT NOT NULL DEFAULT 0,
    error_message VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY IDX_image_analysis_item_id (item_id),
    KEY IDX_image_analysis_status (status),
    CONSTRAINT FK_image_analysis_item_id FOREIGN KEY (item_id) REFERENCES items (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
