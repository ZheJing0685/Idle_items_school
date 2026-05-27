import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useDictStore } from '../../../src/store/dict'

vi.mock('../../../src/api/services/dict', () => ({
  default: {
    getDictByType: vi.fn(),
    getAllDicts: vi.fn(),
  },
}))

import dictService from '../../../src/api/services/dict'

describe('Dict Store', () => {
  let store

  beforeEach(() => {
    setActivePinia(createPinia())
    store = useDictStore()
    vi.clearAllMocks()
  })

  it('fetchDictByType success', async () => {
    const mockData = [{ label: 'Active', value: 1 }]
    dictService.getDictByType.mockResolvedValue({ code: 200, data: mockData })

    const result = await store.fetchDictByType('STATUS')

    expect(result).toEqual(mockData)
    expect(store.dicts['STATUS']).toEqual(mockData)
  })

  it('fetchDictByType uses cache', async () => {
    store.dicts['STATUS'] = [{ label: 'Cached', value: 1 }]

    const result = await store.fetchDictByType('STATUS')

    expect(result).toEqual([{ label: 'Cached', value: 1 }])
    expect(dictService.getDictByType).not.toHaveBeenCalled()
  })

  it('getDictOptions returns cached options', () => {
    store.dicts['STATUS'] = [
      { label: 'Active', value: '1' },
      { label: 'Inactive', value: '0' },
    ]

    const options = store.getDictOptions('STATUS')

    expect(options).toEqual([
      { label: 'Active', value: '1' },
      { label: 'Inactive', value: '0' },
    ])
  })

  it('getDictOptions returns empty for unknown type', () => {
    const options = store.getDictOptions('UNKNOWN')
    expect(options).toEqual([])
  })

  it('clearCache clears dicts', () => {
    store.dicts['STATUS'] = [{ label: 'Test', value: 1 }]
    store.clearCache()
    expect(store.dicts).toEqual({})
  })
})
