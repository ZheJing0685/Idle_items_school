import { describe, it, expect, vi, beforeEach } from 'vitest'
import ErrorHandler from '../../../src/utils/error/errorHandler'

vi.mock('element-plus', () => ({
  ElMessage: vi.fn(),
  ElMessageBox: { alert: vi.fn() },
}))

vi.mock('../../../src/router', () => ({
  default: { push: vi.fn() },
}))

import { ElMessage } from 'element-plus'

describe('ErrorHandler', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('handle returns error', () => {
    const error = new Error('Something went wrong')
    const result = ErrorHandler.handle(error)
    expect(result).toBe(error)
  })

  it('handle returns null for null error', () => {
    const result = ErrorHandler.handle(null)
    expect(result).toBeUndefined()
  })

  it('handle silent mode does not show message', () => {
    const error = new Error('Error')
    ErrorHandler.handle(error, { silent: true })
    expect(ElMessage).not.toHaveBeenCalled()
  })

  it('showErrorMessage displays error', () => {
    ErrorHandler.showErrorMessage({ type: 'SERVER_ERROR', message: 'Server error' })
    expect(ElMessage).toHaveBeenCalled()
  })

  it('isAuthError detects auth errors', () => {
    const error = { response: { status: 401 } }
    expect(ErrorHandler.isAuthError(error)).toBe(true)
  })

  it('isNetworkError detects network errors', () => {
    const error = { code: 'ERR_NETWORK' }
    expect(ErrorHandler.isNetworkError(error)).toBe(true)
  })

  it('isNetworkError detects timeout errors', () => {
    const error = { code: 'ECONNABORTED' }
    expect(ErrorHandler.isNetworkError(error)).toBe(true)
  })

  it('getErrorMessage returns correct messages', () => {
    expect(ErrorHandler.getErrorMessage('NETWORK_ERROR')).toContain('网络')
    expect(ErrorHandler.getErrorMessage('TIMEOUT_ERROR')).toContain('超时')
    expect(ErrorHandler.getErrorMessage('SERVER_ERROR')).toContain('服务器')
  })
})
