import { describe, it, expect, vi, beforeEach } from 'vitest'
import review from '../../../src/api/services/review'

describe('Review Service', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getReviewsByItem 应该导出函数', () => {
    expect(typeof review.getReviewsByItem).toBe('function')
  })

  it('createReview 应该导出函数', () => {
    expect(typeof review.createReview).toBe('function')
  })
})
