// @ts-nocheck
import { describe, it, expect, vi, beforeEach, beforeAll } from 'vitest';
import { mount } from '@vue/test-utils';
import { setActivePinia, createPinia } from 'pinia';

// Mock element-plus
vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn(),
    info: vi.fn(),
  },
}));

// Mock router - use importOriginal to keep createRouter, createWebHistory etc.
const mockPush = vi.fn();
vi.mock('vue-router', async (importOriginal) => {
  const actual = await importOriginal() as any;
  return {
    ...actual,
    useRouter: () => ({
      push: mockPush,
    }),
  };
});

// Mock store
const { mockLogin } = vi.hoisted(() => ({
  mockLogin: vi.fn(),
}));

vi.mock('@/store', () => ({
  userStore: () => ({
    login: mockLogin,
    isLoggedIn: false,
    user: null,
    loading: false,
  }),
}));

let Login: any;
beforeAll(async () => {
  const mod = await import('@/views/Login.vue');
  Login = mod.default;
});

beforeEach(() => {
  setActivePinia(createPinia());
  vi.clearAllMocks();
  mockLogin.mockReset();
  mockLogin.mockResolvedValue({ token: 'test-token', user: { id: 1 } });
  mockPush.mockClear();
});

const mountLogin = () => {
  return mount(Login, {
    global: {
      stubs: {
        'el-card': { template: '<div class="el-card"><slot /></div>' },
        'el-form': {
          template: '<form @submit.prevent="$emit(\'submit\')"><slot /></form>',
          methods: {
            validate: vi.fn().mockResolvedValue(true),
          },
        },
        'el-form-item': { template: '<div class="el-form-item"><slot /></div>' },
        'el-input': {
          template: '<div class="el-input-stub"><input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" /></div>',
          props: ['modelValue', 'type', 'placeholder', 'size'],
          emits: ['update:modelValue'],
        },
        'el-button': {
          template: '<button :disabled="loading" type="button" @click="$emit(\'click\')"><slot /></button>',
          props: ['loading', 'type', 'size', 'native-type'],
          emits: ['click'],
        },
        'el-checkbox': {
          template: '<input type="checkbox" :checked="modelValue" @change="$emit(\'update:modelValue\', $event.target.checked)" />',
          props: ['modelValue', 'label'],
          emits: ['update:modelValue'],
        },
        'el-link': { template: '<a @click="$emit(\'click\')"><slot /></a>' },
        'router-link': { template: '<a @click="$emit(\'click\')"><slot /></a>' },
      },
    },
  });
};

describe('Login Component (TypeScript)', () => {
  describe('组件渲染', () => {
    it('should render login page', () => {
      const wrapper = mountLogin();
      expect(wrapper.text().length).toBeGreaterThan(0);
    });

    it('should render username and password inputs', () => {
      const wrapper = mountLogin();
      const inputs = wrapper.findAll('input');
      expect(inputs.length).toBeGreaterThanOrEqual(2);
    });

    it('should render login button', () => {
      const wrapper = mountLogin();
      const buttons = wrapper.findAll('button');
      expect(buttons.length).toBeGreaterThan(0);
    });

    it('should render register link', () => {
      const wrapper = mountLogin();
      expect(wrapper.text()).toContain('注册');
    });

    it('should render welcome text', () => {
      const wrapper = mountLogin();
      expect(wrapper.text()).toContain('欢迎回来');
    });
  });

  describe('表单状态', () => {
    it('should have correct initial form state', () => {
      const wrapper = mountLogin();
      expect(wrapper.vm.loginForm).toBeDefined();
      expect(wrapper.vm.loginForm.username).toBe('');
      expect(wrapper.vm.loginForm.password).toBe('');
    });

    it('should update form data on input', async () => {
      const wrapper = mountLogin();
      const inputs = wrapper.findAll('input');

      await inputs[0].setValue('testuser');
      expect(wrapper.vm.loginForm.username).toBe('testuser');

      await inputs[1].setValue('password123');
      expect(wrapper.vm.loginForm.password).toBe('password123');
    });

    it('should toggle remember me checkbox', async () => {
      const wrapper = mountLogin();
      const checkbox = wrapper.find('input[type="checkbox"]');
      if (checkbox.exists()) {
        expect(wrapper.vm.rememberMe).toBe(false);
        await checkbox.setChecked();
        expect(wrapper.vm.rememberMe).toBe(true);
      }
    });
  });

  describe('登录功能', () => {
    it('should have handleLogin method', () => {
      const wrapper = mountLogin();
      expect(typeof wrapper.vm.handleLogin).toBe('function');
    });

    it('should have loading state', () => {
      const wrapper = mountLogin();
      expect(typeof wrapper.vm.loading).toBe('boolean');
    });

    it('should have loginForm reactive state', () => {
      const wrapper = mountLogin();
      expect(wrapper.vm.loginForm).toBeDefined();
      expect(typeof wrapper.vm.loginForm).toBe('object');
    });
  });

  describe('导航链接', () => {
    it('should have register link visible', () => {
      const wrapper = mountLogin();
      expect(wrapper.text()).toContain('立即注册');
    });

    it('should have forgot password link', () => {
      const wrapper = mountLogin();
      expect(wrapper.text()).toContain('忘记密码');
    });
  });

  describe('表单验证', () => {
    it('should have validation rules', () => {
      const wrapper = mountLogin();
      expect(wrapper.vm.rules).toBeDefined();
      expect(wrapper.vm.rules.username).toBeDefined();
      expect(wrapper.vm.rules.password).toBeDefined();
    });

    it('should require username', () => {
      const wrapper = mountLogin();
      const usernameRules = wrapper.vm.rules.username;
      expect(usernameRules.some((rule: any) => rule.required)).toBe(true);
    });

    it('should require password', () => {
      const wrapper = mountLogin();
      const passwordRules = wrapper.vm.rules.password;
      expect(passwordRules.some((rule: any) => rule.required)).toBe(true);
    });
  });

  describe('页面标题', () => {
    it('should have card title', () => {
      const wrapper = mountLogin();
      expect(wrapper.text()).toContain('欢迎回来');
    });

    it('should have card subtitle', () => {
      const wrapper = mountLogin();
      expect(wrapper.text()).toContain('登录账号');
    });
  });
});
