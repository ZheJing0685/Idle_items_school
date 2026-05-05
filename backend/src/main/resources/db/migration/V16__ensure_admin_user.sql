-- 确保管理员用户存在且配置正确
-- 使用 ON DUPLICATE KEY UPDATE 实现幂等性

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
