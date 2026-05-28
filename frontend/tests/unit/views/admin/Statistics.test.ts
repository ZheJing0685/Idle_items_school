import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import Statistics from '@/views/admin/Statistics.vue'
import { elementPlusStubs } from '../../helpers/elementPlusMock'

// 创建el-table-column mock，提供row数据
const elTableColumnMock = {
  template: '<div class="el-table-column"><slot :row="{}" /></div>',
  props: ['label', 'prop']
}

// Mock ECharts
vi.mock('echarts', () => ({
  init: vi.fn(() => ({
    setOption: vi.fn(),
    resize: vi.fn(),
    dispose: vi.fn()
  }))
}))

// Mock API
const { mockGetDashboard } = vi.hoisted(() => ({
  mockGetDashboard: vi.fn()
}))

vi.mock('@/api', () => ({
  default: {
    admin: {
      statistics: {
        getDashboard: mockGetDashboard
      }
    }
  }
}))

// Mock dict store
vi.mock('../../../../src/store/dict.js', () => ({
  useDictStore: () => ({
    preloadCommonDicts: vi.fn(),
    getDictLabel: vi.fn().mockReturnValue('已完成')
  })
}))

describe('统计页面', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockGetDashboard.mockReset()
    mockGetDashboard.mockResolvedValue({
      code: 200,
      data: {
        totalOrders: 100,
        totalAmount: 50000,
        pendingOrders: 10,
        completedOrders: 90,
        orderTrend: [],
        orderStatusDistribution: {},
        recentOrders: []
      }
    })
  })

  it('应该渲染统计页面', () => {
    const wrapper = mount(Statistics, {
      global: {
        plugins: [createPinia()],
        stubs: {
          ...elementPlusStubs,
          'el-table-column': elTableColumnMock,
          'OrderTrendChart': { template: '<div class="order-trend-chart" />' },
          'OrderStatusPie': { template: '<div class="order-status-pie" />' }
        }
      }
    })
    
    expect(wrapper.exists()).toBe(true)
  })

  it('应该包含图表容器', () => {
    const wrapper = mount(Statistics, {
      global: {
        plugins: [createPinia()],
        stubs: {
          ...elementPlusStubs,
          'el-table-column': elTableColumnMock,
          'OrderTrendChart': { template: '<div class="order-trend-chart" />' },
          'OrderStatusPie': { template: '<div class="order-status-pie" />' }
        }
      }
    })
    
    expect(wrapper.find('.charts-grid').exists()).toBe(true)
  })
})