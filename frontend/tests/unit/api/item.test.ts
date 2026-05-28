import { describe, it, expect, vi, beforeEach } from 'vitest'

const { mockGet, mockPost, mockPut, mockDel } = vi.hoisted(() => ({
  mockGet: vi.fn(),
  mockPost: vi.fn(),
  mockPut: vi.fn(),
  mockDel: vi.fn(),
}))

vi.mock('@/api/config/http', () => ({
  get: (...args: any[]) => mockGet(...args),
  post: (...args: any[]) => mockPost(...args),
  put: (...args: any[]) => mockPut(...args),
  del: (...args: any[]) => mockDel(...args),
}))

vi.mock('@/utils/network/requestManager', () => ({
  default: {
    request: vi.fn((_url: string, requestFn: () => any) => requestFn()),
  },
}))

import itemApi from '@/api/services/item'

describe('Item API (TypeScript)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getItems', () => {
    it('should call get with correct url and params', async () => {
      const mockResponse = { code: 200, data: { records: [], total: 0 } }
      mockGet.mockResolvedValue(mockResponse)

      const result = await itemApi.getItems({ page: 1, size: 10 })

      expect(mockGet).toHaveBeenCalledWith('/items', {
        params: { page: 1, size: 10 },
      })
      expect(result).toEqual(mockResponse)
    })

    it('should call get without params', async () => {
      const mockResponse = { code: 200, data: { records: [], total: 0 } }
      mockGet.mockResolvedValue(mockResponse)

      await itemApi.getItems()

      expect(mockGet).toHaveBeenCalledWith('/items', { params: undefined })
    })
  })

  describe('getHotItems', () => {
    it('should call get with correct url', async () => {
      const mockResponse = { code: 200, data: [{ id: 1, title: 'Hot Item' }] }
      mockGet.mockResolvedValue(mockResponse)

      const result = await itemApi.getHotItems()

      expect(mockGet).toHaveBeenCalledWith('/items/hot')
      expect(result).toEqual(mockResponse)
    })
  })

  describe('searchItems', () => {
    it('should call get with correct url and search params', async () => {
      const mockResponse = { code: 200, data: { records: [], total: 0 } }
      mockGet.mockResolvedValue(mockResponse)

      const result = await itemApi.searchItems('手机', 1, 10, 'price')

      expect(mockGet).toHaveBeenCalledWith('/items/search', {
        params: { keyword: '手机', page: 1, size: 10, sortBy: 'price' },
      })
      expect(result).toEqual(mockResponse)
    })
  })

  describe('getItem', () => {
    it('should call get with correct url for item detail', async () => {
      const mockResponse = { code: 200, data: { id: 1, title: 'Test Item' } }
      mockGet.mockResolvedValue(mockResponse)

      const result = await itemApi.getItem(1)

      expect(mockGet).toHaveBeenCalledWith('/items/1')
      expect(result).toEqual(mockResponse)
    })

    it('should handle string id', async () => {
      const mockResponse = { code: 200, data: { id: 'abc', title: 'Test Item' } }
      mockGet.mockResolvedValue(mockResponse)

      await itemApi.getItem('abc')

      expect(mockGet).toHaveBeenCalledWith('/items/abc')
    })
  })

  describe('createItem', () => {
    it('should call post with correct url and data', async () => {
      const mockResponse = { code: 200, data: { id: 1, title: 'New Item' } }
      mockPost.mockResolvedValue(mockResponse)

      const itemData = { title: 'New Item', price: 99.9, categoryId: 1 }
      const result = await itemApi.createItem(itemData)

      expect(mockPost).toHaveBeenCalledWith('/items', itemData)
      expect(result).toEqual(mockResponse)
    })
  })

  describe('updateItem', () => {
    it('should call put with correct url and data', async () => {
      const mockResponse = { code: 200, data: { id: 1, title: 'Updated' } }
      mockPut.mockResolvedValue(mockResponse)

      const updateData = { title: 'Updated', price: 88.8 }
      const result = await itemApi.updateItem(1, updateData)

      expect(mockPut).toHaveBeenCalledWith('/items/1', updateData)
      expect(result).toEqual(mockResponse)
    })
  })

  describe('offShelf', () => {
    it('should call post with correct url', async () => {
      const mockResponse = { code: 200, data: null }
      mockPost.mockResolvedValue(mockResponse)

      const result = await itemApi.offShelf(1)

      expect(mockPost).toHaveBeenCalledWith('/items/1/off-shelf')
      expect(result).toEqual(mockResponse)
    })
  })

  describe('onShelf', () => {
    it('should call post with correct url', async () => {
      const mockResponse = { code: 200, data: null }
      mockPost.mockResolvedValue(mockResponse)

      const result = await itemApi.onShelf(1)

      expect(mockPost).toHaveBeenCalledWith('/items/1/on-shelf')
      expect(result).toEqual(mockResponse)
    })
  })

  describe('uploadImage', () => {
    it('should call post with FormData', async () => {
      const mockResponse = { code: 200, data: { url: 'https://example.com/image.jpg' } }
      mockPost.mockResolvedValue(mockResponse)

      const formData = new FormData()
      formData.append('file', 'test.jpg')
      const result = await itemApi.uploadImage(formData)

      expect(mockPost).toHaveBeenCalledWith('/items/upload', formData)
      expect(result).toEqual(mockResponse)
    })
  })

  describe('deleteItem', () => {
    it('should call del with correct url', async () => {
      const mockResponse = { code: 200, data: null }
      mockDel.mockResolvedValue(mockResponse)

      const result = await itemApi.deleteItem(1)

      expect(mockDel).toHaveBeenCalledWith('/items/1')
      expect(result).toEqual(mockResponse)
    })
  })

  describe('error handling', () => {
    it('should propagate errors from http methods', async () => {
      const error = new Error('Network error')
      mockGet.mockRejectedValue(error)

      await expect(itemApi.getItems()).rejects.toThrow('Network error')
    })

    it('should propagate errors from post', async () => {
      const error = new Error('Server error')
      mockPost.mockRejectedValue(error)

      await expect(itemApi.createItem({})).rejects.toThrow('Server error')
    })
  })
})
