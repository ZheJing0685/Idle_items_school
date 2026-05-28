import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import ItemCard from '@/components/user/ItemCard.vue'

describe('物品卡片组件', () => {
  const defaultProps = {
    id: 1,
    title: '测试物品',
    price: 99.99,
    coverImage: 'https://example.com/image.jpg',
    status: 'ON_SALE',
    statusText: '在售',
    viewCount: 100,
    time: '2024-01-01'
  }

  it('应该渲染物品卡片', () => {
    const wrapper = mount(ItemCard, {
      props: defaultProps
    })
    
    expect(wrapper.exists()).toBe(true)
  })

  it('应该显示标题', () => {
    const wrapper = mount(ItemCard, {
      props: defaultProps
    })
    
    expect(wrapper.text()).toContain('测试物品')
  })

  it('应该显示价格', () => {
    const wrapper = mount(ItemCard, {
      props: defaultProps
    })
    
    expect(wrapper.text()).toContain('¥99.99')
  })

  it('应该显示浏览量', () => {
    const wrapper = mount(ItemCard, {
      props: defaultProps
    })
    
    expect(wrapper.text()).toContain('100浏览')
  })

  it('应该显示时间', () => {
    const wrapper = mount(ItemCard, {
      props: defaultProps
    })
    
    expect(wrapper.text()).toContain('2024-01-01')
  })

  it('应该显示状态标签', () => {
    const wrapper = mount(ItemCard, {
      props: defaultProps
    })
    
    expect(wrapper.text()).toContain('在售')
  })

  it('应该点击时触发click事件', async () => {
    const wrapper = mount(ItemCard, {
      props: defaultProps
    })
    
    await wrapper.find('.item-card').trigger('click')
    
    expect(wrapper.emitted('click')).toBeTruthy()
    expect(wrapper.emitted('click')?.length).toBe(1)
  })

  it('应该没有coverImage时使用默认图片', () => {
    const propsWithoutImage = {
      ...defaultProps,
      coverImage: undefined
    }
    
    const wrapper = mount(ItemCard, {
      props: propsWithoutImage
    })
    
    const img = wrapper.find('img')
    expect(img.attributes('src')).toContain('data:image/svg+xml')
  })

  it('应该有不同的状态类', () => {
    const statuses = [
      { status: 'ON_SALE', expectedClass: 'status-on-sale' },
      { status: 'SOLD', expectedClass: 'status-sold' },
      { status: 'PENDING', expectedClass: 'status-pending' },
      { status: 'OFF_SHELF', expectedClass: 'status-off-shelf' }
    ]
    
    statuses.forEach(({ status, expectedClass }) => {
      const wrapper = mount(ItemCard, {
        props: {
          ...defaultProps,
          status,
          statusText: '状态'
        }
      })
      
      expect(wrapper.find('.status-tag').classes()).toContain(expectedClass)
    })
  })
})