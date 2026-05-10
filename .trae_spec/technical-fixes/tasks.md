# 闲置物品校园交易平台技术修复实施计划

## 概述

本实施计划基于 requirements.md 和 design.md，将14个修复需求拆解为可执行的任务。任务按优先级分为4个阶段，确保安全问题优先修复，核心业务逻辑其次，架构优化和运维支持最后完成。

## 任务列表

### 第一阶段：安全加固

- [x] 1. XSS过滤器修复
  - 1.1 修复XssHttpServletRequestWrapper中的数组处理逻辑
  - 1.2 添加JSON数组递归过滤支持
  - 1.3 编写单元测试验证修复效果
  - _需求: 1.1, 1.2, 1.3, 1.4_

- [x] 2. JWT Token黑名单机制
  - 2.1 创建JwtTokenBlacklistService服务类
  - 2.2 在Redis中实现Token黑名单存储
  - 2.3 修改JwtUtil.validateToken()检查黑名单
  - 2.4 在AuthService中实现logout()加入黑名单
  - 2.5 在AuthService中实现changePassword()使所有Token失效
  - 2.6 编写单元测试验证黑名单功能
  - _需求: 2.1, 2.2, 2.3, 2.4, 2.5_

- [x] 3. 敏感数据加密脱敏
  - 3.1 创建DataEncryptionUtil工具类(AES-256)
  - 3.2 创建DataMaskUtil工具类(手机号/身份证/邮箱脱敏)
  - 3.3 修改User.java添加加密字段
  - 3.4 修改VerificationRecord.java添加加密字段
  - 3.5 创建数据库迁移脚本添加加密列
  - 3.6 修改UserDTO返回脱敏数据
  - 3.7 编写单元测试验证加密解密
  - _需求: 3.1, 3.2, 3.3, 3.4, 3.5_

- [x] 4. Redis安全配置
  - 4.1 修改docker-compose.yml添加Redis密码
  - 4.2 修改application*.yml添加Redis密码配置
  - 4.3 修复ConfigService中KEYS命令改为SCAN
  - 4.4 修复DictService中KEYS命令改为SCAN
  - 4.5 修改RateLimitFilter添加降级处理
  - _需求: 4.1, 4.2, 4.3, 4.4_

- [x] 5. WebSocket身份验证
  - 5.1 修改ChatController.handleWebSocketMessage()验证senderId
  - 5.2 修改WebSocketConfig添加连接身份验证
  - 5.3 编写测试验证WebSocket安全
  - _需求: 5.1, 5.2, 5.3_

### 第二阶段：业务逻辑修复

- [x] 6. 订单并发安全
  - 6.1 修改OrderService.cancelOrder()使用悲观锁
  - 6.2 修改OrderService.shipOrder()使用悲观锁
  - 6.3 修改OrderService.confirmReceive()使用悲观锁
  - 6.4 修改OrderService.applyRefund()使用悲观锁
  - 6.5 修复approveRefund()退款拒绝状态回退逻辑
  - 6.6 编写并发测试验证订单安全
  - _需求: 6.1, 6.2, 6.3, 6.4_

- [x] 7. 收藏计数同步
  - 7.1 修改FavoriteService.addFavorite()原子性增加计数
  - 7.2 修改FavoriteService.removeFavorite()原子性减少计数
  - 7.3 在ItemRepository添加incrementFavoriteCount方法
  - 7.4 在ItemRepository添加decrementFavoriteCount方法
  - 7.5 编写单元测试验证计数同步
  - _需求: 7.1, 7.2, 7.3_

- [x] 8. 浏览量准确计数
  - 8.1 修改ItemService.getItemById()移除同步更新
  - 8.2 在ItemRepository添加incrementViewCount方法
  - 8.3 修改incrementViewCountAsync()使用Repository方法
  - 8.4 编写测试验证浏览量计数
  - _需求: 8.1, 8.2, 8.3_

- [x] 9. 异常处理完善
  - 9.1 在GlobalExceptionHandler添加JwtException处理
  - 9.2 在GlobalExceptionHandler添加AccessDeniedException处理
  - 9.3 在GlobalExceptionHandler添加DataIntegrityViolationException处理
  - 9.4 在GlobalExceptionHandler添加NoHandlerFoundException处理
  - 9.5 修复BusinessException返回正确HTTP状态码
  - 9.6 在ErrorCode枚举添加CONFLICT错误码
  - 9.7 编写单元测试验证异常处理
  - _需求: 9.1, 9.2, 9.3, 9.4, 9.5_

### 第三阶段：架构优化

