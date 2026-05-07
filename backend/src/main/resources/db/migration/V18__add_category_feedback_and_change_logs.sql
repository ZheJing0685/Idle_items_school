CREATE TABLE IF NOT EXISTS category_feedbacks (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    feedback_type VARCHAR(20) NOT NULL,
    category_id BIGINT,
    description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    admin_reply TEXT,
    reviewed_by BIGINT,
    reviewed_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_category_feedbacks_user_id (user_id),
    KEY idx_category_feedbacks_status (status),
    KEY idx_category_feedbacks_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS category_change_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    category_id BIGINT NOT NULL,
    category_name VARCHAR(50) NOT NULL,
    action VARCHAR(20) NOT NULL,
    operator_id BIGINT NOT NULL,
    details TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_category_change_logs_category_id (category_id),
    KEY idx_category_change_logs_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
