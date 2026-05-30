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

    it('应该渲染主内容区域', () => {
      const wrapper = mountUserCenter();
      expect(wrapper.find('.main-content').exists()).toBe(true);
    });

    it('应该渲染内容包装器', () => {
      const wrapper = mountUserCenter();
      expect(wrapper.find('.content-wrapper').exists()).toBe(true);
    });

    it('应该渲染路由视图容器', () => {
      const wrapper = mountUserCenter();
      expect(wrapper.find('.router-view-container').exists()).toBe(true);
    });
  });

  describe('组件状态', () => {
    it('应该有sidebarCollapsed状态', () => {
      const wrapper = mountUserCenter();
      expect(wrapper.vm.sidebarCollapsed).toBeDefined();
      expect(typeof wrapper.vm.sidebarCollapsed).toBe('boolean');
    });

    it('应该有userInfo计算属性', () => {
      const wrapper = mountUserCenter();
      expect(wrapper.vm.userInfo).toBeDefined();
    });

    it('应该有statsData计算属性', () => {
      const wrapper = mountUserCenter();
      expect(wrapper.vm.statsData).toBeDefined();
      expect(Array.isArray(wrapper.vm.statsData)).toBe(true);
    });

    it('应该有menuItems数据', () => {
      const wrapper = mountUserCenter();
      expect(wrapper.vm.menuItems).toBeDefined();
      expect(Array.isArray(wrapper.vm.menuItems)).toBe(true);
      expect(wrapper.vm.menuItems.length).toBeGreaterThan(0);
    });

    it('应该有quickActions数据', () => {
      const wrapper = mountUserCenter();
      expect(wrapper.vm.quickActions).toBeDefined();
      expect(Array.isArray(wrapper.vm.quickActions)).toBe(true);
      expect(wrapper.vm.quickActions.length).toBeGreaterThan(0);
    });
  });

  describe('方法', () => {
    it('应该有loadStats方法', () => {
      const wrapper = mountUserCenter();
      expect(typeof wrapper.vm.loadStats).toBe('function');
    });
  });

  describe('数据加载', () => {
    it('应该在挂载时加载统计数据', async () => {
      mountUserCenter();
      expect(mockGetStats).toHaveBeenCalled();
    });

    it('应该正确处理统计数据', async () => {
      const wrapper = mountUserCenter();
      await wrapper.vm.$nextTick();
      expect(wrapper.vm.stats.totalItems).toBe(10);
      expect(wrapper.vm.stats.soldItems).toBe(5);
      expect(wrapper.vm.stats.completedDeals).toBe(8);
      expect(wrapper.vm.stats.rating).toBe(95);
    });
  });

  describe('统计计算', () => {
    it('statsData应该包含发布数量', async () => {
      const wrapper = mountUserCenter();
      await wrapper.vm.$nextTick();
      const publishItem = wrapper.vm.statsData.find((item: any) => item.label === '发布');
      expect(publishItem).toBeDefined();
      expect(publishItem?.value).toBe(10);
    });

    it('statsData应该包含已售数量', async () => {
      const wrapper = mountUserCenter();
      await wrapper.vm.$nextTick();
      const soldItem = wrapper.vm.statsData.find((item: any) => item.label === '已售');
      expect(soldItem).toBeDefined();
      expect(soldItem?.value).toBe(5);
    });

    it('statsData应该包含成交数量', async () => {
      const wrapper = mountUserCenter();
      await wrapper.vm.$nextTick();
      const dealItem = wrapper.vm.statsData.find((item: any) => item.label === '成交');
      expect(dealItem).toBeDefined();
      expect(dealItem?.value).toBe(8);
    });

    it('statsData应该包含信用分', async () => {
      const wrapper = mountUserCenter();
      await wrapper.vm.$nextTick();
      const ratingItem = wrapper.vm.statsData.find((item: any) => item.label === '信用分');
      expect(ratingItem).toBeDefined();
      expect(ratingItem?.value).toBe(95);
      expect(ratingItem?.accent).toBe(true);
    });
  });
});
