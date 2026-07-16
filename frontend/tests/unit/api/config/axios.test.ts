import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';

// Mock依赖
vi.mock('axios', () => {
  const interceptors = {
    request: { use: vi.fn() },
    response: { use: vi.fn() },
  };

  const instance = {
    interceptors,
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
    patch: vi.fn(),
    head: vi.fn(),
    options: vi.fn(),
  };

  return {
    default: {
      create: vi.fn(() => instance),
      defaults: {
        headers: {
          common: {},
        },
      },
    },
  };
});

vi.mock('@/router', () => ({
  default: {
    push: vi.fn(),
  },
}));

vi.mock('@/utils/error', () => ({
  ErrorHandler: {
    showErrorMessage: vi.fn(),
    handle: vi.fn(),
    showMessage: vi.fn(),
    getErrorMessage: vi.fn(),
    isAuthError: vi.fn(),
    isNetworkError: vi.fn(),
    clearAuthStorage: vi.fn(),
  },
}));

vi.mock('element-plus', () => ({
  ElMessageBox: {
    alert: vi.fn(),
  },
}));

describe('Axios配置', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    // 清除sessionStorage
    sessionStorage.clear();
  });

  afterEach(() => {
    sessionStorage.clear();
  });

  it('应该创建axios实例', async () => {
    const axiosModule = await import('@/api/config/axios');
    const instance = axiosModule.default;

    expect(instance).toBeDefined();
    expect(instance.interceptors).toBeDefined();
    expect(instance.interceptors.request.use).toHaveBeenCalled();
    expect(instance.interceptors.response.use).toHaveBeenCalled();
  });

  it('应该正确设置请求拦截器', async () => {
    const axiosModule = await import('@/api/config/axios');
    const instance = axiosModule.default;

    expect(instance).toBeDefined();
    expect(instance.interceptors).toBeDefined();
    expect(instance.interceptors.request).toBeDefined();
    expect(instance.interceptors.request.use).toBeDefined();
    expect(instance.interceptors.response).toBeDefined();
    expect(instance.interceptors.response.use).toBeDefined();
  });

  describe('未授权处理器', () => {
    it('应该设置未授权处理器', async () => {
      const { setUnauthorizedHandler } = await import('@/api/config/axios');
      const handler = vi.fn();

      // 验证函数可以被调用而不抛出错误
      expect(() => setUnauthorizedHandler(handler)).not.toThrow();
    });

    it('应该可以重新设置处理器', async () => {
      const { setUnauthorizedHandler } = await import('@/api/config/axios');
      const handler1 = vi.fn();
      const handler2 = vi.fn();

      setUnauthorizedHandler(handler1);
      setUnauthorizedHandler(handler2);

      // 验证函数可以被多次调用而不抛出错误
      expect(() => setUnauthorizedHandler(handler1)).not.toThrow();
      expect(() => setUnauthorizedHandler(handler2)).not.toThrow();
    });
  });

  describe('模块导出', () => {
    it('应该导出所有必要函数', async () => {
      const axiosModule = await import('@/api/config/axios');

      expect(typeof axiosModule.setUnauthorizedHandler).toBe('function');
      expect(typeof axiosModule.clearAuthState).toBe('function');
      expect(axiosModule.default).toBeDefined();
    });

    it('应该导出axios实例作为默认导出', async () => {
      const axiosModule = await import('@/api/config/axios');

      expect(axiosModule.default).toBeDefined();
      expect(axiosModule.default.interceptors).toBeDefined();
      expect(axiosModule.default.interceptors.request).toBeDefined();
      expect(axiosModule.default.interceptors.response).toBeDefined();
    });
  });
});