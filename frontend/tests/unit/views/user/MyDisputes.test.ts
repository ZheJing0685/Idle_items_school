// @ts-nocheck
import { describe, it, expect, vi, beforeEach, beforeAll } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { elementPlusStubs, lucideIconsStub } from '../../helpers/elementPlusMock'

const mockListDisputes = vi.fn()
const mockGetDispute = vi.fn()
const mockReplyDispute = vi.fn()
const mockSatisfactionDispute = vi.fn()
const mockElMessageBoxConfirm = vi.fn().mockResolvedValue(true)

vi.mock('@/api', () => ({
  default: {
    user: {
      disputes: {
        list: (...args: any[]) => mockListDisputes(...args),
        get: (...args: any[]) => mockGetDispute(...args),
        reply: (...args: any[]) => mockReplyDispute(...args),
        satisfaction: (...args: any[]) => mockSatisfactionDispute(...args),
      }
    }
  }
}))

vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn(),
  },
  ElMessageBox: {
    confirm: (...args: any[]) => mockElMessageBoxConfirm(...args),
  },
}))

let MyDisputes: any
beforeAll(async () => {
  const mod = await import('@/views/user/MyDisputes.vue')
  MyDisputes = mod.default
})

beforeEach(() => {
  setActivePinia(createPinia())
  vi.clearAllMocks()
  mockListDisputes.mockResolvedValue({
    code: 200,
    data: {
      content: [
        {
          id: '1',
          disputeNo: 'D20240115001',
          disputeStatus: 'PENDING',
          disputeType: 1,
          orderId: '1001',
          itemTitle: '测试物品',
          reason: '物品与描述不符',
          description: '详细描述',
          createdAt: new Date().toISOString(),
        }
      ],
      totalElements: 1
    }
  })
  mockGetDispute.mockResolvedValue({
    code: 200,
    data: {
      id: '1',
      disputeNo: 'D20240115001',
      disputeStatus: 'PROCESSING',
      disputeType: 1,
      orderId: '1001',
      reason: '物品与描述不符',
      description: '详细描述',
      createdAt: new Date().toISOString(),
      processLogs: '[]',
    }
  })
  mockReplyDispute.mockResolvedValue({ code: 200 })
  mockSatisfactionDispute.mockResolvedValue({ code: 200 })
  mockElMessageBoxConfirm.mockResolvedValue(true)
})

