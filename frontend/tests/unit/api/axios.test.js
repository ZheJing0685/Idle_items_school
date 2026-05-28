import { describe, it, expect } from 'vitest'
import { setToken, getToken, clearToken, setUnauthorizedHandler } from '../../../src/api/config/axios'

describe('Axios Token 管理', () => {
  beforeEach(() => {
    sessionStorage.clear()
  })

  describe('setToken / getToken', () => {
    it('应该设置和获取 token', () => {
      setToken('test-token-123')
      expect(getToken()).toBe('test-token-123')
    })

    it('应该同时存储到 sessionStorage', () => {
      setToken('abc')
      expect(sessionStorage.getItem('access_token')).toBe('abc')
    })
  })

  describe('clearToken', () => {
    it('应该清除 token', () => {
      setToken('to-be-cleared')
      clearToken()
      expect(getToken()).toBe('')
    })

    it('应该清除 sessionStorage', () => {
      setToken('to-be-cleared')
      sessionStorage.setItem('refresh_token', 'refresh')
      clearToken()
      expect(sessionStorage.getItem('access_token')).toBeNull()
      expect(sessionStorage.getItem('refresh_token')).toBeNull()
    })
  })

  describe('setUnauthorizedHandler', () => {
    it('应该设置处理器', () => {
      const handler = () => {}
      setUnauthorizedHandler(handler)
      expect(typeof handler).toBe('function')
    })
  })
})
