import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import FilterTabs from '../../../src/components/user/FilterTabs.vue'

describe('FilterTabs Component', () => {
  const tabs = [
    { value: 'all', label: '全部', count: 10 },
    { value: 'pending', label: '待处理', count: 5 },
    { value: 'completed', label: '已完成' }
  ]

  it('应该渲染标签页', () => {
    const wrapper = mount(FilterTabs, {
      props: {
        tabs,
        modelValue: 'all'
      }
    })
    expect(wrapper.findAll('.filter-tab')).toHaveLength(3)
  })

  it('应该显示标签文本', () => {
    const wrapper = mount(FilterTabs, {
      props: {
        tabs,
        modelValue: 'all'
      }
    })
    expect(wrapper.text()).toContain('全部')
    expect(wrapper.text()).toContain('待处理')
    expect(wrapper.text()).toContain('已完成')
  })

  it('应该显示计数', () => {
    const wrapper = mount(FilterTabs, {
      props: {
        tabs,
        modelValue: 'all'
      }
    })
    expect(wrapper.text()).toContain('10')
    expect(wrapper.text()).toContain('5')
  })

  it('应该高亮选中的标签', () => {
    const wrapper = mount(FilterTabs, {
      props: {
        tabs,
        modelValue: 'all'
      }
    })
    const allTab = wrapper.findAll('.filter-tab')[0]
    expect(allTab.classes()).toContain('active')
  })

  it('点击应该触发更新事件', async () => {
    const wrapper = mount(FilterTabs, {
      props: {
        tabs,
        modelValue: 'all'
      }
    })
    await wrapper.findAll('.filter-tab')[1].trigger('click')
    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')[0]).toEqual(['pending'])
  })
})
