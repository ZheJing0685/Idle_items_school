<div align="center">

# 🎓 闲置物品校园交易平台

**闲置不闲，变废为宝，绿色校园，你我共创**

[![CI/CD](https://github.com/2790849976/Idle_items_school/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/2790849976/Idle_items_school/actions/workflows/ci-cd.yml)
[![Java](https://img.shields.io/badge/Java-17-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.5-brightgreen)](https://vuejs.org/)
[![License](https://img.shields.io/badge/License-MIT-blue)](#许可证)

一个面向在校学生的安全高效闲置物品交易平台，促进校园资源循环利用，培养环保与经济意识。

</div>

---

## 📖 目录

- [项目简介](#项目简介)
- [核心功能](#核心功能)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
  - [环境要求](#环境要求)
  - [Docker 部署（推荐）](#docker-部署推荐)
  - [本地开发](#本地开发)
- [API 文档](#api-文档)
- [测试](#测试)
- [CI/CD](#cicd)
- [数据库设计](#数据库设计)
- [安全机制](#安全机制)
- [开发规范](#开发规范)
- [路线图](#路线图)
- [贡献指南](#贡献指南)
- [许可证](#许可证)

---

## 项目简介

闲置物品校园交易平台是一个全栈 Web 应用，为在校学生提供闲置物品的发布、浏览、交易、评价等一站式服务。平台通过实名认证机制保障交易安全，借助管理后台实现内容审核与数据统计，致力于打造绿色、可信赖的校园二手交易社区。

### 核心理念

| 理念 | 说明 |
|------|------|
| 🌱 **环保** | 促进闲置物品循环利用，减少资源浪费 |
| 🤝 **信任** | 学生实名认证 + 管理员审核，构建可信赖交易环境 |
| ⚡ **高效** | 卡片式浏览、分类筛选、搜索排序，快速找到心仪物品 |
| 🏫 **社区** | 校园专属身份，同校交易更安心 |

---

## 核心功能

### 用户端

| 模块 | 功能 |
|------|------|
| **用户系统** | 注册 / 登录 / 个人信息管理 / 头像上传 |
| **实名认证** | 学生证上传 / 身份核验 / 认证状态管理 |
| **物品交易** | 发布闲置 / 图片上传 / 分类浏览 / 详情查看 / 收藏 |
| **订单管理** | 下单 / 支付 / 发货 / 收货 / 取消 / 退款 |
| **评价系统** | 交易评价 / 评分 / 图片评价 |
| **即时通讯** | 买卖双方实时聊天（WebSocket） |

### 管理端

| 模块 | 功能 |
|------|------|
| **用户管理** | 用户列表 / 状态管理 / 角色分配 |
| **物品审核** | 发布审核 / 上下架管理 / 违规处理 |
| **分类管理** | 分类增删改查 / 排序 |
| **订单管理** | 订单列表 / 状态流转 / 退款审核 |
| **认证审核** | 实名认证申请审核 |
| **统计分析** | 交易数据看板 / 趋势图表 / 数据导出 |
| **操作日志** | 管理员操作记录 / 日志分析 |

---

## 技术栈

### 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| [Vue.js](https://vuejs.org/) | 3.5+ | 渐进式 JavaScript 框架 |
| [Vite](https://vitejs.dev/) | 8.0+ | 下一代前端构建工具 |
| [Pinia](https://pinia.vuejs.org/) | 3.0+ | 状态管理 |
| [Element Plus](https://element-plus.org/) | 2.13+ | UI 组件库 |
| [Vue Router](https://router.vuejs.org/) | 4.6+ | 路由管理 |
| [Axios](https://axios-http.com/) | 1.15+ | HTTP 请求 |
| [ECharts](https://echarts.apache.org/) | 6.0+ | 数据可视化 |
| [Playwright](https://playwright.dev/) | 1.59+ | E2E 测试 |
| [Vitest](https://vitest.dev/) | 2.0+ | 单元测试 |

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| [Spring Boot](https://spring.io/projects/spring-boot) | 3.2.4 | 应用框架 |
| [Java](https://www.oracle.com/java/) | 17+ | 编程语言 |
| [Spring Security](https://spring.io/projects/spring-security) | - | 安全框架 |
| [Spring Data JPA](https://spring.io/projects/spring-data-jpa) | - | 数据持久化 |
| [MySQL](https://www.mysql.com/) | 8.0+ | 关系型数据库 |
| [Redis](https://redis.io/) | - | 缓存 |
| [Flyway](https://flywaydb.org/) | 9.22+ | 数据库迁移 |
| [JWT (jjwt)](https://github.com/jwtk/jjwt) | 0.12+ | 身份认证 |
| [Knife4j](https://doc.xiaominfo.com/) | 4.4+ | API 文档 |
| [WebSocket](https://spring.io/guides/gs/messaging-stomp/) | - | 实时通讯 |
| [Micrometer + Prometheus](https://micrometer.io/) | - | 监控指标 |

### 基础设施

| 工具 | 用途 |
|------|------|
| [Docker](https://www.docker.com/) + Docker Compose | 容器化部署 |
| [GitHub Actions](https://github.com/features/actions) | CI/CD 流水线 |
| [Nginx](https://nginx.org/) | 前端静态资源服务 & 反向代理 |
| [Qodana](https://www.jetbrains.com/qodana/) | 代码质量分析 |

---

## 项目结构

```
Idle_items_school/
├── .github/workflows/          # CI/CD 配置
│   ├── ci-cd.yml               # 主流水线
│   ├── backend-tests.yml       # 后端测试
│   └── qodana_code_quality.yml # 代码质量检查
│
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/com/idleitems/school/
│   │   ├── annotation/         # 自定义注解（权限控制）
│   │   ├── aspect/             # AOP 切面（权限校验）
│   │   ├── cache/              # 缓存服务
│   │   ├── common/             # 通用类（Result、ErrorCode）
│   │   ├── config/             # 配置类（安全、Redis、WebSocket 等）
│   │   ├── controller/         # 控制器层
│   │   │   └── admin/          # 管理后台接口
│   │   ├── dto/                # 数据传输对象
│   │   ├── entity/             # 实体类
│   │   ├── filter/             # 过滤器（XSS、限流）
│   │   ├── repository/         # 数据访问层
│   │   ├── security/           # JWT 工具
│   │   ├── service/            # 业务逻辑层
│   │   ├── task/               # 定时任务
│   │   └── util/               # 工具类（文件、图片处理）
│   ├── src/main/resources/
│   │   ├── db/migration/       # Flyway 数据库迁移脚本
│   │   └── application*.yml    # 多环境配置
│   ├── src/test/               # 单元测试 & 集成测试
│   ├── Dockerfile
│   └── pom.xml
│
├── frontend/                   # Vue 3 前端
│   ├── src/
│   │   ├── api/                # API 请求封装
│   │   ├── components/         # 公共组件
│   │   ├── composables/        # 组合式函数
│   │   ├── router/             # 路由配置
│   │   ├── store/              # Pinia 状态管理
│   │   ├── styles/             # 样式文件
│   │   ├── utils/              # 工具函数
│   │   └── views/              # 页面视图
│   │       ├── admin/          # 管理后台页面
│   │       └── user/           # 用户中心页面
│   ├── tests/                  # 测试
│   │   ├── unit/               # 单元测试
│   │   └── e2e/                # E2E 测试
│   ├── Dockerfile
│   └── package.json
│
├── docker-compose.yml          # Docker Compose 编排
├── qodana.yaml                 # 代码质量配置
└── sql/                        # 数据库初始化脚本
```

---

## 快速开始

### 环境要求

| 工具 | 版本 |
|------|------|
| JDK | 17+ |
| Node.js | 20+ |
| MySQL | 8.0+ |
| Redis | 6.0+ |
| Maven | 3.8+ |
| Docker & Docker Compose | 最新稳定版（可选） |

### Docker 部署（推荐）

1. **克隆项目**

```bash
git clone https://github.com/2790849976/Idle_items_school.git
cd Idle_items_school
```

2. **配置环境变量**

```bash
cp docker-compose.override.yml.example docker-compose.override.yml
```

编辑 `docker-compose.override.yml`，设置以下变量：

```yaml
services:
  mysql:
    environment:
      MYSQL_ROOT_PASSWORD: your_root_password
  backend:
    environment:
      SPRING_DATASOURCE_USERNAME: your_db_username
      SPRING_DATASOURCE_PASSWORD: your_db_password
      JWT_SECRET: your_jwt_secret_key_at_least_32_chars
```

3. **启动服务**

```bash
docker-compose up -d
```

4. **访问应用**

| 服务 | 地址 |
|------|------|
| 前端页面 | http://localhost |
| 后端 API | http://localhost:8080 |
| API 文档 | http://localhost:8080/swagger-ui.html |
| MySQL | localhost:3306 |

### 本地开发

#### 后端

```bash
cd backend

# 配置数据库连接（编辑 src/main/resources/application-dev.yml）
# 设置环境变量或直接修改配置文件：
#   DB_USERNAME, DB_PASSWORD, DB_URL, JWT_SECRET, REDIS_HOST, REDIS_PORT

# 安装依赖并启动
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

后端默认运行在 `http://localhost:8080`。

#### 前端

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端默认运行在 `http://localhost:5173`，API 请求通过 Vite 代理转发到后端。

---

## API 文档

项目集成了 [Knife4j](https://doc.xiaominfo.com/)（基于 OpenAPI 3），启动后端后访问：

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Knife4j 文档**: http://localhost:8080/doc.html

### 主要 API 模块

| 模块 | 路径前缀 | 说明 |
|------|----------|------|
| 认证 | `/api/auth` | 注册、登录、Token 刷新 |
| 用户 | `/api/user` | 用户信息管理 |
| 物品 | `/api/item` | 闲置物品 CRUD |
| 分类 | `/api/category` | 商品分类 |
| 订单 | `/api/order` | 订单管理 |
| 评价 | `/api/review` | 交易评价 |
| 收藏 | `/api/favorite` | 收藏管理 |
| 聊天 | `/api/chat` | 即时通讯 |
| 认证 | `/api/verification` | 实名认证 |
| 文件上传 | `/api/upload` | 通用图片上传（头像、分类图标） |
| 分片上传 | `/api/items/upload/chunk` | 大文件分片上传 |
| 管理 | `/api/admin/*` | 管理后台接口 |

### 文件上传 API 详情

| 端点 | 方法 | 说明 | 认证 |
|------|------|------|------|
| `/api/upload` | POST | 通用图片上传（头像、分类图标） | 需要 |
| `/api/items/upload` | POST | 物品图片上传 | 需要 |
| `/api/verification/upload` | POST | 认证材料上传（身份证、学生证） | 需要 |
| `/api/items/upload/chunk` | POST | 上传单个分片 | 需要 |
| `/api/items/upload/check` | GET | 检查已上传分片状态 | 需要 |
| `/api/items/upload/complete` | POST | 合并分片完成上传 | 需要 |

**文件限制:**
- 支持格式: JPG, JPEG, PNG, WebP
- 最大大小: 5MB
- 自动处理: 缩放（最大宽度1920px）+ 水印 + 质量压缩（80%）

### 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": "2024-01-01T00:00:00Z"
}
```

---

## 测试

### 前端测试

```bash
cd frontend

# 单元测试
npm run test:unit

# 单元测试 + 覆盖率
npm run test:coverage

# E2E 测试
npm run test:e2e

# E2E 测试（带浏览器界面）
npm run test:e2e:headed

# 全部测试
npm run test:all
```

### 后端测试

```bash
cd backend

# 运行全部测试
mvn test

# 打包（跳过测试）
mvn package -DskipTests
```

### 代码质量检查

项目集成了 JetBrains Qodana 进行静态代码分析：

```bash
# 本地运行 Qodana（需要 Docker）
docker run --rm -v $(pwd):/project -p 8080:8080 jetbrains/qodana-jvm
```

访问 http://localhost:8080 查看代码质量报告。

---

## CI/CD

项目使用 GitHub Actions 实现自动化流水线，配置位于 `.github/workflows/ci-cd.yml`。

```
┌─────────────┐    ┌──────────────┐
│ Frontend    │    │ Backend      │
│ Tests       │    │ Tests        │
│ (Vitest +   │    │ (Maven +     │
│  Playwright)│    │  JUnit)      │
└──────┬──────┘    └──────┬───────┘
       │                  │
       └────────┬─────────┘
                │
         ┌──────▼──────┐
         │   Build     │
         │ (Frontend + │
         │  Backend)   │
         └──────┬──────┘
                │
         ┌──────▼──────┐
         │   Deploy    │
         │  (main 分支) │
         └─────────────┘
```

- **触发条件**: `main` / `develop` 分支的 push 和 PR
- **前端测试**: Node.js 20 + Vitest + Playwright
- **后端测试**: JDK 17 + Maven
- **代码质量**: Qodana 静态分析
- **构建产物**: 前端 `dist/` + 后端 `school-1.0.0.jar`，保留 7 天

---

## 数据库设计

项目使用 [Flyway](https://flywaydb.org/) 管理数据库迁移，迁移脚本位于 `backend/src/main/resources/db/migration/`。

### 核心数据表

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `users` | 用户表 | username, email, phone, role, verified |
| `items` | 商品表 | title, price, condition, status, location |
| `categories` | 分类表 | name, parent_id, sort_order |
| `orders` | 订单表 | order_no, buyer_id, seller_id, order_status |
| `reviews` | 评价表 | rating, content, is_anonymous |
| `favorites` | 收藏表 | user_id, item_id |
| `chats` | 聊天会话表 | buyer_id, seller_id |
| `chat_messages` | 聊天消息表 | content, message_type, is_read |
| `verification_records` | 实名认证表 | real_name, student_id, status |
| `item_images` | 商品图片表 | image_url, thumbnail_url, is_cover |
| `item_tags` | 商品标签表 | tag_name |
| `disputes` | 纠纷表 | reason, dispute_status |
| `admin_logs` | 管理员日志表 | operation, target_type, details |
| `image_analysis` | 图片分析表 | analysis_result, confidence |

### ER 关系概览

```
users ──1:N──> items ──1:N──> item_images
  │               │
  │               ├──1:N──> item_tags
  │               │
  │               ├──1:N──> orders ──1:N──> reviews
  │               │              │
  │               │              └──1:1──> disputes
  │               │
  │               └──1:N──> favorites
  │
  ├──1:N──> verification_records
  ├──1:N──> chats ──1:N──> chat_messages
  └──1:N──> admin_logs
```

---

## 安全机制

| 机制 | 说明 |
|------|------|
| **JWT 认证** | 基于 JWT 的无状态身份认证，支持 Token 刷新 |
| **Spring Security** | 请求级安全控制，角色权限分级 |
| **BCrypt 加密** | 密码使用 BCrypt 算法加密存储 |
| **XSS 防护** | 全局 XSS 过滤器，防止跨站脚本攻击 |
| **接口限流** | RateLimitFilter 防止恶意请求 |
| **权限注解** | `@RequireRole` 自定义注解 + AOP 切面实现方法级权限控制 |
| **文件校验** | 上传文件类型（jpg/png/webp）和大小（5MB）限制 |
| **敏感数据脱敏** | 手机号、身份证等敏感字段脱敏处理 |

### 角色权限

| 角色 | 权限范围 |
|------|----------|
| 访客 | 浏览商品、搜索（受限） |
| 学生 | 完整交易功能（发布、购买、评价等） |
| 管理员 | 内容审核、用户管理、数据统计、系统配置 |

---

## 开发规范

### 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 前端组件/类名 | PascalCase | `UserProfile` |
| 前端常量 | snake_case | `MAX_FILE_SIZE` |
| 后端类名 | PascalCase | `UserController` |
| 后端方法名 | camelCase | `getUserById` |
| 数据库字段 | snake_case | `created_at` |
| 数据库表名 | 复数 snake_case | `users`, `idle_items` |

### 代码质量

- **代码审查**: 每次修改后进行逻辑完整性、边界条件、安全风险、性能影响审查
- **测试覆盖**: 单元测试覆盖率 ≥ 80%，核心流程集成测试覆盖
- **代码风格**: ESLint + Prettier（前端），Checkstyle（后端）
- **静态分析**: Qodana 代码质量检查
- **数据库迁移**: 所有表结构变更通过 Flyway 迁移脚本管理

### Git 提交规范

遵循 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

```
feat: 新功能
fix: 修复 Bug
docs: 文档更新
style: 代码格式调整
refactor: 代码重构
test: 测试相关
chore: 构建/工具变更
```

---

## 路线图

### ✅ 已完成

- [x] 用户注册 / 登录 / JWT 认证
- [x] 闲置物品发布 / 浏览 / 搜索
- [x] 分类管理
- [x] 订单全流程管理
- [x] 实名认证系统
- [x] 管理后台（用户、物品、订单、分类管理）
- [x] 图片上传与处理
- [x] Docker 容器化部署
- [x] CI/CD 自动化流水线
- [x] WebSocket 即时通讯
- [x] 用户中心界面重构（卡片式设计）
- [x] Qodana 代码质量集成

### 🚧 进行中

- [ ] 评价系统完善
- [ ] 纠纷处理流程
- [ ] 数据统计与分析增强

### 📋 计划中

- [ ] AI 图像识别（自动分类、估价）
- [ ] 移动端适配 / 小程序
- [ ] 校园合作接入
- [ ] 消息推送通知
- [ ] 物品推荐算法

---

## 贡献指南

欢迎贡献代码、报告问题或提出建议！

1. Fork 本仓库
2. 创建功能分支：`git checkout -b feature/your-feature`
3. 提交更改：`git commit -m 'feat: add some feature'`
4. 推送分支：`git push origin feature/your-feature`
5. 提交 Pull Request

---

## 许可证

本项目基于 [MIT License](LICENSE) 开源。

---

<div align="center">

**闲置不闲，变废为宝，绿色校园，你我共创** 🌱

</div>