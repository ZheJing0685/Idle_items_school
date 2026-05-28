import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ItemCard from '../../../src/components/user/ItemCard.vue'

describe('ItemCard Component', () => {
  it('应该渲染组件', () => {
    const wrapper = mount(ItemCard, {
      props: {
        id: 1,
        title: '测试商品',
        price: 99
      },
      global: {
        stubs: {
          'Eye': { template: '<div />' }
        }
      }
    })
    expect(wrapper.exists()).toBe(true)
  })

  it('应该显示标题', () => {
    const wrapper = mount(ItemCard, {
      props: {
        id: 1,
        title: '测试商品',
        price: 99
      },
      global: {
        stubs: {
          'Eye': { template: '<div />' }
        }
      }
    })
    expect(wrapper.text()).toContain('测试商品')
  })

  it('应该显示价格', () => {
    const wrapper = mount(ItemCard, {
      props: {
        id: 1,
        title: '测试商品',
        price: 99
      },
      global: {
        stubs: {
          'Eye': { template: '<div />' }
        }
      }
    })
    expect(wrapper.text()).toContain('99')
  })

  it('应该显示浏览次数', () => {
    const wrapper = mount(ItemCard, {
      props: {
        id: 1,
        title: '测试商品',
        price: 99,
        viewCount: 100
      },
      global: {
        stubs: {
          'Eye': { template: '<div />' }
        }
      }
    })
    expect(wrapper.text()).toContain('100')
  })

  it('点击应该触发 click 事件', async () => {
    const wrapper = mount(ItemCard, {
      props: {
        id: 1,
        title: '测试商品',
        price: 99
      },
      global: {
        stubs: {
          'Eye': { template: '<div />' }
        }
      }
    })
    await wrapper.find('.item-card').trigger('click')
    expect(wrapper.emitted('click')).toBeTruthy()
  })
})
