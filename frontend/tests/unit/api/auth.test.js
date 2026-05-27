import { describe, it, expect, vi, beforeEach } from 'vitest';

const mockPost = vi.fn();
const mockGet = vi.fn();

vi.mock('@/api/config/http', () => ({
  post: (...args) => mockPost(...args),
  get: (...args) => mockGet(...args),
}));

vi.mock('@/utils/network/requestManager', () => ({
  default: {
    request: vi.fn((_url, requestFn) => requestFn()),
  },
}));

import authApi from '@/api/services/auth';

describe('Auth API', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('login', () => {
    it('should call post with correct url and data', async () => {
      const mockResponse = { code: 200, data: { token: 'jwt-token' } };
      mockPost.mockResolvedValue(mockResponse);

      const result = await authApi.login({ username: 'test', password: '123' });

      expect(mockPost).toHaveBeenCalledWith('/auth/login', {
        username: 'test',
        password: '123',
      });
      expect(result).toEqual(mockResponse);
    });
  });

  describe('register', () => {
    it('should call post with correct url and userData', async () => {
      const mockResponse = { code: 200, data: '注册成功' };
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
  });

  describe('getCurrentUser', () => {
    it('should call get with correct url', async () => {
      const mockResponse = { code: 200, data: { id: 1, username: 'test' } };
      mockGet.mockResolvedValue(mockResponse);

      const result = await authApi.getCurrentUser();

      expect(mockGet).toHaveBeenCalledWith('/auth/me');
      expect(result).toEqual(mockResponse);
    });
  });

  describe('refreshToken', () => {
    it('should call post with correct url and refreshToken', async () => {
      const mockResponse = { code: 200, data: { token: 'new-token' } };
      mockPost.mockResolvedValue(mockResponse);

      const result = await authApi.refreshToken('old-refresh-token');

      expect(mockPost).toHaveBeenCalledWith('/auth/refresh', {
        refreshToken: 'old-refresh-token',
      });
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

      const result = await authApi.resetPassword(
        'test@example.com',
        '123456',
        'newPass@123',
      );

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

      await expect(
        authApi.login({ username: 'test', password: '123' }),
      ).rejects.toThrow('Network error');
    });
  });
});
