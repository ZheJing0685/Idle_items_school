# 闲置物品校园交易平台缺失功能实现设计文档

## 介绍

本设计文档基于 requirements.md 中的6个缺失功能需求，详细描述技术实现方案、接口设计和数据模型。

## 模块设计

### 模块1: 消息通知系统

#### 1.1 数据模型

**Notification实体**:
```java
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    private Long id;
    private Long userId;           // 接收用户ID
    private String type;           // 通知类型: ORDER, SYSTEM, DISPUTE, CHAT
    private String title;          // 通知标题
    private String content;        // 通知内容
    private Long relatedId;        // 关联业务ID
    private String relatedType;    // 关联业务类型: ORDER, DISPUTE
    private Boolean isRead;        // 是否已读
    private LocalDateTime readTime;// 阅读时间
    private LocalDateTime createdAt;
}
```

#### 1.2 接口设计

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取通知列表 | GET | /api/notifications | 分页查询用户通知 |
| 获取未读数量 | GET | /api/notifications/unread-count | 获取未读通知数量 |
| 标记已读 | PUT | /api/notifications/{id}/read | 标记单条为已读 |
| 全部已读 | PUT | /api/notifications/read-all | 标记所有为已读 |
| 删除通知 | DELETE | /api/notifications/{id} | 删除单条通知 |

#### 1.3 通知触发场景

| 事件 | 通知类型 | 接收方 | 标题模板 |
|------|----------|--------|----------|
| 订单创建 | ORDER | 卖家 | 您有新的订单{orderNo} |
| 订单支付 | ORDER | 卖家 | 订单{orderNo}已支付，请尽快发货 |
| 订单发货 | ORDER | 买家 | 订单{orderNo}已发货，请注意查收 |
| 订单完成 | ORDER | 买卖双方 | 订单{orderNo}已完成 |
| 订单取消 | ORDER | 买卖双方 | 订单{orderNo}已取消 |
| 纠纷发起 | DISPUTE | 被申请人 | 您有一条新的纠纷待处理 |
| 纠纷处理 | DISPUTE | 申请人 | 您的纠纷已处理 |
| 系统公告 | SYSTEM | 全体用户 | {title} |

### 模块2: 纠纷处理系统

#### 2.1 数据模型

**Dispute实体**（扩展现有）:
```java
@Entity
@Table(name = "disputes")
public class Dispute {
    @Id
    private Long id;
    private String disputeNo;      // 纠纷编号
    private Long orderId;          // 关联订单ID
    private Long applicantId;      // 申请人ID
    private Long respondentId;     // 被申请人ID
    private String reason;         // 纠纷原因
    private String description;    // 详细描述
    private String evidenceImages; // 证据图片JSON
    private DisputeStatus status;  // PENDING/PROCESSING/RESOLVED/CLOSED
    private String result;         // 处理结果
    private Long handlerId;        // 处理人ID
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public enum DisputeStatus {
        PENDING, PROCESSING, RESOLVED, CLOSED
    }
}
```

#### 2.2 接口设计

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 发起纠纷 | POST | /api/disputes | 创建纠纷申请 |
| 获取我的纠纷 | GET | /api/disputes | 分页查询我的纠纷 |
| 获取纠纷详情 | GET | /api/disputes/{id} | 查看纠纷详情 |
| 回复纠纷 | POST | /api/disputes/{id}/reply | 添加纠纷回复 |
| 管理员审核 | PUT | /api/admin/disputes/{id}/handle | 管理员处理纠纷 |
| 获取纠纷列表 | GET | /api/admin/disputes | 管理员查看所有纠纷 |

#### 2.3 纠纷流程

```
发起纠纷 → 待处理(PENDING) → 处理中(PROCESSING) → 已解决(RESOLVED)/已关闭(CLOSED)
    │              │                │
    │              │                └→ 管理员审核 → 更新订单状态
    │              │
    │              └→ 被申请人回复
    │
    └→ 申请人提交证据
```

### 模块3: 密码重置功能

#### 3.1 流程设计

```
用户点击忘记密码 → 输入邮箱 → 发送验证码(6位) → 输入验证码 → 设置新密码 → 重置成功
```

#### 3.2 接口设计

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 发送验证码 | POST | /api/auth/forgot-password | 发送重置验证码 |
| 验证验证码 | POST | /api/auth/verify-code | 验证验证码有效性 |
| 重置密码 | POST | /api/auth/reset-password | 使用验证码重置密码 |

#### 3.3 验证码存储

使用Redis存储验证码，5分钟有效期：
```
Key: password_reset:{email}
Value: {验证码}
TTL: 300秒
```

