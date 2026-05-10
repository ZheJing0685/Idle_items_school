# Spec: 修复WebSocket连接失败

## Why

WebSocket连接始终失败，前端控制台显示`WebSocket错误`，后端无任何连接日志。经过根因分析，发现以下核心问题：

1. **Spring Security拦截WebSocket端点**：`/ws`和`/ws-native`未列入`permitAll()`，被`.anyRequest().authenticated()`拦截
2. **缺少ChannelInterceptor**：无STOMP层面的认证机制，无法从CONNECT帧中提取JWT Token
3. **Notifications页面无独立连接**：依赖Chat页面先建立连接
4. **Token获取方式脆弱**：从cookie中硬编码key提取token

## What Changes

### MODIFIED

| 文件 | 变更 |
|------|------|
| `backend/.../config/SecurityConfig.java` | 添加`/ws/**`和`/ws-native/**`到`permitAll()`列表 |
| `backend/.../config/WebSocketConfig.java` | 添加`ClientInboundChannel`的`ChannelInterceptor`进行STOMP认证 |
| `frontend/src/views/user/Chat.vue` | 改进Token获取方式，使用store而非cookie |
| `frontend/src/views/user/Notifications.vue` | 添加WebSocket连接和订阅逻辑 |
| `frontend/.env.development` | 添加`VITE_WS_PORT=7000` |

### ADDED

| 文件 | 说明 |
|------|------|
| `backend/.../websocket/StompAuthInterceptor.java` | STOMP CONNECT帧JWT认证拦截器 |

## Impact

- **安全性**：WebSocket端点开放但通过ChannelInterceptor进行STOMP层认证，安全不下滑
- **兼容性**：前端连接逻辑不变，修复后连接立即可用
- **测试**：需验证HTTP OPTIONS预检请求和WebSocket升级握手均通过

## Root Cause

```
new WebSocket("ws://localhost:7000/ws-native")
  → HTTP Upgrade Request (无法携带自定义Header)
    → Spring Security JWT Filter (无Authorization头)
      → .anyRequest().authenticated() → 拒绝连接
        → WebSocket连接失败
```

## Solution Design

```
浏览器                          Spring Security
  |                                  |
  |-- HTTP Upgrade /ws-native ----->|  (permitAll → 放行)
  |                                  |
  |<-- 101 Switching Protocols -----|
  |                                  |
  |-- STOMP CONNECT (Authorization)---> ChannelInterceptor
  |   帧中包含Bearer Token            |   提取JWT → 验证
  |                                  |   设置Principal
  |<-- STOMP CONNECTED --------------|
  |                                  |
  |-- STOMP SUBSCRIBE /topic/... --->|  (已认证 → 放行)
```
