-- =========================================
-- 修复 verification_records 表 NOT NULL 约束
-- 原因：student_id / student_card 仅学生证
-- 认证时需要，身份证/教师证认证时无需填
-- 写，应允许 NULL
-- =========================================

ALTER TABLE verification_records MODIFY COLUMN student_id VARCHAR(50) DEFAULT NULL COMMENT '学号';
ALTER TABLE verification_records MODIFY COLUMN student_card VARCHAR(512) DEFAULT NULL COMMENT '学生证照片URL';
