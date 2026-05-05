# 数据库表结构分析报告

## 概述

本报告对闲置物品校园交易平台的数据库表结构进行全面分析，识别各表需要补充的字段，以确保系统功能的完整性、数据的可追溯性和未来的可扩展性。

## 一、现有表结构审查

### 1. users 表

**现有字段：**
- id, username, password, email, phone, nickname, avatar
- role, status, verified, student_id
- created_at, updated_at

**建议补充字段：**

| 字段名 | 数据类型 | 是否为空 | 默认值 | 用途说明 |
|--------|----------|----------|--------|----------|
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标识，支持软删除 |
| create_by | BIGINT | NULL | - | 创建人ID，用于追踪用户来源 |
| update_by | BIGINT | NULL | - | 更新人ID，用于操作审计 |
| last_login_time | DATETIME | NULL | - | 最后登录时间，用于用户活跃度分析 |
| last_login_ip | VARCHAR(50) | NULL | - | 最后登录IP，用于安全审计 |
| login_count | INT | NOT NULL | 0 | 登录次数统计 |
| gender | TINYINT | NULL | - | 性别（0-未知，1-男，2-女） |
| birthday | DATE | NULL | - | 生日，用于用户画像 |
| bio | VARCHAR(500) | NULL | - | 个人简介 |
| school_name | VARCHAR(100) | NULL | - | 学校名称（与student_id配合使用） |
| credit_score | INT | NOT NULL | 100 | 信用评分（0-100） |
| total_transactions | INT | NOT NULL | 0 | 累计交易次数 |
| total_sales | INT | NOT NULL | 0 | 累计售出商品数 |
| total_purchases | INT | NOT NULL | 0 | 累计购买商品数 |

**必要性分析：**
- 逻辑删除字段(is_deleted)：支持软删除，保留历史数据
- 操作人字段(create_by/update_by)：满足审计需求
- 登录相关字段：支持安全审计和用户行为分析
- 信用相关字段：支撑平台的信用评价体系

---

### 2. items 表

**现有字段：**
- id, user_id, category_id, title, description, price, original_price
- condition, status, view_count, favorite_count, reject_reason, location
- created_at, updated_at

**建议补充字段：**

| 字段名 | 数据类型 | 是否为空 | 默认值 | 用途说明 |
|--------|----------|----------|--------|----------|
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标识 |
| create_by | BIGINT | NULL | - | 创建人ID |
| update_by | BIGINT | NULL | - | 更新人ID |
| publish_time | DATETIME | NULL | - | 发布时间（与created_at区分） |
| off_shelf_time | DATETIME | NULL | - | 下架时间 |
| sold_time | DATETIME | NULL | - | 售出时间 |
| quality_score | DECIMAL(3,2) | NULL | - | 商品质量评分（AI识别） |
| is_bargain_allowed | TINYINT(1) | NOT NULL | 1 | 是否允许议价 |
| min_price | DECIMAL(10,2) | NULL | - | 最低接受价格（议价功能） |
| contact_type | TINYINT | NOT NULL | 1 | 联系方式（1-平台内，2-微信，3-QQ） |
| contact_info | VARCHAR(100) | NULL | - | 联系信息（脱敏存储） |
| is_recommended | TINYINT(1) | NOT NULL | 0 | 是否推荐商品 |
| recommend_time | DATETIME | NULL | - | 推荐时间 |
| weight | INT | NOT NULL | 0 | 商品重量（克），用于物流计算 |
| delivery_method | TINYINT | NOT NULL | 1 | 配送方式（1-自提，2-快递，3-两者皆可） |
| tags | VARCHAR(500) | NULL | - | 商品标签（JSON格式） |
| brand | VARCHAR(100) | NULL | - | 品牌 |
| purchase_date | DATE | NULL | - | 购买日期 |
| warranty_info | VARCHAR(255) | NULL | - | 保修信息 |

**必要性分析：**
- 时间相关字段：区分创建、发布、下架、售出等不同时间点
- 议价相关字段：支撑议价功能
- 推荐相关字段：支撑商品推荐系统
- 物流相关字段：支撑物流配送功能

---

### 3. orders 表

**现有字段：**
- id, order_no, buyer_id, seller_id, item_id, item_title, item_image, price
- order_status, buyer_address, buyer_phone, buyer_name, payment_method
- payment_time, ship_time, deliver_time, complete_time, cancel_reason
- refund_reason, refund_time, refund_amount, created_at, updated_at

