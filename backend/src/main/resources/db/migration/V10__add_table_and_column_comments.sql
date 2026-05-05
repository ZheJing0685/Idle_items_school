-- V10: 为所有表和字段添加中文注释
-- 闲置物品校园交易平台
-- 基于数据库实际列结构编写

-- ============================================
-- 用户表 (users)
-- ============================================
ALTER TABLE users COMMENT '用户信息表';

ALTER TABLE users MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID，主键';
ALTER TABLE users MODIFY COLUMN username VARCHAR(50) NOT NULL COMMENT '用户名，唯一标识';
ALTER TABLE users MODIFY COLUMN password VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密存储）';
ALTER TABLE users MODIFY COLUMN email VARCHAR(100) NOT NULL COMMENT '电子邮箱，唯一标识';
ALTER TABLE users MODIFY COLUMN phone VARCHAR(20) NOT NULL COMMENT '手机号，唯一标识';
ALTER TABLE users MODIFY COLUMN nickname VARCHAR(50) NOT NULL COMMENT '用户昵称';
ALTER TABLE users MODIFY COLUMN avatar VARCHAR(255) COMMENT '用户头像URL';
ALTER TABLE users MODIFY COLUMN role ENUM('STUDENT','ADMIN') DEFAULT 'STUDENT' COMMENT '用户角色：STUDENT-学生，ADMIN-管理员';
ALTER TABLE users MODIFY COLUMN status ENUM('ACTIVE','DISABLED') DEFAULT 'ACTIVE' COMMENT '账号状态：ACTIVE-激活，DISABLED-禁用';
ALTER TABLE users MODIFY COLUMN verified TINYINT NOT NULL DEFAULT 0 COMMENT '是否已实名认证：0-未认证，1-已认证';
ALTER TABLE users MODIFY COLUMN student_id VARCHAR(20) COMMENT '学号';
ALTER TABLE users MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE users MODIFY COLUMN updated_at DATETIME COMMENT '更新时间';
ALTER TABLE users MODIFY COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除';
ALTER TABLE users MODIFY COLUMN create_by BIGINT COMMENT '创建人ID';
ALTER TABLE users MODIFY COLUMN update_by BIGINT COMMENT '更新人ID';
ALTER TABLE users MODIFY COLUMN last_login_time DATETIME COMMENT '最后登录时间';
ALTER TABLE users MODIFY COLUMN last_login_ip VARCHAR(50) COMMENT '最后登录IP地址';
ALTER TABLE users MODIFY COLUMN login_count INT NOT NULL DEFAULT 0 COMMENT '登录次数';
ALTER TABLE users MODIFY COLUMN credit_score INT NOT NULL DEFAULT 100 COMMENT '信用评分（1-100）';
ALTER TABLE users MODIFY COLUMN total_transactions INT NOT NULL DEFAULT 0 COMMENT '总交易次数';
ALTER TABLE users MODIFY COLUMN total_sales INT NOT NULL DEFAULT 0 COMMENT '总售出次数';
ALTER TABLE users MODIFY COLUMN total_purchases INT NOT NULL DEFAULT 0 COMMENT '总购买次数';
ALTER TABLE users MODIFY COLUMN gender TINYINT COMMENT '性别：0-未知，1-男，2-女';
ALTER TABLE users MODIFY COLUMN birthday DATE COMMENT '出生日期';
ALTER TABLE users MODIFY COLUMN bio VARCHAR(500) COMMENT '个人简介';
ALTER TABLE users MODIFY COLUMN school_name VARCHAR(100) COMMENT '学校名称';

-- ============================================
-- 商品分类表 (categories)
-- ============================================
ALTER TABLE categories COMMENT '商品分类表';

ALTER TABLE categories MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID，主键';
ALTER TABLE categories MODIFY COLUMN name VARCHAR(50) NOT NULL COMMENT '分类名称';
ALTER TABLE categories MODIFY COLUMN parent_id BIGINT COMMENT '父分类ID，NULL表示顶级分类';
ALTER TABLE categories MODIFY COLUMN sort_order INT NOT NULL DEFAULT 0 COMMENT '排序顺序，数字越小越靠前';
ALTER TABLE categories MODIFY COLUMN icon VARCHAR(255) COMMENT '分类图标URL';
ALTER TABLE categories MODIFY COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除';
ALTER TABLE categories MODIFY COLUMN create_by BIGINT COMMENT '创建人ID';
ALTER TABLE categories MODIFY COLUMN update_by BIGINT COMMENT '更新人ID';
ALTER TABLE categories MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE categories MODIFY COLUMN updated_at DATETIME COMMENT '更新时间';

-- ============================================
-- 商品信息表 (items)
-- ============================================
ALTER TABLE items COMMENT '商品信息表';

