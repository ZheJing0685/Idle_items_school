# WebSocket代码审查报告

## 审查概述

**审查日期**: 2026年5月9日
**审查范围**: WebSocket前后端实现代码
**审查方法**: 使用code-review、systematic-debugging、senior-security技能进行全面分析

## 关键问题

### 1. 安全问题 (Critical)

#### 1.1 WebSocket身份验证不完整
- **文件**: `frontend/src/utils/websocket.js:37`
- **问题**: 虽然在STOMP CONNECT帧中发送了Authorization头，但WebSocket连接本身可能没有HTTPS保护
- **影响**: 中间人攻击可能截获Token
- **修复**: 确保生产环境使用wss://协议

#### 1.2 硬编码端口
- **文件**: `frontend/src/utils/websocket.js:22`
- **问题**: 硬编码端口7000，不利于部署和维护
- **影响**: 部署时需要修改代码
- **修复**: 从环境变量读取WebSocket地址

#### 1.3 CORS配置过于宽松
- **文件**: `backend/src/main/java/com/idleitems/school/config/WebSocketConfig.java:19`
- **问题**: 默认允许`http://localhost:5173`，生产环境可能需要更严格的配置
- **影响**: 可能允许未授权的跨域请求
- **修复**: 生产环境使用环境变量配置允许的源

### 2. 正确性问题 (High)

#### 2.1 STOMP帧解析错误
- **文件**: `frontend/src/utils/websocket.js:76`
- **问题**: `line.split(':')`只分割第一个冒号，但STOMP头可能包含冒号
- **影响**: 消息解析失败
- **修复**: 使用`line.split(':', 2)`或更正确的解析逻辑

#### 2.2 STOMP命令处理错误
- **文件**: `frontend/src/utils/websocket.js:86`
- **问题**: 检查`headers['command']`，但STOMP帧的第一行是命令，不是头
- **影响**: 消息可能无法正确处理
- **修复**: 正确解析STOMP帧结构

#### 2.3 消息丢失风险
- **文件**: `backend/src/main/java/com/idleitems/school/controller/ChatController.java:67-70`
- **问题**: 消息只发送给接收者，如果用户不在线，消息会丢失
- **影响**: 消息丢失，用户体验差
- **修复**: 实现消息持久化和离线消息队列

#### 2.4 通知推送失败无重试
- **文件**: `backend/src/main/java/com/idleitems/school/service/impl/NotificationServiceImpl.java:96-112`
- **问题**: WebSocket通知发送失败只记录日志，没有重试机制
- **影响**: 通知可能丢失
- **修复**: 实现重试机制或使用消息队列

### 3. 性能问题 (Medium)

#### 3.1 无指数退避重连
- **文件**: `frontend/src/utils/websocket.js:177`
- **问题**: 固定5秒重连间隔，没有指数退避
- **影响**: 服务器压力大，重连成功率低
- **修复**: 实现指数退避算法

#### 3.2 无最大重连次数限制
- **文件**: `frontend/src/utils/websocket.js:165-178`
- **问题**: 没有最大重连次数限制
- **影响**: 无限重连，浪费资源
- **修复**: 设置最大重连次数

#### 3.3 无心跳机制
- **文件**: `backend/src/main/java/com/idleitems/school/config/WebSocketConfig.java`
- **问题**: 没有配置WebSocket心跳机制
- **影响**: 连接可能被中间件断开
- **修复**: 配置STOMP心跳

### 4. 可维护性问题 (Low)

#### 4.1 代码注释不一致
- **文件**: `frontend/src/utils/websocket.js`
- **问题**: 代码注释是中文，但变量名是英文
- **影响**: 代码可读性降低
- **修复**: 统一注释语言

#### 4.2 缺少单元测试
- **问题**: 没有WebSocket相关的单元测试
- **影响**: 代码质量无法保证
- **修复**: 添加WebSocket单元测试

#### 4.3 错误处理不完善
- **文件**: `frontend/src/utils/websocket.js`
- **问题**: 缺少全局错误边界处理
- **影响**: 错误可能导致应用崩溃
- **修复**: 添加错误边界处理

## 修复建议

### 立即修复 (Critical)

1. **修复STOMP帧解析**
```javascript
// 修复前
const [key, value] = line.split(':');

// 修复后
const colonIndex = line.indexOf(':');
if (colonIndex > 0) {
  const key = line.substring(0, colonIndex).trim();
  const value = line.substring(colonIndex + 1).trim();
  headers[key] = value;
}
```

2. **修复STOMP命令解析**
```javascript
// 修复前
if (headers['command'] === 'MESSAGE' || headers['content-type'] === 'application/json') {

// 修复后
const command = lines[0].trim();
if (command === 'MESSAGE' || headers['content-type'] === 'application/json') {
```

3. **添加环境变量配置**
```javascript
// 修复前
const wsUrl = `${protocol}//${hostname}:7000/ws-native`;

// 修复后
const wsUrl = import.meta.env.VITE_WS_URL || `${protocol}//${hostname}:7000/ws-native`;
```

### 短期修复 (High)

1. **实现指数退避重连**
```javascript
scheduleReconnect(token, userId, attempt = 0) {
  const maxAttempts = 10;
  const baseDelay = 1000;
  const maxDelay = 30000;
  
  if (attempt >= maxAttempts) {
    console.log('达到最大重连次数，停止重连');
    return;
  }
  
  const delay = Math.min(baseDelay * Math.pow(2, attempt), maxDelay);
  
  this.reconnectTimer = setTimeout(() => {
    console.log(`尝试重新连接WebSocket... (第${attempt + 1}次)`);
    this.connect(token, userId).then(() => {
      this.subscribeToUserChannel(userId);
    }).catch(() => {
      this.scheduleReconnect(token, userId, attempt + 1);
    });
  }, delay);
}
```

2. **添加WebSocket心跳**
```java
// WebSocketConfig.java
@Override
public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
    registry.setMessageSizeLimit(128 * 1024);
    registry.setSendBufferSizeLimit(512 * 1024);
    registry.setSendTimeLimit(20 * 1000);
}
```

### 中期修复 (Medium)

1. **实现消息持久化**
2. **添加离线消息队列**
3. **实现消息确认机制**
4. **添加WebSocket单元测试**

## 使用的技能和工具

1. **find-skills**: 查找WebSocket相关技能
2. **code-review**: 代码审查检查清单
3. **systematic-debugging**: 系统化调试方法
4. **fullstack-developer**: 全栈开发最佳实践
5. **senior-security**: 安全架构分析
6. **MCP服务器**:
   - 文件搜索: 查找WebSocket相关文件
   - 内容搜索: 搜索WebSocket关键词
   - 文件读取: 读取代码文件
   - Bash: 运行命令和脚本

## 结论

WebSocket实现存在多个安全、正确性和性能问题。建议立即修复Critical级别问题，短期修复High级别问题。需要添加消息持久化、心跳机制和单元测试来提高系统可靠性。