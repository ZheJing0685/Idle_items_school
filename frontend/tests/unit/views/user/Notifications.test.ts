// @ts-nocheck
import { describe, it, expect, vi, beforeEach, beforeAll } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { elementPlusStubs, lucideIconsStub, routerMock } from '../../helpers/elementPlusMock'

const mockGetNotifications = vi.fn()
const mockMarkAsRead = vi.fn()
const mockMarkAllAsRead = vi.fn()

vi.mock('@/api/services/notification', () => ({
  default: {
    getNotifications: (...args: any[]) => mockGetNotifications(...args),
    markAsRead: (...args: any[]) => mockMarkAsRead(...args),
    markAllAsRead: (...args: any[]) => mockMarkAllAsRead(...args),
  }
}))

vi.mock('@/store', () => ({
  useUserStore: () => ({
    user: {
      id: 1,
      nickname: '测试用户',
      username: 'testuser',
    }
  })
}))

vi.mock('@/utils/websocket', () => ({
  wsService: {
    connect: vi.fn().mockResolvedValue(undefined),
    disconnect: vi.fn(),
    onMessage: vi.fn(),
  }
}))

vi.mock('@/api/config/axios', () => ({
  getToken: () => 'mock-token'
}))

vi.mock('vue-router', () => ({
  useRouter: () => routerMock,
}))

vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn(),
  },
}))

vi.mock('@/components/user/PageHeader.vue', () => ({
  default: {
    template: '<div class="page-header-stub"><slot name="action" /></div>',
    props: ['title', 'subtitle'],
  }
}))

vi.mock('@/components/user/NotificationCard.vue', () => ({
  default: {
    template: '<div class="notification-card-stub" />',
    props: ['id', 'type', 'title', 'content', 'time', 'isRead'],
    emits: ['click', 'read'],
  }
}))

vi.mock('@/components/user/EmptyState.vue', () => ({
  default: {
    template: '<div class="empty-state-stub"><slot name="action" /></div>',
    props: ['title', 'description'],
  }
}))

let Notifications: any
beforeAll(async () => {
  const mod = await import('@/views/user/Notifications.vue')
  Notifications = mod.default
})

beforeEach(() => {
  setActivePinia(createPinia())
  vi.clearAllMocks()
  mockGetNotifications.mockResolvedValue({
    data: {
      content: [
        { id: '1', notificationType: 'ORDER', title: '订单通知', content: '您有新订单', isRead: false, createdAt: new Date().toISOString() }
      ],
      totalElements: 1
    }
  })
  mockMarkAsRead.mockResolvedValue({ code: 200 })
  mockMarkAllAsRead.mockResolvedValue({ code: 200 })
})

const mountNotifications = (options = {}) => {
  return mount(Notifications, {
    global: {
      plugins: [createPinia()],
      stubs: {
        ...elementPlusStubs,
        ...lucideIconsStub,
      },
    },
    ...options,
  })
}

describe('Notifications.vue 消息通知页面', () => {
  describe('组件渲染', () => {
    it('应该渲染通知页面', () => {
      const wrapper = mountNotifications()
      expect(wrapper.find('.notifications-page').exists()).toBe(true)
    })

    it('应该显示通知列表', () => {
      const wrapper = mountNotifications()
      expect(wrapper.find('.notifications-list').exists()).toBe(true)
    })

    it('空状态时应显示EmptyState组件', async () => {
      mockGetNotifications.mockResolvedValue({ data: { content: [], totalElements: 0 } })
      const wrapper = mountNotifications()
      await vi.waitFor(() => {
        expect(mockGetNotifications).toHaveBeenCalled()
      })
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.empty-state-stub').exists()).toBe(true)
    })
  })

  describe('数据加载', () => {
    it('应在挂载时加载通知', async () => {
      mountNotifications()
      await vi.waitFor(() => {
        expect(mockGetNotifications).toHaveBeenCalled()
      })
    })

    it('应正确处理通知数据', async () => {
      const wrapper = mountNotifications()
      await vi.waitFor(() => {
        expect(mockGetNotifications).toHaveBeenCalled()
      })
      expect(wrapper.vm.notifications.length).toBeGreaterThan(0)
    })

    it('加载失败时不应有通知数据', async () => {
      mockGetNotifications.mockRejectedValue(new Error('网络错误'))
      const wrapper = mountNotifications()
      await vi.waitFor(() => {
        expect(mockGetNotifications).toHaveBeenCalled()
      })
      expect(wrapper.vm.notifications.length).toBe(0)
    })
  })

  describe('方法存在性', () => {
    it('应该有loadNotifications方法', () => {
      const wrapper = mountNotifications()
      expect(typeof wrapper.vm.loadNotifications).toBe('function')
    })

    it('应该有markAsRead方法', () => {
      const wrapper = mountNotifications()
      expect(typeof wrapper.vm.markAsRead).toBe('function')
    })

    it('应该有markAllAsRead方法', () => {
      const wrapper = mountNotifications()
      expect(typeof wrapper.vm.markAllAsRead).toBe('function')
    })

    it('应该有handleNotification方法', () => {
      const wrapper = mountNotifications()
      expect(typeof wrapper.vm.handleNotification).toBe('function')
    })

    it('应该有formatTime方法', () => {
      const wrapper = mountNotifications()
      expect(typeof wrapper.vm.formatTime).toBe('function')
    })
  })

  describe('标记已读', () => {
    it('markAsRead应调用API并更新状态', async () => {
      const wrapper = mountNotifications()
      await vi.waitFor(() => {
        expect(mockGetNotifications).toHaveBeenCalled()
      })
      await wrapper.vm.markAsRead('1')
      expect(mockMarkAsRead).toHaveBeenCalledWith('1')
      expect(wrapper.vm.notifications[0].isRead).toBe(true)
    })

    it('markAllAsRead应调用API并标记所有为已读', async () => {
      const wrapper = mountNotifications()
      await vi.waitFor(() => {
        expect(mockGetNotifications).toHaveBeenCalled()
      })
      await wrapper.vm.markAllAsRead()
      expect(mockMarkAllAsRead).toHaveBeenCalled()
      expect(wrapper.vm.notifications.every((n: any) => n.isRead)).toBe(true)
    })
  })

  describe('时间格式化', () => {
    it('formatTime对于空值返回空字符串', () => {
      const wrapper = mountNotifications()
      expect(wrapper.vm.formatTime('')).toBe('')
    })

    it('formatTime格式化近期时间', () => {
      const wrapper = mountNotifications()
      const now = new Date()
      now.setMinutes(now.getMinutes() - 5)
      const result = wrapper.vm.formatTime(now.toISOString())
      expect(result).toContain('分钟前')
    })
  })
})