ALTER TABLE items MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID，主键';
ALTER TABLE items MODIFY COLUMN user_id BIGINT NOT NULL COMMENT '卖家用户ID';
ALTER TABLE items MODIFY COLUMN category_id BIGINT NOT NULL COMMENT '所属分类ID';
ALTER TABLE items MODIFY COLUMN title VARCHAR(100) NOT NULL COMMENT '商品标题';
ALTER TABLE items MODIFY COLUMN description TEXT COMMENT '商品详细描述';
ALTER TABLE items MODIFY COLUMN price DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商品售价';
ALTER TABLE items MODIFY COLUMN original_price DECIMAL(10,2) COMMENT '商品原价';
ALTER TABLE items MODIFY COLUMN item_condition VARCHAR(16) COMMENT '商品新旧程度：NEW-全新，LIKE_NEW-几乎全新，GOOD-良好，FAIR-一般，POOR-较差';
ALTER TABLE items MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '商品状态：DRAFT-草稿，PENDING-待审核，ON_SALE-在售，SOLD-已售出，OFF_SHELF-已下架，REJECTED-审核拒绝';
ALTER TABLE items MODIFY COLUMN view_count INT NOT NULL DEFAULT 0 COMMENT '浏览次数';
ALTER TABLE items MODIFY COLUMN favorite_count INT NOT NULL DEFAULT 0 COMMENT '收藏次数';
ALTER TABLE items MODIFY COLUMN reject_reason VARCHAR(500) COMMENT '审核拒绝原因';
ALTER TABLE items MODIFY COLUMN location VARCHAR(100) COMMENT '商品所在位置';
ALTER TABLE items MODIFY COLUMN contact_name VARCHAR(50) COMMENT '联系人姓名';
ALTER TABLE items MODIFY COLUMN contact_phone VARCHAR(20) COMMENT '联系人电话';
ALTER TABLE items MODIFY COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除';
ALTER TABLE items MODIFY COLUMN create_by BIGINT COMMENT '创建人ID';
ALTER TABLE items MODIFY COLUMN update_by BIGINT COMMENT '更新人ID';
ALTER TABLE items MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE items MODIFY COLUMN updated_at DATETIME COMMENT '更新时间';
ALTER TABLE items MODIFY COLUMN publish_time DATETIME COMMENT '发布时间';
ALTER TABLE items MODIFY COLUMN off_shelf_time DATETIME COMMENT '下架时间';
ALTER TABLE items MODIFY COLUMN tags VARCHAR(500) COMMENT '商品标签，JSON格式存储';
ALTER TABLE items MODIFY COLUMN brand VARCHAR(100) COMMENT '品牌名称';
ALTER TABLE items MODIFY COLUMN purchase_date DATE COMMENT '购买日期';
ALTER TABLE items MODIFY COLUMN warranty_info VARCHAR(255) COMMENT '保修信息';
ALTER TABLE items MODIFY COLUMN delivery_method TINYINT NOT NULL DEFAULT 0 COMMENT '配送方式：0-自提，1-快递，2-面交';
ALTER TABLE items MODIFY COLUMN weight INT NOT NULL DEFAULT 0 COMMENT '商品重量（克）';
ALTER TABLE items MODIFY COLUMN is_recommended TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否推荐：0-否，1-是';
ALTER TABLE items MODIFY COLUMN recommend_time DATETIME COMMENT '推荐时间';
ALTER TABLE items MODIFY COLUMN cover_image VARCHAR(500) COMMENT '封面图片URL';
ALTER TABLE items MODIFY COLUMN images TEXT COMMENT '商品图片列表，JSON格式';

-- ============================================
-- 订单信息表 (orders)
-- ============================================
ALTER TABLE orders COMMENT '订单信息表';

