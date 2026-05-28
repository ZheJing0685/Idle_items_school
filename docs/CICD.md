# CI/CD 配置说明

本文档说明项目的 CI/CD 配置，包括 GitHub Actions 和 GitLab CI。

## 目录

- [GitHub Actions](#github-actions)
- [GitLab CI](#gitlab-ci)
- [本地运行](#本地运行)
- [环境变量](#环境变量)
- [常见问题](#常见问题)

---

## GitHub Actions

### 工作流文件

项目包含以下 GitHub Actions 工作流文件：

| 文件 | 用途 | 触发条件 |
|------|------|---------|
| `.github/workflows/ci-cd.yml` | 完整 CI/CD 流水线 | push/PR 到 main/develop |
| `.github/workflows/test.yml` | 独立测试流水线 | push/PR 到 main/develop |
| `.github/workflows/backend-tests.yml` | 后端测试流水线 | 后端代码变更 |
| `.github/workflows/qodana_code_quality.yml` | 代码质量检查 | push/PR |

### 流水线阶段

```
┌─────────────────┐    ┌─────────────────┐
│ Frontend Tests  │    │ Backend Tests   │
│ (Unit + Coverage)│    │ (Unit + Integration)│
└────────┬────────┘    └────────┬────────┘
         │                      │
         └──────────┬───────────┘
                    │
            ┌───────▼───────┐
            │   E2E Tests   │
            │  (Playwright) │
            └───────┬───────┘
                    │
            ┌───────▼───────┐
            │     Build     │
            │ (Frontend +   │
            │  Backend)     │
            └───────┬───────┘
                    │
            ┌───────▼───────┐
            │    Deploy     │
            │ (Docker Images)│
            └───────────────┘
```

### 运行测试

#### 前端单元测试

```yaml
frontend-unit-tests:
  runs-on: ubuntu-latest
  steps:
    - uses: actions/checkout@v4
    - uses: actions/setup-node@v4
      with:
        node-version: '20'
    - run: npm ci
    - run: npm run test:unit
    - run: npm run test:coverage
```

#### 后端单元测试

```yaml
backend-unit-tests:
  runs-on: ubuntu-latest
  steps:
    - uses: actions/checkout@v4
    - uses: actions/setup-java@v4
      with:
        java-version: '17'
    - run: mvn test -Dtest="!*IntegrationTest"
    - run: mvn jacoco:report
```

#### 后端集成测试

```yaml
backend-integration-tests:
  runs-on: ubuntu-latest
  needs: [backend-unit-tests]
  steps:
    - uses: actions/checkout@v4
    - uses: actions/setup-java@v4
      with:
        java-version: '17'
    - run: mvn test -Dtest="*IntegrationTest"
```

#### E2E 测试

```yaml
e2e-tests:
  runs-on: ubuntu-latest
  needs: [frontend-unit-tests, backend-unit-tests]
  steps:
    - uses: actions/checkout@v4
    - uses: actions/setup-node@v4
      with:
        node-version: '20'
    - run: npm ci
    - run: npx playwright install --with-deps
    - run: npm run test:e2e
```

### 覆盖率报告

覆盖率报告会自动上传到 Codecov：

```yaml
- name: Upload coverage to Codecov
  uses: codecov/codecov-action@v4
  with:
    files: frontend/coverage/lcov.info
    flags: frontend
```

### 测试结果

测试结果作为 artifact 上传：

```yaml
- name: Upload test results
  if: always()
  uses: actions/upload-artifact@v4
  with:
    name: playwright-report
    path: frontend/tests/reports/playwright/
    retention-days: 7
```

---

## GitLab CI

### 配置文件

项目根目录包含 `.gitlab-ci.yml` 配置文件。

### 流水线阶段

```yaml
stages:
  - test          # 单元测试
  - integration   # 集成测试
  - e2e           # E2E测试
  - build         # 构建
  - deploy        # 部署
```

### 服务配置

集成测试需要以下服务：

```yaml
services:
  - name: mysql:8.0
    alias: test-db
    variables:
      MYSQL_ROOT_PASSWORD: testroot
      MYSQL_DATABASE: testdb
  - name: redis:7-alpine
    alias: test-redis
```

### 运行测试

#### 前端单元测试

```yaml
frontend-unit-tests:
  stage: test
  image: node:20-slim
  script:
    - npm ci
    - npm run test:unit
    - npm run test:coverage
  coverage: '/All files\s*\|\s*([\d\.]+)/'
  artifacts:
    reports:
      coverage_report:
        coverage_format: cobertura
        path: frontend/coverage/cobertura-coverage.xml
```

#### 后端单元测试

```yaml
backend-unit-tests:
  stage: test
  image: maven:3.9-eclipse-temurin-17
  script:
    - mvn test -Dtest="!*IntegrationTest"
    - mvn jacoco:report
  coverage: '/Total.*?(\d+\.\d+)%/'
  artifacts:
    reports:
      junit: backend/target/surefire-reports/TEST-*.xml
```

#### 后端集成测试

```yaml
backend-integration-tests:
  stage: integration
  image: maven:3.9-eclipse-temurin-17
  services:
    - name: mysql:8.0
    - name: redis:7-alpine
  variables:
    SPRING_DATASOURCE_URL: "jdbc:mysql://test-db:3306/testdb"
    SPRING_REDIS_HOST: "test-redis"
  script:
    - mvn test -Dtest="*IntegrationTest"
```

#### E2E 测试

```yaml
e2e-tests:
  stage: e2e
  image: mcr.microsoft.com/playwright:v1.59.0-jammy
  script:
    - npm ci
    - npx playwright install --with-deps
    - npm run test:e2e
  artifacts:
    paths:
      - frontend/tests/reports/playwright/
```

### Docker 部署

```yaml
deploy-docker:
  stage: deploy
  image: docker:24-dind
  services:
    - docker:24-dind
  script:
    - docker build -t $CI_REGISTRY_IMAGE/backend:latest ./backend
    - docker push $CI_REGISTRY_IMAGE/backend:latest
    - docker build -t $CI_REGISTRY_IMAGE/frontend:latest ./frontend
    - docker push $CI_REGISTRY_IMAGE/frontend:latest
  rules:
    - if: '$CI_COMMIT_BRANCH == "main"'
```

---

## 本地运行

### 使用 act 运行 GitHub Actions

```bash
# 安装 act
# macOS
brew install act

# Windows
choco install act

# 运行所有工作流
act

# 运行特定工作流
act -j frontend-unit-tests

# 运行并查看详细日志
act -v

# 使用特定事件触发
act push
```

### 使用 Docker Compose 运行集成测试

```bash
# 启动测试环境
docker-compose -f docker-compose.test.yml up -d

# 运行后端集成测试
cd backend
mvn test -Dtest="*IntegrationTest"

# 停止测试环境
docker-compose -f docker-compose.test.yml down
```

### 本地测试脚本

```bash
# Linux/Mac
./scripts/run-tests.sh all

# Windows
scripts\run-tests.bat all
```

---

## 环境变量

### GitHub Actions Secrets

在 GitHub 仓库设置中配置以下 Secrets：

| Secret | 用途 | 必需 |
|--------|------|------|
| `CODECOV_TOKEN` | Codecov 上传令牌 | 否 |
| `DOCKER_USERNAME` | Docker Hub 用户名 | 否 |
| `DOCKER_PASSWORD` | Docker Hub 密码 | 否 |

### GitLab CI Variables

在 GitLab 项目设置中配置以下 Variables：

| Variable | 用途 | 必需 |
|----------|------|------|
| `CI_REGISTRY_USER` | 容器 registry 用户名 | 否 |
| `CI_REGISTRY_PASSWORD` | 容器 registry 密码 | 否 |
| `CODECOV_TOKEN` | Codecov 上传令牌 | 否 |

### 测试环境变量

集成测试使用以下默认变量：

```yaml
# 数据库
SPRING_DATASOURCE_URL: "jdbc:h2:mem:testdb"
SPRING_DATASOURCE_USERNAME: "sa"
SPRING_DATASOURCE_PASSWORD: ""

# Redis
SPRING_REDIS_HOST: "localhost"
SPRING_REDIS_PORT: "6379"

# JWT
JWT_SECRET: "testsecretkey"
JWT_EXPIRATION: "3600000"
```

---

## 常见问题

### 1. Playwright 浏览器安装失败

**问题**: `Failed to download Chromium`

**解决方案**:
```yaml
# 使用官方 Playwright 镜像
image: mcr.microsoft.com/playwright:v1.59.0-jammy

# 或手动安装
- run: npx playwright install --with-deps chromium
```

### 2. Maven 缓存问题

**问题**: `Could not resolve dependencies`

**解决方案**:
```yaml
# 清除缓存
- run: mvn dependency:purge-local-repository

# 或使用新的缓存 key
cache:
  key: ${CI_COMMIT_REF_SLUG}-${CI_PIPELINE_ID}
```

### 3. 内存不足

**问题**: `JavaScript heap out of memory`

**解决方案**:
```yaml
variables:
  NODE_OPTIONS: "--max-old-space-size=4096"
```

### 4. 测试超时

**问题**: `Timeout of 10000ms exceeded`

**解决方案**:
```yaml
# 增加超时时间
script:
  - npm run test:e2e -- --timeout=60000
```

### 5. 数据库连接失败

**问题**: `Connection refused`

**解决方案**:
```yaml
# 等待服务就绪
before_script:
  - sleep 30
  
# 或使用健康检查
services:
  - name: mysql:8.0
    variables:
      MYSQL_ROOT_PASSWORD: test
  script:
    - until mysqladmin ping -h test-db; do sleep 1; done
```

### 6. Codecov 上传失败

**问题**: `Token not found`

**解决方案**:
```yaml
# 确保配置了 CODECOV_TOKEN
- uses: codecov/codecov-action@v4
  with:
    token: ${{ secrets.CODECOV_TOKEN }}
    files: ./coverage/lcov.info
```

### 7. Docker 构建失败

**问题**: `denied: permission denied`

**解决方案**:
```yaml
# 确保有推送权限
- name: Log in to Container Registry
  uses: docker/login-action@v3
  with:
    registry: ghcr.io
    username: ${{ github.actor }}
    password: ${{ secrets.GITHUB_TOKEN }}
```

---

## 最佳实践

### 1. 测试分离

- 单元测试和集成测试分开运行
- E2E 测试在所有单元测试通过后运行

### 2. 缓存优化

```yaml
# npm 缓存
cache:
  key: ${CI_COMMIT_REF_SLUG}
  paths:
    - frontend/node_modules/

# Maven 缓存
cache:
  key: ${CI_COMMIT_REF_SLUG}
  paths:
    - .m2/repository/
```

### 3. 并行执行

```yaml
# 前端和后端测试并行运行
jobs:
  frontend-tests:
    # ...
  backend-tests:
    # 这两个任务会并行执行
```

### 4. 条件执行

```yaml
# 仅在特定条件下运行
rules:
  - if: '$CI_PIPELINE_SOURCE == "merge_request_event"'
  - if: '$CI_COMMIT_BRANCH == "main"'
  - if: '$CI_COMMIT_BRANCH == "develop"'
```

### 5. 失败处理

```yaml
# 即使失败也上传结果
- name: Upload test results
  if: always()
  uses: actions/upload-artifact@v4
  with:
    name: test-results
    path: ./test-results/
```

---

## 参考文档

- [GitHub Actions 文档](https://docs.github.com/en/actions)
- [GitLab CI/CD 文档](https://docs.gitlab.com/ee/ci/)
- [Codecov 文档](https://docs.codecov.com/)
- [Playwright CI 文档](https://playwright.dev/docs/ci)
