# 闲置物品校园交易平台技术修复需求文档

## 介绍

闲置物品校园交易平台经过全维度技术诊断与合规性评估，发现了42项技术债务和安全隐患。本修复计划旨在解决这些关键问题，将系统从"可运行"状态提升至"生产就绪"水平。

**当前问题**：
- 安全层面存在XSS漏洞、JWT无黑名单、敏感数据未脱敏、Redis无密码等严重隐患
- 架构层面存在Controller绕过Service、Entity直接暴露、Map替代DTO、缓存策略混乱等问题
- 业务层面存在订单并发竞态、退款状态错误、收藏计数不同步等漏洞
- 工程层面存在测试覆盖不足、CI/CD不完整、文档缺失等问题

**修复目标**：
- 消除所有P0级安全漏洞
- 修复核心业务逻辑缺陷
- 统一架构设计规范
- 完善工程实践体系

## 术语表

- **JWT**: JSON Web Token，用于无状态身份认证的令牌机制
- **XSS**: 跨站脚本攻击(Cross-Site Scripting)，一种代码注入攻击方式
- **DTO**: 数据传输对象(Data Transfer Object)，用于层间数据传递的载体
- **Entity**: 实体类，与数据库表映射的Java对象
- **悲观锁**: 数据库锁定机制，事务执行期间锁定数据行
- **缓存穿透**: 查询不存在的数据时，每次都穿透到数据库
- **熔断降级**: 系统故障时的保护机制，防止级联失败
- **N+1查询**: 产生1+N次SQL查询的性能问题

## 需求

### 需求 1: XSS过滤器修复
**用户故事:** 作为系统管理员，我希望XSS过滤器能完整处理所有请求数据，以便防止跨站脚本攻击

#### 验收标准
1. WHEN 接收包含JSON数组的请求 THEN 系统SHALL 对数组中的字符串值进行XSS过滤
2. WHEN 过滤器处理表单数据 THEN 系统SHALL 对所有表单字段进行XSS清洗
3. WHEN 过滤器处理JSON请求体 THEN 系统SHALL 递归过滤所有嵌套的字符串值
4. WHEN 过滤器处理请求头 THEN 系统SHALL 对自定义请求头进行XSS清洗

### 需求 2: JWT Token安全加固
**用户故事:** 作为用户，我希望在登出或密码修改后旧Token立即失效，以便保护我的账户安全

#### 验收标准
1. WHEN 用户执行登出操作 THEN 系统SHALL 将当前Token加入Redis黑名单
2. WHEN 用户修改密码 THEN 系统SHALL 使该用户所有Token失效
3. WHEN 系统验证Token THEN 系统SHALL 先检查Token是否在黑名单中
4. WHEN Token在黑名单中 THEN 系统SHALL 拒绝请求并返回401状态码
5. WHEN 管理员禁用用户 THEN 系统SHALL 使该用户所有Token立即失效

### 需求 3: 敏感数据脱敏
**用户故事:** 作为用户，我希望我的手机号和身份证号在系统中被安全存储和脱敏显示，以便保护个人隐私

#### 验收标准
1. WHEN 系统存储手机号 THEN 系统SHALL 使用AES-256加密存储
2. WHEN 系统存储身份证号 THEN 系统SHALL 使用AES-256加密存储
3. WHEN API返回用户信息 THEN 系统SHALL 对手机号进行脱敏处理(138****1234)
4. WHEN API返回实名认证信息 THEN 系统SHALL 对身份证号进行脱敏处理(1101****1234)
5. WHEN 管理后台查看用户详情 THEN 系统SHALL 仅授权用户可查看完整信息

### 需求 4: Redis安全配置
**用户故事:** 作为运维人员，我希望Redis服务配置密码认证，以便防止未授权访问

#### 验收标准
1. WHEN 部署Redis服务 THEN 系统SHALL 配置强密码认证
2. WHEN 应用连接Redis THEN 系统SHALL 使用密码进行认证
3. WHEN 缓存清除操作执行 THEN 系统SHALL 使用SCAN命令替代KEYS命令
4. WHEN Redis服务不可用 THEN 系统SHALL 降级放行请求而非拒绝所有请求

### 需求 5: WebSocket安全验证
**用户故事:** 作为用户，我希望聊天消息的发送者身份被验证，以便防止消息伪造

#### 验收标准
1. WHEN 客户端发送WebSocket消息 THEN 系统SHALL 验证senderId与认证用户一致
2. WHEN senderId与认证用户不匹配 THEN 系统SHALL 拒绝消息并返回错误
3. WHEN WebSocket连接建立 THEN 系统SHALL 验证用户身份有效性

### 需求 6: 订单并发安全
**用户故事:** 作为买家，我希望订单操作不会因并发导致数据不一致，以便保障交易安全

