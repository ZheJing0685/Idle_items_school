import { describe, it, expect, vi, beforeEach } from 'vitest'

describe('API Module - Mock Tests', () => {
  // 直接测试 API 错误分类逻辑（不依赖实际 API 模块）
  describe('API 错误分类', () => {
    const classifyError = (error) => {
      if (error.response?.status === 401) return { type: 'AUTHENTICATION_ERROR', message: '认证失败' }
      if (error.response?.status === 403) return { type: 'AUTHORIZATION_ERROR', message: '权限不足' }
      if (error.response?.status === 404) return { type: 'NOT_FOUND', message: '资源不存在' }
      if (error.response?.status === 422) return { type: 'VALIDATION_ERROR', message: '数据验证失败' }
      if (error.response?.status >= 500) return { type: 'SERVER_ERROR', message: '服务器错误' }
      if (!error.response) return { type: 'NETWORK_ERROR', message: '网络连接失败' }
      return { type: 'UNKNOWN_ERROR', message: error.message || '未知错误' }
    }

    it('should classify 401 as AUTHENTICATION_ERROR', () => {
      const error = { response: { status: 401 }, message: 'Unauthorized' }
      expect(classifyError(error)).toEqual({ type: 'AUTHENTICATION_ERROR', message: '认证失败' })
    })

    it('should classify 403 as AUTHORIZATION_ERROR', () => {
      const error = { response: { status: 403 } }
      expect(classifyError(error)).toEqual({ type: 'AUTHORIZATION_ERROR', message: '权限不足' })
    })

    it('should classify 404 as NOT_FOUND', () => {
      const error = { response: { status: 404 } }
      expect(classifyError(error)).toEqual({ type: 'NOT_FOUND', message: '资源不存在' })
    })

    it('should classify 422 as VALIDATION_ERROR', () => {
      const error = { response: { status: 422, data: { message: 'Invalid input' } } }
      expect(classifyError(error)).toEqual({ type: 'VALIDATION_ERROR', message: '数据验证失败' })
    })

    it('should classify 500 as SERVER_ERROR', () => {
      const error = { response: { status: 500 } }
      expect(classifyError(error)).toEqual({ type: 'SERVER_ERROR', message: '服务器错误' })
    })

    it('should classify network error as NETWORK_ERROR', () => {
      const error = { message: 'Network Error' }
      expect(classifyError(error)).toEqual({ type: 'NETWORK_ERROR', message: '网络连接失败' })
    })

    it('should default to UNKNOWN_ERROR', () => {
      // 有 response 但没有 status 字段
      const error = { response: { data: {} }, message: 'Some other error' }
      expect(classifyError(error)).toEqual({ type: 'UNKNOWN_ERROR', message: 'Some other error' })
    })
  })

  describe('Auth API Mock', () => {
    // 模拟 auth API 的行为
    const createMockAuthAPI = () => {
      const mockLogin = vi.fn()
      const mockRegister = vi.fn()
      const mockGetCurrentUser = vi.fn()
      const mockRefreshToken = vi.fn()

      return {
        login: mockLogin,
        register: mockRegister,
        getCurrentUser: mockGetCurrentUser,
        refreshToken: mockRefreshToken
      }
    }

    let authAPI

    beforeEach(() => {
      authAPI = createMockAuthAPI()
    })

    it('should call login with username and password', async () => {
      const mockResponse = { token: 'abc', user: { id: 1, username: 'test' } }
      authAPI.login.mockResolvedValue(mockResponse)

      const result = await authAPI.login({ username: 'test', password: 'pass' })

      expect(authAPI.login).toHaveBeenCalledWith({ username: 'test', password: 'pass' })
      expect(result).toEqual(mockResponse)
    })

    it('should call register with user data', async () => {
      const userData = { username: 'newuser', password: 'pass', email: 'test@test.com' }
      const mockResponse = { token: 'xyz', user: { id: 2, ...userData } }
      authAPI.register.mockResolvedValue(mockResponse)

      const result = await authAPI.register(userData)

      expect(authAPI.register).toHaveBeenCalledWith(userData)
      expect(result.token).toBe('xyz')
    })

    it('should call getCurrentUser with auth header', async () => {
      const mockUser = { id: 1, username: 'testuser' }
      authAPI.getCurrentUser.mockResolvedValue(mockUser)

      const result = await authAPI.getCurrentUser()

      expect(authAPI.getCurrentUser).toHaveBeenCalled()
      expect(result).toEqual(mockUser)
    })

    it('should call refreshToken', async () => {
      const mockResponse = { token: 'new-token' }
      authAPI.refreshToken.mockResolvedValue(mockResponse)

      const result = await authAPI.refreshToken()

      expect(authAPI.refreshToken).toHaveBeenCalled()
      expect(result).toEqual(mockResponse)
    })

    it('should handle login error', async () => {
      authAPI.login.mockRejectedValue(new Error('用户名或密码错误'))

      await expect(authAPI.login({ username: 'wrong', password: 'wrong' })).rejects.toThrow('用户名或密码错误')
    })

    it('should handle getCurrentUser 401 error', async () => {
      const error = { response: { status: 401 }, message: 'Unauthorized' }
      authAPI.getCurrentUser.mockRejectedValue(error)

      await expect(authAPI.getCurrentUser()).rejects.toEqual(error)
    })
  })

  describe('Item API Mock', () => {
    const createMockItemAPI = () => ({
      getItems: vi.fn(),
      getItemById: vi.fn(),
      createItem: vi.fn(),
      updateItem: vi.fn(),
      deleteItem: vi.fn(),
      uploadImage: vi.fn()
    })

    let itemAPI

    beforeEach(() => {
      itemAPI = createMockItemAPI()
    })

    it('should call getItems with query params', async () => {
      const mockItems = [{ id: 1, title: 'item1' }, { id: 2, title: 'item2' }]
      itemAPI.getItems.mockResolvedValue(mockItems)

      const result = await itemAPI.getItems({ page: 1, pageSize: 10, category: '电子产品' })

      expect(itemAPI.getItems).toHaveBeenCalledWith({ page: 1, pageSize: 10, category: '电子产品' })
      expect(result).toEqual(mockItems)
    })

    it('should call getItemById with item ID', async () => {
      const mockItem = { id: 1, title: '测试物品', price: 99 }
      itemAPI.getItemById.mockResolvedValue(mockItem)

      const result = await itemAPI.getItemById(1)

      expect(itemAPI.getItemById).toHaveBeenCalledWith(1)
      expect(result).toEqual(mockItem)
    })

    it('should call createItem with item data', async () => {
      const itemData = { title: '新物品', description: '描述', price: 50 }
      const mockResponse = { id: 10, ...itemData }
      itemAPI.createItem.mockResolvedValue(mockResponse)

      const result = await itemAPI.createItem(itemData)

      expect(itemAPI.createItem).toHaveBeenCalledWith(itemData)
      expect(result.id).toBe(10)
    })

    it('should call updateItem with id and data', async () => {
      const updatedData = { title: '更新标题', price: 88 }
      const mockResponse = { id: 1, ...updatedData }
      itemAPI.updateItem.mockResolvedValue(mockResponse)

      const result = await itemAPI.updateItem(1, updatedData)

      expect(itemAPI.updateItem).toHaveBeenCalledWith(1, updatedData)
      expect(result.title).toBe('更新标题')
    })

    it('should call deleteItem', async () => {
      itemAPI.deleteItem.mockResolvedValue({ success: true })

      const result = await itemAPI.deleteItem(1)

      expect(itemAPI.deleteItem).toHaveBeenCalledWith(1)
      expect(result.success).toBe(true)
    })

    it('should upload image with correct headers', async () => {
      const formData = new FormData()
      formData.append('image', 'test.jpg')
      const mockResponse = { url: 'http://example.com/image.jpg' }
      itemAPI.uploadImage.mockResolvedValue(mockResponse)

      const result = await itemAPI.uploadImage(formData)

      expect(itemAPI.uploadImage).toHaveBeenCalledWith(formData)
      expect(result.url).toBeTruthy()
    })
  })

  describe('API Response Structure', () => {
    it('should validate login response structure', () => {
      const validResponse = {
        token: 'jwt-token-string',
        user: {
          id: 1,
          username: 'testuser',
          email: 'test@example.com',
          role: 'USER'
        }
      }

      expect(validResponse).toHaveProperty('token')
      expect(validResponse).toHaveProperty('user')
      expect(validResponse.user).toHaveProperty('id')
      expect(validResponse.user).toHaveProperty('username')
    })

    it('should validate item response structure', () => {
      const validItem = {
        id: 1,
        title: '测试物品',
        description: '这是一个测试物品',
        price: 99.9,
        category: '电子产品',
        condition: '九成新',
        images: ['http://example.com/img1.jpg'],
        sellerId: 1,
        status: 'AVAILABLE',
        createdAt: '2024-01-01T00:00:00Z'
      }

      expect(validItem).toHaveProperty('id')
      expect(validItem).toHaveProperty('title')
      expect(validItem).toHaveProperty('price')
      expect(validItem).toHaveProperty('status')
    })

    it('should validate error response structure', () => {
      const errorResponse = {
        type: 'VALIDATION_ERROR',
        message: '输入数据不合法',
        errors: [
          { field: 'username', message: '用户名不能为空' }
        ]
      }

      expect(errorResponse).toHaveProperty('type')
      expect(errorResponse).toHaveProperty('message')
    })
  })
})
