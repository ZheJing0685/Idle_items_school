import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('@/api/config/http', () => ({
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  del: vi.fn()
}))

describe('Config API 服务', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('应该导出configService对象', async () => {
    const { default: configService } = await import('@/api/services/config')
    expect(configService).toBeDefined()
  })

  it('应该有getAllConfigs方法', async () => {
    const { default: configService } = await import('@/api/services/config')
    expect(typeof configService.getAllConfigs).toBe('function')
  })

  it('应该有getConfig方法', async () => {
    const { default: configService } = await import('@/api/services/config')
    expect(typeof configService.getConfig).toBe('function')
  })

  it('应该有getConfigsByGroup方法', async () => {
    const { default: configService } = await import('@/api/services/config')
    expect(typeof configService.getConfigsByGroup).toBe('function')
  })

  it('应该有deleteConfig方法', async () => {
    const { default: configService } = await import('@/api/services/config')
    expect(typeof configService.deleteConfig).toBe('function')
  })
})
