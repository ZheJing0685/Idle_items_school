import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('@/api/config/http', () => ({
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  del: vi.fn()
}))

describe('Dict API 服务', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('应该导出dictService对象', async () => {
    const { default: dictService } = await import('@/api/services/dict')
    expect(dictService).toBeDefined()
  })

  it('应该有getAllDicts方法', async () => {
    const { default: dictService } = await import('@/api/services/dict')
    expect(typeof dictService.getAllDicts).toBe('function')
  })

  it('应该有getDictByType方法', async () => {
    const { default: dictService } = await import('@/api/services/dict')
    expect(typeof dictService.getDictByType).toBe('function')
  })


})