ALTER TABLE orders MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID，主键';
ALTER TABLE orders MODIFY COLUMN order_no VARCHAR(50) NOT NULL COMMENT '订单编号，唯一标识';
ALTER TABLE orders MODIFY COLUMN buyer_id BIGINT NOT NULL COMMENT '买家用户ID';
ALTER TABLE orders MODIFY COLUMN seller_id BIGINT NOT NULL COMMENT '卖家用户ID';
ALTER TABLE orders MODIFY COLUMN item_id BIGINT NOT NULL COMMENT '商品ID';
ALTER TABLE orders MODIFY COLUMN item_title VARCHAR(100) NOT NULL COMMENT '商品标题（冗余存储）';
ALTER TABLE orders MODIFY COLUMN item_image VARCHAR(255) NOT NULL COMMENT '商品图片URL（冗余存储）';
ALTER TABLE orders MODIFY COLUMN price DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商品单价';
ALTER TABLE orders MODIFY COLUMN order_status VARCHAR(30) NOT NULL DEFAULT 'PENDING_PAYMENT' COMMENT '订单状态：PENDING_PAYMENT-待付款，PENDING_SHIPMENT-待发货，SHIPPED-已发货，COMPLETED-已完成，CANCELLED-已取消，REFUND_REQUESTED-退款中，REFUNDED-已退款';
ALTER TABLE orders MODIFY COLUMN total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额';
ALTER TABLE orders MODIFY COLUMN shipping_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '运费金额';
ALTER TABLE orders MODIFY COLUMN discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额';
ALTER TABLE orders MODIFY COLUMN pay_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '实际支付金额';
ALTER TABLE orders MODIFY COLUMN transaction_id VARCHAR(100) COMMENT '支付交易号';
ALTER TABLE orders MODIFY COLUMN buyer_address VARCHAR(200) COMMENT '买家收货地址';
ALTER TABLE orders MODIFY COLUMN buyer_phone VARCHAR(20) COMMENT '买家联系电话';
ALTER TABLE orders MODIFY COLUMN buyer_name VARCHAR(50) COMMENT '买家收货人姓名';
ALTER TABLE orders MODIFY COLUMN payment_method VARCHAR(50) COMMENT '支付方式：OFFLINE-线下交易，WECHAT_PAY-微信支付，ALIPAY-支付宝';
ALTER TABLE orders MODIFY COLUMN payment_time DATETIME COMMENT '支付时间';
ALTER TABLE orders MODIFY COLUMN payment_status TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态：0-未支付，1-已支付';
ALTER TABLE orders MODIFY COLUMN shipping_status TINYINT NOT NULL DEFAULT 0 COMMENT '发货状态：0-未发货，1-已发货';
ALTER TABLE orders MODIFY COLUMN ship_time DATETIME COMMENT '发货时间';
ALTER TABLE orders MODIFY COLUMN tracking_no VARCHAR(50) COMMENT '物流单号';
ALTER TABLE orders MODIFY COLUMN shipping_company VARCHAR(50) COMMENT '快递公司名称';
ALTER TABLE orders MODIFY COLUMN deliver_time DATETIME COMMENT '确认收货时间';
ALTER TABLE orders MODIFY COLUMN complete_time DATETIME COMMENT '订单完成时间';
ALTER TABLE orders MODIFY COLUMN cancel_reason VARCHAR(500) COMMENT '取消订单原因';
ALTER TABLE orders MODIFY COLUMN refund_reason VARCHAR(500) COMMENT '退款原因';
ALTER TABLE orders MODIFY COLUMN refund_time DATETIME COMMENT '退款时间';
ALTER TABLE orders MODIFY COLUMN refund_amount DECIMAL(10,2) COMMENT '退款金额';
ALTER TABLE orders MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE orders MODIFY COLUMN updated_at DATETIME COMMENT '更新时间';
ALTER TABLE orders MODIFY COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除';
ALTER TABLE orders MODIFY COLUMN create_by BIGINT COMMENT '创建人ID';
ALTER TABLE orders MODIFY COLUMN update_by BIGINT COMMENT '更新人ID';
ALTER TABLE orders MODIFY COLUMN seller_note VARCHAR(500) COMMENT '卖家备注';
ALTER TABLE orders MODIFY COLUMN buyer_note VARCHAR(500) COMMENT '买家备注';
ALTER TABLE orders MODIFY COLUMN auto_confirm_time DATETIME COMMENT '自动确认收货时间';
ALTER TABLE orders MODIFY COLUMN close_time DATETIME COMMENT '订单关闭时间';
ALTER TABLE orders MODIFY COLUMN close_type TINYINT COMMENT '关闭类型：1-超时关闭，2-用户取消，3-管理员关闭';
ALTER TABLE orders MODIFY COLUMN source TINYINT NOT NULL DEFAULT 0 COMMENT '订单来源：0-正常下单，1-活动订单';
ALTER TABLE orders MODIFY COLUMN ip_address VARCHAR(50) COMMENT '下单IP地址';
ALTER TABLE orders MODIFY COLUMN user_agent VARCHAR(500) COMMENT '下单浏览器User-Agent';
ALTER TABLE orders MODIFY COLUMN tracking_number VARCHAR(100) COMMENT '物流单号（备用字段）';

-- ============================================
-- 实名认证记录表 (verification_records)
-- ============================================
ALTER TABLE verification_records COMMENT '用户实名认证记录表';

