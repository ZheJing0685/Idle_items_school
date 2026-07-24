// @ts-nocheck
import { describe, it, expect, vi, beforeEach, beforeAll } from 'vitest';
import { mount } from '@vue/test-utils';
import { setActivePinia, createPinia } from 'pinia';
import { ElMessage } from 'element-plus';

const mockRoute = {
  params: {},
  fullPath: '/my-feedbacks',
  query: {},
  path: '/my-feedbacks',
  matched: [],
  meta: {},
};
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
  RouteRecordRaw: null,
}));

vi.mock('element-plus', () => ({
  ElMessage: { success: vi.fn(), error: vi.fn(), warning: vi.fn() },
}));

const mockGetMyFeedbacks = vi.fn();

vi.mock('@/api', () => ({
  default: {
    category: { getMyFeedbacks: mockGetMyFeedbacks },
  },
}));

let MyFeedbacks;
beforeAll(async () => {
  const mod = await import('@/views/user/MyFeedbacks.vue');
  MyFeedbacks = mod.default;
});

beforeEach(() => {
  setActivePinia(createPinia());
  vi.clearAllMocks();
  document.body.innerHTML = '';
  mockGetMyFeedbacks.mockResolvedValue({
    code: 200,
    data: { content: [], totalElements: 0 },
  });
});

const mountMyFeedbacks = () => {
  return mount(MyFeedbacks, {
    global: {
      stubs: {
        'router-link': '<a><slot /></a>',
        'el-button': '<button><slot /></button>',
        'el-table': '<div class="el-table"><slot /></div>',
        'el-table-column': '<div class="el-table-column"><slot /></div>',
        'el-tag': '<span class="el-tag"><slot /></span>',
        'el-pagination': {
          template: '<div class="el-pagination"><slot /></div>',
          props: ['currentPage', 'pageSize', 'total', 'page-sizes', 'layout'],
        },
        'v-loading': {
          template: '<div class="v-loading"><slot /></div>',
          props: ['value'],
        },
      },
    },
  });
};

