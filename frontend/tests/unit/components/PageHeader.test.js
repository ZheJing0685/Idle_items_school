import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import PageHeader from '../../../src/components/user/PageHeader.vue'

describe('PageHeader Component', () => {
  it('应该渲染标题', () => {
    const wrapper = mount(PageHeader, {
      props: {
        title: '页面标题'
      }
    })
    expect(wrapper.text()).toContain('页面标题')
  })

  it('应该渲染副标题', () => {
    const wrapper = mount(PageHeader, {
      props: {
        title: '页面标题',
        subtitle: '页面副标题'
      }
    })
    expect(wrapper.text()).toContain('页面副标题')
  })

  it('没有副标题时不应该渲染', () => {
    const wrapper = mount(PageHeader, {
      props: {
        title: '页面标题'
      }
    })
    expect(wrapper.find('.header-subtitle').exists()).toBe(false)
  })

  it('应该渲染操作按钮插槽', () => {
    const wrapper = mount(PageHeader, {
      props: {
        title: '页面标题'
      },
      slots: {
        action: '<button class="action-btn">操作</button>'
      }
    })
    expect(wrapper.find('.action-btn').exists()).toBe(true)
  })
})