const mountMyDisputes = (options = {}) => {
  return mount(MyDisputes, {
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

describe('MyDisputes.vue 我的纠纷页面', () => {
  describe('组件渲染', () => {
    it('应该渲染纠纷页面', () => {
      const wrapper = mountMyDisputes()
      expect(wrapper.find('.my-disputes').exists()).toBe(true)
    })

    it('应该显示页面标题', () => {
      const wrapper = mountMyDisputes()
      expect(wrapper.text()).toContain('我的纠纷')
    })

    it('应该显示统计卡片', () => {
      const wrapper = mountMyDisputes()
      expect(wrapper.find('.stats-cards').exists()).toBe(true)
    })

    it('应该显示全部纠纷统计', () => {
      const wrapper = mountMyDisputes()
      expect(wrapper.text()).toContain('全部纠纷')
    })

    it('应该显示待处理统计', () => {
      const wrapper = mountMyDisputes()
      expect(wrapper.text()).toContain('待处理')
    })

    it('应该包含分页组件', async () => {
      const wrapper = mountMyDisputes()
      await vi.waitFor(() => {
        expect(mockListDisputes).toHaveBeenCalled()
      })
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.pagination').exists()).toBe(true)
    })
  })

  describe('数据加载', () => {
    it('应在挂载时加载纠纷列表', async () => {
      mountMyDisputes()
      await vi.waitFor(() => {
        expect(mockListDisputes).toHaveBeenCalled()
      })
    })

    it('应正确处理纠纷数据', async () => {
      const wrapper = mountMyDisputes()
      await vi.waitFor(() => {
        expect(mockListDisputes).toHaveBeenCalled()
      })
      expect(wrapper.vm.disputes.length).toBeGreaterThan(0)
    })

    it('加载失败时应显示错误消息', async () => {
      mockListDisputes.mockRejectedValue(new Error('网络错误'))
      const wrapper = mountMyDisputes()
      await vi.waitFor(() => {
        expect(mockListDisputes).toHaveBeenCalled()
      })
      expect(wrapper.vm.loading).toBe(false)
    })
  })

  describe('方法存在性', () => {
    it('应该有fetchDisputes方法', () => {
      const wrapper = mountMyDisputes()
      expect(typeof wrapper.vm.fetchDisputes).toBe('function')
    })

    it('应该有viewDetail方法', () => {
      const wrapper = mountMyDisputes()
      expect(typeof wrapper.vm.viewDetail).toBe('function')
    })

    it('应该有submitReply方法', () => {
      const wrapper = mountMyDisputes()
      expect(typeof wrapper.vm.submitReply).toBe('function')
    })

    it('应该有submitEvaluate方法', () => {
      const wrapper = mountMyDisputes()
      expect(typeof wrapper.vm.submitEvaluate).toBe('function')
    })

    it('应该有formatTime方法', () => {
      const wrapper = mountMyDisputes()
      expect(typeof wrapper.vm.formatTime).toBe('function')
    })

    it('应该有parseLogs方法', () => {
      const wrapper = mountMyDisputes()
      expect(typeof wrapper.vm.parseLogs).toBe('function')
    })
  })

  describe('状态处理', () => {
    it('getStatusLabel应返回正确的状态标签', () => {
      const wrapper = mountMyDisputes()
      expect(wrapper.vm.getStatusLabel('PENDING')).toBe('待处理')
      expect(wrapper.vm.getStatusLabel('PROCESSING')).toBe('处理中')
      expect(wrapper.vm.getStatusLabel('RESOLVED')).toBe('已解决')
    })

    it('getStatusClass应返回正确的CSS类', () => {
      const wrapper = mountMyDisputes()
      expect(wrapper.vm.getStatusClass('PENDING')).toBe('status-pending')
      expect(wrapper.vm.getStatusClass('PROCESSING')).toBe('status-processing')
      expect(wrapper.vm.getStatusClass('RESOLVED')).toBe('status-resolved')
    })

    it('getDisputeTypeLabel应返回纠纷类型文本', () => {
      const wrapper = mountMyDisputes()
      expect(wrapper.vm.getDisputeTypeLabel(1)).toBe('商品问题')
      expect(wrapper.vm.getDisputeTypeLabel(2)).toBe('物流问题')
      expect(wrapper.vm.getDisputeTypeLabel(3)).toBe('退款问题')
    })
  })

  describe('权限控制', () => {
    it('canReply对于PENDING状态应返回true', () => {
      const wrapper = mountMyDisputes()
      expect(wrapper.vm.canReply({ disputeStatus: 'PENDING' })).toBe(true)
    })

    it('canReply对于RESOLVED状态应返回false', () => {
      const wrapper = mountMyDisputes()
      expect(wrapper.vm.canReply({ disputeStatus: 'RESOLVED' })).toBe(false)
    })

    it('canEvaluate对于已解决且未评价状态应返回true', () => {
      const wrapper = mountMyDisputes()
      expect(wrapper.vm.canEvaluate({ disputeStatus: 'RESOLVED', satisfaction: null })).toBe(true)
    })

    it('canEvaluate对于已评价状态应返回false', () => {
      const wrapper = mountMyDisputes()
      expect(wrapper.vm.canEvaluate({ disputeStatus: 'RESOLVED', satisfaction: 5 })).toBe(false)
    })
  })

  describe('详情查看', () => {
    it('viewDetail应调用API并打开对话框', async () => {
      const wrapper = mountMyDisputes()
      await vi.waitFor(() => {
        expect(mockListDisputes).toHaveBeenCalled()
      })
      await wrapper.vm.viewDetail({ id: '1' })
      expect(mockGetDispute).toHaveBeenCalledWith('1')
      expect(wrapper.vm.detailVisible).toBe(true)
    })
  })
})
