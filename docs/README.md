# 状态管理模块API文档

## 1. 概述

本状态管理模块使用Pinia进行状态管理，主要包括用户登录状态管理、物品管理和购物车管理。本文档详细介绍了用户登录状态管理的API和使用示例。

## 2. 安装和配置

### 2.1 安装依赖

```bash
npm install pinia
```

### 2.2 配置Pinia

在 `main.js` 中配置Pinia：

```javascript
import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import router from './router'
import pinia from './store'
import './style.css'
import App from './App.vue'

const app = createApp(App)
app.use(ElementPlus)
app.use(router)
app.use(pinia)
app.mount('#app')
```

## 3. User Store API

### 3.1 导入和使用

```javascript
import { userStore } from './store'

// 获取store实例
const store = userStore()
```

### 3.2 状态

| 状态 | 类型 | 描述 |
|------|------|------|
| token | string | 用户令牌 |
| user | object | 用户信息 |
| loading | boolean | 加载状态 |
| lastLoginTime | string | 最后登录时间 |
| rememberMe | boolean | 是否记住登录状态 |

### 3.3 计算属性

| 计算属性 | 类型 | 描述 |
|---------|------|------|
| isLoggedIn | boolean | 用户是否已登录 |
| isAdmin | boolean | 用户是否为管理员 |
| loginDuration | number | 登录持续时间（毫秒） |

### 3.4 方法

#### 3.4.1 login(username, password, remember)

**参数**：
- `username` (string): 用户名
- `password` (string): 密码
- `remember` (boolean, optional): 是否记住登录状态，默认false

**返回值**：Promise<object> - 登录响应数据

**示例**：

```javascript
const handleLogin = async () => {
  try {
    const response = await store.login('testuser', 'test123', true)
    console.log('登录成功:', response)
  } catch (error) {
    console.error('登录失败:', error)
  }
}
```

#### 3.4.2 register(userData)

**参数**：
- `userData` (object): 用户注册数据

**返回值**：Promise<object> - 注册响应数据

**示例**：

```javascript
const handleRegister = async () => {
  try {
    const response = await store.register({
      username: 'newuser',
      password: 'password123',
      email: 'newuser@example.com',
      phone: '13800138000',
      nickname: '新用户'
    })
    console.log('注册成功:', response)
  } catch (error) {
    console.error('注册失败:', error)
  }
}
```

#### 3.4.3 logout()

**参数**：无

**返回值**：无

**示例**：

```javascript
const handleLogout = () => {
  store.logout()
  console.log('已登出')
}
```

#### 3.4.4 getCurrentUser()

**参数**：无

**返回值**：Promise<object> - 用户信息

**示例**：

```javascript
const fetchUserInfo = async () => {
  try {
    const userInfo = await store.getCurrentUser()
    console.log('用户信息:', userInfo)
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}
```

#### 3.4.5 checkTokenExpiry()

**参数**：无

**返回值**：boolean - 令牌是否过期

**示例**：

```javascript
const isExpired = store.checkTokenExpiry()
console.log('令牌是否过期:', isExpired)
```

#### 3.4.6 refreshToken()

**参数**：无

**返回值**：Promise<string> - 刷新后的令牌

**示例**：

```javascript
const refreshToken = async () => {
  try {
    const newToken = await store.refreshToken()
    console.log('令牌已刷新:', newToken)
  } catch (error) {
    console.error('令牌刷新失败:', error)
  }
}
```

## 4. 存储工具类

### 4.1 导入和使用

```javascript
import storage from '../utils/storage'

// 使用持久化存储（localStorage）
const persistentStorage = storage.persistent

// 使用会话存储（sessionStorage）
const sessionStorage = storage.session

// 使用Cookie存储
const cookieStorage = storage.cookie

// 根据配置选择存储方案
const storageInstance = storage.getStorage('persistent') // 'persistent' | 'session' | 'cookie'
```

### 4.2 方法

#### 4.2.1 set(key, value)

**参数**：
- `key` (string): 存储键名
- `value` (any): 存储值

**返回值**：无

**示例**：

```javascript
storage.persistent.set('token', 'your-token-here')
```

#### 4.2.2 get(key)

**参数**：
- `key` (string): 存储键名

**返回值**：any - 存储值，如果不存在或已过期则返回null

**示例**：

```javascript
const token = storage.persistent.get('token')
console.log('Token:', token)
```

#### 4.2.3 remove(key)

**参数**：
- `key` (string): 存储键名

**返回值**：无

**示例**：

```javascript
storage.persistent.remove('token')
```

#### 4.2.4 clear()

**参数**：无

**返回值**：无

**示例**：

```javascript
storage.persistent.clear()
```

## 5. 错误处理工具类

### 5.1 导入和使用

```javascript
import ErrorHandler from '../utils/errorHandler'
```

### 5.2 方法

