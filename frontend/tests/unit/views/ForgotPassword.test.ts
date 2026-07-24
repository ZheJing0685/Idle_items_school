// @ts-nocheck
import { describe, it, expect, vi, beforeEach, beforeAll } from 'vitest';
import { mount } from '@vue/test-utils';
import { setActivePinia, createPinia } from 'pinia';

const mockRoute = { params: {}, fullPath: '/forgot-password', query: {}, path: '/forgot-password', matched: [] };
const mockPush = vi.fn();

vi.mock('vue-router', () => ({
  useRoute: () => mockRoute,
  useRouter: () => ({ push: mockPush, replace: vi.fn() }),
  createRouter: vi.fn(() => ({
    beforeEach: vi.fn(),
    afterEach: vi.fn(),
    isReady: vi.fn(() => Promise.resolve()),
  })),
  createWebHistory: vi.fn(),
}));

const mockElMessage = {
  success: vi.fn(),
  error: vi.fn(),
  warning: vi.fn(),
};

vi.mock('element-plus', () => ({
  ElMessage: mockElMessage,
}));

const mockForgotPassword = vi.fn();
const mockVerifyCode = vi.fn();
const mockResetPassword = vi.fn();

vi.mock('@/api', () => ({
  default: {
    auth: {
      forgotPassword: mockForgotPassword,
      verifyCode: mockVerifyCode,
      resetPassword: mockResetPassword,
    },
  },
}));

let ForgotPassword;
beforeAll(async () => {
  const mod = await import('@/views/ForgotPassword.vue');
  ForgotPassword = mod.default;
});

beforeEach(() => {
  setActivePinia(createPinia());
  vi.clearAllMocks();
  document.body.innerHTML = '';
  mockRoute.params = {};
  mockElMessage.success.mockClear();
  mockElMessage.error.mockClear();
  mockElMessage.warning.mockClear();
});

const mountForgotPassword = () => {
  return mount(ForgotPassword, {
    global: {
      stubs: {
        'router-link': '<a><slot /></a>',
      },
    },
  });
};

