# 需求实施计划

- [ ] 1. 定时任务分布式锁
  - 在 `backend/pom.xml` 中添加 `shedlock-spring` 和 `shedlock-provider-redis-spring` 依赖
  - 在 `SchedulingConfig` 中配置 `LockProvider`（基于 Redis），启用 `@EnableSchedulerLock(defaultLockAtMostFor = "PT30S")`
  - 为 `AutoConfirmReceiveTask.java` 添加 `@SchedulerLock(name = "autoConfirmReceive", lockAtLeastFor = "PT5S", lockAtMostFor = "PT30S")`
  - 为 `OrderTimeoutTask.java` 添加 `@SchedulerLock(name = "orderTimeout", lockAtLeastFor = "PT5S", lockAtMostFor = "PT30S")`
  - 为 `FavoriteCountSyncTask.java` 添加 `@SchedulerLock(name = "favoriteCountSync", lockAtLeastFor = "PT5S", lockAtMostFor = "PT30S")`
  - 为 `ViewCountService.java` 中的定时方法添加 `@SchedulerLock(name = "viewCountSync", lockAtLeastFor = "PT5S", lockAtMostFor = "PT30S")`
  - 为 `NotificationServiceImpl.java` 中的定时方法添加 `@SchedulerLock(name = "notificationTask", lockAtLeastFor = "PT5S", lockAtMostFor = "PT30S")`
  - [ ] 1.1 编写定时任务分布式锁的单元测试，验证多实例场景下只有一个实例执行

- [ ] 2. 安全检查点 - 确保后端编译通过，所有现有测试通过

- [ ] 3. 生产安全加固
  - [ ] 3.1 修复 Actuator 端点暴露
    - 修改 `SecurityConfig.java`，将 `/actuator/**` 的 `permitAll()` 改为仅对 `/actuator/health` 和 `/actuator/info` 放行，其余端点要求认证
    - 修改 `application-common.yml`，仅暴露 `health,info`（移除 `metrics,prometheus` 的公开暴露，改为内部认证访问）

  - [ ] 3.2 启用数据库 SSL 连接
    - 修改 `application-prod.yml`，将 `useSSL=false` 改为 `useSSL=true&requireSSL=true`
    - 在 `application-prod.yml` 中补充 SSL 信任证书参数（通过环境变量注入）

  - [ ] 3.3 关闭 Flyway 自动修复
    - 修改 `application-prod.yml`，添加 `spring.flyway.repair-on-migrate: false`

  - [ ] 3.4 修复 CORS 通配符配置
    - 修改 `SecurityConfig.java`，将 `addAllowedHeader("*")` 和 `addAllowedMethod("*")` 改为显式列表（`Authorization, Content-Type, X-Requested-With` 和 `GET, POST, PUT, DELETE, OPTIONS`）

  - [ ] 3.5 修复 docker-compose CORS 硬编码
    - 修改 `docker-compose.yml`，将 CORS_ALLOWED_ORIGINS 的硬编码值改为 `${CORS_ALLOWED_ORIGINS}`

- [ ] 4. 安全检查点 - 确保后端编译通过，所有现有测试通过

- [ ] 5. 容器化与部署加固
  - [ ] 5.1 为 backend Dockerfile 添加 HEALTHCHECK 和非 root 用户
    - 修改 `backend/Dockerfile.separate`，添加 `HEALTHCHECK` 指令（间隔 15s，超时 3s）
    - 添加 `USER appuser` 指令切换到非 root 用户

  - [ ] 5.2 为 frontend Dockerfile 添加 HEALTHCHECK
    - 修改 `frontend/Dockerfile.separate`，添加 `HEALTHCHECK` 指令（间隔 15s，超时 3s）

  - [ ] 5.3 添加优雅关闭配置
    - 修改 `application-prod.yml`，添加 `server.shutdown: graceful` 和 `spring.lifecycle.timeout-per-shutdown-phase: 30s`

  - [ ] 5.4 补充 docker-compose MySQL 服务定义
    - 修改 `docker-compose.yml`，添加 MySQL 8.0 服务定义（含数据卷挂载和 healthcheck）
    - backend 服务添加 `depends_on mysql` 条件

- [ ] 6. Nginx 安全加固
  - [ ] 6.1 添加 CSP 安全头
    - 修改 `frontend/nginx.conf`，在 `add_header` 块中添加 `Content-Security-Policy` 头

  - [ ] 6.2 添加 HSTS 头
    - 修改 `frontend/nginx.conf`，添加 `Strict-Transport-Security` 头（max-age=31536000）

  - [ ] 6.3 添加 HTTPS 强制重定向
    - 修改 `frontend/nginx.conf`，在 server 块中添加基于 `X-Forwarded-Proto` 的 301 重定向

- [ ] 7. 日志与监控完善
  - [ ] 7.1 调整生产日志级别
    - 修改 `application-prod.yml`，将 `com.idleitems.school` 和 `root` 日志级别从 `WARN` 调整为 `INFO`

  - [ ] 7.2 添加文件日志输出与滚动策略
    - 修改 `application-common.yml`，添加 `logging.file.name` 配置
    - 修改 `logback.xml`，添加 `RollingFileAppender`（按日期和大小滚动，保留 30 天）

  - [ ] 7.3 补充生产邮件配置
    - 修改 `application-prod.yml`，添加 `spring.mail` 配置块（全部参数通过环境变量注入）

- [ ] 8. 安全检查点 - 确保后端编译通过，所有现有测试通过

- [ ] 9. 性能优化
  - [ ] 9.1 为热点查询添加 Redis 缓存
    - 在 `CategoryQueryService` 的分类树查询方法上添加 `@Cacheable(value = "categoryTree", key = "'all'")`
    - 在 `CategoryCommandService` 的增删改方法上添加 `@CacheEvict(value = "categoryTree", allEntries = true)`
    - 在 `HomeController.getHomeStats()` 上添加 `@Cacheable(value = "homeStats", key = "'stats'")`

  - [ ] 9.2 修复关键查询的 N+1 问题
    - 修改 `ItemRepository`，为 `findByUserId` 和 `findByCategoryId` 添加 `@EntityGraph(attributePaths = {"images", "tags"})`
    - 修改 `OrderRepository`，为 `findByBuyerId` 和 `findBySellerId` 添加 `@EntityGraph(attributePaths = {"item"})`

- [ ] 10. WebSocket 跨实例扩展支持
  - 在 `WebSocketConfig.java` 中添加基于环境变量的 broker 切换逻辑（开发环境用 `enableSimpleBroker`，生产环境用 `enableStompBrokerRelay`）
  - 添加 RabbitMQ STOMP 连接参数配置（host/port/username/password，通过环境变量注入）

- [ ] 11. 最终检查点 - 运行全部测试，验证所有修改
  - 运行 `mvn test` 确保后端 915 个测试全部通过
  - 运行 `npm run test:unit` 确保前端 976 个测试全部通过
  - 运行 `docker-compose build` 确保 Docker 构建成功
