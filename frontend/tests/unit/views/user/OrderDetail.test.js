import { describe, it, expect, vi } from 'vitest'

// Mock vue-router completely before importing
vi.mock('vue-router', () => ({
  useRoute: () => ({
    params: { orderId: '1' },
    query: {},
    path: '/user/order/1'
  }),
  useRouter: () => ({
    push: vi.fn(),
    back: vi.fn(),
    replace: vi.fn()
  }),
  createRouter: vi.fn(() => ({
    push: vi.fn(),
    replace: vi.fn(),
    beforeEach: vi.fn()
  })),
  createWebHistory: vi.fn(),
  createWebHashHistory: vi.fn(),
  RouterView: { template: '<div><slot /></div>' },
  RouterLink: { template: '<a><slot /></a>' }
}))

// Mock the router module
vi.mock('../../../../src/router', () => ({
  default: {
    push: vi.fn(),
    replace: vi.fn(),
    beforeEach: vi.fn(),
    currentRoute: { value: { params: { orderId: '1' } } }
  }
}))

import { mount } from '@vue/test-utils'
import { getAllStubs } from '../../helpers/elementPlusMock'
import OrderDetail from '../../../../src/views/user/OrderDetail.vue'

const stubs = {
  ...getAllStubs(),
  'router-link': { template: '<a class="router-link"><slot /></a>' },
  'router-view': { template: '<div class="router-view"><slot /></div>' },
}

describe('OrderDetail View', () => {
  it('应该渲染组件', () => {
    const wrapper = mount(OrderDetail, { 
      global: { 
        stubs,
        plugins: []
      },
      attachTo: document.body
    })
    expect(wrapper.exists()).toBe(true)
  })

  it('应该显示订单详情标题', () => {
    const wrapper = mount(OrderDetail, { 
      global: { 
        stubs,
        plugins: []
      },
      attachTo: document.body
    })
    expect(wrapper.text()).toContain('订单')
  })
})
