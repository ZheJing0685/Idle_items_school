import { describe, it, expect, vi, beforeEach } from 'vitest';

const { mockGet, mockPut } = vi.hoisted(() => ({
  mockGet: vi.fn(),
  mockPut: vi.fn(),
}));

vi.mock('@/api/config/http', () => ({
  get: (...args: any[]) => mockGet(...args),
  put: (...args: any[]) => mockPut(...args),
}));

vi.mock('@/utils/network/requestManager', () => ({
  default: {
    request: vi.fn((_url: string, requestFn: () => any) => requestFn()),
  },
}));

import userApi from '@/api/services/user';

describe('User API (TypeScript)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('getProfile', () => {
    it('should call get with correct url', async () => {
      const mockResponse = { code: 200, data: { id: 1, username: 'testuser' } };
      mockGet.mockResolvedValue(mockResponse);

      const result = await userApi.getProfile();

      expect(mockGet).toHaveBeenCalledWith('/user/profile');
      expect(result).toEqual(mockResponse);
    });

    it('should throw error when fetch fails', async () => {
      const error = new Error('获取用户信息失败');
      mockGet.mockRejectedValue(error);

      await expect(userApi.getProfile()).rejects.toThrow('获取用户信息失败');
    });
  });

  describe('updateProfile', () => {
    it('should call put with correct url and data', async () => {
      const updates = { nickname: '新昵称', phone: '13900139000' };
      const mockResponse = { code: 200, data: { id: 1, ...updates } };
      mockPut.mockResolvedValue(mockResponse);

      const result = await userApi.updateProfile(updates);

      expect(mockPut).toHaveBeenCalledWith('/user/profile', updates);
      expect(result).toEqual(mockResponse);
    });

    it('should throw error when update fails', async () => {
      const error = new Error('更新失败');
      mockPut.mockRejectedValue(error);

      await expect(userApi.updateProfile({})).rejects.toThrow('更新失败');
    });
  });

  describe('getStats', () => {
    it('should call get with correct url', async () => {
      const mockResponse = {
        code: 200,
        data: { totalItems: 10, totalSales: 5, totalPurchases: 3, rating: 4.5 },
      };
      mockGet.mockResolvedValue(mockResponse);

      const result = await userApi.getStats();

      expect(mockGet).toHaveBeenCalledWith('/user/stats');
      expect(result).toEqual(mockResponse);
    });
  });

  describe('getItems', () => {
    it('should call get with correct url and params', async () => {
      const mockResponse = { code: 200, data: [] };
      mockGet.mockResolvedValue(mockResponse);

      const result = await userApi.getItems('available', 1, 10);

      expect(mockGet).toHaveBeenCalledWith('/items/user', {
        params: { status: 'available', page: 1, size: 10 },
      });
      expect(result).toEqual(mockResponse);
    });
  });

  describe('error handling', () => {
    it('should propagate errors from get', async () => {
      const error = new Error('Network error');
      mockGet.mockRejectedValue(error);

      await expect(userApi.getProfile()).rejects.toThrow('Network error');
    });

    it('should propagate errors from put', async () => {
      const error = new Error('Server error');
      mockPut.mockRejectedValue(error);

      await expect(userApi.updateProfile({})).rejects.toThrow('Server error');
    });
  });
});
