# API 接口文档

## 1. API 概述

| 配置项 | 值 |
|--------|-----|
| 基础路径 | /api |
| 认证方式 | JWT Bearer Token（Authorization header） |
| 响应格式 | 统一 JSON：{ code, message, data, timestamp } |
| 限流策略 | Redis 滑动窗口，60 次/分钟/IP |

---

## 2. 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1234567890123
}
```

---

## 3. API 端点清单

### 3.1 认证模块（/auth）

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | /auth/register | 公开 | 用户注册 |
| POST | /auth/login | 公开 | 用户登录 |
| POST | /auth/logout | 需认证 | 用户登出 |
| POST | /auth/refresh | 需认证 | 刷新令牌 |
| GET | /auth/me | 需认证 | 获取当前用户信息 |
| PUT | /auth/profile | 需认证 | 更新个人资料 |
| PUT | /auth/password | 需认证 | 修改密码 |
| POST | /auth/avatar | 需认证 | 上传头像 |

### 3.2 物品模块（/items）

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /items | 公开 | 获取物品列表（分页、筛选） |
| GET | /items/{id} | 公开 | 获取物品详情 |
| POST | /items | 需认证 | 发布物品 |
| PUT | /items/{id} | 需认证（所有者） | 更新物品 |
| DELETE | /items/{id} | 需认证（所有者） | 删除物品 |
| PUT | /items/{id}/status | 需认证（所有者） | 更新物品状态 |
| GET | /items/my | 需认证 | 获取我的物品列表 |
| GET | /items/hot | 公开 | 获取热门物品 |
| POST | /items/{id}/images | 需认证（所有者） | 上传物品图片 |
| DELETE | /items/{id}/images/{imageId} | 需认证（所有者） | 删除物品图片 |

### 3.3 分类模块（/categories）

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /categories | 公开 | 获取所有分类 |
| GET | /categories/tree | 公开 | 获取分类树 |
| GET | /categories/{id} | 公开 | 获取分类详情 |
| POST | /categories | 管理员 | 创建分类 |
| PUT | /categories/{id} | 管理员 | 更新分类 |
| DELETE | /categories/{id} | 管理员 | 删除分类 |

### 3.4 订单模块（/orders）

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | /orders | 需认证 | 创建订单 |
| GET | /orders | 需认证 | 获取订单列表 |
| GET | /orders/{id} | 需认证 | 获取订单详情 |
| PUT | /orders/{id}/pay | 需认证（买家） | 支付订单 |
| PUT | /orders/{id}/ship | 需认证（卖家） | 发货 |
| PUT | /orders/{id}/deliver | 需认证（买家） | 确认收货 |
| PUT | /orders/{id}/complete | 需认证 | 完成订单 |
| PUT | /orders/{id}/cancel | 需认证 | 取消订单 |
| POST | /orders/{id}/refund | 需认证（买家） | 申请退款 |
| GET | /orders/{id}/review | 需认证 | 获取订单评价 |
| POST | /orders/{id}/review | 需认证（买家） | 创建订单评价 |

### 3.5 聊天模块（/chats）

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | /chats | 需认证 | 创建聊天会话 |
| GET | /chats | 需认证 | 获取聊天列表 |
| GET | /chats/{id} | 需认证 | 获取聊天详情 |
| GET | /chats/{id}/messages | 需认证 | 获取聊天消息 |
| POST | /chats/{id}/messages | 需认证 | 发送消息 |
| PUT | /chats/{id}/read | 需认证 | 标记已读 |

### 3.6 收藏模块（/favorites）

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | /favorites/{itemId} | 需认证 | 添加收藏 |
| DELETE | /favorites/{itemId} | 需认证 | 取消收藏 |
| GET | /favorites | 需认证 | 获取收藏列表 |
| GET | /favorites/check/{itemId} | 需认证 | 检查收藏状态 |

### 3.7 评价模块（/reviews）

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /reviews/item/{itemId} | 公开 | 获取物品评价 |
| GET | /reviews/user/{userId} | 公开 | 获取用户评价 |
| POST | /reviews | 需认证 | 创建评价 |
| GET | /reviews/{id} | 公开 | 获取评价详情 |

### 3.8 实名认证模块（/verification）

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | /verification | 需认证 | 提交实名认证 |
| GET | /verification/status | 需认证 | 获取认证状态 |
| GET | /verification/records | 需认证 | 获取认证记录 |

### 3.9 管理员 — 用户管理

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /admin/users | 管理员 | 获取用户列表 |
| PUT | /admin/users/{id}/status | 管理员 | 更新用户状态 |

### 3.10 管理员 — 物品管理

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /admin/items | 管理员 | 获取物品列表（含待审核） |
| PUT | /admin/items/{id}/status | 管理员 | 审核物品 |

### 3.11 管理员 — 订单管理

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /admin/orders | 管理员 | 获取订单列表 |
| PUT | /admin/orders/{id}/refund | 管理员 | 处理退款 |

### 3.12 管理员 — 认证审核

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /admin/verifications | 管理员 | 获取认证列表 |
| PUT | /admin/verifications/{id} | 管理员 | 审核认证 |

### 3.13 管理员 — 操作日志

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /admin/logs | 管理员 | 获取操作日志 |

### 3.14 统计模块（/admin/statistics）

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /admin/statistics/dashboard | 管理员 | 仪表盘数据 |
| GET | /admin/statistics/orders | 管理员 | 订单统计 |
| GET | /admin/statistics/users | 管理员 | 用户统计 |
| GET | /admin/statistics/items | 管理员 | 物品统计 |

### 3.15 监控模块（/admin/monitor）

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /admin/monitor/metrics | 管理员 | 系统指标 |
| GET | /admin/monitor/health | 管理员 | 健康检查 |
| GET | /admin/monitor/cache | 管理员 | 缓存状态 |

### 3.16 日志分析模块（/admin/logs）

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /admin/logs/analysis | 管理员 | 日志分析 |
| GET | /admin/logs/export | 管理员 | 导出日志 |
| DELETE | /admin/logs/cleanup | 管理员 | 清理日志 |

### 3.17 测试模块（/test）

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /test/health | 公开 | 健康检查 |
| GET | /test/auth | 需认证 | 测试认证 |
| GET | /test/admin | 管理员 | 测试管理员权限 |

---

## 4. 安全白名单

以下路径不需要 JWT 认证：

| 路径 | 说明 |
|------|------|
| /auth/login | 登录 |
| /auth/register | 注册 |
| /test/** | 测试端点 |
| /items/** (GET) | 物品浏览 |
| /categories/** (GET) | 分类浏览 |
| /reviews/** (GET) | 评价查看 |
| /uploads/** | 静态文件 |
| /ws/** | WebSocket |

---

## 5. 错误码表

| 错误码 | 含义 | 说明 |
|--------|------|------|
| 200 | 操作成功 | 请求成功处理 |
| 201 | 创建成功 | 资源创建成功 |
| 400 | 请求参数错误 | 参数校验失败 |
| 401 | 未授权 | 未登录或 Token 过期 |
| 403 | 无权限 | 角色权限不足 |
| 404 | 资源不存在 | 请求的资源未找到 |
| 409 | 资源冲突 | 如用户名已存在 |
| 500 | 服务器内部错误 | 未知服务端错误 |
