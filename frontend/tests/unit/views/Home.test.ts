// @ts-nocheck
import { describe, it, expect, vi, beforeEach, beforeAll } from 'vitest';
import { mount } from '@vue/test-utils';
import { setActivePinia, createPinia } from 'pinia';

const mockRoute = {
  params: {},
  fullPath: '/',
  query: {},
  path: '/',
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

const mockCarbonStats = vi.hoisted(() => ({
  mockGetStats: vi.fn().mockResolvedValue({ data: null }),
}));

const mockApiCategory = vi.hoisted(() => ({
  mockFetchAll: vi.fn().mockResolvedValue(undefined),
}));

vi.mock('@/api', () => ({
  default: {
    category: { fetchAll: mockApiCategory.mockFetchAll },
    carbon: { getStats: mockCarbonStats.mockGetStats },
    item: { getHotItems: vi.fn().mockResolvedValue({ code: 200, data: [] }) },
  },
}));

vi.mock('@/store/category', () => ({
  useCategoryStore: () => ({
    categories: [],
    flatCategories: [],
    categoryTree: [],
    loaded: true,
    error: null,
    fetchAll: mockApiCategory.mockFetchAll,
    getCategoryIcon: (name) => '',
  }),
}));

vi.mock('@/store/modules/user', () => ({
  useUserStore: () => ({
    user: null,
    isLoggedIn: false,
  }),
}));

let Home;
beforeAll(async () => {
  const mod = await import('@/views/Home.vue');
  Home = mod.default;
});

beforeEach(() => {
  setActivePinia(createPinia());
  vi.clearAllMocks();
  mockCarbonStats.mockGetStats.mockResolvedValue({ data: null });
  mockApiCategory.mockFetchAll.mockResolvedValue(undefined);
  document.body.innerHTML = '';
});

const mountHome = () => {
  return mount(Home, {
    global: {
      stubs: {
        'router-link': '<a><slot /></a>',
        teleport: '<div><slot /></div>',
        ItemSection: '<div class="item-section"><slot /></div>',
        Transition: false,
      },
    },
  });
};

describe('Home Component (TypeScript)', () => {
  it('should render home page', async () => {
    const wrapper = mountHome();
    await wrapper.vm.$nextTick();
    await new Promise(r => setTimeout(r, 50));
    expect(wrapper.text()).toContain('让闲置流动');
  });

  it('should render hero section', async () => {
    const wrapper = mountHome();
    await wrapper.vm.$nextTick();
    await new Promise(r => setTimeout(r, 50));
    expect(wrapper.text()).toContain('让校园更绿');
  });

  it('should render eco stats bar', async () => {
    const wrapper = mountHome();
    await wrapper.vm.$nextTick();
    await new Promise(r => setTimeout(r, 50));
    expect(wrapper.text()).toContain('本月校园交易已减少');
  });

  it('should render categories section', async () => {
    const wrapper = mountHome();
    await wrapper.vm.$nextTick();
    await new Promise(r => setTimeout(r, 50));
    const text = wrapper.text();
    expect(text.includes('全部') || text.includes('分类')).toBe(true);
  });

  it('should have loading state', async () => {
    const wrapper = mountHome();
    expect(typeof wrapper.vm.loading).toBe('boolean');
  });

  it('should have fetchRecommendedItems method', async () => {
    const wrapper = mountHome();
    expect(typeof wrapper.vm.fetchRecommendedItems).toBe('function');
  });

  it('should toggle like for items', async () => {
    const wrapper = mountHome();
    expect(typeof wrapper.vm.toggleLike).toBe('function');
    const id = 1;
    wrapper.vm.toggleLike(id);
    expect(wrapper.vm.likedItems.has(id)).toBe(true);
    wrapper.vm.toggleLike(id);
    expect(wrapper.vm.likedItems.has(id)).toBe(false);
  });

  it('should select category and navigate', async () => {
    const wrapper = mountHome();
    expect(typeof wrapper.vm.selectCategory).toBe('function');
    wrapper.vm.selectCategory('all');
    expect(wrapper.vm.activeCategory).toBe('all');
  });
});
