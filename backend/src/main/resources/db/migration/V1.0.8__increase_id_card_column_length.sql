-- =========================================
-- 扩展 verification_records 表字段长度
-- 原因：
--   1. id_card: 身份证号需 AES-256-GCM 加密后存储，
--      加密后的 Base64 约 44 字符，原 18 位不够
--   2. 图片字段: 原 VARCHAR(255) 对于图片 URL 够用，
--      但安全起见统一扩展到 512
-- =========================================

ALTER TABLE verification_records MODIFY COLUMN id_card VARCHAR(512) DEFAULT NULL COMMENT '身份证号（加密存储）';
ALTER TABLE verification_records MODIFY COLUMN id_card_front VARCHAR(512) DEFAULT NULL COMMENT '身份证正面照片URL';
ALTER TABLE verification_records MODIFY COLUMN id_card_back VARCHAR(512) DEFAULT NULL COMMENT '身份证反面照片URL';
ALTER TABLE verification_records MODIFY COLUMN student_card VARCHAR(512) DEFAULT NULL COMMENT '学生证照片URL';
ALTER TABLE verification_records MODIFY COLUMN teacher_card VARCHAR(512) DEFAULT NULL COMMENT '教师证照片URL';
