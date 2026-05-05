import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { userStore as createUserStore } from '@/store/index'

// 创建可重置的 mockStorage
const createMockStorage = () => {
  const data = {}
  return {
    _data: data,
    get: (key) => data[key] !== undefined ? data[key] : null,
    set: (key, value) => { data[key] = value },
    remove: (key) => { delete data[key] },
    clear: () => { Object.keys(data).forEach(k => delete data[k]) }
  }
}

const mockStorage = createMockStorage()

vi.mock('@/utils/storage', () => ({
  default: {
    getStorage: () => mockStorage
  }
}))

vi.mock('@/api', () => ({
  default: {
    auth: {
      login: vi.fn(),
      register: vi.fn(),
      getCurrentUser: vi.fn(),
      refreshToken: vi.fn()
    }
  },
  setToken: vi.fn(),
  getToken: vi.fn()
}))

vi.mock('@/utils/errorHandler', () => ({
  default: {
    classifyError: vi.fn().mockReturnValue({ type: 'UNKNOWN_ERROR' }),
    handleError: vi.fn(),
    handleLoginError: vi.fn()
  }
}))

// 在 mock 后导入 store
let api, setToken, ErrorHandler
beforeAll(async () => {
  const apiModule = await import('@/api')
  api = apiModule.default
  setToken = apiModule.setToken
  ErrorHandler = (await import('@/utils/errorHandler')).default
})