ALTER TABLE verification_records MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '认证记录ID，主键';
ALTER TABLE verification_records MODIFY COLUMN user_id BIGINT NOT NULL COMMENT '用户ID';
ALTER TABLE verification_records MODIFY COLUMN real_name VARCHAR(50) NOT NULL COMMENT '真实姓名';
ALTER TABLE verification_records MODIFY COLUMN student_id VARCHAR(20) NOT NULL COMMENT '学号';
ALTER TABLE verification_records MODIFY COLUMN id_card VARCHAR(18) COMMENT '身份证号码';
ALTER TABLE verification_records MODIFY COLUMN teacher_id VARCHAR(50) COMMENT '教师证号';
ALTER TABLE verification_records MODIFY COLUMN student_card VARCHAR(255) NOT NULL COMMENT '学生证照片URL';
ALTER TABLE verification_records MODIFY COLUMN teacher_card VARCHAR(255) COMMENT '教师证照片URL';
ALTER TABLE verification_records MODIFY COLUMN status ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING' COMMENT '认证状态：PENDING-待审核，APPROVED-已通过，REJECTED-已拒绝';
ALTER TABLE verification_records MODIFY COLUMN reject_reason VARCHAR(255) COMMENT '拒绝原因';
ALTER TABLE verification_records MODIFY COLUMN reviewer_id BIGINT COMMENT '审核人ID';
ALTER TABLE verification_records MODIFY COLUMN reviewed_at DATETIME COMMENT '审核时间';
ALTER TABLE verification_records MODIFY COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE verification_records MODIFY COLUMN updated_at DATETIME COMMENT '更新时间';
ALTER TABLE verification_records MODIFY COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除';
ALTER TABLE verification_records MODIFY COLUMN create_by BIGINT COMMENT '创建人ID';
ALTER TABLE verification_records MODIFY COLUMN update_by BIGINT COMMENT '更新人ID';
ALTER TABLE verification_records MODIFY COLUMN school VARCHAR(100) COMMENT '学校名称';
ALTER TABLE verification_records MODIFY COLUMN department VARCHAR(100) COMMENT '院系';
ALTER TABLE verification_records MODIFY COLUMN major VARCHAR(100) COMMENT '专业';
ALTER TABLE verification_records MODIFY COLUMN enrollment_year INT COMMENT '入学年份';
ALTER TABLE verification_records MODIFY COLUMN graduation_year INT COMMENT '毕业年份';
ALTER TABLE verification_records MODIFY COLUMN student_card_back_image VARCHAR(255) COMMENT '学生证反面照片URL';
ALTER TABLE verification_records MODIFY COLUMN id_card_front VARCHAR(255) COMMENT '身份证正面照片URL';
ALTER TABLE verification_records MODIFY COLUMN id_card_back VARCHAR(255) COMMENT '身份证反面照片URL';
ALTER TABLE verification_records MODIFY COLUMN type ENUM('ID_CARD','STUDENT_CARD','TEACHER_CARD') NOT NULL COMMENT '认证类型：ID_CARD-身份证认证，STUDENT_CARD-学生证认证，TEACHER_CARD-教师证认证';
ALTER TABLE verification_records MODIFY COLUMN face_recognition_image VARCHAR(255) COMMENT '人脸识别照片URL';
ALTER TABLE verification_records MODIFY COLUMN face_recognition_passED TINYINT(1) COMMENT '人脸识别是否通过：0-未通过，1-通过，NULL-未进行';
ALTER TABLE verification_records MODIFY COLUMN submit_count INT NOT NULL DEFAULT 1 COMMENT '提交次数';
ALTER TABLE verification_records MODIFY COLUMN last_submit_time DATETIME COMMENT '最后提交时间';
ALTER TABLE verification_records MODIFY COLUMN review_remark VARCHAR(500) COMMENT '审核备注';
ALTER TABLE verification_records MODIFY COLUMN auto_approved TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否自动通过：0-否，1-是';
ALTER TABLE verification_records MODIFY COLUMN risk_level TINYINT NOT NULL DEFAULT 0 COMMENT '风险等级：0-低风险，1-中风险，2-高风险';

-- ============================================
-- 商品评价表 (reviews)
-- ============================================
ALTER TABLE reviews COMMENT '商品评价表';

