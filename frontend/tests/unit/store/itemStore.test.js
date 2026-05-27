import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useItemStore } from '../../../src/store/modules/item'

vi.mock('../../../src/api', () => ({
  default: {
    item: {
      getItems: vi.fn(),
      getHotItems: vi.fn(),
      searchItems: vi.fn(),
      getItem: vi.fn(),
      createItem: vi.fn(),
      updateItem: vi.fn(),
      offShelf: vi.fn(),
      uploadImage: vi.fn(),
    },
    user: {
      getItems: vi.fn(),
    },
    clearCache: vi.fn(),
  },
}))

vi.mock('../../../src/utils/error', () => ({
  ErrorHandler: { handle: vi.fn() },
}))

import api from '../../../src/api'

describe('Item Store', () => {
  let store

  beforeEach(() => {
    setActivePinia(createPinia())
    store = useItemStore()
    vi.clearAllMocks()
  })

  it('fetchItems success', async () => {
    const mockResponse = { code: 200, data: { content: [{ id: 1 }], totalElements: 1 } }
    api.item.getItems.mockResolvedValue(mockResponse)

    const result = await store.fetchItems({ page: 1, size: 10 })

    expect(result).toEqual(mockResponse)
    expect(store.items).toEqual([{ id: 1 }])
    expect(store.total).toBe(1)
    expect(store.loading).toBe(false)
  })

  it('fetchItems error', async () => {
    api.item.getItems.mockRejectedValue(new Error('Network error'))

    await expect(store.fetchItems()).rejects.toThrow()
    expect(store.loading).toBe(false)
  })

  it('fetchHotItems success', async () => {
    const mockResponse = { code: 200, data: [{ id: 1, title: 'Hot Item' }] }
    api.item.getHotItems.mockResolvedValue(mockResponse)

    const result = await store.fetchHotItems()

    expect(result).toEqual(mockResponse)
    expect(store.hotItems).toEqual([{ id: 1, title: 'Hot Item' }])
  })

  it('fetchHotItems error returns null', async () => {
    api.item.getHotItems.mockRejectedValue(new Error('Error'))

    const result = await store.fetchHotItems()

    expect(result).toBeNull()
  })

  it('searchItems success', async () => {
    const mockResponse = { code: 200, data: { content: [{ id: 2 }], totalElements: 1 } }
    api.item.searchItems.mockResolvedValue(mockResponse)

    const result = await store.searchItems('test')

    expect(store.searchResults).toEqual([{ id: 2 }])
    expect(store.searchTotal).toBe(1)
    expect(store.loading).toBe(false)
  })

  it('searchItems error', async () => {
    api.item.searchItems.mockRejectedValue(new Error('Error'))

    await expect(store.searchItems('test')).rejects.toThrow()
    expect(store.loading).toBe(false)
  })

  it('getItem success', async () => {
    const mockResponse = { code: 200, data: { id: 1, title: 'Item' } }
    api.item.getItem.mockResolvedValue(mockResponse)

    const result = await store.getItem(1)

    expect(store.currentItem).toEqual({ id: 1, title: 'Item' })
  })

  it('getItem error', async () => {
    api.item.getItem.mockRejectedValue(new Error('Error'))

    await expect(store.getItem(1)).rejects.toThrow()
  })

  it('createItem success', async () => {
    const mockResponse = { code: 200, data: { id: 1 } }
    api.item.createItem.mockResolvedValue(mockResponse)

    const result = await store.createItem({ title: 'New Item' })

    expect(result).toEqual(mockResponse)
    expect(store.loading).toBe(false)
  })

  it('createItem error', async () => {
    api.item.createItem.mockRejectedValue(new Error('Error'))

    await expect(store.createItem({})).rejects.toThrow()
    expect(store.loading).toBe(false)
  })

  it('updateItem success', async () => {
    const mockResponse = { code: 200, data: { id: 1 } }
    api.item.updateItem.mockResolvedValue(mockResponse)

    await store.updateItem(1, { title: 'Updated' })

    expect(store.loading).toBe(false)
  })

  it('offShelfItem success', async () => {
    const mockResponse = { code: 200, data: { id: 1 } }
    api.item.offShelf.mockResolvedValue(mockResponse)

    await store.offShelfItem(1)

    expect(store.loading).toBe(false)
  })

  it('fetchUserItems success', async () => {
    const mockResponse = { code: 200, data: { content: [{ id: 1 }], totalElements: 1 } }
    api.user.getItems.mockResolvedValue(mockResponse)

    await store.fetchUserItems('ON_SALE')

    expect(store.userItems).toEqual([{ id: 1 }])
    expect(store.userItemsTotal).toBe(1)
    expect(store.loading).toBe(false)
  })

  it('uploadImage success', async () => {
    const mockResponse = { code: 200, data: { url: 'http://example.com/img.jpg' } }
    api.item.uploadImage.mockResolvedValue(mockResponse)

    const result = await store.uploadImage(new FormData())

    expect(result).toEqual(mockResponse)
    expect(store.loading).toBe(false)
  })
})
