// @ts-nocheck
import { describe, it, expect, vi, beforeEach, beforeAll } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { elementPlusStubs, lucideIconsStub, routerMock } from '../../helpers/elementPlusMock'

const mockGetFavorites = vi.fn()
const mockRemoveFavorite = vi.fn()
const mockPreloadCommonDicts = vi.fn()

vi.mock('@/api', () => ({
  default: {
    favorite: {
      getFavorites: (...args: any[]) => mockGetFavorites(...args),
      removeFavorite: (...args: any[]) => mockRemoveFavorite(...args),
    }
  }
}))

vi.mock('@/store/dict', () => ({
  useDictStore: () => ({
    getDictLabel: vi.fn((type: string, value: string) => value),
    preloadCommonDicts: mockPreloadCommonDicts,
  })
}))

vi.mock('vue-router', () => ({
  useRouter: () => routerMock,
}))

vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn(),
  },
}))

vi.mock('@/components/user/PageHeader.vue', () => ({
  default: {
    template: '<div class="page-header-stub"><slot /></div>',
    props: ['title', 'subtitle'],
  }
}))

vi.mock('@/components/user/ItemCard.vue', () => ({
  default: {
    template: '<div class="item-card-stub"><slot /></div>',
    props: ['id', 'title', 'price', 'coverImage', 'status', 'statusText', 'time'],
  }
}))

vi.mock('@/components/user/EmptyState.vue', () => ({
  default: {
    template: '<div class="empty-state-stub"><slot name="action" /></div>',
    props: ['title', 'description'],
  }
}))

let Favorites: any
beforeAll(async () => {
  const mod = await import('@/views/user/Favorites.vue')
  Favorites = mod.default
})

beforeEach(() => {
  setActivePinia(createPinia())
  vi.clearAllMocks()
  mockPreloadCommonDicts.mockResolvedValue(undefined)
  mockGetFavorites.mockResolvedValue({
    code: 200,
    data: {
      content: [
        { id: '1', itemId: '100', title: '测试物品', price: 99, coverImage: '', status: 'ON_SALE', createdAt: '2024-01-15' }
      ],
      totalElements: 1
    }
  })
  mockRemoveFavorite.mockResolvedValue({ code: 200 })
})

const mountFavorites = (options = {}) => {
  return mount(Favorites, {
    global: {
      plugins: [createPinia()],
      stubs: {
        ...elementPlusStubs,
        ...lucideIconsStub,
      },
    },
    ...options,
  })
}

describe('Favorites.vue 我的收藏页面', () => {
  describe('组件渲染', () => {
    it('应该渲染收藏页面', () => {
      const wrapper = mountFavorites()
      expect(wrapper.find('.favorites-page').exists()).toBe(true)
    })

    it('数据加载完成后loading应为false', async () => {
      const wrapper = mountFavorites()
      await vi.waitFor(() => {
        expect(mockGetFavorites).toHaveBeenCalled()
      })
      expect(wrapper.vm.loading).toBe(false)
    })

    it('应该显示分页组件', async () => {
      const wrapper = mountFavorites()
      await vi.waitFor(() => {
        expect(mockGetFavorites).toHaveBeenCalled()
      })
      expect(wrapper.find('.pagination').exists()).toBe(true)
    })
  })

  describe('数据加载', () => {
    it('应在挂载时加载收藏列表', async () => {
      mountFavorites()
      await vi.waitFor(() => {
        expect(mockGetFavorites).toHaveBeenCalled()
      })
    })

    it('应在挂载时预加载字典', async () => {
      mountFavorites()
      await vi.waitFor(() => {
        expect(mockPreloadCommonDicts).toHaveBeenCalled()
      })
    })

    it('应正确处理加载完成状态', async () => {
      const wrapper = mountFavorites()
      await vi.waitFor(() => {
        expect(mockGetFavorites).toHaveBeenCalled()
      })
      expect(wrapper.vm.loading).toBe(false)
    })

    it('加载失败时应显示错误信息', async () => {
      mockGetFavorites.mockRejectedValue(new Error('网络错误'))
      const wrapper = mountFavorites()
      await vi.waitFor(() => {
        expect(mockGetFavorites).toHaveBeenCalled()
      })
      expect(wrapper.vm.error).toBe('网络错误，请稍后重试')
    })
  })

  describe('方法存在性', () => {
    it('应该有loadFavorites方法', () => {
      const wrapper = mountFavorites()
      expect(typeof wrapper.vm.loadFavorites).toBe('function')
    })

    it('应该有removeFavorite方法', () => {
      const wrapper = mountFavorites()
      expect(typeof wrapper.vm.removeFavorite).toBe('function')
    })

    it('应该有goToItemDetail方法', () => {
      const wrapper = mountFavorites()
      expect(typeof wrapper.vm.goToItemDetail).toBe('function')
    })

    it('应该有formatDate方法', () => {
      const wrapper = mountFavorites()
      expect(typeof wrapper.vm.formatDate).toBe('function')
    })
  })

  describe('分页功能', () => {
    it('handleSizeChange应重置页码并重新加载', async () => {
      const wrapper = mountFavorites()
      await vi.waitFor(() => {
        expect(mockGetFavorites).toHaveBeenCalled()
      })
      mockGetFavorites.mockClear()
      wrapper.vm.handleSizeChange(24)
      expect(wrapper.vm.pageSize).toBe(24)
      expect(wrapper.vm.currentPage).toBe(1)
      await vi.waitFor(() => {
        expect(mockGetFavorites).toHaveBeenCalled()
      })
    })

    it('handleCurrentChange应更新页码并重新加载', async () => {
      const wrapper = mountFavorites()
      await vi.waitFor(() => {
        expect(mockGetFavorites).toHaveBeenCalled()
      })
      mockGetFavorites.mockClear()
      wrapper.vm.handleCurrentChange(2)
      expect(wrapper.vm.currentPage).toBe(2)
      await vi.waitFor(() => {
        expect(mockGetFavorites).toHaveBeenCalled()
      })
    })
  })

  describe('导航功能', () => {
    it('goToItemDetail应跳转到物品详情页', () => {
      const wrapper = mountFavorites()
      wrapper.vm.goToItemDetail('100')
      expect(routerMock.push).toHaveBeenCalledWith('/item/100')
    })
  })
})
