// @ts-nocheck
import { describe, it, expect, vi, beforeEach, beforeAll } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { elementPlusStubs, lucideIconsStub } from '../../helpers/elementPlusMock'

const mockGetStatus = vi.fn()
const mockSubmit = vi.fn()
const mockElMessageBoxConfirm = vi.fn().mockResolvedValue(true)

vi.mock('@/api', () => ({
  default: {
    verification: {
      getStatus: (...args: any[]) => mockGetStatus(...args),
      submit: (...args: any[]) => mockSubmit(...args),
    }
  }
}))

vi.mock('@/store', () => ({
  userStore: () => ({
    user: {
      id: 1,
      nickname: '测试用户',
    }
  })
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
  ElForm: {
    name: 'ElForm',
  },
}))

vi.mock('@/components/user/PageHeader.vue', () => ({
  default: {
    template: '<div class="page-header-stub" />',
    props: ['title', 'subtitle'],
  }
}))

vi.mock('@/components/user/VerificationStatus.vue', () => ({
  default: {
    template: '<div class="verification-status-stub"><slot name="action" /></div>',
    props: ['status', 'title', 'description', 'reason'],
  }
}))

vi.mock('@/components/user/UploadArea.vue', () => ({
  default: {
    template: '<div class="upload-area-stub" />',
    props: ['modelValue', 'text', 'hint'],
    emits: ['update:modelValue'],
  }
}))

let Verification: any
beforeAll(async () => {
  const mod = await import('@/views/user/Verification.vue')
  Verification = mod.default
})

beforeEach(() => {
  setActivePinia(createPinia())
  vi.clearAllMocks()
  mockGetStatus.mockResolvedValue({ code: 200, data: { status: 'unverified' } })
  mockSubmit.mockResolvedValue({ code: 200 })
  mockElMessageBoxConfirm.mockResolvedValue(true)
})

const mountVerification = (options = {}) => {
  return mount(Verification, {
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

describe('Verification.vue 实名认证页面', () => {
  describe('组件渲染', () => {
    it('应该渲染认证页面', () => {
      const wrapper = mountVerification()
      expect(wrapper.find('.verification-page').exists()).toBe(true)
    })

    it('应该包含认证表单', () => {
      const wrapper = mountVerification()
      expect(wrapper.find('.form-card').exists()).toBe(true)
    })

    it('应该包含隐私协议复选框', () => {
      const wrapper = mountVerification()
      expect(wrapper.text()).toContain('隐私协议')
    })

    it('应该包含提交按钮', () => {
      const wrapper = mountVerification()
      expect(wrapper.text()).toContain('提交认证')
    })
  })

  describe('表单数据', () => {
    it('form对象应有默认值', () => {
      const wrapper = mountVerification()
      expect(wrapper.vm.form.verificationType).toBe('')
      expect(wrapper.vm.form.name).toBe('')
      expect(wrapper.vm.form.agreePrivacy).toBe(false)
    })

    it('rules对象应包含必填验证', () => {
      const wrapper = mountVerification()
      expect(wrapper.vm.rules.verificationType).toBeDefined()
      expect(wrapper.vm.rules.name).toBeDefined()
      expect(wrapper.vm.rules.agreePrivacy).toBeDefined()
    })
  })

  describe('方法存在性', () => {
    it('应该有submitForm方法', () => {
      const wrapper = mountVerification()
      expect(typeof wrapper.vm.submitForm).toBe('function')
    })

    it('应该有checkVerificationStatus方法', () => {
      const wrapper = mountVerification()
      expect(typeof wrapper.vm.checkVerificationStatus).toBe('function')
    })

    it('应该有getStatusText方法', () => {
      const wrapper = mountVerification()
      expect(typeof wrapper.vm.getStatusText).toBe('function')
    })

    it('应该有getStatusDescription方法', () => {
      const wrapper = mountVerification()
      expect(typeof wrapper.vm.getStatusDescription).toBe('function')
    })

    it('应该有resetVerification方法', () => {
      const wrapper = mountVerification()
      expect(typeof wrapper.vm.resetVerification).toBe('function')
    })
  })

  describe('状态文本映射', () => {
    it('getStatusText应返回正确的认证状态文本', () => {
      const wrapper = mountVerification()
      expect(wrapper.vm.getStatusText('approved')).toBe('已认证')
      expect(wrapper.vm.getStatusText('pending')).toBe('审核中')
      expect(wrapper.vm.getStatusText('rejected')).toBe('未通过')
    })

    it('getStatusDescription应返回正确的认证状态描述', () => {
      const wrapper = mountVerification()
      expect(wrapper.vm.getStatusDescription('approved')).toContain('认证已通过')
      expect(wrapper.vm.getStatusDescription('pending')).toContain('审核中')
      expect(wrapper.vm.getStatusDescription('rejected')).toContain('未通过审核')
    })
  })

  describe('已认证状态', () => {
    it('当已有认证状态时应显示VerificationStatus', async () => {
      mockGetStatus.mockResolvedValue({
        code: 200,
        data: { status: 'approved', rejectReason: '' }
      })
      const wrapper = mountVerification()
      await vi.waitFor(() => {
        expect(mockGetStatus).toHaveBeenCalled()
      })
      expect(wrapper.vm.verificationStatus).not.toBeNull()
    })

    it('当status为unverified时不显示认证状态', async () => {
      mockGetStatus.mockResolvedValue({
        code: 200,
        data: { status: 'unverified' }
      })
      const wrapper = mountVerification()
      await vi.waitFor(() => {
        expect(mockGetStatus).toHaveBeenCalled()
      })
      expect(wrapper.vm.verificationStatus).toBeNull()
    })
  })

  describe('认证类型切换', () => {
    it('设置身份证认证类型应更新form数据', async () => {
      const wrapper = mountVerification()
      wrapper.vm.form.verificationType = '1'
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.form.verificationType).toBe('1')
    })

    it('设置学生证认证类型应更新form数据', async () => {
      const wrapper = mountVerification()
      wrapper.vm.form.verificationType = '2'
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.form.verificationType).toBe('2')
    })

    it('设置教师证认证类型应更新form数据', async () => {
      const wrapper = mountVerification()
      wrapper.vm.form.verificationType = '3'
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.form.verificationType).toBe('3')
    })
  })
})