ALTER TABLE reviews MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价ID，主键';
ALTER TABLE reviews MODIFY COLUMN order_id BIGINT NOT NULL COMMENT '订单ID';
ALTER TABLE reviews MODIFY COLUMN reviewer_id BIGINT NOT NULL COMMENT '评价人ID';
ALTER TABLE reviews MODIFY COLUMN reviewed_user_id BIGINT NOT NULL COMMENT '被评价人ID';
ALTER TABLE reviews MODIFY COLUMN item_id BIGINT NOT NULL COMMENT '商品ID';
ALTER TABLE reviews MODIFY COLUMN rating INT NOT NULL DEFAULT 5 COMMENT '评分，1-5分';
ALTER TABLE reviews MODIFY COLUMN content TEXT COMMENT '评价内容';
ALTER TABLE reviews MODIFY COLUMN images TEXT COMMENT '评价图片，多个用逗号分隔';
ALTER TABLE reviews MODIFY COLUMN is_anonymous TINYINT(1) DEFAULT 0 COMMENT '是否匿名：0-实名，1-匿名';
ALTER TABLE reviews MODIFY COLUMN created_at DATETIME COMMENT '创建时间';
ALTER TABLE reviews MODIFY COLUMN updated_at DATETIME COMMENT '更新时间';
ALTER TABLE reviews MODIFY COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除';
ALTER TABLE reviews MODIFY COLUMN create_by BIGINT COMMENT '创建人ID';
ALTER TABLE reviews MODIFY COLUMN update_by BIGINT COMMENT '更新人ID';
ALTER TABLE reviews MODIFY COLUMN reply_content VARCHAR(1000) COMMENT '卖家回复内容';
ALTER TABLE reviews MODIFY COLUMN reply_time DATETIME COMMENT '卖家回复时间';
ALTER TABLE reviews MODIFY COLUMN is_show TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否显示：0-隐藏，1-显示';
ALTER TABLE reviews MODIFY COLUMN helpful_count INT NOT NULL DEFAULT 0 COMMENT '有帮助次数';
ALTER TABLE reviews MODIFY COLUMN report_count INT NOT NULL DEFAULT 0 COMMENT '举报次数';
ALTER TABLE reviews MODIFY COLUMN is_reported TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否被举报：0-否，1-是';
ALTER TABLE reviews MODIFY COLUMN report_reason VARCHAR(255) COMMENT '举报原因';
ALTER TABLE reviews MODIFY COLUMN check_status TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态：0-待审核，1-通过，2-拒绝';
ALTER TABLE reviews MODIFY COLUMN check_time DATETIME COMMENT '审核时间';
ALTER TABLE reviews MODIFY COLUMN check_remark VARCHAR(255) COMMENT '审核备注';
ALTER TABLE reviews MODIFY COLUMN tag VARCHAR(100) COMMENT '评价标签';

-- ============================================
-- 商品收藏表 (favorites)
-- ============================================
ALTER TABLE favorites COMMENT '商品收藏表';

ALTER TABLE favorites MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏ID，主键';
ALTER TABLE favorites MODIFY COLUMN user_id BIGINT NOT NULL COMMENT '用户ID';
ALTER TABLE favorites MODIFY COLUMN item_id BIGINT NOT NULL COMMENT '商品ID';
ALTER TABLE favorites MODIFY COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除';
ALTER TABLE favorites MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间';

-- ============================================
-- 聊天会话表 (chats)
-- ============================================
ALTER TABLE chats COMMENT '聊天会话表';

ALTER TABLE chats MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '会话ID，主键';
ALTER TABLE chats MODIFY COLUMN order_id BIGINT COMMENT '关联订单ID（可选）';
ALTER TABLE chats MODIFY COLUMN item_id BIGINT COMMENT '关联商品ID（可选）';
ALTER TABLE chats MODIFY COLUMN buyer_id BIGINT NOT NULL COMMENT '买家用户ID';
ALTER TABLE chats MODIFY COLUMN seller_id BIGINT NOT NULL COMMENT '卖家用户ID';
ALTER TABLE chats MODIFY COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除';
ALTER TABLE chats MODIFY COLUMN create_by BIGINT COMMENT '创建人ID';
ALTER TABLE chats MODIFY COLUMN update_by BIGINT COMMENT '更新人ID';
ALTER TABLE chats MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE chats MODIFY COLUMN updated_at DATETIME COMMENT '更新时间';

-- ============================================
-- 聊天消息表 (chat_messages)
-- ============================================
ALTER TABLE chat_messages COMMENT '聊天消息表';

ALTER TABLE chat_messages MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID，主键';
ALTER TABLE chat_messages MODIFY COLUMN chat_id BIGINT NOT NULL COMMENT '所属会话ID';
ALTER TABLE chat_messages MODIFY COLUMN sender_id BIGINT NOT NULL COMMENT '发送者用户ID';
ALTER TABLE chat_messages MODIFY COLUMN receiver_id BIGINT NOT NULL COMMENT '接收者用户ID';
ALTER TABLE chat_messages MODIFY COLUMN message_type VARCHAR(20) DEFAULT 'TEXT' COMMENT '消息类型：TEXT-文本，IMAGE-图片，SYSTEM-系统';
ALTER TABLE chat_messages MODIFY COLUMN content VARCHAR(1000) COMMENT '消息内容';
ALTER TABLE chat_messages MODIFY COLUMN is_read TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读';
ALTER TABLE chat_messages MODIFY COLUMN read_at DATETIME COMMENT '阅读时间';
ALTER TABLE chat_messages MODIFY COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除';
ALTER TABLE chat_messages MODIFY COLUMN create_by BIGINT COMMENT '创建人ID';
ALTER TABLE chat_messages MODIFY COLUMN update_by BIGINT COMMENT '更新人ID';
ALTER TABLE chat_messages MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间';