#### 验收标准
1. WHEN 执行订单状态变更操作 THEN 系统SHALL 使用悲观锁保护
2. WHEN 用户A取消订单同时用户B支付 THEN 系统SHALL 保证只有一个操作成功
3. WHEN 订单状态变更完成 THEN 系统SHALL 更新关联的缓存数据
4. WHEN 退款被拒绝 THEN 系统SHALL 根据原订单状态正确回退(非固定PENDING_SHIPMENT)

### 需求 7: 收藏计数同步
**用户故事:** 作为用户，我希望收藏物品时物品的收藏数量能准确更新，以便了解物品热度

#### 验收标准
1. WHEN 用户收藏物品 THEN 系统SHALL 原子性增加item.favoriteCount
2. WHEN 用户取消收藏 THEN 系统SHALL 原子性减少item.favoriteCount
3. WHEN 查询物品详情 THEN 系统SHALL 返回准确的收藏数量

### 需求 8: 浏览量准确计数
**用户故事:** 作为用户，我希望物品的浏览量能准确反映实际访问次数，以便了解物品关注度

#### 验收标准
1. WHEN 用户访问物品详情 THEN 系统SHALL 仅增加一次浏览量(非+2)
2. WHEN 浏览量更新 THEN 系统SHALL 使用异步方式减少响应延迟
3. WHEN 浏览量缓存存在 THEN 系统SHALL 仅使用异步更新(非同步+异步双重更新)

### 需求 9: 异常处理完善
**用户故事:** 作为开发者，我希望系统能统一处理所有异常类型，以便提供一致的错误响应

#### 验收标准
1. WHEN 抛出BusinessException THEN 系统SHALL 返回对应的HTTP状态码(非固定200)
2. WHEN 抛出JwtException THEN 系统SHALL 返回401状态码
3. WHEN 抛出DataIntegrityViolationException THEN 系统SHALL 返回409状态码
4. WHEN 请求路径不存在 THEN 系统SHALL 返回404状态码
5. WHEN 请求方法不允许 THEN 系统SHALL 返回405状态码

### 需求 10: 前端Store统一
**用户故事:** 作为前端开发者，我希望有统一的Store管理方案，以便减少维护混乱

#### 验收标准
1. WHEN 项目中存在重复Store文件 THEN 系统SHALL 仅保留一份使用Composition API的版本
2. WHEN 导入Store THEN 系统SHALL 统一从store/index.js导出
3. WHEN 旧版Store存在 THEN 系统SHALL 删除根目录下的冗余文件

### 需求 11: 接口路径一致性
**用户故事:** 作为前后端开发者，我希望接口路径定义一致，以便提高联调效率

#### 验收标准
1. WHEN 后端定义接口路径 THEN 系统SHALL 使用ApiPaths常量(非硬编码)
2. WHEN 前后端定义路径常量 THEN 系统SHALL 保持路径完全一致
3. WHEN 发现路径不一致(如confirm vs confirm-receive) THEN 系统SHALL 统一修复

### 需求 12: 数据库备份策略
**用户故事:** 作为运维人员，我希望有自动化的数据库备份策略，以便在数据丢失时能恢复

#### 验收标准
1. WHEN 生产环境运行 THEN 系统SHALL 每日自动备份数据库
2. WHEN 备份完成 THEN 系统SHALL 校验备份文件完整性
3. WHEN 备份文件超过保留期 THEN 系统SHALL 自动清理旧备份
4. WHEN 需要恢复数据 THEN 系统SHALL 支持从备份文件恢复

### 需求 13: 测试覆盖提升
**用户故事:** 作为开发者，我希望核心业务模块有完善的测试覆盖，以便保障代码质量

#### 验收标准
1. WHEN ItemService代码变更 THEN 系统SHALL 有单元测试验证核心逻辑
2. WHEN CategoryService代码变更 THEN 系统SHALL 有单元测试验证核心逻辑
3. WHEN OrderService代码变更 THEN 系统SHALL 有单元测试验证核心逻辑
4. WHEN 测试执行 THEN 系统SHALL 覆盖率达到80%以上

### 需求 14: 死代码清理
**用户故事:** 作为开发者，我希望代码库保持整洁，以便提高可维护性

#### 验收标准
1. WHEN 存在废弃实体(Notification/OperationLog/UserFollow/UserAddress) THEN 系统SHALL 清理相关代码和数据库表
2. WHEN 存在未使用的Store(Cart Store) THEN 系统SHALL 删除相关文件
3. WHEN 存在测试控制器(TestController) THEN 系统SHALL 移除或限制仅dev环境
4. WHEN 存在JVM崩溃日志/dump.rdb THEN 系统SHALL 从仓库中移除
5. WHEN 存在前端dist编译产物 THEN 系统SHALL 删除所有dist目录
