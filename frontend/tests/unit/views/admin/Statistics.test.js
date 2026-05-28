import { describe, it, expect, vi, beforeAll, afterAll } from 'vitest'
import { mount } from '@vue/test-utils'
import { getAllStubs } from '../../helpers/elementPlusMock'
import { routerMockPlugin } from '../../helpers/routerMock'

// Mock canvas
beforeAll(() => {
  HTMLCanvasElement.prototype.getContext = vi.fn(() => ({
    fillRect: vi.fn(), clearRect: vi.fn(), getImageData: vi.fn(() => ({ data: [] })),
    putImageData: vi.fn(), createImageData: vi.fn(() => []), setTransform: vi.fn(),
    drawImage: vi.fn(), save: vi.fn(), fillText: vi.fn(), restore: vi.fn(),
    beginPath: vi.fn(), moveTo: vi.fn(), lineTo: vi.fn(), closePath: vi.fn(),
    stroke: vi.fn(), fill: vi.fn(), translate: vi.fn(), scale: vi.fn(),
    rotate: vi.fn(), arc: vi.fn(), measureText: vi.fn(() => ({ width: 0 })),
    transform: vi.fn(), rect: vi.fn(), clip: vi.fn(),
    createLinearGradient: vi.fn(() => ({ addColorStop: vi.fn() })),
    createRadialGradient: vi.fn(() => ({ addColorStop: vi.fn() })),
    createPattern: vi.fn(), canvas: { width: 0, height: 0 },
  }))

  // Mock window.addEventListener and removeEventListener for ECharts
  const originalAddEventListener = window.addEventListener
  const originalRemoveEventListener = window.removeEventListener
  window.addEventListener = vi.fn()
  window.removeEventListener = vi.fn()
  
  // Store original functions for cleanup
  window.__originalAddEventListener = originalAddEventListener
  window.__originalRemoveEventListener = originalRemoveEventListener
})

// Mock dictStore
vi.mock('../../../src/store', () => ({
  useDictStore: () => ({
    preloadCommonDicts: vi.fn().mockResolvedValue(undefined),
    dicts: {},
    getDictLabel: vi.fn((_, v) => v),
    getDictOptions: vi.fn(() => []),
  }),
  useConfigStore: () => ({
    fetchAllConfigs: vi.fn().mockResolvedValue({}),
    configs: {},
    getConfigString: vi.fn(() => ''),
  }),
}))

// Mock admin API with complete data
vi.mock('../../../src/api/services/admin', () => ({
  default: {
    statistics: {
      getDashboard: vi.fn().mockResolvedValue({
        code: 200,
        data: {
          totalUsers: 100, 
          newUsersToday: 5, 
          totalItems: 200, 
          newItemsToday: 10,
          totalOrders: 50, 
          newOrdersToday: 3, 
          totalRevenue: 10000,
          userGrowth: [], 
          orderTrend: [], 
          revenueTrend: [],
          categoryDistribution: [], 
          hotItems: [],
          recentOrders: [
            { id: 1, orderNo: 'ORD001', buyerName: '买家1', sellerName: '卖家1', amount: 100, status: 'COMPLETED' },
            { id: 2, orderNo: 'ORD002', buyerName: '买家2', sellerName: '卖家2', amount: 200, status: 'PENDING' }
          ],
          topSellers: [
            { userId: 1, username: 'seller1', nickname: '卖家1', salesCount: 10, totalAmount: 1000 }
          ]
        },
      }),
    },
  },
}))

import Statistics from '../../../../src/views/admin/Statistics.vue'

// Create el-table mock that passes data to slot
const ElTableMock = {
  template: `
    <div class="el-table">
      <slot :data="tableData"></slot>
    </div>
  `,
  props: ['data'],
  data() {
    return {
      tableData: this.data || []
    }
  }
}

// Create el-table-column mock
const ElTableColumnMock = {
  template: `
    <div class="el-table-column">
      <template v-if="$slots.default">
        <slot :row="row"></slot>
      </template>
      <template v-else>{{ label }}</template>
    </div>
  `,
  props: ['prop', 'label', 'width'],
  data() {
    return {
      row: this.$parent?.tableData?.[0] || {}
    }
  }
}

const stubs = {
  ...getAllStubs(),
  'el-radio-button': { template: '<button class="el-radio-button"><slot /></button>' },
  'el-radio-group': { template: '<div class="el-radio-group"><slot /></div>' },
  'el-date-picker': { template: '<div class="el-date-picker" />' },
  'v-chart': { template: '<div class="v-chart" />' },
  'el-table': ElTableMock,
  'el-table-column': ElTableColumnMock,
}

describe('Statistics View', () => {
  it('应该渲染组件', () => {
    const wrapper = mount(Statistics, {
      global: { stubs, plugins: [routerMockPlugin] },
    })
    expect(wrapper.exists()).toBe(true)
  })

  it('应该显示数据统计标题', () => {
    const wrapper = mount(Statistics, {
      global: { stubs, plugins: [routerMockPlugin] },
    })
    expect(wrapper.text()).toContain('数据统计')
  })

  it('应该显示统计周期选项', () => {
    const wrapper = mount(Statistics, {
      global: { stubs, plugins: [routerMockPlugin] },
    })
    expect(wrapper.text()).toContain('今日')
    expect(wrapper.text()).toContain('本周')
    expect(wrapper.text()).toContain('本月')
  })
})
