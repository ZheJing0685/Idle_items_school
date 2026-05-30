// @ts-nocheck
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import Dashboard from '@/views/admin/Dashboard.vue'
import { elementPlusStubs, lucideIconsStub } from '../../helpers/elementPlusMock'

// Mock API
const mockGetUserStats = vi.fn()
const mockGetItemStats = vi.fn()
const mockGetStats = vi.fn()

vi.mock('@/api', () => ({
  default: {
    admin: {
      users: {
        getUserStats: (...args: any[]) => mockGetUserStats(...args),
      },
      items: {
        getItemStats: (...args: any[]) => mockGetItemStats(...args),
      },
      orders: {
        getStats: (...args: any[]) => mockGetStats(...args),
      },
    },
  },
}))

// Mock Element Plus
vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn(),
  },
}))

// Mock lucide icons
const lucideIcons = [
  'Users', 'Package', 'ClipboardList', 'DollarSign', 'CheckCircle',
  'ChevronRight', 'Menu', 'MessageSquare', 'TrendingUp', 'FileText',
]
const lucideStubs = Object.fromEntries(
  lucideIcons.map((name) => [name, { template: '<div class="icon" />', props: ['size', 'strokeWidth'] }])
)

const stubs = {
  ...elementPlusStubs,
  ...lucideStubs,
  'router-link': {
    template: '<a class="router-link-stub"><slot /></a>',
    props: ['to'],
  },
}

describe('Dashboard.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    mockGetUserStats.mockResolvedValue({ code: 200, data: { total: 100, todayNew: 5 } })
    mockGetItemStats.mockResolvedValue({ code: 200, data: { total: 500, todayNew: 12 } })
    mockGetStats.mockResolvedValue({
      code: 200,
      data: { total: 200, todayNew: 8, totalAmount: 50000, todayAmount: 1200 },
    })
  })

  const mountDashboard = (options = {}) => {
    return mount(Dashboard, {
      global: {
        plugins: [createPinia()],
        stubs,
        ...options,
      },
    })
  }

  it('应该正确渲染控制台页面', () => {
    const wrapper = mountDashboard()
    expect(wrapper.find('.dashboard').exists()).toBe(true)
  })

  it('应该显示标题和描述', () => {
    const wrapper = mountDashboard()
    expect(wrapper.find('.section-title').text()).toBe('控制台')
    expect(wrapper.find('.section-desc').text()).toContain('平台运营数据概览')
  })

  it('应该包含四个统计卡片', () => {
    const wrapper = mountDashboard()
    const statCards = wrapper.findAll('.stat-card')
    expect(statCards.length).toBe(4)
  })

  it('应该显示总用户数', () => {
    const wrapper = mountDashboard()
    const totalUsersCard = wrapper.find('.card-total')
    expect(totalUsersCard.exists()).toBe(true)
    expect(totalUsersCard.find('.stat-label').text()).toBe('总用户数')
  })

  it('应该显示总交易额', () => {
    const wrapper = mountDashboard()
    const totalAmountCard = wrapper.find('.card-info')
    expect(totalAmountCard.exists()).toBe(true)
    expect(totalAmountCard.find('.stat-label').text()).toBe('总交易额')
  })

  it('应该包含快捷操作区域', () => {
    const wrapper = mountDashboard()
    expect(wrapper.find('.quick-actions').exists()).toBe(true)
    expect(wrapper.find('.section-subtitle').text()).toBe('快捷操作')
  })

  it('应该有用户管理快捷入口', () => {
    const wrapper = mountDashboard()
    const actionCards = wrapper.findAll('.action-card')
    const titles = actionCards.map(card => card.find('.action-title').text())
    expect(titles).toContain('用户管理')
  })

  it('应该包含待办事项区域', () => {
    const wrapper = mountDashboard()
    expect(wrapper.find('.todo-section').exists()).toBe(true)
  })

  it('应该显示待审核用户认证待办', () => {
    const wrapper = mountDashboard()
    const todoItems = wrapper.findAll('.todo-item')
    expect(todoItems.length).toBeGreaterThan(0)
    const titles = todoItems.map(item => item.find('.todo-title').text())
    expect(titles).toContain('待审核用户认证')
  })

  it('应该包含最近活动区域', () => {
    const wrapper = mountDashboard()
    expect(wrapper.find('.activity-section').exists()).toBe(true)
  })

  it('应该显示最近活动内容', () => {
    const wrapper = mountDashboard()
    const activities = wrapper.findAll('.activity-item')
    expect(activities.length).toBeGreaterThan(0)
  })

  it('formatNumber 应格式化数字', () => {
    const wrapper = mountDashboard()
    const vm = wrapper.vm as any
    expect(vm.formatNumber(1234567)).toBe('1,234,567')
    expect(vm.formatNumber(0)).toBe('0')
    expect(vm.formatNumber(null)).toBe('0')
  })

  it('onMounted 应调用 fetchStats', async () => {
    mountDashboard()
    // 等待异步操作
    await vi.waitFor(() => {
      expect(mockGetUserStats).toHaveBeenCalled()
      expect(mockGetItemStats).toHaveBeenCalled()
      expect(mockGetStats).toHaveBeenCalled()
    })
  })
})
