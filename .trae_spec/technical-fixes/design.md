# 闲置物品校园交易平台技术修复设计文档

## 介绍

本设计文档基于 requirements.md 中的14个修复需求，详细描述技术实现方案、架构设计变更和实施策略。

## 技术架构

### 现有架构

```
┌─────────────────────────────────────────────────────────┐
│                    Frontend (Vue 3)                      │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐ │
│  │   Store   │  │ Components│  │ Services │  │   Views  │ │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘ │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                    Nginx (Reverse Proxy)                 │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                Backend (Spring Boot 3.2)                │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐ │
│  │Controller │→ │ Service  │→ │Repository│→ │   MySQL  │ │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘ │
│       │             │                                    │
│       │             ▼                                    │
│       │        ┌──────────┐                             │
│       │        │  Cache   │←→ Redis                     │
│       │        └──────────┘                             │
│       ▼                                                  │
│  ┌──────────┐                                           │
│  │ Filters  │ (XSS, RateLimit, JWT)                    │
│  └──────────┘                                           │
└─────────────────────────────────────────────────────────┘
```

### 目标架构

```
┌─────────────────────────────────────────────────────────┐
│                    Frontend (Vue 3)                      │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐ │
│  │   Store   │  │Components│  │ Services │  │   Views  │ │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘ │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                    Nginx (HTTPS + Security Headers)     │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                Backend (Spring Boot 3.2)                │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐ │
│  │Controller │→ │   DTO    │→ │ Service  │→ │Repository│ │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘ │
│                    │           │                          │
│                    │           ▼                          │
│                    │      ┌──────────┐                   │
│                    │      │  Cache   │←→ Redis (Auth)    │
│                    │      └──────────┘                   │
│                    ▼                                      │
│              ┌──────────┐                                │
│              │ Encrypt  │ (AES-256)                     │
│              └──────────┘                                │
│                                                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐              │
│  │XSSFilter │→ │RateLimit │→ │JWTFilter │              │
│  │ (Fixed)  │  │ (Degrad) │  │(Blacklist)│              │
│  └──────────┘  └──────────┘  └──────────┘              │
└─────────────────────────────────────────────────────────┘
```

## 模块设计

### 模块1: 安全加固模块

#### 1.1 XSS过滤器修复

**设计目标**: 完整处理所有请求数据类型，包括JSON数组

**实现方案**:
```java
// XssHttpServletRequestWrapper.java 修复方案
@Override
public Object apply(Object node) {
    if (node.isTextual()) {
        TextNode textNode = (TextNode) node;
        String text = textNode.text();
        if (containsHtmlOrScript(text)) {
            String cleaned = cleanXss(text);
            return textNode.text(cleaned);
        }
    } else if (node.isObject()) {
        ObjectNode objectNode = (ObjectNode) node;
        objectNode.fieldNames().forEachRemaining(field -> {
            JsonNode child = objectNode.get(field);
            objectNode.set(field, apply(child));
        });
    } else if (node.isArray()) {
        // 修复：递归处理数组中的每个元素
        ArrayNode arrayNode = (ArrayNode) node;
        for (int i = 0; i < arrayNode.size(); i++) {
            arrayNode.set(i, apply(arrayNode.get(i)));
        }
    }
    return node;
}
```

**依赖关系**: 无

#### 1.2 JWT Token黑名单

**设计目标**: 实现Token撤销机制

**实现方案**:
```java
// JwtTokenBlacklistService.java (新增)
@Service
@RequiredArgsConstructor
public class JwtTokenBlacklistService {
    private final RedisTemplate<String, String> redisTemplate;
    
    private static final String BLACKLIST_PREFIX = "token:blacklist:";
    
    public void addToBlacklist(String token, long expirationMs) {
        String key = BLACKLIST_PREFIX + getTokenHash(token);
        redisTemplate.opsForValue().set(key, "1", expirationMs, TimeUnit.MILLISECONDS);
    }
    
    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + getTokenHash(token);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    private String getTokenHash(String token) {
        return DigestUtils.md5DigestAsHex(token.getBytes());
    }
}

// JwtUtil.java 修改
public boolean validateToken(String token) {
    try {
        if (jwtTokenBlacklistService.isBlacklisted(token)) {
            return false;
        }
        parseToken(token);
        return true;
    } catch (Exception e) {
        return false;
    }
}

// AuthService.java 修改
public void logout(String token) {
    long expiration = getExpirationFromToken(token);
    long ttl = expiration - System.currentTimeMillis();
    if (ttl > 0) {
        jwtTokenBlacklistService.addToBlacklist(token, ttl);
    }
}

public void changePassword(Long userId, String newPassword) {
    // 修改密码时使所有Token失效
    String redisKey = "user:tokens:" + userId;
    Set<String> tokens = redisTemplate.opsForValue().get(redisKey);
    if (tokens != null) {
        tokens.forEach(token -> jwtTokenBlacklistService.addToBlacklist(token, 0));
    }
    redisTemplate.delete(redisKey);
}
```

