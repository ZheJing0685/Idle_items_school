// @ts-nocheck
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ItemManagement from '@/views/admin/ItemManagement.vue'
import { elementPlusStubs, lucideIconsStub } from '../../helpers/elementPlusMock'

// Mock API
const mockGetItems = vi.fn()
const mockGetItemStats = vi.fn()
const mockGetCategories = vi.fn()
const mockApproveItem = vi.fn()
const mockRejectItem = vi.fn()
const mockOffShelfItem = vi.fn()
const mockDeleteItem = vi.fn()
const mockBatchOffShelf = vi.fn()
const mockExportItems = vi.fn()

vi.mock('@/api', () => ({
  default: {
    admin: {
      items: {
        getItems: (...args: any[]) => mockGetItems(...args),
        getItemStats: (...args: any[]) => mockGetItemStats(...args),
        approveItem: (...args: any[]) => mockApproveItem(...args),
        rejectItem: (...args: any[]) => mockRejectItem(...args),
        offShelfItem: (...args: any[]) => mockOffShelfItem(...args),
        deleteItem: (...args: any[]) => mockDeleteItem(...args),
        batchOffShelf: (...args: any[]) => mockBatchOffShelf(...args),
        exportItems: (...args: any[]) => mockExportItems(...args),
      },
      categories: {
        getCategories: (...args: any[]) => mockGetCategories(...args),
      },
    },
  },
}))

// Mock dict store
vi.mock('@/store/dict.js', () => ({
  useDictStore: () => ({
    preloadCommonDicts: vi.fn().mockResolvedValue(undefined),
    getDictLabel: vi.fn().mockImplementation((type, value) => {
      const map: Record<string, string> = {
        ITEM_CONDITION: { NEW: '全新', LIKE_NEW: '几乎全新', GOOD: '良好', FAIR: '一般', POOR: '较差' }[value] || value,
        ITEM_STATUS: { DRAFT: '草稿', PENDING: '待审核', ON_SALE: '在售', SOLD: '已售', OFF_SHELF: '已下架', REJECTED: '已驳回' }[value] || value,
        DELIVERY_METHOD: { PICKUP: '自取', DELIVERY: '配送' }[value] || value,
      }
      return (map as any)[type] || value
    }),
  }),
}))

// Mock Element Plus
vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn(),
  },
  ElMessageBox: {
    confirm: vi.fn().mockResolvedValue(undefined),
    prompt: vi.fn().mockResolvedValue({ value: '驳回原因' }),
  },
}))

// Mock lucide icons
const lucideIcons = [
  'Package', 'Clock', 'DollarSign', 'CheckCircle', 'Download', 'Search',
  'Eye', 'Heart', 'XCircle', 'ArrowLeft', 'ArrowUp', 'Trash2',
]
const lucideStubs = Object.fromEntries(
  lucideIcons.map((name) => [name, { template: '<div class="icon" />', props: ['size', 'strokeWidth'] }])
)

const stubs = {
  ...elementPlusStubs,
  ...lucideStubs,
  'el-table-column': {
    template: '<div class="el-table-column"><slot :row="{}" /></div>',
    props: ['label', 'prop', 'width', 'minWidth', 'fixed', 'type', 'selectable'],
  },
}

