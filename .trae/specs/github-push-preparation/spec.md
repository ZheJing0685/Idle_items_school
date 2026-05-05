# GitHub 推送准备规范 Spec

## Why

当前仓库已推送到 GitHub（`origin/main`），但存在严重的安全隐患和仓库卫生问题：敏感配置文件（含默认密码、JWT 密钥）已被提交、用户上传文件和测试报告等二进制大文件占用仓库空间、`.gitignore` 规则不完整。需要在下次推送前彻底清理。

## What Changes

- 更新根目录 `.gitignore`，补全缺失的忽略规则
- 从 Git 跟踪中移除不应被追踪的文件（`git rm --cached`）
- 清理 `docker-compose.yml` 中的硬编码密码
- 清理 `application-dev.yml` / `application-staging.yml` 中的默认 JWT 密钥
- 制定标准化的 Git 提交/推送流程文档
- 补充分支规范、提交信息规范、隐私保护最佳实践

## Impact

- Affected specs: 无（新建 spec）
- Affected code:
  - `.gitignore`（根目录）
  - `docker-compose.yml`
  - `backend/src/main/resources/application-dev.yml`
  - `backend/src/main/resources/application-staging.yml`

---

## 文件分类分析

### 绝对不能推送的文件（敏感信息 / 大文件 / 本地缓存）

| 文件/目录 | 原因 | 严重程度 |
|-----------|------|----------|
| `backend/uploads/` | 用户上传的图片和文件，属于运行时数据，不应入库 | 🔴 高 |
| `uploads/` | 同上，根目录下的用户上传文件 | 🔴 高 |
| `frontend/.env.development` | 包含本地开发环境地址 `localhost:7000`，属于环境特定配置 | 🔴 高 |
| `frontend/.env.production` | 包含生产环境地址，泄露后可被攻击者利用 | 🔴 高 |
| `frontend/.env.staging` | 包含预发布环境地址 | 🔴 高 |
| `frontend/tests/reports/` | 测试覆盖率报告和 Playwright 录屏（.webm/.png），数百个二进制文件，严重膨胀仓库 | 🔴 高 |
| `outputs/` | 运行时会话数据，属于临时产物 | 🟡 中 |
| `docker-compose.yml` 中的硬编码密码 | `MYSQL_ROOT_PASSWORD: root`、`SPRING_DATASOURCE_PASSWORD: root` 泄露数据库凭据 | 🔴 高 |
| `application-dev.yml` 中的默认 JWT 密钥 | `jwt.secret` 含默认值 `idle-items-school-dev-secret-key-2024-for-jwt-token` | 🔴 高 |
| `application-staging.yml` 中的默认 JWT 密钥 | `jwt.secret` 含默认值 `idle-items-school-staging-secret-key-2024-for-jwt-token` | 🔴 高 |
| `sql/idle_items_school.sql` | 数据库完整导出，可能含用户数据、密码哈希等敏感信息 | 🟡 中 |
| `分析.txt`、`已修复清单.txt` | 临时笔记文件，不属于项目源码 | 🟡 中 |
| `doc/` | 内部规划文档，与 `docs/` 重复且为临时性质 | 🟢 低 |

### 适合推送的文件

| 文件/目录 | 说明 |
|-----------|------|
| `backend/src/` | 后端 Java 源码（含测试源码） |
| `frontend/src/` | 前端 Vue 源码 |
| `frontend/tests/unit/`、`frontend/tests/e2e/` | 测试源码（不含报告） |
| `frontend/.env.example` | 环境变量模板，供其他开发者参考 |
| `backend/src/main/resources/application.yml` | 基础配置（无敏感信息） |
| `backend/src/main/resources/application-prod.yml` | 生产配置（已使用环境变量，无硬编码密钥） |
| `backend/src/main/resources/application-test.yml` | 测试配置（使用 H2 内存库，可接受） |
| `backend/src/main/resources/db/migration/` | Flyway 数据库迁移脚本 |
| `backend/src/main/resources/logback.xml` | 日志配置 |
| `pom.xml`、`package.json`、`package-lock.json` | 依赖管理文件 |
| `Dockerfile`（前后端） | 容器构建文件 |
| `docker-compose.yml` | 容器编排（需清理密码后） |
| `.github/workflows/` | CI/CD 配置 |
| `nginx.conf` | Nginx 配置 |
| `eslint.config.js`、`.prettierrc.js`、`vite.config.js` | 代码规范和构建配置 |
| `playwright.config.js` | E2E 测试配置 |
| `qodana.yaml` | 代码质量分析配置 |
| `scripts/` | 数据库脚本和备份脚本 |
| `docs/` | 项目文档 |
| `README.md`（根目录和子目录） | 项目说明 |
| `.gitignore` | Git 忽略规则 |

---

## ADDED Requirements

### Requirement: 完善 .gitignore 规则

系统 SHALL 在根目录 `.gitignore` 中补充以下缺失的忽略规则：

#### Scenario: 用户上传目录被忽略
- **WHEN** 执行 `git status`
- **THEN** `backend/uploads/` 和 `uploads/` 不应出现在未跟踪文件列表中

#### Scenario: 测试报告被忽略
- **WHEN** 执行 `git status`
- **THEN** `frontend/tests/reports/` 不应出现在未跟踪文件列表中

#### Scenario: 环境配置文件被忽略
- **WHEN** 执行 `git status`
- **THEN** `frontend/.env.development`、`frontend/.env.production`、`frontend/.env.staging` 不应被跟踪（仅 `.env.example` 保留）

#### Scenario: 运行时产物被忽略
- **WHEN** 执行 `git status`
- **THEN** `outputs/` 目录不应出现在未跟踪文件列表中

### Requirement: 清理已提交的敏感文件

系统 SHALL 从 Git 跟踪中移除不应被追踪的文件，但保留本地文件：

#### Scenario: 移除用户上传文件的 Git 跟踪
- **WHEN** 执行 `git rm --cached -r backend/uploads/ uploads/`
- **THEN** 文件从 Git 索引中移除，本地文件保留

#### Scenario: 移除测试报告的 Git 跟踪
- **WHEN** 执行 `git rm --cached -r frontend/tests/reports/`
- **THEN** 报告文件从 Git 索引中移除，本地文件保留

#### Scenario: 移除环境配置文件的 Git 跟踪
- **WHEN** 执行 `git rm --cached frontend/.env.development frontend/.env.production frontend/.env.staging`
- **THEN** 配置文件从 Git 索引中移除，本地文件保留

### Requirement: 清理配置文件中的硬编码凭据

系统 SHALL 将 `docker-compose.yml` 和 Spring Boot 配置文件中的硬编码密码替换为环境变量引用。

#### Scenario: docker-compose.yml 使用环境变量
- **WHEN** 查看 `docker-compose.yml`
- **THEN** 数据库密码使用 `${MYSQL_ROOT_PASSWORD}` 环境变量，不再硬编码 `root`

#### Scenario: application-dev.yml JWT 密钥使用环境变量
- **WHEN** 查看 `application-dev.yml`
- **THEN** `jwt.secret` 仅保留 `${JWT_SECRET}` 引用，移除默认值中的硬编码密钥字符串

### Requirement: 标准化 Git 工作流

系统 SHALL 建立标准化的分支规范、提交信息规范和推送流程。

---

## MODIFIED Requirements

无（本 spec 为新建）

## REMOVED Requirements

无（本 spec 为新建）
