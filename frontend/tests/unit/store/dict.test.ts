import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'

vi.mock('@/api/services/dict', () => ({
  default: {
    getAllDicts: vi.fn(),
    getDictByType: vi.fn()
  }
}))

describe('Dict Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('应该导出useDictStore', async () => {
    const { useDictStore } = await import('@/store/dict')
    expect(useDictStore).toBeDefined()
  })

  it('应该有正确的初始状态', async () => {
    const { useDictStore } = await import('@/store/dict')
    const store = useDictStore()
    
    expect(store.dicts).toEqual({})
    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
    expect(store.lastFetchTime).toBeNull()
  })

  it('应该有isCacheValid计算属性', async () => {
    const { useDictStore } = await import('@/store/dict')
    const store = useDictStore()
    
    expect(store.isCacheValid).toBe(false)
  })

  it('应该有fetchAllDicts方法', async () => {
    const { useDictStore } = await import('@/store/dict')
    const store = useDictStore()
    
    expect(typeof store.fetchAllDicts).toBe('function')
  })

  it('应该有fetchDictByType方法', async () => {
    const { useDictStore } = await import('@/store/dict')
    const store = useDictStore()
    
    expect(typeof store.fetchDictByType).toBe('function')
  })

  it('应该有getDictLabel方法', async () => {
    const { useDictStore } = await import('@/store/dict')
    const store = useDictStore()
    
    expect(typeof store.getDictLabel).toBe('function')
  })

  it('应该有getDictLabelSync方法', async () => {
    const { useDictStore } = await import('@/store/dict')
    const store = useDictStore()
    
    expect(typeof store.getDictLabelSync).toBe('function')
  })

  it('应该有getDictOptions方法', async () => {
    const { useDictStore } = await import('@/store/dict')
    const store = useDictStore()
    
    expect(typeof store.getDictOptions).toBe('function')
  })

  it('应该有getDictCssClass方法', async () => {
    const { useDictStore } = await import('@/store/dict')
    const store = useDictStore()
    
    expect(typeof store.getDictCssClass).toBe('function')
  })

  it('应该有clearCache方法', async () => {
    const { useDictStore } = await import('@/store/dict')
    const store = useDictStore()
    
    expect(typeof store.clearCache).toBe('function')
  })

  it('应该有refreshDicts方法', async () => {
    const { useDictStore } = await import('@/store/dict')
    const store = useDictStore()
    
    expect(typeof store.refreshDicts).toBe('function')
  })

  it('应该有preloadCommonDicts方法', async () => {
    const { useDictStore } = await import('@/store/dict')
    const store = useDictStore()
    
    expect(typeof store.preloadCommonDicts).toBe('function')
  })

  it('getDictLabel应该返回itemValue当字典不存在', async () => {
    const { useDictStore } = await import('@/store/dict')
    const store = useDictStore()
    
    expect(store.getDictLabel('nonexistent', 'value')).toBe('value')
  })

  it('getDictLabel应该返回空字符串当itemValue为空', async () => {
    const { useDictStore } = await import('@/store/dict')
    const store = useDictStore()
    
    expect(store.getDictLabel('nonexistent', '')).toBe('')
  })

  it('getDictLabelSync应该返回itemValue当字典不存在', async () => {
    const { useDictStore } = await import('@/store/dict')
    const store = useDictStore()
    
    expect(store.getDictLabelSync('nonexistent', 'value')).toBe('value')
  })

  it('getDictOptions应该返回空数组当字典不存在', async () => {
    const { useDictStore } = await import('@/store/dict')
    const store = useDictStore()
    
    expect(store.getDictOptions('nonexistent')).toEqual([])
  })

  it('getDictCssClass应该返回空字符串当字典不存在', async () => {
    const { useDictStore } = await import('@/store/dict')
    const store = useDictStore()
    
    expect(store.getDictCssClass('nonexistent', 'value')).toBe('')
  })

  it('getDictCssClass应该返回空字符串当itemValue为空', async () => {
    const { useDictStore } = await import('@/store/dict')
    const store = useDictStore()
    
    expect(store.getDictCssClass('nonexistent', '')).toBe('')
  })

  it('clearCache应该清空字典', async () => {
    const { useDictStore } = await import('@/store/dict')
    const store = useDictStore()
    
    store.clearCache()
    expect(store.dicts).toEqual({})
    expect(store.lastFetchTime).toBeNull()
  })
})
