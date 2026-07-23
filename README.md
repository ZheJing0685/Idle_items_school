
<div align="center">

# 闲物校园

**闲置不闲，变废为宝，绿色校园，你我共创**

[![CI/CD](https://github.com/2790849976/Idle_items_school/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/2790849976/Idle_items_school/actions/workflows/ci-cd.yml)
[![Java 17](https://img.shields.io/badge/Java-17-orange)](https://www.oracle.com/java/)
[![Spring Boot 3.5](https://img.shields.io/badge/Spring%20Boot-3.5.14-green)](https://spring.io/projects/spring-boot)
[![Vue 3.5](https://img.shields.io/badge/Vue-3.5-brightgreen)](https://vuejs.org/)
[![License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)

面向高校学生的全功能闲置物品交易平台，覆盖发布、浏览、交易、评价、即时通讯全链路，通过实名认证与管理审核构建可信赖的校园二手交易社区。

</div>

---

## 目录

- [平台功能](#平台功能)
- [系统架构](#系统架构)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [配置参考](#配置参考)
- [API 概览](#api-概览)
- [安全机制](#安全机制)
- [数据库设计](#数据库设计)
- [测试](#测试)
- [开发规范](#开发规范)
- [CI/CD](#cicd)
- [路线图](#路线图)

---

## 平台功能

### 用户端

| 模块 | 功能 |
|------|------|
| 用户系统 | 注册、登录、JWT 认证、Token 刷新、密码找回、资料编辑、头像上传 |
| 实名认证 | 学生证/身份证上传提交、身份核验、管理员审核 |
| 物品交易 | 发布闲置（标题/描述/价格/成色/分类/标签/多图）、浏览、搜索、收藏 |
| 订单管理 | 下单、支付确认、发货、收货、取消、退款，完整状态机流转 |
| 评价系统 | 交易完成互评、评分管理 |
| 即时通讯 | WebSocket (STOMP) 实时聊天、消息已读状态 |
| 通知中心 | 系统通知、订单变更推送、未读计数、批量已读、邮件通知 |
| 纠纷处理 | 订单纠纷发起、买卖双方留言、管理员裁决 |
| 分类反馈 | 用户提交分类建议或举报，辅助完善分类体系 |
| 碳减排追踪 | 记录闲置交易的环保贡献，统计减碳量数据 |

### 管理端

| 模块 | 功能 |
|------|------|
| 控制台 | 运营数据概览（用户/物品/订单/交易额）、今日新增指标 |
| 用户管理 | 用户列表、状态管理（启用/禁用）、角色分配、搜索筛选 |
| 物品管理 | 物品审核（上架/下架/驳回）、违规处理 |
| 订单管理 | 订单查询、状态流转跟踪、退款审核 |
| 分类管理 | 分类树增删改查、排序、反馈处理 |
| 认证审核 | 实名认证申请列表、资料审核（通过/驳回） |
| 统计分析 | ECharts 可视化看板、交易趋势图表、订单分布 |
| 纠纷管理 | 纠纷列表、详情查看、管理员回复 |
| 操作日志 | 管理员操作追踪、日志分析与可视化 |
| 系统监控 | 运行状态、健康检查、Prometheus 指标导出 |

---

## 系统架构

```
                          ┌──────────────┐
                          │   浏览器 / PWA  │
                          └──────┬───────┘
                                 │  HTTPS
                          ┌──────▼───────┐
   ┌──────────────────────│ Nginx (80)  │──────────────────────┐
   │  静态资源 + API 反向代理  └──────┬───────┘                      │
   │                                │                              │
┌──▼──────┐                  ┌──────▼───────┐              ┌──────▼──────┐
│ Vue 3   │                  │ Spring Boot  │              │   Redis 7   │
│ Vite 8  │                  │   API:7000   │◄─────────────│   :6379     │
│ + Pinia │                  │ JWT + WS     │  缓存/限流/    │  AOF 持久化  │
│ + PWA   │                  └──────┬───────┘  Session/黑名单 └─────────────┘
└─────────┘                        │
                                   │ JDBC
                            ┌──────▼───────┐
                            │  MySQL 8.0   │
                            │   :3306      │
                            │  + Flyway    │
                            └──────────────┘
```

### 关键设计决策

| 决策 | 说明 |
|------|------|
| 前后端分离 | 前端 Nginx 静态服务 + 反向代理 `/api` 到后端，开发时 Vite proxy |
| JWT 无状态认证 | Access Token + Refresh Token 双令牌，Redis 黑名单支持主动失效 |
| DDD 模块化 | 12 个业务模块各自包含 controller/service/repository/entity/dto |
| 读写分离服务 | 复杂模块分离 CommandService（写）和 QueryService（读） |
| 乐观锁 | Item 表 `version` 字段防并发覆盖 |
| 幂等设计 | 订单创建等关键写接口使用 `@Idempotent` 注解 + Redis 去重 |

---

## 项目结构

```
Idle_items_school/
├── .github/workflows/                   # CI/CD 工作流 (4 个)
│   ├── backend-tests.yml                 #   后端单测 + 集成测试
│   ├── test.yml                          #   前端单测 + E2E 测试
│   ├── ci-cd.yml                         #   构建 + Docker 镜像推送
│   └── qodana_code_quality.yml           #   JetBrains Qodana 代码质量
├── backend/                              # Spring Boot 后端
│   ├── src/main/java/com/idleitems/school/
│   │   ├── config/                       #   SecurityConfig, RedisConfig,
│   │   │                                 #   WebSocketConfig, CorsConfig
│   │   ├── common/                       #   公共层 (BusinessException, Result,
│   │   │                                 #   @Idempotent, @RequireRole, @Timeout, AOP)
│   │   ├── module/                       #   12 个业务模块
│   │   │   ├── admin/                    #     管理后台 (9 Controllers, 4 Services)
│   │   │   ├── auth/                     #     认证授权 (注册/登录/JWT/密码重置)
│   │   │   ├── user/                     #     用户中心 (资料/卖家主页/实名认证)
│   │   │   ├── item/                     #     物品核心 (CRUD/搜索/推荐/收藏/浏览量)
│   │   │   ├── category/                 #     分类管理 (树结构/变更日志/反馈)
│   │   │   ├── order/                    #     订单流程 (状态机/退款/超时/评价)
│   │   │   ├── chat/                     #     实时聊天 (WebSocket STOMP)
│   │   │   ├── notification/             #     消息通知 (站内/邮件)
│   │   │   ├── dispute/                  #     纠纷仲裁
│   │   │   ├── file/                     #     文件服务 (分片上传/图片处理)
│   │   │   ├── carbon/                   #     碳减排追踪
│   │   │   └── system/                   #     系统管理 (配置/字典)
│   │   ├── security/                     #   JwtUtil, RateLimitFilter, XssFilter,
│   │   │                                 #   JwtAuthenticationFilter
│   │   └── util/                         #   CookieUtil, FileValidationService,
│   │                                     #   DataEncryptionUtil, DataMaskUtil
│   ├── src/main/resources/
│   │   ├── application.yml               #   主配置 (端口, profile)
│   │   ├── application-common.yml        #   公共配置 (JPA, Flyway, 限流, WS)
│   │   ├── application-dev.yml           #   开发环境 (debug, swagger on)
│   │   ├── application-staging.yml       #   预发布环境
│   │   ├── application-prod.yml          #   生产环境 (swagger off, HikariCP 10-50)
│   │   ├── application-test.yml          #   测试环境 (H2 + MySQL)
│   │   └── db/migration/                 #   Flyway 迁移脚本 (V1.0.1 ~ V1.0.10)
│   ├── src/test/                         # 50+ 测试类 (915 个用例)
│   ├── Dockerfile.separate               # 独立 Docker 构建
│   └── pom.xml
├── frontend/                             # Vue 3 + TypeScript 前端
│   ├── src/
│   │   ├── api/                          #   16 个 API 服务 + Axios 封装 + 路径常量
│   │   ├── components/                   #   16 个组件 (Header/Footer/卡片/侧栏/上传等)
│   │   ├── composables/                  #   useDarkMode, useThemeColor
│   │   ├── config/                       #   导航配置
│   │   ├── locale/                       #   国际化 (zh-CN / en)
│   │   ├── plugins/                      #   Element Plus 全局配置
│   │   ├── router/                       #   路由 + 鉴权守卫
│   │   ├── store/                        #   Pinia 状态管理 (6 个 store)
│   │   ├── types/                        #   TypeScript 类型定义
│   │   ├── utils/                        #   工具函数 (错误处理/网络/存储/上传/WebSocket)
│   │   └── views/                        #   34 个页面 (12 公共 + 12 管理后台 + 11 用户中心)
│   ├── tests/                            #   Vitest 单测 + Playwright E2E
│   ├── Dockerfile.separate               #   Nginx 静态服务
│   └── package.json
├── scripts/
│   └── run-tests.sh                      # 统一测试入口
├── docker-compose.yml                    # Redis + Backend + Frontend 编排
├── docker-compose.override.yml.example   # 本地覆盖配置模板
└── qodana.yaml                           # Qodana 代码质量规则
```

---

## 快速开始

### 环境要求

| 依赖 | 最低版本 |
|------|---------|
| JDK | 17+ |
| Node.js | 20+ |
| MySQL | 8.0+ |
| Redis | 7+ |
| Maven | 3.8+ |

### Docker 一键部署

```bash
# 克隆项目
git clone https://github.com/2790849976/Idle_items_school.git
cd Idle_items_school

# 配置密钥
cp docker-compose.override.yml.example docker-compose.override.yml
# 编辑 docker-compose.override.yml，设置 DB_PASSWORD, JWT_SECRET 等

# 启动所有服务
docker-compose up -d
```

服务地址：

| 服务 | 地址 |
|------|------|
| 前端页面 | http://localhost:5173 |
| 后端 API | http://localhost:7000 |
| API 文档 | http://localhost:7000/doc.html |
| 健康检查 | http://localhost:7000/actuator/health |
| Prometheus | http://localhost:7000/actuator/prometheus |

### 本地开发

```bash
# 终端 1：启动后端
cd backend
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 终端 2：启动前端
cd frontend
npm install
npm run dev
```

前端默认运行在 `http://localhost:5173`，Vite 开发服务器自动代理 `/api` 请求到后端 `localhost:7000`。

### 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | `admin` | `admin123` |
| 学生 | `student` | `student123` |

---

## 配置参考

### 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `SPRING_PROFILES_ACTIVE` | 激活的配置环境 | `dev` |
| `DB_URL` | MySQL 连接地址 | `jdbc:mysql://localhost:3306/idle_items_school?...` |
| `DB_USERNAME` | 数据库用户名 | — |
| `DB_PASSWORD` | 数据库密码 | — |
| `REDIS_HOST` | Redis 主机地址 | `localhost` |
| `REDIS_PORT` | Redis 端口 | `6380` (dev) / `6379` (prod) |
| `REDIS_PASSWORD` | Redis 密码 | — |
| `JWT_SECRET` | JWT 签名密钥 | — (必填，需 Base64 编码的 256-bit 密钥) |
| `JWT_EXPIRATION` | Access Token 过期时间 (ms) | `3600000` (1h) |
| `JWT_REFRESH_EXPIRATION` | Refresh Token 过期时间 (ms) | `604800000` (7d) |
| `ENCRYPTION_SECRET_KEY` | AES 加密密钥 | — |
| `CORS_ALLOWED_ORIGINS` | 允许的跨域来源 | `http://localhost:5173,...` |
| `FILE_UPLOAD_PATH` | 文件上传目录 | `./uploads/` (dev) / `/var/data/uploads/` (prod) |

### 限流参数

| 参数 | 默认值 |
|------|--------|
| 通用接口 | 60 次 / 60 秒 |
| 登录接口 | 5 次 / 60 秒 |
| 认证接口 | 20 次 / 60 秒 |

### 登录安全

| 参数 | 默认值 |
|------|--------|
| 最大失败次数 | 5 次 |
| 锁定时间 | 15 分钟 |
| 失败计数过期 | 30 分钟 |
| 验证码长度 | 8 位 |
| 验证码过期 | 5 分钟 |
| 每小时最大发送 | 3 次 |

---

## API 概览

所有 API 前缀为 `/api`，认证接口通过请求头 `Authorization: Bearer <token>` 传递令牌。

| 模块 | 路径 | 认证 | 主要端点 |
|------|------|------|----------|
| 认证 | `/api/auth` | 公开/认证 | `POST /login` `POST /register` `POST /refresh` `POST /change-password` `POST /forgot-password` `POST /reset-password` `POST /logout` `GET /me` |
| 用户 | `/api/user` | 认证 | `GET /profile` `PUT /profile` `GET /stats` `GET /{id}/profile` `GET /{id}/items` `GET /{id}/reviews` |
| 实名认证 | `/api/verification` | 认证 | `POST /upload` `POST /submit` `GET /status` |
| 物品 | `/api/items` | 认证* | `GET /` `POST /` `GET /{id}` `PUT /{id}` `GET /search` `GET /hot` `GET /recommended` `GET /{id}/related` `POST /{id}/off-shelf` |
| 收藏 | `/api/favorites` | 认证 | `GET /` `POST /` `DELETE /{itemId}` |
| 订单 | `/api/orders` | 认证 | `GET /` `POST /` `GET /{id}` `POST /{id}/pay` `POST /{id}/ship` `POST /{id}/confirm-receive` `POST /{id}/cancel` `POST /{id}/refund` |
| 评价 | `/api/reviews` | 认证 | `POST /` `GET /item/{itemId}` `GET /user` |
| 聊天 | `/api/chats` | 认证 | WebSocket (STOMP) + REST 会话/消息 |
| 通知 | `/api/notifications` | 认证 | `GET /` `GET /unread-count` `POST /{id}/read` `POST /read-all` `DELETE /{id}` |
| 纠纷 | `/api/disputes` | 认证 | `POST /` `GET /` `GET /{id}` `POST /{id}/reply` `GET /stats` |
| 分类 | `/api/categories` | 公开/认证 | `GET /` `GET /tree` `GET /suggest` `GET /{id}/breadcrumb` `POST /feedback` |
| 碳减排 | `/api/carbon` | 认证 | `GET /stats` |
| 文件上传 | `/api/upload` | 认证 | `POST /` (通用图片) `POST /chat-media` (聊天媒体) |
| 分片上传 | `/api/items/upload/chunk` | 认证 | `POST /chunk` `GET /check` `POST /complete` |
| 字典 | `/api/dicts` | 认证 | `GET /all` `GET /{typeCode}` `GET /label` `GET /{typeCode}/options` |
| 配置 | `/api/configs` | 管理员 | `GET /` `GET /{key}` `GET /group/{group}` `POST /` |
| 首页 | `/api/home` | 公开 | `GET /stats` |
| 管理后台 | `/api/admin` | 管理员 | `GET /dashboard` `GET /users` `POST /users` `PUT /users/{id}` `DELETE /users/{id}` `GET /items` `GET /orders` `GET /categories` `GET /statistics` `GET /logs` `GET /disputes` `POST /disputes/{id}/handle` `GET /verifications` `GET /monitor` |

> \* 物品列表和搜索为公开端点，CRUD 需认证

启动后端后访问 `http://localhost:7000/doc.html` 可查看 Knife4j 生成的完整接口文档。

---

## 安全机制

### 多层防护

| 层级 | 机制 | 实现 |
|------|------|------|
| 传输层 | CSRF 禁用 + CORS 白名单 | SecurityConfig |
| 认证层 | JWT + Refresh Token 双令牌 | JwtUtil + JwtAuthenticationFilter + TokenRefreshAuthFilter |
| 鉴权层 | 方法级 `@RequireRole` + AOP | PermissionAspect |
| 输入层 | XSS 过滤 (参数/Cookie/Header) | XssFilter + OWASP Java Encoder |
| 速率层 | Redis Lua 滑动窗口限流 | RateLimitFilter |
| 应用层 | 幂等防重 `@Idempotent` | IdempotentAspect + Redis |
| 数据层 | BCrypt 密码哈希 + AES 敏感加密 | PasswordEncoder + DataEncryptionUtil |
| 展示层 | 手机号/身份证自动脱敏 | DataMaskUtil |
| 存储层 | Token 黑名单 (Redis) | JwtTokenBlacklistService |
| 文件层 | 类型白名单 + 大小限制 + 路径穿越防护 | FileValidationService |

### 角色矩阵

| 端点类别 | 访客 | 学生 | 管理员 |
|----------|:--:|:--:|:-----:|
| 商品浏览/搜索 | Y | Y | Y |
| 分类/首页 | Y | Y | Y |
| 用户注册/登录 | Y | — | — |
| 物品发布/修改 | — | Y | Y |
| 下单/支付/收货 | — | Y | Y |
| 聊天/收藏/评价 | — | Y | Y |
| 纠纷发起/回复 | — | Y | Y |
| 实名认证 | — | Y | — |
| 管理后台 | — | — | Y |
| 系统监控 | — | — | Y |

---

## 数据库设计

### 核心表关系

```
users ──────┬─── items ──────── item_images, item_tags
            ├─── favorites
            ├─── orders ──────── reviews, disputes, carbon_records
            ├─── chats ───────── chat_messages
            ├─── notifications
            ├─── verification_records
            └─── admin_logs

categories ───── category_change_logs, category_feedbacks

system_config, dict_types ──── dict_items
```

### DDL 管理

采用 Flyway 管理所有表结构变更，迁移脚本位于 `backend/src/main/resources/db/migration/`：

| 版本 | 变更内容 |
|------|---------|
| V1.0.1 | `chats` 表新增 `last_message` 列 |
| V1.0.2 | 新增 10 个复合索引优化查询性能 |
| V1.0.3 | 移除低效索引 + `items` 全文索引 |
| V1.0.4 | 标记废弃字段 |
| V1.0.5 | `items` 表新增 `version` 乐观锁 |
| V1.0.6 | `categories` 表新增 `carbon_saving_kg` |
| V1.0.7 | 新建 `carbon_records` 碳减排记录表 |
| V1.0.8 | `verification_records` 字段扩展至 512 字符 |
| V1.0.9 | 修复 NOT NULL 约束 |
| V1.0.10 | `users` 表新增 `department`/`major`/`grade` |

### 乐观锁并发控制

`items` 表使用 `version` 字段 + JPA `@Version` 注解，防止库存类数据的并发覆盖问题。

---

## 测试

### 后端测试

```bash
cd backend

# 全部测试 (915 个用例)
mvn test

# 单元测试
mvn test -Dtest="!*IntegrationTest"

# 集成测试 (需要 MySQL + Redis)
mvn test -Dtest="*IntegrationTest"

# 生成覆盖率报告
mvn test jacoco:report
```

### 前端测试

```bash
cd frontend

# 单元测试 (Vitest)
npm run test:unit

# 单元测试 + 覆盖率
npm run test:coverage

# E2E 测试 (Playwright)
npm run test:e2e

# 全部测试
npm run test:all

# 代码质量
npm run lint
npm run typecheck
```

### 覆盖率目标

| 模块 | Line | Branch |
|------|------|--------|
| 后端 Service | >= 80% | >= 60% |
| 后端 Controller | >= 90% | — |
| 前端 Store / Utils | >= 80% | — |
| 前端组件 | >= 70% | — |
| 集成测试 | 核心 API 100% | — |

---

## 开发规范

### 命名约定

| 层级 | 后端 (Java) | 前端 (TS/Vue) | 数据库 |
|------|------------|--------------|--------|
| 类/文件 | PascalCase | PascalCase | — |
| 方法/变量 | camelCase | camelCase | — |
| 表/字段 | — | — | snake_case |
| 常量 | UPPER_SNAKE | UPPER_SNAKE | — |
| 目录 | lowercase | lowercase | — |

### 提交规范

遵循 [Conventional Commits](https://www.conventionalcommits.org/)：

```
feat: 新增分片上传功能
fix: 修复订单超时未自动取消的问题
docs: 更新 API 文档
refactor: 提取订单状态机到独立服务
test: 补充评价模块单元测试
chore: 升级 Spring Boot 至 3.5.14
```

### 代码质量

| 工具 | 范围 | 规则 |
|------|------|------|
| ESLint | 前端 | `@typescript-eslint` 推荐规则 |
| Prettier | 前端 | 统一格式化 |
| Qodana | 全栈 | JetBrains 代码质量扫描 (PR + Push) |
| JaCoCo | 后端 | 覆盖率阈值检查 |

### 数据库迁移规范

- 所有 DDL 变更通过 Flyway 迁移脚本管理
- 命名规则：`V<major>.<minor>.<patch>__<description>.sql`
- 禁止直接修改数据库表结构
- 禁止 `V` 版本号重复

---

## CI/CD

| 工作流 | 触发条件 | 任务 |
|--------|---------|------|
| `backend-tests.yml` | Push/PR 到 main/develop (backend 变更) | 单元测试 (H2) + 集成测试 (MySQL+Redis) + JaCoCo 覆盖率 |
| `test.yml` | Push/PR 到 main/develop (frontend 变更) | 前端单元测试 (Vitest) + E2E 测试 (Playwright) |
| `ci-cd.yml` | 上述工作流完成后 | 构建前后端 → 推送 Docker 镜像到 GHCR (仅 main 分支) |
| `qodana_code_quality.yml` | Push (main) / PR / 手动触发 | JetBrains Qodana 代码质量扫描 + SARIF 输出 |

---

## 路线图

### 已完成 (24 项)

- [x] JWT 注册/登录/Token 刷新/密码找回
- [x] 闲置物品完整 CRUD + 多图上传 + 分片大文件上传
- [x] 分类树结构 + 用户反馈
- [x] 收藏系统
- [x] 订单全生命周期状态机（支付/发货/收货/取消/退款）
- [x] 交易评价系统
- [x] WebSocket 实时聊天 (STOMP)
- [x] 实名认证（提交 + 审核）
- [x] 通知系统（站内 + 邮件 + WebSocket 推送）
- [x] 纠纷处理流程
- [x] 管理后台（控制台/用户/物品/订单/分类/认证/日志/纠纷/统计/监控）
- [x] 图片处理（缩放/水印/压缩）
- [x] 系统配置 + 数据字典
- [x] XSS 过滤 + 接口限流 + 数据加密 + 脱敏
- [x] 权限注解 AOP + 角色分级
- [x] 定时任务（订单超时/自动确认收货/收藏计数同步）
- [x] 碳减排追踪模块
- [x] 缓存服务 (Redis)
- [x] Docker 容器化部署
- [x] CI/CD (GitHub Actions)
- [x] 暗色模式（响应式 + 持久化）
- [x] 国际化（中/英）
- [x] 前端错误边界 + 全局错误处理
- [x] Qodana 代码质量分析

### 计划中

- [ ] AI 图像识别（自动分类/估值）
- [ ] 移动端适配 / 微信小程序
- [ ] 校园合作接入
- [ ] 物品推荐算法
- [ ] 支付网关对接

---

## 许可证

[MIT License](LICENSE)
