// @ts-nocheck
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import UserManagement from '@/views/admin/UserManagement.vue'
import { elementPlusStubs, lucideIconsStub } from '../../helpers/elementPlusMock'

// Mock API
const mockGetUsers = vi.fn()
const mockGetUserStats = vi.fn()
const mockUpdateStatus = vi.fn()
const mockDeleteUsers = vi.fn()
const mockBatchUpdateStatus = vi.fn()
const mockBatchDelete = vi.fn()
const mockUpdateUser = vi.fn()
const mockCreateUser = vi.fn()
const mockExportUsers = vi.fn()

vi.mock('@/api', () => ({
  default: {
    admin: {
      users: {
        getUsers: (...args: any[]) => mockGetUsers(...args),
        getUserStats: (...args: any[]) => mockGetUserStats(...args),
        updateStatus: (...args: any[]) => mockUpdateStatus(...args),
        deleteUsers: (...args: any[]) => mockDeleteUsers(...args),
        batchUpdateStatus: (...args: any[]) => mockBatchUpdateStatus(...args),
        batchDelete: (...args: any[]) => mockBatchDelete(...args),
        updateUser: (...args: any[]) => mockUpdateUser(...args),
        createUser: (...args: any[]) => mockCreateUser(...args),
        exportUsers: (...args: any[]) => mockExportUsers(...args),
      },
    },
  },
}))

// Mock Element Plus
vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn(),
  },
  ElMessageBox: {
    confirm: vi.fn().mockResolvedValue(undefined),
    prompt: vi.fn().mockResolvedValue({ value: '原因' }),
  },
}))

// Mock lucide icons
const lucideIcons = [
  'User', 'Clock', 'CheckCircle', 'Plus', 'Download', 'Search',
  'Eye', 'Edit3', 'Ban', 'Trash2',
]
const lucideStubs = Object.fromEntries(
  lucideIcons.map((name) => [name, { template: '<div class="icon" />', props: ['size', 'strokeWidth'] }])
)

const stubs = {
  ...elementPlusStubs,
  ...lucideStubs,
  'el-table-column': {
    template: '<div class="el-table-column"><slot :row="{}" /></div>',
    props: ['label', 'prop', 'width', 'minWidth', 'fixed', 'type', 'selectable'],
  },
  'router-link': {
    template: '<a class="router-link-stub"><slot /></a>',
    props: ['to'],
  },
}

