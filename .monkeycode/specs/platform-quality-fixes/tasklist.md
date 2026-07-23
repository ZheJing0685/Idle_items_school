# 需求实施计划

## 上下文说明

本计划基于《闲置物品校园交易平台问题分析报告》中列出的 35 个问题，按 P0 → P3 优先级排列。P0 为阻塞级问题（运行时崩溃 / 构建失败 / 远程代码执行漏洞），P1 为高危问题（安全缺陷 / 数据一致性风险 / 架构违规），P2 为中等问题（代码质量 / 可维护性），P3 为低优先级优化项。

---

- [x] 1. 修复前端运行时阻塞问题
  - [x] 1.1 创建缺失的 `frontend/src/utils/logger.ts` 模块 —— 问题 #1
    - 导出 `logger` 对象，至少提供 `error`、`warn`、`log` 方法
    - 将 6 个引用文件 (`storage/index.ts`, `error/errorHandler.ts`, `websocket.ts`, `store/config.ts`, `store/dict.ts`, `views/Home.vue`) 的导入恢复可用
  - [x] 1.2 修复 `frontend/store/modules/item.ts` 中的空指针风险 —— 问题 #18
    - 在访问 `response.data.content` 和 `response.data.totalElements` 前增加 null/undefined 保护
    - 对齐 `fetchHotItems` 的 `response.code === 200` 检查模式，其他方法统一
  - [x] 1.3 修复 `clearAuthState()` 空实现 —— 问题 #19
    - 实现退出登录时的 Token 清理逻辑（清除 Cookie、localStorage 中的认证状态）
  - [ ]* 1.4 为 logger 模块和修复后的 store 逻辑编写单元测试

- [x] 2. 检查点 —— 确认前端核心运行时不再抛出 import 错误和 TypeError

- [x] 3. 修复前端安全漏洞与构建配置
  - [x] 3.1 升级 npm 高危依赖 —— 问题 #4、#7、#8、#9
    - `vitest` → ^4.1.10、`@vitest/coverage-v8` → 跟随 vitest
    - `happy-dom` → ^20.11.1
    - `axios` → ^1.18.0
    - `vite` → ^8.0.16
    - `vue-i18n` → ^10.0.8
  - [x] 3.2 修复 `vite.config.ts` 中 `allowedHosts: true` 为 `['.monkeycode-ai.online']` —— 问题 #6
  - [x] 3.3 修复 `frontend/playwright.config.js` 硬编码 Windows 路径 —— 问题 #3
    - 修改 `executablePath` 为条件判断：CI 环境使用 undefined，本地开发使用 Windows 路径

- [x] 4. 检查点 —— 确认 `npm audit` 无 critical/high 漏洞，vite 配置合规

- [ ] 5. 修复 Docker 部署基础设施
  - [ ] 5.1 创建缺失的 `frontend/Dockerfile.separate` —— 问题 #2
    - 参考已有的 `frontend/Dockerfile` 结构，适配 docker-compose.yml 中的环境变量配置
  - [ ] 5.2 修复 `deploy/Dockerfile` 中的路径引用 —— 问题 #5
    - 第 9 行 `combined/nginx.conf` → `deploy/nginx.conf`
    - 第 10 行 `combined/supervisord.conf` → `deploy/supervisord.conf`
  - [ ] 5.3 修复 `.gitignore` 不应忽略自身 —— 问题 #10
    - 删除 `.gitignore` 中第 171 行 `.gitignore` 自引用
  - [ ] 5.4 完善 3 个 `.env.example` 文件 —— 问题 #24
    - 根目录 `.env.example`：补充 `ENCRYPTION_SECRET_KEY`，统一 `SPRING_PROFILES_ACTIVE` 与 compose 一致
    - `backend/.env.example`：补充 `DB_URL` 或 `SPRING_DATASOURCE_URL` 占位符
    - `frontend/.env.example`：补充 `VITE_APP_TITLE` 等前端基础变量
  - [ ] 5.5 修复 `docker-compose.override.yml.example` 兼容性 —— 问题 #25
    - 移除废弃的 `version: '3.8'` 声明

- [ ] 6. 检查点 —— 确认 `docker compose config` 无报错

- [ ] 7. 修复后端输入校验缺失 —— 安全关键项
  - [ ] 7.1 为 `AdminBatchController` 6 个 POST 方法添加输入校验 —— 问题 #11
    - `batchApproveItems`、`batchRejectItems`、`batchOffShelfItems` 等接口
    - 将 `List<Long>` 包装为带 `@Valid` 的 DTO，增加非空和数量上限校验
  - [ ] 7.2 为 `AdminCategoryController` 7 个接口添加 `@Valid` —— 问题 #12
    - 分类 CURD 接口全部补全 `@Valid` 注解
  - [ ] 7.3 为 `UserController.updateProfile` 添加 `@Valid` —— 问题 #13
    - 确保 `UpdateProfileRequest` 的 Bean Validation 注解生效
  - [ ]* 7.4 为修复后的 Controller 编写参数校验单元测试