**依赖关系**: Redis服务

#### 1.3 敏感数据加密

**设计目标**: AES-256加密存储，返回时脱敏

**实现方案**:
```java
// DataEncryptionUtil.java (新增)
@Component
public class DataEncryptionUtil {
    @Value("${app.encryption.secret-key}")
    private String secretKey;
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        try {
            SecretKeySpec keySpec = new SecretKeySpec(
                secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("加密失败", e);
        }
    }
    
    public String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }
        try {
            SecretKeySpec keySpec = new SecretKeySpec(
                secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(cipherText));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("解密失败", e);
        }
    }
}

// DataMaskUtil.java (新增)
@Component
public class DataMaskUtil {
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
    
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) return idCard;
        return idCard.substring(0, 4) + "**********" + idCard.substring(idCard.length() - 4);
    }
    
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) return email;
        return email.substring(0, 2) + "****" + email.substring(atIndex);
    }
}
```

**数据库变更**:
```sql
-- 需要添加加密字段
ALTER TABLE users ADD COLUMN phone_encrypted VARCHAR(255);
ALTER TABLE users ADD COLUMN email_encrypted VARCHAR(255);
ALTER TABLE verification_records ADD COLUMN id_card_encrypted VARCHAR(255);
```

**依赖关系**: 无

#### 1.4 Redis安全配置

**设计目标**: 密码认证 + SCAN命令 + 降级处理

**实现方案**:
```yaml
# docker-compose.yml 修改
redis:
  image: redis:7-alpine
  command: redis-server --appendonly yes --requirepass ${REDIS_PASSWORD}
  ports:
    - "6379:6379"
```

```java
// RateLimitFilter.java 修改
@Override
public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
    try {
        Long result = redisTemplate.execute(rateLimitScript, 
            List.of(getClientIP(request), String.valueOf(System.currentTimeMillis())));
        if (result != null && result > 0) {
            chain.doFilter(request, response);
        } else {
            httpResponse.setStatus(429);
            httpResponse.getWriter().write("{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\"}");
        }
    } catch (Exception e) {
        // 降级处理：Redis不可用时放行请求
        log.warn("Redis限流服务不可用，降级放行: {}", e.getMessage());
        chain.doFilter(request, response);
    }
}

// ConfigService.java 修改
public void clearConfigCache() {
    // 使用SCAN替代KEYS
    ScanOptions options = ScanOptions.scanOptions()
        .match(CONFIG_CACHE_PREFIX + "*")
        .count(100)
        .build();
    
    try (Cursor<String> cursor = redisTemplate.scan(options)) {
        List<String> keys = new ArrayList<>();
        while (cursor.hasNext()) {
            keys.add(cursor.next());
        }
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
```

**依赖关系**: 无

#### 1.5 WebSocket身份验证

**设计目标**: 验证消息发送者身份

**实现方案**:
```java
// ChatController.java 修改
@MessageMapping("/chat/send")
public void handleWebSocketMessage(@Payload ChatMessage message, Principal principal) {
    // 从WebSocket会话获取认证用户ID
    Long authenticatedUserId = getUserIdFromPrincipal(principal);
    
    // 验证发送者身份
    if (!authenticatedUserId.equals(message.getSenderId())) {
        throw new SecurityException("无权发送此消息：发送者身份不匹配");
    }
    
    ChatMessage savedMessage = chatService.sendMessage(
            message.getChatId(),
            authenticatedUserId,
            message.getReceiverId(),
            message.getContent(),
            message.getMessageType()
    );
    
    messagingTemplate.convertAndSend("/topic/chat/" + message.getReceiverId(), savedMessage);
}

// WebSocketConfig.java 修改
@Override
public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(new ChannelInterceptor() {
        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            StompHeaderAccessor accessor = MessageHeaderAccessor
                    .getAccessor(message, StompHeaderAccessor.class);
            
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                // 验证WebSocket连接的用户身份
                Authentication auth = (Authentication) accessor.getUser();
                if (auth == null || !auth.isAuthenticated()) {
                    throw new AccessDeniedException("WebSocket连接未授权");
                }
            }
            return message;
        }
    });
}
```

**依赖关系**: Spring Security

### 模块2: 业务逻辑修复

