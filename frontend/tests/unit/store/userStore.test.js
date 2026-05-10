import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { userStore as createUserStore } from '@/store/index'

const mockStorage = vi.hoisted(() => {
  const data = {}
  return {
    _data: data,
    get: (key) => data[key] !== undefined ? data[key] : null,
    set: (key, value) => { data[key] = value },
    remove: (key) => { delete data[key] },
    clear: () => { Object.keys(data).forEach(k => delete data[k]) }
  }
})

const mockSetToken = vi.hoisted(() => vi.fn())
const mockClearToken = vi.hoisted(() => vi.fn())
const mockGetToken = vi.hoisted(() => vi.fn().mockReturnValue(''))

vi.mock('@/utils/storage', () => ({
  default: () => mockStorage
}))

vi.mock('@/api/config/axios', () => ({
  setToken: (...args) => mockSetToken(...args),
  clearToken: (...args) => mockClearToken(...args),
  getToken: (...args) => mockGetToken(...args),
}))

vi.mock('@/api', () => ({
  default: {
    auth: {
      login: vi.fn(),
      register: vi.fn(),
      getCurrentUser: vi.fn(),
    },
    user: {
      updateProfile: vi.fn(),
    }
  },
}))

const ErrorHandlerMock = vi.hoisted(() => ({
  handle: vi.fn(),
}))

vi.mock('@/utils/error', () => ({
  ErrorHandler: ErrorHandlerMock
}))

let api
beforeAll(async () => {
  const apiModule = await import('@/api')
  api = apiModule.default
})

