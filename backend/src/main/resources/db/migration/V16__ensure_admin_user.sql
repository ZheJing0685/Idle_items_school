-- ==========================================================================
-- 确保管理员用户存在且配置正确
-- 使用 ON DUPLICATE KEY UPDATE 实现幂等性
-- ==========================================================================
-- !! 安全警告 !!
-- 以下密码哈希对应明文密码 'admin123'，仅用于开发/测试环境初始化。
-- 部署到生产环境前，必须：
--   1. 使用 BCrypt 生成新的密码哈希替换下方哈希值
--   2. 或在首次登录后立即修改管理员密码
--   3. 确保通过环境变量或密钥管理服务注入凭据
-- ==========================================================================

INSERT INTO users (username, password, email, phone, nickname, avatar, role, status, verified)
VALUES (
    'admin',
    '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW',
    'admin@example.com',
    '13800138000',
    '管理员',
    '/uploads/avatars/admin.png',
    'ADMIN',
    'ACTIVE',
    1
)
ON DUPLICATE KEY UPDATE
    role = 'ADMIN',
    status = 'ACTIVE',
    password = VALUES(password),
    verified = 1;