- [x] 10. 前端Store统一
  - 10.1 删除src/store/user.js旧版文件
  - 10.2 删除src/store/item.js旧版文件
  - 10.3 删除src/store/cart.js旧版文件
  - 10.4 修改src/store/index.js统一导出
  - 10.5 更新所有导入Store的组件使用统一路径
  - 10.6 验证前端功能正常
  - _需求: 10.1, 10.2, 10.3_

- [x] 11. 接口路径一致性
  - 11.1 修改OrderController使用ApiPaths常量
  - 11.2 修改FavoriteController使用ApiPaths常量
  - 11.3 修改ReviewController使用ApiPaths常量
  - 11.4 修改ChatController使用ApiPaths常量
  - 11.5 修改VerificationController使用ApiPaths常量
  - 11.6 修改所有Admin子控制器使用ApiPaths常量
  - 11.7 修改前端paths.js统一CONFIRM路径为confirm-receive
  - 11.8 验证前后端接口联调正常
  - _需求: 11.1, 11.2, 11.3, 11.4, 11.5, 11.6, 11.7_

- [x] 12. 死代码清理
  - 12.1 删除Notification.java和NotificationRepository.java
  - 12.2 删除OperationLog.java和OperationLogRepository.java
  - 12.3 删除UserFollow.java和UserFollowRepository.java
  - 12.4 删除UserAddress.java和UserAddressRepository.java
  - 12.5 删除Cart Store相关文件
  - 12.6 删除TestController.java或限制仅dev环境
  - 12.7 从Git历史移除JVM崩溃日志(hs_err_pid*.log)
  - 12.8 从Git历史移除dump.rdb
  - 12.9 删除前端dist编译产物目录
  - 12.10 删除空包目录(handler/runner/migration/exception)
  - 12.11 更新.gitignore添加dump.rdb
  - 12.12 验证项目编译和功能正常
  - _需求: 14.1, 14.2, 14.3, 14.4, 14.5_

### 第四阶段：运维支持

- [x] 13. 数据库备份策略
  - 13.1 创建scripts/backup/backup.sh备份脚本
  - 13.2 创建scripts/backup/restore.sh恢复脚本
  - 13.3 修改docker-compose.yml添加备份服务
  - 13.4 配置定时任务每日自动备份
  - 13.5 编写备份验证脚本
  - _需求: 12.1, 12.2, 12.3, 12.4_

- [x] 14. 测试覆盖提升
  - 14.1 编写ItemService单元测试
  - 14.2 编写CategoryService单元测试
  - 14.3 编写OrderService单元测试
  - 14.4 编写FavoriteService单元测试
  - 14.5 编写UserService单元测试
  - 14.6 编写ChatService单元测试
  - 14.7 配置测试覆盖率报告
  - 14.8 验证测试覆盖率达到80%
  - _需求: 13.1, 13.2, 13.3, 13.4_

## 任务依赖关系

```
第一阶段 (安全加固)
    │
    ├── 1. XSS过滤器修复 (无依赖)
    ├── 2. JWT Token黑名单 (依赖Redis配置)
    ├── 3. 敏感数据加密脱敏 (无依赖)
    ├── 4. Redis安全配置 (无依赖)
    └── 5. WebSocket身份验证 (依赖Spring Security)
    
    ▼
    
第二阶段 (业务逻辑修复)
    │
    ├── 6. 订单并发安全 (无依赖)
    ├── 7. 收藏计数同步 (无依赖)
    ├── 8. 浏览量准确计数 (无依赖)
    └── 9. 异常处理完善 (依赖JWT修复)
    
    ▼
    
第三阶段 (架构优化)
    │
    ├── 10. 前端Store统一 (无依赖)
    ├── 11. 接口路径一致性 (无依赖)
    └── 12. 死代码清理 (依赖所有修复完成)
    
    ▼
    
第四阶段 (运维支持)
    │
    ├── 13. 数据库备份策略 (无依赖)
    └── 14. 测试覆盖提升 (依赖所有修复完成)
```

## 验证清单

### 安全验证
- [x] XSS过滤器能处理JSON数组
- [x] JWT Token登出后立即失效
- [x] 手机号/身份证返回脱敏数据
- [x] Redis连接需要密码认证
- [x] WebSocket消息发送者身份已验证

### 功能验证
- [x] 订单取消/发货/收货操作并发安全
- [x] 收藏/取消收藏计数准确
- [x] 浏览量每次访问仅+1
- [x] 异常返回正确HTTP状态码
- [x] 前后端接口路径一致

### 架构验证
- [x] 前端仅有一套Store文件
- [x] 所有Controller使用ApiPaths常量
- [x] 废弃代码已清理
- [ ] 测试覆盖率达到80%

### 运维验证
- [x] 数据库自动备份正常
- [x] 备份恢复流程可用
- [x] 所有单元测试通过
