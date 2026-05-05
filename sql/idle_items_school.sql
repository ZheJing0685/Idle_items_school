/*
 Navicat Premium Dump SQL

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80045 (8.0.45)
 Source Host           : localhost:3306
 Source Schema         : idle_items_school

 Target Server Type    : MySQL
 Target Server Version : 80045 (8.0.45)
 File Encoding         : 65001

 Date: 21/04/2026 16:33:57
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin_logs
-- ----------------------------
DROP TABLE IF EXISTS `admin_logs`;
CREATE TABLE `admin_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint NULL DEFAULT NULL,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `details` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `ip_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `operation` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `target_id` bigint NULL DEFAULT NULL,
  `target_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `user_agent` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  `log_type` tinyint NOT NULL DEFAULT 1,
  `log_level` tinyint NOT NULL DEFAULT 1,
  `request_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `request_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `response_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `execution_time` int NULL DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `error_message` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `stack_trace` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `IDX_admin_logs_log_type_log_level`(`log_type` ASC, `log_level` ASC) USING BTREE,
  INDEX `IDX_admin_logs_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of admin_logs
-- ----------------------------
INSERT INTO `admin_logs` VALUES (1, 1, '2025-10-15 08:00:00.000000', '{\"username\":\"admin\",\"result\":\"success\"}', '127.0.0.1', '登录系统', 1, 'USER', 'Mozilla/5.0', 0, 1, 1, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL);
INSERT INTO `admin_logs` VALUES (2, 1, '2025-10-15 09:30:00.000000', '{\"itemId\":1,\"title\":\"iPhone 14 Pro Max\",\"result\":\"approved\"}', '127.0.0.1', '审核通过物品', 1, 'ITEM', 'Mozilla/5.0', 0, 1, 1, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL);
INSERT INTO `admin_logs` VALUES (3, 1, '2025-10-16 10:00:00.000000', '{\"userId\":6,\"username\":\"sunqi\",\"action\":\"disabled\"}', '127.0.0.1', '禁用用户', 6, 'USER', 'Mozilla/5.0', 0, 1, 1, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL);
INSERT INTO `admin_logs` VALUES (4, 1, '2025-10-16 11:20:00.000000', '{\"itemId\":2,\"title\":\"小米笔记本Pro 15\",\"result\":\"approved\"}', '127.0.0.1', '审核通过物品', 2, 'ITEM', 'Mozilla/5.0', 0, 1, 1, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL);
INSERT INTO `admin_logs` VALUES (5, 1, '2025-10-17 14:00:00.000000', '{\"itemId\":4,\"title\":\"索尼WH-1000XM4\",\"result\":\"approved\"}', '127.0.0.1', '审核通过物品', 4, 'ITEM', 'Mozilla/5.0', 0, 1, 1, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL);
INSERT INTO `admin_logs` VALUES (6, 1, '2025-10-18 09:15:00.000000', '{\"userId\":8,\"username\":\"wujiu\",\"action\":\"password_reset\"}', '127.0.0.1', '重置用户密码', 8, 'USER', 'Mozilla/5.0', 0, 1, 1, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL);
INSERT INTO `admin_logs` VALUES (7, 1, '2025-10-18 10:30:00.000000', '{\"itemId\":7,\"title\":\"芙丽芳丝洗面奶\",\"result\":\"approved\"}', '127.0.0.1', '审核通过物品', 7, 'ITEM', 'Mozilla/5.0', 0, 1, 1, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL);
INSERT INTO `admin_logs` VALUES (8, 1, '2025-10-19 08:45:00.000000', '{\"page\":\"dashboard\",\"action\":\"view\"}', '127.0.0.1', '查看系统统计', NULL, 'SYSTEM', 'Mozilla/5.0', 0, 1, 1, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL);
INSERT INTO `admin_logs` VALUES (9, 1, NULL, '{\"recordId\":6,\"realName\":\"陈一\",\"userId\":10}', '0:0:0:0:0:0:0:1', '通过实名认证', 6, 'VERIFICATION', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.107.1 Chrome/142.0.7444.235 Electron/39.2.7 Safari/537.36', 0, 1, 1, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL);
INSERT INTO `admin_logs` VALUES (10, 1, NULL, '{\"categoryName\":\"数码产品\",\"categoryId\":1}', '0:0:0:0:0:0:0:1', '更新分类', 1, 'CATEGORY', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36 Edg/147.0.0.0', 0, 1, 1, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL);
INSERT INTO `admin_logs` VALUES (11, 1, NULL, '{\"categoryName\":\"数码产品\",\"categoryId\":1}', '0:0:0:0:0:0:0:1', '更新分类', 1, 'CATEGORY', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36 Edg/147.0.0.0', 0, 1, 1, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL);
INSERT INTO `admin_logs` VALUES (12, 1, NULL, '{\"orderNo\":\"202510160001\",\"orderId\":2,\"status\":\"REFUNDED\"}', '0:0:0:0:0:0:0:1', '审批退款', 2, 'ORDER', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36', 0, 1, 1, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL);
INSERT INTO `admin_logs` VALUES (13, 1, NULL, '{\"orderNo\":\"202510160001\",\"orderId\":2,\"status\":\"REFUNDED\"}', '0:0:0:0:0:0:0:1', '审批退款', 2, 'ORDER', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36', 0, 1, 1, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL);
INSERT INTO `admin_logs` VALUES (14, 1, NULL, '{\"orderNo\":\"202510160001\",\"orderId\":2,\"status\":\"REFUNDED\"}', '0:0:0:0:0:0:0:1', '审批退款', 2, 'ORDER', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36', 0, 1, 1, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL);
INSERT INTO `admin_logs` VALUES (15, 1, NULL, '{\"orderNo\":\"202510170001\",\"orderId\":3,\"status\":\"REFUNDED\"}', '0:0:0:0:0:0:0:1', '审批退款', 3, 'ORDER', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36', 0, 1, 1, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL);
INSERT INTO `admin_logs` VALUES (16, 1, NULL, '{\"orderNo\":\"202510180001\",\"orderId\":4,\"status\":\"REFUNDED\"}', '0:0:0:0:0:0:0:1', '审批退款', 4, 'ORDER', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36', 0, 1, 1, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL);
INSERT INTO `admin_logs` VALUES (17, 1, NULL, '{\"recordId\":7,\"realName\":\"王浩\",\"reason\":\"未选择认证类型。\",\"userId\":14}', '0:0:0:0:0:0:0:1', '拒绝实名认证', 7, 'VERIFICATION', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36 Edg/147.0.0.0', 0, 1, 1, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL);
INSERT INTO `admin_logs` VALUES (18, 1, NULL, '{\"recordId\":8,\"realName\":\"王浩\",\"userId\":14}', '0:0:0:0:0:0:0:1', '通过实名认证', 8, 'VERIFICATION', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36 Edg/147.0.0.0', 0, 1, 1, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL);

-- ----------------------------
-- Table structure for categories
-- ----------------------------
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类主键ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称，如\"数码产品\"、\"书籍教材\"',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父分类ID，NULL表示一级分类',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序序号，数值越小越靠前',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类图标URL地址',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分类创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '分类更新时间',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  `create_by` bigint NULL DEFAULT NULL,
  `update_by` bigint NULL DEFAULT NULL,
  `level` tinyint NOT NULL DEFAULT 1,
  `path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `is_show` tinyint(1) NOT NULL DEFAULT 1,
  `item_count` int NOT NULL DEFAULT 0,
  `keywords` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `meta_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `background_color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_sort_order`(`sort_order` ASC) USING BTREE,
  INDEX `IDX_categories_level_sort_order`(`level` ASC, `sort_order` ASC) USING BTREE,
  INDEX `IDX_categories_is_show`(`is_show` ASC) USING BTREE,
  CONSTRAINT `categories_ibfk_1` FOREIGN KEY (`parent_id`) REFERENCES `categories` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 47 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '物品分类表：管理闲置物品的分类体系，支持二级分类' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of categories
-- ----------------------------
INSERT INTO `categories` VALUES (1, '数码产品', NULL, 1, '/icons/digital.png', '2025-09-01 00:00:00', '2026-04-18 20:00:11', NULL, 0, NULL, NULL, 1, NULL, 1, 0, NULL, NULL, NULL, NULL);
INSERT INTO `categories` VALUES (2, '书籍教材', NULL, 2, '/icons/book.png', '2025-09-01 00:00:00', '2025-09-01 00:00:00', NULL, 0, NULL, NULL, 1, NULL, 1, 0, NULL, NULL, NULL, NULL);
INSERT INTO `categories` VALUES (3, '服饰鞋包', NULL, 3, '/icons/clothing.png', '2025-09-01 00:00:00', '2025-09-01 00:00:00', NULL, 0, NULL, NULL, 1, NULL, 1, 0, NULL, NULL, NULL, NULL);
INSERT INTO `categories` VALUES (4, '生活用品', NULL, 4, '/icons/living.png', '2025-09-01 00:00:00', '2025-09-01 00:00:00', NULL, 0, NULL, NULL, 1, NULL, 1, 0, NULL, NULL, NULL, NULL);
INSERT INTO `categories` VALUES (5, '运动户外', NULL, 5, '/icons/sports.png', '2025-09-01 00:00:00', '2025-09-01 00:00:00', NULL, 0, NULL, NULL, 1, NULL, 1, 0, NULL, NULL, NULL, NULL);
INSERT INTO `categories` VALUES (6, '虚拟物品', NULL, 6, '/icons/virtual.png', '2025-09-01 00:00:00', '2025-09-01 00:00:00', NULL, 0, NULL, NULL, 1, NULL, 1, 0, NULL, NULL, NULL, NULL);
INSERT INTO `categories` VALUES (7, '其他', NULL, 7, '/icons/other.png', '2025-09-01 00:00:00', '2025-09-01 00:00:00', NULL, 0, NULL, NULL, 1, NULL, 1, 0, NULL, NULL, NULL, NULL);
INSERT INTO `categories` VALUES (8, '手机', 1, 1, '/icons/phone.png', '2025-09-01 00:00:00', '2025-09-01 00:00:00', NULL, 0, NULL, NULL, 1, NULL, 1, 0, NULL, NULL, NULL, NULL);
INSERT INTO `categories` VALUES (9, '电脑/平板', 1, 2, '/icons/computer.png', '2025-09-01 00:00:00', '2025-09-01 00:00:00', NULL, 0, NULL, NULL, 1, NULL, 1, 0, NULL, NULL, NULL, NULL);
INSERT INTO `categories` VALUES (10, '耳机/音响', 1, 3, '/icons/audio.png', '2025-09-01 00:00:00', '2025-09-01 00:00:00', NULL, 0, NULL, NULL, 1, NULL, 1, 0, NULL, NULL, NULL, NULL);
INSERT INTO `categories` VALUES (11, '相机', 1, 4, '/icons/camera.png', '2025-09-01 00:00:00', '2025-09-01 00:00:00', NULL, 0, NULL, NULL, 1, NULL, 1, 0, NULL, NULL, NULL, NULL);
INSERT INTO `categories` VALUES (12, '教材教辅', 2, 1, '/icons/textbook.png', '2025-09-01 00:00:00', '2025-09-01 00:00:00', NULL, 0, NULL, NULL, 1, NULL, 1, 0, NULL, NULL, NULL, NULL);
INSERT INTO `categories` VALUES (13, '小说文学', 2, 2, '/icons/novel.png', '2025-09-01 00:00:00', '2025-09-01 00:00:00', NULL, 0, NULL, NULL, 1, NULL, 1, 0, NULL, NULL, NULL, NULL);
INSERT INTO `categories` VALUES (14, '专业书籍', 2, 3, '/icons/professional.png', '2025-09-01 00:00:00', '2025-09-01 00:00:00', NULL, 0, NULL, NULL, 1, NULL, 1, 0, NULL, NULL, NULL, NULL);
INSERT INTO `categories` VALUES (15, '男装', 3, 1, '/icons/male.png', '2025-09-01 00:00:00', '2025-09-01 00:00:00', NULL, 0, NULL, NULL, 1, NULL, 1, 0, NULL, NULL, NULL, NULL);
INSERT INTO `categories` VALUES (16, '女装', 3, 2, '/icons/female.png', '2025-09-01 00:00:00', '2025-09-01 00:00:00', NULL, 0, NULL, NULL, 1, NULL, 1, 0, NULL, NULL, NULL, NULL);
INSERT INTO `categories` VALUES (17, '鞋类', 3, 3, '/icons/shoes.png', '2025-09-01 00:00:00', '2025-09-01 00:00:00', NULL, 0, NULL, NULL, 1, NULL, 1, 0, NULL, NULL, NULL, NULL);
INSERT INTO `categories` VALUES (18, '箱包', 3, 4, '/icons/bag.png', '2025-09-01 00:00:00', '2025-09-01 00:00:00', NULL, 0, NULL, NULL, 1, NULL, 1, 0, NULL, NULL, NULL, NULL);
INSERT INTO `categories` VALUES (19, '护肤品', 4, 1, '/icons/skincare.png', '2025-09-01 00:00:00', '2025-09-01 00:00:00', NULL, 0, NULL, NULL, 1, NULL, 1, 0, NULL, NULL, NULL, NULL);
INSERT INTO `categories` VALUES (20, '清洁用品', 4, 2, '/icons/cleaning.png', '2025-09-01 00:00:00', '2025-09-01 00:00:00', NULL, 0, NULL, NULL, 1, NULL, 1, 0, NULL, NULL, NULL, NULL);
INSERT INTO `categories` VALUES (21, '床上用品', 4, 3, '/icons/bedding.png', '2025-09-01 00:00:00', '2025-09-01 00:00:00', NULL, 0, NULL, NULL, 1, NULL, 1, 0, NULL, NULL, NULL, NULL);
INSERT INTO `categories` VALUES (22, '文具办公', 4, 4, '/icons/stationery.png', '2025-09-01 00:00:00', '2025-09-01 00:00:00', NULL, 0, NULL, NULL, 1, NULL, 1, 0, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for chat_messages
-- ----------------------------
DROP TABLE IF EXISTS `chat_messages`;
CREATE TABLE `chat_messages`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息主键ID',
  `chat_id` bigint NOT NULL COMMENT '所属会话ID，外键关联chats表',
  `sender_id` bigint NOT NULL COMMENT '发送者用户ID，外键关联users表',
  `receiver_id` bigint NOT NULL COMMENT '接收者用户ID，外键关联users表',
  `message_type` enum('TEXT','IMAGE','SYSTEM') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'TEXT' COMMENT '消息类型：TEXT-文字消息，IMAGE-图片消息，SYSTEM-系统消息',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容，文字消息为文本，图片消息为URL',
  `is_anonymous` tinyint(1) NULL DEFAULT 0 COMMENT '是否匿名：TRUE-是，FALSE-否',
  `is_read` tinyint(1) NULL DEFAULT 0 COMMENT '是否已读：TRUE-已读，FALSE-未读',
  `read_at` datetime NULL DEFAULT NULL COMMENT '阅读时间',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消息发送时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  `create_by` bigint NULL DEFAULT NULL,
  `update_by` bigint NULL DEFAULT NULL,
  `message_status` tinyint NOT NULL DEFAULT 1,
  `send_time` datetime NULL DEFAULT NULL,
  `receive_time` datetime NULL DEFAULT NULL,
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `image_width` int NULL DEFAULT NULL,
  `image_height` int NULL DEFAULT NULL,
  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `file_size` bigint NULL DEFAULT NULL,
  `is_recalled` tinyint(1) NOT NULL DEFAULT 0,
  `recall_time` datetime NULL DEFAULT NULL,
  `is_deleted_by_sender` tinyint(1) NOT NULL DEFAULT 0,
  `is_deleted_by_receiver` tinyint(1) NOT NULL DEFAULT 0,
  `reply_to_message_id` bigint NULL DEFAULT NULL,
  `reply_to_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_chat_id`(`chat_id` ASC) USING BTREE,
  INDEX `idx_sender_id`(`sender_id` ASC) USING BTREE,
  INDEX `idx_receiver_id`(`receiver_id` ASC) USING BTREE,
  INDEX `idx_is_read`(`is_read` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `IDX_chat_messages_chat_id_send_time`(`chat_id` ASC, `send_time` ASC) USING BTREE,
  CONSTRAINT `chat_messages_ibfk_1` FOREIGN KEY (`chat_id`) REFERENCES `chats` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `chat_messages_ibfk_2` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `chat_messages_ibfk_3` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '聊天消息表：存储聊天会话中的详细消息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of chat_messages
-- ----------------------------
INSERT INTO `chat_messages` VALUES (1, 1, 3, 2, 'TEXT', '你好，请问手机还在吗？', 0, 1, '2025-10-14 10:32:00', '2025-10-14 10:30:00', 0, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 0, NULL, NULL);
INSERT INTO `chat_messages` VALUES (2, 1, 2, 3, 'TEXT', '在的，有什么问题可以问哦', 0, 1, '2025-10-14 10:35:00', '2025-10-14 10:33:00', 0, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 0, NULL, NULL);
INSERT INTO `chat_messages` VALUES (3, 1, 3, 2, 'TEXT', '电池健康度真的98%吗？能截图看看吗', 0, 1, '2025-10-14 10:40:00', '2025-10-14 10:38:00', 0, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 0, NULL, NULL);
INSERT INTO `chat_messages` VALUES (4, 1, 2, 3, 'IMAGE', 'https://picsum.photos/300/200?random=10', 0, 1, '2025-10-14 10:45:00', '2025-10-14 10:42:00', 0, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 0, NULL, NULL);
INSERT INTO `chat_messages` VALUES (5, 1, 3, 2, 'TEXT', '好的，我看了一下，确实不错。价格能少点吗？', 0, 1, '2025-10-14 11:00:00', '2025-10-14 10:55:00', 0, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 0, NULL, NULL);
INSERT INTO `chat_messages` VALUES (6, 1, 2, 3, 'TEXT', '最低价了，已经是九折了。可以当面交易验货', 0, 1, '2025-10-14 12:00:00', '2025-10-14 11:30:00', 0, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 0, NULL, NULL);
INSERT INTO `chat_messages` VALUES (7, 1, 3, 2, 'TEXT', '行，明天下午图书馆门口见', 0, 1, '2025-10-14 12:30:00', '2025-10-14 12:20:00', 0, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 0, NULL, NULL);
INSERT INTO `chat_messages` VALUES (8, 1, 2, 3, 'TEXT', '好的，到时候联系', 0, 1, '2025-10-14 13:00:00', '2025-10-14 12:35:00', 0, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 0, NULL, NULL);
INSERT INTO `chat_messages` VALUES (9, 2, 4, 3, 'TEXT', '学长好，请问笔记本性能怎么样？', 0, 1, '2025-10-15 09:20:00', '2025-10-15 09:15:00', 0, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 0, NULL, NULL);
INSERT INTO `chat_messages` VALUES (10, 2, 3, 4, 'TEXT', '挺好的，日常办公编程都没问题，偶尔打打游戏也行', 0, 1, '2025-10-15 09:45:00', '2025-10-15 09:30:00', 0, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 0, NULL, NULL);
INSERT INTO `chat_messages` VALUES (11, 3, 7, 4, 'TEXT', '耳机效果真的好吗？降噪明显吗？', 0, 1, '2025-10-17 14:25:00', '2025-10-17 14:20:00', 0, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 0, NULL, NULL);
INSERT INTO `chat_messages` VALUES (12, 3, 4, 7, 'TEXT', '效果很棒，戴上去基本听不到外界声音', 0, 1, '2025-10-17 14:50:00', '2025-10-17 14:35:00', 0, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 0, NULL, NULL);
INSERT INTO `chat_messages` VALUES (13, 4, 9, 2, 'TEXT', '学长，手机还能便宜吗？', 0, 0, NULL, '2025-10-18 11:30:00', 0, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 0, NULL, NULL);
INSERT INTO `chat_messages` VALUES (14, 4, 2, 9, 'TEXT', '抱歉，已经是最低价了', 0, 0, NULL, '2025-10-18 11:35:00', 0, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 0, NULL, NULL);
INSERT INTO `chat_messages` VALUES (15, 5, 10, 3, 'TEXT', '你好，AirPods还在吗？', 0, 1, '2025-10-19 09:05:00', '2025-10-19 09:00:00', 0, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 0, NULL, NULL);
INSERT INTO `chat_messages` VALUES (16, 5, 3, 10, 'TEXT', '在的，欢迎询问！', 0, 1, '2025-10-19 09:15:00', '2025-10-19 09:10:00', 0, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 0, NULL, NULL);
INSERT INTO `chat_messages` VALUES (17, 5, 10, 3, 'TEXT', '音质怎么样？降噪效果好吗？', 0, 0, NULL, '2025-10-19 09:30:00', 0, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, 0, NULL, NULL);

-- ----------------------------
-- Table structure for chats
-- ----------------------------
DROP TABLE IF EXISTS `chats`;
CREATE TABLE `chats`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会话主键ID',
  `order_id` bigint NULL DEFAULT NULL COMMENT '关联订单ID，可为空，外键关联orders表',
  `item_id` bigint NULL DEFAULT NULL COMMENT '关联物品ID，可为空，外键关联items表',
  `buyer_id` bigint NOT NULL COMMENT '买家用户ID，外键关联users表',
  `seller_id` bigint NOT NULL COMMENT '卖家用户ID，外键关联users表',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '会话创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '会话最后更新时间（有新消息时更新）',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  `create_by` bigint NULL DEFAULT NULL,
  `update_by` bigint NULL DEFAULT NULL,
  `last_message_id` bigint NULL DEFAULT NULL,
  `last_message_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `last_message_time` datetime NULL DEFAULT NULL,
  `last_message_sender_id` bigint NULL DEFAULT NULL,
  `buyer_unread_count` int NOT NULL DEFAULT 0,
  `seller_unread_count` int NOT NULL DEFAULT 0,
  `is_blocked` tinyint(1) NOT NULL DEFAULT 0,
  `blocked_by` bigint NULL DEFAULT NULL,
  `blocked_time` datetime NULL DEFAULT NULL,
  `is_muted` tinyint(1) NOT NULL DEFAULT 0,
  `muted_by` bigint NULL DEFAULT NULL,
  `chat_status` tinyint NOT NULL DEFAULT 1,
  `close_time` datetime NULL DEFAULT NULL,
  `close_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_buyer_seller_item`(`buyer_id` ASC, `seller_id` ASC, `item_id` ASC) USING BTREE,
  UNIQUE INDEX `UKa4fqri43jn5r3qy93xlueh64k`(`buyer_id` ASC, `seller_id` ASC, `item_id` ASC) USING BTREE,
  INDEX `order_id`(`order_id` ASC) USING BTREE,
  INDEX `item_id`(`item_id` ASC) USING BTREE,
  INDEX `idx_buyer_id`(`buyer_id` ASC) USING BTREE,
  INDEX `idx_seller_id`(`seller_id` ASC) USING BTREE,
  INDEX `idx_updated_at`(`updated_at` ASC) USING BTREE,
  INDEX `IDX_chats_last_message_time`(`last_message_time` ASC) USING BTREE,
  INDEX `IDX_chats_chat_status_is_blocked`(`chat_status` ASC, `is_blocked` ASC) USING BTREE,
  CONSTRAINT `chats_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `chats_ibfk_2` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `chats_ibfk_3` FOREIGN KEY (`buyer_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `chats_ibfk_4` FOREIGN KEY (`seller_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '聊天会话表：管理买家和卖家之间的聊天会话' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of chats
-- ----------------------------
INSERT INTO `chats` VALUES (1, 1, 1, 3, 2, '2025-10-14 10:30:00', '2025-10-15 14:25:00', 0, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0, NULL, NULL, 0, NULL, 1, NULL, NULL);
INSERT INTO `chats` VALUES (2, 2, 2, 4, 3, '2025-10-15 09:15:00', '2025-10-16 10:15:00', 0, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0, NULL, NULL, 0, NULL, 1, NULL, NULL);
INSERT INTO `chats` VALUES (3, NULL, 4, 7, 4, '2025-10-17 14:20:00', '2025-10-17 14:50:00', 0, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0, NULL, NULL, 0, NULL, 1, NULL, NULL);
INSERT INTO `chats` VALUES (4, NULL, 1, 9, 2, '2025-10-18 11:30:00', '2025-10-18 11:35:00', 0, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0, NULL, NULL, 0, NULL, 1, NULL, NULL);
INSERT INTO `chats` VALUES (5, NULL, 15, 10, 3, '2025-10-19 09:00:00', '2025-10-19 09:30:00', 0, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0, NULL, NULL, 0, NULL, 1, NULL, NULL);

-- ----------------------------
-- Table structure for disputes
-- ----------------------------
DROP TABLE IF EXISTS `disputes`;
CREATE TABLE `disputes`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '纠纷主键ID',
  `order_id` bigint NOT NULL COMMENT '关联订单ID，外键关联orders表',
  `applicant_id` bigint NOT NULL COMMENT '申请人用户ID，发起纠纷的用户',
  `respondent_id` bigint NOT NULL COMMENT '被申请人用户ID，纠纷对方',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '纠纷原因简述',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '纠纷详细描述',
  `evidence_images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '证据图片数组，JSON格式存储',
  `dispute_status` enum('PENDING','PROCESSING','RESOLVED','CLOSED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'PENDING' COMMENT '纠纷状态：PENDING-待处理，PROCESSING-处理中，RESOLVED-已解决，CLOSED-已关闭',
  `handler_id` bigint NULL DEFAULT NULL COMMENT '处理人管理员ID，外键关联users表',
  `result` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '处理结果说明',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '纠纷申请时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '纠纷更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  `create_by` bigint NULL DEFAULT NULL,
  `update_by` bigint NULL DEFAULT NULL,
  `dispute_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `dispute_type` tinyint NOT NULL DEFAULT 1,
  `expect_result` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `expect_refund_amount` decimal(10, 2) NULL DEFAULT NULL,
  `actual_refund_amount` decimal(10, 2) NULL DEFAULT NULL,
  `process_remark` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `process_logs` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `is_urgent` tinyint(1) NOT NULL DEFAULT 0,
  `priority` tinyint NOT NULL DEFAULT 1,
  `assign_time` datetime NULL DEFAULT NULL,
  `start_process_time` datetime NULL DEFAULT NULL,
  `complete_time` datetime NULL DEFAULT NULL,
  `close_time` datetime NULL DEFAULT NULL,
  `close_type` tinyint NULL DEFAULT NULL,
  `is_escalated` tinyint(1) NOT NULL DEFAULT 0,
  `escalated_to` bigint NULL DEFAULT NULL,
  `escalated_time` datetime NULL DEFAULT NULL,
  `escalated_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `satisfaction` tinyint NULL DEFAULT NULL,
  `satisfaction_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_disputes_dispute_no`(`dispute_no` ASC) USING BTREE,
  INDEX `respondent_id`(`respondent_id` ASC) USING BTREE,
  INDEX `handler_id`(`handler_id` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_applicant_id`(`applicant_id` ASC) USING BTREE,
  INDEX `idx_dispute_status`(`dispute_status` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `IDX_disputes_dispute_status_priority`(`dispute_status` ASC, `priority` ASC) USING BTREE,
  INDEX `IDX_disputes_assign_time_start_process_time`(`assign_time` ASC, `start_process_time` ASC) USING BTREE,
  CONSTRAINT `disputes_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `disputes_ibfk_2` FOREIGN KEY (`applicant_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `disputes_ibfk_3` FOREIGN KEY (`respondent_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `disputes_ibfk_4` FOREIGN KEY (`handler_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '纠纷处理表：记录和处理交易纠纷' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of disputes
-- ----------------------------
INSERT INTO `disputes` VALUES (1, 1, 3, 2, '商品与描述不符', '手机电池健康度只有85%，与卖家描述的98%严重不符。严重影响使用体验。', '[\"https://picsum.photos/400/300?random=d1\",\"https://picsum.photos/400/300?random=d2\"]', 'RESOLVED', 1, '经核实，买家提供证据显示电池健康度确实与描述不符。卖家承认失误，同意退款500元作为补偿。双方达成和解。', '2025-10-18 18:00:00', '2025-10-20 15:30:00', 0, NULL, NULL, '', 1, NULL, NULL, NULL, NULL, NULL, 0, 1, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for favorites
-- ----------------------------
DROP TABLE IF EXISTS `favorites`;
CREATE TABLE `favorites`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏记录主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID，外键关联users表',
  `item_id` bigint NOT NULL COMMENT '物品ID，外键关联items表',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  `category_id` bigint NULL DEFAULT NULL,
  `price_snapshot` decimal(10, 2) NULL DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `notify_when_price_drop` tinyint(1) NOT NULL DEFAULT 0,
  `target_price` decimal(10, 2) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_item`(`user_id` ASC, `item_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_item_id`(`item_id` ASC) USING BTREE,
  CONSTRAINT `favorites_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `favorites_ibfk_2` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '收藏表：记录用户收藏的物品' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of favorites
-- ----------------------------
INSERT INTO `favorites` VALUES (1, 3, 4, '2025-10-05 10:00:00', 0, NULL, NULL, NULL, 0, NULL);
INSERT INTO `favorites` VALUES (2, 3, 7, '2025-10-08 14:30:00', 0, NULL, NULL, NULL, 0, NULL);
INSERT INTO `favorites` VALUES (3, 4, 1, '2025-10-10 09:20:00', 0, NULL, NULL, NULL, 0, NULL);
INSERT INTO `favorites` VALUES (4, 4, 11, '2025-10-12 16:45:00', 0, NULL, NULL, NULL, 0, NULL);
INSERT INTO `favorites` VALUES (5, 5, 4, '2025-10-14 11:30:00', 0, NULL, NULL, NULL, 0, NULL);
INSERT INTO `favorites` VALUES (6, 7, 10, '2025-10-16 08:15:00', 0, NULL, NULL, NULL, 0, NULL);
INSERT INTO `favorites` VALUES (7, 9, 1, '2025-10-17 15:20:00', 0, NULL, NULL, NULL, 0, NULL);
INSERT INTO `favorites` VALUES (8, 10, 6, '2025-10-18 10:40:00', 0, NULL, NULL, NULL, 0, NULL);
INSERT INTO `favorites` VALUES (9, 5, 14, '2025-10-19 13:55:00', 0, NULL, NULL, NULL, 0, NULL);
INSERT INTO `favorites` VALUES (10, 7, 15, '2025-10-19 16:30:00', 0, NULL, NULL, NULL, 0, NULL);

-- ----------------------------
-- Table structure for flyway_schema_history
-- ----------------------------
DROP TABLE IF EXISTS `flyway_schema_history`;
CREATE TABLE `flyway_schema_history`  (
  `installed_rank` int NOT NULL,
  `version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `script` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `checksum` int NULL DEFAULT NULL,
  `installed_by` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`) USING BTREE,
  INDEX `flyway_schema_history_s_idx`(`success` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of flyway_schema_history
-- ----------------------------
INSERT INTO `flyway_schema_history` VALUES (1, '1', '<< Flyway Baseline >>', 'BASELINE', '<< Flyway Baseline >>', NULL, 'root', '2026-04-20 21:20:44', 0, 1);
INSERT INTO `flyway_schema_history` VALUES (2, '2', 'add core fields', 'SQL', 'V2__add_core_fields.sql', -1010632978, 'root', '2026-04-20 21:20:46', 2099, 1);
INSERT INTO `flyway_schema_history` VALUES (3, '3', 'add optional fields', 'SQL', 'V3__add_optional_fields.sql', -2106125660, 'root', '2026-04-20 21:20:50', 4084, 1);
INSERT INTO `flyway_schema_history` VALUES (4, '4', 'add new tables', 'SQL', 'V4__add_new_tables.sql', -1931837753, 'root', '2026-04-20 21:20:51', 174, 1);
INSERT INTO `flyway_schema_history` VALUES (5, '5', 'data initialization', 'SQL', 'V5__data_initialization.sql', 972160794, 'root', '2026-04-20 21:20:51', 59, 1);

-- ----------------------------
-- Table structure for image_analysis
-- ----------------------------
DROP TABLE IF EXISTS `image_analysis`;
CREATE TABLE `image_analysis`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分析记录主键ID',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '被分析的图片URL',
  `item_id` bigint NULL DEFAULT NULL COMMENT '关联物品ID，外键关联items表',
  `analysis_result` json NULL COMMENT '完整分析结果，JSON格式存储',
  `item_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '识别的物品类型，如\"手机\"、\"笔记本\"、\"书籍\"',
  `brand` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '识别的品牌，如\"Apple\"、\"华为\"、\"小米\"',
  `color` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '识别的颜色',
  `confidence` decimal(5, 2) NULL DEFAULT NULL COMMENT '识别置信度，0-100，数值越大越准确',
  `analysis_status` enum('PENDING','SUCCESS','FAILED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'PENDING' COMMENT '分析状态：PENDING-待分析，SUCCESS-分析成功，FAILED-分析失败',
  `error_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分析失败时的错误信息',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分析创建时间',
  `status` enum('PENDING','SUCCESS','FAILED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  `create_by` bigint NULL DEFAULT NULL,
  `update_by` bigint NULL DEFAULT NULL,
  `updated_at` datetime NULL DEFAULT NULL,
  `analysis_type` tinyint NOT NULL DEFAULT 1,
  `model_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `processing_time` int NULL DEFAULT NULL,
  `raw_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `is_manual_reviewed` tinyint(1) NOT NULL DEFAULT 0,
  `reviewer_id` bigint NULL DEFAULT NULL,
  `review_result` tinyint NULL DEFAULT NULL,
  `review_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `is_used_for_training` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_item_id`(`item_id` ASC) USING BTREE,
  INDEX `idx_analysis_status`(`analysis_status` ASC) USING BTREE,
  INDEX `idx_confidence`(`confidence` ASC) USING BTREE,
  INDEX `IDX_image_analysis_analysis_type`(`analysis_type` ASC) USING BTREE,
  CONSTRAINT `image_analysis_ibfk_1` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '图片分析记录表：记录AI图像识别的分析结果' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of image_analysis
-- ----------------------------
INSERT INTO `image_analysis` VALUES (1, 'https://picsum.photos/800/600?random=101', 1, '{\"result\": \"success\", \"details\": \"iPhone 14 Pro Max, 256GB, Deep Purple\"}', '手机', 'Apple', '暗紫色', 98.50, 'SUCCESS', NULL, '2025-10-01 10:35:00', 'SUCCESS', 0, NULL, NULL, NULL, 1, NULL, NULL, NULL, 0, NULL, NULL, NULL, 0);
INSERT INTO `image_analysis` VALUES (2, 'https://picsum.photos/800/600?random=201', 2, '{\"result\": \"success\", \"details\": \"Xiaomi Laptop Pro 15, Intel i7, 16GB RAM\"}', '笔记本电脑', '小米', '灰色', 95.20, 'SUCCESS', NULL, '2025-10-02 14:25:00', 'SUCCESS', 0, NULL, NULL, NULL, 1, NULL, NULL, NULL, 0, NULL, NULL, NULL, 0);
INSERT INTO `image_analysis` VALUES (3, 'https://picsum.photos/800/600?random=401', 4, '{\"result\": \"success\", \"details\": \"Sony WH-1000XM4, Black, Noise Cancelling\"}', '耳机', '索尼', '黑色', 97.80, 'SUCCESS', NULL, '2025-10-04 16:50:00', 'SUCCESS', 0, NULL, NULL, NULL, 1, NULL, NULL, NULL, 0, NULL, NULL, NULL, 0);
INSERT INTO `image_analysis` VALUES (4, 'https://picsum.photos/800/600?random=601', 6, '{\"result\": \"success\", \"details\": \"Three-Body Problem trilogy, hard cover\"}', '书籍', NULL, NULL, 93.40, 'SUCCESS', NULL, '2025-10-06 08:35:00', 'SUCCESS', 0, NULL, NULL, NULL, 1, NULL, NULL, NULL, 0, NULL, NULL, NULL, 0);
INSERT INTO `image_analysis` VALUES (5, 'https://picsum.photos/800/600?random=501', 5, '{\"result\": \"success\", \"details\": \"UNIQLO collaboration hoodie, black, XL\"}', '服装', '优衣库', '黑色', 91.30, 'SUCCESS', NULL, '2025-10-05 11:25:00', 'SUCCESS', 0, NULL, NULL, NULL, 1, NULL, NULL, NULL, 0, NULL, NULL, NULL, 0);

-- ----------------------------
-- Table structure for item_images
-- ----------------------------
DROP TABLE IF EXISTS `item_images`;
CREATE TABLE `item_images`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '图片主键ID',
  `item_id` bigint NOT NULL COMMENT '关联物品ID，外键关联items表',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原图URL地址',
  `thumbnail_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '缩略图URL地址，用于列表展示',
  `is_cover` tinyint(1) NULL DEFAULT 0 COMMENT '是否封面图片：TRUE-是，FALSE-否，一个物品只有一个封面',
  `sort_order` int NULL DEFAULT 0 COMMENT '图片排序序号，数值越小越靠前',
  `width` int NULL DEFAULT NULL COMMENT '图片宽度，单位：像素',
  `height` int NULL DEFAULT NULL COMMENT '图片高度，单位：像素',
  `file_size` bigint NULL DEFAULT NULL COMMENT '文件大小，单位：字节',
  `format` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图片格式，如webp、jpg、png',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '图片上传时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  `create_by` bigint NULL DEFAULT NULL,
  `update_by` bigint NULL DEFAULT NULL,
  `updated_at` datetime NULL DEFAULT NULL,
  `image_hash` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `storage_type` tinyint NOT NULL DEFAULT 1,
  `storage_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `is_compressed` tinyint(1) NOT NULL DEFAULT 0,
  `is_watermarked` tinyint(1) NOT NULL DEFAULT 0,
  `ai_analysis_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `ai_analysis_status` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_item_id`(`item_id` ASC) USING BTREE,
  INDEX `idx_is_cover`(`is_cover` ASC) USING BTREE,
  INDEX `IDX_item_images_ai_analysis_status`(`ai_analysis_status` ASC) USING BTREE,
  CONSTRAINT `item_images_ibfk_1` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '物品图片表：存储物品的多张图片，支持封面设置和排序' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of item_images
-- ----------------------------
INSERT INTO `item_images` VALUES (1, 1, 'https://picsum.photos/800/600?random=101', 'https://picsum.photos/400/300?random=101', 1, 0, 800, 600, 245760, 'webp', '2025-10-01 10:30:00', 0, NULL, NULL, NULL, NULL, 1, NULL, 0, 0, NULL, 0);
INSERT INTO `item_images` VALUES (2, 1, 'https://picsum.photos/800/600?random=102', 'https://picsum.photos/400/300?random=102', 0, 1, 800, 600, 234567, 'webp', '2025-10-01 10:31:00', 0, NULL, NULL, NULL, NULL, 1, NULL, 0, 0, NULL, 0);
INSERT INTO `item_images` VALUES (3, 1, 'https://picsum.photos/800/600?random=103', 'https://picsum.photos/400/300?random=103', 0, 2, 800, 600, 198765, 'webp', '2025-10-01 10:32:00', 0, NULL, NULL, NULL, NULL, 1, NULL, 0, 0, NULL, 0);
INSERT INTO `item_images` VALUES (4, 2, 'https://picsum.photos/800/600?random=201', 'https://picsum.photos/400/300?random=201', 1, 0, 800, 600, 312456, 'webp', '2025-10-02 14:20:00', 0, NULL, NULL, NULL, NULL, 1, NULL, 0, 0, NULL, 0);
INSERT INTO `item_images` VALUES (5, 2, 'https://picsum.photos/800/600?random=202', 'https://picsum.photos/400/300?random=202', 0, 1, 800, 600, 287654, 'webp', '2025-10-02 14:21:00', 0, NULL, NULL, NULL, NULL, 1, NULL, 0, 0, NULL, 0);
INSERT INTO `item_images` VALUES (6, 3, 'https://picsum.photos/800/600?random=301', 'https://picsum.photos/400/300?random=301', 1, 0, 800, 600, 156789, 'webp', '2025-10-03 09:15:00', 0, NULL, NULL, NULL, NULL, 1, NULL, 0, 0, NULL, 0);
INSERT INTO `item_images` VALUES (7, 4, 'https://picsum.photos/800/600?random=401', 'https://picsum.photos/400/300?random=401', 1, 0, 800, 600, 278654, 'webp', '2025-10-04 16:45:00', 0, NULL, NULL, NULL, NULL, 1, NULL, 0, 0, NULL, 0);
INSERT INTO `item_images` VALUES (8, 4, 'https://picsum.photos/800/600?random=402', 'https://picsum.photos/400/300?random=402', 0, 1, 800, 600, 256789, 'webp', '2025-10-04 16:46:00', 0, NULL, NULL, NULL, NULL, 1, NULL, 0, 0, NULL, 0);
INSERT INTO `item_images` VALUES (9, 5, 'https://picsum.photos/800/600?random=501', 'https://picsum.photos/400/300?random=501', 1, 0, 800, 600, 134567, 'webp', '2025-10-05 11:20:00', 0, NULL, NULL, NULL, NULL, 1, NULL, 0, 0, NULL, 0);
INSERT INTO `item_images` VALUES (10, 6, 'https://picsum.photos/800/600?random=601', 'https://picsum.photos/400/300?random=601', 1, 0, 800, 600, 345678, 'webp', '2025-10-06 08:30:00', 0, NULL, NULL, NULL, NULL, 1, NULL, 0, 0, NULL, 0);

-- ----------------------------
-- Table structure for item_tags
-- ----------------------------
DROP TABLE IF EXISTS `item_tags`;
CREATE TABLE `item_tags`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '标签记录主键ID',
  `item_id` bigint NOT NULL COMMENT '关联物品ID，外键关联items表',
  `tag_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签名称，如\"急售\"、\"可小刀\"、\"自提\"',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '标签添加时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  `create_by` bigint NULL DEFAULT NULL,
  `tag_type` tinyint NOT NULL DEFAULT 1,
  `tag_category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `weight` int NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_item_tag`(`item_id` ASC, `tag_name` ASC) USING BTREE,
  UNIQUE INDEX `UKlvi6cdtksmbkjihkfsk37huqi`(`item_id` ASC, `tag_name` ASC) USING BTREE,
  INDEX `idx_tag_name`(`tag_name` ASC) USING BTREE,
  INDEX `IDX_item_tags_tag_name_tag_type`(`tag_name` ASC, `tag_type` ASC) USING BTREE,
  INDEX `IDX_item_tags_weight`(`weight` ASC) USING BTREE,
  CONSTRAINT `item_tags_ibfk_1` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '物品标签表：为物品添加标签，支持更灵活的分类和搜索' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of item_tags
-- ----------------------------
INSERT INTO `item_tags` VALUES (1, 1, '急售', '2025-10-01 10:30:00', 0, NULL, 1, NULL, 1);
INSERT INTO `item_tags` VALUES (2, 1, '可小刀', '2025-10-01 10:30:00', 0, NULL, 1, NULL, 1);
INSERT INTO `item_tags` VALUES (3, 2, '性价比高', '2025-10-02 14:20:00', 0, NULL, 1, NULL, 1);
INSERT INTO `item_tags` VALUES (4, 2, '办公利器', '2025-10-02 14:20:00', 0, NULL, 1, NULL, 1);
INSERT INTO `item_tags` VALUES (5, 3, '考研必备', '2025-10-03 09:15:00', 0, NULL, 1, NULL, 1);
INSERT INTO `item_tags` VALUES (6, 4, '降噪神器', '2025-10-04 16:45:00', 0, NULL, 1, NULL, 1);
INSERT INTO `item_tags` VALUES (7, 4, '音乐发烧友', '2025-10-04 16:45:00', 0, NULL, 1, NULL, 1);
INSERT INTO `item_tags` VALUES (8, 5, '百搭款', '2025-10-05 11:20:00', 0, NULL, 1, NULL, 1);
INSERT INTO `item_tags` VALUES (9, 7, '敏感肌可用', '2025-10-07 13:50:00', 0, NULL, 1, NULL, 1);
INSERT INTO `item_tags` VALUES (10, 9, '编程入门', '2025-10-09 15:25:00', 0, NULL, 1, NULL, 1);
INSERT INTO `item_tags` VALUES (11, 10, '经典款', '2025-10-10 09:40:00', 0, NULL, 1, NULL, 1);
INSERT INTO `item_tags` VALUES (12, 11, '学习必备', '2025-10-11 14:30:00', 0, NULL, 1, NULL, 1);
INSERT INTO `item_tags` VALUES (13, 14, '秋冬新款', '2025-10-14 16:15:00', 0, NULL, 1, NULL, 1);

-- ----------------------------
-- Table structure for items
-- ----------------------------
DROP TABLE IF EXISTS `items`;
CREATE TABLE `items`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '物品主键ID',
  `user_id` bigint NOT NULL COMMENT '发布者用户ID，外键关联users表',
  `category_id` bigint NOT NULL COMMENT '分类ID，外键关联categories表',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物品标题，用于展示和搜索',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '物品详细描述，支持富文本',
  `price` decimal(10, 2) NOT NULL COMMENT '出售价格，单位：元，保留两位小数',
  `original_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '原价，用于显示折扣信息',
  `item_condition` enum('NEW','LIKE_NEW','GOOD','FAIR','POOR') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'GOOD' COMMENT '新旧程度：NEW-全新，LIKE_NEW-几乎全新，GOOD-良好，FAIR-一般，POOR-较差',
  `item_status` enum('DRAFT','PENDING','ON_SALE','SOLD','OFF_SHELF','REJECTED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'PENDING' COMMENT '物品状态：DRAFT-草稿，PENDING-待审核，ON_SALE-在售，SOLD-已售，OFF_SHELF-下架，REJECTED-驳回',
  `view_count` int NULL DEFAULT 0 COMMENT '浏览次数，统计物品热度',
  `favorite_count` int NULL DEFAULT 0 COMMENT '收藏次数，统计物品受欢迎程度',
  `reject_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核驳回理由，状态为REJECTED时必填',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '交易地点，如\"图书馆门口\"、\"宿舍区\"',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `status` enum('DRAFT','PENDING','ON_SALE','SOLD','OFF_SHELF','REJECTED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  `create_by` bigint NULL DEFAULT NULL,
  `update_by` bigint NULL DEFAULT NULL,
  `publish_time` datetime NULL DEFAULT NULL,
  `off_shelf_time` datetime NULL DEFAULT NULL,
  `sold_time` datetime NULL DEFAULT NULL,
  `quality_score` decimal(3, 2) NULL DEFAULT NULL,
  `is_bargain_allowed` tinyint(1) NOT NULL DEFAULT 1,
  `min_price` decimal(10, 2) NULL DEFAULT NULL,
  `contact_type` tinyint NOT NULL DEFAULT 1,
  `contact_info` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `is_recommended` tinyint(1) NOT NULL DEFAULT 0,
  `recommend_time` datetime NULL DEFAULT NULL,
  `weight` int NOT NULL DEFAULT 0,
  `delivery_method` tinyint NOT NULL DEFAULT 1,
  `tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `brand` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `purchase_date` date NULL DEFAULT NULL,
  `warranty_info` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
  INDEX `idx_item_status`(`item_status` ASC) USING BTREE,
  INDEX `idx_price`(`price` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_view_count`(`view_count` ASC) USING BTREE,
  INDEX `idx_items_status`(`status` ASC) USING BTREE,
  INDEX `idx_items_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_items_category_id`(`category_id` ASC) USING BTREE,
  INDEX `idx_items_view_count`(`view_count` ASC) USING BTREE,
  INDEX `idx_items_user_id_status`(`user_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_items_category_id_status`(`category_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_items_status_view_count`(`status` ASC, `view_count` ASC) USING BTREE,
  INDEX `IDX_items_status_price`(`status` ASC, `price` ASC) USING BTREE,
  INDEX `IDX_items_publish_time`(`publish_time` ASC) USING BTREE,
  INDEX `IDX_items_is_recommended_recommend_time`(`is_recommended` ASC, `recommend_time` ASC) USING BTREE,
  INDEX `IDX_items_view_count_favorite_count`(`view_count` ASC, `favorite_count` ASC) USING BTREE,
  CONSTRAINT `items_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `items_ibfk_2` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '物品信息表：存储闲置物品的详细信息，支持发布、搜索和交易' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of items
-- ----------------------------
INSERT INTO `items` VALUES (1, 2, 8, 'iPhone 14 Pro Max 256G 暗紫色', '国行正品，2023年3月购买，电池健康度98%，无划痕无磕碰，配件齐全。屏幕一直贴膜保护，赠品众多。', 5999.00, 8999.00, 'LIKE_NEW', 'ON_SALE', 1264, 89, NULL, '学生宿舍楼下', '2025-10-01 10:30:00', '2026-04-21 16:29:25', 'ON_SALE', 0, NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, 1, NULL, 0, NULL, 0, 1, NULL, NULL, NULL, NULL);
INSERT INTO `items` VALUES (2, 3, 9, '小米笔记本Pro 15 2022款', 'i7-12650H处理器，16GB内存，512GB固态，MX550独立显卡。办公学习神器，轻薄便携。', 4599.00, 6499.00, 'GOOD', 'ON_SALE', 896, 45, NULL, '图书馆门口', '2025-10-02 14:20:00', '2026-04-19 19:59:10', 'ON_SALE', 0, NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, 1, NULL, 0, NULL, 0, 1, NULL, NULL, NULL, NULL);
INSERT INTO `items` VALUES (3, 2, 12, '高等数学同济第七版上下册', '大一购买，九成新，笔记较少，适合考研或期末复习。附赠习题解答指南。', 35.00, 68.00, 'GOOD', 'ON_SALE', 456, 23, NULL, '教学楼A座大厅', '2025-10-03 09:15:00', '2025-10-03 09:15:00', 'ON_SALE', 0, NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, 1, NULL, 0, NULL, 0, 1, NULL, NULL, NULL, NULL);
INSERT INTO `items` VALUES (4, 4, 10, '索尼WH-1000XM4降噪耳机', '黑色款，2024年6月购买。使用频率不高，配件全在。降噪效果顶级，音质出色。', 1299.00, 2299.00, 'LIKE_NEW', 'ON_SALE', 678, 56, NULL, '宿舍区南门', '2025-10-04 16:45:00', '2025-10-04 16:45:00', 'ON_SALE', 0, NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, 1, NULL, 0, NULL, 0, 1, NULL, NULL, NULL, NULL);
INSERT INTO `items` VALUES (5, 5, 15, '优衣库联名款卫衣 黑色 XL', '只穿过两次，尺码不合适所以出。质量很好，纯棉面料，宽松版型。', 89.00, 199.00, 'LIKE_NEW', 'ON_SALE', 234, 12, NULL, '学生超市门口', '2025-10-05 11:20:00', '2025-10-05 11:20:00', 'ON_SALE', 0, NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, 1, NULL, 0, NULL, 0, 1, NULL, NULL, NULL, NULL);
INSERT INTO `items` VALUES (6, 3, 13, '三体全套精装版 刘慈欣', '科幻经典，九成新，书脊完好，无折痕无涂写。藏书爱好者首选。', 68.00, 128.00, 'GOOD', 'ON_SALE', 347, 34, NULL, '图书馆东门', '2025-10-06 08:30:00', '2026-04-19 11:53:49', 'ON_SALE', 0, NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, 1, NULL, 0, NULL, 0, 1, NULL, NULL, NULL, NULL);
INSERT INTO `items` VALUES (7, 7, 19, '芙丽芳丝洗面奶100g', '日本进口，敏感肌可用。618囤货太多用不完，出两只。保质期到2026年底。', 89.00, 150.00, 'NEW', 'ON_SALE', 567, 78, NULL, '女生宿舍楼下', '2025-10-07 13:50:00', '2025-10-07 13:50:00', 'ON_SALE', 0, NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, 1, NULL, 0, NULL, 0, 1, NULL, NULL, NULL, NULL);
INSERT INTO `items` VALUES (8, 9, 5, '迪卡侬瑜伽垫加厚10mm', '蓝色，2024年9月购买。练了两个月膝盖不舒服暂停了。送瑜伽砖一对。', 45.00, 89.00, 'GOOD', 'ON_SALE', 125, 8, NULL, '体育馆门口', '2025-10-08 10:10:00', '2026-04-19 13:50:02', 'ON_SALE', 0, NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, 1, NULL, 0, NULL, 0, 1, NULL, NULL, NULL, NULL);
INSERT INTO `items` VALUES (9, 10, 14, 'Python编程从入门到实践', '计算机专业教材，九五新。内容涵盖Python基础、数据分析、Web开发。', 42.00, 79.00, 'LIKE_NEW', 'ON_SALE', 289, 19, NULL, '计算机楼201', '2025-10-09 15:25:00', '2025-10-09 15:25:00', 'ON_SALE', 0, NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, 1, NULL, 0, NULL, 0, 1, NULL, NULL, NULL, NULL);
INSERT INTO `items` VALUES (10, 5, 17, 'Nike Air Force 1 白色 42码', '2023年双十一购买，穿了大概二十次。刷洗后依然洁白如新，鞋盒配件齐全。', 399.00, 799.00, 'GOOD', 'ON_SALE', 446, 31, NULL, '菜鸟驿站旁', '2025-10-10 09:40:00', '2026-04-19 19:39:53', 'ON_SALE', 0, NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, 1, NULL, 0, NULL, 0, 1, NULL, NULL, NULL, NULL);
INSERT INTO `items` VALUES (11, 4, 9, 'iPad Air 5 64G WiFi版 星光色', '2024年教育优惠购入，一直贴膜戴壳使用。送Apple Pencil一代。', 3299.00, 4799.00, 'LIKE_NEW', 'ON_SALE', 789, 67, NULL, '行政楼102', '2025-10-11 14:30:00', '2025-10-11 14:30:00', 'ON_SALE', 0, NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, 1, NULL, 0, NULL, 0, 1, NULL, NULL, NULL, NULL);
INSERT INTO `items` VALUES (12, 7, 21, '小米手环8 NFC版', '黑色，买了两个月，功能正常。续航持久，抬腕即亮。', 199.00, 299.00, 'LIKE_NEW', 'ON_SALE', 234, 15, NULL, '学生宿舍楼下', '2025-10-12 10:20:00', '2025-10-12 10:20:00', 'ON_SALE', 0, NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, 1, NULL, 0, NULL, 0, 1, NULL, NULL, NULL, NULL);
INSERT INTO `items` VALUES (13, 2, 12, '线性代数同济第六版', '大二上学期购买，笔记较少。期末考试用的这本书。', 25.00, 45.00, 'GOOD', 'ON_SALE', 168, 9, NULL, '理学楼大厅', '2025-10-13 11:50:00', '2026-04-19 19:32:16', 'ON_SALE', 0, NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, 1, NULL, 0, NULL, 0, 1, NULL, NULL, NULL, NULL);
INSERT INTO `items` VALUES (14, 9, 16, 'ZARA女装针织开衫 M码 米白', '只试穿了一次，吊牌还在。秋冬季百搭款，面料柔软舒适。', 129.00, 299.00, 'NEW', 'ON_SALE', 178, 22, NULL, '女生宿舍区', '2025-10-14 16:15:00', '2025-10-14 16:15:00', 'ON_SALE', 0, NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, 1, NULL, 0, NULL, 0, 1, NULL, NULL, NULL, NULL);
INSERT INTO `items` VALUES (15, 3, 8, 'AirPods Pro 2代 带充电盒', '国行正品，2024年1月购买。降噪效果很好，音质清晰。配件全在。', 1299.00, 1899.00, 'GOOD', 'ON_SALE', 570, 43, NULL, '音乐厅门口', '2025-10-15 09:30:00', '2026-04-20 21:55:54', 'ON_SALE', 0, NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, 1, NULL, 0, NULL, 0, 1, NULL, NULL, NULL, NULL);
INSERT INTO `items` VALUES (16, 6, 11, '佳能EOS M50 微单相机', '2023年购买，套机镜头，拍人像风景都很不错。配有原装电池和充电器。', 2899.00, 4599.00, 'GOOD', 'ON_SALE', 346, 28, NULL, '摄影协会门口', '2025-10-16 11:20:00', '2026-04-19 16:10:59', 'ON_SALE', 0, NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, 1, NULL, 0, NULL, 0, 1, NULL, NULL, NULL, NULL);
INSERT INTO `items` VALUES (17, 8, 20, '戴森V12吸尘器', '2024年618购买，使用次数不超过10次。配件齐全，吸力强劲。', 2599.00, 3999.00, 'LIKE_NEW', 'ON_SALE', 416, 35, NULL, '教师公寓楼下', '2025-10-17 14:30:00', '2026-04-20 21:56:41', 'ON_SALE', 0, NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, 1, NULL, 0, NULL, 0, 1, NULL, NULL, NULL, NULL);
INSERT INTO `items` VALUES (18, 10, 13, '活着 余华 著', '经典文学作品，九五新。讲述了人生的苦难与希望。', 28.00, 45.00, 'LIKE_NEW', 'ON_SALE', 190, 15, NULL, '文学社活动室', '2025-10-18 09:15:00', '2026-04-18 10:47:24', 'ON_SALE', 0, NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, 1, NULL, 0, NULL, 0, 1, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for notifications
-- ----------------------------
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `notification_type` tinyint NOT NULL,
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `related_id` bigint NULL DEFAULT NULL,
  `related_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `is_read` tinyint(1) NOT NULL DEFAULT 0,
  `read_time` datetime NULL DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `IDX_notifications_user_id_is_read_created_at`(`user_id` ASC, `is_read` ASC, `created_at` ASC) USING BTREE,
  INDEX `IDX_notifications_user_id_notification_type`(`user_id` ASC, `notification_type` ASC) USING BTREE,
  CONSTRAINT `FK_notifications_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notifications
-- ----------------------------

-- ----------------------------
-- Table structure for operation_logs
-- ----------------------------
DROP TABLE IF EXISTS `operation_logs`;
CREATE TABLE `operation_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NULL DEFAULT NULL,
  `operation_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `operation_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `request_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `request_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `execution_time` int NULL DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `error_message` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `IDX_operation_logs_user_id_operation_type`(`user_id` ASC, `operation_type` ASC) USING BTREE,
  INDEX `IDX_operation_logs_created_at`(`created_at` ASC) USING BTREE,
  INDEX `IDX_operation_logs_status`(`status` ASC) USING BTREE,
  CONSTRAINT `FK_operation_logs_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of operation_logs
-- ----------------------------

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单主键ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号，唯一标识，格式：YYYYMMDDHHMMSS+随机数',
  `buyer_id` bigint NOT NULL COMMENT '买家用户ID，外键关联users表',
  `seller_id` bigint NOT NULL COMMENT '卖家用户ID，外键关联users表',
  `item_id` bigint NOT NULL COMMENT '物品ID，外键关联items表',
  `item_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '物品标题快照，防止物品信息变更影响历史订单',
  `item_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '物品图片快照',
  `price` decimal(10, 2) NOT NULL COMMENT '成交价格，单位：元',
  `order_status` enum('PENDING_PAYMENT','PAID','SHIPPED','DELIVERED','COMPLETED','CANCELLED','REFUND_REQUESTED','REFUNDED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'PENDING_PAYMENT' COMMENT '订单状态：PENDING_PAYMENT-待支付，PAID-已支付，SHIPPED-已发货，DELIVERED-已收货，COMPLETED-已完成，CANCELLED-已取消，REFUND_REQUESTED-退款申请中，REFUNDED-已退款',
  `buyer_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '收货地址',
  `buyer_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '买家联系电话',
  `buyer_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '买家姓名',
  `payment_method` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '支付方式，如\"微信支付\"、\"支付宝\"、\"线下交易\"',
  `payment_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `ship_time` datetime NULL DEFAULT NULL COMMENT '发货时间',
  `deliver_time` datetime NULL DEFAULT NULL COMMENT '收货时间',
  `complete_time` datetime NULL DEFAULT NULL COMMENT '订单完成时间',
  `cancel_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '取消原因',
  `refund_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '退款原因',
  `refund_time` datetime NULL DEFAULT NULL COMMENT '退款时间',
  `refund_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '退款金额',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '订单创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '订单更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  `create_by` bigint NULL DEFAULT NULL,
  `update_by` bigint NULL DEFAULT NULL,
  `total_amount` decimal(10, 2) NOT NULL DEFAULT 0.00,
  `shipping_fee` decimal(10, 2) NOT NULL DEFAULT 0.00,
  `discount_amount` decimal(10, 2) NOT NULL DEFAULT 0.00,
  `pay_amount` decimal(10, 2) NOT NULL DEFAULT 0.00,
  `transaction_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `payment_status` tinyint NOT NULL DEFAULT 0,
  `shipping_status` tinyint NOT NULL DEFAULT 0,
  `tracking_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `shipping_company` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `seller_note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `buyer_note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `auto_confirm_time` datetime NULL DEFAULT NULL,
  `close_time` datetime NULL DEFAULT NULL,
  `close_type` tinyint NULL DEFAULT NULL,
  `source` tinyint NOT NULL DEFAULT 1,
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_buyer_id`(`buyer_id` ASC) USING BTREE,
  INDEX `idx_seller_id`(`seller_id` ASC) USING BTREE,
  INDEX `idx_item_id`(`item_id` ASC) USING BTREE,
  INDEX `idx_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_order_status`(`order_status` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_orders_buyer_id`(`buyer_id` ASC) USING BTREE,
  INDEX `idx_orders_seller_id`(`seller_id` ASC) USING BTREE,
  INDEX `idx_orders_order_status`(`order_status` ASC) USING BTREE,
  INDEX `idx_orders_item_id`(`item_id` ASC) USING BTREE,
  INDEX `idx_orders_buyer_id_status`(`buyer_id` ASC, `order_status` ASC) USING BTREE,
  INDEX `idx_orders_seller_id_status`(`seller_id` ASC, `order_status` ASC) USING BTREE,
  INDEX `idx_orders_order_no`(`order_no` ASC) USING BTREE,
  INDEX `IDX_orders_buyer_id_order_status`(`buyer_id` ASC, `order_status` ASC) USING BTREE,
  INDEX `IDX_orders_seller_id_order_status`(`seller_id` ASC, `order_status` ASC) USING BTREE,
  INDEX `IDX_orders_payment_time`(`payment_time` ASC) USING BTREE,
  INDEX `IDX_orders_ship_time`(`ship_time` ASC) USING BTREE,
  INDEX `IDX_orders_deliver_time`(`deliver_time` ASC) USING BTREE,
  CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`buyer_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `orders_ibfk_2` FOREIGN KEY (`seller_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `orders_ibfk_3` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单表：管理交易订单，跟踪订单全生命周期' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES (1, '202510150001', 3, 2, 1, 'iPhone 14 Pro Max 256G 暗紫色', 'https://picsum.photos/400/300?random=1', 5999.00, 'COMPLETED', '男生宿舍楼3-201', '13800000003', '李四', '微信支付', '2025-10-15 14:35:00', '2025-10-15 15:00:00', '2025-10-17 10:00:00', '2025-10-17 16:20:00', NULL, NULL, NULL, NULL, '2025-10-15 14:30:00', '2025-10-17 16:20:00', 0, NULL, NULL, 0.00, 0.00, 0.00, 0.00, NULL, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL);
INSERT INTO `orders` VALUES (2, '202510160001', 4, 3, 2, '小米笔记本Pro 15 2022款', 'https://picsum.photos/400/300?random=2', 4599.00, 'REFUNDED', '计算机学院实验楼502', '13800000004', '王五', '支付宝', '2025-10-16 10:30:00', '2025-10-16 11:00:00', '2025-10-17 14:00:00', '2025-10-18 11:30:00', NULL, NULL, NULL, NULL, '2025-10-16 10:20:00', '2026-04-19 13:11:05', 0, NULL, NULL, 0.00, 0.00, 0.00, 0.00, NULL, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL);
INSERT INTO `orders` VALUES (3, '202510170001', 5, 2, 3, '高等数学同济第七版上下册', 'https://picsum.photos/400/300?random=3', 35.00, 'REFUNDED', '教学楼A座301', '13800000005', '赵六', '线下交易', NULL, NULL, NULL, '2025-10-17 12:00:00', NULL, NULL, NULL, NULL, '2025-10-17 09:15:00', '2026-04-19 13:11:29', 0, NULL, NULL, 0.00, 0.00, 0.00, 0.00, NULL, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL);
INSERT INTO `orders` VALUES (4, '202510180001', 7, 4, 4, '索尼WH-1000XM4降噪耳机', 'https://picsum.photos/400/300?random=4', 1299.00, 'REFUNDED', '艺术学院楼403', '13800000007', '周八', '微信支付', '2025-10-18 15:50:00', '2025-10-18 16:10:00', NULL, NULL, NULL, NULL, NULL, NULL, '2025-10-18 15:40:00', '2026-04-19 13:11:55', 0, NULL, NULL, 0.00, 0.00, 0.00, 0.00, NULL, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL);
INSERT INTO `orders` VALUES (5, '202510190001', 9, 5, 5, '优衣库联名款卫衣 黑色 XL', 'https://picsum.photos/400/300?random=5', 89.00, 'PENDING_PAYMENT', '体育学院104', '13800000009', '郑十', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2025-10-19 11:25:00', '2025-10-19 11:25:00', 0, NULL, NULL, 0.00, 0.00, 0.00, 0.00, NULL, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL);
INSERT INTO `orders` VALUES (6, '202510200001', 10, 6, 16, '佳能EOS M50 微单相机', 'https://picsum.photos/400/300?random=6', 2899.00, 'PAID', '美术学院画室', '13800000010', '陈一', '支付宝', '2025-10-20 14:45:00', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2025-10-20 14:30:00', '2025-10-20 15:00:00', 0, NULL, NULL, 0.00, 0.00, 0.00, 0.00, NULL, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL);

-- ----------------------------
-- Table structure for reviews
-- ----------------------------
DROP TABLE IF EXISTS `reviews`;
CREATE TABLE `reviews`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评价主键ID',
  `order_id` bigint NOT NULL COMMENT '关联订单ID，外键关联orders表',
  `reviewer_id` bigint NOT NULL COMMENT '评价者用户ID，外键关联users表',
  `reviewed_user_id` bigint NOT NULL COMMENT '被评价者用户ID，外键关联users表',
  `item_id` bigint NOT NULL COMMENT '关联物品ID，外键关联items表',
  `rating` int NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '评价文字内容',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '评价图片，存储为JSON格式',
  `is_anonymous` tinyint(1) NULL DEFAULT 0 COMMENT '是否匿名评价：TRUE-匿名，FALSE-实名',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  `create_by` bigint NULL DEFAULT NULL,
  `update_by` bigint NULL DEFAULT NULL,
  `reply_content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `reply_time` datetime NULL DEFAULT NULL,
  `is_show` tinyint(1) NOT NULL DEFAULT 1,
  `helpful_count` int NOT NULL DEFAULT 0,
  `report_count` int NOT NULL DEFAULT 0,
  `is_reported` tinyint(1) NOT NULL DEFAULT 0,
  `report_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `check_status` tinyint NOT NULL DEFAULT 0,
  `check_time` datetime NULL DEFAULT NULL,
  `check_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `tag` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_reviewer`(`order_id` ASC, `reviewer_id` ASC) USING BTREE,
  INDEX `idx_reviewer_id`(`reviewer_id` ASC) USING BTREE,
  INDEX `idx_reviewed_user_id`(`reviewed_user_id` ASC) USING BTREE,
  INDEX `idx_item_id`(`item_id` ASC) USING BTREE,
  INDEX `idx_rating`(`rating` ASC) USING BTREE,
  INDEX `IDX_reviews_item_id_rating`(`item_id` ASC, `rating` ASC) USING BTREE,
  INDEX `IDX_reviews_check_status_is_show`(`check_status` ASC, `is_show` ASC) USING BTREE,
  CONSTRAINT `reviews_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `reviews_ibfk_2` FOREIGN KEY (`reviewer_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `reviews_ibfk_3` FOREIGN KEY (`reviewed_user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `reviews_ibfk_4` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_rating` CHECK ((`rating` >= 1) and (`rating` <= 5))
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '评价表：记录交易完成后的双方评价' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of reviews
-- ----------------------------
INSERT INTO `reviews` VALUES (1, 1, 3, 2, 1, 5, '手机非常新，跟描述一致。卖家发货很快，包装仔细，好评！', NULL, 0, '2025-10-17 16:30:00', '2025-10-17 16:30:00', 0, NULL, NULL, NULL, NULL, 1, 0, 0, 0, NULL, 0, NULL, NULL, NULL);
INSERT INTO `reviews` VALUES (2, 1, 2, 3, 1, 5, '买家很爽快，沟通顺畅，收到款后立刻确认，好评！', NULL, 0, '2025-10-17 17:00:00', '2025-10-17 17:00:00', 0, NULL, NULL, NULL, NULL, 1, 0, 0, 0, NULL, 0, NULL, NULL, NULL);
INSERT INTO `reviews` VALUES (3, 2, 4, 3, 2, 4, '笔记本整体不错，就是有点轻微划痕，在可接受范围内。', NULL, 0, '2025-10-18 12:00:00', '2025-10-18 12:00:00', 0, NULL, NULL, NULL, NULL, 1, 0, 0, 0, NULL, 0, NULL, NULL, NULL);
INSERT INTO `reviews` VALUES (4, 2, 3, 4, 2, 5, '买家很nice，价格合理，交易愉快！', NULL, 0, '2025-10-18 13:30:00', '2025-10-18 13:30:00', 0, NULL, NULL, NULL, NULL, 1, 0, 0, 0, NULL, 0, NULL, NULL, NULL);
INSERT INTO `reviews` VALUES (5, 3, 5, 2, 3, 5, '书很新，教材内容完整，感谢学长！', NULL, 0, '2025-10-17 12:30:00', '2025-10-17 12:30:00', 0, NULL, NULL, NULL, NULL, 1, 0, 0, 0, NULL, 0, NULL, NULL, NULL);
INSERT INTO `reviews` VALUES (6, 3, 2, 5, 3, 5, '买家很准时，面交顺利，好评！', NULL, 0, '2025-10-17 13:00:00', '2025-10-17 13:00:00', 0, NULL, NULL, NULL, NULL, 1, 0, 0, 0, NULL, 0, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for system_configs
-- ----------------------------
DROP TABLE IF EXISTS `system_configs`;
CREATE TABLE `system_configs`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `config_type` tinyint NOT NULL DEFAULT 1,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `is_editable` tinyint(1) NOT NULL DEFAULT 1,
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_system_configs_config_key`(`config_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of system_configs
-- ----------------------------

-- ----------------------------
-- Table structure for user_addresses
-- ----------------------------
DROP TABLE IF EXISTS `user_addresses`;
CREATE TABLE `user_addresses`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `receiver_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `receiver_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `district` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `detail_address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `zip_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `is_default` tinyint(1) NOT NULL DEFAULT 0,
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `IDX_user_addresses_user_id_is_default`(`user_id` ASC, `is_default` ASC) USING BTREE,
  INDEX `IDX_user_addresses_user_id_is_deleted`(`user_id` ASC, `is_deleted` ASC) USING BTREE,
  CONSTRAINT `FK_user_addresses_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_addresses
-- ----------------------------

-- ----------------------------
-- Table structure for user_follows
-- ----------------------------
DROP TABLE IF EXISTS `user_follows`;
CREATE TABLE `user_follows`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `follower_id` bigint NOT NULL,
  `following_id` bigint NOT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_user_follows_follower_following`(`follower_id` ASC, `following_id` ASC) USING BTREE,
  INDEX `IDX_user_follows_follower_id_is_deleted`(`follower_id` ASC, `is_deleted` ASC) USING BTREE,
  INDEX `IDX_user_follows_following_id_is_deleted`(`following_id` ASC, `is_deleted` ASC) USING BTREE,
  CONSTRAINT `FK_user_follows_follower_id` FOREIGN KEY (`follower_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `FK_user_follows_following_id` FOREIGN KEY (`following_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_follows
-- ----------------------------

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户主键ID，自增长',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名，用于登录，唯一标识',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码，BCrypt加密存储，长度60字符',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱地址，用于注册验证和密码找回',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号码，用于注册验证和密码找回',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户昵称，用于展示',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户头像URL地址',
  `role` enum('STUDENT','ADMIN') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'STUDENT' COMMENT '用户角色：STUDENT-学生，ADMIN-管理员',
  `status` enum('ACTIVE','DISABLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'ACTIVE' COMMENT '账号状态：ACTIVE-活跃，DISABLED-禁用',
  `verified` tinyint(1) NULL DEFAULT 0 COMMENT '是否实名认证：TRUE-已认证，FALSE-未认证',
  `student_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学号，实名认证时填写',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '账号创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '账号更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  `create_by` bigint NULL DEFAULT NULL,
  `update_by` bigint NULL DEFAULT NULL,
  `last_login_time` datetime NULL DEFAULT NULL,
  `last_login_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `login_count` int NOT NULL DEFAULT 0,
  `credit_score` int NOT NULL DEFAULT 100,
  `total_transactions` int NOT NULL DEFAULT 0,
  `total_sales` int NOT NULL DEFAULT 0,
  `total_purchases` int NOT NULL DEFAULT 0,
  `gender` tinyint NULL DEFAULT NULL,
  `birthday` date NULL DEFAULT NULL,
  `bio` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `school_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `email`(`email` ASC) USING BTREE,
  UNIQUE INDEX `phone`(`phone` ASC) USING BTREE,
  INDEX `idx_email`(`email` ASC) USING BTREE,
  INDEX `idx_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_role`(`role` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `IDX_users_role_status_verified`(`role` ASC, `status` ASC, `verified` ASC) USING BTREE,
  INDEX `IDX_users_last_login_time`(`last_login_time` ASC) USING BTREE,
  INDEX `IDX_users_credit_score`(`credit_score` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户信息表：存储系统用户的基本信息、认证状态和角色权限' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'cgl', '$2b$10$MJKq.rMhDsVvURSb/0EiNO6xlWr/aA1C7nGN4eeGJEz3zj8AveVEK', NULL, NULL, '系统管理员', 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin', 'ADMIN', 'ACTIVE', 0, NULL, '2026-04-18 02:16:42', '2026-04-19 16:04:07', 0, NULL, NULL, NULL, NULL, 0, 100, 0, 0, 0, NULL, NULL, NULL, NULL);
INSERT INTO `users` VALUES (2, 'admin', '$2b$10$MJKq.rMhDsVvURSb/0EiNO6xlWr/aA1C7nGN4eeGJEz3zj8AveVEK', 'admin@school.edu.cn', '13800000001', '系统管理员', 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin', 'ADMIN', 'ACTIVE', 1, 'ADMIN001', '2025-09-01 08:00:00', '2026-04-19 16:04:05', 0, NULL, NULL, NULL, NULL, 0, 100, 0, 0, 0, NULL, NULL, NULL, NULL);
INSERT INTO `users` VALUES (3, 'zhangsan', '$2b$10$MJKq.rMhDsVvURSb/0EiNO6xlWr/aA1C7nGN4eeGJEz3zj8AveVEK', 'zhangsan@school.edu.cn', '13800000002', '张三', 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhangsan', 'STUDENT', 'ACTIVE', 1, '2025001001', '2025-09-05 10:30:00', '2026-04-18 02:27:21', 0, NULL, NULL, NULL, NULL, 0, 100, 0, 0, 0, NULL, NULL, NULL, NULL);
INSERT INTO `users` VALUES (4, 'lisi', '$2b$10$MJKq.rMhDsVvURSb/0EiNO6xlWr/aA1C7nGN4eeGJEz3zj8AveVEK', 'lisi@school.edu.cn', '13800000003', '李四', 'https://api.dicebear.com/7.x/avataaars/svg?seed=lisi', 'STUDENT', 'ACTIVE', 1, '2025001002', '2025-09-06 14:20:00', '2026-04-18 02:27:22', 0, NULL, NULL, NULL, NULL, 0, 100, 0, 0, 0, NULL, NULL, NULL, NULL);
INSERT INTO `users` VALUES (5, 'wangwu', '$2b$10$MJKq.rMhDsVvURSb/0EiNO6xlWr/aA1C7nGN4eeGJEz3zj8AveVEK', 'wangwu@school.edu.cn', '13800000004', '王五', 'https://api.dicebear.com/7.x/avataaars/svg?seed=wangwu', 'STUDENT', 'ACTIVE', 0, '2025001003', '2025-09-08 09:15:00', '2026-04-18 02:27:23', 0, NULL, NULL, NULL, NULL, 0, 100, 0, 0, 0, NULL, NULL, NULL, NULL);
INSERT INTO `users` VALUES (6, 'zhaoliu', '$2b$10$MJKq.rMhDsVvURSb/0EiNO6xlWr/aA1C7nGN4eeGJEz3zj8AveVEK', 'zhaoliu@school.edu.cn', '13800000005', '赵六', 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhaoliu', 'STUDENT', 'ACTIVE', 1, '2025001004', '2025-09-10 16:45:00', '2026-04-18 02:27:26', 0, NULL, NULL, NULL, NULL, 0, 100, 0, 0, 0, NULL, NULL, NULL, NULL);
INSERT INTO `users` VALUES (7, 'sunqi', '$2b$10$MJKq.rMhDsVvURSb/0EiNO6xlWr/aA1C7nGN4eeGJEz3zj8AveVEK', 'sunqi@school.edu.cn', '13800000006', '孙七', 'https://api.dicebear.com/7.x/avataaars/svg?seed=sunqi', 'STUDENT', 'DISABLED', 0, '2025001005', '2025-09-12 11:20:00', '2026-04-18 02:27:28', 0, NULL, NULL, NULL, NULL, 0, 100, 0, 0, 0, NULL, NULL, NULL, NULL);
INSERT INTO `users` VALUES (8, 'zhouba', '$2b$10$MJKq.rMhDsVvURSb/0EiNO6xlWr/aA1C7nGN4eeGJEz3zj8AveVEK', 'zhouba@school.edu.cn', '13800000007', '周八', 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhouba', 'STUDENT', 'ACTIVE', 1, '2025001006', '2025-09-15 08:30:00', '2026-04-18 02:27:29', 0, NULL, NULL, NULL, NULL, 0, 100, 0, 0, 0, NULL, NULL, NULL, NULL);
INSERT INTO `users` VALUES (9, 'wujiu', '$2b$10$MJKq.rMhDsVvURSb/0EiNO6xlWr/aA1C7nGN4eeGJEz3zj8AveVEK', 'wujiu@school.edu.cn', '13800000008', '吴九', 'https://api.dicebear.com/7.x/avataaars/svg?seed=wujiu', 'STUDENT', 'ACTIVE', 0, '2025001007', '2025-09-18 13:50:00', '2026-04-18 02:27:30', 0, NULL, NULL, NULL, NULL, 0, 100, 0, 0, 0, NULL, NULL, NULL, NULL);
INSERT INTO `users` VALUES (10, 'zhengshi', '$2b$10$MJKq.rMhDsVvURSb/0EiNO6xlWr/aA1C7nGN4eeGJEz3zj8AveVEK', 'zhengshi@school.edu.cn', '13800000009', '郑十', 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhengshi', 'STUDENT', 'ACTIVE', 1, '2025001008', '2025-09-20 10:10:00', '2026-04-18 02:27:31', 0, NULL, NULL, NULL, NULL, 0, 100, 0, 0, 0, NULL, NULL, NULL, NULL);
INSERT INTO `users` VALUES (11, 'chenyi', '$2b$10$MJKq.rMhDsVvURSb/0EiNO6xlWr/aA1C7nGN4eeGJEz3zj8AveVEK', 'chenyi@school.edu.cn', '13800000010', '陈一', 'https://api.dicebear.com/7.x/avataaars/svg?seed=chenyi', 'STUDENT', 'ACTIVE', 1, '2025001009', '2025-09-22 15:25:00', '2026-04-18 02:27:33', 0, NULL, NULL, NULL, NULL, 0, 100, 0, 0, 0, NULL, NULL, NULL, NULL);
INSERT INTO `users` VALUES (14, 'wanghao', '$2a$10$aBZPTnLNfejfsGsE2rcGBu8aaORHgDPRpxJDRZ93BhF9xJaoIvrFi', '2316970384@qq.com', '15666611128', '土狗', NULL, 'STUDENT', 'ACTIVE', 0, NULL, '2026-04-20 19:30:36', '2026-04-20 19:30:36', 0, NULL, NULL, NULL, NULL, 0, 100, 0, 0, 0, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for verification_records
-- ----------------------------
DROP TABLE IF EXISTS `verification_records`;
CREATE TABLE `verification_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '认证记录主键ID',
  `user_id` bigint NOT NULL COMMENT '关联的用户ID，外键关联users表',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '真实姓名，实名认证必填',
  `student_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学号，实名认证必填',
  `id_card` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '身份证号码，可选，用于高级认证',
  `teacher_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `student_card` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `teacher_card` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `status` enum('PENDING','APPROVED','REJECTED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'PENDING' COMMENT '审核状态：PENDING-待审核，APPROVED-已通过，REJECTED-已驳回',
  `reject_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '驳回理由，状态为REJECTED时必填',
  `reviewer_id` bigint NULL DEFAULT NULL COMMENT '审核人ID，外键关联users表（管理员）',
  `reviewed_at` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请提交时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  `create_by` bigint NULL DEFAULT NULL,
  `update_by` bigint NULL DEFAULT NULL,
  `school` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `department` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `major` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `enrollment_year` int NULL DEFAULT NULL,
  `graduation_year` int NULL DEFAULT NULL,
  `student_card_back_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `id_card_front` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `id_card_back` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `type` enum('ID_CARD','STUDENT_CARD','TEACHER_CARD') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `face_recognition_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `face_recognition_passed` tinyint(1) NULL DEFAULT NULL,
  `submit_count` int NOT NULL DEFAULT 1,
  `last_submit_time` datetime NULL DEFAULT NULL,
  `review_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `auto_approved` tinyint(1) NOT NULL DEFAULT 0,
  `risk_level` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `reviewer_id`(`reviewer_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `IDX_verification_records_user_id_status`(`user_id` ASC, `status` ASC) USING BTREE,
  INDEX `IDX_verification_records_status_reviewed_at`(`status` ASC, `reviewed_at` ASC) USING BTREE,
  CONSTRAINT `verification_records_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `verification_records_ibfk_2` FOREIGN KEY (`reviewer_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '实名认证记录表：记录学生实名认证的申请和审核流程' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of verification_records
-- ----------------------------
INSERT INTO `verification_records` VALUES (1, 2, '张三', '2025001001', '110101199901011234', NULL, 'https://picsum.photos/400/300?random=v1', NULL, 'APPROVED', NULL, 1, '2025-09-06 10:00:00', '2025-09-05 11:00:00', '2025-09-06 10:00:00', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'ID_CARD', NULL, NULL, 1, NULL, NULL, 0, 0);
INSERT INTO `verification_records` VALUES (2, 3, '李四', '2025001002', '110101199902022345', NULL, 'https://picsum.photos/400/300?random=v2', NULL, 'APPROVED', NULL, 1, '2025-09-07 09:30:00', '2025-09-06 14:30:00', '2025-09-07 09:30:00', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'ID_CARD', NULL, NULL, 1, NULL, NULL, 0, 0);
INSERT INTO `verification_records` VALUES (3, 5, '赵六', '2025001004', '110101199904044567', NULL, 'https://picsum.photos/400/300?random=v3', NULL, 'APPROVED', NULL, 1, '2025-09-11 11:00:00', '2025-09-10 17:00:00', '2025-09-11 11:00:00', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'ID_CARD', NULL, NULL, 1, NULL, NULL, 0, 0);
INSERT INTO `verification_records` VALUES (4, 7, '周八', '2025001006', '110101199906066789', NULL, 'https://picsum.photos/400/300?random=v4', NULL, 'APPROVED', NULL, 1, '2025-09-16 10:30:00', '2025-09-15 09:00:00', '2025-09-16 10:30:00', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'ID_CARD', NULL, NULL, 1, NULL, NULL, 0, 0);
INSERT INTO `verification_records` VALUES (5, 9, '郑十', '2025001008', '110101199908088901', NULL, 'https://picsum.photos/400/300?random=v5', NULL, 'APPROVED', NULL, 1, '2025-09-21 14:00:00', '2025-09-20 10:30:00', '2025-09-21 14:00:00', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'ID_CARD', NULL, NULL, 1, NULL, NULL, 0, 0);
INSERT INTO `verification_records` VALUES (6, 10, '陈一', '2025001009', '110101199910101123', NULL, 'https://picsum.photos/400/300?random=v6', NULL, 'APPROVED', NULL, 1, '2026-04-18 18:08:46', '2025-10-19 16:00:00', '2026-04-18 18:08:46', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'ID_CARD', NULL, NULL, 1, NULL, NULL, 0, 0);
INSERT INTO `verification_records` VALUES (7, 14, '王浩', '202503071028', NULL, NULL, 'http://localhost:7000/uploads/2026/04/20/19b29858-7e7b-4dde-a0bb-1e47c0d80a3c.png', NULL, 'REJECTED', '未选择认证类型。', 1, '2026-04-20 23:59:30', '2026-04-20 23:44:47', '2026-04-20 23:59:30', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'ID_CARD', NULL, NULL, 1, NULL, NULL, 0, 0);
INSERT INTO `verification_records` VALUES (8, 14, '王浩', '202503071027', NULL, NULL, 'http://localhost:7000/uploads/2026/04/21/b6804b92-62da-419f-902e-04f9f3126f10.png', NULL, 'APPROVED', NULL, 1, '2026-04-21 16:28:05', '2026-04-21 00:24:25', '2026-04-21 16:28:05', 0, NULL, NULL, '山东外国语职业技术大学', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'STUDENT_CARD', NULL, NULL, 1, NULL, NULL, 0, 0);

SET FOREIGN_KEY_CHECKS = 1;