**建议补充字段：**

| 字段名 | 数据类型 | 是否为空 | 默认值 | 用途说明 |
|--------|----------|----------|--------|----------|
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标识 |
| create_by | BIGINT | NULL | - | 创建人ID |
| update_by | BIGINT | NULL | - | 更新人ID |
| total_amount | DECIMAL(10,2) | NOT NULL | 0.00 | 订单总金额（含运费等） |
| shipping_fee | DECIMAL(10,2) | NOT NULL | 0.00 | 运费 |
| discount_amount | DECIMAL(10,2) | NOT NULL | 0.00 | 优惠金额 |
| pay_amount | DECIMAL(10,2) | NOT NULL | 0.00 | 实际支付金额 |
| transaction_id | VARCHAR(100) | NULL | - | 第三方支付交易号 |
| payment_status | TINYINT | NOT NULL | 0 | 支付状态（0-未支付，1-已支付，2-支付失败） |
| shipping_status | TINYINT | NOT NULL | 0 | 物流状态（0-未发货，1-已发货，2-已收货） |
| tracking_no | VARCHAR(50) | NULL | - | 物流单号 |
| shipping_company | VARCHAR(50) | NULL | - | 物流公司 |
| seller_note | VARCHAR(500) | NULL | - | 卖家备注 |
| buyer_note | VARCHAR(500) | NULL | - | 买家备注 |
| auto_confirm_time | DATETIME | NULL | - | 自动确认收货时间 |
| close_time | DATETIME | NULL | - | 订单关闭时间 |
| close_type | TINYINT | NULL | - | 关闭类型（1-超时未支付，2-买家取消，3-卖家取消） |
| source | TINYINT | NOT NULL | 1 | 订单来源（1-直接购买，2-议价成交） |
| ip_address | VARCHAR(50) | NULL | - | 下单IP地址 |
| user_agent | VARCHAR(500) | NULL | - | 用户代理信息 |

**必要性分析：**
- 金额相关字段：支撑完整的订单金额计算
- 物流相关字段：支撑完整的物流跟踪
- 订单生命周期字段：支撑订单状态流转的完整记录

---

### 4. categories 表

**现有字段：**
- id, name, description, parent_id, sort_order, icon, created_at, updated_at

**建议补充字段：**

| 字段名 | 数据类型 | 是否为空 | 默认值 | 用途说明 |
|--------|----------|----------|--------|----------|
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标识 |
| create_by | BIGINT | NULL | - | 创建人ID |
| update_by | BIGINT | NULL | - | 更新人ID |
| level | TINYINT | NOT NULL | 1 | 分类层级（1-一级，2-二级，3-三级） |
| path | VARCHAR(500) | NULL | - | 分类路径（如：1/2/3） |
| is_show | TINYINT(1) | NOT NULL | 1 | 是否显示 |
| item_count | INT | NOT NULL | 0 | 该分类下的商品数量 |
| keywords | VARCHAR(255) | NULL | - | SEO关键词 |
| meta_description | VARCHAR(500) | NULL | - | SEO描述 |
| image | VARCHAR(255) | NULL | - | 分类图片 |
| background_color | VARCHAR(20) | NULL | - | 背景颜色（前端展示用） |

**必要性分析：**
- 层级相关字段：支撑多级分类的完整展示
- SEO相关字段：支撑搜索引擎优化
- 统计相关字段：支撑分类商品数量统计

---

### 5. verification_records 表

**现有字段：**
- id, user_id, real_name, student_id, id_card, student_card_image
- status, reject_reason, reviewer_id, reviewed_at, created_at, updated_at

**建议补充字段：**

