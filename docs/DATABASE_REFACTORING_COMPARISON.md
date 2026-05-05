# 数据库重构对比文档

## 概述

本文档详细记录了学生闲置物品交易平台数据库从旧版本到新版本的重构过程，包括表结构、字段定义、索引设计等方面的变更。重构严格遵循MySQL 8.0.44的语法规范和最佳实践，确保数据库结构与系统实体类保持一致，并考虑了未来扩展性和性能需求。

## 重构前后差异对比

### 1. 整体变更

| 项目 | 旧版本 | 新版本 | 变更原因 |
|------|-------|-------|----------|
| 版本号 | 1.0.0 | 2.0.0 | 标识重构版本 |
| 兼容性 | MySQL 8.0+ | MySQL 8.0.44+ | 明确兼容版本 |
| 脚本结构 | 基础结构 | 优化结构，增加详细注释 | 提高可读性和可维护性 |

### 2. 表结构变更

#### 2.1 orders表

| 变更类型 | 变更内容 | 变更原因 |
|---------|---------|----------|
| 字段添加 | 添加refund_reason字段 | 支持退款原因记录，与Order实体类保持一致 |
| 字段添加 | 添加refund_time字段 | 支持退款时间记录，与Order实体类保持一致 |
| 字段添加 | 添加refund_amount字段 | 支持退款金额记录，与Order实体类保持一致 |
| 时间戳优化 | 使用DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 自动更新时间戳，减少代码维护 |

#### 2.2 reviews表

| 变更类型 | 变更内容 | 变更原因 |
|---------|---------|----------|
| 字段类型调整 | images字段从JSON类型改为TEXT类型 | 与Review实体类保持一致，使用字符串存储JSON格式 |
| 时间戳优化 | 添加updated_at字段，使用DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 支持记录评价更新时间 |

#### 2.3 disputes表

| 变更类型 | 变更内容 | 变更原因 |
|---------|---------|----------|
| 字段类型调整 | evidence_images字段从JSON类型改为TEXT类型 | 与系统其他表保持一致，使用字符串存储JSON格式 |

#### 2.4 items表

| 变更类型 | 变更内容 | 变更原因 |
|---------|---------|----------|
| 字段重命名 | condition字段重命名为item_condition | 避免与MySQL保留字冲突 |
| 字段重命名 | status字段重命名为item_status | 提高字段命名的明确性 |

### 3. 索引设计优化

| 表名 | 索引变更 | 变更原因 |
|------|---------|----------|
| users | 保持原有索引 | 索引设计合理，无需变更 |
| items | 保持原有索引 | 索引设计合理，无需变更 |
| orders | 保持原有索引 | 索引设计合理，无需变更 |
| reviews | 保持原有索引 | 索引设计合理，无需变更 |
| favorites | 保持原有索引 | 索引设计合理，无需变更 |

### 4. 约束条件优化

| 表名 | 约束变更 | 变更原因 |
|------|---------|----------|
| reviews | 保持CHECK约束 | 约束设计合理，无需变更 |
| 所有表 | 保持外键约束 | 外键设计合理，无需变更 |
| 所有表 | 保持唯一约束 | 唯一约束设计合理，无需变更 |

### 5. 存储过程和触发器

| 变更类型 | 变更内容 | 变更原因 |
|---------|---------|----------|
| 时间戳管理 | 使用MySQL内置的DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 减少代码维护，提高性能 |

## 重构验证方案

### 1. 数据一致性检查

1. **结构一致性**：
   - 验证所有表结构与系统实体类保持一致
   - 验证所有字段类型、长度、约束与实体类注解保持一致

2. **数据完整性**：
   - 执行重构脚本后，验证初始数据（分类数据、管理员账号）是否正确插入
   - 验证外键约束是否正常工作
   - 验证唯一约束是否正常工作

### 2. 性能测试

1. **查询性能**：
   - 测试物品列表查询响应时间
   - 测试订单查询响应时间
   - 测试用户查询响应时间

2. **写入性能**：
   - 测试物品发布响应时间
   - 测试订单创建响应时间
   - 测试评价提交响应时间

3. **并发测试**：
   - 模拟多用户同时访问系统
   - 测试并发下单场景
   - 测试并发浏览场景

### 3. 功能验证步骤