#### 2.1 订单并发安全

**设计目标**: 所有订单状态变更使用悲观锁

**实现方案**:
```java
// OrderService.java 修改
@Transactional
public Order cancelOrder(Long orderId, Long userId, CancelOrderRequest request) {
    // 使用悲观锁
    Order order = orderRepository.findByIdWithLock(orderId)
            .orElseThrow(() -> new IllegalArgumentException("订单不存在"));
    
    // 验证用户权限
    if (!order.getBuyerId().equals(userId)) {
        throw new SecurityException("无权取消此订单");
    }
    
    // 状态验证
    if (order.getOrderStatus() != Order.OrderStatus.PENDING_PAYMENT) {
        throw new BusinessException("订单状态不允许取消");
    }
    
    order.setOrderStatus(Order.OrderStatus.CANCELLED);
    order.setCancelReason(request.getReason());
    order.setCancelTime(LocalDateTime.now());
    
    return orderRepository.save(order);
}

@Transactional
public Order shipOrder(Long orderId, Long sellerId, String trackingNumber, String shippingCompany) {
    Order order = orderRepository.findByIdWithLock(orderId)
            .orElseThrow(() -> new IllegalArgumentException("订单不存在"));
    
    if (!order.getSellerId().equals(sellerId)) {
        throw new SecurityException("无权操作此订单");
    }
    
    if (order.getOrderStatus() != Order.OrderStatus.PENDING_SHIPMENT) {
        throw new BusinessException("订单状态不允许发货");
    }
    
    order.setOrderStatus(Order.OrderStatus.SHIPPED);
    order.setTrackingNumber(trackingNumber);
    order.setShippingCompany(shippingCompany);
    order.setShipTime(LocalDateTime.now());
    
    return orderRepository.save(order);
}

@Transactional
public Order approveRefund(Long orderId, Long adminId, String result) {
    Order order = orderRepository.findByIdWithLock(orderId)
            .orElseThrow(() -> new IllegalArgumentException("订单不存在"));
    
    // 修复：记住原状态
    Order.OrderStatus previousStatus = order.getPreviousStatus() != null 
            ? order.getPreviousStatus() 
            : order.getOrderStatus();
    
    if ("APPROVED".equals(result)) {
        order.setOrderStatus(Order.OrderStatus.REFUNDED);
        order.setRefundTime(LocalDateTime.now());
    } else if ("REJECTED".equals(result)) {
        // 修复：根据原状态回退
        if (previousStatus == Order.OrderStatus.SHIPPED) {
            order.setOrderStatus(Order.OrderStatus.SHIPPED);
        } else {
            order.setOrderStatus(Order.OrderStatus.PENDING_SHIPMENT);
        }
    }
    
    order.setRefundResult(result);
    order.setRefundAdminId(adminId);
    
    return orderRepository.save(order);
}
```

**依赖关系**: 无

#### 2.2 收藏计数同步

**设计目标**: 原子性更新收藏计数

**实现方案**:
```java
// FavoriteService.java 修改
@Transactional
public Favorite addFavorite(Long userId, Long itemId) {
    if (favoriteRepository.existsByUserIdAndItemId(userId, itemId)) {
        throw new BusinessException("已收藏该物品");
    }
    
    Favorite favorite = new Favorite();
    favorite.setUserId(userId);
    favorite.setItemId(itemId);
    Favorite savedFavorite = favoriteRepository.save(favorite);
    
    // 原子性增加收藏计数
    itemRepository.incrementFavoriteCount(itemId);
    
    return savedFavorite;
}

@Transactional
public void removeFavorite(Long userId, Long itemId) {
    favoriteRepository.deleteByUserIdAndItemId(userId, itemId);
    
    // 原子性减少收藏计数
    itemRepository.decrementFavoriteCount(itemId);
}

// ItemRepository.java 新增
@Modifying
@Query("UPDATE Item i SET i.favoriteCount = i.favoriteCount + 1 WHERE i.id = :itemId")
void incrementFavoriteCount(@Param("itemId") Long itemId);

@Modifying
@Query("UPDATE Item i SET i.favoriteCount = i.favoriteCount - 1 WHERE i.id = :itemId AND i.favoriteCount > 0")
void decrementFavoriteCount(@Param("itemId") Long itemId);
```

**依赖关系**: 无

#### 2.3 浏览量准确计数

**设计目标**: 异步原子性更新浏览量

