// @ts-nocheck
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import OrderManagement from '@/views/admin/OrderManagement.vue'
import { elementPlusStubs, lucideIconsStub } from '../../helpers/elementPlusMock'

// Mock API
const mockGetOrders = vi.fn()
const mockGetStats = vi.fn()
const mockGetOrder = vi.fn()
const mockApproveRefund = vi.fn()
const mockCancelOrder = vi.fn()
const mockBatchCancel = vi.fn()

vi.mock('@/api', () => ({
  default: {
    admin: {
      orders: {
        getOrders: (...args: any[]) => mockGetOrders(...args),
        getStats: (...args: any[]) => mockGetStats(...args),
        getOrder: (...args: any[]) => mockGetOrder(...args),
        approveRefund: (...args: any[]) => mockApproveRefund(...args),
        cancelOrder: (...args: any[]) => mockCancelOrder(...args),
        batchCancel: (...args: any[]) => mockBatchCancel(...args),
      },
    },
  },
}))

// Mock userStore
vi.mock('@/store', () => ({
  userStore: vi.fn(() => ({
    user: { role: 'ADMIN' },
  })),
}))

// Mock adminOrderFlow
vi.mock('@/utils/business/adminOrderFlow', () => ({
  ADMIN_ORDER_PAYMENT_OPTIONS: [
    { label: '全部支付方式', value: '' },
    { label: '微信支付', value: 'WECHAT' },
  ],
  ADMIN_ORDER_STATUS_OPTIONS: [
    { label: '全部状态', value: '' },
    { label: '待付款', value: 'PENDING_PAYMENT' },
    { label: '已完成', value: 'COMPLETED' },
  ],
  canAdminApproveRefund: vi.fn((status) => status === 'REFUND_REQUESTED'),
  canAdminCancelOrder: vi.fn((status) => ['PENDING_PAYMENT', 'PENDING_SHIPMENT'].includes(status)),
  getAdminOrderActions: vi.fn((order) => {
    const actions = [{ key: 'view', label: '查看详情', tone: 'primary' }]
    if (order.orderStatus === 'REFUND_REQUESTED') actions.push({ key: 'approveRefund', label: '审批退款', tone: 'warning' })
    if (['PENDING_PAYMENT', 'PENDING_SHIPMENT'].includes(order.orderStatus)) actions.push({ key: 'cancel', label: '取消', tone: 'danger' })
    return actions
  }),
  getAdminOrderStatusClass: vi.fn((status) => {
    const map: Record<string, string> = {
      PENDING_PAYMENT: 'badge-warning',
      PENDING_SHIPMENT: 'badge-warning',
      SHIPPED: 'badge-info',
      COMPLETED: 'badge-success',
      CANCELLED: 'badge-default',
      REFUND_REQUESTED: 'badge-danger',
      REFUNDED: 'badge-danger',
    }
    return map[status] || 'badge-default'
  }),
  getAdminOrderStatusText: vi.fn((status) => {
    const map: Record<string, string> = {
      PENDING_PAYMENT: '待付款',
      PENDING_SHIPMENT: '待发货',
      SHIPPED: '已发货',
      COMPLETED: '已完成',
      CANCELLED: '已取消',
      REFUND_REQUESTED: '退款申请中',
      REFUNDED: '已退款',
    }
    return map[status] || status
  }),
  getAdminOrderStatusTime: vi.fn((order) => order.createdAt),
  getAdminPaymentText: vi.fn((method) => {
    const map: Record<string, string> = { WECHAT: '微信支付', ALIPAY: '支付宝' }
    return map[method] || method || '未知'
  }),
  normalizeAdminOrder: vi.fn((order) => ({
    ...order,
    orderStatus: order.status || order.orderStatus,
  })),
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
    prompt: vi.fn().mockResolvedValue({ value: '取消原因' }),
  },
}))

// Mock lucide icons
const lucideIcons = ['Search']
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
  'v-loading': {
    template: '<div><slot /></div>',
  },
}

