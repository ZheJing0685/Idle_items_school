import { describe, it, expect, vi, beforeEach } from 'vitest'

const { mockGet, mockPost } = vi.hoisted(() => ({
  mockGet: vi.fn(),
  mockPost: vi.fn()
}))

vi.mock('@/api/config/http', () => ({
  get: (...args: any[]) => mockGet(...args),
  post: (...args: any[]) => mockPost(...args)
}))

import reviewApi from '@/api/services/review'

describe('Review API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getReviewsByItem', () => {
    it('should call get with correct url', async () => {
      const mockResponse = {
        code: 200,
        data: [{
          id: 1,
          rating: 5,
          content: '很好的物品',
          reviewerName: '测试用户',
          createdAt: '2026-05-28'
        }]
      }
      mockGet.mockResolvedValue(mockResponse)

      const result = await reviewApi.getReviewsByItem(1)

      expect(mockGet).toHaveBeenCalledWith('/reviews/item/1')
      expect(result).toEqual(mockResponse)
    })

    it('should handle string id', async () => {
      const mockResponse = { code: 200, data: [] }
      mockGet.mockResolvedValue(mockResponse)

      await reviewApi.getReviewsByItem('abc')

      expect(mockGet).toHaveBeenCalledWith('/reviews/item/abc')
    })

    it('should handle empty reviews', async () => {
      const mockResponse = { code: 200, data: [] }
      mockGet.mockResolvedValue(mockResponse)

      const result = await reviewApi.getReviewsByItem(1)

      expect(result.data).toEqual([])
    })

    it('should throw error when fetch fails', async () => {
      const error = new Error('获取评价失败')
      mockGet.mockRejectedValue(error)

      await expect(reviewApi.getReviewsByItem(1)).rejects.toThrow('获取评价失败')
    })
  })

  describe('createReview', () => {
    it('should call post with correct url and data', async () => {
      const mockResponse = {
        code: 200,
        data: { id: 1, rating: 5, content: '很好的物品' }
      }
      mockPost.mockResolvedValue(mockResponse)

      const reviewData = { rating: 5, content: '很好的物品' }
      const result = await reviewApi.createReview(1, reviewData)

      expect(mockPost).toHaveBeenCalledWith('/reviews/order/1', reviewData)
      expect(result).toEqual(mockResponse)
    })

    it('should handle string order id', async () => {
      const mockResponse = { code: 200, data: { id: 1 } }
      mockPost.mockResolvedValue(mockResponse)

      const reviewData = { rating: 5, content: '测试' }
      await reviewApi.createReview('order-abc', reviewData)

      expect(mockPost).toHaveBeenCalledWith('/reviews/order/order-abc', reviewData)
    })

    it('should throw error when create fails', async () => {
      const error = new Error('创建评价失败')
      mockPost.mockRejectedValue(error)

      await expect(reviewApi.createReview(1, { rating: 5, content: '测试' })).rejects.toThrow('创建评价失败')
    })
  })
})
