import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useCartStore } from '../../../src/store/modules/cart'

// Mock storage
const mockStorageInstance = {
  get: vi.fn().mockReturnValue([]),
  set: vi.fn(),
  remove: vi.fn()
}

vi.mock('../../../src/utils/storage', () => ({
  default: vi.fn(() => mockStorageInstance)
}))

describe('Cart Store', () => {
  let store

  beforeEach(async () => {
    vi.clearAllMocks()
    mockStorageInstance.get.mockReturnValue([])
    setActivePinia(createPinia())
    store = useCartStore()
  })

  describe('初始状态', () => {
    it('应该有正确的初始状态', () => {
      expect(store.items).toEqual([])
      expect(store.totalItems).toBe(0)
      expect(store.totalPrice).toBe(0)
    })
  })

  describe('addItem', () => {
    it('应该添加新商品到购物车', () => {
      const item = { id: 1, name: '测试商品', price: 100 }

      store.addItem(item)

      expect(store.items).toHaveLength(1)
      expect(store.items[0]).toEqual({ ...item, quantity: 1 })
      expect(mockStorageInstance.set).toHaveBeenCalled()
    })

    it('应该增加已有商品的数量', () => {
      const item = { id: 1, name: '测试商品', price: 100 }
      store.items = [{ ...item, quantity: 1 }]

      store.addItem(item)

      expect(store.items).toHaveLength(1)
      expect(store.items[0].quantity).toBe(2)
    })
  })

  describe('removeItem', () => {
    it('应该从购物车移除商品', () => {
      store.items = [
        { id: 1, name: '商品1', price: 100, quantity: 1 },
        { id: 2, name: '商品2', price: 200, quantity: 1 }
      ]

      store.removeItem(1)

      expect(store.items).toHaveLength(1)
      expect(store.items[0].id).toBe(2)
    })
  })

  describe('updateQuantity', () => {
    it('应该更新商品数量', () => {
      store.items = [{ id: 1, name: '商品1', price: 100, quantity: 1 }]

      store.updateQuantity(1, 5)

      expect(store.items[0].quantity).toBe(5)
    })

    it('应该忽略无效数量', () => {
      store.items = [{ id: 1, name: '商品1', price: 100, quantity: 1 }]

      store.updateQuantity(1, 0)

      expect(store.items[0].quantity).toBe(1)
    })
  })

  describe('clear', () => {
    it('应该清空购物车', () => {
      store.items = [
        { id: 1, name: '商品1', price: 100, quantity: 1 },
        { id: 2, name: '商品2', price: 200, quantity: 2 }
      ]

      store.clear()

      expect(store.items).toEqual([])
      expect(mockStorageInstance.set).toHaveBeenCalledWith('items', [])
    })
  })

  describe('计算属性', () => {
    it('totalItems 应该返回商品总数', () => {
      store.items = [
        { id: 1, quantity: 2 },
        { id: 2, quantity: 3 }
      ]

      expect(store.totalItems).toBe(2)
    })

    it('totalPrice 应该返回总价格', () => {
      store.items = [
        { id: 1, price: 100, quantity: 2 },
        { id: 2, price: 200, quantity: 1 }
      ]

      expect(store.totalPrice).toBe(400)
    })
  })

  describe('syncFromStorage', () => {
    it('应该从存储同步数据', () => {
      const mockItems = [{ id: 1, name: '同步商品', price: 50, quantity: 1 }]
      mockStorageInstance.get.mockReturnValue(mockItems)

      store.syncFromStorage()

      expect(store.items).toEqual(mockItems)
    })
  })
})
