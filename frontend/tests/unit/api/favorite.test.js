import { describe, it, expect, vi, beforeEach } from 'vitest'
import favorite from '../../../src/api/services/favorite'

describe('Favorite Service', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getFavorites 应该导出函数', () => {
    expect(typeof favorite.getFavorites).toBe('function')
  })

  it('addFavorite 应该导出函数', () => {
    expect(typeof favorite.addFavorite).toBe('function')
  })

  it('removeFavorite 应该导出函数', () => {
    expect(typeof favorite.removeFavorite).toBe('function')
  })

  it('checkFavorite 应该导出函数', () => {
    expect(typeof favorite.checkFavorite).toBe('function')
  })
})
