import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import NotFound from '../../../src/views/NotFound.vue'

describe('NotFound View', () => {
  it('应该渲染组件', () => {
    const wrapper = mount(NotFound, {
      global: {
        stubs: {
          'router-link': { template: '<a><slot /></a>' },
          'Smile': { template: '<div />' },
          'Circle': { template: '<div />' },
          'Home': { template: '<div />' },
          'Grid': { template: '<div />' }
        }
      }
    })
    expect(wrapper.exists()).toBe(true)
  })

  it('应该显示 404 错误码', () => {
    const wrapper = mount(NotFound, {
      global: {
        stubs: {
          'router-link': { template: '<a><slot /></a>' },
          'Smile': { template: '<div />' },
          'Circle': { template: '<div />' },
          'Home': { template: '<div />' },
          'Grid': { template: '<div />' }
        }
      }
    })
    expect(wrapper.text()).toContain('404')
  })

  it('应该显示页面不存在标题', () => {
    const wrapper = mount(NotFound, {
      global: {
        stubs: {
          'router-link': { template: '<a><slot /></a>' },
          'Smile': { template: '<div />' },
          'Circle': { template: '<div />' },
          'Home': { template: '<div />' },
          'Grid': { template: '<div />' }
        }
      }
    })
    expect(wrapper.text()).toContain('页面不存在')
  })

  it('应该显示返回首页按钮', () => {
    const wrapper = mount(NotFound, {
      global: {
        stubs: {
          'router-link': { template: '<a class="action-btn"><slot /></a>' },
          'Smile': { template: '<div />' },
          'Circle': { template: '<div />' },
          'Home': { template: '<div />' },
          'Grid': { template: '<div />' }
        }
      }
    })
    expect(wrapper.text()).toContain('返回首页')
  })

  it('应该显示浏览好物按钮', () => {
    const wrapper = mount(NotFound, {
      global: {
        stubs: {
          'router-link': { template: '<a class="action-btn"><slot /></a>' },
          'Smile': { template: '<div />' },
          'Circle': { template: '<div />' },
          'Home': { template: '<div />' },
          'Grid': { template: '<div />' }
        }
      }
    })
    expect(wrapper.text()).toContain('浏览好物')
  })
})
