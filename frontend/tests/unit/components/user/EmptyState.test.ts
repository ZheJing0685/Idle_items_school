// @ts-nocheck
import { describe, it, expect, vi, beforeAll } from 'vitest'
import { mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import { lucideIconsStub } from '../../helpers/elementPlusMock'

vi.mock('lucide-vue-next', () => ({
  CheckCircle: { template: '<div class="icon-check-circle" />', props: ['size', 'color'] },
  AlertCircle: { template: '<div class="icon-alert-circle" />', props: ['size', 'color'] },
  XCircle: { template: '<div class="icon-x-circle" />', props: ['size', 'color'] },
  Inbox: { template: '<div class="icon-inbox" />', props: ['size', 'color'] },
}))

let EmptyState: any
beforeAll(async () => {
  const mod = await import('@/components/user/EmptyState.vue')
  EmptyState = mod.default
})

const mountEmptyState = (props = {}, slots = {}) => {
  setActivePinia(createPinia())
  return mount(EmptyState, {
    props: {
      title: '暂无数据',
      ...props,
    },
    slots,
  })
}

describe('EmptyState.vue 空状态组件', () => {
  describe('组件渲染', () => {
    it('应该渲染空状态组件', () => {
      const wrapper = mountEmptyState()
      expect(wrapper.find('.empty-state').exists()).toBe(true)
    })

    it('应该显示默认标题', () => {
      const wrapper = mountEmptyState()
      expect(wrapper.find('.empty-title').text()).toBe('暂无数据')
    })

    it('应该显示自定义标题', () => {
      const wrapper = mountEmptyState({ title: '自定义标题' })
      expect(wrapper.find('.empty-title').text()).toBe('自定义标题')
    })

    it('应该显示描述文字', () => {
      const wrapper = mountEmptyState({ description: '这是一段描述' })
      expect(wrapper.find('.empty-desc').text()).toBe('这是一段描述')
    })

    it('不传description时不应显示描述元素', () => {
      const wrapper = mountEmptyState({ description: '' })
      expect(wrapper.find('.empty-desc').exists()).toBe(false)
    })
  })

  describe('图标类型', () => {
    it('默认类型应显示Inbox图标', () => {
      const wrapper = mountEmptyState({ type: 'default' })
      expect(wrapper.find('.icon-inbox').exists()).toBe(true)
    })

    it('success类型应显示CheckCircle图标', () => {
      const wrapper = mountEmptyState({ type: 'success' })
      expect(wrapper.find('.icon-check-circle').exists()).toBe(true)
    })

    it('warning类型应显示AlertCircle图标', () => {
      const wrapper = mountEmptyState({ type: 'warning' })
      expect(wrapper.find('.icon-alert-circle').exists()).toBe(true)
    })

    it('error类型应显示XCircle图标', () => {
      const wrapper = mountEmptyState({ type: 'error' })
      expect(wrapper.find('.icon-x-circle').exists()).toBe(true)
    })
  })

  describe('插槽功能', () => {
    it('有action插槽时应显示操作区域', () => {
      const wrapper = mountEmptyState({}, {
        action: '<button class="action-btn">操作</button>',
      })
      expect(wrapper.find('.empty-action').exists()).toBe(true)
      expect(wrapper.find('.action-btn').exists()).toBe(true)
    })

    it('没有action插槽时不应显示操作区域', () => {
      const wrapper = mountEmptyState()
      expect(wrapper.find('.empty-action').exists()).toBe(false)
    })
  })

  describe('Props验证', () => {
    it('应该接受有效type属性', () => {
      const wrapper = mountEmptyState({ type: 'success' })
      expect(wrapper.props('type')).toBe('success')
    })

    it('应该使用默认type值', () => {
      const wrapper = mountEmptyState()
      expect(wrapper.props('type')).toBe('default')
    })

    it('应该使用默认title值', () => {
      const wrapper = mountEmptyState({ title: undefined })
      expect(wrapper.find('.empty-title').text()).toBe('暂无数据')
    })
  })

  describe('样式类', () => {
    it('应包含empty-state类', () => {
      const wrapper = mountEmptyState()
      expect(wrapper.find('.empty-state').exists()).toBe(true)
    })

    it('应包含empty-title类', () => {
      const wrapper = mountEmptyState()
      expect(wrapper.find('.empty-title').exists()).toBe(true)
    })

    it('应包含empty-icon类', () => {
      const wrapper = mountEmptyState()
      expect(wrapper.find('.empty-icon').exists()).toBe(true)
    })
  })
})