describe('ForgotPassword Component', () => {
  describe('步骤1 - 输入邮箱', () => {
    it('应该渲染第一步：输入邮箱表单', () => {
      const wrapper = mountForgotPassword();
      expect(wrapper.find('.step-content').exists()).toBe(true);
      expect(wrapper.text()).toContain('忘记密码');
    });

    it('应该有email表单字段', () => {
      const wrapper = mountForgotPassword();
      const inputs = wrapper.findAll('input');
      expect(inputs.length).toBeGreaterThan(0);
    });

    it('空邮箱应显示警告并保持第一步', async () => {
      const wrapper = mountForgotPassword();
      await wrapper.vm.sendCode();
      expect(mockElMessage.warning).toHaveBeenCalled();
      expect(wrapper.vm.step).toBe(1);
    });

    it('发送验证码成功应进入第二步', async () => {
      mockForgotPassword.mockResolvedValue({});
      const wrapper = mountForgotPassword();
      wrapper.vm.form.email = 'test@example.com';
      await wrapper.vm.sendCode();
      expect(mockElMessage.success).toHaveBeenCalled();
      expect(wrapper.vm.step).toBe(2);
    });

    it('发送验证码失败应显示错误并保持第一步', async () => {
      mockForgotPassword.mockRejectedValue({ response: { data: { message: '邮箱不存在' } } });
      const wrapper = mountForgotPassword();
      wrapper.vm.form.email = 'test@example.com';
      await wrapper.vm.sendCode();
      expect(mockElMessage.error).toHaveBeenCalled();
      expect(wrapper.vm.step).toBe(1);
    });
  });

  describe('步骤2 - 输入验证码', () => {
    it('验证码长度不为6应显示警告', async () => {
      const wrapper = mountForgotPassword();
      wrapper.vm.step = 2;
      await wrapper.vm.$nextTick();
      wrapper.vm.form.code = '123';
      wrapper.vm.form.email = 'test@example.com';
      await wrapper.vm.verifyCode();
      expect(mockElMessage.warning).toHaveBeenCalled();
      expect(wrapper.vm.step).toBe(2);
    });

    it('验证成功应进入第三步', async () => {
      mockVerifyCode.mockResolvedValue({});
      const wrapper = mountForgotPassword();
      wrapper.vm.step = 2;
      wrapper.vm.form.code = '123456';
      wrapper.vm.form.email = 'test@example.com';
      await wrapper.vm.verifyCode();
      expect(mockElMessage.success).toHaveBeenCalled();
      expect(wrapper.vm.step).toBe(3);
    });

    it('验证失败应显示错误', async () => {
      mockVerifyCode.mockRejectedValue({ response: { data: { message: '验证码错误' } } });
      const wrapper = mountForgotPassword();
      wrapper.vm.step = 2;
      wrapper.vm.form.code = '123456';
      wrapper.vm.form.email = 'test@example.com';
      await wrapper.vm.verifyCode();
      expect(mockElMessage.error).toHaveBeenCalled();
      expect(wrapper.vm.step).toBe(2);
    });
  });

  describe('步骤3 - 设置新密码', () => {
    it('应该正确渲染第三步：设置新密码', async () => {
      const wrapper = mountForgotPassword();
      wrapper.vm.step = 3;
      await wrapper.vm.$nextTick();
      expect(wrapper.vm.step).toBe(3);
    });

    it('空密码应显示警告', async () => {
      const wrapper = mountForgotPassword();
      wrapper.vm.step = 3;
      await wrapper.vm.resetPassword();
      expect(mockElMessage.warning).toHaveBeenCalled();
      expect(wrapper.vm.step).toBe(3);
    });

    it('密码不一致应显示警告', async () => {
      const wrapper = mountForgotPassword();
      wrapper.vm.step = 3;
      wrapper.vm.form.newPassword = 'password1';
      wrapper.vm.form.confirmPassword = 'password2';
      await wrapper.vm.resetPassword();
      expect(mockElMessage.warning).toHaveBeenCalled();
      expect(wrapper.vm.step).toBe(3);
    });

    it('重置成功应进入第四步', async () => {
      mockResetPassword.mockResolvedValue({});
      const wrapper = mountForgotPassword();
      wrapper.vm.step = 3;
      wrapper.vm.form.newPassword = 'newpass123';
      wrapper.vm.form.confirmPassword = 'newpass123';
      wrapper.vm.form.code = '123456';
      wrapper.vm.form.email = 'test@example.com';
      await wrapper.vm.resetPassword();
      expect(mockElMessage.success).toHaveBeenCalled();
      expect(wrapper.vm.step).toBe(4);
    });

    it('重置失败应显示错误', async () => {
      mockResetPassword.mockRejectedValue({ response: { data: { message: '重置失败' } } });
      const wrapper = mountForgotPassword();
      wrapper.vm.step = 3;
      wrapper.vm.form.newPassword = 'newpass123';
      wrapper.vm.form.confirmPassword = 'newpass123';
      wrapper.vm.form.code = '123456';
      wrapper.vm.form.email = 'test@example.com';
      await wrapper.vm.resetPassword();
      expect(mockElMessage.error).toHaveBeenCalled();
      expect(wrapper.vm.step).toBe(3);
    });
  });

  describe('步骤4 - 成功', () => {
    it('应该渲染第四步：成功页面', async () => {
      const wrapper = mountForgotPassword();
      wrapper.vm.step = 4;
      await wrapper.vm.$nextTick();
      expect(wrapper.text()).toContain('密码重置成功');
    });

    it('成功页面应该有去登录按钮', async () => {
      const wrapper = mountForgotPassword();
      wrapper.vm.step = 4;
      await wrapper.vm.$nextTick();
      expect(wrapper.text()).toContain('去登录');
    });

    it('点击去登录应跳转到登录页', () => {
      const wrapper = mountForgotPassword();
      wrapper.vm.step = 4;
      wrapper.vm.goToLogin();
      expect(mockPush).toHaveBeenCalledWith('/login');
    });
  });

  describe('返回登录链接', () => {
    it('应该始终显示返回登录链接', () => {
      const wrapper = mountForgotPassword();
      expect(wrapper.text()).toContain('返回登录');
    });
  });

  describe('加载状态', () => {
    it('发送验证码时loading应为true', async () => {
      mockForgotPassword.mockResolvedValue({});
      const wrapper = mountForgotPassword();
      wrapper.vm.form.email = 'test@example.com';
      await wrapper.vm.sendCode();
      expect(wrapper.vm.loading).toBe(false);
    });
  });
});
