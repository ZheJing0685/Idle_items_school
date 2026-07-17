// @ts-nocheck
import { describe, it, expect, vi, beforeEach, beforeAll } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { elementPlusStubs, lucideIconsStub, routerMock, routeMock } from '../../helpers/elementPlusMock'

const mockGetChats = vi.fn()
const mockGetMessages = vi.fn()
const mockSendMessage = vi.fn()

vi.mock('@/api/services/chat', () => ({
  default: {
    getChats: (...args: any[]) => mockGetChats(...args),
    getMessages: (...args: any[]) => mockGetMessages(...args),
    sendMessage: (...args: any[]) => mockSendMessage(...args),
  }
}))

vi.mock('@/store', () => ({
  useUserStore: () => ({
    user: {
      id: 1,
      nickname: '测试用户',
      username: 'testuser',
      avatar: null,
    }
  })
}))

vi.mock('@/utils/websocket', () => ({
  wsManager: {
    subscribe: vi.fn(),
    unsubscribe: vi.fn(),
    send: vi.fn(),
    connect: vi.fn().mockResolvedValue(undefined),
    disconnect: vi.fn(),
  },
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
  useRoute: () => routeMock,
  useRouter: () => routerMock,
}))

vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn(),
  },
}))

let Chat: any
beforeAll(async () => {
  const mod = await import('@/views/user/Chat.vue')
  Chat = mod.default
})

beforeEach(() => {
  setActivePinia(createPinia())
  vi.clearAllMocks()
  mockGetChats.mockResolvedValue({
    code: 200,
    data: [
      {
        id: '1',
        buyerId: 1,
        sellerId: 2,
        buyerNickname: '测试用户',
        sellerNickname: '卖家用户',
        buyerUsername: 'testuser',
        sellerUsername: 'seller',
        lastMessage: '你好',
        lastMessageTime: new Date().toISOString(),
      }
    ]
  })
  mockGetMessages.mockResolvedValue({
    data: { content: [] }
  })
  mockSendMessage.mockResolvedValue({ code: 200 })
})

const mountChat = (options = {}) => {
  return mount(Chat, {
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

describe('Chat.vue 聊天页面', () => {
  describe('组件渲染', () => {
    it('应该渲染聊天页面', () => {
      const wrapper = mountChat()
      expect(wrapper.find('.chat-page').exists()).toBe(true)
    })

    it('应该显示左侧会话列表', () => {
      const wrapper = mountChat()
      expect(wrapper.find('.chat-sidebar').exists()).toBe(true)
    })

    it('应该显示右侧聊天窗口', () => {
      const wrapper = mountChat()
      expect(wrapper.find('.chat-main').exists()).toBe(true)
    })

    it('应该显示消息标题', () => {
      const wrapper = mountChat()
      expect(wrapper.find('.sidebar-header h3').text()).toBe('消息')
    })

    it('应该显示未选择会话提示', () => {
      const wrapper = mountChat()
      expect(wrapper.find('.no-chat-selected').exists()).toBe(true)
      expect(wrapper.find('.no-chat-selected').text()).toContain('选择一个会话开始聊天')
    })
  })

  describe('会话列表', () => {
    it('应该显示聊天列表', async () => {
      const wrapper = mountChat()
      await vi.waitFor(() => {
        expect(mockGetChats).toHaveBeenCalled()
      })
      expect(wrapper.vm.chatList.length).toBeGreaterThan(0)
    })

    it('应该加载聊天列表数据', async () => {
      const wrapper = mountChat()
      await vi.waitFor(() => {
        expect(mockGetChats).toHaveBeenCalled()
      })
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.chatList.length).toBeGreaterThan(0)
    })

    it('chatList数据应正确设置', async () => {
      const wrapper = mountChat()
      await vi.waitFor(() => {
        expect(mockGetChats).toHaveBeenCalled()
      })
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.chatList[0].id).toBe('1')
    })

    it('空列表时chatList为空', async () => {
      mockGetChats.mockResolvedValue({ code: 200, data: [] })
      const wrapper = mountChat()
      await vi.waitFor(() => {
        expect(mockGetChats).toHaveBeenCalled()
      })
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.chatList.length).toBe(0)
    })
  })

  describe('方法存在性', () => {
    it('应该有selectChat方法', () => {
      const wrapper = mountChat()
      expect(typeof wrapper.vm.selectChat).toBe('function')
    })

    it('应该有sendMessage方法', () => {
      const wrapper = mountChat()
      expect(typeof wrapper.vm.sendMessage).toBe('function')
    })

    it('应该有getOtherUser方法', () => {
      const wrapper = mountChat()
      expect(typeof wrapper.vm.getOtherUser).toBe('function')
    })

    it('应该有formatTime方法', () => {
      const wrapper = mountChat()
      expect(typeof wrapper.vm.formatTime).toBe('function')
    })
  })

  describe('交互功能', () => {
    it('选择会话应设置currentChat', async () => {
      const wrapper = mountChat()
      await vi.waitFor(() => {
        expect(mockGetChats).toHaveBeenCalled()
      })
      const chat = wrapper.vm.chatList[0]
      await wrapper.vm.selectChat(chat)
      expect(wrapper.vm.currentChat).toEqual(chat)
    })

    it('发送空消息不应调用API', async () => {
      const wrapper = mountChat()
      await vi.waitFor(() => {
        expect(mockGetChats).toHaveBeenCalled()
      })
      await wrapper.vm.selectChat(wrapper.vm.chatList[0])
      wrapper.vm.newMessage = ''
      await wrapper.vm.sendMessage()
      expect(mockSendMessage).not.toHaveBeenCalled()
    })

    it('发送消息后应清空输入', async () => {
      const wrapper = mountChat()
      await vi.waitFor(() => {
        expect(mockGetChats).toHaveBeenCalled()
      })
      await wrapper.vm.selectChat(wrapper.vm.chatList[0])
      wrapper.vm.newMessage = '测试消息'
      await wrapper.vm.sendMessage()
      expect(wrapper.vm.newMessage).toBe('')
    })
  })

  describe('getOtherUser方法', () => {
    it('当chat为null时返回空对象', () => {
      const wrapper = mountChat()
      expect(wrapper.vm.getOtherUser(null)).toEqual({})
    })

    it('当用户是买家时返回卖家信息', () => {
      const wrapper = mountChat()
      const chat = { buyerId: 1, sellerId: 2, sellerNickname: '卖家', sellerUsername: 'seller' }
      const result = wrapper.vm.getOtherUser(chat)
      expect(result.id).toBe(2)
      expect(result.nickname).toBe('卖家')
    })
  })

  describe('消息格式化', () => {
    it('formatTime对于空值返回空字符串', () => {
      const wrapper = mountChat()
      expect(wrapper.vm.formatTime(null)).toBe('')
      expect(wrapper.vm.formatTime('')).toBe('')
    })

    it('formatMessageTime对于空值返回空字符串', () => {
      const wrapper = mountChat()
      expect(wrapper.vm.formatMessageTime('')).toBe('')
    })

    it('formatMessageTime格式化时间字符串', () => {
      const wrapper = mountChat()
      const time = wrapper.vm.formatMessageTime('2024-01-15T10:30:00')
      expect(time).toContain(':')
    })
  })
})
