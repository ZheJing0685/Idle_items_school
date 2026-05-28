import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useDictStore } from '../../../src/store/dict'

// Mock dictService
vi.mock('../../../src/api/services/dict', () => ({
  default: {
    getAllDicts: vi.fn(),
    getDictByType: vi.fn()
  }
}))

describe('Dict Store', () => {
  let store
  let mockDictService

  beforeEach(async () => {
    setActivePinia(createPinia())
    mockDictService = vi.mocked((await import('../../../src/api/services/dict')).default)
    vi.clearAllMocks()
    store = useDictStore()
  })

  describe('初始状态', () => {
    it('应该有正确的初始状态', () => {
      expect(store.dicts).toEqual({})
      expect(store.loading).toBe(false)
      expect(store.error).toBeNull()
      expect(store.lastFetchTime).toBeNull()
      expect(store.isCacheValid).toBe(false)
    })
  })

  describe('fetchAllDicts', () => {
    it('应该获取所有字典', async () => {
      const mockData = { ITEM_STATUS: [{ value: 'ACTIVE', label: '活跃' }] }
      mockDictService.getAllDicts.mockResolvedValue({ data: mockData })

      const result = await store.fetchAllDicts()

      expect(result).toEqual(mockData)
      expect(store.dicts).toEqual(mockData)
      expect(store.loading).toBe(false)
    })

    it('应该处理获取失败的情况', async () => {
      mockDictService.getAllDicts.mockRejectedValue(new Error('网络错误'))

      await expect(store.fetchAllDicts()).rejects.toThrow('网络错误')
      expect(store.error).toBe('网络错误')
      expect(store.loading).toBe(false)
    })

    it('应该使用缓存（如果有效）', async () => {
      const mockData = { ITEM_STATUS: [{ value: 'ACTIVE', label: '活跃' }] }
      mockDictService.getAllDicts.mockResolvedValue({ data: mockData })

      // 第一次获取
      await store.fetchAllDicts()
      expect(mockDictService.getAllDicts).toHaveBeenCalledTimes(1)

      // 第二次应该使用缓存
      await store.fetchAllDicts()
      expect(mockDictService.getAllDicts).toHaveBeenCalledTimes(1)
    })

    it('强制刷新时应该忽略缓存', async () => {
      const mockData = { ITEM_STATUS: [{ value: 'ACTIVE', label: '活跃' }] }
      mockDictService.getAllDicts.mockResolvedValue({ data: mockData })

      await store.fetchAllDicts()
      await store.fetchAllDicts(true)

      expect(mockDictService.getAllDicts).toHaveBeenCalledTimes(2)
    })
  })

  describe('fetchDictByType', () => {
    it('应该获取指定类型的字典', async () => {
      const mockData = [{ value: 'ACTIVE', label: '活跃' }]
      mockDictService.getDictByType.mockResolvedValue({ data: mockData })

      const result = await store.fetchDictByType('ITEM_STATUS')

      expect(result).toEqual(mockData)
      expect(store.dicts['ITEM_STATUS']).toEqual(mockData)
    })

    it('应该使用已缓存的字典', async () => {
      store.dicts = { ITEM_STATUS: [{ value: 'ACTIVE', label: '活跃' }] }

      const result = await store.fetchDictByType('ITEM_STATUS')

      expect(result).toEqual(store.dicts['ITEM_STATUS'])
      expect(mockDictService.getDictByType).not.toHaveBeenCalled()
    })
  })

  describe('getDictLabel', () => {
    it('应该返回字典标签', () => {
      store.dicts = { ITEM_STATUS: [{ value: 'ACTIVE', label: '活跃' }] }

      expect(store.getDictLabel('ITEM_STATUS', 'ACTIVE')).toBe('活跃')
    })

    it('找不到时应该返回原值', () => {
      store.dicts = { ITEM_STATUS: [{ value: 'ACTIVE', label: '活跃' }] }

      expect(store.getDictLabel('ITEM_STATUS', 'UNKNOWN')).toBe('UNKNOWN')
    })

    it('类型不存在时应该返回原值', () => {
      expect(store.getDictLabel('NONEXISTENT', 'VALUE')).toBe('VALUE')
    })

    it('值为空时应该返回空字符串', () => {
      expect(store.getDictLabel('ITEM_STATUS', '')).toBe('')
    })
  })

  describe('getDictLabelSync', () => {
    it('应该返回字典标签', () => {
      store.dicts = { ITEM_STATUS: [{ value: 'ACTIVE', label: '活跃' }] }

      expect(store.getDictLabelSync('ITEM_STATUS', 'ACTIVE')).toBe('活跃')
    })

    it('类型不存在时应该返回原值', () => {
      expect(store.getDictLabelSync('NONEXISTENT', 'VALUE')).toBe('VALUE')
    })
  })

  describe('getDictOptions', () => {
    it('应该返回格式化的选项', () => {
      store.dicts = { ITEM_STATUS: [{ value: 'ACTIVE', label: '活跃' }] }

      const options = store.getDictOptions('ITEM_STATUS')

      expect(options).toEqual([{ value: 'ACTIVE', label: '活跃' }])
    })

    it('类型不存在时应该返回空数组', () => {
      expect(store.getDictOptions('NONEXISTENT')).toEqual([])
    })
  })

  describe('getDictCssClass', () => {
    it('应该返回 CSS 类', () => {
      store.dicts = { ITEM_STATUS: [{ value: 'ACTIVE', label: '活跃', cssClass: 'active-class' }] }

      expect(store.getDictCssClass('ITEM_STATUS', 'ACTIVE')).toBe('active-class')
    })

    it('没有 CSS 类时应该返回空字符串', () => {
      store.dicts = { ITEM_STATUS: [{ value: 'ACTIVE', label: '活跃' }] }

      expect(store.getDictCssClass('ITEM_STATUS', 'ACTIVE')).toBe('')
    })

    it('类型不存在时应该返回空字符串', () => {
      expect(store.getDictCssClass('NONEXISTENT', 'VALUE')).toBe('')
    })
  })

  describe('clearCache', () => {
    it('应该清空缓存', () => {
      store.dicts = { ITEM_STATUS: [{ value: 'ACTIVE', label: '活跃' }] }
      store.lastFetchTime = Date.now()

      store.clearCache()

      expect(store.dicts).toEqual({})
      expect(store.lastFetchTime).toBeNull()
      expect(store.isCacheValid).toBe(false)
    })
  })

  describe('refreshDicts', () => {
    it('应该强制刷新字典', async () => {
      const mockData = { ITEM_STATUS: [{ value: 'ACTIVE', label: '活跃' }] }
      mockDictService.getAllDicts.mockResolvedValue({ data: mockData })

      await store.refreshDicts()

      expect(mockDictService.getAllDicts).toHaveBeenCalled()
      expect(store.dicts).toEqual(mockData)
    })
  })
})