describe('ItemManagement.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    mockGetItems.mockResolvedValue({
      code: 200,
      data: {
        content: [
          { id: 1, title: '测试物品一', price: 100, status: 'ON_SALE', itemCondition: 'GOOD', category: '电子产品' },
          { id: 2, title: '测试物品二', price: 50, status: 'PENDING', itemCondition: 'NEW', category: '图书' },
        ],
        totalElements: 2,
      },
    })
    mockGetItemStats.mockResolvedValue({
      code: 200,
      data: { total: 200, pending: 10, onSale: 150, sold: 40 },
    })
    mockGetCategories.mockResolvedValue({
      code: 200,
      data: { content: [{ id: 1, name: '电子产品' }, { id: 2, name: '图书' }] },
    })
  })

  const mountItemManagement = (options = {}) => {
    return mount(ItemManagement, {
      global: {
        plugins: [createPinia()],
        stubs,
        ...options,
      },
    })
  }

  it('应该正确渲染物品管理页面', () => {
    const wrapper = mountItemManagement()
    expect(wrapper.find('.item-management').exists()).toBe(true)
  })

  it('应该显示页面标题和描述', () => {
    const wrapper = mountItemManagement()
    expect(wrapper.find('.section-title').text()).toBe('物品管理')
    expect(wrapper.find('.section-desc').text()).toContain('审核和管理平台发布的闲置物品')
  })

  it('应该包含物品统计卡片', () => {
    const wrapper = mountItemManagement()
    const statCards = wrapper.findAll('.stat-card')
    expect(statCards.length).toBe(4)
  })

  it('应该显示物品总数统计', () => {
    const wrapper = mountItemManagement()
    expect(wrapper.find('.stat-icon-total').exists()).toBe(true)
  })

  it('应该显示待审核统计', () => {
    const wrapper = mountItemManagement()
    expect(wrapper.find('.stat-icon-pending').exists()).toBe(true)
  })

  it('应该包含搜索输入框', () => {
    const wrapper = mountItemManagement()
    expect(wrapper.find('.search-input').exists()).toBe(true)
  })

  it('应该包含筛选器', () => {
    const wrapper = mountItemManagement()
    const filterBar = wrapper.find('.filter-selects')
    expect(filterBar.exists()).toBe(true)
  })

  it('应该包含导出按钮', () => {
    const wrapper = mountItemManagement()
    const exportBtn = wrapper.find('.btn-ghost')
    expect(exportBtn.exists()).toBe(true)
  })

  it('应该包含分页组件', () => {
    const wrapper = mountItemManagement()
    expect(wrapper.find('.pagination-wrapper').exists()).toBe(true)
  })

  it('应该包含详情对话框', () => {
    const wrapper = mountItemManagement()
    expect(wrapper.find('.el-dialog').exists()).toBe(true)
  })

  it('handleSearch 应重置页码并获取物品', async () => {
    const wrapper = mountItemManagement()
    const vm = wrapper.vm as any
    vm.page = 3
    vm.handleSearch()
    await wrapper.vm.$nextTick()
    expect(vm.page).toBe(1)
    expect(mockGetItems).toHaveBeenCalled()
  })

  it('handleReset 应清空所有筛选条件', async () => {
    const wrapper = mountItemManagement()
    const vm = wrapper.vm as any
    vm.searchKeyword = 'test'
    vm.itemStatus = 'PENDING'
    vm.categoryId = '1'
    vm.itemCondition = 'NEW'
    vm.bargainAllowed = 'true'
    vm.handleReset()
    await wrapper.vm.$nextTick()
    expect(vm.searchKeyword).toBe('')
    expect(vm.itemStatus).toBe('')
    expect(vm.categoryId).toBe('')
    expect(vm.itemCondition).toBe('')
    expect(vm.bargainAllowed).toBe('')
  })

  it('handleView 应设置当前物品并打开对话框', () => {
    const wrapper = mountItemManagement()
    const vm = wrapper.vm as any
    const item = { id: 1, title: '测试物品', images: '[]' }
    vm.handleView(item)
    expect(vm.detailDialogVisible).toBe(true)
    expect(vm.currentItem.title).toBe('测试物品')
  })

  it('getConditionClass 应返回正确的CSS类', () => {
    const wrapper = mountItemManagement()
    const vm = wrapper.vm as any
    expect(vm.getConditionClass('NEW')).toContain('success')
    expect(vm.getConditionClass('GOOD')).toContain('primary')
    expect(vm.getConditionClass('FAIR')).toContain('warning')
    expect(vm.getConditionClass('UNKNOWN')).toContain('default')
  })

  it('getStatusClass 应返回正确的CSS类', () => {
    const wrapper = mountItemManagement()
    const vm = wrapper.vm as any
    expect(vm.getStatusClass('ON_SALE')).toContain('success')
    expect(vm.getStatusClass('PENDING')).toContain('warning')
    expect(vm.getStatusClass('REJECTED')).toContain('danger')
    expect(vm.getStatusClass('UNKNOWN')).toContain('default')
  })

  it('truncateText 应截断过长文本', () => {
    const wrapper = mountItemManagement()
    const vm = wrapper.vm as any
    expect(vm.truncateText('这是很长的文本内容', 5)).toBe('这是很长的...')
    expect(vm.truncateText('短文本', 10)).toBe('短文本')
    expect(vm.truncateText('', 10)).toBe('')
  })

  it('getFirstImage 应返回第一张图片', () => {
    const wrapper = mountItemManagement()
    const vm = wrapper.vm as any
    expect(vm.getFirstImage({ coverImage: 'cover.jpg' })).toBe('cover.jpg')
    expect(vm.getFirstImage({ images: '["img1.jpg","img2.jpg"]' })).toBe('img1.jpg')
    expect(vm.getFirstImage({})).toBe('/placeholder.png')
  })

  it('parseImages 应正确解析图片', () => {
    const wrapper = mountItemManagement()
    const vm = wrapper.vm as any
    expect(vm.parseImages(null)).toEqual([])
    expect(vm.parseImages(['a.jpg'])).toEqual(['a.jpg'])
    expect(vm.parseImages('["a.jpg"]')).toEqual(['a.jpg'])
    expect(vm.parseImages('invalid')).toEqual([])
  })
})