1. **用户管理**：
   - 测试用户注册、登录功能
   - 测试用户信息更新功能
   - 测试用户实名认证功能

2. **物品管理**：
   - 测试物品发布功能
   - 测试物品编辑功能
   - 测试物品搜索功能
   - 测试物品收藏功能

3. **订单管理**：
   - 测试订单创建功能
   - 测试订单状态更新功能
   - 测试订单退款功能

4. **评价管理**：
   - 测试评价提交功能
   - 测试评价查询功能

5. **聊天功能**：
   - 测试聊天会话创建功能
   - 测试消息发送和接收功能

6. **纠纷处理**：
   - 测试纠纷申请功能
   - 测试纠纷处理功能

## 数据库设计文档

### 1. ER图

![数据库ER图](database_er_diagram.png)

### 2. 表结构说明

| 表名 | 用途描述 |
|------|----------|
| users | 存储系统用户（学生和管理员）的基本信息、认证状态和角色权限 |
| verification_records | 记录学生实名认证的申请和审核流程 |
| categories | 管理闲置物品的分类体系，支持二级分类 |
| items | 存储闲置物品的详细信息，支持发布、搜索和交易 |
| item_images | 存储物品的多张图片，支持封面设置和排序 |
| item_tags | 为物品添加标签，支持更灵活的分类和搜索 |
| favorites | 记录用户收藏的物品 |
| orders | 管理交易订单，跟踪订单全生命周期 |
| reviews | 记录交易完成后的双方评价 |
| chats | 管理买家和卖家之间的聊天会话 |
| chat_messages | 存储聊天会话中的详细消息 |
| disputes | 记录和处理交易纠纷 |
| image_analysis | 记录AI图像识别的分析结果 |

### 3. 字段定义

#### 3.1 users表

| 字段名 | 数据类型 | 长度 | 约束条件 | 业务含义 |
|--------|---------|------|----------|----------|
| id | BIGINT | - | PRIMARY KEY AUTO_INCREMENT | 用户主键ID，自增长 |
| username | VARCHAR | 50 | UNIQUE NOT NULL | 用户名，用于登录，唯一标识 |
| password | VARCHAR | 255 | NOT NULL | 密码，BCrypt加密存储 |
| email | VARCHAR | 100 | UNIQUE | 邮箱地址，用于注册验证和密码找回 |
| phone | VARCHAR | 20 | UNIQUE | 手机号码，用于注册验证和密码找回 |
| nickname | VARCHAR | 50 | - | 用户昵称，用于展示 |
| avatar | VARCHAR | 255 | - | 用户头像URL地址 |
| role | ENUM | - | DEFAULT 'STUDENT' | 用户角色：STUDENT-学生，ADMIN-管理员 |
| status | ENUM | - | DEFAULT 'ACTIVE' | 账号状态：ACTIVE-活跃，DISABLED-禁用 |
| verified | BOOLEAN | - | DEFAULT FALSE | 是否实名认证：TRUE-已认证，FALSE-未认证 |
| student_id | VARCHAR | 50 | - | 学号，实名认证时填写 |
| created_at | DATETIME | - | DEFAULT CURRENT_TIMESTAMP | 账号创建时间 |
| updated_at | DATETIME | - | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 账号更新时间 |

#### 3.2 items表

| 字段名 | 数据类型 | 长度 | 约束条件 | 业务含义 |
|--------|---------|------|----------|----------|
| id | BIGINT | - | PRIMARY KEY AUTO_INCREMENT | 物品主键ID |
| user_id | BIGINT | - | NOT NULL | 发布者用户ID，外键关联users表 |
| category_id | BIGINT | - | NOT NULL | 分类ID，外键关联categories表 |
| title | VARCHAR | 200 | NOT NULL | 物品标题，用于展示和搜索 |
| description | TEXT | - | - | 物品详细描述，支持富文本 |
| price | DECIMAL | 10,2 | NOT NULL | 出售价格，单位：元，保留两位小数 |
| original_price | DECIMAL | 10,2 | - | 原价，用于显示折扣信息 |
| item_condition | ENUM | - | DEFAULT 'GOOD' | 新旧程度：NEW-全新，LIKE_NEW-几乎全新，GOOD-良好，FAIR-一般，POOR-较差 |
| item_status | ENUM | - | DEFAULT 'PENDING' | 物品状态：DRAFT-草稿，PENDING-待审核，ON_SALE-在售，SOLD-已售，OFF_SHELF-下架，REJECTED-驳回 |
| view_count | INT | - | DEFAULT 0 | 浏览次数，统计物品热度 |
| favorite_count | INT | - | DEFAULT 0 | 收藏次数，统计物品受欢迎程度 |
| reject_reason | VARCHAR | 255 | - | 审核驳回理由，状态为REJECTED时必填 |
| location | VARCHAR | 200 | - | 交易地点，如"图书馆门口"、"宿舍区" |
| created_at | DATETIME | - | DEFAULT CURRENT_TIMESTAMP | 发布时间 |
| updated_at | DATETIME | - | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