-- ============================================
-- 交易纠纷表 (disputes)
-- ============================================
ALTER TABLE disputes COMMENT '交易纠纷表';

ALTER TABLE disputes MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '纠纷ID，主键';
ALTER TABLE disputes MODIFY COLUMN order_id BIGINT NOT NULL COMMENT '关联订单ID';
ALTER TABLE disputes MODIFY COLUMN dispute_no VARCHAR(50) NOT NULL COMMENT '纠纷编号，唯一标识';
ALTER TABLE disputes MODIFY COLUMN applicant_id BIGINT NOT NULL COMMENT '申请人用户ID';
ALTER TABLE disputes MODIFY COLUMN respondent_id BIGINT NOT NULL COMMENT '被申请人用户ID';
ALTER TABLE disputes MODIFY COLUMN reason VARCHAR(255) NOT NULL COMMENT '纠纷原因分类';
ALTER TABLE disputes MODIFY COLUMN description TEXT NOT NULL COMMENT '纠纷详细描述';
ALTER TABLE disputes MODIFY COLUMN evidence_images TEXT COMMENT '证据图片，多个用逗号分隔';
ALTER TABLE disputes MODIFY COLUMN dispute_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '纠纷状态：PENDING-待处理，PROCESSING-处理中，RESOLVED-已解决，CLOSED-已关闭';
ALTER TABLE disputes MODIFY COLUMN handler_id BIGINT COMMENT '处理人ID（管理员）';
ALTER TABLE disputes MODIFY COLUMN result TEXT COMMENT '处理结果';
ALTER TABLE disputes MODIFY COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除';
ALTER TABLE disputes MODIFY COLUMN create_by BIGINT COMMENT '创建人ID';
ALTER TABLE disputes MODIFY COLUMN update_by BIGINT COMMENT '更新人ID';
ALTER TABLE disputes MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE disputes MODIFY COLUMN updated_at DATETIME COMMENT '更新时间';

-- ============================================
-- 商品图片表 (item_images)
-- ============================================
ALTER TABLE item_images COMMENT '商品图片表';

ALTER TABLE item_images MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '图片ID，主键';
ALTER TABLE item_images MODIFY COLUMN item_id BIGINT NOT NULL COMMENT '所属商品ID';
ALTER TABLE item_images MODIFY COLUMN image_url VARCHAR(255) NOT NULL COMMENT '原图URL';
ALTER TABLE item_images MODIFY COLUMN thumbnail_url VARCHAR(255) NOT NULL COMMENT '缩略图URL';
ALTER TABLE item_images MODIFY COLUMN is_cover TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否封面图：0-否，1-是';
ALTER TABLE item_images MODIFY COLUMN sort_order INT NOT NULL DEFAULT 0 COMMENT '排序顺序';
ALTER TABLE item_images MODIFY COLUMN width INT NOT NULL DEFAULT 0 COMMENT '图片宽度（像素）';
ALTER TABLE item_images MODIFY COLUMN height INT NOT NULL DEFAULT 0 COMMENT '图片高度（像素）';
ALTER TABLE item_images MODIFY COLUMN file_size BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小（字节）';
ALTER TABLE item_images MODIFY COLUMN format VARCHAR(10) NOT NULL COMMENT '图片格式：jpg、png、webp等';
ALTER TABLE item_images MODIFY COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除';
ALTER TABLE item_images MODIFY COLUMN create_by BIGINT COMMENT '创建人ID';
ALTER TABLE item_images MODIFY COLUMN update_by BIGINT COMMENT '更新人ID';
ALTER TABLE item_images MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间';
ALTER TABLE item_images MODIFY COLUMN updated_at DATETIME COMMENT '更新时间';

-- ============================================
-- 商品标签表 (item_tags)
-- ============================================
ALTER TABLE item_tags COMMENT '商品标签表';

ALTER TABLE item_tags MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '标签ID，主键';
ALTER TABLE item_tags MODIFY COLUMN item_id BIGINT NOT NULL COMMENT '所属商品ID';
ALTER TABLE item_tags MODIFY COLUMN tag_name VARCHAR(50) NOT NULL COMMENT '标签名称';
ALTER TABLE item_tags MODIFY COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除';
ALTER TABLE item_tags MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';