describe('UserManagement.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    mockGetUsers.mockResolvedValue({
      code: 200,
      data: {
        content: [
          { id: 1, username: 'user1', nickname: '用户一', email: 'user1@test.com', role: 'STUDENT', status: 'ACTIVE', verified: true, creditScore: 95 },
          { id: 2, username: 'user2', nickname: '用户二', email: 'user2@test.com', role: 'ADMIN', status: 'ACTIVE', verified: false, creditScore: 80 },
        ],
        totalElements: 2,
      },
    })
    mockGetUserStats.mockResolvedValue({
      code: 200,
      data: { total: 100, active: 80, verified: 50, newThisWeek: 10 },
    })
  })

  const mountUserManagement = (options = {}) => {
    return mount(UserManagement, {
      global: {
        plugins: [createPinia()],
        stubs,
        ...options,
      },
    })
  }

  it('应该正确渲染用户管理页面', () => {
    const wrapper = mountUserManagement()
    expect(wrapper.find('.user-management').exists()).toBe(true)
  })

  it('应该显示页面标题和描述', () => {
    const wrapper = mountUserManagement()
    expect(wrapper.find('.section-title').text()).toBe('用户概览')
    expect(wrapper.find('.section-desc').text()).toContain('管理平台用户账户')
  })

  it('应该包含用户统计卡片', () => {
    const wrapper = mountUserManagement()
    const statCards = wrapper.findAll('.stat-card')
    expect(statCards.length).toBe(4)
  })

  it('应该显示总用户数统计', () => {
    const wrapper = mountUserManagement()
    const statValues = wrapper.findAll('.stat-value')
    expect(statValues.length).toBeGreaterThan(0)
  })

  it('应该包含搜索输入框', () => {
    const wrapper = mountUserManagement()
    expect(wrapper.find('.search-input').exists()).toBe(true)
  })

  it('应该包含添加用户按钮', () => {
    const wrapper = mountUserManagement()
    expect(wrapper.find('.btn-primary').exists()).toBe(true)
  })

  it('应该包含导出按钮', () => {
    const wrapper = mountUserManagement()
    expect(wrapper.find('.btn-ghost').exists()).toBe(true)
  })

  it('应该包含筛选器', () => {
    const wrapper = mountUserManagement()
    const filterSelects = wrapper.findAll('.filter-selects .el-select')
    expect(filterSelects.length).toBeGreaterThanOrEqual(3)
  })

  it('应该包含分页组件', () => {
    const wrapper = mountUserManagement()
    expect(wrapper.find('.pagination-wrapper').exists()).toBe(true)
  })

  it('应该包含详情对话框', () => {
    const wrapper = mountUserManagement()
    const dialogs = wrapper.findAll('.el-dialog')
    expect(dialogs.length).toBeGreaterThanOrEqual(2)
  })

  it('handleSearch 应重置页码并获取数据', async () => {
    const wrapper = mountUserManagement()
    const vm = wrapper.vm as any
    vm.page = 3
    vm.handleSearch()
    await wrapper.vm.$nextTick()
    expect(vm.page).toBe(1)
    expect(mockGetUsers).toHaveBeenCalled()
  })

  it('handleReset 应清空筛选条件', async () => {
    const wrapper = mountUserManagement()
    const vm = wrapper.vm as any
    vm.searchKeyword = 'test'
    vm.userRole = 'ADMIN'
    vm.userStatus = 'ACTIVE'
    vm.handleReset()
    await wrapper.vm.$nextTick()
    expect(vm.searchKeyword).toBe('')
    expect(vm.userRole).toBe('')
    expect(vm.userStatus).toBe('')
  })

  it('handleView 应设置当前用户并打开对话框', () => {
    const wrapper = mountUserManagement()
    const vm = wrapper.vm as any
    const user = { id: 1, username: 'user1' }
    vm.handleView(user)
    expect(vm.currentUser).toEqual(user)
    expect(vm.dialogVisible).toBe(true)
  })

  it('handleEdit 应设置编辑表单并打开编辑对话框', () => {
    const wrapper = mountUserManagement()
    const vm = wrapper.vm as any
    const user = { id: 1, username: 'user1', email: 'test@test.com', phone: '123', nickname: '昵称', role: 'STUDENT', status: 'ACTIVE', studentId: 'S001', gender: 1, bio: '简介', schoolName: '学校' }
    vm.handleEdit(user)
    expect(vm.editForm.username).toBe('user1')
    expect(vm.editDialogVisible).toBe(true)
  })

  it('getAvatarText 应返回昵称首字', () => {
    const wrapper = mountUserManagement()
    const vm = wrapper.vm as any
    expect(vm.getAvatarText({ nickname: '张三', username: 'zhang' })).toBe('张')
    expect(vm.getAvatarText({ nickname: '', username: 'zhang' })).toBe('z')
    expect(vm.getAvatarText({ nickname: '', username: '' })).toBe('用')
  })

  it('getScoreColor 应根据分数返回不同颜色', () => {
    const wrapper = mountUserManagement()
    const vm = wrapper.vm as any
    expect(vm.getScoreColor(90)).toContain('success')
    expect(vm.getScoreColor(70)).toContain('warning')
    expect(vm.getScoreColor(50)).toContain('danger')
  })

  it('formatDate 应格式化日期字符串', () => {
    const wrapper = mountUserManagement()
    const vm = wrapper.vm as any
    expect(vm.formatDate('')).toBe('-')
    expect(vm.formatDate('2024-01-15T10:00:00')).toContain('2024')
  })

  it('formatDateTime 应格式化日期时间字符串', () => {
    const wrapper = mountUserManagement()
    const vm = wrapper.vm as any
    expect(vm.formatDateTime('')).toBe('-')
    expect(vm.formatDateTime('2024-01-15T10:00:00')).toContain('2024')
  })
})
