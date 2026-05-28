import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import StatsCard from '../../../src/components/user/StatsCard.vue'

describe('StatsCard Component', () => {
  const stats = [
    { value: 100, label: '总发布' },
    { value: 50, label: '已售出', accent: true },
    { value: 10, label: '收藏数' }
  ]

  it('应该渲染统计项', () => {
    const wrapper = mount(StatsCard, {
      props: { stats }
    })
    expect(wrapper.findAll('.stat-item')).toHaveLength(3)
  })

  it('应该显示统计值', () => {
    const wrapper = mount(StatsCard, {
      props: { stats }
    })
    expect(wrapper.text()).toContain('100')
    expect(wrapper.text()).toContain('50')
    expect(wrapper.text()).toContain('10')
  })

  it('应该显示统计标签', () => {
    const wrapper = mount(StatsCard, {
      props: { stats }
    })
    expect(wrapper.text()).toContain('总发布')
    expect(wrapper.text()).toContain('已售出')
    expect(wrapper.text()).toContain('收藏数')
  })

  it('应该应用 accent 类', () => {
    const wrapper = mount(StatsCard, {
      props: { stats }
    })
    const accentItem = wrapper.findAll('.stat-item')[1]
    expect(accentItem.classes()).toContain('accent')
  })
})
