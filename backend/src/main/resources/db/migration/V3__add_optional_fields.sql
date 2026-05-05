-- 添加可选字段（中低优先级）

-- users表
ALTER TABLE users ADD COLUMN gender TINYINT;
ALTER TABLE users ADD COLUMN birthday DATE;
ALTER TABLE users ADD COLUMN bio VARCHAR(500);
ALTER TABLE users ADD COLUMN school_name VARCHAR(100);

-- items表
ALTER TABLE items ADD COLUMN publish_time DATETIME;
ALTER TABLE items ADD COLUMN off_shelf_time DATETIME;
ALTER TABLE items ADD COLUMN sold_time DATETIME;
ALTER TABLE items ADD COLUMN quality_score DECIMAL(3,2);
ALTER TABLE items ADD COLUMN is_bargain_allowed TINYINT(1) NOT NULL DEFAULT 1;
ALTER TABLE items ADD COLUMN min_price DECIMAL(10,2);
ALTER TABLE items ADD COLUMN contact_type TINYINT NOT NULL DEFAULT 1;
ALTER TABLE items ADD COLUMN contact_info VARCHAR(100);
ALTER TABLE items ADD COLUMN is_recommended TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE items ADD COLUMN recommend_time DATETIME;
ALTER TABLE items ADD COLUMN weight INT NOT NULL DEFAULT 0;
ALTER TABLE items ADD COLUMN delivery_method TINYINT NOT NULL DEFAULT 1;
ALTER TABLE items ADD COLUMN tags VARCHAR(500);
ALTER TABLE items ADD COLUMN brand VARCHAR(100);
ALTER TABLE items ADD COLUMN purchase_date DATE;
ALTER TABLE items ADD COLUMN warranty_info VARCHAR(255);

-- 添加items表索引
CREATE INDEX IDX_items_publish_time ON items (publish_time);
CREATE INDEX IDX_items_is_recommended_recommend_time ON items (is_recommended, recommend_time);
CREATE INDEX IDX_items_view_count_favorite_count ON items (view_count, favorite_count);

-- orders表
ALTER TABLE orders ADD COLUMN seller_note VARCHAR(500);
ALTER TABLE orders ADD COLUMN buyer_note VARCHAR(500);
ALTER TABLE orders ADD COLUMN auto_confirm_time DATETIME;
ALTER TABLE orders ADD COLUMN close_time DATETIME;
ALTER TABLE orders ADD COLUMN close_type TINYINT;
ALTER TABLE orders ADD COLUMN source TINYINT NOT NULL DEFAULT 1;
ALTER TABLE orders ADD COLUMN ip_address VARCHAR(50);
ALTER TABLE orders ADD COLUMN user_agent VARCHAR(500);

-- categories表
ALTER TABLE categories ADD COLUMN level TINYINT NOT NULL DEFAULT 1;
ALTER TABLE categories ADD COLUMN path VARCHAR(500);
ALTER TABLE categories ADD COLUMN is_show TINYINT(1) NOT NULL DEFAULT 1;
ALTER TABLE categories ADD COLUMN item_count INT NOT NULL DEFAULT 0;
ALTER TABLE categories ADD COLUMN keywords VARCHAR(255);
ALTER TABLE categories ADD COLUMN meta_description VARCHAR(500);
ALTER TABLE categories ADD COLUMN image VARCHAR(255);
ALTER TABLE categories ADD COLUMN background_color VARCHAR(20);

-- 添加categories表索引
CREATE INDEX IDX_categories_level_sort_order ON categories (level, sort_order);
CREATE INDEX IDX_categories_is_show ON categories (is_show);

-- verification_records表
ALTER TABLE verification_records ADD COLUMN school_name VARCHAR(100);
ALTER TABLE verification_records ADD COLUMN department VARCHAR(100);
ALTER TABLE verification_records ADD COLUMN major VARCHAR(100);
ALTER TABLE verification_records ADD COLUMN enrollment_year INT;
ALTER TABLE verification_records ADD COLUMN graduation_year INT;
ALTER TABLE verification_records ADD COLUMN student_card_back_image VARCHAR(255);
ALTER TABLE verification_records ADD COLUMN id_card_front_image VARCHAR(255);
ALTER TABLE verification_records ADD COLUMN id_card_back_image VARCHAR(255);
ALTER TABLE verification_records ADD COLUMN face_recognition_image VARCHAR(255);
ALTER TABLE verification_records ADD COLUMN face_recognition_passed TINYINT(1);
ALTER TABLE verification_records ADD COLUMN submit_count INT NOT NULL DEFAULT 1;
ALTER TABLE verification_records ADD COLUMN last_submit_time DATETIME;
ALTER TABLE verification_records ADD COLUMN review_remark VARCHAR(500);
ALTER TABLE verification_records ADD COLUMN auto_approved TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE verification_records ADD COLUMN risk_level TINYINT NOT NULL DEFAULT 0;