#### 3.3 orders表

| 字段名 | 数据类型 | 长度 | 约束条件 | 业务含义 |
|--------|---------|------|----------|----------|
| id | BIGINT | - | PRIMARY KEY AUTO_INCREMENT | 订单主键ID |
| order_no | VARCHAR | 50 | UNIQUE NOT NULL | 订单号，唯一标识，格式：YYYYMMDDHHMMSS+随机数 |
| buyer_id | BIGINT | - | NOT NULL | 买家用户ID，外键关联users表 |
| seller_id | BIGINT | - | NOT NULL | 卖家用户ID，外键关联users表 |
| item_id | BIGINT | - | NOT NULL | 物品ID，外键关联items表 |
| item_title | VARCHAR | 200 | - | 物品标题快照，防止物品信息变更影响历史订单 |
| item_image | VARCHAR | 255 | - | 物品图片快照 |
| price | DECIMAL | 10,2 | NOT NULL | 成交价格，单位：元 |
| order_status | ENUM | - | DEFAULT 'PENDING_PAYMENT' | 订单状态：PENDING_PAYMENT-待支付，PAID-已支付，SHIPPED-已发货，DELIVERED-已收货，COMPLETED-已完成，CANCELLED-已取消，REFUND_REQUESTED-退款申请中，REFUNDED-已退款 |
| buyer_address | VARCHAR | 500 | - | 收货地址 |
| buyer_phone | VARCHAR | 20 | - | 买家联系电话 |
| buyer_name | VARCHAR | 50 | - | 买家姓名 |
| payment_method | VARCHAR | 50 | - | 支付方式，如"微信支付"、"支付宝"、"线下交易" |
| payment_time | DATETIME | - | - | 支付时间 |
| ship_time | DATETIME | - | - | 发货时间 |
| deliver_time | DATETIME | - | - | 收货时间 |
| complete_time | DATETIME | - | - | 订单完成时间 |
| cancel_reason | VARCHAR | 255 | - | 取消原因 |
| refund_reason | VARCHAR | 255 | - | 退款原因 |
| refund_time | DATETIME | - | - | 退款时间 |
| refund_amount | DECIMAL | 10,2 | - | 退款金额 |
| created_at | DATETIME | - | DEFAULT CURRENT_TIMESTAMP | 订单创建时间 |
| updated_at | DATETIME | - | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 订单更新时间 |

### 4. 关系说明

