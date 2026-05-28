import { describe, it, expect, vi, beforeEach } from 'vitest'
import requestManager from '../../../src/utils/network/requestManager'

describe('RequestManager', () => {
  beforeEach(() => {
    requestManager.clearAllCache()
    requestManager.cancelAllRequests()
  })

  describe('generateKey', () => {
    it('应该生成正确的缓存键', () => {
      expect(requestManager.generateKey('/api/test')).toBe('/api/test')
    })

    it('应该包含参数的缓存键', () => {
      const key = requestManager.generateKey('/api/test', { b: 2, a: 1 })
      expect(key).toBe('/api/test?a=1&b=2')
    })
  })

  describe('setCache 和 getCache', () => {
    it('应该设置和获取缓存', () => {
      requestManager.setCache('test-key', { data: 'test' }, 60000)
      const cached = requestManager.getCache('test-key')
      expect(cached).toBeDefined()
      expect(cached?.data).toEqual({ data: 'test' })
    })

    it('应该返回 undefined 对于不存在的键', () => {
      const cached = requestManager.getCache('nonexistent')
      expect(cached).toBeUndefined()
    })
  })

  describe('isCacheExpired', () => {
    it('应该检测过期的缓存', () => {
      const expiredCache = {
        data: 'test',
        timestamp: Date.now() - 100000,
        expiry: 5000
      }
      expect(requestManager.isCacheExpired(expiredCache)).toBe(true)
    })

    it('应该检测未过期的缓存', () => {
      const validCache = {
        data: 'test',
        timestamp: Date.now(),
        expiry: 60000
      }
      expect(requestManager.isCacheExpired(validCache)).toBe(false)
    })
  })

  describe('clearCache', () => {
    it('应该清除指定缓存', () => {
      requestManager.setCache('test-key', 'data', 60000)
      requestManager.clearCache('test-key')
      expect(requestManager.getCache('test-key')).toBeUndefined()
    })
  })

  describe('clearAllCache', () => {
    it('应该清除所有缓存', () => {
      requestManager.setCache('key1', 'data1', 60000)
      requestManager.setCache('key2', 'data2', 60000)
      requestManager.clearAllCache()
      expect(requestManager.getCacheSize()).toBe(0)
    })
  })

  describe('clearCacheByPattern', () => {
    it('应该按模式清除缓存', () => {
      requestManager.setCache('/api/users/1', 'data1', 60000)
      requestManager.setCache('/api/users/2', 'data2', 60000)
      requestManager.setCache('/api/items/1', 'data3', 60000)
      requestManager.clearCacheByPattern('/api/users')
      expect(requestManager.getCacheSize()).toBe(1)
    })
  })

  describe('getCacheSize', () => {
    it('应该返回缓存大小', () => {
      expect(requestManager.getCacheSize()).toBe(0)
      requestManager.setCache('key1', 'data1', 60000)
      expect(requestManager.getCacheSize()).toBe(1)
    })
  })

  describe('setCacheSize', () => {
    it('应该设置缓存大小限制', () => {
      requestManager.setCacheSize(2)
      requestManager.setCache('key1', 'data1', 60000)
      requestManager.setCache('key2', 'data2', 60000)
      requestManager.setCache('key3', 'data3', 60000)
      expect(requestManager.getCacheSize()).toBe(2)
    })
  })

  describe('setCacheExpiry', () => {
    it('应该设置默认缓存过期时间', () => {
      const original = requestManager.getDefaultCacheExpiry()
      requestManager.setDefaultCacheExpiry(10000)
      expect(requestManager.getDefaultCacheExpiry()).toBe(10000)
      requestManager.setDefaultCacheExpiry(original)
    })
  })

  describe('request', () => {
    it('应该执行请求并返回结果', async () => {
      const mockFn = vi.fn().mockResolvedValue('result')
      const result = await requestManager.request('/api/test', mockFn)
      expect(result).toBe('result')
      expect(mockFn).toHaveBeenCalled()
    })

    it('应该使用缓存', async () => {
      const mockFn = vi.fn().mockResolvedValue('cached')
      await requestManager.request('/api/test', mockFn, { useCache: true, cacheExpiry: 60000 })
      const result = await requestManager.request('/api/test', mockFn, { useCache: true, cacheExpiry: 60000 })
      expect(result).toBe('cached')
      expect(mockFn).toHaveBeenCalledTimes(1)
    })

    it('应该处理请求错误', async () => {
      const mockFn = vi.fn().mockRejectedValue(new Error('Network error'))
      await expect(requestManager.request('/api/test', mockFn)).rejects.toThrow('Network error')
    })
  })

  describe('batchRequest', () => {
    it('应该批量执行请求', async () => {
      const requests = [
        { url: '/api/1', requestFn: () => Promise.resolve('result1') },
        { url: '/api/2', requestFn: () => Promise.resolve('result2') }
      ]
      const results = await requestManager.batchRequest(requests)
      expect(results).toEqual(['result1', 'result2'])
    })
  })

  describe('cancelAllRequests', () => {
    it('应该取消所有请求', () => {
      requestManager.cancelAllRequests()
      expect(requestManager.getCacheSize()).toBe(0)
    })
  })

  describe('evictOldestCache', () => {
    it('应该驱逐最旧的缓存', () => {
      requestManager.setCacheSize(2)
      requestManager.setCache('key1', 'data1', 60000)
      requestManager.setCache('key2', 'data2', 60000)
      requestManager.setCache('key3', 'data3', 60000)
      expect(requestManager.getCacheSize()).toBe(2)
    })
  })
})
