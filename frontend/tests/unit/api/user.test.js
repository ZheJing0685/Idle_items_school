import { describe, it, expect, vi, beforeEach } from 'vitest'
import user from '../../../src/api/services/user'

describe('User Service', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getItems 应该导出函数', () => {
    expect(typeof user.getItems).toBe('function')
  })

  it('getProfile 应该导出函数', () => {
    expect(typeof user.getProfile).toBe('function')
  })

  it('updateProfile 应该导出函数', () => {
    expect(typeof user.updateProfile).toBe('function')
  })

  it('getStats 应该导出函数', () => {
    expect(typeof user.getStats).toBe('function')
  })
})
