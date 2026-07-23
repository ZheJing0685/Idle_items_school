// @ts-nocheck
import { describe, it, expect, vi, beforeEach, beforeAll } from 'vitest';
import { mount } from '@vue/test-utils';
import { setActivePinia, createPinia } from 'pinia';

const mockRoute = { query: {}, path: '/user', matched: [{ path: '/user', meta: { title: '用户中心' } }] };

vi.mock('vue-router', () => ({
  useRoute: () => mockRoute,
  useRouter: () => ({ push: vi.fn(), replace: vi.fn() }),
  createRouter: vi.fn(() => ({
    beforeEach: vi.fn(),
    afterEach: vi.fn(),
    isReady: vi.fn(() => Promise.resolve()),
  })),
  createWebHistory: vi.fn(),
}));

vi.mock('@/store', () => ({
  userStore: () => ({
    user: {
      id: 1,
      nickname: '测试用户',
      username: 'testuser',
      avatar: '',
      schoolName: '测试大学',
      department: '计算机学院',
      grade: '2022级',
      bio: '热爱环保的码农',
    },
  }),
}));

const mockGetStats = vi.fn().mockResolvedValue({
  code: 200,
  data: { totalItems: 10, soldItems: 5, favorites: 3, rating: 95 },
});

vi.mock('@/api', () => ({
  default: {
    user: {
      getStats: mockGetStats,
    },
  },
}));

let UserCenter;
beforeAll(async () => {
  const mod = await import('@/views/UserCenter.vue');
  UserCenter = mod.default;
});

beforeEach(() => {
  setActivePinia(createPinia());
  vi.clearAllMocks();
  mockGetStats.mockResolvedValue({
    code: 200,
    data: { totalItems: 10, soldItems: 5, favorites: 3, rating: 95 },
  });
});

describe('UserCenter Component', () => {
  it('应该渲染用户中心页面', async () => {
    const wrapper = mount(UserCenter, {
      global: {
        stubs: {
          'router-link': { template: '<a><slot /></a>' },
          'router-view': { template: '<div class="router-view"><slot /></div>' },
          Transition: false,
        },
      },
    });
    await new Promise(r => setTimeout(r, 100));
    expect(wrapper.find('.user-center-page').exists()).toBe(true);
  });

  it('应该渲染用户信息区域', async () => {
    const wrapper = mount(UserCenter, {
      global: {
        stubs: {
          'router-link': { template: '<a><slot /></a>' },
          'router-view': { template: '<div class="router-view"><slot /></div>' },
          Transition: false,
        },
      },
    });
    await new Promise(r => setTimeout(r, 100));
    expect(wrapper.find('.profile-header').exists()).toBe(true);
  });

  it('应该渲染统计区域', async () => {
    const wrapper = mount(UserCenter, {
      global: {
        stubs: {
          'router-link': { template: '<a><slot /></a>' },
          'router-view': { template: '<div class="router-view"><slot /></div>' },
          Transition: false,
        },
      },
    });
    await new Promise(r => setTimeout(r, 100));
    expect(wrapper.find('.profile-stats').exists()).toBe(true);
  });

  it('应该渲染标签页导航', async () => {
    const wrapper = mount(UserCenter, {
      global: {
        stubs: {
          'router-link': { template: '<a><slot /></a>' },
          'router-view': { template: '<div class="router-view"><slot /></div>' },
          Transition: false,
        },
      },
    });
    await new Promise(r => setTimeout(r, 100));
    expect(wrapper.find('.profile-tabs').exists()).toBe(true);
  });

  it('应该有userInfo计算属性', async () => {
    const wrapper = mount(UserCenter, {
      global: {
        stubs: {
          'router-link': { template: '<a><slot /></a>' },
          'router-view': { template: '<div class="router-view"><slot /></div>' },
          Transition: false,
        },
      },
    });
    await new Promise(r => setTimeout(r, 50));
    expect(wrapper.vm.userInfo).toBeDefined();
  });

  it('应该在挂载时加载统计数据', async () => {
    mount(UserCenter, {
      global: {
        stubs: {
          'router-link': { template: '<a><slot /></a>' },
          'router-view': { template: '<div class="router-view"><slot /></div>' },
          Transition: false,
        },
      },
    });
    await new Promise(r => setTimeout(r, 100));
    expect(mockGetStats).toHaveBeenCalled();
  });
});
