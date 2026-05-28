import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import Profile from '@/views/user/Profile.vue'
import { elementPlusStubs } from '../../helpers/elementPlusMock'

// Mock API
const { mockGetProfile } = vi.hoisted(() => ({
  mockGetProfile: vi.fn()
}))

vi.mock('@/api', () => ({
  default: {
    user: {
      getProfile: mockGetProfile
    }
  }
}))

// Mock store
vi.mock('../../../../src/store', () => ({
  userStore: () => ({
    user: null,
    updateProfile: vi.fn()
  })
}))

// Mock getToken
vi.mock('../../../../src/api/config/axios', () => ({
  getToken: () => 'mock-token'
}))

describe('用户资料页面', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockGetProfile.mockReset()
    mockGetProfile.mockResolvedValue({
      code: 200,
      data: {
        username: 'testuser',
        nickname: '测试用户',
        email: 'test@example.com',
        phone: '13800138000',
        avatar: '',
        gender: 1,
        birthday: '2000-01-01',
        bio: '这是个人简介',
        schoolName: '测试大学',
        studentId: '2020001',
        createdAt: '2024-01-01',
        lastLoginTime: '2024-01-02',
        creditScore: 95,
        totalTransactions: 10,
        totalSales: 5,
        totalPurchases: 5
      }
    })
  })

  it('应该渲染用户资料页面', () => {
    const wrapper = mount(Profile, {
      global: {
        plugins: [createPinia()],
        stubs: elementPlusStubs
      }
    })
    
    expect(wrapper.exists()).toBe(true)
  })

  it('应该包含保存按钮', () => {
    const wrapper = mount(Profile, {
      global: {
        plugins: [createPinia()],
        stubs: elementPlusStubs
      }
    })
    
    expect(wrapper.find('button').exists()).toBe(true)
  })

  it('应该显示用户名', async () => {
    const wrapper = mount(Profile, {
      global: {
        plugins: [createPinia()],
        stubs: elementPlusStubs
      }
    })
    
    // 等待API调用完成
    await vi.waitFor(() => {
      expect(mockGetProfile).toHaveBeenCalled()
    })
    
    // 检查form.username的值
    expect(wrapper.vm.form.username).toBe('testuser')
  })
})