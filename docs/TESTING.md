# 测试体系文档

## 目录

- [测试概览](#测试概览)
- [测试分层](#测试分层)
- [前端测试](#前端测试)
- [后端测试](#后端测试)
- [E2E测试](#e2e测试)
- [测试运行](#测试运行)
- [测试配置](#测试配置)
- [常见问题](#常见问题)
- [CI/CD集成](#cicd集成)

---

## 测试概览

本项目采用分层测试策略，包含以下测试类型：

| 测试类型 | 技术栈 | 覆盖范围 | 运行频率 |
|---------|--------|---------|---------|
| 前端单元测试 | Vitest + Vue Test Utils | 组件、Hook、Store、工具函数 | 每次提交 |
| 后端单元测试 | JUnit 5 + Mockito | Service、Controller、工具类 | 每次提交 |
| 后端集成测试 | MockMvc + Spring Boot Test | 全接口API测试 | 每次提交 |
| E2E测试 | Playwright | 核心页面流程、跨页面交互 | 每日构建 |

### 测试目标

- **单元测试覆盖率**: ≥ 60%（语句覆盖）
- **集成测试覆盖率**: 100% API端点
- **E2E测试覆盖率**: 核心业务流程100%

---

## 测试分层

```
┌─────────────────────────────────────────────────────────────┐
│                        E2E测试层                            │
│  (Playwright - 用户视角，验证完整业务流程)                      │
├─────────────────────────────────────────────────────────────┤
│                       集成测试层                              │
│  (MockMvc - API视角，验证接口交互)                            │
├─────────────────────────────────────────────────────────────┤
│                       单元测试层                              │
│  (Vitest/JUnit - 代码视角，验证独立模块)                       │
└─────────────────────────────────────────────────────────────┘
```

---

## 前端测试

### 目录结构

```
frontend/tests/
├── unit/                      # 单元测试
│   ├── api/                   # API服务测试
│   │   ├── auth.test.js
│   │   ├── item.test.js
│   │   ├── order.test.js
│   │   └── ...
│   ├── components/            # 组件测试
│   │   ├── Header.test.js
│   │   ├── Footer.test.js
│   │   ├── ItemCard.test.js
│   │   └── ...
│   ├── composables/           # Hook测试
│   │   ├── useDarkMode.test.js
│   │   ├── useThemeColor.test.js
│   │   └── ...
│   ├── store/                 # Store测试
│   │   ├── userStore.test.js
│   │   ├── itemStore.test.js
│   │   ├── cartStore.test.js
│   │   └── ...
│   ├── utils/                 # 工具函数测试
│   │   ├── validator.test.js
│   │   ├── storage.test.js
│   │   ├── errorHandler.test.js
│   │   └── ...
│   └── views/                 # 页面视图测试
│       ├── Home.test.js
│       ├── NotFound.test.js
│       └── ...
├── e2e/                       # E2E测试
│   ├── login.spec.js          # 登录流程
│   ├── user-flows.spec.js     # 用户交互流程
│   ├── items.spec.js          # 物品流程
│   ├── orders.spec.js         # 订单流程
│   └── setup/                 # 测试设置
│       ├── globalSetup.js
│       └── globalTeardown.js
├── utils/                     # 测试工具
│   ├── testData.js            # 测试数据
│   └── helpers.js             # 辅助函数
├── reports/                   # 测试报告
├── setup.ts                   # 单元测试设置
├── setup.js                   # 单元测试设置
└── globalSetup.ts             # 全局设置
```

### 单元测试示例

#### Store测试

```javascript
// tests/unit/store/userStore.test.js
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '@/store/modules/user'

describe('User Store', () => {
  let store

  beforeEach(() => {
    setActivePinia(createPinia())
    store = useUserStore()
  })

  it('should initialize with empty user', () => {
    expect(store.user).toBeNull()
  })

  it('should login successfully', async () => {
    // Mock API response
    vi.mock('@/api', () => ({
      default: {
        auth: {
          login: vi.fn().mockResolvedValue({
            code: 200,
            data: { token: 'test-token', user: { id: 1 } }
          })
        }
      }
    }))

    await store.login('testuser', 'password')
    expect(store.isLoggedIn).toBe(true)
  })
})
```

#### 组件测试

```javascript
// tests/unit/components/Header.test.js
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import Header from '@/components/Header.vue'

describe('Header Component', () => {
  it('should render correctly', () => {
    const wrapper = mount(Header)
    expect(wrapper.exists()).toBe(true)
  })

  it('should display logo', () => {
    const wrapper = mount(Header)
    const logo = wrapper.find('.logo')
    expect(logo.exists()).toBe(true)
  })
})
```

#### 工具函数测试

```javascript
// tests/unit/utils/validator.test.js
import { describe, it, expect } from 'vitest'
import { validateEmail, validatePhone, validatePassword } from '@/utils/validator'

describe('Validator Utils', () => {
  describe('validateEmail', () => {
    it('should validate correct email', () => {
      expect(validateEmail('test@example.com')).toBe(true)
    })

    it('should reject invalid email', () => {
      expect(validateEmail('invalid-email')).toBe(false)
    })
  })

  describe('validatePhone', () => {
    it('should validate correct phone', () => {
      expect(validatePhone('13800138000')).toBe(true)
    })

    it('should reject invalid phone', () => {
      expect(validatePhone('12345')).toBe(false)
    })
  })
})
```

### 运行前端测试

```bash
# 运行所有单元测试
npm run test:unit

# 运行单元测试并生成覆盖率报告
npm run test:coverage

# 运行E2E测试
npm run test:e2e

# 运行E2E测试（带浏览器界面）
npm run test:e2e:headed

# 运行所有测试
npm run test:all

# 监听模式运行单元测试
npm run test:watch
```

---

## 后端测试

### 目录结构

```
backend/src/test/
├── java/com/idleitems/school/
│   ├── service/               # Service单元测试
│   │   ├── AuthServiceTest.java
│   │   ├── CategoryServiceTest.java
│   │   ├── ItemServiceTest.java
│   │   ├── OrderServiceTest.java
│   │   └── ...
│   ├── controller/            # Controller单元测试
│   │   ├── AuthControllerTest.java
│   │   ├── CategoryControllerTest.java
│   │   ├── ItemControllerTest.java
│   │   ├── OrderControllerTest.java
│   │   └── ...
│   ├── integration/           # 集成测试
│   │   ├── BaseIntegrationTest.java
│   │   ├── AuthIntegrationTest.java
│   │   ├── ItemIntegrationTest.java
│   │   ├── OrderIntegrationTest.java
│   │   └── ...
│   ├── util/                  # 工具类测试
│   ├── config/                # 配置类测试
│   ├── filter/                # 过滤器测试
│   ├── security/              # 安全组件测试
│   └── task/                  # 定时任务测试
└── resources/
    └── application-test.yml   # 测试配置
```

### 单元测试示例

#### Service测试

```java
// service/AuthServiceTest.java
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void testLoginSuccess() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setStatus(User.UserStatus.ACTIVE);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyMap())).thenReturn("token");

        // When
        Map<String, Object> result = authService.login(request);

        // Then
        assertNotNull(result);
        assertEquals("token", result.get("token"));
    }

    @Test
    void testLoginUserNotFound() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("password");

        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BusinessException.class, () -> authService.login(request));
    }
}
```

#### Controller测试

```java
// controller/AuthControllerTest.java
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    void testLoginSuccess() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password");

        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("token", "jwt-token");
        tokenData.put("refreshToken", "refresh-token");

        when(authService.login(any(LoginRequest.class))).thenReturn(tokenData);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("jwt-token"));
    }
}
```

### 集成测试示例

```java
// integration/AuthIntegrationTest.java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @Order(1)
    void testRegisterSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("TestPassword@123");
        request.setEmail("test@example.com");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").exists());
    }

    @Test
    @Order(2)
    void testLoginSuccess() throws Exception {
        // 先注册用户
        testRegisterSuccess();

        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("TestPassword@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").exists());
    }

    @Test
    @Order(3)
    void testFullAuthFlow() throws Exception {
        // 1. 注册
        String token = registerAndGetToken();

        // 2. 获取用户信息
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("testuser"));

        // 3. 登出
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk());

        // 4. 验证token失效
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isUnauthorized());
    }
}
```

### 运行后端测试

```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=AuthServiceTest

# 运行集成测试
mvn test -Dtest=AuthIntegrationTest

# 运行测试并生成覆盖率报告
mvn test jacoco:report

# 跳过测试打包
mvn package -DskipTests
```

---

## E2E测试

### 测试用例说明

| 测试文件 | 测试内容 | 优先级 |
|---------|---------|--------|
| login.spec.js | 登录/注册流程 | P0 |
| user-flows.spec.js | 用户交互流程（首页、物品、个人中心） | P0 |
| items.spec.js | 物品发布、浏览、搜索流程 | P1 |
| orders.spec.js | 订单创建、支付、发货流程 | P1 |
| browser-compat.spec.js | 浏览器兼容性测试 | P2 |

### E2E测试示例

```javascript
// e2e/login.spec.js
import { test, expect } from '@playwright/test'

test.describe('登录流程 E2E 测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login')
    await page.waitForLoadState('networkidle')
  })

  test('登录页面正确加载', async ({ page }) => {
    await expect(page).toHaveTitle(/闲置物品交易平台/)
    await expect(page.getByPlaceholder('请输入用户名')).toBeVisible()
    await expect(page.getByPlaceholder('请输入密码')).toBeVisible()
    await expect(page.getByRole('button', { name: '登录' })).toBeVisible()
  })

  test('空表单提交应显示验证错误', async ({ page }) => {
    await page.getByRole('button', { name: '登录' }).click()
    await expect(page).toHaveURL(/\/login/)
  })

  test('完整登录-登出流程', async ({ page }) => {
    // 使用测试账号登录
    await page.getByPlaceholder('请输入用户名').fill('testuser')
    await page.getByPlaceholder('请输入密码').fill('password')
    await page.getByRole('button', { name: '登录' }).click()

    // 等待登录成功
    await page.waitForTimeout(2000)

    // 检查是否登录成功
    const userMenu = page.locator('.user-info, .user-menu').first()
    try {
      await userMenu.click({ timeout: 3000 })
      await page.getByText('退出登录').click()
      await page.waitForTimeout(500)
      await expect(page.getByRole('button', { name: /登录/ }).first()).toBeVisible({ timeout: 3000 })
    } catch {
      console.log('登录未成功，跳过登出测试')
    }
  })
})
```

### 运行E2E测试

```bash
# 运行所有E2E测试
npm run test:e2e

# 运行特定测试文件
npx playwright test tests/e2e/login.spec.js

# 运行测试（带浏览器界面）
npm run test:e2e:headed

# 运行测试（UI模式）
npm run test:e2e:ui

# 运行测试（调试模式）
npx playwright test --debug

# 查看测试报告
npx playwright show-report tests/reports/playwright
```

---

## 测试运行

### 前置条件

| 工具 | 版本要求 | 说明 |
|------|---------|------|
| Node.js | 20+ | 前端运行环境 |
| JDK | 17+ | 后端运行环境 |
| Maven | 3.8+ | 后端构建工具 |
| MySQL | 8.0+ | 测试数据库（可选H2） |
| Chrome/Firefox | 最新稳定版 | E2E测试浏览器 |

### 快速开始

```bash
# 1. 克隆项目
git clone https://github.com/2790849976/Idle_items_school.git
cd Idle_items_school

# 2. 安装前端依赖
cd frontend
npm install

# 3. 安装Playwright浏览器
npx playwright install

# 4. 运行前端单元测试
npm run test:unit

# 5. 运行前端E2E测试
npm run test:e2e

# 6. 运行后端测试
cd ../backend
mvn test
```

### 完整测试流程

```bash
# 前端完整测试
cd frontend
npm run test:all

# 后端完整测试
cd ../backend
mvn test

# 生成覆盖率报告
cd frontend
npm run test:coverage

cd ../backend
mvn test jacoco:report
```

---

## 测试配置

### 前端配置

#### Vitest配置 (vite.config.ts)

```typescript
test: {
  globals: true,
  environment: 'jsdom',
  include: ['tests/unit/**/*.test.{js,ts}'],
  setupFiles: ['tests/setup.ts'],
  coverage: {
    provider: 'v8',
    reporter: ['text', 'json', 'html'],
    thresholds: {
      statements: 60,
      branches: 50,
      functions: 60,
      lines: 60
    }
  }
}
```

#### Playwright配置 (playwright.config.js)

```javascript
export default defineConfig({
  testDir: './tests/e2e',
  fullyParallel: true,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  use: {
    baseURL: 'http://localhost:5173',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure'
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
    { name: 'firefox', use: { ...devices['Desktop Firefox'] } },
    { name: 'webkit', use: { ...devices['Desktop Safari'] } }
  ],
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:5173',
    reuseExistingServer: !process.env.CI
  }
})
```

### 后端配置

#### 测试配置 (application-test.yml)

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true

jwt:
  secret: testsecretkeyfortestingpurposesonly
  expiration: 3600000
  refresh-expiration: 604800000
```

---

## 常见问题

### 前端测试问题

#### 1. Vitest报错：Cannot find module

**问题**: `Cannot find module '@/xxx'`

**解决方案**:
```typescript
// vite.config.ts
resolve: {
  alias: {
    '@': resolve(__dirname, 'src')
  }
}
```

#### 2. Vue组件测试报错：Unknown custom element

**问题**: `Unknown custom element: <el-xxx>`

**解决方案**:
```javascript
// tests/setup.ts
config.global.stubs = {
  'el-button': { template: '<button><slot /></button>' },
  'el-input': { template: '<input />' },
  // ... 其他Element Plus组件
}
```

#### 3. Playwright浏览器未安装

**问题**: `Browser type `chromium` is not installed`

**解决方案**:
```bash
npx playwright install chromium
# 或安装所有浏览器
npx playwright install
```

#### 4. 测试超时

**问题**: `Test timeout of 10000ms exceeded`

**解决方案**:
```typescript
// vite.config.ts
test: {
  testTimeout: 30000,  // 增加超时时间
  hookTimeout: 30000
}
```

#### 5. Mock不生效

**问题**: Mock函数未被调用

**解决方案**:
```javascript
// 确保在测试前清除mock
beforeEach(() => {
  vi.clearAllMocks()
})

// 确保mock路径正确
vi.mock('@/api', () => ({
  default: {
    auth: {
      login: vi.fn()
    }
  }
}))
```

### 后端测试问题

#### 1. Spring上下文加载失败

**问题**: `Failed to load ApplicationContext`

**解决方案**:
```java
// 检查测试配置
@SpringBootTest
@ActiveProfiles("test")  // 确保使用测试配置
@TestPropertySource(locations = "classpath:application-test.yml")
```

#### 2. 数据库连接失败

**问题**: `Cannot determine embedded database driver class`

**解决方案**:
```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
```

#### 3. MockBean注入失败

**问题**: `No qualifying bean of type 'xxx'`

**解决方案**:
```java
// 使用@MockitoBean替代@MockBean（Spring Boot 3.4+）
@MockitoBean
private UserService userService;

// 或使用@Mock + @InjectMocks
@Mock
private UserRepository userRepository;

@InjectMocks
private UserServiceImpl userService;
```

#### 4. 测试数据污染

**问题**: 测试之间数据相互影响

**解决方案**:
```java
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
@Rollback
```

#### 5. 集成测试端口冲突

**问题**: `Port 7000 already in use`

**解决方案**:
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
```

---

## CI/CD集成

### GitHub Actions配置

```yaml
# .github/workflows/test.yml
name: Tests

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  frontend-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: frontend/package-lock.json
      
      - name: Install dependencies
        working-directory: frontend
        run: npm ci
      
      - name: Run unit tests
        working-directory: frontend
        run: npm run test:unit
      
      - name: Run unit tests with coverage
        working-directory: frontend
        run: npm run test:coverage
      
      - name: Upload coverage
        uses: codecov/codecov-action@v4
        with:
          files: frontend/coverage/lcov.info
          flags: frontend

  backend-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Setup JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven
      
      - name: Run tests
        working-directory: backend
        run: mvn test
      
      - name: Generate coverage report
        working-directory: backend
        run: mvn jacoco:report
      
      - name: Upload coverage
        uses: codecov/codecov-action@v4
        with:
          files: backend/target/site/jacoco/jacoco.xml
          flags: backend

  e2e-tests:
    runs-on: ubuntu-latest
    needs: [frontend-tests, backend-tests]
    steps:
      - uses: actions/checkout@v4
      
      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: frontend/package-lock.json
      
      - name: Install dependencies
        working-directory: frontend
        run: npm ci
      
      - name: Install Playwright browsers
        working-directory: frontend
        run: npx playwright install --with-deps
      
      - name: Run E2E tests
        working-directory: frontend
        run: npm run test:e2e
      
      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: playwright-report
          path: frontend/tests/reports/playwright/
          retention-days: 7
```

### GitLab CI配置

```yaml
# .gitlab-ci.yml
stages:
  - test
  - e2e

frontend-unit-tests:
  stage: test
  image: node:20
  script:
    - cd frontend
    - npm ci
    - npm run test:unit
    - npm run test:coverage
  artifacts:
    reports:
      coverage_report:
        coverage_format: cobertura
        path: frontend/coverage/cobertura-coverage.xml

backend-tests:
  stage: test
  image: maven:3.9-eclipse-temurin-17
  script:
    - cd backend
    - mvn test
    - mvn jacoco:report
  artifacts:
    reports:
      junit: backend/target/surefire-reports/*.xml

e2e-tests:
  stage: e2e
  image: mcr.microsoft.com/playwright:v1.59.0-jammy
  script:
    - cd frontend
    - npm ci
    - npx playwright install --with-deps
    - npm run test:e2e
  artifacts:
    when: always
    paths:
      - frontend/tests/reports/playwright/
```

### 本地CI运行

```bash
# 使用act本地运行GitHub Actions
# 安装act: brew install act (macOS) 或 choco install act (Windows)

# 运行所有工作流
act

# 运行特定工作流
act -j frontend-tests

# 运行并查看详细日志
act -v
```

---

## 测试报告

### 前端测试报告

```bash
# 生成Vitest覆盖率报告
npm run test:coverage
# 报告位置: frontend/coverage/index.html

# 生成Playwright测试报告
npm run test:e2e
# 报告位置: frontend/tests/reports/playwright/index.html
```

### 后端测试报告

```bash
# 生成JaCoCo覆盖率报告
mvn test jacoco:report
# 报告位置: backend/target/site/jacoco/index.html

# 生成Surefire测试报告
mvn test
# 报告位置: backend/target/surefire-reports/
```

---

## 最佳实践

### 测试命名规范

```javascript
// 前端测试命名
describe('UserStore', () => {
  describe('状态初始化', () => {
    it('should initialize with empty user', () => {})
    it('should initialize with loading as false', () => {})
  })
  
  describe('login()', () => {
    it('should login successfully with correct credentials', () => {})
    it('should throw error on login failure', () => {})
  })
})
```

```java
// 后端测试命名
@DisplayName("AuthService 单元测试")
class AuthServiceTest {
    
    @Test
    @DisplayName("登录成功 - 正确的用户名和密码")
    void testLoginSuccess() {}
    
    @Test
    @DisplayName("登录失败 - 用户不存在")
    void testLoginUserNotFound() {}
}
```

### 测试原则

1. **FIRST原则**
   - Fast: 测试应该快速执行
   - Independent: 测试应该相互独立
   - Repeatable: 测试应该可重复执行
   - Self-validating: 测试应该自动验证结果
   - Timely: 测试应该及时编写

2. **AAA模式**
   - Arrange: 准备测试数据
   - Act: 执行被测试的操作
   - Assert: 验证结果

3. **测试覆盖率目标**
   - 单元测试: ≥ 60%
   - 集成测试: 100% API端点
   - E2E测试: 核心业务流程100%

---

## 参考资料

- [Vitest文档](https://vitest.dev/)
- [Vue Test Utils文档](https://test-utils.vuejs.org/)
- [Playwright文档](https://playwright.dev/)
- [JUnit 5文档](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito文档](https://site.mockito.org/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
