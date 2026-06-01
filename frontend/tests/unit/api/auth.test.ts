import { describe, it, expect, vi, beforeEach } from 'vitest';

const { mockPost, mockGet } = vi.hoisted(() => ({
  mockPost: vi.fn(),
  mockGet: vi.fn(),
}));

vi.mock('@/api/config/http', () => ({
  post: (...args: any[]) => mockPost(...args),
  get: (...args: any[]) => mockGet(...args),
}));

vi.mock('@/utils/network/requestManager', () => ({
  default: {
    request: vi.fn((_url: string, requestFn: () => any) => requestFn()),
  },
}));

import authApi from '@/api/services/auth';

describe('Auth API (TypeScript)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('login', () => {
    it('should call post with correct url and data', async () => {
      const mockResponse = { code: 200, data: { token: 'jwt-token' } };
      mockPost.mockResolvedValue(mockResponse);

      const result = await authApi.login({ username: 'testuser', password: 'Password@123' });

      expect(mockPost).toHaveBeenCalledWith('/auth/login', {
        username: 'testuser',
        password: 'Password@123',
      });
      expect(result).toEqual(mockResponse);
    });

    it('should throw error when login fails', async () => {
      const error = new Error('登录失败');
      mockPost.mockRejectedValue(error);

      await expect(authApi.login({ username: 'test', password: '123' })).rejects.toThrow('登录失败');
    });
  });

  describe('register', () => {
    it('should call post with correct url and userData', async () => {
      const mockResponse = { code: 200, data: { token: 'jwt-token' } };
      mockPost.mockResolvedValue(mockResponse);

      const userData = {
        username: 'newuser',
        password: 'Password@123',
        email: 'test@example.com',
        phone: '13800138000',
      };
      const result = await authApi.register(userData);

      expect(mockPost).toHaveBeenCalledWith('/auth/register', userData);
      expect(result).toEqual(mockResponse);
    });

    it('should throw error when username exists', async () => {
      const error = new Error('用户名已存在');
      mockPost.mockRejectedValue(error);

      await expect(authApi.register({ username: 'existing', password: '123', email: 'test@test.com', phone: '13800138000' })).rejects.toThrow('用户名已存在');
    });
  });

  describe('getCurrentUser', () => {
    it('should call get with correct url', async () => {
      const mockResponse = { code: 200, data: { id: 1, username: 'testuser' } };
      mockGet.mockResolvedValue(mockResponse);

      const result = await authApi.getCurrentUser();

      expect(mockGet).toHaveBeenCalledWith('/auth/me');
      expect(result).toEqual(mockResponse);
    });

    it('should throw error when unauthorized', async () => {
      const error = new Error('未授权');
      mockGet.mockRejectedValue(error);

      await expect(authApi.getCurrentUser()).rejects.toThrow('未授权');
    });
  });

  describe('refreshToken', () => {
    it('should call post with correct url and no body (refresh token from HttpOnly cookie)', async () => {
      const mockResponse = { code: 200, data: { token: 'new-token' } };
      mockPost.mockResolvedValue(mockResponse);

      const result = await authApi.refreshToken();

      expect(mockPost).toHaveBeenCalledWith('/auth/refresh');
      expect(result).toEqual(mockResponse);
    });
  });

  describe('forgotPassword', () => {
    it('should call post with correct url and email', async () => {
      const mockResponse = { code: 200, data: null };
      mockPost.mockResolvedValue(mockResponse);

      const result = await authApi.forgotPassword('test@example.com');

      expect(mockPost).toHaveBeenCalledWith('/auth/forgot-password', {
        email: 'test@example.com',
      });
      expect(result).toEqual(mockResponse);
    });
  });

  describe('verifyCode', () => {
    it('should call post with correct url, email and code', async () => {
      const mockResponse = { code: 200, data: null };
      mockPost.mockResolvedValue(mockResponse);

      const result = await authApi.verifyCode('test@example.com', '123456');

      expect(mockPost).toHaveBeenCalledWith('/auth/verify-code', {
        email: 'test@example.com',
        code: '123456',
      });
      expect(result).toEqual(mockResponse);
    });
  });

  describe('resetPassword', () => {
    it('should call post with correct url and params', async () => {
      const mockResponse = { code: 200, data: null };
      mockPost.mockResolvedValue(mockResponse);

      const result = await authApi.resetPassword('test@example.com', '123456', 'newPass@123');

      expect(mockPost).toHaveBeenCalledWith('/auth/reset-password', {
        email: 'test@example.com',
        code: '123456',
        newPassword: 'newPass@123',
      });
      expect(result).toEqual(mockResponse);
    });
  });

  describe('changePassword', () => {
    it('should call post with correct url and passwords', async () => {
      const mockResponse = { code: 200, data: null };
      mockPost.mockResolvedValue(mockResponse);

      const result = await authApi.changePassword('oldPass@123', 'newPass@456');

      expect(mockPost).toHaveBeenCalledWith('/auth/change-password', {
        oldPassword: 'oldPass@123',
        newPassword: 'newPass@456',
      });
      expect(result).toEqual(mockResponse);
    });
  });

  describe('logout', () => {
    it('should call post with correct url', async () => {
      const mockResponse = { code: 200, data: null };
      mockPost.mockResolvedValue(mockResponse);

      const result = await authApi.logout();

      expect(mockPost).toHaveBeenCalledWith('/auth/logout');
      expect(result).toEqual(mockResponse);
    });
  });

  describe('error handling', () => {
    it('should propagate errors from post', async () => {
      const error = new Error('Network error');
      mockPost.mockRejectedValue(error);

      await expect(authApi.login({ username: 'test', password: '123' })).rejects.toThrow('Network error');
    });
  });
});
