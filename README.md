<div align="center">

# 闲置物品校园交易平台

**闲置不闲，变废为宝，绿色校园，你我共创**

[![CI/CD](https://github.com/2790849976/Idle_items_school/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/2790849976/Idle_items_school/actions/workflows/ci-cd.yml)
![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-green)
![Vue.js](https://img.shields.io/badge/Vue.js-3.5-brightgreen)
![License](https://img.shields.io/badge/License-MIT-blue)

面向在校学生的全功能闲置物品交易平台，覆盖发布、浏览、交易、评价、即时通讯全链路，通过实名认证与管理员审核机制构建可信赖的校园二手交易社区。

</div>

---

## 核心功能

### 用户端

| 模块 | 功能 |
|------|------|
| **用户系统** | 注册、登录、JWT 无状态认证、Token 刷新、密码找回、个人信息编辑、头像上传 |
| **实名认证** | 学生证上传提交、身份核验、管理员审核、认证状态追踪 |
| **物品交易** | 发布闲置（标题/描述/价格/成色/分类/标签/多图）、分类浏览、关键词搜索、热门推荐、详情查看、收藏 |
| **订单管理** | 下单、支付确认、发货、收货确认、取消订单、退款申请，全状态机流转 |
| **评价系统** | 交易完成后互评、评分、评价内容管理 |
| **即时通讯** | 基于 WebSocket (STOMP) 的买卖双方实时聊天、消息已读状态 |
| **通知中心** | 系统通知、订单状态变更通知、未读计数、批量已读 |
| **纠纷处理** | 订单纠纷发起、买卖双方留言、管理员介入裁决 |
| **分类反馈** | 用户可提交分类建议或举报，辅助运营完善分类体系 |

### 管理端

| 模块 | 功能 |
|------|------|
| **控制台** | 平台运营数据概览（用户/物品/订单/交易额）、今日新增指标、快捷操作入口 |
| **用户管理** | 用户列表、状态管理（启用/禁用）、角色分配、搜索筛选 |
| **物品管理** | 物品审核（上架/下架/驳回）、分类筛选、违规处理 |
| **订单管理** | 订单查询、状态流转跟踪、退款审核 |
| **分类管理** | 分类增删改查、层级排序、反馈管理 |
| **认证审核** | 实名认证申请列表、资料审核（通过/驳回） |
| **统计分析** | 交易数据看板、趋势图表（ECharts）、订单分布、数据概览 |
| **纠纷管理** | 纠纷列表、详情查看、管理员回复 |
| **操作日志** | 管理员操作追踪、日志分析 |
| **系统监控** | 系统运行状态、健康检查 |

---

## 技术栈

### 前端

| 技术 | 用途 |
|------|------|
| [Vue.js 3.5](https://vuejs.org/) + TypeScript | 渐进式框架 |
| [Vite 8](https://vitejs.dev/) | 构建工具 |
| [Pinia 3](https://pinia.vuejs.org/) | 状态管理 |
| [Element Plus 2.13](https://element-plus.org/) | UI 组件库 |
| [Vue Router 4](https://router.vuejs.org/) | 路由管理 |
| [Axios](https://axios-http.com/) | HTTP 请求封装 |
| [ECharts 6](https://echarts.apache.org/) | 数据可视化 |
| [Vue I18n 10](https://vue-i18n.intlify.dev/) | 国际化（中/英） |
| [Lucide Vue Next](https://lucide.dev/) | 图标库 |
| [Day.js](https://day.js.org/) | 日期处理 |
| [Vue ECharts](https://ecomfe.github.io/vue-echarts/) | ECharts Vue 集成 |
| [Vitest](https://vitest.dev/) + [Vue Test Utils](https://test-utils.vuejs.org/) | 单元测试 |
| [Playwright](https://playwright.dev/) | E2E 测试 |
| [Vite PWA](https://vite-pwa-org.netlify.app/) | PWA 支持 |

### 后端

| 技术 | 用途 |
|------|------|
| [Spring Boot 3.5](https://spring.io/projects/spring-boot) | 应用框架 |
| [Java 17](https://www.oracle.com/java/) | 编程语言 |
| [Spring Security](https://spring.io/projects/spring-security) | 安全框架 |
| [Spring Data JPA](https://spring.io/projects/spring-data-jpa) | 数据持久化 |
| [MySQL 8.0](https://www.mysql.com/) | 关系型数据库 |
| [Redis 7](https://redis.io/) | 缓存、限流、Token 黑名单 |
| [Flyway](https://flywaydb.org/) | 数据库迁移管理 |
| [JWT (jjwt 0.12)](https://github.com/jwtk/jjwt) | 无状态身份认证 |
| [WebSocket (STOMP)](https://spring.io/guides/gs/messaging-stomp/) | 实时双向通讯 |
| [Knife4j 4.4](https://doc.xiaominfo.com/) | OpenAPI 3 接口文档 |
| [Thumbnailator 0.4](https://github.com/coobird/thumbnailator) | 图片缩放处理 |
| [Micrometer](https://micrometer.io/) | 监控度量 |
| [Lombok](https://projectlombok.org/) | 代码简化 |
| [OWASP Encoder](https://owasp.org/www-project-java-encoder/) | XSS 防护 |
| JUnit 5 + Mockito + MockMvc | 测试框架 |
| JaCoCo | 测试覆盖率 |

### 基础设施

| 工具 | 用途 |
|------|------|
| Docker + Docker Compose | 容器化部署（Redis + Backend + Frontend(Nginx)） |
| GitHub Actions | CI/CD 流水线（测试 + 构建） |
| GitLab CI | 镜像 CI/CD 流水线（测试 + 构建 + Docker 推送） |
| Nginx | 前端静态资源服务 & API 反向代理 |
| Qodana | JetBrains 代码质量分析 |

---

## 项目结构

```
Idle_items_school/
├── .github/workflows/           # GitHub Actions CI/CD
│   └── ci-cd.yml
├── backend/                     # Spring Boot 后端 (Java 17)
│   ├── src/main/java/com/idleitems/school/
│   │   ├── config/              # 配置类（Security/Redis/WebSocket/Swagger/Async等）
│   │   ├── module/              # 业务模块
│   │   │   ├── auth/            # 认证模块（登录/注册/JWT）
│   │   │   ├── user/            # 用户模块（信息/实名认证）
│   │   │   ├── item/            # 物品模块（CRUD/收藏/浏览/搜索）
│   │   │   ├── category/        # 分类模块（树结构/反馈）
│   │   │   ├── order/           # 订单模块（状态机/退款/超时）
│   │   │   ├── chat/            # 聊天模块（WebSocket）
│   │   │   ├── notification/    # 通知模块（系统通知/邮件）
│   │   │   ├── dispute/         # 纠纷模块
│   │   │   ├── file/            # 文件上传（分片上传/图片处理）
│   │   │   ├── admin/           # 管理后台模块
│   │   │   └── system/          # 系统管理（配置/字典）
│   │   ├── security/            # 安全过滤（JWT/XSS/限流）
│   │   ├── shared/              # 共享服务（缓存/定时任务）
│   │   ├── util/                # 工具（文件校验/加密/脱敏/存储适配）
│   │   └── IdleItemsSchoolApplication.java
│   ├── src/main/resources/
│   │   ├── db/migration/        # Flyway 数据库迁移脚本
│   │   └── application-*.yml    # 多环境配置（dev/prod/staging/local）
│   ├── src/test/                # 50+ 个测试类
│   ├── Dockerfile / Dockerfile.separate
│   └── pom.xml
├── frontend/                    # Vue 3 + TypeScript 前端
│   ├── src/
│   │   ├── api/                 # Axios 封装 + API 服务
│   │   ├── components/          # 公共组件（Header/Footer/卡片/侧栏等）
│   │   ├── composables/         # 组合式函数（暗色模式/主题色）
│   │   ├── config/              # 导航配置
│   │   ├── locale/              # i18n 国际化（zh-CN / en）
│   │   ├── plugins/             # Element Plus 插件配置
│   │   ├── router/              # 路由配置（含鉴权守卫）
│   │   ├── store/               # Pinia 状态管理
│   │   ├── styles/              # 全局样式 + 暗色模式主题
│   │   ├── types/               # TypeScript 类型定义
│   │   ├── utils/               # 工具（业务流/错误处理/网络/存储/上传/验证/WebSocket）
│   │   └── views/               # 页面（首页/物品/详情/发布/用户中心/管理后台）
│   ├── tests/                   # 单元测试 + E2E 测试
│   ├── Dockerfile.separate
│   └── package.json
├── docker-compose.yml           # Redis + Backend + Frontend(Nginx) 编排
├── sql/                         # 数据库初始化脚本
└── docs/                        # 文档
```

---

## 安全机制

| 机制 | 说明 |
|------|------|
| **JWT 无状态认证** | 基于 jjwt 0.12，支持 Token 刷新与黑名单失效 |
| **Spring Security** | 角色权限分级（STUDENT / ADMIN），方法级 `@RequireRole` 注解 + AOP 切面 |
| **BCrypt 加密** | 密码安全哈希存储 |
| **XSS 防护** | 全局 XSS 过滤器（基于 OWASP Encoder），请求参数/Cookie/Header 清洗 |
| **接口限流** | Redis Lua 脚本实现滑动窗口限流（默认 60次/分钟，登录 5次/分钟） |
| **文件校验** | 类型白名单（jpg/png/webp）、大小限制（5MB）、内容安全校验 |
| **敏感数据加密** | AES 加密敏感字段（DataEncryptionUtil） |
| **数据脱敏** | 手机号、身份证等字段展示时自动脱敏（DataMaskUtil） |

### 角色权限

| 角色 | 权限范围 |
|------|----------|
| 访客 | 浏览商品、搜索、查看详情 |
| 学生 | 完整交易功能（发布/购买/评价/聊天/收藏/纠纷） |
| 管理员 | 所有用户端功能 + 管理后台（审核/管理/统计/日志/监控） |

---

## API 概览

| 模块 | 路径前缀 | 说明 |
|------|----------|------|
| 认证 | `/api/auth` | 注册、登录、Token 刷新、密码找回/重置、修改密码 |
| 用户 | `/api/user` | 个人信息、资料编辑、统计 |
| 物品 | `/api/items` | CRUD、搜索、热门推荐、用户物品列表、上下架 |
| 分类 | `/api/categories` | 分类列表、树结构 |
| 订单 | `/api/orders` | 创建、列表、详情、支付、发货、收货、取消、退款 |
| 评价 | `/api/reviews` | 评价创建、物品评价、用户评价 |
| 收藏 | `/api/favorites` | 收藏/取消、列表 |
| 聊天 | `/api/chats` | 会话列表、消息发送/历史 |
| 通知 | `/api/notifications` | 列表、未读计数、已读、删除 |
| 纠纷 | `/api/disputes` | 创建、列表、详情、回复 |
| 实名认证 | `/api/verification` | 资料提交、记录查询、审核材料上传 |
| 文件上传 | `/api/upload` | 通用图片上传 |
| 分片上传 | `/api/items/upload/chunk` | 大文件分片上传/校验/合并 |
| 系统配置 | `/api/configs` | 动态配置管理 |
| 数据字典 | `/api/dicts` | 字典类型/项管理 |
| 管理后台 | `/api/admin` | 控制台、用户/物品/订单/分类/认证/日志/纠纷/监控/统计 |

API 文档启动后访问：`/doc.html`（Knife4j）或 `/swagger-ui/index.html`

---

## 快速开始

### 环境要求

- JDK 17+ / Node.js 20+ / MySQL 8.0+ / Redis 6+ / Maven 3.8+

### Docker 部署

```bash
git clone https://github.com/2790849976/Idle_items_school.git
cd Idle_items_school

cp docker-compose.override.yml.example docker-compose.override.yml
# 编辑 docker-compose.override.yml 设置数据库密码和 JWT 密钥

docker-compose up -d
```

| 服务 | 地址 |
|------|------|
| 前端 | http://localhost:5173 |
| 后端 API | http://localhost:7000 |
| API 文档 | http://localhost:7000/doc.html |

### 本地开发

**后端：**
```bash
cd backend
# 配置 application-dev.yml 中的数据库/Redis/JWT 信息
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**前端：**
```bash
cd frontend
npm install
npm run dev
```

---

## 测试

### 后端（50+ 测试类）

```bash
cd backend
mvn test                                    # 全部测试
mvn test -Dtest="*ServiceTest"              # Service 单元测试
mvn test -Dtest="*ControllerTest"           # Controller 单元测试
mvn test -Dtest="*IntegrationTest"          # 集成测试
mvn test jacoco:report                      # 覆盖率报告
```

### 前端

```bash
cd frontend
npm run test:unit                           # 单元测试
npm run test:coverage                       # 单元测试 + 覆盖率
npm run test:e2e                            # Playwright E2E 测试
npm run test:all                            # 全部测试
npm run lint                                # ESLint 检查
npm run typecheck                           # TypeScript 类型检查
```

### 覆盖率目标

| 模块 | 目标 |
|------|------|
| 前端 Store / 工具函数 | ≥ 80% / ≥ 90% |
| 前端组件 | ≥ 70% |
| 后端 Service | ≥ 80% |
| 后端 Controller | ≥ 90% |
| 集成测试 | 100% 核心 API |

---

## 数据库

采用 Flyway 管理增量迁移，核心表：

`users` → `items` → `item_images` / `item_tags`
`users` → `orders` → `reviews` / `disputes`
`users` → `chats` → `chat_messages`
`users` → `verification_records`
`categories` → `category_change_logs` / `category_feedback`

辅助表：`admin_logs`、`notifications`、`favorites`、`system_config`、`dict_type`、`dict_item`

---

## 路线图

### 已完成

- [x] JWT 注册/登录/Token 刷新/密码找回
- [x] 闲置物品完整 CRUD + 多图上传（含分片大文件上传）
- [x] 分类树结构 + 用户反馈
- [x] 收藏系统
- [x] 订单全生命周期状态机（支付→发货→收货→取消→退款）
- [x] 交易评价系统
- [x] WebSocket 实时聊天（STOMP）
- [x] 实名认证（用户提交 + 管理员审核）
- [x] 通知系统（站内/邮件/WebSocket 推送）
- [x] 纠纷处理流程
- [x] 管理后台（控制台/用户/物品/订单/分类/认证/日志/纠纷/统计/监控）
- [x] 图片处理（缩放/水印/压缩）
- [x] 系统配置 + 数据字典
- [x] 安全防护（XSS 过滤/接口限流/数据加密/脱敏）
- [x] 权限注解 AOP + 角色分级
- [x] 定时任务（订单超时/自动确认收货/收藏计数同步）
- [x] 缓存服务（Redis）
- [x] Docker 容器化部署
- [x] CI/CD（GitHub Actions + GitLab CI）
- [x] 暗色模式（响应式 + 持久化）
- [x] 国际化（中文/英文）
- [x] 前端错误边界 + 全局错误处理
- [x] Qodana 代码质量分析
- [x] Cloudflare Tunnel 外网访问优化

### 计划中

- [ ] AI 图像识别（自动分类/估值）
- [ ] 移动端适配 / 微信小程序
- [ ] 校园合作接入
- [ ] 物品推荐算法
- [ ] 支付网关对接

---

## 开发规范

- **命名**: 前端 PascalCase / 后端 camelCase / 数据库 snake_case
- **提交**: 遵循 [Conventional Commits](https://www.conventionalcommits.org/)（feat/fix/docs/refactor/test/chore）
- **质量**: ESLint + Prettier（前端）、Qodana（全项目）
- **迁移**: 所有表结构变更通过 Flyway 迁移脚本管理

---

## 许可证

[MIT License](LICENSE)