-- 添加verification_records表索引
CREATE INDEX IDX_verification_records_user_id_status ON verification_records (user_id, status);
CREATE INDEX IDX_verification_records_status_reviewed_at ON verification_records (status, reviewed_at);

-- reviews表
ALTER TABLE reviews ADD COLUMN reply_content VARCHAR(1000);
ALTER TABLE reviews ADD COLUMN reply_time DATETIME;
ALTER TABLE reviews ADD COLUMN is_show TINYINT(1) NOT NULL DEFAULT 1;
ALTER TABLE reviews ADD COLUMN helpful_count INT NOT NULL DEFAULT 0;
ALTER TABLE reviews ADD COLUMN report_count INT NOT NULL DEFAULT 0;
ALTER TABLE reviews ADD COLUMN is_reported TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE reviews ADD COLUMN report_reason VARCHAR(255);
ALTER TABLE reviews ADD COLUMN check_status TINYINT NOT NULL DEFAULT 0;
ALTER TABLE reviews ADD COLUMN check_time DATETIME;
ALTER TABLE reviews ADD COLUMN check_remark VARCHAR(255);
ALTER TABLE reviews ADD COLUMN tag VARCHAR(100);

-- 添加reviews表索引
CREATE INDEX IDX_reviews_item_id_rating ON reviews (item_id, rating);
CREATE INDEX IDX_reviews_check_status_is_show ON reviews (check_status, is_show);

-- favorites表
ALTER TABLE favorites ADD COLUMN category_id BIGINT;
ALTER TABLE favorites ADD COLUMN price_snapshot DECIMAL(10,2);
ALTER TABLE favorites ADD COLUMN remark VARCHAR(255);
ALTER TABLE favorites ADD COLUMN notify_when_price_drop TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE favorites ADD COLUMN target_price DECIMAL(10,2);

-- chats表
ALTER TABLE chats ADD COLUMN last_message_id BIGINT;
ALTER TABLE chats ADD COLUMN last_message_content VARCHAR(500);
ALTER TABLE chats ADD COLUMN last_message_time DATETIME;
ALTER TABLE chats ADD COLUMN last_message_sender_id BIGINT;
ALTER TABLE chats ADD COLUMN buyer_unread_count INT NOT NULL DEFAULT 0;
ALTER TABLE chats ADD COLUMN seller_unread_count INT NOT NULL DEFAULT 0;
ALTER TABLE chats ADD COLUMN is_blocked TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE chats ADD COLUMN blocked_by BIGINT;
ALTER TABLE chats ADD COLUMN blocked_time DATETIME;
ALTER TABLE chats ADD COLUMN is_muted TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE chats ADD COLUMN muted_by BIGINT;
ALTER TABLE chats ADD COLUMN chat_status TINYINT NOT NULL DEFAULT 1;
ALTER TABLE chats ADD COLUMN close_time DATETIME;
ALTER TABLE chats ADD COLUMN close_reason VARCHAR(255);

-- 添加chats表索引
CREATE INDEX IDX_chats_last_message_time ON chats (last_message_time);
CREATE INDEX IDX_chats_chat_status_is_blocked ON chats (chat_status, is_blocked);

-- chat_messages表
ALTER TABLE chat_messages ADD COLUMN message_status TINYINT NOT NULL DEFAULT 1;
ALTER TABLE chat_messages ADD COLUMN send_time DATETIME;
ALTER TABLE chat_messages ADD COLUMN receive_time DATETIME;
ALTER TABLE chat_messages ADD COLUMN image_url VARCHAR(255);
ALTER TABLE chat_messages ADD COLUMN image_width INT;
ALTER TABLE chat_messages ADD COLUMN image_height INT;
ALTER TABLE chat_messages ADD COLUMN file_url VARCHAR(255);
ALTER TABLE chat_messages ADD COLUMN file_name VARCHAR(255);
ALTER TABLE chat_messages ADD COLUMN file_size BIGINT;
ALTER TABLE chat_messages ADD COLUMN is_recalled TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE chat_messages ADD COLUMN recall_time DATETIME;
ALTER TABLE chat_messages ADD COLUMN is_deleted_by_sender TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE chat_messages ADD COLUMN is_deleted_by_receiver TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE chat_messages ADD COLUMN reply_to_message_id BIGINT;
ALTER TABLE chat_messages ADD COLUMN reply_to_content VARCHAR(500);

-- 添加chat_messages表索引
CREATE INDEX IDX_chat_messages_chat_id_send_time ON chat_messages (chat_id, send_time);