-- ============================================
-- 管理员操作日志表 (admin_logs)
-- ============================================
ALTER TABLE admin_logs COMMENT '管理员操作日志表';

ALTER TABLE admin_logs MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID，主键';
ALTER TABLE admin_logs MODIFY COLUMN admin_id BIGINT NOT NULL COMMENT '管理员用户ID';
ALTER TABLE admin_logs MODIFY COLUMN operation VARCHAR(100) NOT NULL COMMENT '操作类型';
ALTER TABLE admin_logs MODIFY COLUMN target_type VARCHAR(50) NOT NULL COMMENT '操作对象类型';
ALTER TABLE admin_logs MODIFY COLUMN target_id BIGINT COMMENT '操作对象ID';
ALTER TABLE admin_logs MODIFY COLUMN details TEXT COMMENT '操作详情描述';
ALTER TABLE admin_logs MODIFY COLUMN ip_address VARCHAR(50) COMMENT '操作者IP地址';
ALTER TABLE admin_logs MODIFY COLUMN user_agent VARCHAR(500) COMMENT '浏览器User-Agent';
ALTER TABLE admin_logs MODIFY COLUMN status TINYINT NOT NULL DEFAULT 1 COMMENT '操作状态：1-成功，0-失败';
ALTER TABLE admin_logs MODIFY COLUMN error_message VARCHAR(1000) COMMENT '错误信息';
ALTER TABLE admin_logs MODIFY COLUMN created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间';

-- ============================================
-- 图片分析记录表 (image_analysis)
-- ============================================
ALTER TABLE image_analysis COMMENT '商品图片AI分析记录表';

ALTER TABLE image_analysis MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID，主键';
ALTER TABLE image_analysis MODIFY COLUMN image_url VARCHAR(255) NOT NULL COMMENT '图片URL';
ALTER TABLE image_analysis MODIFY COLUMN item_id BIGINT COMMENT '关联商品ID';
ALTER TABLE image_analysis MODIFY COLUMN analysis_result TEXT COMMENT 'AI分析结果（JSON格式）';
ALTER TABLE image_analysis MODIFY COLUMN item_type VARCHAR(50) COMMENT '识别出的物品类型';
ALTER TABLE image_analysis MODIFY COLUMN brand VARCHAR(100) COMMENT '识别出的品牌';
ALTER TABLE image_analysis MODIFY COLUMN color VARCHAR(50) COMMENT '识别出的颜色';
ALTER TABLE image_analysis MODIFY COLUMN confidence DECIMAL(5,4) COMMENT '识别置信度（0-1之间）';
ALTER TABLE image_analysis MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '分析状态：PENDING-待处理，SUCCESS-成功，FAILED-失败';
ALTER TABLE image_analysis MODIFY COLUMN error_message VARCHAR(500) COMMENT '错误信息';
ALTER TABLE image_analysis MODIFY COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除';
ALTER TABLE image_analysis MODIFY COLUMN create_by BIGINT COMMENT '创建人ID';
ALTER TABLE image_analysis MODIFY COLUMN update_by BIGINT COMMENT '更新人ID';
ALTER TABLE image_analysis MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE image_analysis MODIFY COLUMN updated_at DATETIME COMMENT '更新时间';

-- ============================================
-- 通知消息表 (notifications)
-- ============================================
ALTER TABLE notifications COMMENT '通知消息表';

ALTER TABLE notifications MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID，主键';
ALTER TABLE notifications MODIFY COLUMN user_id BIGINT NOT NULL COMMENT '接收用户ID';
ALTER TABLE notifications MODIFY COLUMN notification_type TINYINT NOT NULL COMMENT '通知类型：1-系统通知，2-订单通知，3-商品通知，4-互动通知';
ALTER TABLE notifications MODIFY COLUMN title VARCHAR(100) NOT NULL COMMENT '通知标题';
ALTER TABLE notifications MODIFY COLUMN content VARCHAR(500) NOT NULL COMMENT '通知内容';
ALTER TABLE notifications MODIFY COLUMN related_id BIGINT COMMENT '关联ID（订单ID/商品ID等）';
ALTER TABLE notifications MODIFY COLUMN related_type VARCHAR(50) COMMENT '关联类型：ORDER/ITEM/REVIEW等';
ALTER TABLE notifications MODIFY COLUMN is_read TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读';
ALTER TABLE notifications MODIFY COLUMN read_time DATETIME COMMENT '阅读时间';
ALTER TABLE notifications MODIFY COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除';
ALTER TABLE notifications MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';