- [ ] 8. 修复后端架构违规
  - [ ] 8.1 将 `@Transactional` 从 Controller 层迁移到 Service 层 —— 问题 #14
    - `AdminBatchController`：创建 `AdminBatchService`，将事务注解和 Repository 操作下沉
    - `AdminCategoryController`：创建或复用已有 `CategoryCommandService`，移除 Controller 上的 `@Transactional`
    - `AdminVerificationController`：将事务逻辑迁移到 `VerificationService`
  - [ ] 8.2 为缺少 `@Transactional` 的方法补齐注解 —— 问题 #15、#16
    - `AuthServiceImpl.login()` 中 `userRepository.save(user)` 上加 `@Transactional`
    - `PasswordResetServiceImpl.resetPassword()` 中密码更新 + Token 失效加 `@Transactional`
  - [ ] 8.3 修复 `AdminLogService.logOperation()` 事务隔离 —— 问题 #15
    - 添加 `@Transactional(propagation = REQUIRES_NEW)` 确保日志不因主事务回滚丢失
  - [ ] 8.4 修复 `StatisticsController` 直接注入 Repository 的架构违规 —— 问题 #17
    - 创建 `StatisticsService`，将 4 个 Repository 依赖和查询逻辑迁移到 Service 层
  - [ ]* 8.5 为新创建的 Service 层方法编写单元测试

- [ ] 9. 检查点 —— 确认后端编译通过，分层架构符合规范

- [ ] 10. 修复后端代码质量与安全问题
  - [ ] 10.1 移除 `application-test.yml` 中的硬编码测试密钥 —— 问题 #22
    - JWT secret 和 encryption secret-key 改为仅从环境变量读取，无默认值
  - [ ] 10.2 移除 `pom.xml` 中 `-Xlint:-deprecation` 抑制 —— 问题 #23
    - 改为 `-Xlint:deprecation`，让弃用 API 告警在编译期可见
  - [ ] 10.3 修复 `ErrorHandler` "静默后重抛" 矛盾模式 —— 问题 #26
    - 统一行为：`silent: true` 时不应再 `throw error`
  - [ ] 10.4 清理 `StorageServiceFactory.java` 中的 Dead Code —— 问题 #29
    - 移除注释掉的 S3/OSS 适配器分支
  - [ ] 10.5 统一 `StorageServiceFactory.java` 注入方式 —— 问题 #31
    - 将 `@Autowired` 字段注入改为构造函数注入，与其他 200+ 个类一致

- [ ] 11. 修复前端代码质量与规范问题
  - [ ] 11.1 替换 25 处 `console.error` 为 `logger.error` —— 问题 #21
    - 涉及文件：`SellerStore.vue`、`VerificationManagement.vue`、`Statistics.vue`、`Dashboard.vue`、`Chat.vue`、`Notifications.vue`、`Items.vue`、`UserCenter.vue`、`Login.vue`、`router/index.ts`、`Header.vue`
  - [ ] 11.2 修复 `UserCenter.vue` 中 `<a href="/admin">` 为 `<router-link to="/admin">` —— 问题 #32
  - [ ] 11.3 修复 `router/index.ts` 中 `setTimeout` 恢复滚动为 `nextTick` —— 问题 #33
  - [ ] 11.4 修复 `eslint.config.js` 中的重复规则 —— 问题 #34
    - 删除第 42 行被覆盖的 `'no-unused-vars': 'off'`
  - [ ] 11.5 修复 `typescript-eslint` 未声明依赖 —— 问题 #28
    - 在 `frontend/package.json` 的 `devDependencies` 中显式声明
  - [ ] 11.6 移除 `eslint.config.js` 中 Vue 文件的冗余 `globals` 块 —— 问题 #33
  - [ ] 11.7 在 `application-common.yml` 中显式声明 `file.chunk-size` 配置 —— 问题 #30
  - [ ] 11.8 将 `vite.config.ts` 中 `emptyOutDir` 设为 `true` —— 问题 #35

- [ ] 12. 修复 CI/CD 流水线
  - [ ] 12.1 消除 `backend-tests.yml` 和 `test.yml` 的重复后端测试作业 —— 问题 #20
    - 将后端测试逻辑抽取为可复用 workflow，两个 workflow 统一引用
  - [ ]* 12.2 为 CI workflow 变更编写验证

- [ ] 13. 检查点 —— 确认 CI workflow 无重复执行，ESLint 配置无冗余规则

- [ ] 14. 清理冗余文件
  - [ ] 14.1 删除 `scripts/run-tests.bat` Windows 批处理文件 —— 问题 #35

- [ ] 15. 最终检查点 —— 确认全量修复完成并通过检查清单
