# Checklist: 修复WebSocket连接验证清单

## 后端验证

- [ ] **C1**: `/ws-native`端点HTTP可访问（不返回401/403）
  ```
  curl -v http://localhost:7000/ws-native
  期望: HTTP 101 或 200（非401）
  ```

- [ ] **C2**: `StompAuthInterceptor`正确验证JWT
  ```
  发送STOMP CONNECT帧带有效Token → CONNECTED
  发送STOMP CONNECT帧带无效Token → ERROR
  发送STOMP CONNECT帧无Token → ERROR
  ```

- [ ] **C3**: `StompAuthInterceptor`检查Token黑名单
  ```
  用户登出后，用旧Token发送CONNECT → ERROR
  ```

- [ ] **C4**: WebSocket消息发送者身份验证仍然有效
  ```
  用户A尝试以用户B的身份发消息 → 被拒绝
  ```

## 前端验证

- [ ] **C5**: Chat页面WebSocket连接成功
  ```
  进入Chat页面 → 控制台显示"WebSocket已连接"
  ```

- [ ] **C6**: 聊天消息实时推送
  ```
  发送消息 → 接收方收到实时推送
  ```

- [ ] **C7**: Notifications页面独立连接成功
  ```
  刷新浏览器 → 直接进入Notifications页面 → WebSocket连接成功
  ```

- [ ] **C8**: 通知实时推送
  ```
  触发通知（如创建订单）→ Notifications页面收到实时推送
  ```

- [ ] **C9**: 断网重连机制正常工作
  ```
  断开网络 → 恢复 → WebSocket自动重连（指数退避）
  ```

## 回归验证

- [ ] **C10**: REST API正常运行（登录/注册/商品列表）
- [ ] **C11**: Swagger文档可访问
- [ ] **C12**: 后端启动无异常日志
- [ ] **C13**: 前端构建无错误

## 通过标准

- 所有 [ ] C1-C13 项均通过
- 后端无异常启动
- 前端无控制台错误
