import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { setActivePinia, createPinia } from 'pinia'

// Hoisted refs - 确保在 ESM mock 模块作用域内共享
const { mockLogin, mockLogout, mockIsLoggedIn } = vi.hoisted(() => ({
  mockLogin: vi.fn(),
  mockLogout: vi.fn(),
  mockIsLoggedIn: false
}))

const ErrorHandlerMock = vi.hoisted(() => ({
  handleLoginError: vi.fn().mockResolvedValue({ message: '登录失败' }),
  classifyError: vi.fn().mockReturnValue({ type: 'UNKNOWN_ERROR' })
}))

const ElMessageMock = vi.hoisted(() => ({
  success: vi.fn(),
  error: vi.fn(),
  warning: vi.fn(),
  info: vi.fn()
}))

// Mock 必须在组件 import 之前
vi.mock('element-plus', () => ({
  ElMessage: ElMessageMock,
  ElMessageBox: {
    confirm: vi.fn().mockResolvedValue(true),
    alert: vi.fn()
  }
}))

vi.mock('@/utils/errorHandler', () => ({
  default: ErrorHandlerMock
}))

vi.mock('@/store', () => ({
  userStore: () => ({
    login: mockLogin,
    logout: mockLogout,
    isLoggedIn: mockIsLoggedIn,
    user: null,
    loading: false
  })
}))

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'Home', component: { template: '<div>Home</div>' } },
    { path: '/login', name: 'Login', component: { template: '<div>Login</div>' } },
    { path: '/register', name: 'Register', component: { template: '<div>Register</div>' } }
  ]
})

let Login
beforeAll(async () => {
  const mod = await import('@/views/Login.vue')
  Login = mod.default
})

beforeEach(() => {
  setActivePinia(createPinia())
  vi.clearAllMocks()
  mockLogin.mockReset()
  mockLogin.mockResolvedValue({ token: 'test-token', user: { id: 1 } })
  mockLogout.mockReset()
    ErrorHandlerMock.handleLoginError.mockResolvedValue({ message: '登录失败' })
})

const mountLogin = (options = {}) => {
  return mount(Login, {
    global: {
      plugins: [router],
      stubs: {
        'el-card': { template: '<div class="el-card"><slot /></div>' },
        'el-form': { template: '<form @submit.prevent="$emit(\'submit\')"><slot /></form>' },
        'el-form-item': { template: '<div class="el-form-item"><slot /></div>' },
        'el-input': {
          template: '<div class="el-input-stub"><input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" /></div>',
          props: ['modelValue', 'type', 'placeholder'],
          emits: ['update:modelValue']
        },
        'el-button': {
          template: '<button :disabled="loading" type="button" @click="$emit(\'click\')"><slot /></button>',
          props: ['loading', 'type', 'native-type']
        },
        'el-checkbox': {
          template: '<input type="checkbox" :checked="modelValue" @change="$emit(\'update:modelValue\', $event.target.checked)" />',
          props: ['modelValue', 'label']
        },
        'el-link': { template: '<a @click="$emit(\'click\')"><slot /></a>' },
        'router-link': { template: '<a @click="$emit(\'click\')"><slot /></a>' }
      }
    },
    ...options
  })
}

describe('Login Component', () => {
  describe('组件渲染', () => {
    it('should render login page', () => {
      const wrapper = mountLogin()
      expect(wrapper.text().length > 0).toBe(true)
    })

    it('should render username and password inputs', () => {
      const wrapper = mountLogin()
      const inputs = wrapper.findAll('input')
      expect(inputs.length).toBeGreaterThanOrEqual(2)
    })

    it('should render login button', () => {
      const wrapper = mountLogin()
      const buttons = wrapper.findAll('button')
      expect(buttons.length).toBeGreaterThan(0)
    })

    it('should render register link', () => {
      const wrapper = mountLogin()
      expect(wrapper.text()).toContain('注册')
    })
  })

  describe('表单状态', () => {
    it('should have correct initial form state', () => {
      const wrapper = mountLogin()
      expect(wrapper.vm.loginForm).toBeDefined()
      expect(wrapper.vm.loginForm.username).toBe('')
      expect(wrapper.vm.loginForm.password).toBe('')
    })

    it('should update form data on input', async () => {
      const wrapper = mountLogin()
      const inputs = wrapper.findAll('input')

      await inputs[0].setValue('testuser')
      expect(wrapper.vm.loginForm.username).toBe('testuser')

      await inputs[1].setValue('password123')
      expect(wrapper.vm.loginForm.password).toBe('password123')
    })

    it('should toggle remember me checkbox', async () => {
      const wrapper = mountLogin()
      const checkbox = wrapper.find('input[type="checkbox"]')
      if (checkbox.exists()) {
        expect(wrapper.vm.rememberMe).toBe(false)
        await checkbox.setChecked()
        expect(wrapper.vm.rememberMe).toBe(true)
      }
    })
  })

  describe('登录功能', () => {
    it('should have handleLogin method', () => {
      const wrapper = mountLogin()
      expect(typeof wrapper.vm.handleLogin).toBe('function')
    })

    it('should have loginForm reactive state', () => {
      const wrapper = mountLogin()
      expect(wrapper.vm.loginForm).toBeDefined()
      expect(typeof wrapper.vm.loginForm).toBe('object')
    })

    it('should have loading state', () => {
      const wrapper = mountLogin()
      expect(typeof wrapper.vm.loading).toBe('boolean')
    })
  })

  describe('导航链接', () => {
    it('should have register link visible', () => {
      const wrapper = mountLogin()
      expect(wrapper.text()).toContain('立即注册')
    })
  })
})