-- disputes表
ALTER TABLE disputes ADD COLUMN dispute_type TINYINT NOT NULL DEFAULT 1;
ALTER TABLE disputes ADD COLUMN expect_result VARCHAR(500);
ALTER TABLE disputes ADD COLUMN expect_refund_amount DECIMAL(10,2);
ALTER TABLE disputes ADD COLUMN actual_refund_amount DECIMAL(10,2);
ALTER TABLE disputes ADD COLUMN process_remark VARCHAR(1000);
ALTER TABLE disputes ADD COLUMN process_logs TEXT;
ALTER TABLE disputes ADD COLUMN is_urgent TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE disputes ADD COLUMN priority TINYINT NOT NULL DEFAULT 1;
ALTER TABLE disputes ADD COLUMN assign_time DATETIME;
ALTER TABLE disputes ADD COLUMN start_process_time DATETIME;
ALTER TABLE disputes ADD COLUMN complete_time DATETIME;
ALTER TABLE disputes ADD COLUMN close_time DATETIME;
ALTER TABLE disputes ADD COLUMN close_type TINYINT;
ALTER TABLE disputes ADD COLUMN is_escalated TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE disputes ADD COLUMN escalated_to BIGINT;
ALTER TABLE disputes ADD COLUMN escalated_time DATETIME;
ALTER TABLE disputes ADD COLUMN escalated_reason VARCHAR(255);
ALTER TABLE disputes ADD COLUMN satisfaction TINYINT;
ALTER TABLE disputes ADD COLUMN satisfaction_remark VARCHAR(500);

-- 添加disputes表索引
CREATE INDEX IDX_disputes_dispute_status_priority ON disputes (dispute_status, priority);
CREATE INDEX IDX_disputes_assign_time_start_process_time ON disputes (assign_time, start_process_time);

-- item_images表
ALTER TABLE item_images ADD COLUMN image_hash VARCHAR(64);
ALTER TABLE item_images ADD COLUMN storage_type TINYINT NOT NULL DEFAULT 1;
ALTER TABLE item_images ADD COLUMN storage_path VARCHAR(500);
ALTER TABLE item_images ADD COLUMN is_compressed TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE item_images ADD COLUMN is_watermarked TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE item_images ADD COLUMN ai_analysis_result TEXT;
ALTER TABLE item_images ADD COLUMN ai_analysis_status TINYINT NOT NULL DEFAULT 0;

-- 添加item_images表索引
CREATE INDEX IDX_item_images_ai_analysis_status ON item_images (ai_analysis_status);

-- item_tags表
ALTER TABLE item_tags ADD COLUMN tag_type TINYINT NOT NULL DEFAULT 1;
ALTER TABLE item_tags ADD COLUMN tag_category VARCHAR(50);
ALTER TABLE item_tags ADD COLUMN weight INT NOT NULL DEFAULT 1;

-- 添加item_tags表索引
CREATE INDEX IDX_item_tags_tag_name_tag_type ON item_tags (tag_name, tag_type);
CREATE INDEX IDX_item_tags_weight ON item_tags (weight);

-- admin_logs表
ALTER TABLE admin_logs ADD COLUMN log_type TINYINT NOT NULL DEFAULT 1;
ALTER TABLE admin_logs ADD COLUMN log_level TINYINT NOT NULL DEFAULT 1;
ALTER TABLE admin_logs ADD COLUMN request_url VARCHAR(500);
ALTER TABLE admin_logs ADD COLUMN request_method VARCHAR(10);
ALTER TABLE admin_logs ADD COLUMN request_params TEXT;
ALTER TABLE admin_logs ADD COLUMN response_data TEXT;
ALTER TABLE admin_logs ADD COLUMN execution_time INT;
ALTER TABLE admin_logs ADD COLUMN status TINYINT NOT NULL DEFAULT 1;
ALTER TABLE admin_logs ADD COLUMN error_message VARCHAR(1000);
ALTER TABLE admin_logs ADD COLUMN stack_trace TEXT;

-- 添加admin_logs表索引
CREATE INDEX IDX_admin_logs_log_type_log_level ON admin_logs (log_type, log_level);
CREATE INDEX IDX_admin_logs_created_at ON admin_logs (created_at);

-- image_analysis表
ALTER TABLE image_analysis ADD COLUMN analysis_type TINYINT NOT NULL DEFAULT 1;
ALTER TABLE image_analysis ADD COLUMN model_version VARCHAR(50);
ALTER TABLE image_analysis ADD COLUMN processing_time INT;
ALTER TABLE image_analysis ADD COLUMN raw_result TEXT;
ALTER TABLE image_analysis ADD COLUMN is_manual_reviewed TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE image_analysis ADD COLUMN reviewer_id BIGINT;
ALTER TABLE image_analysis ADD COLUMN review_result TINYINT;
ALTER TABLE image_analysis ADD COLUMN review_remark VARCHAR(500);
ALTER TABLE image_analysis ADD COLUMN is_used_for_training TINYINT(1) NOT NULL DEFAULT 0;

-- 添加image_analysis表索引
CREATE INDEX IDX_image_analysis_analysis_type ON image_analysis (analysis_type);