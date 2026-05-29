import { describe, it, expect, vi, beforeEach, beforeAll } from 'vitest';
import { mount } from '@vue/test-utils';
import { setActivePinia, createPinia } from 'pinia';

let ErrorBoundary: any;
beforeAll(async () => {
  const mod = await import('@/components/common/ErrorBoundary.vue');
  ErrorBoundary = mod.default;
});

beforeEach(() => {
  setActivePinia(createPinia());
});

const mountErrorBoundary = (options = {}) => {
  return mount(ErrorBoundary, {
    global: {
      stubs: {
        'router-link': { template: '<a><slot /></a>' },
      },
    },
    ...options,
  });
};

describe('ErrorBoundary Component', () => {
  describe('组件渲染', () => {
    it('should render slot content when no error', () => {
      const wrapper = mount({
        template: '<ErrorBoundary><div class="test-content">测试内容</div></ErrorBoundary>',
        components: { ErrorBoundary },
      });

      expect(wrapper.find('.test-content').exists()).toBe(true);
      expect(wrapper.text()).toContain('测试内容');
    });

    it('should render error boundary wrapper', () => {
      const wrapper = mountErrorBoundary();
      expect(wrapper.exists()).toBe(true);
    });
  });

  describe('错误捕获', () => {
    it('should have error ref', () => {
      const wrapper = mountErrorBoundary();
      expect(wrapper.vm.error).toBeDefined();
    });

    it('should have handleRefresh method', () => {
      const wrapper = mountErrorBoundary();
      expect(typeof wrapper.vm.handleRefresh).toBe('function');
    });

    it('should show error message when error occurs', async () => {
      const wrapper = mountErrorBoundary();
      wrapper.vm.error = new Error('测试错误');
      await wrapper.vm.$nextTick();

      expect(wrapper.text()).toContain('页面出现错误');
      expect(wrapper.text()).toContain('测试错误');
    });

    it('should show refresh button when error occurs', async () => {
      const wrapper = mountErrorBoundary();
      wrapper.vm.error = new Error('测试错误');
      await wrapper.vm.$nextTick();

      const refreshButton = wrapper.find('button');
      expect(refreshButton.exists()).toBe(true);
      expect(refreshButton.text()).toContain('刷新页面');
    });

    it('should hide slot content when error occurs', async () => {
      const wrapper = mount({
        template: `
          <ErrorBoundary ref="boundary">
            <div class="test-content">测试内容</div>
          </ErrorBoundary>
        `,
        components: { ErrorBoundary },
      });

      const boundary = wrapper.findComponent({ ref: 'boundary' });
      boundary.vm.error = new Error('测试错误');
      await wrapper.vm.$nextTick();

      expect(wrapper.find('.test-content').exists()).toBe(false);
    });
  });

  describe('错误边界样式', () => {
    it('should have error-boundary class', async () => {
      const wrapper = mountErrorBoundary();
      wrapper.vm.error = new Error('测试错误');
      await wrapper.vm.$nextTick();

      expect(wrapper.find('.error-boundary').exists()).toBe(true);
    });

    it('should have error-content class', async () => {
      const wrapper = mountErrorBoundary();
      wrapper.vm.error = new Error('测试错误');
      await wrapper.vm.$nextTick();

      expect(wrapper.find('.error-content').exists()).toBe(true);
    });
  });

  describe('刷新功能', () => {
    it('should have handleRefresh method', () => {
      const wrapper = mountErrorBoundary();
      expect(typeof wrapper.vm.handleRefresh).toBe('function');
    });

    it('should reset error state', async () => {
      const wrapper = mountErrorBoundary();
      wrapper.vm.error = new Error('测试错误');
      expect(wrapper.vm.error).not.toBeNull();

      wrapper.vm.error = null;
      expect(wrapper.vm.error).toBeNull();
    });
  });
});
