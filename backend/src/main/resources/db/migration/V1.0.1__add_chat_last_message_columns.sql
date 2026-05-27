ALTER TABLE chats
    ADD COLUMN last_message VARCHAR(500) NULL COMMENT '最后一条消息内容' AFTER updated_at;