#### 5.2.1 handleError(error, options)

**参数**：
- `error` (Error): 错误对象
- `options` (object, optional): 配置选项
  - `silent` (boolean): 是否静默处理，默认false
  - `callback` (function): 回调函数

**返回值**：object - 分类后的错误信息

**示例**：

```javascript
try {
  // 执行可能出错的操作
} catch (error) {
  ErrorHandler.handleError(error, {
    silent: false,
    callback: (error, classifiedError) => {
      console.log('错误处理完成:', classifiedError)
    }
  })
}
```

#### 5.2.2 handleLoginError(error)

**参数**：
- `error` (Error): 错误对象

**返回值**：Promise<object> - 分类后的错误信息

**示例**：

```javascript
try {
  // 执行登录操作
} catch (error) {
  const classifiedError = await ErrorHandler.handleLoginError(error)
  console.log('登录错误处理:', classifiedError)
}
```

#### 5.2.3 handleTokenExpiry()

**参数**：无

**返回值**：Promise<boolean> - 是否成功刷新令牌

**示例**：

```javascript
const success = await ErrorHandler.handleTokenExpiry()
if (success) {
  console.log('令牌刷新成功')
} else {
  console.log('令牌刷新失败')
}
```

## 6. 请求管理工具类

### 6.1 导入和使用

```javascript
import requestManager from '../utils/requestManager'
```

### 6.2 方法

#### 6.2.1 request(url, requestFn, options)

**参数**：
- `url` (string): 请求URL
- `requestFn` (function): 请求函数
- `options` (object, optional): 配置选项
  - `useCache` (boolean): 是否使用缓存，默认true
  - `useMerge` (boolean): 是否使用请求合并，默认true
  - `params` (object): 请求参数

**返回值**：Promise<any> - 请求结果

**示例**：

```javascript
const result = await requestManager.request('/api/items', () => axios.get('/api/items'), {
  useCache: true,
  useMerge: true,
  params: { page: 1, size: 10 }
})
console.log('请求结果:', result)
```

#### 6.2.2 clearCache(url, params)

**参数**：
- `url` (string): 请求URL
- `params` (object, optional): 请求参数

**返回值**：无

**示例**：

```javascript
requestManager.clearCache('/api/items', { page: 1, size: 10 })
```

#### 6.2.3 clearAllCache()

**参数**：无

**返回值**：无

**示例**：

```javascript
requestManager.clearAllCache()
```

## 7. 完整使用示例

### 7.1 登录组件

```javascript
<template>
  <div class="login">
    <el-card class="login-card">
      <template #header>
        <div class="login-header">
          <h2>用户登录</h2>
          <p>欢迎使用学生闲置物品交易平台</p>
        </div>
      </template>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password></el-input>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="rememberMe">记住我</el-checkbox>
          <el-link type="primary" :underline="false" style="float: right;">忘记密码？</el-link>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin" style="width: 100%;">登录</el-button>
        </el-form-item>
        <div class="register-link">
          还没有账号？ <el-link type="primary" @click="$router.push('/register')">立即注册</el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userStore } from '../store'
import ErrorHandler from '../utils/errorHandler'

const router = useRouter()
const loginFormRef = ref()
const loading = ref(false)
const rememberMe = ref(false)
const store = userStore()

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
    loading.value = true
    
    const response = await store.login(loginForm.username, loginForm.password, rememberMe.value)
    ElMessage.success('登录成功')
    
    // 登录后重定向到之前的页面
    const redirectPath = localStorage.getItem('redirectPath')
    if (redirectPath) {
      localStorage.removeItem('redirectPath')
      router.push(redirectPath)
    } else {
      router.push('/')
    }
  } catch (error) {
    await ErrorHandler.handleLoginError(error)
  } finally {
    loading.value = false
  }
}
</script>
```

### 7.2 头部组件