describe('User Store', () => {
  let store

  beforeEach(() => {
    setActivePinia(createPinia())
    mockStorage.clear()
    vi.clearAllMocks()
    mockGetToken.mockReturnValue('')
    store = createUserStore()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('状态初始化', () => {
    it('should initialize with empty refreshToken', () => {
      expect(store.refreshToken).toBe('')
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

    it('should initialize isLoggedIn from getToken', () => {
      expect(store.isLoggedIn).toBe(false)
    })
  })

  describe('计算属性', () => {
    it('isLoggedIn should be false when no token', () => {
      expect(store.isLoggedIn).toBe(false)
    })

    it('isLoggedIn should be true after login', async () => {
      api.auth.login.mockResolvedValue({
        code: 200,
        data: { token: 't', refreshToken: 'rt', user: { id: 1 } }
      })
      await store.login('u', 'p')
      expect(store.isLoggedIn).toBe(true)
    })

    it('isAdmin should be true when user role is ADMIN', () => {
      store.user = { id: 1, username: 'admin', role: 'ADMIN' }
      expect(store.isAdmin).toBe(true)
    })

    it('isAdmin should be falsy when user role is USER', () => {
      store.user = { id: 1, username: 'user', role: 'USER' }
      expect(store.isAdmin).toBeFalsy()
    })

    it('isAdmin should be falsy when no user', () => {
      expect(store.isAdmin).toBeFalsy()
    })

    it('isVerified should be true when user is verified', () => {
      store.user = { id: 1, verified: true }
      expect(store.isVerified).toBe(true)
    })

    it('isVerified should be false when user is not verified', () => {
      store.user = { id: 1, verified: false }
      expect(store.isVerified).toBe(false)
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
      expect(mockSetToken).toHaveBeenCalledWith('mock-token-123')
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

      expect(mockSetToken).toHaveBeenCalledWith('token-456')
      expect(mockStorage.get('user')).toEqual(mockResponse.data.user)
      expect(mockStorage.get('refreshToken')).toBe('refresh-token-456')
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
      expect(ErrorHandlerMock.handle).toHaveBeenCalled()
    })

    it('should pass remember flag to storage', async () => {
      api.auth.login.mockResolvedValue({
        code: 200,
        data: { token: 't', refreshToken: 'rt', user: { id: 1 } }
      })

      await store.login('u', 'p', true)
      expect(store.rememberMe).toBe(true)
      expect(mockStorage.get('rememberMe')).toBe(true)
    })
  })

  describe('logout()', () => {
    it('should clear state on logout', () => {
      store.isLoggedIn = true
      store.user = { id: 1 }
      store.lastLoginTime = new Date().toISOString()

      store.logout()

      expect(mockClearToken).toHaveBeenCalled()
      expect(store.user).toBeNull()
      expect(store.isLoggedIn).toBe(false)
      expect(store.lastLoginTime).toBeNull()
    })

    it('should remove data from storage on logout', () => {
      mockStorage.set('refreshToken', 'some-token')
      mockStorage.set('user', { id: 1 })
      mockStorage.set('lastLoginTime', '2026-01-01')

      store.logout()

      expect(mockStorage.get('refreshToken')).toBeNull()
      expect(mockStorage.get('user')).toBeNull()
      expect(mockStorage.get('lastLoginTime')).toBeNull()
    })

    it('should preserve rememberMe if rememberMe was true', () => {
      store.rememberMe = true
      mockStorage.set('rememberMe', true)

      store.logout()

      expect(store.rememberMe).toBe(true)
      expect(mockStorage.get('rememberMe')).toBe(true)
    })

    it('should remove rememberMe if rememberMe was false', () => {
      store.rememberMe = false
      mockStorage.set('rememberMe', false)

      store.logout()

      expect(mockStorage.get('rememberMe')).toBeNull()
    })
  })

  describe('getCurrentUser()', () => {
    it('should return null when no token', async () => {
      mockGetToken.mockReturnValue('')
      const result = await store.getCurrentUser()
      expect(result).toBeNull()
      expect(api.auth.getCurrentUser).not.toHaveBeenCalled()
    })

    it('should fetch and update user when token exists', async () => {
      const mockUser = { id: 1, username: 'testuser', nickname: '测试' }
      mockGetToken.mockReturnValue('valid-token')
      api.auth.getCurrentUser.mockResolvedValue({ code: 200, data: mockUser })

      const result = await store.getCurrentUser()

      expect(result).toEqual(mockUser)
      expect(store.user).toEqual(mockUser)
      expect(mockStorage.get('user')).toEqual(mockUser)
    })

    it('should return null on error', async () => {
      mockGetToken.mockReturnValue('valid-token')
      api.auth.getCurrentUser.mockRejectedValue(new Error('Network error'))

      const result = await store.getCurrentUser()
      expect(result).toBeNull()
    })

    it('should logout on 401 error', async () => {
      mockGetToken.mockReturnValue('valid-token')
      store.user = { id: 1 }
      const error = new Error('Unauthorized')
      error.code = 401
      api.auth.getCurrentUser.mockRejectedValue(error)

      await store.getCurrentUser()
      expect(store.user).toBeNull()
      expect(store.isLoggedIn).toBe(false)
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
        data: { message: '注册成功' }
      }
      api.auth.register.mockResolvedValue(mockResponse)

      const result = await store.register(userData)

      expect(result).toEqual(mockResponse)
      expect(store.loading).toBe(false)
    })

    it('should throw error on registration failure', async () => {
      api.auth.register.mockRejectedValue(new Error('注册失败'))

      await expect(store.register({ username: 'test' })).rejects.toThrow('注册失败')
      expect(ErrorHandlerMock.handle).toHaveBeenCalled()
    })
  })

  describe('updateProfile()', () => {
    it('should update profile successfully', async () => {
      store.user = { id: 1, username: 'test' }
      const mockResponse = {
        code: 200,
        data: { nickname: '新昵称' }
      }
      api.user.updateProfile.mockResolvedValue(mockResponse)

      const result = await store.updateProfile({ nickname: '新昵称' })

      expect(result).toEqual(mockResponse)
      expect(store.user.nickname).toBe('新昵称')
      expect(mockStorage.get('user')).toEqual(store.user)
    })

    it('should throw error on update failure', async () => {
      api.user.updateProfile.mockRejectedValue(new Error('更新失败'))

      await expect(store.updateProfile({})).rejects.toThrow('更新失败')
      expect(ErrorHandlerMock.handle).toHaveBeenCalled()
    })
  })

  describe('setRefreshToken()', () => {
    it('should update refreshToken in state and storage', () => {
      store.setRefreshToken('new-refresh-token')
      expect(store.refreshToken).toBe('new-refresh-token')
      expect(mockStorage.get('refreshToken')).toBe('new-refresh-token')
    })
  })
})
