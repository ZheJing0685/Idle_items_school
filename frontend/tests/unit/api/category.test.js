import { describe, it, expect, vi, beforeEach } from 'vitest'
import category from '../../../src/api/services/category'

// Mock requestManager
vi.mock('../../../src/utils/network/requestManager', () => ({
  default: {
    request: vi.fn((key, fetcher, options) => fetcher())
  }
}))

// Mock API_PATHS
vi.mock('../../../src/api/config/paths', () => ({
  API_PATHS: {
    CATEGORY: {
      LIST: '/categories',
      TREE: '/categories/tree',
      SEARCH: '/categories/search',
      FEEDBACK: '/categories/feedback',
      MY_FEEDBACK: '/categories/my-feedback'
    }
  }
}))

describe('Category Service', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getCategories 应该导出函数', () => {
    expect(typeof category.getCategories).toBe('function')
  })

  it('getCategoryTree 应该导出函数', () => {
    expect(typeof category.getCategoryTree).toBe('function')
  })

  it('searchCategories 应该导出函数', () => {
    expect(typeof category.searchCategories).toBe('function')
  })

  it('submitFeedback 应该导出函数', () => {
    expect(typeof category.submitFeedback).toBe('function')
  })

  it('getMyFeedbacks 应该导出函数', () => {
    expect(typeof category.getMyFeedbacks).toBe('function')
  })
})
