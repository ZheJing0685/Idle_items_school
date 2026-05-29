import { describe, it, expect, vi, beforeEach } from 'vitest';

const { mockGet, mockPost } = vi.hoisted(() => ({
  mockGet: vi.fn(),
  mockPost: vi.fn(),
}));

vi.mock('@/api/config/http', () => ({
  get: (...args: any[]) => mockGet(...args),
  post: (...args: any[]) => mockPost(...args),
}));

vi.mock('@/utils/network/requestManager', () => ({
  default: {
    request: vi.fn((_url: string, requestFn: () => any) => requestFn()),
  },
}));

import categoryApi from '@/api/services/category';

describe('Category API', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('getCategories', () => {
    it('should call get with correct url', async () => {
      const mockResponse = { code: 200, data: [{ id: 1, name: '电子产品' }] };
      mockGet.mockResolvedValue(mockResponse);

      const result = await categoryApi.getCategories();

      expect(mockGet).toHaveBeenCalledWith('/categories');
      expect(result).toEqual(mockResponse);
    });

    it('should throw error when fetch fails', async () => {
      const error = new Error('获取分类失败');
      mockGet.mockRejectedValue(error);

      await expect(categoryApi.getCategories()).rejects.toThrow('获取分类失败');
    });
  });

  describe('getCategoryTree', () => {
    it('should call get with correct url', async () => {
      const mockResponse = {
        code: 200,
        data: [{ id: 1, name: '电子产品', children: [{ id: 2, name: '手机' }] }],
      };
      mockGet.mockResolvedValue(mockResponse);

      const result = await categoryApi.getCategoryTree();

      expect(mockGet).toHaveBeenCalledWith('/categories/tree');
      expect(result).toEqual(mockResponse);
    });

    it('should throw error when fetch fails', async () => {
      const error = new Error('获取分类树失败');
      mockGet.mockRejectedValue(error);

      await expect(categoryApi.getCategoryTree()).rejects.toThrow('获取分类树失败');
    });
  });

  describe('searchCategories', () => {
    it('should call get with correct url and params', async () => {
      const mockResponse = { code: 200, data: [{ id: 1, name: '电子产品' }] };
      mockGet.mockResolvedValue(mockResponse);

      const result = await categoryApi.searchCategories('电子');

      expect(mockGet).toHaveBeenCalledWith('/categories/search', { params: { keyword: '电子' } });
      expect(result).toEqual(mockResponse);
    });

    it('should handle empty search result', async () => {
      const mockResponse = { code: 200, data: [] };
      mockGet.mockResolvedValue(mockResponse);

      const result = await categoryApi.searchCategories('不存在的分类');

      expect(result.data).toEqual([]);
    });
  });

  describe('submitFeedback', () => {
    it('should call post with correct url and data', async () => {
      const mockResponse = { code: 200, data: null };
      mockPost.mockResolvedValue(mockResponse);

      const feedbackData = { categoryName: '新分类', reason: '需要添加新分类' };
      const result = await categoryApi.submitFeedback(feedbackData);

      expect(mockPost).toHaveBeenCalledWith('/categories/feedback', feedbackData);
      expect(result).toEqual(mockResponse);
    });

    it('should throw error when submit fails', async () => {
      const error = new Error('提交反馈失败');
      mockPost.mockRejectedValue(error);

      await expect(categoryApi.submitFeedback({ categoryName: '测试', reason: '测试' })).rejects.toThrow('提交反馈失败');
    });
  });

  describe('getMyFeedbacks', () => {
    it('should call get with correct url', async () => {
      const mockResponse = { code: 200, data: [{ id: 1, categoryName: '测试', status: 'PENDING' }] };
      mockGet.mockResolvedValue(mockResponse);

      const result = await categoryApi.getMyFeedbacks();

      expect(mockGet).toHaveBeenCalledWith('/categories/feedback/my', { params: undefined });
      expect(result).toEqual(mockResponse);
    });

    it('should throw error when fetch fails', async () => {
      const error = new Error('获取反馈失败');
      mockGet.mockRejectedValue(error);

      await expect(categoryApi.getMyFeedbacks()).rejects.toThrow('获取反馈失败');
    });
  });
});
