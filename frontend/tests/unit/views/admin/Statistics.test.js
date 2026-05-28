import { describe, it, expect, vi, beforeAll } from 'vitest'
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

// Mock admin API
vi.mock('../../../src/api/services/admin', () => ({
  default: {
    statistics: {
      getDashboard: vi.fn().mockResolvedValue({
        data: {
          totalUsers: 100, newUsersToday: 5, totalItems: 200, newItemsToday: 10,
          totalOrders: 50, newOrdersToday: 3, totalRevenue: 10000,
          userGrowth: [], orderTrend: [], revenueTrend: [],
          categoryDistribution: [], hotItems: [],
        },
      }),
    },
  },
}))

import Statistics from '../../../../src/views/admin/Statistics.vue'

const stubs = {
  ...getAllStubs(),
  'el-radio-button': { template: '<button class="el-radio-button"><slot /></button>' },
  'el-radio-group': { template: '<div class="el-radio-group"><slot /></div>' },
  'el-date-picker': { template: '<div class="el-date-picker" />' },
  'v-chart': { template: '<div class="v-chart" />' },
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
