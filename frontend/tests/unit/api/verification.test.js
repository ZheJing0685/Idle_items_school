import { describe, it, expect, vi, beforeEach } from 'vitest'
import verification from '../../../src/api/services/verification'

describe('Verification Service', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getStatus 应该导出函数', () => {
    expect(typeof verification.getStatus).toBe('function')
  })

  it('submit 应该导出函数', () => {
    expect(typeof verification.submit).toBe('function')
  })

  it('upload 应该导出函数', () => {
    expect(typeof verification.upload).toBe('function')
  })

  it('resubmit 应该导出函数', () => {
    expect(typeof verification.resubmit).toBe('function')
  })
})
