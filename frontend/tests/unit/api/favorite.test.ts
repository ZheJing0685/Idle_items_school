import { describe, it, expect, vi, beforeEach } from 'vitest'

const { mockGet, mockPost, mockDel } = vi.hoisted(() => ({
  mockGet: vi.fn(),
  mockPost: vi.fn(),
  mockDel: vi.fn()
}))

vi.mock('@/api/config/http', () => ({
  get: (...args: any[]) => mockGet(...args),
  post: (...args: any[]) => mockPost(...args),
  del: (...args: any[]) => mockDel(...args)
}))

import favoriteApi from '@/api/services/favorite'

describe('Favorite API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getFavorites', () => {
    it('should call get with correct url', async () => {
      const mockResponse = { code: 200, data: [{ id: 1, itemId: 1, title: '收藏物品' }] }
      mockGet.mockResolvedValue(mockResponse)

      const result = await favoriteApi.getFavorites()

      expect(mockGet).toHaveBeenCalledWith('/favorites', { params: { page: undefined, size: undefined } })
      expect(result).toEqual(mockResponse)
    })

    it('should call get with pagination params', async () => {
      const mockResponse = { code: 200, data: [] }
      mockGet.mockResolvedValue(mockResponse)

      await favoriteApi.getFavorites(1, 10)

      expect(mockGet).toHaveBeenCalledWith('/favorites', { params: { page: 1, size: 10 } })
    })

    it('should throw error when fetch fails', async () => {
      const error = new Error('获取收藏失败')
      mockGet.mockRejectedValue(error)

      await expect(favoriteApi.getFavorites()).rejects.toThrow('获取收藏失败')
    })
  })

  describe('addFavorite', () => {
    it('should call post with correct url', async () => {
      const mockResponse = { code: 200, data: null }
      mockPost.mockResolvedValue(mockResponse)

      const result = await favoriteApi.addFavorite(1)

      expect(mockPost).toHaveBeenCalledWith('/favorites/1')
      expect(result).toEqual(mockResponse)
    })

    it('should handle string id', async () => {
      const mockResponse = { code: 200, data: null }
      mockPost.mockResolvedValue(mockResponse)

      await favoriteApi.addFavorite('abc')

      expect(mockPost).toHaveBeenCalledWith('/favorites/abc')
    })

    it('should throw error when add fails', async () => {
      const error = new Error('添加收藏失败')
      mockPost.mockRejectedValue(error)

      await expect(favoriteApi.addFavorite(1)).rejects.toThrow('添加收藏失败')
    })
  })

  describe('removeFavorite', () => {
    it('should call del with correct url', async () => {
      const mockResponse = { code: 200, data: null }
      mockDel.mockResolvedValue(mockResponse)

      const result = await favoriteApi.removeFavorite(1)

      expect(mockDel).toHaveBeenCalledWith('/favorites/1')
      expect(result).toEqual(mockResponse)
    })

    it('should handle string id', async () => {
      const mockResponse = { code: 200, data: null }
      mockDel.mockResolvedValue(mockResponse)

      await favoriteApi.removeFavorite('abc')

      expect(mockDel).toHaveBeenCalledWith('/favorites/abc')
    })

    it('should throw error when remove fails', async () => {
      const error = new Error('删除收藏失败')
      mockDel.mockRejectedValue(error)

      await expect(favoriteApi.removeFavorite(1)).rejects.toThrow('删除收藏失败')
    })
  })

  describe('checkFavorite', () => {
    it('should call get with correct url', async () => {
      const mockResponse = { code: 200, data: { isFavorited: true } }
      mockGet.mockResolvedValue(mockResponse)

      const result = await favoriteApi.checkFavorite(1)

      expect(mockGet).toHaveBeenCalledWith('/favorites/1/status')
      expect(result).toEqual(mockResponse)
    })

    it('should return false when not favorited', async () => {
      const mockResponse = { code: 200, data: { isFavorited: false } }
      mockGet.mockResolvedValue(mockResponse)

      const result = await favoriteApi.checkFavorite(1)

      expect(result.data.isFavorited).toBe(false)
    })

    it('should throw error when check fails', async () => {
      const error = new Error('检查收藏状态失败')
      mockGet.mockRejectedValue(error)

      await expect(favoriteApi.checkFavorite(1)).rejects.toThrow('检查收藏状态失败')
    })
  })
})
