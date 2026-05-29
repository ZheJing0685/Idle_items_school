import { describe, it, expect, vi, beforeEach } from 'vitest';

// Mock axios实例
const mockGet = vi.fn();
const mockPost = vi.fn();
const mockPut = vi.fn();
const mockDelete = vi.fn();

vi.mock('@/api/config/axios', () => ({
  default: {
    get: mockGet,
    post: mockPost,
    put: mockPut,
    delete: mockDelete,
    interceptors: {
      request: { use: vi.fn() },
      response: { use: vi.fn() },
    },
  },
}));

describe('HTTP封装层', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('get函数', () => {
    it('应该调用axios.get方法', async () => {
      const { get } = await import('@/api/config/http');
      const mockResponse = { code: 200, data: { id: 1 } };
      mockGet.mockResolvedValue(mockResponse);

      const result = await get('/api/test');

      expect(mockGet).toHaveBeenCalledWith('/api/test', undefined);
      expect(result).toEqual(mockResponse);
    });

    it('应该传递config参数', async () => {
      const { get } = await import('@/api/config/http');
      const mockResponse = { code: 200, data: {} };
      mockGet.mockResolvedValue(mockResponse);
      const config = { params: { page: 1 } };

      await get('/api/test', config);

      expect(mockGet).toHaveBeenCalledWith('/api/test', config);
    });
  });

  describe('post函数', () => {
    it('应该调用axios.post方法', async () => {
      const { post } = await import('@/api/config/http');
      const mockResponse = { code: 200, data: { id: 1 } };
      mockPost.mockResolvedValue(mockResponse);

      const result = await post('/api/test', { name: 'test' });

      expect(mockPost).toHaveBeenCalledWith('/api/test', { name: 'test' }, undefined);
      expect(result).toEqual(mockResponse);
    });

    it('应该传递config参数', async () => {
      const { post } = await import('@/api/config/http');
      const mockResponse = { code: 200, data: {} };
      mockPost.mockResolvedValue(mockResponse);
      const data = { name: 'test' };
      const config = { headers: { 'Content-Type': 'application/json' } };

      await post('/api/test', data, config);

      expect(mockPost).toHaveBeenCalledWith('/api/test', data, config);
    });
  });

  describe('put函数', () => {
    it('应该调用axios.put方法', async () => {
      const { put } = await import('@/api/config/http');
      const mockResponse = { code: 200, data: { id: 1 } };
      mockPut.mockResolvedValue(mockResponse);

      const result = await put('/api/test/1', { name: 'updated' });

      expect(mockPut).toHaveBeenCalledWith('/api/test/1', { name: 'updated' }, undefined);
      expect(result).toEqual(mockResponse);
    });
  });

  describe('del函数', () => {
    it('应该调用axios.delete方法', async () => {
      const { del } = await import('@/api/config/http');
      const mockResponse = { code: 200, data: null };
      mockDelete.mockResolvedValue(mockResponse);

      const result = await del('/api/test/1');

      expect(mockDelete).toHaveBeenCalledWith('/api/test/1', undefined);
      expect(result).toEqual(mockResponse);
    });
  });

  describe('getBlob函数', () => {
    it('应该调用axios.get方法并设置responseType为blob', async () => {
      const { getBlob } = await import('@/api/config/http');
      const mockBlob = new Blob(['test'], { type: 'text/plain' });
      mockGet.mockResolvedValue(mockBlob);

      const result = await getBlob('/api/download');

      expect(mockGet).toHaveBeenCalledWith('/api/download', { responseType: 'blob' });
      expect(result).toEqual(mockBlob);
    });

    it('应该合并现有config参数', async () => {
      const { getBlob } = await import('@/api/config/http');
      const mockBlob = new Blob(['test'], { type: 'text/plain' });
      mockGet.mockResolvedValue(mockBlob);
      const config = { params: { id: 1 } };

      await getBlob('/api/download', config);

      expect(mockGet).toHaveBeenCalledWith('/api/download', { ...config, responseType: 'blob' });
    });
  });

  describe('错误处理', () => {
    it('应该传播axios错误', async () => {
      const { get } = await import('@/api/config/http');
      const error = new Error('Network error');
      mockGet.mockRejectedValue(error);

      await expect(get('/api/test')).rejects.toThrow('Network error');
    });
  });
});