import { describe, it, expect, vi, beforeEach } from 'vitest'

const { mockGet, mockPost } = vi.hoisted(() => ({
  mockGet: vi.fn(),
  mockPost: vi.fn()
}))

vi.mock('@/api/config/http', () => ({
  get: (...args: any[]) => mockGet(...args),
  post: (...args: any[]) => mockPost(...args)
}))

import verificationApi from '@/api/services/verification'

describe('Verification API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getStatus', () => {
    it('should call get with correct url', async () => {
      const mockResponse = {
        code: 200,
        data: { id: 1, status: 'PENDING', realName: '张三' }
      }
      mockGet.mockResolvedValue(mockResponse)

      const result = await verificationApi.getStatus()

      expect(mockGet).toHaveBeenCalledWith('/verification/status')
      expect(result).toEqual(mockResponse)
    })

    it('should return null when no verification', async () => {
      const mockResponse = { code: 200, data: null }
      mockGet.mockResolvedValue(mockResponse)

      const result = await verificationApi.getStatus()

      expect(result.data).toBeNull()
    })

    it('should throw error when fetch fails', async () => {
      const error = new Error('获取认证状态失败')
      mockGet.mockRejectedValue(error)

      await expect(verificationApi.getStatus()).rejects.toThrow('获取认证状态失败')
    })
  })

  describe('submit', () => {
    it('should call post with correct url and data', async () => {
      const mockResponse = {
        code: 200,
        data: { id: 1, status: 'PENDING', realName: '张三' }
      }
      mockPost.mockResolvedValue(mockResponse)

      const submitData = {
        realName: '张三',
        studentId: '2021001',
        idCard: '110101200001010011'
      }
      const result = await verificationApi.submit(submitData)

      expect(mockPost).toHaveBeenCalledWith('/verification/submit', submitData)
      expect(result).toEqual(mockResponse)
    })

    it('should throw error when submit fails', async () => {
      const error = new Error('提交认证失败')
      mockPost.mockRejectedValue(error)

      await expect(verificationApi.submit({
        realName: '张三',
        studentId: '2021001',
        idCard: '110101200001010011'
      })).rejects.toThrow('提交认证失败')
    })
  })

  describe('upload', () => {
    it('should call post with FormData', async () => {
      const mockResponse = {
        code: 200,
        data: { url: 'https://example.com/verification.jpg' }
      }
      mockPost.mockResolvedValue(mockResponse)

      const formData = new FormData()
      formData.append('file', 'verification.jpg')
      const result = await verificationApi.upload(formData)

      expect(mockPost).toHaveBeenCalledWith('/verification/upload', formData)
      expect(result).toEqual(mockResponse)
    })

    it('should throw error when upload fails', async () => {
      const error = new Error('上传失败')
      mockPost.mockRejectedValue(error)

      const formData = new FormData()
      await expect(verificationApi.upload(formData)).rejects.toThrow('上传失败')
    })
  })

  describe('resubmit', () => {
    it('should call post with correct url and data', async () => {
      const mockResponse = {
        code: 200,
        data: { id: 1, status: 'PENDING', realName: '张三' }
      }
      mockPost.mockResolvedValue(mockResponse)

      const submitData = {
        realName: '张三',
        studentId: '2021001',
        idCard: '110101200001010011'
      }
      const result = await verificationApi.resubmit(submitData)

      expect(mockPost).toHaveBeenCalledWith('/verification/resubmit', submitData)
      expect(result).toEqual(mockResponse)
    })

    it('should throw error when resubmit fails', async () => {
      const error = new Error('重新提交失败')
      mockPost.mockRejectedValue(error)

      await expect(verificationApi.resubmit({
        realName: '张三',
        studentId: '2021001',
        idCard: '110101200001010011'
      })).rejects.toThrow('重新提交失败')
    })
  })
})
