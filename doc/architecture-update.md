# 系统架构文档更新

## 更新概述

- **更新日期**: 2026年5月9日
- **更新内容**: 基于技术修复任务的架构优化
- **版本**: 1.1.0

## 架构变更

### 1. 安全架构升级

#### 1.1 JWT Token管理

**变更前**:
- Token无法主动失效
- 用户被禁用后旧Token仍有效

**变更后**:
- 新增`JwtTokenBlacklistService`服务
- 支持Token黑名单机制
- 用户登出/密码修改时Token立即失效
- Redis存储黑名单，自动过期清理

**架构图**:
```
┌─────────────────────────────────────────────────────────┐
│                    安全认证流程                          │
├─────────────────────────────────────────────────────────┤
│  HTTP Request → XSS Filter → JWT Filter → Controller   │
│                      │                                  │
│                      ▼                                  │
│              ┌──────────────┐                          │
│              │ Token验证    │                          │
│              │ 1.签名验证   │                          │
│              │ 2.过期检查   │                          │
│              │ 3.黑名单检查 │←── Redis                 │
│              └──────────────┘                          │
└─────────────────────────────────────────────────────────┘
```

#### 1.2 敏感数据保护

**新增组件**:
- `DataEncryptionUtil`: AES-256加密工具
- `DataMaskUtil`: 数据脱敏工具

**保护范围**:
- 手机号: 加密存储，返回时脱敏(138****1234)
- 身份证号: 加密存储，返回时脱敏(1101****1234)
- 邮箱: 返回时脱敏(zh****@example.com)

### 2. 数据层优化

#### 2.1 并发控制

**订单操作悲观锁**:
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT o FROM Order o WHERE o.id = :id")
Optional<Order> findByIdWithLock(@Param("id") Long id);
```

**适用场景**:
- 订单取消
- 订单发货
- 确认收货
- 退款申请

#### 2.2 原子性计数器

**收藏计数**:
```java
@Modifying
@Query("UPDATE Item i SET i.favoriteCount = i.favoriteCount + 1 WHERE i.id = :itemId")
void incrementFavoriteCount(@Param("itemId") Long itemId);
```

**浏览量计数**:
```java
@Modifying
@Query("UPDATE Item i SET i.viewCount = i.viewCount + 1 WHERE i.id = :itemId")
void incrementViewCount(@Param("itemId") Long itemId);
```

### 3. 缓存架构优化

#### 3.1 Redis安全配置

**配置变更**:
- 添加密码认证
- 使用SCAN替代KEYS命令
- 降级处理机制

**限流器降级**:
```java
try {
    Long result = redisTemplate.execute(rateLimitScript, ...);
    // 正常限流逻辑
} catch (Exception e) {
    // Redis不可用时放行请求
    log.warn("Redis限流服务不可用，降级放行");
    chain.doFilter(request, response);
}
```

### 4. 异常处理架构

**异常处理链**:
```
Controller → Service → Repository
    ↓           ↓           ↓
    └───────────┴───────────┘
                ↓
        GlobalExceptionHandler
                ↓
        统一错误响应格式
```

**支持的异常类型**:
- BusinessException: 业务异常
- JwtException: JWT相关异常
- AccessDeniedException: 权限不足
- DataIntegrityViolationException: 数据冲突
- PessimisticLockException: 锁超时

## 新增组件清单

### 后端新增

| 组件 | 路径 | 说明 |
|------|------|------|
| JwtTokenBlacklistService | security/ | JWT Token黑名单服务 |
| DataEncryptionUtil | util/ | 数据加密工具 |
| DataMaskUtil | util/ | 数据脱敏工具 |

### 运维新增

| 组件 | 路径 | 说明 |
|------|------|------|
| backup.sh | scripts/backup/ | 数据库备份脚本 |
| restore.sh | scripts/backup/ | 数据库恢复脚本 |

### 测试新增

| 组件 | 路径 | 说明 |
|------|------|------|
| ItemServiceTest | service/ | ItemService单元测试 |
| OrderServiceTest | service/ | OrderService单元测试 |
| FavoriteServiceTest | service/ | FavoriteService单元测试 |
| AdminOrderControllerTest | controller/admin/ | AdminOrderController测试 |
| AdminUserControllerTest | controller/admin/ | AdminUserController测试 |
| AdminItemControllerTest | controller/admin/ | AdminItemController测试 |
| AdminCategoryControllerTest | controller/admin/ | AdminCategoryController测试 |
| AdminVerificationControllerTest | controller/admin/ | AdminVerificationController测试 |
| AdminLogControllerTest | controller/admin/ | AdminLogController测试 |
| AdminBatchControllerTest | controller/admin/ | AdminBatchController测试 |
| StatisticsControllerTest | controller/admin/ | StatisticsController测试 |

## 配置变更

### docker-compose.yml

```yaml
# Redis添加密码认证
redis:
  command: redis-server --appendonly yes --requirepass ${REDIS_PASSWORD}

# 新增备份服务
backup:
  image: mysql:8.0
  volumes:
    - ./scripts/backup:/backup
    - backup-data:/backups
```

### application.yml

```yaml
# 新增加密配置
app:
  encryption:
    secret-key: "${ENCRYPTION_SECRET_KEY}"
```

## 性能优化

### 数据库查询优化

1. **消除N+1查询**: 批量查询用户信息和评分
2. **原子性计数器**: 避免并发计数问题
3. **异步浏览量更新**: 减少响应延迟

### 缓存策略

1. **SCAN替代KEYS**: 避免阻塞Redis
2. **降级处理**: Redis不可用时系统仍可用
3. **缓存穿透保护**: 缓存空值防止穿透

## 安全加固

### 已修复的安全漏洞

| 漏洞 | 严重程度 | 修复方案 |
|------|----------|----------|
| XSS数组过滤不完整 | 高 | 递归处理JSON数组 |
| JWT Token无法撤销 | 高 | 黑名单机制 |
| 敏感数据明文存储 | 高 | AES-256加密 |
| Redis无密码认证 | 中 | 添加密码配置 |
| WebSocket身份验证缺失 | 中 | 验证发送者身份 |

## 监控与运维

### 日志规范

**日志格式**:
```
%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
```

**日志级别**:
- ERROR: 系统错误
- WARN: 警告信息
- INFO: 关键业务日志
- DEBUG: 调试信息

### 备份策略

| 环境 | 备份频率 | 保留时间 |
|------|----------|----------|
| 开发环境 | 每日 | 2天 |
| 测试环境 | 每日 | 7天 |
| 生产环境 | 每日 | 30天 |

## 后续优化建议

### 短期（1-2周）

1. 补充API文档注解
2. 添加性能监控指标
3. 完善集成测试

### 中期（1-2月）

1. 引入熔断降级组件
2. 实现灰度发布
3. 添加链路追踪

### 长期（3-6月）

1. 微服务架构演进
2. 引入消息队列
3. 实现读写分离

## 文档维护

**文档版本**: 1.1.0
**最后更新**: 2026年5月9日
**维护人员**: 开发团队

---

**更新完成**: 所有架构变更已记录，后续迭代请同步更新本文档。
