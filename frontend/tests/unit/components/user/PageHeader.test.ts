// @ts-nocheck
import { describe, it, expect, vi, beforeAll } from 'vitest'
import { mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'

let PageHeader: any
beforeAll(async () => {
  const mod = await import('@/components/user/PageHeader.vue')
  PageHeader = mod.default
})

const mountPageHeader = (props = {}, slots = {}) => {
  setActivePinia(createPinia())
  return mount(PageHeader, {
    props: {
      title: '页面标题',
      ...props,
    },
    slots,
  })
}

describe('PageHeader.vue 页面头部组件', () => {
  describe('组件渲染', () => {
    it('应该渲染页面头部组件', () => {
      const wrapper = mountPageHeader()
      expect(wrapper.find('.page-header').exists()).toBe(true)
    })

    it('应该显示标题', () => {
      const wrapper = mountPageHeader()
      expect(wrapper.find('.header-title').text()).toBe('页面标题')
    })

    it('应该显示自定义标题', () => {
      const wrapper = mountPageHeader({ title: '自定义标题' })
      expect(wrapper.find('.header-title').text()).toBe('自定义标题')
    })

    it('应该显示副标题', () => {
      const wrapper = mountPageHeader({ subtitle: '这是一段副标题' })
      expect(wrapper.find('.header-subtitle').text()).toBe('这是一段副标题')
    })

    it('不传subtitle时不显示副标题元素', () => {
      const wrapper = mountPageHeader()
      expect(wrapper.find('.header-subtitle').exists()).toBe(false)
    })
  })

  describe('插槽功能', () => {
    it('有action插槽时应显示操作区域', () => {
      const wrapper = mountPageHeader({}, {
        action: '<button>操作按钮</button>',
      })
      expect(wrapper.find('.header-right').exists()).toBe(true)
      expect(wrapper.find('button').exists()).toBe(true)
    })

    it('没有action插槽时不应显示操作区域', () => {
      const wrapper = mountPageHeader()
      expect(wrapper.find('.header-right').exists()).toBe(false)
    })

    it('action插槽内容应正确显示', () => {
      const wrapper = mountPageHeader({}, {
        action: '<span class="custom-action">点击我</span>',
      })
      expect(wrapper.find('.custom-action').text()).toBe('点击我')
    })
  })

  describe('Props验证', () => {
    it('应接受title属性', () => {
      const wrapper = mountPageHeader({ title: '测试' })
      expect(wrapper.props('title')).toBe('测试')
    })

    it('应接受subtitle属性', () => {
      const wrapper = mountPageHeader({ subtitle: '副标题' })
      expect(wrapper.props('subtitle')).toBe('副标题')
    })

    it('subtitle默认为空字符串', () => {
      const wrapper = mountPageHeader()
      expect(wrapper.props('subtitle')).toBe('')
    })
  })

  describe('样式类', () => {
    it('应包含page-header类', () => {
      const wrapper = mountPageHeader()
      expect(wrapper.find('.page-header').exists()).toBe(true)
    })

    it('应包含header-content类', () => {
      const wrapper = mountPageHeader()
      expect(wrapper.find('.header-content').exists()).toBe(true)
    })

    it('应包含header-left类', () => {
      const wrapper = mountPageHeader()
      expect(wrapper.find('.header-left').exists()).toBe(true)
    })

    it('应包含header-title类', () => {
      const wrapper = mountPageHeader()
      expect(wrapper.find('.header-title').exists()).toBe(true)
    })
  })

  describe('标题装饰', () => {
    it('标题元素应正确渲染', () => {
      const wrapper = mountPageHeader()
      const title = wrapper.find('.header-title')
      expect(title.exists()).toBe(true)
      expect(title.text()).toBe('页面标题')
    })
  })
})
