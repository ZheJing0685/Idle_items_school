import { describe, it, expect } from 'vitest'
import { ErrorCode, ErrorType, classifyError } from '../../../src/utils/error/errorTypes'

describe('ErrorTypes', () => {
  describe('ErrorCode', () => {
    it('应该有正确的错误码', () => {
      expect(ErrorCode.SUCCESS).toBe(200)
      expect(ErrorCode.CREATED).toBe(201)
      expect(ErrorCode.BAD_REQUEST).toBe(400)
      expect(ErrorCode.UNAUTHORIZED).toBe(401)
      expect(ErrorCode.FORBIDDEN).toBe(403)
      expect(ErrorCode.NOT_FOUND).toBe(404)
      expect(ErrorCode.CONFLICT).toBe(409)
      expect(ErrorCode.VALIDATION_ERROR).toBe(400)
      expect(ErrorCode.INTERNAL_SERVER_ERROR).toBe(500)
    })
  })

  describe('ErrorType', () => {
    it('应该有所有错误类型', () => {
      expect(ErrorType.SUCCESS).toBe('SUCCESS')
      expect(ErrorType.NETWORK_ERROR).toBe('NETWORK_ERROR')
      expect(ErrorType.TIMEOUT_ERROR).toBe('TIMEOUT_ERROR')
      expect(ErrorType.VALIDATION_ERROR).toBe('VALIDATION_ERROR')
      expect(ErrorType.AUTHENTICATION_ERROR).toBe('AUTHENTICATION_ERROR')
      expect(ErrorType.AUTHORIZATION_ERROR).toBe('AUTHORIZATION_ERROR')
      expect(ErrorType.NOT_FOUND_ERROR).toBe('NOT_FOUND_ERROR')
      expect(ErrorType.CONFLICT_ERROR).toBe('CONFLICT_ERROR')
      expect(ErrorType.SERVER_ERROR).toBe('SERVER_ERROR')
      expect(ErrorType.CLIENT_ERROR).toBe('CLIENT_ERROR')
      expect(ErrorType.UNKNOWN_ERROR).toBe('UNKNOWN_ERROR')
    })
  })

  describe('classifyError', () => {
    it('null 错误应该返回 UNKNOWN_ERROR', () => {
      expect(classifyError(null)).toBe(ErrorType.UNKNOWN_ERROR)
    })

    it('undefined 错误应该返回 UNKNOWN_ERROR', () => {
      expect(classifyError(undefined)).toBe(ErrorType.UNKNOWN_ERROR)
    })

    it('超时错误应该返回 TIMEOUT_ERROR', () => {
      const error = { code: 'ECONNABORTED', message: 'timeout' }
      expect(classifyError(error)).toBe(ErrorType.TIMEOUT_ERROR)
    })

    it('网络错误应该返回 NETWORK_ERROR', () => {
      const error = { code: 'ERR_NETWORK', message: 'Network Error' }
      expect(classifyError(error)).toBe(ErrorType.NETWORK_ERROR)
    })

    it('401 错误应该返回 AUTHENTICATION_ERROR', () => {
      const error = { response: { status: 401 } }
      expect(classifyError(error)).toBe(ErrorType.AUTHENTICATION_ERROR)
    })

    it('403 错误应该返回 AUTHORIZATION_ERROR', () => {
      const error = { response: { status: 403 } }
      expect(classifyError(error)).toBe(ErrorType.AUTHORIZATION_ERROR)
    })

    it('404 错误应该返回 NOT_FOUND_ERROR', () => {
      const error = { response: { status: 404 } }
      expect(classifyError(error)).toBe(ErrorType.NOT_FOUND_ERROR)
    })

    it('409 错误应该返回 CONFLICT_ERROR', () => {
      const error = { response: { status: 409 } }
      expect(classifyError(error)).toBe(ErrorType.CONFLICT_ERROR)
    })

    it('500 错误应该返回 SERVER_ERROR', () => {
      const error = { response: { status: 500 } }
      expect(classifyError(error)).toBe(ErrorType.SERVER_ERROR)
    })

    it('4xx 错误应该返回 CLIENT_ERROR', () => {
      const error = { response: { status: 422 } }
      expect(classifyError(error)).toBe(ErrorType.CLIENT_ERROR)
    })

    it('后端 401 错误码应该返回 AUTHENTICATION_ERROR', () => {
      const error = { response: { data: { code: 401 } } }
      expect(classifyError(error)).toBe(ErrorType.AUTHENTICATION_ERROR)
    })

    it('后端 403 错误码应该返回 AUTHORIZATION_ERROR', () => {
      const error = { response: { data: { code: 403 } } }
      expect(classifyError(error)).toBe(ErrorType.AUTHORIZATION_ERROR)
    })

    it('后端 404 错误码应该返回 NOT_FOUND_ERROR', () => {
      const error = { response: { data: { code: 404 } } }
      expect(classifyError(error)).toBe(ErrorType.NOT_FOUND_ERROR)
    })

    it('后端 5xx 错误码应该返回 SERVER_ERROR', () => {
      const error = { response: { data: { code: 500 } } }
      expect(classifyError(error)).toBe(ErrorType.SERVER_ERROR)
    })

    it('未知错误应该返回 UNKNOWN_ERROR', () => {
      const error = { message: 'something' }
      expect(classifyError(error)).toBe(ErrorType.UNKNOWN_ERROR)
    })
  })
})
