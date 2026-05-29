import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'

vi.mock('@/api/services/config', () => ({
  default: {
    getAllConfigs: vi.fn(),
    getConfig: vi.fn(),
    getConfigsByGroup: vi.fn()
  }
}))

describe('Config Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('应该导出useConfigStore', async () => {
    const { useConfigStore } = await import('@/store/config')
    expect(useConfigStore).toBeDefined()
  })

  it('应该有正确的初始状态', async () => {
    const { useConfigStore } = await import('@/store/config')
    const store = useConfigStore()
    
    expect(store.configs).toEqual({})
    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
    expect(store.lastFetchTime).toBeNull()
  })

  it('应该有isCacheValid计算属性', async () => {
    const { useConfigStore } = await import('@/store/config')
    const store = useConfigStore()
    
    expect(store.isCacheValid).toBe(false)
  })

  it('应该有fetchAllConfigs方法', async () => {
    const { useConfigStore } = await import('@/store/config')
    const store = useConfigStore()
    
    expect(typeof store.fetchAllConfigs).toBe('function')
  })

  it('应该有fetchConfig方法', async () => {
    const { useConfigStore } = await import('@/store/config')
    const store = useConfigStore()
    
    expect(typeof store.fetchConfig).toBe('function')
  })

  it('应该有getConfigSync方法', async () => {
    const { useConfigStore } = await import('@/store/config')
    const store = useConfigStore()
    
    expect(typeof store.getConfigSync).toBe('function')
  })

  it('应该有getConfigString方法', async () => {
    const { useConfigStore } = await import('@/store/config')
    const store = useConfigStore()
    
    expect(typeof store.getConfigString).toBe('function')
  })

  it('应该有getConfigInt方法', async () => {
    const { useConfigStore } = await import('@/store/config')
    const store = useConfigStore()
    
    expect(typeof store.getConfigInt).toBe('function')
  })

  it('应该有getConfigFloat方法', async () => {
    const { useConfigStore } = await import('@/store/config')
    const store = useConfigStore()
    
    expect(typeof store.getConfigFloat).toBe('function')
  })

  it('应该有getConfigBoolean方法', async () => {
    const { useConfigStore } = await import('@/store/config')
    const store = useConfigStore()
    
    expect(typeof store.getConfigBoolean).toBe('function')
  })

  it('应该有fetchConfigsByGroup方法', async () => {
    const { useConfigStore } = await import('@/store/config')
    const store = useConfigStore()
    
    expect(typeof store.fetchConfigsByGroup).toBe('function')
  })

  it('应该有clearCache方法', async () => {
    const { useConfigStore } = await import('@/store/config')
    const store = useConfigStore()
    
    expect(typeof store.clearCache).toBe('function')
  })

  it('应该有refreshConfigs方法', async () => {
    const { useConfigStore } = await import('@/store/config')
    const store = useConfigStore()
    
    expect(typeof store.refreshConfigs).toBe('function')
  })

  it('getConfigSync应该返回默认值当配置不存在', async () => {
    const { useConfigStore } = await import('@/store/config')
    const store = useConfigStore()
    
    expect(store.getConfigSync('nonexistent')).toBeNull()
    expect(store.getConfigSync('nonexistent', 'default')).toBe('default')
  })

  it('getConfigString应该返回空字符串当配置不存在', async () => {
    const { useConfigStore } = await import('@/store/config')
    const store = useConfigStore()
    
    expect(store.getConfigString('nonexistent')).toBe('')
    expect(store.getConfigString('nonexistent', 'default')).toBe('default')
  })

  it('getConfigInt应该返回0当配置不存在', async () => {
    const { useConfigStore } = await import('@/store/config')
    const store = useConfigStore()
    
    expect(store.getConfigInt('nonexistent')).toBe(0)
    expect(store.getConfigInt('nonexistent', 10)).toBe(10)
  })

  it('getConfigFloat应该返回0.0当配置不存在', async () => {
    const { useConfigStore } = await import('@/store/config')
    const store = useConfigStore()
    
    expect(store.getConfigFloat('nonexistent')).toBe(0.0)
    expect(store.getConfigFloat('nonexistent', 1.5)).toBe(1.5)
  })

  it('getConfigBoolean应该返回false当配置不存在', async () => {
    const { useConfigStore } = await import('@/store/config')
    const store = useConfigStore()
    
    expect(store.getConfigBoolean('nonexistent')).toBe(false)
    expect(store.getConfigBoolean('nonexistent', true)).toBe(true)
  })

  it('clearCache应该清空配置', async () => {
    const { useConfigStore } = await import('@/store/config')
    const store = useConfigStore()
    
    store.clearCache()
    expect(store.configs).toEqual({})
    expect(store.lastFetchTime).toBeNull()
  })
})
