import { describe, it, expect, beforeEach } from 'vitest'
import storage from '../../../src/utils/storage'

describe('Storage 源码测试', () => {
  let store

  beforeEach(() => {
    localStorage.clear()
    store = storage('test')
  })

  describe('基本操作', () => {
    it('应该存储和获取值', () => {
      store.set('key', 'value')
      expect(store.get('key')).toBe('value')
    })

    it('应该存储对象', () => {
      const obj = { name: 'test', age: 18 }
      store.set('obj', obj)
      expect(store.get('obj')).toEqual(obj)
    })

    it('应该删除值', () => {
      store.set('key', 'value')
      store.remove('key')
      expect(store.get('key')).toBeNull()
    })

    it('应该清空前缀下的所有值', () => {
      store.set('a', 1)
      store.set('b', 2)
      store.clear()
      expect(store.get('a')).toBeNull()
      expect(store.get('b')).toBeNull()
    })

    it('获取不存在的键应返回 null', () => {
      expect(store.get('nonexistent')).toBeNull()
    })
  })

  describe('getAll', () => {
    it('应该返回所有带前缀的值', () => {
      store.set('x', 10)
      store.set('y', 20)
      const all = store.getAll()
      expect(all.x).toBe(10)
      expect(all.y).toBe(20)
    })
  })

  describe('has', () => {
    it('存在时应返回 true', () => {
      store.set('exists', true)
      expect(store.has('exists')).toBe(true)
    })

    it('不存在时应返回 false', () => {
      expect(store.has('nope')).toBe(false)
    })
  })

  describe('setWithExpiry / getWithExpiry', () => {
    it('应该存储带过期时间的值', () => {
      store.setWithExpiry('token', 'abc', 60000)
      expect(store.getWithExpiry('token')).toBe('abc')
    })

    it('过期后应返回 null', () => {
      store.setWithExpiry('token', 'abc', 1)
      // 等待 2ms 使其过期
      const start = Date.now()
      while (Date.now() - start < 5) { /* busy wait */ }
      expect(store.getWithExpiry('token')).toBeNull()
    })
  })

  describe('命名空间隔离', () => {
    it('不同命名空间应互不干扰', () => {
      const storeA = storage('app')
      const storeB = storage('other')
      storeA.set('key', 'fromA')
      storeB.set('key', 'fromB')
      expect(storeA.get('key')).toBe('fromA')
      expect(storeB.get('key')).toBe('fromB')
    })
  })

  describe('错误处理', () => {
    it('set 失败应返回 false', () => {
      // 模拟 localStorage 满的情况
      const original = Storage.prototype.setItem
      Storage.prototype.setItem = () => { throw new Error('full') }
      const result = store.set('key', 'value')
      expect(result).toBe(false)
      Storage.prototype.setItem = original
    })

    it('get 异常应返回 null', () => {
      const original = Storage.prototype.getItem
      Storage.prototype.getItem = () => { throw new Error('err') }
      const result = store.get('key')
      expect(result).toBeNull()
      Storage.prototype.getItem = original
    })
  })
})
