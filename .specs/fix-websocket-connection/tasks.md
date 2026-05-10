# Tasks: 修复WebSocket连接失败

## 任务列表

### Task 1: SecurityConfig添加WebSocket端点白名单

**优先级**: Critical
**依赖**: 无
**文件**: `backend/.../config/SecurityConfig.java`

**操作**:
- 在`authorizeHttpRequests`中添加`.requestMatchers("/ws/**", "/ws-native/**").permitAll()`
- 确保放在`.anyRequest().authenticated()`之前

**验证**: 启动后端，直接用浏览器访问`http://localhost:7000/ws-native`看是否返回HTTP 200（WebSocket握手响应为101而非401/403）

---

### Task 2: 创建StompAuthInterceptor

**优先级**: Critical
**依赖**: Task 1
**文件**: `backend/.../websocket/StompAuthInterceptor.java`（新建）

**操作**:
- 实现`ChannelInterceptor`接口
- 重写`preSend`方法
- 从STOMP CONNECT帧的`nativeHeaders`中提取`Authorization`头
- 验证JWT Token有效性（使用`JwtUtil`）
- 检查Token黑名单（使用`JwtTokenBlacklistService`）
- 设置`StompHeaderAccessor`的`user`属性（Principal）
- 对非CONNECT帧放行

**验证**: 发送有效的STOMP CONNECT帧，验证Principal是否设置正确

---

### Task 3: WebSocketConfig注册ChannelInterceptor

**优先级**: Critical
**依赖**: Task 2
**文件**: `backend/.../config/WebSocketConfig.java`

**操作**:
- 重写`configureClientInboundChannel`方法
- 注入`JwtUtil`和`JwtTokenBlacklistService`
- 注册`StompAuthInterceptor`

**验证**: 启动后端，发送STOMP CONNECT帧，观察日志

---

### Task 4: Chat.vue改进Token获取方式

**优先级**: High
**依赖**: 无（独立于后端修复）
**文件**: `frontend/src/views/user/Chat.vue:210`

**操作**:
- 从`userStore`获取token（假设store中已有）而非从cookie解析
- 如果store中没有token，fallback到cookie

**验证**: 打开Chat页面，检查控制台WebSocket连接日志

---

### Task 5: Notifications.vue添加独立WebSocket连接

**优先级**: High
**依赖**: Task 4
**文件**: `frontend/src/views/user/Notifications.vue`

**操作**:
- 在`onMounted`中获取token和userId
- 调用`wsService.connect(token, userId)`
- 连接成功后订阅通知频道

**验证**: 先进入Notifications页面（不经过Chat），检查WebSocket是否连接成功

---

### Task 6: .env.development添加VITE_WS_PORT

**优先级**: Low
**依赖**: 无
**文件**: `frontend/.env.development`

**操作**:
- 在文件末尾添加`VITE_WS_PORT=7000`

**验证**: 启动前端，检查控制台WebSocket连接URL是否使用正确端口

## 任务依赖图

```
Task 1 ──→ Task 2 ──→ Task 3
                          │
Task 4 ──→ Task 5         │
                          │
              Task 6 (独立)
```

## 预计工作量

| 任务 | 预计时间 |
|------|----------|
| Task 1 | 5分钟 |
| Task 2 | 20分钟 |
| Task 3 | 5分钟 |
| Task 4 | 5分钟 |
| Task 5 | 10分钟 |
| Task 6 | 2分钟 |

**总计**: ~50分钟
