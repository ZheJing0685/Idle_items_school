// @ts-nocheck
import { describe, it, expect, vi, beforeAll } from 'vitest'
import { mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'

vi.mock('vue-router', () => ({
  useRoute: () => ({
    path: '/user/items',
    params: {},
    query: {},
  }),
}))

vi.mock('lucide-vue-next', () => ({
  ChevronRight: { template: '<div class="icon-chevron-right" />', props: ['size'] },
  ChevronLeft: { template: '<div class="icon-chevron-left" />', props: ['size'] },
  User: { template: '<div class="icon-user" />', props: ['size'] },
  Package: { template: '<div class="icon-package" />', props: ['size'] },
  ShoppingBag: { template: '<div class="icon-shopping-bag" />', props: ['size'] },
  Heart: { template: '<div class="icon-heart" />', props: ['size'] },
  MessageSquare: { template: '<div class="icon-message" />', props: ['size'] },
  Bell: { template: '<div class="icon-bell" />', props: ['size'] },
  Shield: { template: '<div class="icon-shield" />', props: ['size'] },
  CheckCircle: { template: '<div class="icon-check" />', props: ['size'] },
  Plus: { template: '<div class="icon-plus" />', props: ['size'] },
  FileText: { template: '<div class="icon-file" />', props: ['size'] },
}))

let Sidebar: any
beforeAll(async () => {
  const mod = await import('@/components/user/Sidebar.vue')
  Sidebar = mod.default
})

const defaultMenuItems = [
  { path: '/user/profile', name: '个人资料', icon: 'user' },
  { path: '/user/items', name: '我的发布', icon: 'package' },
  { path: '/user/orders', name: '我的订单', icon: 'shopping-bag' },
  { path: '/user/favorites', name: '我的收藏', icon: 'heart' },
  { path: '/user/chat', name: '消息', icon: 'message' },
]

const mountSidebar = (props = {}, options = {}) => {
  setActivePinia(createPinia())
  return mount(Sidebar, {
    props: {
      collapsed: false,
      menuItems: defaultMenuItems,
      ...props,
    },
    global: {
      stubs: {
        'router-link': {
          template: '<a class="router-link-stub" :href="to"><slot /></a>',
          props: ['to'],
        },
      },
      ...options,
    },
    ...options,
  })
}

describe('Sidebar.vue 侧边栏组件', () => {
  describe('组件渲染', () => {
    it('应该渲染侧边栏', () => {
      const wrapper = mountSidebar()
      expect(wrapper.find('.sidebar').exists()).toBe(true)
    })

    it('应该渲染导航列表', () => {
      const wrapper = mountSidebar()
      expect(wrapper.find('.sidebar-nav').exists()).toBe(true)
    })

    it('应该渲染正确数量的菜单项', () => {
      const wrapper = mountSidebar()
      const items = wrapper.findAll('.nav-item')
      expect(items.length).toBe(5)
    })

    it('应该显示菜单项文字', () => {
      const wrapper = mountSidebar()
      expect(wrapper.text()).toContain('个人资料')
      expect(wrapper.text()).toContain('我的发布')
      expect(wrapper.text()).toContain('我的收藏')
    })

    it('应该渲染切换按钮', () => {
      const wrapper = mountSidebar()
      expect(wrapper.find('.toggle-btn').exists()).toBe(true)
    })
  })

  describe('折叠状态', () => {
    it('collapsed为true时应有collapsed类', () => {
      const wrapper = mountSidebar({ collapsed: true })
      expect(wrapper.find('.sidebar').classes()).toContain('collapsed')
    })

    it('collapsed为false时不应有collapsed类', () => {
      const wrapper = mountSidebar({ collapsed: false })
      expect(wrapper.find('.sidebar').classes()).not.toContain('collapsed')
    })

    it('折叠时不应显示菜单文字', () => {
      const wrapper = mountSidebar({ collapsed: true })
      expect(wrapper.find('.nav-text').exists()).toBe(false)
    })

    it('展开时应显示菜单文字', () => {
      const wrapper = mountSidebar({ collapsed: false })
      expect(wrapper.find('.nav-text').exists()).toBe(true)
    })
  })

  describe('交互功能', () => {
    it('点击切换按钮应触发toggle事件', async () => {
      const wrapper = mountSidebar()
      await wrapper.find('.toggle-btn').trigger('click')
      expect(wrapper.emitted('toggle')).toBeTruthy()
    })

    it('toggle按钮应有正确的aria-label', () => {
      const wrapper = mountSidebar({ collapsed: false })
      const btn = wrapper.find('.toggle-btn')
      expect(btn.attributes('aria-label')).toBe('收起侧边栏')
    })

    it('折叠时toggle按钮aria-label应为展开', () => {
      const wrapper = mountSidebar({ collapsed: true })
      const btn = wrapper.find('.toggle-btn')
      expect(btn.attributes('aria-label')).toBe('展开侧边栏')
    })
  })

  describe('活动状态', () => {
    it('当前路径匹配的菜单项应有active类', () => {
      const wrapper = mountSidebar()
      const items = wrapper.findAll('.nav-item')
      const activeItem = items.find(item => item.classes().includes('active'))
      expect(activeItem).toBeDefined()
    })

    it('active菜单项应包含我的发布文字', () => {
      const wrapper = mountSidebar()
      const activeItem = wrapper.find('.nav-item.active')
      expect(activeItem.text()).toContain('我的发布')
    })
  })

  describe('Props验证', () => {
    it('应接受collapsed布尔属性', () => {
      const wrapper = mountSidebar({ collapsed: true })
      expect(wrapper.props('collapsed')).toBe(true)
    })

    it('应接受menuItems数组属性', () => {
      const wrapper = mountSidebar()
      expect(wrapper.props('menuItems')).toBeDefined()
      expect(Array.isArray(wrapper.props('menuItems'))).toBe(true)
    })

    it('menuItems为空数组时应渲染空导航', () => {
      const wrapper = mountSidebar({ menuItems: [] })
      const items = wrapper.findAll('.nav-item')
      expect(items.length).toBe(0)
    })
  })

  describe('Badge显示', () => {
    it('有badge属性时应显示badge', () => {
      const menuWithBadge = [
        { path: '/user/chat', name: '消息', icon: 'message', badge: 5 },
      ]
      const wrapper = mountSidebar({ menuItems: menuWithBadge })
      expect(wrapper.text()).toContain('5')
    })

    it('无badge属性时不显示badge', () => {
      const wrapper = mountSidebar()
      expect(wrapper.find('.nav-badge').exists()).toBe(false)
    })

    it('折叠时不应显示badge', () => {
      const menuWithBadge = [
        { path: '/user/chat', name: '消息', icon: 'message', badge: 5 },
      ]
      const wrapper = mountSidebar({ menuItems: menuWithBadge, collapsed: true })
      expect(wrapper.find('.nav-badge').exists()).toBe(false)
    })
  })
})