**实现方案**:
```java
// ItemService.java 修改
public Item getItemById(Long id) {
    String cacheKey = CacheManager.getItemKey(id);
    Object cachedObject = cacheManager.get(cacheKey);
    if (cachedObject instanceof Item) {
        // 仅使用异步更新
        incrementViewCountAsync(id);
        return (Item) cachedObject;
    }
    
    Item item = itemRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("物品不存在"));
    
    // 移除同步更新，仅使用异步
    incrementViewCountAsync(id);
    
    // 转换为DTO避免Entity污染
    Item cachedItem = item;
    cacheManager.set(cacheKey, cachedItem, 600);
    
    return cachedItem;
}

@Async
public void incrementViewCountAsync(Long itemId) {
    try {
        itemRepository.incrementViewCount(itemId);
    } catch (Exception e) {
        log.error("更新浏览量失败: {}", e.getMessage());
    }
}

// ItemRepository.java 新增
@Modifying
@Query("UPDATE Item i SET i.viewCount = i.viewCount + 1 WHERE i.id = :itemId")
void incrementViewCount(@Param("itemId") Long itemId);
```

**依赖关系**: 异步线程池配置

### 模块3: 架构优化

#### 3.1 异常处理完善

**设计目标**: 统一处理所有异常类型

**实现方案**:
```java
// GlobalExceptionHandler.java 修改
@ExceptionHandler(BusinessException.class)
public Result<Void> handleBusinessException(BusinessException e) {
    log.warn("业务异常: {}", e.getMessage());
    return Result.error(e.getErrorCode().getCode(), e.getMessage());
}

@ExceptionHandler(JwtException.class)
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public Result<Void> handleJwtException(JwtException e) {
    log.warn("JWT异常: {}", e.getMessage());
    return Result.error(ErrorCode.UNAUTHORIZED.getCode(), "Token无效或已过期");
}

@ExceptionHandler(AccessDeniedException.class)
@ResponseStatus(HttpStatus.FORBIDDEN)
public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
    log.warn("访问被拒绝: {}", e.getMessage());
    return Result.error(ErrorCode.FORBIDDEN.getCode(), "无权访问");
}

@ExceptionHandler(DataIntegrityViolationException.class)
@ResponseStatus(HttpStatus.CONFLICT)
public Result<Void> handleDataIntegrityViolation(DataIntegrityViolationException e) {
    log.error("数据完整性违反: {}", e.getMessage());
    return Result.error(ErrorCode.CONFLICT.getCode(), "数据冲突，请稍后重试");
}

@ExceptionHandler(NoHandlerFoundException.class)
@ResponseStatus(HttpStatus.NOT_FOUND)
public Result<Void> handleNoHandlerFound(NoHandlerFoundException e) {
    return Result.error(ErrorCode.NOT_FOUND.getCode(), "资源不存在");
}

@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
public Result<Void> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
    return Result.error(405, "请求方法不允许");
}

// ErrorCode.java 新增
public enum ErrorCode {
    // ... 现有错误码
    CONFLICT(409, "数据冲突", HttpStatus.CONFLICT);
}
```

**依赖关系**: 无

#### 3.2 前端Store统一

**设计目标**: 删除冗余Store，统一导入方式

**实现方案**:
```bash
# 删除冗余文件
rm src/store/user.js
rm src/store/item.js
rm src/store/cart.js
```

```javascript
// src/store/index.js 修改
import { createPinia } from 'pinia';
import { useUserStore } from './modules/user';
import { useItemStore } from './modules/item';
import { useCartStore } from './modules/cart';

const pinia = createPinia();

export { pinia, useUserStore, useItemStore, useCartStore };
export default pinia;
```

**依赖关系**: 无

#### 3.3 接口路径一致性

**设计目标**: 统一使用ApiPaths常量

**实现方案**:
```java
// OrderController.java 修改
@RestController
@RequestMapping(ApiPaths.Order.BASE)  // 使用ApiPaths常量
@RequiredArgsConstructor
public class OrderController {
    
    @PutMapping("/{id}/confirm-receive")  // 统一路径
    public Result<OrderSummaryResponse> confirmReceive(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        // ...
    }
}
```

```javascript
// frontend/src/api/config/paths.js 修改
ORDER: {
    LIST: '/orders',
    DETAIL: (id) => `/orders/${id}`,
    CREATE: '/orders',
    CONFIRM: (id) => `/orders/${id}/confirm-receive`,  // 统一为confirm-receive
    // ...
}
```

**依赖关系**: 无

### 模块4: 运维支持

#### 4.1 数据库备份

**设计目标**: 自动化备份策略