| 关系类型 | 关联表 | 关联字段 | 关联规则 |
|---------|--------|---------|----------|
| 一对多 | users → items | users.id → items.user_id | 一个用户可以发布多个物品 |
| 一对多 | categories → items | categories.id → items.category_id | 一个分类可以包含多个物品 |
| 一对多 | items → item_images | items.id → item_images.item_id | 一个物品可以有多个图片 |
| 一对多 | items → item_tags | items.id → item_tags.item_id | 一个物品可以有多个标签 |
| 一对多 | users → favorites | users.id → favorites.user_id | 一个用户可以收藏多个物品 |
| 一对多 | items → favorites | items.id → favorites.item_id | 一个物品可以被多个用户收藏 |
| 一对多 | users → orders (buyer) | users.id → orders.buyer_id | 一个用户可以作为买家创建多个订单 |
| 一对多 | users → orders (seller) | users.id → orders.seller_id | 一个用户可以作为卖家接收多个订单 |
| 一对多 | items → orders | items.id → orders.item_id | 一个物品可以被交易多次（不同订单） |
| 一对多 | orders → reviews | orders.id → reviews.order_id | 一个订单可以有多个评价（买家和卖家各一个） |
| 一对多 | users → reviews (reviewer) | users.id → reviews.reviewer_id | 一个用户可以作为评价者发布多个评价 |
| 一对多 | users → reviews (reviewed) | users.id → reviews.reviewed_user_id | 一个用户可以作为被评价者接收多个评价 |
| 一对多 | items → reviews | items.id → reviews.item_id | 一个物品可以有多个评价 |
| 一对多 | users → chats (buyer) | users.id → chats.buyer_id | 一个用户可以作为买家参与多个聊天 |
| 一对多 | users → chats (seller) | users.id → chats.seller_id | 一个用户可以作为卖家参与多个聊天 |
| 一对多 | items → chats | items.id → chats.item_id | 一个物品可以关联多个聊天 |
| 一对多 | orders → chats | orders.id → chats.order_id | 一个订单可以关联多个聊天 |
| 一对多 | chats → chat_messages | chats.id → chat_messages.chat_id | 一个聊天可以包含多个消息 |
| 一对多 | users → chat_messages (sender) | users.id → chat_messages.sender_id | 一个用户可以发送多个消息 |
| 一对多 | users → chat_messages (receiver) | users.id → chat_messages.receiver_id | 一个用户可以接收多个消息 |
| 一对多 | orders → disputes | orders.id → disputes.order_id | 一个订单可以有多个纠纷 |
| 一对多 | users → disputes (applicant) | users.id → disputes.applicant_id | 一个用户可以作为申请人发起多个纠纷 |
| 一对多 | users → disputes (respondent) | users.id → disputes.respondent_id | 一个用户可以作为被申请人参与多个纠纷 |
| 一对多 | users → disputes (handler) | users.id → disputes.handler_id | 一个管理员可以处理多个纠纷 |
| 一对多 | items → image_analysis | items.id → image_analysis.item_id | 一个物品可以有多个图片分析记录 |

### 5. 使用规范

#### 5.1 SQL编写规范

1. **命名规范**：
   - 表名：使用小写字母，单词之间用下划线分隔（如`user_items`）
   - 字段名：使用小写字母，单词之间用下划线分隔（如`user_id`）
   - 索引名：使用`idx_`前缀，后跟字段名（如`idx_user_id`）
   - 唯一约束：使用`uk_`前缀，后跟字段名（如`uk_username`）

2. **SQL语句规范**：
   - 使用大写字母书写SQL关键字（如`SELECT`、`INSERT`、`UPDATE`）
   - 使用缩进和换行提高可读性
   - 为复杂SQL语句添加注释

3. **性能规范**：
   - 避免使用`SELECT *`，只查询需要的字段
   - 为频繁查询的字段创建索引
   - 避免在WHERE子句中使用函数，以免影响索引使用
   - 使用JOIN时注意表的顺序，将小表放在前面

#### 5.2 索引使用建议

1. **必建索引**：
   - 主键字段（自动创建）
   - 外键字段
   - 频繁用于查询条件的字段
   - 用于排序的字段

2. **索引优化**：
   - 避免创建过多索引，影响写入性能
   - 考虑复合索引，提高多字段查询效率
   - 定期检查索引使用情况，优化未使用的索引

#### 5.3 性能优化指南

1. **查询优化**：
   - 使用EXPLAIN分析查询执行计划
   - 优化复杂查询，避免嵌套子查询
   - 合理使用分页查询，避免一次性查询大量数据

2. **写入优化**：
   - 使用批量插入减少网络开销
   - 合理使用事务，避免长事务
   - 考虑使用异步处理大量数据写入

3. **存储优化**：
   - 定期清理无用数据
   - 考虑使用分区表管理大量数据
   - 合理设置表的存储引擎和字符集

4. **连接池优化**：
   - 合理配置连接池大小
   - 设置适当的连接超时时间
   - 定期监控连接池使用情况

## 结论

本次数据库重构严格遵循MySQL 8.0.44的语法规范和最佳实践，确保了数据库结构与系统实体类的一致性，同时考虑了未来扩展性和性能需求。重构后的数据库结构更加合理、高效，为系统的稳定运行和功能扩展提供了坚实的基础。

通过执行验证方案，可以确保重构过程中数据的一致性和系统功能的正常运行，为系统的后续开发和维护奠定良好的基础。