#### 3.4 安全措施

- 验证码5分钟有效期
- 每小时最多发送3次验证码
- 验证码使用后立即失效
- 新密码必须与旧密码不同

### 模块4: 聊天页面

#### 4.1 前端组件设计

```
views/
├── Chat.vue                    # 聊天主页面
│   ├── ChatList.vue           # 左侧会话列表
│   ├── ChatWindow.vue         # 右侧聊天窗口
│   └── MessageItem.vue        # 单条消息组件
```

#### 4.2 接口设计

后端接口已完整，前端需要调用：

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取会话列表 | GET | /api/chats | 获取用户的聊天列表 |
| 获取消息列表 | GET | /api/chats/{chatId}/messages | 获取聊天消息 |
| 发送消息 | WebSocket | /app/chat/send | 实时发送消息 |
| 创建会话 | POST | /api/chats | 创建新的聊天会话 |

#### 4.3 WebSocket连接

```javascript
// 前端WebSocket连接
const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:7000/ws',
    connectHeaders: { Authorization: `Bearer ${token}` }
});

// 订阅个人消息频道
stompClient.subscribe(`/topic/chat/${userId}`, (message) => {
    // 处理收到的消息
});
```

### 模块5: 自动确认收货

#### 5.1 定时任务设计

```java
@Component
public class AutoConfirmTask {
    
    @Scheduled(cron = "0 0 2 * * ?")  // 每天凌晨2点执行
    public void autoConfirmReceived() {
        // 查询发货超过7天的订单
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);
        List<Order> shippedOrders = orderRepository.findByStatusAndShipTimeBefore(
            OrderStatus.SHIPPED, threshold);
        
        for (Order order : shippedOrders) {
            // 自动确认收货
            order.setOrderStatus(OrderStatus.COMPLETED);
            order.setCompleteTime(LocalDateTime.now());
            orderRepository.save(order);
            
            // 发送通知
            notificationService.createOrderNotification(order, "AUTO_CONFIRM");
        }
    }
}
```

#### 5.2 配置参数

- 自动确认天数: 7天（可通过配置修改）
- 执行时间: 每天凌晨2点
- 通知: 自动发送给买卖双方

### 模块6: WebSocket通知推送

#### 6.1 推送架构

```
业务事件 → NotificationService → WebSocket推送 → 在线用户
                ↓
            数据库存储 → 离线用户上线时推送
```

#### 6.2 推送频道设计

| 频道 | 用途 | 示例 |
|------|------|------|
| /topic/chat/{userId} | 聊天消息 | 私人聊天 |
| /topic/notifications/{userId} | 个人通知 | 订单状态变更 |
| /topic/admin | 管理员通知 | 纠纷待处理 |

#### 6.3 前端集成

```javascript
// 订阅通知频道
stompClient.subscribe(`/topic/notifications/${userId}`, (message) => {
    const notification = JSON.parse(message.body);
    // 更新通知徽章
    updateNotificationBadge();
    // 显示通知提示
    showNotificationToast(notification);
});
```

## 数据流设计

### 通知发送流程

```
业务操作 → Service层 → NotificationRepository.save()
                ↓
        SimpMessagingTemplate.convertAndSend()
                ↓
        WebSocket推送 → 前端接收
```

### 纠纷处理流程

```
发起纠纷 → 创建Dispute → 通知被申请人
                ↓
被申请人回复 → 更新Dispute → 通知申请人
                ↓
管理员审核 → 更新Dispute状态 → 通知双方 → 更新订单状态
```

## 实施计划

### 第一阶段: 基础设施

1. 完善Notification实体和Repository
2. 创建DisputeService和Controller
3. 实现NotificationService核心逻辑

### 第二阶段: 核心功能

4. 实现消息通知触发机制
5. 实现纠纷处理流程
6. 实现密码重置功能

### 第三阶段: 前端实现

7. 创建聊天页面组件
8. 集成WebSocket客户端
9. 实现通知中心页面

### 第四阶段: 优化完善

10. 实现自动确认收货定时任务
11. 完善WebSocket通知推送
12. 测试和优化

## 依赖关系

```
NotificationService
    ├── 依赖 WebSocket (推送)
    ├── 依赖 Redis (验证码存储)
    └── 依赖 各业务Service (触发通知)

DisputeService
    ├── 依赖 NotificationService (发送通知)
    ├── 依赖 OrderService (更新订单状态)
    └── 依赖 UserService (获取用户信息)

ChatService (已有)
    ├── 依赖 WebSocket (消息推送)
    └── 依赖 ChatRepository/ChatMessageRepository
```
