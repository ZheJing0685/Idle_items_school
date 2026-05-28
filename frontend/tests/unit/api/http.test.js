import { describe, it, expect } from 'vitest'
import { get, post, put, del, getBlob } from '../../../src/api/config/http'

describe('HTTP 工具函数', () => {
  it('get 应该是函数', () => {
    expect(typeof get).toBe('function')
  })

  it('post 应该是函数', () => {
    expect(typeof post).toBe('function')
  })

  it('put 应该是函数', () => {
    expect(typeof put).toBe('function')
  })

  it('del 应该是函数', () => {
    expect(typeof del).toBe('function')
  })

  it('getBlob 应该是函数', () => {
    expect(typeof getBlob).toBe('function')
  })
})