| 字段名 | 数据类型 | 是否为空 | 默认值 | 用途说明 |
|--------|----------|----------|--------|----------|
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标识 |
| create_by | BIGINT | NULL | - | 创建人ID |
| update_by | BIGINT | NULL | - | 更新人ID |
| school_name | VARCHAR(100) | NULL | - | 学校名称 |
| department | VARCHAR(100) | NULL | - | 院系 |
| major | VARCHAR(100) | NULL | - | 专业 |
| enrollment_year | INT | NULL | - | 入学年份 |
| graduation_year | INT | NULL | - | 预计毕业年份 |
| student_card_back_image | VARCHAR(255) | NULL | - | 学生证背面照片 |
| id_card_front_image | VARCHAR(255) | NULL | - | 身份证正面照片 |
| id_card_back_image | VARCHAR(255) | NULL | - | 身份证背面照片 |
| face_recognition_image | VARCHAR(255) | NULL | - | 人脸识别照片 |
| face_recognition_passed | TINYINT(1) | NULL | - | 人脸识别是否通过 |
| submit_count | INT | NOT NULL | 1 | 提交次数 |
| last_submit_time | DATETIME | NULL | - | 最后提交时间 |
| review_remark | VARCHAR(500) | NULL | - | 审核备注 |
| auto_approved | TINYINT(1) | NOT NULL | 0 | 是否自动审核通过 |
| risk_level | TINYINT | NOT NULL | 0 | 风险等级（0-正常，1-低风险，2-中风险，3-高风险） |

**必要性分析：**
- 学生信息字段：支撑更完整的实名认证信息收集
- 审核相关字段：支撑多次提交、自动审核等高级功能
- 安全相关字段：支撑风险控制和人工复核

---

### 6. reviews 表

**现有字段：**
- id, order_id, reviewer_id, reviewed_user_id, item_id, rating, content
- images, is_anonymous, created_at, updated_at

**建议补充字段：**

| 字段名 | 数据类型 | 是否为空 | 默认值 | 用途说明 |
|--------|----------|----------|--------|----------|
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标识 |
| create_by | BIGINT | NULL | - | 创建人ID |
| update_by | BIGINT | NULL | - | 更新人ID |
| reply_content | VARCHAR(1000) | NULL | - | 被评价者回复内容 |
| reply_time | DATETIME | NULL | - | 回复时间 |
| is_show | TINYINT(1) | NOT NULL | 1 | 是否显示 |
| helpful_count | INT | NOT NULL | 0 | 觉得有用的人数 |
| report_count | INT | NOT NULL | 0 | 被举报次数 |
| is_reported | TINYINT(1) | NOT NULL | 0 | 是否被举报 |
| report_reason | VARCHAR(255) | NULL | - | 举报原因 |
| check_status | TINYINT | NOT NULL | 0 | 审核状态（0-待审核，1-已通过，2-未通过） |
| check_time | DATETIME | NULL | - | 审核时间 |
| check_remark | VARCHAR(255) | NULL | - | 审核备注 |
| tag | VARCHAR(100) | NULL | - | 评价标签（JSON格式，如：["描述相符", "发货快"]） |

**必要性分析：**
- 互动相关字段：支撑评价回复、点赞等功能
- 审核相关字段：支撑评价的审核管理
- 统计相关字段：支撑评价质量分析

---

### 7. favorites 表

**现有字段：**
- id, user_id, item_id, created_at

**建议补充字段：**

| 字段名 | 数据类型 | 是否为空 | 默认值 | 用途说明 |
|--------|----------|----------|--------|----------|
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标识 |
| category_id | BIGINT | NULL | - | 商品分类ID（冗余存储，便于统计） |
| price_snapshot | DECIMAL(10,2) | NULL | - | 收藏时的价格快照 |
| remark | VARCHAR(255) | NULL | - | 用户备注 |
| notify_when_price_drop | TINYINT(1) | NOT NULL | 0 | 降价时是否通知 |
| target_price | DECIMAL(10,2) | NULL | - | 目标价格（达到此价格时通知） |

**必要性分析：**
- 价格相关字段：支撑降价提醒功能
- 备注字段：支撑用户个性化管理收藏

---

### 8. chats 表

**现有字段：**
- id, order_id, item_id, buyer_id, seller_id, created_at, updated_at

**建议补充字段：**

| 字段名 | 数据类型 | 是否为空 | 默认值 | 用途说明 |
|--------|----------|----------|--------|----------|
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标识 |
| create_by | BIGINT | NULL | - | 创建人ID |
| update_by | BIGINT | NULL | - | 更新人ID |
| last_message_id | BIGINT | NULL | - | 最后一条消息ID |
| last_message_content | VARCHAR(500) | NULL | - | 最后一条消息内容预览 |
| last_message_time | DATETIME | NULL | - | 最后消息时间 |
| last_message_sender_id | BIGINT | NULL | - | 最后消息发送者ID |
| buyer_unread_count | INT | NOT NULL | 0 | 买家未读消息数 |
| seller_unread_count | INT | NOT NULL | 0 | 卖家未读消息数 |
| is_blocked | TINYINT(1) | NOT NULL | 0 | 是否被屏蔽 |
| blocked_by | BIGINT | NULL | - | 屏蔽者ID |
| blocked_time | DATETIME | NULL | - | 屏蔽时间 |
| is_muted | TINYINT(1) | NOT NULL | 0 | 是否静音 |
| muted_by | BIGINT | NULL | - | 静音者ID |
| chat_status | TINYINT | NOT NULL | 1 | 聊天状态（1-正常，2-已关闭） |
| close_time | DATETIME | NULL | - | 关闭时间 |
| close_reason | VARCHAR(255) | NULL | - | 关闭原因 |

