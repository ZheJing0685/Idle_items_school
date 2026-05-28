import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import EmptyState from '../../../src/components/user/EmptyState.vue'

describe('EmptyState Component', () => {
  it('应该渲染默认标题', () => {
    const wrapper = mount(EmptyState, {
      global: {
        stubs: {
          'CheckCircle': { template: '<div />' },
          'AlertCircle': { template: '<div />' },
          'XCircle': { template: '<div />' },
          'Inbox': { template: '<div />' }
        }
      }
    })
    expect(wrapper.text()).toContain('暂无数据')
  })

  it('应该渲染自定义标题', () => {
    const wrapper = mount(EmptyState, {
      props: {
        title: '没有订单'
      },
      global: {
        stubs: {
          'CheckCircle': { template: '<div />' },
          'AlertCircle': { template: '<div />' },
          'XCircle': { template: '<div />' },
          'Inbox': { template: '<div />' }
        }
      }
    })
    expect(wrapper.text()).toContain('没有订单')
  })

  it('应该渲染描述', () => {
    const wrapper = mount(EmptyState, {
      props: {
        title: '没有数据',
        description: '您还没有任何数据'
      },
      global: {
        stubs: {
          'CheckCircle': { template: '<div />' },
          'AlertCircle': { template: '<div />' },
          'XCircle': { template: '<div />' },
          'Inbox': { template: '<div />' }
        }
      }
    })
    expect(wrapper.text()).toContain('您还没有任何数据')
  })

  it('应该渲染操作插槽', () => {
    const wrapper = mount(EmptyState, {
      props: {
        title: '没有数据'
      },
      slots: {
        action: '<button class="action-btn">添加</button>'
      },
      global: {
        stubs: {
          'CheckCircle': { template: '<div />' },
          'AlertCircle': { template: '<div />' },
          'XCircle': { template: '<div />' },
          'Inbox': { template: '<div />' }
        }
      }
    })
    expect(wrapper.find('.action-btn').exists()).toBe(true)
  })
})
