import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import Footer from '../../../src/components/Footer.vue'

describe('Footer Component', () => {
  it('应该渲染组件', () => {
    const wrapper = mount(Footer, {
      global: {
        stubs: {
          'router-link': { template: '<a><slot /></a>' },
          'Package': { template: '<div />' },
          'Leaf': { template: '<div />' },
          'Mail': { template: '<div />' },
          'Phone': { template: '<div />' },
          'MapPin': { template: '<div />' },
          'Smile': { template: '<div />' }
        }
      }
    })
    expect(wrapper.exists()).toBe(true)
  })

  it('应该显示品牌名称', () => {
    const wrapper = mount(Footer, {
      global: {
        stubs: {
          'router-link': { template: '<a><slot /></a>' },
          'Package': { template: '<div />' },
          'Leaf': { template: '<div />' },
          'Mail': { template: '<div />' },
          'Phone': { template: '<div />' },
          'MapPin': { template: '<div />' },
          'Smile': { template: '<div />' }
        }
      }
    })
    expect(wrapper.text()).toContain('闲置好物')
  })

  it('应该显示版权信息', () => {
    const wrapper = mount(Footer, {
      global: {
        stubs: {
          'router-link': { template: '<a><slot /></a>' },
          'Package': { template: '<div />' },
          'Leaf': { template: '<div />' },
          'Mail': { template: '<div />' },
          'Phone': { template: '<div />' },
          'MapPin': { template: '<div />' },
          'Smile': { template: '<div />' }
        }
      }
    })
    expect(wrapper.text()).toContain('2026')
  })
})
