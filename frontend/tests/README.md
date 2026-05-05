# 自动化测试方案

## 概述

本项目实施全面的自动化测试策略，覆盖前端组件测试、API 接口测试、用户交互流程测试及浏览器兼容性测试。

## 测试体系架构

```
tests/
├── unit/                           # 单元测试（Vitest）
│   ├── store/
│   │   └── userStore.test.js      # 用户状态管理测试
│   ├── utils/
│   │   ├── storage.test.js        # 存储工具测试
│   │   └── api.test.js            # API 模块测试
│   └── components/
│       ├── Login.test.js           # 登录组件测试
│       └── Header.test.js          # 头部组件测试
├── e2e/                            # E2E 测试（Playwright）
│   ├── login.spec.js              # 登录注册流程
│   ├── browser-compat.spec.js    # 浏览器兼容性
│   ├── user-flows.spec.js         # 用户交互流程
│   ├── utils/testData.js          # 测试数据
│   └── setup/globalSetup.js       # 全局设置
├── reports/                        # 测试报告
│   ├── coverage/                   # 覆盖率报告
│   └── playwright/                # E2E 测试报告
├── setup.js                        # Vitest 全局设置
├── playwright.config.js            # Playwright 配置
└── README.md                        # 本文档
```

## 测试框架

| 层级 | 框架 | 用途 |
|------|------|------|
| 单元测试 | Vitest + @vue/test-utils | 组件、Store、工具函数 |
| E2E 测试 | Playwright | 完整用户流程、跨浏览器 |
| Mock | Vitest vi.fn() / @vue/test-utils | 隔离外部依赖 |
| 覆盖率 | @vitest/coverage-v8 | 生成覆盖率报告 |

## 快速开始

### 安装依赖

```bash
cd frontend
npm install
```

### 安装 Playwright 浏览器

```bash
npx playwright install --with-deps
```

### 运行所有测试

```bash
npm run test:all
```

### 单元测试

```bash
# 单次运行
npm run test:unit

# 监听模式（开发时）
npm run test:watch

# 带覆盖率
npm run test:coverage
```

### E2E 测试

```bash
# 运行所有浏览器
npm run test:e2e

# 带 UI（可视化）
npm run test:e2e:ui

# 带浏览器界面
npm run test:e2e:headed
```

## 测试用例说明

### 单元测试覆盖范围

#### UserStore (`tests/unit/store/userStore.test.js`)

| 测试项 | 描述 |
|--------|------|
| 状态初始化 | token、user、loading 初始值 |
| 计算属性 | isLoggedIn、isAdmin |
| login() | 登录成功/失败、loading 状态、存储 |
| logout() | 状态清除、存储清理 |
| getCurrentUser() | 获取用户信息、错误处理 |
| checkTokenExpiry() | 令牌过期判断 |
| refreshToken() | 令牌刷新 |
| register() | 用户注册 |

#### Storage 工具 (`tests/unit/utils/storage.test.js`)

| 测试项 | 描述 |
|--------|------|
| set/get | 字符串、对象、数组、布尔值、数字 |
| remove | 删除指定键 |
| clear | 清空所有数据 |
| 数据加密 | base64 加密/解密 |
| 错误处理 | 不存在的 key、损坏数据 |

#### API 模块 (`tests/unit/utils/api.test.js`)

| 测试项 | 描述 |
|--------|------|
| Auth API | login、register、getCurrentUser、refreshToken |
| Item API | getItems、createItem、updateItem、uploadImage |
| 错误处理 | 401、403、404、500、网络错误 |

#### 组件测试 (`tests/unit/components/`)

| 测试项 | 描述 |
|--------|------|
| 渲染 | 页面元素、表单、按钮 |
| 表单验证 | 空表单、错误输入 |
| 交互 | 登录、登出、导航 |
| 状态 | 登录/未登录状态切换 |

### E2E 测试覆盖范围

#### 登录注册流程 (`tests/e2e/login.spec.js`)

- 页面正确加载
- 表单输入
- 表单验证
- 登录成功/失败
- 登出功能
- 页面跳转

#### 浏览器兼容性 (`tests/e2e/browser-compat.spec.js`)

- 登录状态存储（Chrome/Firefox/Safari/Edge）
- 页面刷新状态保持
- 路由切换状态一致性
- ES6+ 特性支持
- 响应式布局（桌面/平板/手机）

#### 用户交互流程 (`tests/e2e/user-flows.spec.js`)

- 首页加载和搜索
- 物品浏览
- 个人中心
- 发布物品
- 管理后台
- 404 页面
- 性能测试

## CI/CD 集成

### GitHub Actions

CI 工作流文件：`.github/workflows/test.yml`

触发条件：
- 推送到 main/develop 分支
- 提交 frontend/ 目录下的文件
- Pull Request 到 main/develop

测试矩阵：
- Node.js: 18.x, 20.x
- Playwright 浏览器: Chromium, Firefox, WebKit

### 本地 CI 模拟

```bash
# 模拟 CI 环境
CI=true npm run test:unit
CI=true npm run test:e2e
```

## 测试覆盖率目标

| 类型 | 目标覆盖率 |
|------|-----------|
| 语句 (statements) | ≥ 70% |
| 分支 (branches) | ≥ 60% |
| 函数 (functions) | ≥ 70% |
| 行 (lines) | ≥ 70% |

查看覆盖率报告：

```bash
# HTML 报告
open tests/reports/coverage/index.html
```

## 测试报告

### 单元测试报告

```bash
# 终端输出
npm run test:unit

# JSON 报告
tests/reports/coverage/coverage-summary.json
```

### E2E 测试报告

```bash
# HTML 报告
tests/reports/playwright/index.html

# JSON 结果
tests/reports/playwright/results.json
```

## 测试数据

测试账号和数据定义在 `tests/utils/testData.js`

## 故障排除

### 单元测试常见问题

**模块导入失败**

```bash
# 确保 @ 别名配置正确
# 检查 vite.config.js 中的 resolve.alias
```

**Vue 组件测试报错**

```bash
# 确保 @vue/test-utils 和 vue 版本兼容
npm ls @vue/test-utils vue
```

### E2E 测试常见问题

**浏览器未安装**

```bash
npx playwright install --with-deps
```

**端口被占用**

```bash
# 修改 playwright.config.js 中的端口
# 或 kill占用端口的进程
```

**测试超时**

```bash
# 增加超时时间
# 修改 playwright.config.js 中的 actionTimeout
```

## 最佳实践

1. **单元测试**：每个 store 方法和工具函数都应有测试
2. **E2E 测试**：覆盖主要用户路径，但不追求 100% 覆盖
3. **测试隔离**：单元测试不应依赖外部服务，使用 Mock
4. **测试数据**：使用工厂函数生成测试数据
5. **持续集成**：每次 PR 都应运行完整测试套件
6. **覆盖率监控**：新增代码应达到覆盖率要求

## 相关文档

- Vitest 文档：https://vitest.dev/
- Playwright 文档：https://playwright.dev/
- @vue/test-utils 文档：https://test-utils.vuejs.org/
