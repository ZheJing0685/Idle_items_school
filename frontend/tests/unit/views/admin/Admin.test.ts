// @ts-nocheck
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import Admin from '@/views/admin/Admin.vue'
import { elementPlusStubs, lucideIconsStub } from '../../helpers/elementPlusMock'
import { createRouteMock, createRouterMock } from '../../helpers/routerMock'

// Mock vue-router
vi.mock('vue-router', () => ({
  useRoute: vi.fn(() => createRouteMock({ 
    path: '/admin',
    matched: [{ path: '/admin', meta: { title: '管理后台' } }],
  })),
  useRouter: vi.fn(() => createRouterMock()),
}))

// Mock Element Plus
vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn(),
  },
}))

// Mock userStore
const mockLogout = vi.fn()
const mockUser = { nickname: '测试管理员', role: 'ADMIN' }

vi.mock('@/store', () => ({
  userStore: vi.fn(() => ({
    user: mockUser,
    logout: mockLogout,
  })),
}))

// Mock lucide icons
const lucideIcons = [
  'LayoutDashboard', 'Users', 'CheckCircle', 'Package', 'Menu',
  'MessageSquare', 'ClipboardList', 'AlertTriangle', 'TrendingUp',
  'FileText', 'LogOut', 'Search', 'Bell',
]
const lucideStubs = Object.fromEntries(
  lucideIcons.map((name) => [name, { template: '<div class="icon" />', props: ['size', 'strokeWidth'] }])
)

const stubs = {
  ...elementPlusStubs,
  ...lucideStubs,
  'router-link': {
    template: '<a class="router-link-stub"><slot /></a>',
    props: ['to'],
  },
  'router-view': {
    template: '<div class="router-view-stub"><slot /></div>',
  },
}

describe('Admin.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  const mountAdmin = (options = {}) => {
    return mount(Admin, {
      global: {
        plugins: [createPinia()],
        stubs,
        ...options,
      },
    })
  }

  it('应该正确渲染管理后台壳布局', () => {
    const wrapper = mountAdmin()
    expect(wrapper.find('.admin-shell').exists()).toBe(true)
    expect(wrapper.find('.admin-sidebar').exists()).toBe(true)
    expect(wrapper.find('.admin-main').exists()).toBe(true)
  })

  it('应该显示品牌名称', () => {
    const wrapper = mountAdmin()
    expect(wrapper.find('.brand-name').text()).toBe('闲置物品平台')
    expect(wrapper.find('.brand-tagline').text()).toBe('管理后台')
  })

  it('应该包含导航菜单项', () => {
    const wrapper = mountAdmin()
    const navTexts = wrapper.findAll('.nav-text').map(el => el.text())
    expect(navTexts).toContain('用户管理')
    expect(navTexts).toContain('物品管理')
    expect(navTexts).toContain('订单管理')
    expect(navTexts).toContain('分类管理')
  })

  it('应该显示管理员用户信息', () => {
    const wrapper = mountAdmin()
    expect(wrapper.find('.user-name').text()).toBe('测试管理员')
    expect(wrapper.find('.user-role').text()).toBe('管理员')
  })

  it('应该有退出登录按钮', () => {
    const wrapper = mountAdmin()
    const logoutBtn = wrapper.find('.logout-btn')
    expect(logoutBtn.exists()).toBe(true)
  })

  it('点击退出登录应该调用logout方法', async () => {
    const wrapper = mountAdmin()
    await wrapper.find('.logout-btn').trigger('click')
    expect(mockLogout).toHaveBeenCalled()
  })

  it('isCollapsed 初始值应为 false', () => {
    const wrapper = mountAdmin()
    expect(wrapper.find('.admin-sidebar.is-collapsed').exists()).toBe(false)
  })

  it('点击品牌区域应切换侧边栏折叠状态', async () => {
    const wrapper = mountAdmin()
    const brandMark = wrapper.find('.brand-mark')
    expect(brandMark.exists()).toBe(true)
    await brandMark.trigger('click')
    await wrapper.vm.$nextTick()
    expect(wrapper.find('.admin-sidebar.is-collapsed').exists()).toBe(true)
  })

  it('页面标题应显示当前路由对应的标题', () => {
    const wrapper = mountAdmin()
    expect(wrapper.find('.page-title').text()).toBe('控制台')
  })

  it('应该包含搜索输入框', () => {
    const wrapper = mountAdmin()
    const searchInput = wrapper.find('.search-input')
    expect(searchInput.exists()).toBe(true)
  })

  it('应该包含通知按钮', () => {
    const wrapper = mountAdmin()
    expect(wrapper.find('.header-action').exists()).toBe(true)
    expect(wrapper.find('.notification-badge').text()).toBe('3')
  })

  it('getRoleText 方法应返回管理员', () => {
    const wrapper = mountAdmin()
    const vm = wrapper.vm as any
    expect(vm.getRoleText()).toBe('管理员')
  })

  it('页面内容区域应存在', () => {
    const wrapper = mountAdmin()
    expect(wrapper.find('.admin-content').exists()).toBe(true)
  })
})