describe('MyFeedbacks Component', () => {
  describe('工具函数 - feedbackTypeText', () => {
    it('应该返回"分类无效"对应INVALID类型', () => {
      const wrapper = mountMyFeedbacks();
      expect(wrapper.vm.feedbackTypeText('INVALID')).toBe('分类无效');
    });

    it('应该返回"缺少分类"对应MISSING类型', () => {
      const wrapper = mountMyFeedbacks();
      expect(wrapper.vm.feedbackTypeText('MISSING')).toBe('缺少分类');
    });

    it('应该返回"其他"对应OTHER类型', () => {
      const wrapper = mountMyFeedbacks();
      expect(wrapper.vm.feedbackTypeText('OTHER')).toBe('其他');
    });

    it('未知类型应返回原始值', () => {
      const wrapper = mountMyFeedbacks();
      expect(wrapper.vm.feedbackTypeText('UNKNOWN')).toBe('UNKNOWN');
    });
  });

  describe('工具函数 - feedbackTypeTag', () => {
    it('INVALID应返回danger标签类型', () => {
      const wrapper = mountMyFeedbacks();
      expect(wrapper.vm.feedbackTypeTag('INVALID')).toBe('danger');
    });

    it('MISSING应返回warning标签类型', () => {
      const wrapper = mountMyFeedbacks();
      expect(wrapper.vm.feedbackTypeTag('MISSING')).toBe('warning');
    });

    it('OTHER应返回info标签类型', () => {
      const wrapper = mountMyFeedbacks();
      expect(wrapper.vm.feedbackTypeTag('OTHER')).toBe('info');
    });

    it('未知类型应返回info标签类型', () => {
      const wrapper = mountMyFeedbacks();
      expect(wrapper.vm.feedbackTypeTag('UNKNOWN')).toBe('info');
    });
  });

  describe('工具函数 - statusText', () => {
    it('PENDING应返回"待处理"', () => {
      const wrapper = mountMyFeedbacks();
      expect(wrapper.vm.statusText('PENDING')).toBe('待处理');
    });

    it('ACCEPTED应返回"已采纳"', () => {
      const wrapper = mountMyFeedbacks();
      expect(wrapper.vm.statusText('ACCEPTED')).toBe('已采纳');
    });

    it('REJECTED应返回"已拒绝"', () => {
      const wrapper = mountMyFeedbacks();
      expect(wrapper.vm.statusText('REJECTED')).toBe('已拒绝');
    });

    it('未知状态应返回原始值', () => {
      const wrapper = mountMyFeedbacks();
      expect(wrapper.vm.statusText('UNKNOWN')).toBe('UNKNOWN');
    });
  });

  describe('工具函数 - statusTag', () => {
    it('PENDING应返回warning标签类型', () => {
      const wrapper = mountMyFeedbacks();
      expect(wrapper.vm.statusTag('PENDING')).toBe('warning');
    });

    it('ACCEPTED应返回success标签类型', () => {
      const wrapper = mountMyFeedbacks();
      expect(wrapper.vm.statusTag('ACCEPTED')).toBe('success');
    });

    it('REJECTED应返回danger标签类型', () => {
      const wrapper = mountMyFeedbacks();
      expect(wrapper.vm.statusTag('REJECTED')).toBe('danger');
    });

    it('未知状态应返回info标签类型', () => {
      const wrapper = mountMyFeedbacks();
      expect(wrapper.vm.statusTag('UNKNOWN')).toBe('info');
    });
  });

  describe('工具函数 - formatTime', () => {
    it('有效时间应返回格式化字符串', () => {
      const wrapper = mountMyFeedbacks();
      const result = wrapper.vm.formatTime('2024-01-15T10:30:00.000Z');
      expect(result).toBeTruthy();
      expect(typeof result).toBe('string');
    });

    it('空时间应返回"-"', () => {
      const wrapper = mountMyFeedbacks();
      expect(wrapper.vm.formatTime('')).toBe('-');
      expect(wrapper.vm.formatTime(null)).toBe('-');
      expect(wrapper.vm.formatTime(undefined)).toBe('-');
    });
  });

  describe('方法 - loadFeedbacks', () => {
    it('loadFeedbacks应该是函数', () => {
      const wrapper = mountMyFeedbacks();
      expect(typeof wrapper.vm.loadFeedbacks).toBe('function');
    });

    it('成功时应设置feedbacks和total', async () => {
      const responseData = {
        content: [
          { id: 1, feedbackType: 'INVALID', status: 'PENDING', description: '测试反馈', createdAt: '2024-01-15T10:30:00.000Z' },
        ],
        totalElements: 1,
      };
      // Use mockImplementation to override the default
      mockGetMyFeedbacks.mockImplementation(() => Promise.resolve({ code: 200, data: responseData }));

      const wrapper = mountMyFeedbacks();
      await new Promise(r => setTimeout(r, 50));
      await wrapper.vm.loadFeedbacks();
      expect(mockGetMyFeedbacks).toHaveBeenCalled();
      expect(wrapper.vm.feedbacks.length).toBe(1);
      expect(wrapper.vm.total).toBe(1);
    });

    it('失败时应清空feedbacks', async () => {
      mockGetMyFeedbacks.mockImplementation(() => Promise.resolve({ code: 500, data: null }));

      const wrapper = mountMyFeedbacks();
      await new Promise(r => setTimeout(r, 50));
      await wrapper.vm.loadFeedbacks();
      expect(wrapper.vm.feedbacks.length).toBe(0);
    });

    it('响应data为数组时应正确处理', async () => {
      mockGetMyFeedbacks.mockImplementation(() => Promise.resolve({
        code: 200,
        data: [{ id: 1 }],
      }));

      const wrapper = mountMyFeedbacks();
      await new Promise(r => setTimeout(r, 50));
      await wrapper.vm.loadFeedbacks();
      expect(wrapper.vm.feedbacks.length).toBe(1);
    });

    it('加载过程中loading应为true', async () => {
      let resolveFn;
      const promise = new Promise((resolve) => { resolveFn = resolve; });
      mockGetMyFeedbacks.mockImplementation(() => promise);

      const wrapper = mountMyFeedbacks();
      await new Promise(r => setTimeout(r, 50));
      const loadPromise = wrapper.vm.loadFeedbacks();
      expect(wrapper.vm.loading).toBe(true);
      resolveFn({ code: 200, data: { content: [], totalElements: 0 } });
      await loadPromise;
      expect(wrapper.vm.loading).toBe(false);
    });
  });

  describe('组件状态', () => {
    it('应该有loading状态', () => {
      const wrapper = mountMyFeedbacks();
      expect(typeof wrapper.vm.loading).toBe('boolean');
    });

    it('应该有feedbacks数组', () => {
      const wrapper = mountMyFeedbacks();
      expect(Array.isArray(wrapper.vm.feedbacks)).toBe(true);
    });

    it('应该有currentPage状态', () => {
      const wrapper = mountMyFeedbacks();
      expect(typeof wrapper.vm.currentPage).toBe('number');
    });

    it('应该有pageSize状态', () => {
      const wrapper = mountMyFeedbacks();
      expect(typeof wrapper.vm.pageSize).toBe('number');
    });

    it('应该有total状态', () => {
      const wrapper = mountMyFeedbacks();
      expect(typeof wrapper.vm.total).toBe('number');
    });
  });

  describe('路由功能', () => {
    it('应有提交新反馈按钮元素', () => {
      const wrapper = mountMyFeedbacks();
      const btn = wrapper.findComponent({ name: 'ElButton' });
      expect(btn.exists()).toBe(true);
    });
  });
});