-- ============================================
-- 操作日志表 (operation_logs)
-- ============================================
ALTER TABLE operation_logs COMMENT '操作日志表';

ALTER TABLE operation_logs MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID，主键';
ALTER TABLE operation_logs MODIFY COLUMN user_id BIGINT COMMENT '操作用户ID';
ALTER TABLE operation_logs MODIFY COLUMN operation_type VARCHAR(50) NOT NULL COMMENT '操作类型';
ALTER TABLE operation_logs MODIFY COLUMN operation_desc VARCHAR(255) NOT NULL COMMENT '操作描述';
ALTER TABLE operation_logs MODIFY COLUMN request_url VARCHAR(500) COMMENT '请求URL';
ALTER TABLE operation_logs MODIFY COLUMN request_method VARCHAR(10) COMMENT '请求方法：GET、POST等';
ALTER TABLE operation_logs MODIFY COLUMN request_params TEXT COMMENT '请求参数（JSON格式）';
ALTER TABLE operation_logs MODIFY COLUMN ip_address VARCHAR(50) COMMENT '操作者IP地址';
ALTER TABLE operation_logs MODIFY COLUMN user_agent VARCHAR(500) COMMENT '浏览器User-Agent';
ALTER TABLE operation_logs MODIFY COLUMN execution_time INT COMMENT '执行时间（毫秒）';
ALTER TABLE operation_logs MODIFY COLUMN status TINYINT NOT NULL COMMENT '操作状态：1-成功，0-失败';
ALTER TABLE operation_logs MODIFY COLUMN error_message VARCHAR(1000) COMMENT '错误信息';
ALTER TABLE operation_logs MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';

-- ============================================
-- 系统配置表 (system_configs)
-- ============================================
ALTER TABLE system_configs COMMENT '系统配置表';

ALTER TABLE system_configs MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID，主键';
ALTER TABLE system_configs MODIFY COLUMN config_key VARCHAR(100) NOT NULL COMMENT '配置键';
ALTER TABLE system_configs MODIFY COLUMN config_value TEXT NOT NULL COMMENT '配置值';
ALTER TABLE system_configs MODIFY COLUMN config_type TINYINT NOT NULL COMMENT '配置类型：1-系统配置，2-业务配置';
ALTER TABLE system_configs MODIFY COLUMN description VARCHAR(255) COMMENT '配置描述';
ALTER TABLE system_configs MODIFY COLUMN is_editable TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否可编辑：0-否，1-是';
ALTER TABLE system_configs MODIFY COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除';
ALTER TABLE system_configs MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE system_configs MODIFY COLUMN updated_at DATETIME COMMENT '更新时间';

-- ============================================
-- 用户收货地址表 (user_addresses)
-- ============================================
ALTER TABLE user_addresses COMMENT '用户收货地址表';

ALTER TABLE user_addresses MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '地址ID，主键';
ALTER TABLE user_addresses MODIFY COLUMN user_id BIGINT NOT NULL COMMENT '用户ID';
ALTER TABLE user_addresses MODIFY COLUMN receiver_name VARCHAR(50) NOT NULL COMMENT '收货人姓名';
ALTER TABLE user_addresses MODIFY COLUMN receiver_phone VARCHAR(20) NOT NULL COMMENT '收货人电话';
ALTER TABLE user_addresses MODIFY COLUMN province VARCHAR(50) NOT NULL COMMENT '省份';
ALTER TABLE user_addresses MODIFY COLUMN city VARCHAR(50) NOT NULL COMMENT '城市';
ALTER TABLE user_addresses MODIFY COLUMN district VARCHAR(50) NOT NULL COMMENT '区县';
ALTER TABLE user_addresses MODIFY COLUMN detail_address VARCHAR(200) NOT NULL COMMENT '详细地址';
ALTER TABLE user_addresses MODIFY COLUMN zip_code VARCHAR(10) COMMENT '邮政编码';
ALTER TABLE user_addresses MODIFY COLUMN is_default TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否默认地址：0-否，1-是';
ALTER TABLE user_addresses MODIFY COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除';
ALTER TABLE user_addresses MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE user_addresses MODIFY COLUMN updated_at DATETIME COMMENT '更新时间';

-- ============================================
-- 用户关注表 (user_follows)
-- ============================================
ALTER TABLE user_follows COMMENT '用户关注表';

ALTER TABLE user_follows MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '关注记录ID，主键';
ALTER TABLE user_follows MODIFY COLUMN follower_id BIGINT NOT NULL COMMENT '关注者用户ID';
ALTER TABLE user_follows MODIFY COLUMN following_id BIGINT NOT NULL COMMENT '被关注者用户ID';
ALTER TABLE user_follows MODIFY COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除';
ALTER TABLE user_follows MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间';