**必要性分析：**
- 消息统计字段：支撑未读消息计数和消息预览
- 会话管理字段：支撑屏蔽、静音、关闭等会话管理功能

---

### 9. chat_messages 表

**现有字段：**
- id, chat_id, sender_id, receiver_id, message_type, content
- is_anonymous, is_read, read_at, created_at

**建议补充字段：**

| 字段名 | 数据类型 | 是否为空 | 默认值 | 用途说明 |
|--------|----------|----------|--------|----------|
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标识 |
| create_by | BIGINT | NULL | - | 创建人ID |
| update_by | BIGINT | NULL | - | 更新人ID |
| message_status | TINYINT | NOT NULL | 1 | 消息状态（1-已发送，2-已送达，3-发送失败） |
| send_time | DATETIME | NULL | - | 发送时间（与created_at区分） |
| receive_time | DATETIME | NULL | - | 接收时间 |
| image_url | VARCHAR(255) | NULL | - | 图片消息URL |
| image_width | INT | NULL | - | 图片宽度 |
| image_height | INT | NULL | - | 图片高度 |
| file_url | VARCHAR(255) | NULL | - | 文件消息URL |
| file_name | VARCHAR(255) | NULL | - | 文件名 |
| file_size | BIGINT | NULL | - | 文件大小 |
| is_recalled | TINYINT(1) | NOT NULL | 0 | 是否已撤回 |
| recall_time | DATETIME | NULL | - | 撤回时间 |
| is_deleted_by_sender | TINYINT(1) | NOT NULL | 0 | 发送者是否删除 |
| is_deleted_by_receiver | TINYINT(1) | NOT NULL | 0 | 接收者是否删除 |
| reply_to_message_id | BIGINT | NULL | - | 回复的消息ID |
| reply_to_content | VARCHAR(500) | NULL | - | 回复的消息内容预览 |

**必要性分析：**
- 消息状态字段：支撑消息发送状态跟踪
- 多媒体字段：支撑图片、文件等多媒体消息
- 消息管理字段：支撑撤回、删除等消息管理功能

---

### 10. disputes 表

**现有字段：**
- id, order_id, applicant_id, respondent_id, reason, description
- evidence_images, dispute_status, handler_id, result, created_at, updated_at

**建议补充字段：**

| 字段名 | 数据类型 | 是否为空 | 默认值 | 用途说明 |
|--------|----------|----------|--------|----------|
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标识 |
| create_by | BIGINT | NULL | - | 创建人ID |
| update_by | BIGINT | NULL | - | 更新人ID |
| dispute_no | VARCHAR(50) | NOT NULL | - | 纠纷单号（唯一） |
| dispute_type | TINYINT | NOT NULL | 1 | 纠纷类型（1-商品问题，2-物流问题，3-其他） |
| expect_result | VARCHAR(500) | NULL | - | 期望处理结果 |
| expect_refund_amount | DECIMAL(10,2) | NULL | - | 期望退款金额 |
| actual_refund_amount | DECIMAL(10,2) | NULL | - | 实际退款金额 |
| process_remark | VARCHAR(1000) | NULL | - | 处理过程备注 |
| process_logs | TEXT | NULL | - | 处理日志（JSON格式） |
| is_urgent | TINYINT(1) | NOT NULL | 0 | 是否加急 |
| priority | TINYINT | NOT NULL | 1 | 优先级（1-低，2-中，3-高，4-紧急） |
| assign_time | DATETIME | NULL | - | 分配处理人时间 |
| start_process_time | DATETIME | NULL | - | 开始处理时间 |
| complete_time | DATETIME | NULL | - | 完成处理时间 |
| close_time | DATETIME | NULL | - | 关闭时间 |
| close_type | TINYINT | NULL | - | 关闭类型（1-正常关闭，2-超时关闭，3-撤销） |
| is_escalated | TINYINT(1) | NOT NULL | 0 | 是否已升级 |
| escalated_to | BIGINT | NULL | - | 升级给谁处理 |
| escalated_time | DATETIME | NULL | - | 升级时间 |
| escalated_reason | VARCHAR(255) | NULL | - | 升级原因 |
| satisfaction | TINYINT | NULL | - | 满意度评分（1-5分） |
| satisfaction_remark | VARCHAR(500) | NULL | - | 满意度评价内容 |

