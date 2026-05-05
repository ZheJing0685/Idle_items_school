-- 数据备份
DROP TABLE IF EXISTS categories_backup;
CREATE TABLE categories_backup LIKE categories;
INSERT INTO categories_backup SELECT * FROM categories;

-- 删除已存在的新表
DROP TABLE IF EXISTS categories_new;

-- 创建新表
CREATE TABLE `categories_new` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `parent_id` BIGINT NULL COMMENT '父分类ID，NULL表示一级分类',
  `level` TINYINT NOT NULL DEFAULT 1 COMMENT '分类级别',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序权重',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `description` VARCHAR(500) NULL COMMENT '分类描述',
  `icon` VARCHAR(255) NULL COMMENT '分类图标',
  `image` VARCHAR(255) NULL COMMENT '分类图片',
  `keywords` VARCHAR(255) NULL COMMENT '关键词',
  `meta_description` VARCHAR(500) NULL COMMENT 'SEO描述',
  `path` VARCHAR(500) NULL COMMENT '分类路径',
  `background_color` VARCHAR(20) NULL COMMENT '背景色',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` BIGINT NULL COMMENT '创建人',
  `updated_by` BIGINT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  INDEX `idx_parent_id` (`parent_id`),
  INDEX `idx_level` (`level`),
  INDEX `idx_sort` (`sort`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';

-- 导入数据
INSERT INTO categories_new (
  id, name, parent_id, level, sort, status, 
  created_at, updated_at
) SELECT 
  id, name, parent_id, level, sort, 1, 
  created_at, updated_at
FROM categories;

-- 禁用外键约束
SET FOREIGN_KEY_CHECKS = 0;

-- 删除已存在的旧表
DROP TABLE IF EXISTS categories_old;

-- 重命名表
RENAME TABLE categories TO categories_old, categories_new TO categories;

-- 启用外键约束
SET FOREIGN_KEY_CHECKS = 1;

-- 验证数据
SELECT COUNT(*) FROM categories;
SELECT COUNT(*) FROM categories_old;