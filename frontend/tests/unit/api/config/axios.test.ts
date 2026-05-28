import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'

// Mock依赖
vi.mock('axios', () => {
  const interceptors = {
    request: { use: vi.fn() },
    response: { use: vi.fn() }
  }
  
  return {
    default: {
      create: vi.fn(() => ({
        interceptors,
        get: vi.fn(),
        post: vi.fn(),
        put: vi.fn(),
        delete: vi.fn(),
        patch: vi.fn(),
        head: vi.fn(),
        options: vi.fn()
      })),
      defaults: {
        headers: {
          common: {}
        }
      }
    }
  }
})

vi.mock('@/router', () => ({
  default: {
    push: vi.fn()
  }
}))

vi.mock('@/utils/error', () => ({
  ErrorHandler: {
    showErrorMessage: vi.fn(),
    handle: vi.fn(),
    showMessage: vi.fn(),
    getErrorMessage: vi.fn(),
    isAuthError: vi.fn(),
    isNetworkError: vi.fn(),
    clearAuthStorage: vi.fn()
  }
}))

vi.mock('element-plus', () => ({
  ElMessageBox: {
    alert: vi.fn()
  }
}))

describe('Axios配置', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // 清除sessionStorage
    sessionStorage.clear()
  })

  afterEach(() => {
    sessionStorage.clear()
  })

  it('应该创建axios实例', async () => {
    const { default: axios } = await import('axios')
    const instance = axios.create()
    expect(axios.create).toHaveBeenCalled()
    // 验证实例被创建
    expect(instance).toBeDefined()
  })

  describe('Token管理', () => {
    it('应该正确设置和获取token', async () => {
      const { setToken, getToken } = await import('@/api/config/axios')
      
      setToken('test-token-123')
      expect(getToken()).toBe('test-token-123')
      expect(sessionStorage.getItem('access_token')).toBe('test-token-123')
    })

    it('应该清除token', async () => {
      const { setToken, clearToken, getToken } = await import('@/api/config/axios')
      
      setToken('test-token-123')
      clearToken()
      
      expect(getToken()).toBe('')
      expect(sessionStorage.getItem('access_token')).toBeNull()
      expect(sessionStorage.getItem('refresh_token')).toBeNull()
    })

    it('应该从sessionStorage恢复token', async () => {
      // 先设置sessionStorage，然后重新导入模块
      sessionStorage.setItem('access_token', 'stored-token')
      
      // 重置模块缓存以重新执行initToken()
      vi.resetModules()
      
      const { getToken } = await import('@/api/config/axios')
      expect(getToken()).toBe('stored-token')
    })

    it('应该多次设置token', async () => {
      const { setToken, getToken } = await import('@/api/config/axios')
      
      setToken('token1')
      expect(getToken()).toBe('token1')
      
      setToken('token2')
      expect(getToken()).toBe('token2')
      
      setToken('token3')
      expect(getToken()).toBe('token3')
    })
  })

  describe('未授权处理器', () => {
    it('应该设置未授权处理器', async () => {
      const { setUnauthorizedHandler } = await import('@/api/config/axios')
      const handler = vi.fn()
      
      // 验证函数可以被调用而不抛出错误
      expect(() => setUnauthorizedHandler(handler)).not.toThrow()
    })

    it('应该可以设置null处理器', async () => {
      const { setUnauthorizedHandler } = await import('@/api/config/axios')
      
      expect(() => setUnauthorizedHandler(null)).not.toThrow()
    })
  })

  describe('模块导出', () => {
    it('应该导出所有必要函数', async () => {
      const axiosModule = await import('@/api/config/axios')
      
      expect(typeof axiosModule.setToken).toBe('function')
      expect(typeof axiosModule.getToken).toBe('function')
      expect(typeof axiosModule.clearToken).toBe('function')
      expect(typeof axiosModule.setUnauthorizedHandler).toBe('function')
      expect(axiosModule.default).toBeDefined()
    })

    it('应该导出axios实例作为默认导出', async () => {
      const axiosModule = await import('@/api/config/axios')
      
      expect(axiosModule.default).toBeDefined()
      expect(axiosModule.default.interceptors).toBeDefined()
      expect(axiosModule.default.interceptors.request).toBeDefined()
      expect(axiosModule.default.interceptors.response).toBeDefined()
    })
  })
})