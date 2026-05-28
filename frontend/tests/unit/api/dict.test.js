import { describe, it, expect, vi, beforeEach } from 'vitest'
import dictService from '../../../src/api/services/dict'

describe('Dict Service', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getAllDicts 应该导出函数', () => {
    expect(typeof dictService.getAllDicts).toBe('function')
  })

  it('getDictByType 应该导出函数', () => {
    expect(typeof dictService.getDictByType).toBe('function')
  })

  it('getDictLabel 应该导出函数', () => {
    expect(typeof dictService.getDictLabel).toBe('function')
  })

  it('getDictOptions 应该导出函数', () => {
    expect(typeof dictService.getDictOptions).toBe('function')
  })

  it('clearDictCache 应该导出函数', () => {
    expect(typeof dictService.clearDictCache).toBe('function')
  })

  it('reloadDictCache 应该导出函数', () => {
    expect(typeof dictService.reloadDictCache).toBe('function')
  })
})