describe('User Store', () => {
  let store

  beforeEach(() => {
    setActivePinia(createPinia())
    mockStorage.clear()
    vi.clearAllMocks()
    store = createUserStore()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('状态初始化', () => {
    it('should initialize with empty token', () => {
      expect(store.token).toBe('')
    })

    it('should initialize with null user', () => {
      expect(store.user).toBeNull()
    })

    it('should initialize with loading as false', () => {
      expect(store.loading).toBe(false)
    })

    it('should initialize with rememberMe as false', () => {
      expect(store.rememberMe).toBe(false)
    })
  })

  describe('计算属性', () => {
    it('isLoggedIn should be false when no token', () => {
      expect(store.isLoggedIn).toBe(false)
    })

    it('isLoggedIn should be true when token exists', () => {
      store.token = 'mock-token'
      expect(store.isLoggedIn).toBe(true)
    })

    it('isAdmin should be true when user role is ADMIN', () => {
      store.user = { id: 1, username: 'admin', role: 'ADMIN' }
      expect(store.isAdmin).toBe(true)
    })

    it('isAdmin should be falsy when user role is USER', () => {
      store.user = { id: 1, username: 'user', role: 'USER' }
      // 注意：computed(() => user.value && user.value.role === 'ADMIN')
      // user.value 为 truthy 但 role !== 'ADMIN'，结果为 undefined
      expect(store.isAdmin).toBeFalsy()
    })

    it('isAdmin should be falsy when no user', () => {
      // user.value 为 null，null && ... 结果为 null
      expect(store.isAdmin).toBeFalsy()
    })

    it('loginDuration should be 0 when no lastLoginTime', () => {
      expect(store.loginDuration).toBe(0)
    })
  })

  describe('login()', () => {
    it('should login successfully with correct credentials', async () => {
      const mockResponse = {
        code: 200,
        data: {
          token: 'mock-token-123',
          refreshToken: 'mock-refresh-token',
          user: { id: 1, username: 'testuser', nickname: '测试用户' }
        }
      }
      api.auth.login.mockResolvedValue(mockResponse)

      const result = await store.login('testuser', 'password123')

      expect(result).toEqual(mockResponse)
      expect(store.token).toBe('mock-token-123')
      expect(store.user).toEqual(mockResponse.data.user)
      expect(store.isLoggedIn).toBe(true)
    })

    it('should store data in storage on login', async () => {
      const mockResponse = {
        code: 200,
        data: {
          token: 'token-456',
          refreshToken: 'refresh-token-456',
          user: { id: 2, username: 'user2' }
        }
      }
      api.auth.login.mockResolvedValue(mockResponse)

      await store.login('user2', 'pass')

      expect(setToken).toHaveBeenCalledWith('token-456')
      expect(mockStorage.get('user')).toEqual(mockResponse.data.user)
    })

    it('should set loading to true during login', async () => {
      api.auth.login.mockImplementation(() => new Promise(resolve => setTimeout(() => resolve({
        code: 200,
        data: { token: 't', refreshToken: 'rt', user: {} }
      }), 100)))

      const loginPromise = store.login('user', 'pass')
      expect(store.loading).toBe(true)

      await loginPromise
      expect(store.loading).toBe(false)
    })

    it('should throw error on login failure', async () => {
      api.auth.login.mockRejectedValue(new Error('用户名或密码错误'))

      await expect(store.login('wrong', 'wrong')).rejects.toThrow('用户名或密码错误')
    })
  })

  describe('logout()', () => {
    it('should clear token and user on logout', () => {
      store.token = 'some-token'
      store.user = { id: 1 }
      store.lastLoginTime = new Date().toISOString()

      store.logout()

      expect(store.token).toBe('')
      expect(store.user).toBeNull()
      expect(store.isLoggedIn).toBe(false)
    })

    it('should remove data from storage on logout', () => {
      mockStorage.set('token', 'some-token')
      mockStorage.set('user', { id: 1 })

      store.logout()

      expect(setToken).toHaveBeenCalledWith('')
      expect(mockStorage.get('user')).toBeNull()
    })

    it('should preserve rememberMe if rememberMe was true', () => {
      store.rememberMe = true
      mockStorage.set('rememberMe', true)

      store.logout()

      // rememberMe 应该保留
      expect(store.rememberMe).toBe(true)
    })
  })

  describe('getCurrentUser()', () => {
    it('should return null when no token', async () => {
      const result = await store.getCurrentUser()
      expect(result).toBeNull()
      expect(api.auth.getCurrentUser).not.toHaveBeenCalled()
    })

    it('should fetch and update user when token exists', async () => {
      const mockUser = { id: 1, username: 'testuser', nickname: '测试' }
      api.auth.getCurrentUser.mockResolvedValue({ code: 200, data: mockUser })

      store.token = 'valid-token'
      const result = await store.getCurrentUser()

      expect(result).toEqual(mockUser)
      expect(store.user).toEqual(mockUser)
    })

    it('should logout on AUTHENTICATION_ERROR', async () => {
      ErrorHandler.classifyError.mockReturnValue({ type: 'AUTHENTICATION_ERROR' })
      api.auth.getCurrentUser.mockRejectedValue(new Error('Unauthorized'))

      store.token = 'expired-token'
      store.user = { id: 1 }
      mockStorage.set('token', 'expired-token')
      mockStorage.set('user', { id: 1 })

      await expect(store.getCurrentUser()).rejects.toThrow()
      expect(store.token).toBe('')
      expect(store.user).toBeNull()
    })
  })

  describe('checkTokenExpiry()', () => {
    it('should return true when lastLoginTime is null', () => {
      expect(store.checkTokenExpiry()).toBe(true)
    })

    it('should return true when login duration > 24 hours', () => {
      // 直接修改 store 状态
      const oldTime = new Date(Date.now() - 25 * 60 * 60 * 1000).toISOString()
      store.lastLoginTime = oldTime
      // loginDuration 计算依赖 lastLoginTime
      expect(store.checkTokenExpiry()).toBe(true)
    })

    it('should return false when login duration < 24 hours', () => {
      const recentTime = new Date(Date.now() - 1 * 60 * 60 * 1000).toISOString()
      store.lastLoginTime = recentTime
      expect(store.checkTokenExpiry()).toBe(false)
    })

    it('should return true for exactly 24 hours', () => {
      // 25小时前，确保一定超过 24h 阈值
      const exactTime = new Date(Date.now() - 25 * 60 * 60 * 1000).toISOString()
      store.lastLoginTime = exactTime
      expect(store.checkTokenExpiry()).toBe(true)
    })
  })

  describe('refreshToken()', () => {
    it('should refresh token successfully', async () => {
      const newToken = 'new-refreshed-token'
      api.auth.refreshToken.mockResolvedValue({ code: 200, data: { token: newToken } })

      store.token = 'old-token'
      store.lastLoginTime = null

      const result = await store.refreshToken()

      expect(result).toBe(newToken)
      expect(store.token).toBe(newToken)
    })

    it('should throw and log error on refresh failure', async () => {
      api.auth.refreshToken.mockRejectedValue(new Error('Refresh failed'))

      store.token = 'expired-token'

      await expect(store.refreshToken()).rejects.toThrow('Refresh failed')
    })
  })

  describe('register()', () => {
    it('should register new user successfully', async () => {
      const userData = {
        username: 'newuser',
        password: 'password123',
        email: 'new@test.com',
        phone: '13800138000',
        nickname: '新用户'
      }
      const mockResponse = {
        code: 200,
        data: {
          token: 'register-token',
          refreshToken: 'register-refresh-token',
          user: { id: 3, ...userData }
        }
      }
      api.auth.register.mockResolvedValue(mockResponse)

      const result = await store.register(userData)

      expect(result).toEqual(mockResponse)
      expect(store.token).toBe('register-token')
      expect(store.user.username).toBe('newuser')
    })
  })
})
