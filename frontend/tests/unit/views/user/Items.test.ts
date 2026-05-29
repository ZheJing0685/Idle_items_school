import { describe, it, expect, vi, beforeEach, beforeAll } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { elementPlusStubs, lucideIconsStub, routerMock } from '../../helpers/elementPlusMock'

const mockGetItems = vi.fn()
const mockDeleteItem = vi.fn()
const mockOnShelf = vi.fn()
const mockOffShelf = vi.fn()
const mockPreloadCommonDicts = vi.fn()
const mockElMessageBoxConfirm = vi.fn().mockResolvedValue(true)

vi.mock('@/api', () => ({
  default: {
    user: {
      getItems: (...args: any[]) => mockGetItems(...args),
    },
    item: {
      deleteItem: (...args: any[]) => mockDeleteItem(...args),
      onShelf: (...args: any[]) => mockOnShelf(...args),
      offShelf: (...args: any[]) => mockOffShelf(...args),
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
  ElMessageBox: {
    confirm: (...args: any[]) => mockElMessageBoxConfirm(...args),
  },
}))

vi.mock('@/components/user/PageHeader.vue', () => ({
  default: {
    template: '<div class="page-header-stub"><slot name="action" /></div>',
    props: ['title', 'subtitle'],
  }
}))

vi.mock('@/components/user/FilterTabs.vue', () => ({
  default: {
    template: '<div class="filter-tabs-stub" />',
    props: ['tabs', 'modelValue'],
  }
}))

vi.mock('@/components/user/ItemCard.vue', () => ({
  default: {
    template: '<div class="item-card-stub"><slot name="actions" /></div>',
    props: ['id', 'title', 'price', 'coverImage', 'status', 'statusText', 'viewCount', 'time'],
  }
}))

vi.mock('@/components/user/EmptyState.vue', () => ({
  default: {
    template: '<div class="empty-state-stub"><slot name="action" /></div>',
    props: ['title', 'description'],
  }
}))

vi.mock('@element-plus/icons-vue', () => ({
  Plus: { template: '<span />' },
}))

let Items: any
beforeAll(async () => {
  const mod = await import('@/views/user/Items.vue')
  Items = mod.default
})

beforeEach(() => {
  setActivePinia(createPinia())
  vi.clearAllMocks()
  mockPreloadCommonDicts.mockResolvedValue(undefined)
  mockGetItems.mockResolvedValue({
    code: 200,
    data: {
      content: [
        { id: '1', title: '测试物品', price: 99, coverImage: '', status: 'ON_SALE', viewCount: 10, createdAt: '2024-01-15' }
      ],
      totalElements: 1
    }
  })
  mockDeleteItem.mockResolvedValue({ code: 200 })
  mockOnShelf.mockResolvedValue({ code: 200 })
  mockOffShelf.mockResolvedValue({ code: 200 })
  mockElMessageBoxConfirm.mockResolvedValue(true)
})

const mountItems = (options = {}) => {
  return mount(Items, {
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

describe('Items.vue 我的发布页面', () => {
  describe('组件渲染', () => {
    it('应该渲染物品发布页面', () => {
      const wrapper = mountItems()
      expect(wrapper.find('.items-page').exists()).toBe(true)
    })

    it('数据加载完成后loading应为false', async () => {
      const wrapper = mountItems()
      await vi.waitFor(() => {
        expect(mockGetItems).toHaveBeenCalled()
      })
      expect(wrapper.vm.loading).toBe(false)
    })

    it('应该包含分页组件', async () => {
      const wrapper = mountItems()
      await vi.waitFor(() => {
        expect(mockGetItems).toHaveBeenCalled()
      })
      expect(wrapper.find('.pagination').exists()).toBe(true)
    })

    it('应该显示filterTabs配置', () => {
      const wrapper = mountItems()
      expect(wrapper.vm.filterTabs.length).toBe(5)
      expect(wrapper.vm.filterTabs[0].label).toBe('全部')
    })
  })

  describe('数据加载', () => {
    it('应在挂载时加载物品列表', async () => {
      mountItems()
      await vi.waitFor(() => {
        expect(mockGetItems).toHaveBeenCalled()
      })
    })

    it('应在挂载时预加载字典', async () => {
      mountItems()
      await vi.waitFor(() => {
        expect(mockPreloadCommonDicts).toHaveBeenCalled()
      })
    })

    it('加载失败时应显示错误信息', async () => {
      mockGetItems.mockRejectedValue(new Error('网络错误'))
      const wrapper = mountItems()
      await vi.waitFor(() => {
        expect(mockGetItems).toHaveBeenCalled()
      })
      expect(wrapper.vm.error).toBe('网络错误，请稍后重试')
    })
  })

  describe('方法存在性', () => {
    it('应该有loadItems方法', () => {
      const wrapper = mountItems()
      expect(typeof wrapper.vm.loadItems).toBe('function')
    })

    it('应该有deleteItem方法', () => {
      const wrapper = mountItems()
      expect(typeof wrapper.vm.deleteItem).toBe('function')
    })

    it('应该有toggleShelf方法', () => {
      const wrapper = mountItems()
      expect(typeof wrapper.vm.toggleShelf).toBe('function')
    })

    it('应该有canEdit方法', () => {
      const wrapper = mountItems()
      expect(typeof wrapper.vm.canEdit).toBe('function')
    })

    it('应该有canToggleShelf方法', () => {
      const wrapper = mountItems()
      expect(typeof wrapper.vm.canToggleShelf).toBe('function')
    })

    it('应该有canDelete方法', () => {
      const wrapper = mountItems()
      expect(typeof wrapper.vm.canDelete).toBe('function')
    })
  })

  describe('业务逻辑', () => {
    it('已售出物品不可编辑', () => {
      const wrapper = mountItems()
      expect(wrapper.vm.canEdit({ status: 'SOLD' })).toBe(false)
    })

    it('在售物品可编辑', () => {
      const wrapper = mountItems()
      expect(wrapper.vm.canEdit({ status: 'ON_SALE' })).toBe(true)
    })

    it('审核中物品不可上下架', () => {
      const wrapper = mountItems()
      expect(wrapper.vm.canToggleShelf({ status: 'PENDING' })).toBe(false)
    })

    it('已售出物品不可上下架', () => {
      const wrapper = mountItems()
      expect(wrapper.vm.canToggleShelf({ status: 'SOLD' })).toBe(false)
    })

    it('已下架物品可上下架', () => {
      const wrapper = mountItems()
      expect(wrapper.vm.canToggleShelf({ status: 'OFF_SHELF' })).toBe(true)
    })

    it('审核中和已下架物品可删除', () => {
      const wrapper = mountItems()
      expect(wrapper.vm.canDelete({ status: 'PENDING' })).toBe(true)
      expect(wrapper.vm.canDelete({ status: 'OFF_SHELF' })).toBe(true)
    })

    it('在售物品不可删除', () => {
      const wrapper = mountItems()
      expect(wrapper.vm.canDelete({ status: 'ON_SALE' })).toBe(false)
    })
  })

  describe('导航功能', () => {
    it('goToItemDetail应跳转到物品详情页', () => {
      const wrapper = mountItems()
      wrapper.vm.goToItemDetail('100')
      expect(routerMock.push).toHaveBeenCalledWith('/item/100')
    })

    it('editItem应跳转到发布页编辑模式', () => {
      const wrapper = mountItems()
      wrapper.vm.editItem('100')
      expect(routerMock.push).toHaveBeenCalledWith('/publish?edit=100')
    })
  })

  describe('状态筛选', () => {
    it('handleStatusChange应重置页码并重新加载', async () => {
      const wrapper = mountItems()
      await vi.waitFor(() => {
        expect(mockGetItems).toHaveBeenCalled()
      })
      mockGetItems.mockClear()
      wrapper.vm.handleStatusChange()
      expect(wrapper.vm.currentPage).toBe(1)
      await vi.waitFor(() => {
        expect(mockGetItems).toHaveBeenCalled()
      })
    })
  })
})