**实现方案**:
```bash
#!/bin/bash
# scripts/backup/backup.sh

BACKUP_DIR="/var/backups/mysql"
DATE=$(date +%Y%m%d_%H%M%S)
MYSQL_HOST=${DB_HOST:-mysql}
MYSQL_USER=${BACKUP_USER:-idle_items_backup}
MYSQL_PASSWORD=${BACKUP_PASSWORD}
DATABASE=${DB_NAME:-idle_items_school}

# 创建备份目录
mkdir -p $BACKUP_DIR

# 执行备份
mysqldump -h $MYSQL_HOST -u $MYSQL_USER -p$MYSQL_PASSWORD \
    --single-transaction --routines --triggers \
    $DATABASE | gzip > "$BACKUP_DIR/backup_$DATE.sql.gz"

# 验证备份完整性
if [ $? -eq 0 ]; then
    echo "$(date): 备份成功 - backup_$DATE.sql.gz" >> /var/log/backup.log
    
    # 清理7天前的备份
    find $BACKUP_DIR -name "backup_*.sql.gz" -mtime +7 -delete
else
    echo "$(date): 备份失败" >> /var/log/backup.log
    exit 1
fi
```

```yaml
# docker-compose.yml 添加备份服务
services:
  backup:
    image: mysql:8.0
    volumes:
      - ./scripts/backup:/backup
      - backup-data:/backups
    environment:
      - MYSQL_HOST=mysql
      - MYSQL_USER=idle_items_backup
      - MYSQL_PASSWORD=${BACKUP_DB_PASSWORD}
    command: >
      sh -c "echo '0 2 * * * /backup/backup.sh' > /etc/crontabs/root &&
             crond -f -l 8"
    depends_on:
      mysql:
        condition: service_healthy
```

**依赖关系**: MySQL服务

## 数据流设计

### 订单创建流程

```
用户下单 → Controller → DTO校验 → Service → 悲观锁查询 → 验证库存 → 创建订单 → 返回结果
    │         │              │          │           │            │
    │         │              │          │           │            └→ 更新缓存
    │         │              │          │           └→ 锁定物品
    │         │              │          └→ 事务开始
    │         │              └→ @Valid校验
    │         └→ CreateOrderRequest
    └→ POST /api/orders
```

### 用户登录流程

```
用户登录 → Controller → LoginRequest校验 → AuthService → 验证密码 → 生成Token → 记录Token → 返回结果
    │         │              │                  │           │          │            │
    │         │              │                  │           │          │            └→ Redis存储
    │         │              │                  │           │          └→ JWT生成
    │         │              │                  │           └→ BCrypt验证
    │         │              │                  └→ 查询用户
    │         │              └→ @Valid校验
    │         └→ LoginRequest
    └→ POST /api/auth/login
```

## 错误处理策略

| 错误类型 | HTTP状态码 | 响应格式 | 处理方式 |
|---------|-----------|---------|---------|
| 参数校验失败 | 400 | Result.error(400, "参数错误") | GlobalExceptionHandler |
| 未授权 | 401 | Result.error(401, "未授权") | JWT Filter |
| 禁止访问 | 403 | Result.error(403, "禁止访问") | Security Config |
| 资源不存在 | 404 | Result.error(404, "资源不存在") | GlobalExceptionHandler |
| 业务异常 | 4xx/5xx | Result.error(code, message) | GlobalExceptionHandler |
| 数据冲突 | 409 | Result.error(409, "数据冲突") | GlobalExceptionHandler |
| 服务器错误 | 500 | Result.error(500, "服务器错误") | GlobalExceptionHandler |

## 测试策略

| 测试类型 | 覆盖范围 | 工具 |
|---------|---------|------|
| 单元测试 | Service层、工具类 | JUnit 5 + Mockito |
| 集成测试 | Controller层、Repository层 | Spring Boot Test |
| 安全测试 | XSS过滤、JWT验证 | 自定义测试用例 |
| 性能测试 | 并发订单操作 | JMeter |

## 部署策略

| 环境 | 部署方式 | 验证步骤 |
|------|---------|---------|
| 开发环境 | 本地启动 | 单元测试通过 |
| 测试环境 | Docker Compose | 集成测试通过 |
| 生产环境 | Docker Compose | 完整测试 + 人工验证 |

## 实施计划

| 阶段 | 任务 | 依赖 |
|------|------|------|
| 第一阶段 | 安全加固(XSS、JWT、敏感数据、Redis、WebSocket) | 无 |
| 第二阶段 | 业务修复(订单锁、收藏计数、浏览量、异常处理) | 第一阶段 |
| 第三阶段 | 架构优化(Store统一、路径一致性、死代码清理) | 第二阶段 |
| 第四阶段 | 运维支持(备份策略、测试覆盖) | 第三阶段 |