```javascript
<template>
  <header class="header">
    <div class="header-top">
      <div class="container">
        <div class="header-top-content">
          <!-- Logo -->
          <div class="logo">
            <router-link to="/" class="logo-link">
              <div class="logo-icon">🎯</div>
              <h1 class="logo-text">闲置物品交易平台</h1>
            </router-link>
          </div>
          
          <!-- 搜索框 -->
          <div class="search-box">
            <div class="search-container">
              <el-input 
                v-model="searchKeyword" 
                placeholder="搜索闲置物品" 
                @keyup.enter="handleSearch"
                class="search-input"
              >
                <template #prefix>
                  <el-icon class="search-icon"><Search /></el-icon>
                </template>
                <template #append>
                  <el-button 
                    type="primary" 
                    @click="handleSearch"
                    class="search-button"
                  >
                    搜索
                  </el-button>
                </template>
              </el-input>
            </div>
          </div>
          
          <!-- 用户菜单 -->
          <div class="user-menu">
            <template v-if="store.isLoggedIn">
              <el-dropdown trigger="click" @visible-change="handleDropdownVisible">
                <div class="user-info">
                  <el-avatar 
                    :size="36" 
                    :src="(store.user && store.user.avatar) || undefined"
                    class="user-avatar"
                  >
                    {{ getAvatarText() }}
                  </el-avatar>
                  <span class="user-name">{{ getUserName() }}</span>
                  <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
                </div>
                <template #dropdown>
                  <el-dropdown-menu class="user-dropdown">
                    <el-dropdown-item class="dropdown-item">
                      <router-link to="/user/profile" class="dropdown-link">
                        <el-icon class="dropdown-item-icon"><User /></el-icon>
                        <span>个人中心</span>
                      </router-link>
                    </el-dropdown-item>
                    <el-dropdown-item class="dropdown-item">
                      <router-link to="/publish" class="dropdown-link">
                        <el-icon class="dropdown-item-icon"><Edit /></el-icon>
                        <span>发布闲置</span>
                      </router-link>
                    </el-dropdown-item>
                    <el-dropdown-item class="dropdown-item">
                      <router-link to="/orders" class="dropdown-link">
                        <el-icon class="dropdown-item-icon"><Document /></el-icon>
                        <span>我的订单</span>
                      </router-link>
                    </el-dropdown-item>
                    <el-dropdown-item v-if="store.isAdmin" class="dropdown-item">
                      <router-link to="/admin/users" class="dropdown-link">
                        <el-icon class="dropdown-item-icon"><Setting /></el-icon>
                        <span>管理后台</span>
                      </router-link>
                    </el-dropdown-item>
                    <el-dropdown-item divided class="dropdown-item" @click="handleLogout">
                      <el-icon class="dropdown-item-icon"><SwitchButton /></el-icon>
                      <span>退出登录</span>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
            <template v-else>
              <el-button type="primary" plain class="login-button">
                <router-link to="/login">登录</router-link>
              </el-button>
              <el-button class="register-button">
                <router-link to="/register">注册</router-link>
              </el-button>
            </template>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search, ArrowDown, User, Edit, Document, Setting, SwitchButton } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { userStore } from '../store'

const route = useRoute()
const router = useRouter()
const searchKeyword = ref('')
const dropdownVisible = ref(false)
const store = userStore()

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    store.logout()
    ElMessage.success('退出登录成功')
    router.push('/login')
  }).catch(() => {
    // 取消退出
  })
}

const getAvatarText = () => {
  if (store.user) {
    if (store.user.nickname && store.user.nickname.length > 0) {
      return store.user.nickname.charAt(0)
    } else if (store.user.username && store.user.username.length > 0) {
      return store.user.username.charAt(0)
    }
  }
  return '用'
}

const getUserName = () => {
  if (store.user) {
    if (store.user.nickname && store.user.nickname.length > 0) {
      return store.user.nickname
    } else if (store.user.username && store.user.username.length > 0) {
      return store.user.username
    }
  }
  return '用户'
}

onMounted(async () => {
  if (store.isLoggedIn) {
    try {
      await store.getCurrentUser()
    } catch (error) {
      console.error('获取用户信息失败', error)
    }
  }
})
</script>
```

## 8. 最佳实践

1. **使用store实例**：总是通过 `userStore()` 获取store实例，而不是直接使用导入的对象
2. **错误处理**：使用ErrorHandler工具类处理错误，确保错误处理的一致性
3. **存储选择**：根据需要选择合适的存储方案（localStorage、sessionStorage或Cookie）
4. **请求优化**：使用requestManager工具类优化HTTP请求，减少重复请求
5. **令牌管理**：定期检查令牌是否过期，及时刷新令牌
6. **状态同步**：监听storage事件，实现多标签页状态同步
7. **安全性**：确保存储的敏感数据经过加密处理
8. **性能优化**：使用缓存策略减少不必要的网络请求

## 9. 故障排除

### 9.1 登录状态不显示
- 检查网络请求是否成功
- 检查localStorage中是否存储了登录状态数据
- 检查store实例是否正确获取
- 检查路由守卫是否正确处理登录状态

### 9.2 令牌过期
- 检查令牌过期时间设置
- 验证令牌刷新机制是否正常工作
- 检查后端refreshToken API是否正常

### 9.3 多标签页状态不同步
- 检查storage事件监听器是否正确实现
- 验证不同标签页是否共享localStorage

### 9.4 存储数据丢失
- 检查浏览器是否支持localStorage
- 验证存储数据是否超过浏览器限制
- 检查存储数据是否被其他代码清除

## 10. 总结

本状态管理模块提供了完整的登录状态管理功能，包括前端存储机制、状态保持策略、后端身份验证、数据交互优化和错误处理机制。通过使用本模块，可以确保用户登录状态的安全、稳定和高效管理，提升用户体验。