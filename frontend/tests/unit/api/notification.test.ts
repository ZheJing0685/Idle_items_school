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

import notificationApi from '@/api/services/notification'

describe('Notification API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getNotifications', () => {
    it('should call get with correct url', async () => {
      const mockResponse = {
        code: 200,
        data: [{ id: 1, title: '系统通知', content: '测试内容', read: false }]
      }
      mockGet.mockResolvedValue(mockResponse)

      const result = await notificationApi.getNotifications()

      expect(mockGet).toHaveBeenCalledWith('/notifications', { params: undefined })
      expect(result).toEqual(mockResponse)
    })

    it('should call get with params', async () => {
      const mockResponse = { code: 200, data: [] }
      mockGet.mockResolvedValue(mockResponse)

      await notificationApi.getNotifications({ page: 1, size: 10 })

      expect(mockGet).toHaveBeenCalledWith('/notifications', { params: { page: 1, size: 10 } })
    })

    it('should throw error when fetch fails', async () => {
      const error = new Error('获取通知失败')
      mockGet.mockRejectedValue(error)

      await expect(notificationApi.getNotifications()).rejects.toThrow('获取通知失败')
    })
  })

  describe('getUnreadCount', () => {
    it('should call get with correct url', async () => {
      const mockResponse = { code: 200, data: { count: 5 } }
      mockGet.mockResolvedValue(mockResponse)

      const result = await notificationApi.getUnreadCount()

      expect(mockGet).toHaveBeenCalledWith('/notifications/unread-count')
      expect(result).toEqual(mockResponse)
    })

    it('should return 0 when no unread', async () => {
      const mockResponse = { code: 200, data: { count: 0 } }
      mockGet.mockResolvedValue(mockResponse)

      const result = await notificationApi.getUnreadCount()

      expect(result.data.count).toBe(0)
    })

    it('should throw error when fetch fails', async () => {
      const error = new Error('获取未读数量失败')
      mockGet.mockRejectedValue(error)

      await expect(notificationApi.getUnreadCount()).rejects.toThrow('获取未读数量失败')
    })
  })

  describe('markAsRead', () => {
    it('should call post with correct url', async () => {
      const mockResponse = { code: 200, data: null }
      mockPost.mockResolvedValue(mockResponse)

      const result = await notificationApi.markAsRead(1)

      expect(mockPost).toHaveBeenCalledWith('/notifications/1/read')
      expect(result).toEqual(mockResponse)
    })

    it('should handle string id', async () => {
      const mockResponse = { code: 200, data: null }
      mockPost.mockResolvedValue(mockResponse)

      await notificationApi.markAsRead('abc')

      expect(mockPost).toHaveBeenCalledWith('/notifications/abc/read')
    })

    it('should throw error when mark fails', async () => {
      const error = new Error('标记已读失败')
      mockPost.mockRejectedValue(error)

      await expect(notificationApi.markAsRead(1)).rejects.toThrow('标记已读失败')
    })
  })

  describe('markAllAsRead', () => {
    it('should call post with correct url', async () => {
      const mockResponse = { code: 200, data: null }
      mockPost.mockResolvedValue(mockResponse)

      const result = await notificationApi.markAllAsRead()

      expect(mockPost).toHaveBeenCalledWith('/notifications/read-all')
      expect(result).toEqual(mockResponse)
    })

    it('should throw error when mark all fails', async () => {
      const error = new Error('标记全部已读失败')
      mockPost.mockRejectedValue(error)

      await expect(notificationApi.markAllAsRead()).rejects.toThrow('标记全部已读失败')
    })
  })

  describe('deleteNotification', () => {
    it('should call del with correct url', async () => {
      const mockResponse = { code: 200, data: null }
      mockDel.mockResolvedValue(mockResponse)

      const result = await notificationApi.deleteNotification(1)

      expect(mockDel).toHaveBeenCalledWith('/notifications/1')
      expect(result).toEqual(mockResponse)
    })

    it('should handle string id', async () => {
      const mockResponse = { code: 200, data: null }
      mockDel.mockResolvedValue(mockResponse)

      await notificationApi.deleteNotification('abc')

      expect(mockDel).toHaveBeenCalledWith('/notifications/abc')
    })

    it('should throw error when delete fails', async () => {
      const error = new Error('删除通知失败')
      mockDel.mockRejectedValue(error)

      await expect(notificationApi.deleteNotification(1)).rejects.toThrow('删除通知失败')
    })
  })
})
