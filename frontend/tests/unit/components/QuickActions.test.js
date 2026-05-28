import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import QuickActions from '../../../src/components/user/QuickActions.vue'

describe('QuickActions Component', () => {
  const actions = [
    { path: '/publish', name: '发布闲置', icon: 'plus' },
    { path: '/user/orders', name: '我的订单', icon: 'shopping-bag' },
    { path: '/user/chat', name: '消息中心', icon: 'message' }
  ]

  it('应该渲染操作项', () => {
    const wrapper = mount(QuickActions, {
      props: { actions },
      global: {
        stubs: {
          'router-link': { template: '<a class="action-item"><slot /></a>' },
          'Plus': { template: '<div />' },
          'ShoppingBag': { template: '<div />' },
          'MessageSquare': { template: '<div />' }
        }
      }
    })
    expect(wrapper.findAll('.action-item')).toHaveLength(3)
  })

  it('应该显示操作名称', () => {
    const wrapper = mount(QuickActions, {
      props: { actions },
      global: {
        stubs: {
          'router-link': { template: '<a class="action-item"><slot /></a>' },
          'Plus': { template: '<div />' },
          'ShoppingBag': { template: '<div />' },
          'MessageSquare': { template: '<div />' }
        }
      }
    })
    expect(wrapper.text()).toContain('发布闲置')
    expect(wrapper.text()).toContain('我的订单')
    expect(wrapper.text()).toContain('消息中心')
  })
})
