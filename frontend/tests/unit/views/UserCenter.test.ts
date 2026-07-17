// @ts-nocheck
import { describe, it, expect, vi, beforeEach, beforeAll } from 'vitest';
import { mount } from '@vue/test-utils';
import { setActivePinia, createPinia } from 'pinia';

// Mock route
const mockRoute = {
  query: {},
  path: '/user',
};

vi.mock('vue-router', () => ({
  useRoute: () => mockRoute,
}));

// Mock store
vi.mock('@/store', () => ({
  userStore: () => ({
    user: {
      id: 1,
      nickname: '测试用户',
      username: 'testuser',
      avatar: '',
    },
  }),
}));

// Mock API
const mockGetStats = vi.fn();
vi.mock('@/api', () => ({
  default: {
    user: {
      getStats: mockGetStats,
    },
  },
}));

// Mock navigation config
vi.mock('@/config/navigation', () => ({
  userMenuConfig: {
    items: [
      { name: '我的发布', path: '/user/items', icon: 'package' },
      { name: '我的订单', path: '/user/orders', icon: 'shopping-bag' },
      { name: '我的收藏', path: '/user/favorites', icon: 'heart' },
    ],
  },
}));

// Mock child components
vi.mock('@/components/user/Sidebar.vue', () => ({
  default: {
    template: '<div class="sidebar"><slot /></div>',
    props: ['collapsed', 'menuItems'],
  },
}));

vi.mock('@/components/user/UserInfoCard.vue', () => ({
  default: {
    template: '<div class="user-info-card"><slot /></div>',
    props: ['user'],
  },
}));

vi.mock('@/components/user/StatsCard.vue', () => ({
  default: {
    template: '<div class="stats-card"><slot /></div>',
    props: ['stats'],
  },
}));

vi.mock('@/components/user/QuickActions.vue', () => ({
  default: {
    template: '<div class="quick-actions"><slot /></div>',
    props: ['actions'],
  },
}));

let UserCenter: any;
beforeAll(async () => {
  const mod = await import('@/views/UserCenter.vue');
  UserCenter = mod.default;
});

beforeEach(() => {
  setActivePinia(createPinia());
  vi.clearAllMocks();
  mockGetStats.mockResolvedValue({
    code: 200,
    data: {
      totalItems: 10,
      soldItems: 5,
      completedDeals: 8,
      rating: 95,
    },
  });
});

const mountUserCenter = () => {
  return mount(UserCenter, {
    global: {
      stubs: {
        'router-view': { template: '<div class="router-view"><slot /></div>' },
        'router-link': { template: '<a><slot /></a>' },
      },
    },
  });
};

describe('UserCenter Component', () => {
  describe('组件渲染', () => {
    it('应该渲染用户中心页面', () => {
      const wrapper = mountUserCenter();
      expect(wrapper.find('.user-center-page').exists()).toBe(true);
    });

    it('应该渲染用户信息区域', () => {
      const wrapper = mountUserCenter();
      expect(wrapper.find('.profile-header').exists()).toBe(true);
    });

    it('应该渲染统计区域', () => {
      const wrapper = mountUserCenter();
      expect(wrapper.find('.profile-stats').exists()).toBe(true);
    });

    it('应该渲染标签页导航', () => {
      const wrapper = mountUserCenter();
      expect(wrapper.find('.profile-tabs').exists()).toBe(true);
    });
  });

  describe('组件状态', () => {
    it('应该有userInfo计算属性', () => {
      const wrapper = mountUserCenter();
      expect(wrapper.vm.userInfo).toBeDefined();
    });
  });

  describe('数据加载', () => {
    it('应该在挂载时加载统计数据', async () => {
      mountUserCenter();
      expect(mockGetStats).toHaveBeenCalled();
    });
  });
});
