-- =========================================
-- 为 users 表添加 department（学院/系）、
-- major（专业）和 grade（年级）字段
-- =========================================

ALTER TABLE users ADD COLUMN department VARCHAR(100) DEFAULT NULL COMMENT '学院/系' AFTER school_name;
ALTER TABLE users ADD COLUMN major VARCHAR(100) DEFAULT NULL COMMENT '专业' AFTER department;
ALTER TABLE users ADD COLUMN grade VARCHAR(50) DEFAULT NULL COMMENT '年级' AFTER major;
