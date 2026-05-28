import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useConfigStore } from '../../../src/store/config'

// Mock configService
vi.mock('../../../src/api/services/config', () => ({
  default: {
    getAllConfigs: vi.fn(),
    getConfig: vi.fn(),
    getConfigsByGroup: vi.fn()
  }
}))

describe('Config Store', () => {
  let store
  let mockConfigService

  beforeEach(async () => {
    setActivePinia(createPinia())
    mockConfigService = vi.mocked((await import('../../../src/api/services/config')).default)
    vi.clearAllMocks()
    store = useConfigStore()
  })

  describe('初始状态', () => {
    it('应该有正确的初始状态', () => {
      expect(store.configs).toEqual({})
      expect(store.loading).toBe(false)
      expect(store.error).toBeNull()
      expect(store.lastFetchTime).toBeNull()
      expect(store.isCacheValid).toBe(false)
    })
  })

  describe('fetchAllConfigs', () => {
    it('应该获取所有配置', async () => {
      const mockData = { 'site.name': '测试平台' }
      mockConfigService.getAllConfigs.mockResolvedValue({ data: mockData })

      const result = await store.fetchAllConfigs()

      expect(result).toEqual(mockData)
      expect(store.configs).toEqual(mockData)
      expect(store.loading).toBe(false)
    })

    it('应该处理获取失败的情况', async () => {
      mockConfigService.getAllConfigs.mockRejectedValue(new Error('网络错误'))

      await expect(store.fetchAllConfigs()).rejects.toThrow('网络错误')
      expect(store.error).toBe('网络错误')
      expect(store.loading).toBe(false)
    })

    it('应该使用缓存（如果有效）', async () => {
      const mockData = { 'site.name': '测试平台' }
      mockConfigService.getAllConfigs.mockResolvedValue({ data: mockData })

      // 第一次获取
      await store.fetchAllConfigs()
      expect(mockConfigService.getAllConfigs).toHaveBeenCalledTimes(1)

      // 第二次应该使用缓存
      await store.fetchAllConfigs()
      expect(mockConfigService.getAllConfigs).toHaveBeenCalledTimes(1)
    })

    it('强制刷新时应该忽略缓存', async () => {
      const mockData = { 'site.name': '测试平台' }
      mockConfigService.getAllConfigs.mockResolvedValue({ data: mockData })

      await store.fetchAllConfigs()
      await store.fetchAllConfigs(true)

      expect(mockConfigService.getAllConfigs).toHaveBeenCalledTimes(2)
    })
  })

  describe('fetchConfig', () => {
    it('应该获取单个配置', async () => {
      const mockData = { configKey: 'site.name', configValue: '测试' }
      mockConfigService.getConfig.mockResolvedValue({ data: mockData })

      const result = await store.fetchConfig('site.name')

      expect(result).toEqual(mockData)
    })

    it('应该使用已缓存的配置', async () => {
      store.configs = { 'site.name': '已缓存' }

      const result = await store.fetchConfig('site.name')

      expect(result).toBe('已缓存')
      expect(mockConfigService.getConfig).not.toHaveBeenCalled()
    })
  })

  describe('getConfigSync', () => {
    it('应该返回配置值', () => {
      store.configs = { 'site.name': '测试' }

      expect(store.getConfigSync('site.name')).toBe('测试')
    })

    it('应该返回默认值（如果配置不存在）', () => {
      expect(store.getConfigSync('nonexistent', '默认')).toBe('默认')
    })
  })

  describe('getConfigString', () => {
    it('应该返回字符串配置', () => {
      store.configs = { 'site.name': '测试' }

      expect(store.getConfigString('site.name')).toBe('测试')
    })

    it('应该返回默认值', () => {
      expect(store.getConfigString('nonexistent', '默认')).toBe('默认')
    })
  })

  describe('getConfigInt', () => {
    it('应该返回整数配置', () => {
      store.configs = { 'max.items': '10' }

      expect(store.getConfigInt('max.items')).toBe(10)
    })

    it('应该处理无效值', () => {
      store.configs = { 'max.items': 'invalid' }

      expect(store.getConfigInt('max.items', 5)).toBe(5)
    })
  })

  describe('getConfigFloat', () => {
    it('应该返回浮点数配置', () => {
      store.configs = { 'commission.rate': '0.05' }

      expect(store.getConfigFloat('commission.rate')).toBe(0.05)
    })

    it('应该处理无效值', () => {
      store.configs = { 'commission.rate': 'invalid' }

      expect(store.getConfigFloat('commission.rate', 0.1)).toBe(0.1)
    })
  })

  describe('getConfigBoolean', () => {
    it('应该返回布尔配置', () => {
      store.configs = { 'enable.notification': true }

      expect(store.getConfigBoolean('enable.notification')).toBe(true)
    })

    it('应该转换字符串布尔值', () => {
      store.configs = { 'enable.notification': 'true' }

      expect(store.getConfigBoolean('enable.notification')).toBe(true)
    })

    it('应该返回默认值', () => {
      expect(store.getConfigBoolean('nonexistent', true)).toBe(true)
    })
  })

  describe('fetchConfigsByGroup', () => {
    it('应该获取分组配置', async () => {
      const mockData = { 'site.name': '测试' }
      mockConfigService.getConfigsByGroup.mockResolvedValue({ data: mockData })

      const result = await store.fetchConfigsByGroup('site')

      expect(result).toEqual(mockData)
    })
  })

  describe('clearCache', () => {
    it('应该清空缓存', () => {
      store.configs = { 'site.name': '测试' }
      store.lastFetchTime = Date.now()

      store.clearCache()

      expect(store.configs).toEqual({})
      expect(store.lastFetchTime).toBeNull()
      expect(store.isCacheValid).toBe(false)
    })
  })

  describe('refreshConfigs', () => {
    it('应该强制刷新配置', async () => {
      const mockData = { 'site.name': '新值' }
      mockConfigService.getAllConfigs.mockResolvedValue({ data: mockData })

      await store.refreshConfigs()

      expect(mockConfigService.getAllConfigs).toHaveBeenCalled()
      expect(store.configs).toEqual(mockData)
    })
  })
})
