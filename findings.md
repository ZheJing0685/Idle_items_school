# 审计发现汇总

## 一、后端安全问题（严重）

### S-1: JWT密钥硬编码且可预测
- **位置**: `backend/.env`
- **详情**: JWT_SECRET解码后为 `dev-secret-key-for-development-only-32bytes`
- **影响**: 攻击者可伪造任意JWT令牌
- **修复**: 使用强随机生成的256位以上密钥

### S-2: 数据库密码使用root/root
- **位置**: `backend/.env`
- **详情**: DB_USERNAME=root, DB_PASSWORD=root
- **影响**: 数据库完全暴露
- **修复**: 使用强密码，限制数据库用户权限

### S-3: ConfigController缺少权限控制
- **位置**: `backend/src/main/java/.../controller/ConfigController.java`
- **详情**: 任何已认证用户都可以修改系统配置
- **影响**: 权限提升漏洞
- **修复**: 添加 `@RequireRole({User.Role.ADMIN})` 注解

### S-4: @RequireRole注解未生效
- **位置**: 项目全局
- **详情**: 注解已定义但没有对应的AOP切面处理
- **影响**: 权限控制形同虚设
- **修复**: 实现RequireRoleAspect切面

### S-5: /uploads/** 静态资源完全公开
- **位置**: `SecurityConfig.java`
- **详情**: 身份证、学生证等敏感文件可被任何人访问
- **影响**: 隐私泄露风险
- **修复**: 添加访问控制或签名URL机制

### S-6: JWT角色从Token直接读取
- **位置**: `SecurityConfig.java`
- **详情**: 用户角色变更后旧token仍有效
- **影响**: 降权操作无法立即生效
- **修复**: 每次请求从数据库验证用户状态

## 二、后端代码质量问题（严重）

### C-1: VerificationController代码完全重复
- **位置**: `VerificationController.java` 第33-75行 vs 第103-145行
- **详情**: submitVerification和resubmitVerification方法实现完全相同
- **修复**: 抽取公共方法

### C-2: ItemService字段赋值代码重复
- **位置**: `ItemService.java` 第106-139行 vs 第363-400行
- **详情**: createItem和updateItem中约30行代码重复
- **修复**: 抽取parseItemFields方法

### C-3: 大量使用Map<String, Object>接收参数
- **位置**: 多个Controller
- **详情**: 绕过Spring Validation，缺乏类型安全
- **修复**: 使用DTO类接收参数

### C-4: 直接暴露Entity作为响应
- **位置**: 多个Controller
- **详情**: 数据库字段结构泄露给前端
- **修复**: 统一使用DTO/VO

### C-5: AdminController过于膨胀
- **位置**: `AdminController.java` (968行)
- **详情**: 包含用户、物品、订单、分类、认证、日志等全部管理功能
- **修复**: 拆分为多个独立Controller

### C-6: 缺乏自定义业务异常体系
- **位置**: 项目全局
- **详情**: 使用Java内置异常，无法区分不同业务错误
- **修复**: 创建BusinessException和ErrorCode枚举

## 三、前端安全与质量问题

### F-1: Cookie缺少SameSite属性
- **位置**: `frontend/src/api/config/axios.js`
- **详情**: setCookie未设置SameSite，存在CSRF风险
- **修复**: 添加 `sameSite: 'Strict'` 或 `'Lax'`

### F-2: 两套Store系统混用
- **位置**: `frontend/src/store/`
- **详情**: 根目录和modules目录下存在同名Store
- **修复**: 统一使用modules下的Store，废弃根目录版本

### F-3: 错误处理器重复定义
- **位置**: `api/config/errorHandler.js` 和 `utils/error/errorHandler.js`
- **详情**: 两个ErrorHandler类命名冲突，功能不同
- **修复**: 统一为一个错误处理系统

### F-4: ItemDetail.vue登录状态判断失效
- **位置**: `frontend/src/views/user/ItemDetail.vue`
- **详情**: localStorage key不匹配，token存储位置判断错误
- **修复**: 统一从Store获取登录状态

### F-5: classifyError函数逻辑错误
- **位置**: `frontend/src/utils/error/errorTypes.js`
- **详情**: 检查 `error.code` 而非 `error.response.status`
- **修复**: 修正错误分类逻辑

### F-6: 购买表单缺少验证
- **位置**: `frontend/src/views/user/ItemDetail.vue`
- **详情**: buyerName、buyerPhone、buyerAddress无必填验证
- **修复**: 添加表单验证规则

## 四、数据库相关问题

### D-1: 用户角色枚举值不一致
- **数据库**: `users.role` ENUM('STUDENT','ADMIN')
- **后端代码**: 部分检查 `ROLE_ADMIN`
- **前端代码**: 检查 `ADMIN` 和 `SUPER_ADMIN`
- **修复**: 统一角色枚举值

### D-2: 软删除与物理删除不一致
- **数据库**: `users.is_deleted` 字段存在
- **AdminController.deleteUser**: 使用 `deleteById()` 物理删除
- **修复**: 统一使用软删除

### D-3: Seller评分硬编码
- **位置**: `ItemService.java` 第281行、346行
- **详情**: sellerRating硬编码为5.0，未从评价表查询
- **修复**: 实现动态评分查询

## 五、配置与部署问题

### P-1: Staging环境暴露API文档
- **位置**: `application-staging.yml`
- **详情**: knife4j.enable=true
- **修复**: 生产和staging环境禁用

### P-2: CORS配置过于宽松
- **位置**: `SecurityConfig.java`
- **详情**: 允许所有HTTP方法和请求头
- **修复**: 限制为必要的方法

### P-3: Dockerfile以root运行
- **位置**: `backend/Dockerfile`
- **详情**: 未使用非root用户
- **修复**: 添加USER指令

### P-4: Nginx缺少安全头
- **位置**: `frontend/nginx.conf`
- **详情**: 缺少CSP、HSTS等安全头
- **修复**: 添加安全响应头

## 六、性能与可维护性问题

### M-1: 图片串行上传
- **位置**: `frontend/src/views/user/Publish.vue`
- **详情**: 多张图片依次上传
- **修复**: 改为Promise.all并行上传

### M-2: 分页参数处理重复
- **位置**: 多个Controller
- **详情**: `PageRequest.of(page-1, size, Sort.by(...))` 重复10+次
- **修复**: 抽取工具方法

### M-3: 卖家信息填充逻辑重复
- **位置**: UserService、ItemService、AdminController
- **详情**: 相同逻辑在4个地方实现
- **修复**: 统一到UserService

---

## 统计汇总

| 类别 | 严重 | 中等 | 低 | 总计 |
|------|------|------|-----|------|
| 后端安全 | 6 | 8 | 4 | 18 |
| 后端代码 | 6 | 12 | 8 | 26 |
| 前端问题 | 6 | 10 | 6 | 22 |
| 配置部署 | 0 | 4 | 2 | 6 |
| **总计** | **18** | **34** | **20** | **72** |
