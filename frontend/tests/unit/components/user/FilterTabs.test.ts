import { describe, it, expect, vi, beforeAll } from 'vitest'
import { mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'

let FilterTabs: any
beforeAll(async () => {
  const mod = await import('@/components/user/FilterTabs.vue')
  FilterTabs = mod.default
})

const defaultTabs = [
  { value: '', label: '全部' },
  { value: 'ON_SALE', label: '在售' },
  { value: 'SOLD', label: '已售出' },
]

const mountFilterTabs = (props = {}, options = {}) => {
  setActivePinia(createPinia())
  return mount(FilterTabs, {
    props: {
      tabs: defaultTabs,
      modelValue: '',
      ...props,
    },
    ...options,
  })
}

describe('FilterTabs.vue 筛选标签组件', () => {
  describe('组件渲染', () => {
    it('应该渲染筛选标签组件', () => {
      const wrapper = mountFilterTabs()
      expect(wrapper.find('.filter-tabs').exists()).toBe(true)
    })

    it('应该渲染正确数量的标签', () => {
      const wrapper = mountFilterTabs()
      const tabs = wrapper.findAll('.filter-tab')
      expect(tabs.length).toBe(3)
    })

    it('应该显示标签文字', () => {
      const wrapper = mountFilterTabs()
      expect(wrapper.text()).toContain('全部')
      expect(wrapper.text()).toContain('在售')
      expect(wrapper.text()).toContain('已售出')
    })
  })

  describe('交互功能', () => {
    it('点击标签应触发update:modelValue事件', async () => {
      const wrapper = mountFilterTabs()
      const tabs = wrapper.findAll('.filter-tab')
      await tabs[1].trigger('click')
      expect(wrapper.emitted('update:modelValue')).toBeTruthy()
      expect(wrapper.emitted('update:modelValue')![0]).toEqual(['ON_SALE'])
    })

    it('点击第一个标签应触发空值事件', async () => {
      const wrapper = mountFilterTabs()
      const tabs = wrapper.findAll('.filter-tab')
      await tabs[0].trigger('click')
      expect(wrapper.emitted('update:modelValue')![0]).toEqual([''])
    })
  })

  describe('活跃状态', () => {
    it('当前选中的标签应有active类', () => {
      const wrapper = mountFilterTabs({ modelValue: 'ON_SALE' })
      const tabs = wrapper.findAll('.filter-tab')
      expect(tabs[1].classes()).toContain('active')
    })

    it('非选中的标签不应有active类', () => {
      const wrapper = mountFilterTabs({ modelValue: 'ON_SALE' })
      const tabs = wrapper.findAll('.filter-tab')
      expect(tabs[0].classes()).not.toContain('active')
      expect(tabs[2].classes()).not.toContain('active')
    })

    it('modelValue为空时第一个标签应为活跃', () => {
      const wrapper = mountFilterTabs({ modelValue: '' })
      const tabs = wrapper.findAll('.filter-tab')
      expect(tabs[0].classes()).toContain('active')
    })
  })

  describe('计数显示', () => {
    it('有count时应显示计数标签', () => {
      const tabsWithCount = [
        { value: '', label: '全部', count: 10 },
        { value: 'ON_SALE', label: '在售', count: 5 },
      ]
      const wrapper = mountFilterTabs({ tabs: tabsWithCount })
      expect(wrapper.text()).toContain('10')
      expect(wrapper.text()).toContain('5')
    })

    it('没有count时不应显示计数', () => {
      const wrapper = mountFilterTabs()
      expect(wrapper.find('.tab-count').exists()).toBe(false)
    })

    it('count为0时也应显示', () => {
      const tabsWithCount = [
        { value: '', label: '全部', count: 0 },
      ]
      const wrapper = mountFilterTabs({ tabs: tabsWithCount })
      expect(wrapper.find('.tab-count').exists()).toBe(true)
    })
  })

  describe('Props验证', () => {
    it('应接受有效的tabs属性', () => {
      const wrapper = mountFilterTabs()
      expect(wrapper.props('tabs')).toBeDefined()
      expect(Array.isArray(wrapper.props('tabs'))).toBe(true)
    })

    it('应接受有效的modelValue属性', () => {
      const wrapper = mountFilterTabs({ modelValue: 'ON_SALE' })
      expect(wrapper.props('modelValue')).toBe('ON_SALE')
    })

    it('应支持数字类型的value', () => {
      const tabs = [
        { value: 0, label: '类型一' },
        { value: 1, label: '类型二' },
      ]
      const wrapper = mountFilterTabs({ tabs, modelValue: 0 })
      const tabElements = wrapper.findAll('.filter-tab')
      expect(tabElements.length).toBe(2)
    })
  })

  describe('动态标签', () => {
    it('应支持动态标签列表', () => {
      const tabs = [
        { value: 'A', label: '标签A' },
        { value: 'B', label: '标签B' },
        { value: 'C', label: '标签C' },
        { value: 'D', label: '标签D' },
      ]
      const wrapper = mountFilterTabs({ tabs })
      const tabElements = wrapper.findAll('.filter-tab')
      expect(tabElements.length).toBe(4)
    })

    it('空标签列表应渲染空容器', () => {
      const wrapper = mountFilterTabs({ tabs: [] })
      const tabElements = wrapper.findAll('.filter-tab')
      expect(tabElements.length).toBe(0)
    })
  })
})