**必要性分析：**
- 流程管理字段：支撑完整的纠纷处理流程
- 金额相关字段：支撑退款金额的计算和记录
- 升级相关字段：支撑纠纷升级机制
- 满意度字段：支撑服务质量评估

---

### 11. item_images 表

**现有字段：**
- id, item_id, image_url, thumbnail_url, is_cover, sort_order
- width, height, file_size, format, created_at

**建议补充字段：**

| 字段名 | 数据类型 | 是否为空 | 默认值 | 用途说明 |
|--------|----------|----------|--------|----------|
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标识 |
| create_by | BIGINT | NULL | - | 创建人ID |
| update_by | BIGINT | NULL | - | 更新人ID |
| update_at | DATETIME | NULL | - | 更新时间 |
| image_hash | VARCHAR(64) | NULL | - | 图片哈希值（用于去重） |
| storage_type | TINYINT | NOT NULL | 1 | 存储类型（1-本地，2-云存储） |
| storage_path | VARCHAR(500) | NULL | - | 存储路径 |
| is_compressed | TINYINT(1) | NOT NULL | 0 | 是否已压缩 |
| is_watermarked | TINYINT(1) | NOT NULL | 0 | 是否已添加水印 |
| ai_analysis_result | TEXT | NULL | - | AI分析结果（JSON格式） |
| ai_analysis_status | TINYINT | NOT NULL | 0 | AI分析状态（0-未分析，1-分析中，2-已完成，3-失败） |

**必要性分析：**
- 存储相关字段：支撑多种存储方式
- 处理状态字段：支撑图片处理流程跟踪
- AI分析字段：支撑图片智能分析功能

---

### 12. item_tags 表

**现有字段：**
- id, item_id, tag_name, created_at

**建议补充字段：**

| 字段名 | 数据类型 | 是否为空 | 默认值 | 用途说明 |
|--------|----------|----------|--------|----------|
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标识 |
| create_by | BIGINT | NULL | - | 创建人ID |
| tag_type | TINYINT | NOT NULL | 1 | 标签类型（1-系统标签，2-用户自定义） |
| tag_category | VARCHAR(50) | NULL | - | 标签分类 |
| weight | INT | NOT NULL | 1 | 标签权重（用于排序） |

**必要性分析：**
- 标签分类字段：支撑标签的分类管理
- 权重字段：支撑标签的优先级排序

---

### 13. admin_logs 表

**现有字段：**
- id, admin_id, operation, target_type, target_id, details, created_at, ip_address, user_agent

**建议补充字段：**

| 字段名 | 数据类型 | 是否为空 | 默认值 | 用途说明 |
|--------|----------|----------|--------|----------|
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标识 |
| log_type | TINYINT | NOT NULL | 1 | 日志类型（1-操作日志，2-安全日志，3-系统日志） |
| log_level | TINYINT | NOT NULL | 1 | 日志级别（1-INFO，2-WARN，3-ERROR） |
| request_url | VARCHAR(500) | NULL | - | 请求URL |
| request_method | VARCHAR(10) | NULL | - | 请求方法 |
| request_params | TEXT | NULL | - | 请求参数 |
| response_data | TEXT | NULL | - | 响应数据 |
| execution_time | INT | NULL | - | 执行时间（毫秒） |
| status | TINYINT | NOT NULL | 1 | 状态（1-成功，2-失败） |
| error_message | VARCHAR(1000) | NULL | - | 错误信息 |
| stack_trace | TEXT | NULL | - | 异常堆栈 |

**必要性分析：**
- 请求相关字段：支撑完整的请求追踪
- 性能相关字段：支撑接口性能分析
- 错误相关字段：支撑错误诊断

---

### 14. image_analysis 表

**现有字段：**
- id, image_url, item_id, analysis_result, item_type, brand, color
- confidence, status, error_message, created_at