describe('OrderManagement.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    mockGetOrders.mockResolvedValue({
      code: 200,
      data: {
        content: [
          { id: 1, orderNo: 'ORD001', itemTitle: '测试物品', price: 100, orderStatus: 'PENDING_PAYMENT', buyerId: 1, sellerId: 2, createdAt: '2024-01-15T10:00:00' },
          { id: 2, orderNo: 'ORD002', itemTitle: '另一物品', price: 200, orderStatus: 'COMPLETED', buyerId: 3, sellerId: 4, createdAt: '2024-01-14T10:00:00' },
        ],
        totalElements: 2,
      },
    })
    mockGetStats.mockResolvedValue({
      code: 200,
      data: { total: 100, pendingPayment: 10, pendingShipment: 5, shipped: 20, refundRequested: 3, completed: 60, amount: 50000 },
    })
  })

  const mountOrderManagement = (options = {}) => {
    return mount(OrderManagement, {
      global: {
        plugins: [createPinia()],
        stubs,
        ...options,
      },
    })
  }

  it('应该正确渲染订单管理页面', () => {
    const wrapper = mountOrderManagement()
    expect(wrapper.find('.order-management').exists()).toBe(true)
  })

  it('应该显示页面标题和描述', () => {
    const wrapper = mountOrderManagement()
    expect(wrapper.find('.section-title').text()).toBe('订单管理')
    expect(wrapper.find('.section-desc').text()).toContain('订单')
  })

  it('应该包含统计卡片', () => {
    const wrapper = mountOrderManagement()
    const statCards = wrapper.findAll('.stat-card')
    expect(statCards.length).toBeGreaterThanOrEqual(4)
  })

  it('应该显示订单总数统计', () => {
    const wrapper = mountOrderManagement()
    expect(wrapper.find('.stats-grid-primary').exists()).toBe(true)
  })

  it('应该包含搜索输入框', () => {
    const wrapper = mountOrderManagement()
    expect(wrapper.find('.search-input').exists()).toBe(true)
  })

  it('应该包含查询按钮', () => {
    const wrapper = mountOrderManagement()
    const buttons = wrapper.findAll('.btn')
    expect(buttons.length).toBeGreaterThan(0)
  })

  it('应该包含分页组件', () => {
    const wrapper = mountOrderManagement()
    expect(wrapper.find('.footer-bar').exists()).toBe(true)
  })

  it('应该包含详情对话框', () => {
    const wrapper = mountOrderManagement()
    expect(wrapper.find('.el-dialog').exists()).toBe(true)
  })

  it('handleSearch 应重置页码并获取订单', async () => {
    const wrapper = mountOrderManagement()
    const vm = wrapper.vm as any
    vm.page = 3
    vm.handleSearch()
    await wrapper.vm.$nextTick()
    expect(vm.page).toBe(1)
    expect(mockGetOrders).toHaveBeenCalled()
  })

  it('handleReset 应清空筛选条件', async () => {
    const wrapper = mountOrderManagement()
    const vm = wrapper.vm as any
    vm.searchKeyword = 'test'
    vm.orderStatus = 'PENDING_PAYMENT'
    vm.paymentMethod = 'WECHAT'
    await vm.handleReset()
    expect(vm.searchKeyword).toBe('')
    expect(vm.orderStatus).toBe('')
    expect(vm.paymentMethod).toBe('')
    expect(vm.page).toBe(1)
  })

  it('formatPrice 应正确格式化价格', () => {
    const wrapper = mountOrderManagement()
    const vm = wrapper.vm as any
    expect(vm.formatPrice(100)).toBe('100')
    expect(vm.formatPrice(99.5)).toBe('99.50')
    expect(vm.formatPrice(0)).toBe('0')
  })

  it('formatDateTime 应格式化日期时间', () => {
    const wrapper = mountOrderManagement()
    const vm = wrapper.vm as any
    expect(vm.formatDateTime('')).toBe('')
    expect(vm.formatDateTime('2024-01-15T10:00:00')).toContain('2024')
  })

  it('handleSelectionChange 应更新选中订单', () => {
    const wrapper = mountOrderManagement()
    const vm = wrapper.vm as any
    vm.handleSelectionChange([{ id: 1 }, { id: 2 }])
    expect(vm.selectedOrders).toEqual([1, 2])
  })

  it('handleAction 应分发到正确的方法', async () => {
    const wrapper = mountOrderManagement()
    const vm = wrapper.vm as any
    const order = { id: 1, orderNo: 'ORD001' }
    await vm.handleAction('view', order)
    expect(mockGetOrder).toHaveBeenCalledWith(1)
  })

  it('onMounted 应调用 refreshData', async () => {
    mountOrderManagement()
    await vi.waitFor(() => {
      expect(mockGetOrders).toHaveBeenCalled()
      expect(mockGetStats).toHaveBeenCalled()
    })
  })
})
