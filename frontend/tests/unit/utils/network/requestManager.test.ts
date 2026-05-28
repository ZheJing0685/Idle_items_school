import { describe, it, expect, vi, beforeEach } from 'vitest';
import requestManager from '@/utils/network/requestManager';

describe('RequestManager工具', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    requestManager.clearAllCache();
  });

  describe('request', () => {
    it('应该能执行基本请求', async () => {
      const mockFn = vi.fn().mockResolvedValue({ data: 'test' });

      const result = await requestManager.request('/api/test', mockFn);

      expect(result).toEqual({ data: 'test' });
      expect(mockFn).toHaveBeenCalledOnce();
    });

    it('应该能处理请求错误', async () => {
      const mockFn = vi.fn().mockRejectedValue(new Error('Network error'));

      await expect(requestManager.request('/api/test', mockFn)).rejects.toThrow('Network error');
    });

    it('应该能缓存请求结果', async () => {
      const mockFn = vi.fn().mockResolvedValue({ data: 'cached' });

      // 第一次请求
      await requestManager.request('/api/test', mockFn, { useCache: true });
      // 第二次请求（应该使用缓存）
      const result = await requestManager.request('/api/test', mockFn, { useCache: true });

      expect(result).toEqual({ data: 'cached' });
      expect(mockFn).toHaveBeenCalledOnce(); // 只调用了一次
    });

    it('应该能合并重复请求', async () => {
      const mockFn = vi.fn().mockResolvedValue({ data: 'merged' });

      // 并发两个相同请求
      const promise1 = requestManager.request('/api/test', mockFn, { useMerge: true });
      const promise2 = requestManager.request('/api/test', mockFn, { useMerge: true });

      const [result1, result2] = await Promise.all([promise1, promise2]);

      expect(result1).toEqual({ data: 'merged' });
      expect(result2).toEqual({ data: 'merged' });
      expect(mockFn).toHaveBeenCalledOnce(); // 只调用了一次
    });

    it('应该能使用自定义缓存键', async () => {
      const mockFn = vi.fn().mockResolvedValue({ data: 'custom' });

      await requestManager.request('/api/test', mockFn, {
        useCache: true,
        cacheKey: 'custom-key',
      });

      const cached = requestManager.getCache('custom-key');
      expect(cached).toBeDefined();
      expect(cached?.data).toEqual({ data: 'custom' });
    });
  });

  describe('batchRequest', () => {
    it('应该能批量执行请求', async () => {
      const mockFn1 = vi.fn().mockResolvedValue({ data: 'test1' });
      const mockFn2 = vi.fn().mockResolvedValue({ data: 'test2' });

      const results = await requestManager.batchRequest([
        { url: '/api/test1', requestFn: mockFn1 },
        { url: '/api/test2', requestFn: mockFn2 },
      ]);

      expect(results).toHaveLength(2);
      expect(results[0]).toEqual({ data: 'test1' });
      expect(results[1]).toEqual({ data: 'test2' });
    });

    it('应该能处理批量请求中的错误', async () => {
      const mockFn1 = vi.fn().mockResolvedValue({ data: 'test1' });
      const mockFn2 = vi.fn().mockRejectedValue(new Error('Error'));

      const results = await requestManager.batchRequest([
        { url: '/api/test1', requestFn: mockFn1 },
        { url: '/api/test2', requestFn: mockFn2 },
      ]);

      expect(results).toHaveLength(2);
      expect(results[0]).toEqual({ data: 'test1' });
      expect(results[1]).toBeInstanceOf(Error);
    });

    it('应该能合并相同URL的批量请求', async () => {
      const mockFn = vi.fn().mockResolvedValue({ data: 'merged' });

      const results = await requestManager.batchRequest([
        { url: '/api/test', requestFn: mockFn },
        { url: '/api/test', requestFn: mockFn },
      ]);

      expect(results).toHaveLength(1);
      expect(mockFn).toHaveBeenCalledOnce();
    });
  });

  describe('缓存管理', () => {
    it('应该能设置缓存', () => {
      requestManager.setCache('key1', { data: 'test' }, 5000);

      const cached = requestManager.getCache('key1');
      expect(cached).toBeDefined();
      expect(cached?.data).toEqual({ data: 'test' });
    });

    it('应该能清除缓存', () => {
      requestManager.setCache('key1', { data: 'test' }, 5000);
      requestManager.clearCache('key1');

      const cached = requestManager.getCache('key1');
      expect(cached).toBeUndefined();
    });

    it('应该能清除所有缓存', () => {
      requestManager.setCache('key1', { data: 'test1' }, 5000);
      requestManager.setCache('key2', { data: 'test2' }, 5000);

      requestManager.clearAllCache();

      expect(requestManager.getCacheSize()).toBe(0);
    });

    it('应该能按模式清除缓存', () => {
      requestManager.setCache('/api/users/1', { data: 'user1' }, 5000);
      requestManager.setCache('/api/users/2', { data: 'user2' }, 5000);
      requestManager.setCache('/api/items/1', { data: 'item1' }, 5000);

      requestManager.clearCacheByPattern('/api/users');

      expect(requestManager.getCacheSize()).toBe(1);
    });

    it('应该能检查缓存是否过期', () => {
      requestManager.setCache('key1', { data: 'test' }, 1000); // 1秒过期

      const cached = requestManager.getCache('key1');
      expect(cached).toBeDefined();
      expect(requestManager.isCacheExpired(cached!)).toBe(false);
    });

    it('应该能驱逐最旧的缓存', () => {
      requestManager.setCacheSize(2);

      requestManager.setCache('key1', { data: 'test1' }, 5000);
      requestManager.setCache('key2', { data: 'test2' }, 5000);
      requestManager.setCache('key3', { data: 'test3' }, 5000); // 应该驱逐key1

      expect(requestManager.getCacheSize()).toBe(2);
      expect(requestManager.getCache('key1')).toBeUndefined();
    });
  });

  describe('请求管理', () => {
    it('应该能生成缓存键', () => {
      const key1 = requestManager.generateKey('/api/test');
      const key2 = requestManager.generateKey('/api/test', { id: 1 });
      const key3 = requestManager.generateKey('/api/test', { id: 1, name: 'test' });

      expect(key1).toBe('/api/test');
      expect(key2).toBe('/api/test?id=1');
      expect(key3).toBe('/api/test?id=1&name=test');
    });

    it('应该能取消所有请求', async () => {
      const mockFn = vi.fn().mockResolvedValue({ data: 'test' });

      requestManager.request('/api/test', mockFn, { useMerge: true });

      requestManager.cancelAllRequests();

      // 验证请求已被清除
      const key = requestManager.generateKey('/api/test');
      expect(requestManager.getCache(key)).toBeUndefined();
    });

    it('应该能取消特定请求', async () => {
      const mockFn = vi.fn().mockResolvedValue({ data: 'test' });

      requestManager.request('/api/test', mockFn, { useMerge: true });

      requestManager.cancelRequest('/api/test');

      // 验证请求已被清除
      const key = requestManager.generateKey('/api/test');
      expect(requestManager.getCache(key)).toBeUndefined();
    });
  });

  describe('配置', () => {
    it('应该能设置和获取缓存大小', () => {
      requestManager.setCacheSize(50);
      expect(requestManager.getCacheSize()).toBe(0); // 当前没有缓存
    });

    it('应该能设置和获取默认缓存过期时间', () => {
      requestManager.setDefaultCacheExpiry(10000);
      expect(requestManager.getDefaultCacheExpiry()).toBe(10000);
    });
  });
});