**建议补充字段：**

| 字段名 | 数据类型 | 是否为空 | 默认值 | 用途说明 |
|--------|----------|----------|--------|----------|
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标识 |
| create_by | BIGINT | NULL | - | 创建人ID |
| update_by | BIGINT | NULL | - | 更新人ID |
| update_at | DATETIME | NULL | - | 更新时间 |
| analysis_type | TINYINT | NOT NULL | 1 | 分析类型（1-商品识别，2-内容审核） |
| model_version | VARCHAR(50) | NULL | - | AI模型版本 |
| processing_time | INT | NULL | - | 处理时间（毫秒） |
| raw_result | TEXT | NULL | - | 原始分析结果 |
| is_manual_reviewed | TINYINT(1) | NOT NULL | 0 | 是否人工复核 |
| reviewer_id | BIGINT | NULL | - | 复核人ID |
| review_result | TINYINT | NULL | - | 复核结果（1-通过，2-不通过） |
| review_remark | VARCHAR(500) | NULL | - | 复核备注 |
| is_used_for_training | TINYINT(1) | NOT NULL | 0 | 是否用于模型训练 |

**必要性分析：**
- 模型相关字段：支撑AI模型版本管理
- 复核相关字段：支撑人工复核流程
- 训练相关字段：支撑模型训练数据收集

---

## 二、新增表建议

### 1. user_addresses 表（用户地址表）

用于存储用户的收货地址信息。

| 字段名 | 数据类型 | 是否为空 | 默认值 | 用途说明 |
|--------|----------|----------|--------|----------|
| id | BIGINT | NOT NULL | AUTO_INCREMENT | 主键 |
| user_id | BIGINT | NOT NULL | - | 用户ID |
| receiver_name | VARCHAR(50) | NOT NULL | - | 收件人姓名 |
| receiver_phone | VARCHAR(20) | NOT NULL | - | 收件人电话 |
| province | VARCHAR(50) | NOT NULL | - | 省份 |
| city | VARCHAR(50) | NOT NULL | - | 城市 |
| district | VARCHAR(50) | NOT NULL | - | 区县 |
| detail_address | VARCHAR(200) | NOT NULL | - | 详细地址 |
| zip_code | VARCHAR(10) | NULL | - | 邮编 |
| is_default | TINYINT(1) | NOT NULL | 0 | 是否默认地址 |
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标识 |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | NULL | - | 更新时间 |

### 2. user_follows 表（用户关注表）

用于存储用户之间的关注关系。

| 字段名 | 数据类型 | 是否为空 | 默认值 | 用途说明 |
|--------|----------|----------|--------|----------|
| id | BIGINT | NOT NULL | AUTO_INCREMENT | 主键 |
| follower_id | BIGINT | NOT NULL | - | 关注者ID |
| following_id | BIGINT | NOT NULL | - | 被关注者ID |
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标识 |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |

### 3. notifications 表（消息通知表）

用于存储系统消息通知。

| 字段名 | 数据类型 | 是否为空 | 默认值 | 用途说明 |
|--------|----------|----------|--------|----------|
| id | BIGINT | NOT NULL | AUTO_INCREMENT | 主键 |
| user_id | BIGINT | NOT NULL | - | 接收用户ID |
| notification_type | TINYINT | NOT NULL | - | 通知类型 |
| title | VARCHAR(100) | NOT NULL | - | 通知标题 |
| content | VARCHAR(500) | NOT NULL | - | 通知内容 |
| related_id | BIGINT | NULL | - | 关联ID |
| related_type | VARCHAR(50) | NULL | - | 关联类型 |
| is_read | TINYINT(1) | NOT NULL | 0 | 是否已读 |
| read_time | DATETIME | NULL | - | 阅读时间 |
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标识 |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |

### 4. system_configs 表（系统配置表）

用于存储系统配置参数。

| 字段名 | 数据类型 | 是否为空 | 默认值 | 用途说明 |
|--------|----------|----------|--------|----------|
| id | BIGINT | NOT NULL | AUTO_INCREMENT | 主键 |
| config_key | VARCHAR(100) | NOT NULL | - | 配置键（唯一） |
| config_value | TEXT | NOT NULL | - | 配置值 |
| config_type | TINYINT | NOT NULL | 1 | 配置类型（1-字符串，2-数字，3-JSON） |
| description | VARCHAR(255) | NULL | - | 配置说明 |
| is_editable | TINYINT(1) | NOT NULL | 1 | 是否可编辑 |
| is_deleted | TINYINT(1) | NOT NULL | 0 | 逻辑删除标识 |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | NULL | - | 更新时间 |

