import { describe, it, expect, vi, beforeEach } from 'vitest';

// Mock element-plus
vi.mock('element-plus', () => ({
  ElMessage: vi.fn(),
  ElMessageBox: {
    alert: vi.fn(),
  },
}));

// Mock router
vi.mock('@/router', () => ({
  default: {
    push: vi.fn(),
  },
}));

describe('ErrorHandler', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('ErrorHandler class', () => {
    it('should export ErrorHandler', async () => {
      const { ErrorHandler } = await import('@/utils/error');
      expect(ErrorHandler).toBeDefined();
    });

    it('should have handle method', async () => {
      const { ErrorHandler } = await import('@/utils/error');
      expect(typeof ErrorHandler.handle).toBe('function');
    });

    it('should have showMessage method', async () => {
      const { ErrorHandler } = await import('@/utils/error');
      expect(typeof ErrorHandler.showMessage).toBe('function');
    });

    it('should have getErrorMessage method', async () => {
      const { ErrorHandler } = await import('@/utils/error');
      expect(typeof ErrorHandler.getErrorMessage).toBe('function');
    });

    it('should have isAuthError method', async () => {
      const { ErrorHandler } = await import('@/utils/error');
      expect(typeof ErrorHandler.isAuthError).toBe('function');
    });

    it('should have isNetworkError method', async () => {
      const { ErrorHandler } = await import('@/utils/error');
      expect(typeof ErrorHandler.isNetworkError).toBe('function');
    });
  });

  describe('handle method', () => {
    it('should return undefined for null error', async () => {
      const { ErrorHandler } = await import('@/utils/error');
      const result = ErrorHandler.handle(null);
      expect(result).toBeUndefined();
    });

    it('should return error object', async () => {
      const { ErrorHandler } = await import('@/utils/error');
      const error = new Error('test error');
      const result = ErrorHandler.handle(error);
      expect(result).toBe(error);
    });

    it('should not show message in silent mode', async () => {
      const { ElMessage } = await import('element-plus');
      const { ErrorHandler } = await import('@/utils/error');

      ErrorHandler.handle(new Error('test'), { silent: true });

      expect(ElMessage).not.toHaveBeenCalled();
    });
  });

  describe('getErrorMessage method', () => {
    it('should return network error message', async () => {
      const { ErrorHandler } = await import('@/utils/error');
      const { errorTypes } = await import('@/utils/error');

      const message = ErrorHandler.getErrorMessage(errorTypes.ErrorType.NETWORK_ERROR);
      expect(message).toContain('网络');
    });

    it('should return timeout error message', async () => {
      const { ErrorHandler } = await import('@/utils/error');
      const { errorTypes } = await import('@/utils/error');

      const message = ErrorHandler.getErrorMessage(errorTypes.ErrorType.TIMEOUT_ERROR);
      expect(message).toContain('超时');
    });

    it('should return auth error message', async () => {
      const { ErrorHandler } = await import('@/utils/error');
      const { errorTypes } = await import('@/utils/error');

      const message = ErrorHandler.getErrorMessage(errorTypes.ErrorType.AUTHENTICATION_ERROR);
      expect(message).toContain('登录');
    });

    it('should return server error message', async () => {
      const { ErrorHandler } = await import('@/utils/error');
      const { errorTypes } = await import('@/utils/error');

      const message = ErrorHandler.getErrorMessage(errorTypes.ErrorType.SERVER_ERROR);
      expect(message).toContain('服务器');
    });
  });

  describe('error type checking', () => {
    it('should have isAuthError method', async () => {
      const { ErrorHandler } = await import('@/utils/error');
      expect(typeof ErrorHandler.isAuthError).toBe('function');
    });

    it('should have isNetworkError method', async () => {
      const { ErrorHandler } = await import('@/utils/error');
      expect(typeof ErrorHandler.isNetworkError).toBe('function');
    });

    it('should return boolean for isAuthError', async () => {
      const { ErrorHandler } = await import('@/utils/error');
      const result = ErrorHandler.isAuthError({ response: { status: 401 } });
      expect(typeof result).toBe('boolean');
    });

    it('should return boolean for isNetworkError', async () => {
      const { ErrorHandler } = await import('@/utils/error');
      const result = ErrorHandler.isNetworkError({ message: 'Network Error' });
      expect(typeof result).toBe('boolean');
    });
  });

  describe('clearAuthStorage', () => {
    it('should have clearAuthStorage method', async () => {
      const { ErrorHandler } = await import('@/utils/error');
      expect(typeof ErrorHandler.clearAuthStorage).toBe('function');
    });
  });
});
