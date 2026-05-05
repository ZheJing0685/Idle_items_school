-- 添加新表

-- 创建用户地址表
CREATE TABLE IF NOT EXISTS user_addresses (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    receiver_name VARCHAR(50) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    province VARCHAR(50) NOT NULL,
    city VARCHAR(50) NOT NULL,
    district VARCHAR(50) NOT NULL,
    detail_address VARCHAR(200) NOT NULL,
    zip_code VARCHAR(10),
    is_default TINYINT(1) NOT NULL DEFAULT 0,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    PRIMARY KEY (id),
    KEY IDX_user_addresses_user_id_is_default (user_id, is_default),
    KEY IDX_user_addresses_user_id_is_deleted (user_id, is_deleted),
    CONSTRAINT FK_user_addresses_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建用户关注表
CREATE TABLE IF NOT EXISTS user_follows (
    id BIGINT NOT NULL AUTO_INCREMENT,
    follower_id BIGINT NOT NULL,
    following_id BIGINT NOT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY UK_user_follows_follower_following (follower_id, following_id),
    KEY IDX_user_follows_follower_id_is_deleted (follower_id, is_deleted),
    KEY IDX_user_follows_following_id_is_deleted (following_id, is_deleted),
    CONSTRAINT FK_user_follows_follower_id FOREIGN KEY (follower_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT FK_user_follows_following_id FOREIGN KEY (following_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建消息通知表
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    notification_type TINYINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content VARCHAR(500) NOT NULL,
    related_id BIGINT,
    related_type VARCHAR(50),
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    read_time DATETIME,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY IDX_notifications_user_id_is_read_created_at (user_id, is_read, created_at),
    KEY IDX_notifications_user_id_notification_type (user_id, notification_type),
    CONSTRAINT FK_notifications_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建系统配置表
CREATE TABLE IF NOT EXISTS system_configs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    config_key VARCHAR(100) NOT NULL,
    config_value TEXT NOT NULL,
    config_type TINYINT NOT NULL DEFAULT 1,
    description VARCHAR(255),
    is_editable TINYINT(1) NOT NULL DEFAULT 1,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY UK_system_configs_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建操作日志表
CREATE TABLE IF NOT EXISTS operation_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT,
    operation_type VARCHAR(50) NOT NULL,
    operation_desc VARCHAR(255) NOT NULL,
    request_url VARCHAR(500),
    request_method VARCHAR(10),
    request_params TEXT,
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    execution_time INT,
    status TINYINT NOT NULL DEFAULT 1,
    error_message VARCHAR(1000),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY IDX_operation_logs_user_id_operation_type (user_id, operation_type),
    KEY IDX_operation_logs_created_at (created_at),
    KEY IDX_operation_logs_status (status),
    CONSTRAINT FK_operation_logs_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;