### 5. operation_logs 表（操作日志表）

用于记录用户的操作行为。

| 字段名 | 数据类型 | 是否为空 | 默认值 | 用途说明 |
|--------|----------|----------|--------|----------|
| id | BIGINT | NOT NULL | AUTO_INCREMENT | 主键 |
| user_id | BIGINT | NULL | - | 用户ID |
| operation_type | VARCHAR(50) | NOT NULL | - | 操作类型 |
| operation_desc | VARCHAR(255) | NOT NULL | - | 操作描述 |
| request_url | VARCHAR(500) | NULL | - | 请求URL |
| request_method | VARCHAR(10) | NULL | - | 请求方法 |
| request_params | TEXT | NULL | - | 请求参数 |
| ip_address | VARCHAR(50) | NULL | - | IP地址 |
| user_agent | VARCHAR(500) | NULL | - | 用户代理 |
| execution_time | INT | NULL | - | 执行时间（毫秒） |
| status | TINYINT | NOT NULL | 1 | 状态（1-成功，2-失败） |
| error_message | VARCHAR(1000) | NULL | - | 错误信息 |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |

---

## 三、字段补充优先级

### 高优先级（必须补充）

1. **所有表的逻辑删除字段(is_deleted)**：支撑软删除功能，保留历史数据
2. **所有表的审计字段(create_by/update_by)**：支撑操作审计
3. **users表的信用相关字段**：支撑平台信用体系
4. **orders表的金额相关字段**：支撑完整的订单金额计算
5. **orders表的物流相关字段**：支撑物流跟踪

### 中优先级（建议补充）

1. **users表的登录相关字段**：支撑安全审计
2. **items表的议价相关字段**：支撑议价功能
3. **items表的推荐相关字段**：支撑商品推荐
4. **chats表的消息统计字段**：支撑未读消息管理
5. **verification_records表的扩展字段**：支撑更完整的实名认证

### 低优先级（可选补充）

1. **各表的SEO相关字段**：支撑搜索引擎优化
2. **image_analysis表的AI相关字段**：支撑AI功能扩展
3. **新增表（user_addresses等）**：支撑相关功能模块

---

## 四、数据库设计规范建议

### 1. 命名规范
- 表名使用小写字母，单词间用下划线分隔（如：user_addresses）
- 字段名使用小写字母，单词间用下划线分隔（如：created_at）
- 主键统一使用id
- 外键使用[表名]_id格式（如：user_id）

### 2. 字段类型规范
- 主键使用BIGINT
- 状态字段使用TINYINT
- 金额字段使用DECIMAL(10,2)
- 时间字段使用DATETIME
- 布尔字段使用TINYINT(1)
- 长文本使用TEXT
- JSON数据使用TEXT或JSON类型（MySQL 5.7+）

### 3. 索引建议
- 所有外键字段建立索引
- 经常查询的状态字段建立索引
- 时间字段建立索引（用于排序和范围查询）
- 组合查询字段建立联合索引

### 4. 约束建议
- 所有表必须有主键
- 外键必须建立约束（开发环境可禁用，生产环境建议启用）
- 唯一约束用于业务唯一性字段
- 非空约束用于必填字段

---

## 五、实施建议

### 1. 数据库迁移
- 使用Flyway或Liquibase进行数据库版本管理
- 编写增量迁移脚本，避免数据丢失
- 在生产环境执行前，先在测试环境验证

### 2. 代码适配
- 同步更新实体类，添加新字段
- 更新DTO和VO，支撑新字段的传输和展示
- 更新Service层，处理新字段的业务逻辑
- 更新前端页面，展示和编辑新字段

### 3. 数据初始化
- 对于新增字段，编写数据初始化脚本
- 对于状态类字段，设置合理的默认值
- 对于时间类字段，根据业务规则填充

---

## 六、总结

本报告对闲置物品校园交易平台的14个现有表进行了全面分析，识别出需要补充的字段共计100+个，并建议新增5个表。这些补充将显著提升系统的功能性、可维护性和可扩展性。

建议按照优先级分阶段实施，先完成高优先级的字段补充，确保核心功能的完整性，再逐步补充中低优先级的字段，完善系统功能。
