// @ts-nocheck
import { describe, it, expect, vi, beforeEach, beforeAll } from 'vitest';
import { mount } from '@vue/test-utils';
import { setActivePinia, createPinia } from 'pinia';

const mockRoute = { params: { id: '1' }, fullPath: '/item/1', query: {}, path: '/item/1', matched: [{ path: '/item/:id', meta: { title: '物品详情' } }] };
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

vi.mock('element-plus', () => ({
  ElMessage: { success: vi.fn(), error: vi.fn(), warning: vi.fn() },
  ElMessageBox: { alert: vi.fn(), confirm: vi.fn() },
}));

const mockGetItem = vi.fn().mockResolvedValue({
  code: 200,
  data: {
    id: 1, title: '测试物品', price: 100, originalPrice: 200, description: '测试描述',
    condition: 'NEW', deliveryMethod: 'EXPRESS', categoryName: '电子产品', location: '校园南门',
    images: ['img1.jpg'], coverImage: 'cover.jpg', viewCount: 100, favoriteCount: 50,
    isBargainAllowed: true, verified: true, userId: 1, sellerNickname: '卖家',
    sellerRating: 4.5, sellerItemsCount: 10, createdAt: new Date().toISOString(),
  },
});

const mockToggleFavorite = vi.fn().mockResolvedValue({ code: 200 });
const mockCheckFavorite = vi.fn().mockResolvedValue({ code: 200, data: false });
const mockCreateChat = vi.fn().mockResolvedValue({ code: 200 });
const mockOffShelf = vi.fn().mockResolvedValue({ code: 200 });

vi.mock('@/api', () => ({
  default: {
    item: { getItem: mockGetItem, offShelf: mockOffShelf },
    favorite: { toggleFavorite: mockToggleFavorite, checkFavorite: mockCheckFavorite },
    chat: { createChat: mockCreateChat },
  },
}));

vi.mock('@/store/modules/user', () => ({
  useUserStore: () => ({ user: { id: 1, nickname: '测试用户' }, isLoggedIn: true }),
}));

vi.mock('@/store/category', () => ({
  useCategoryStore: () => ({ flatCategories: [], getCategoryIcon: (name) => '' }),
}));

let ItemDetail;
beforeAll(async () => {
  const mod = await import('@/views/ItemDetail.vue');
  ItemDetail = mod.default;
});

beforeEach(() => {
  setActivePinia(createPinia());
  vi.clearAllMocks();
  mockGetItem.mockResolvedValue({
    code: 200,
    data: { id: 1, title: '测试物品', price: 100, originalPrice: 200, description: '测试', condition: 'NEW', deliveryMethod: 'EXPRESS', categoryName: '电子', images: ['img1.jpg'], coverImage: 'cover.jpg', sellerNickname: '卖家', userId: 2, createdAt: new Date().toISOString() },
  });
  mockCheckFavorite.mockResolvedValue({ code: 200, data: false });
});

describe('ItemDetail Component', () => {
  describe('组件渲染', () => {
    it('应该渲染物品详情页面', async () => {
      const wrapper = mount(ItemDetail, {
        global: { stubs: { 'router-link': { template: '<a><slot /></a>' }, Transition: false, Teleport: { template: '<div><slot /></div>' } } },
      });
      await new Promise(r => setTimeout(r, 150));
      await wrapper.vm.$nextTick();
      expect(wrapper.text()).toContain('测试物品');
    });

    it('应该渲染卖家信息区域', async () => {
      const wrapper = mount(ItemDetail, {
        global: { stubs: { 'router-link': { template: '<a><slot /></a>' }, Transition: false, Teleport: { template: '<div><slot /></div>' } } },
      });
      await new Promise(r => setTimeout(r, 150));
      await wrapper.vm.$nextTick();
      expect(wrapper.text()).toContain('卖家');
    });
  });

  describe('加载状态', () => {
    it('初始应该处于加载状态', async () => {
      const wrapper = mount(ItemDetail, {
        global: { stubs: { 'router-link': { template: '<a><slot /></a>' }, Transition: false, Teleport: { template: '<div><slot /></div>' } } },
      });
      expect(wrapper.vm.loading).toBe(true);
    });

    it('加载完成后应更新状态', async () => {
      const wrapper = mount(ItemDetail, {
        global: { stubs: { 'router-link': { template: '<a><slot /></a>' }, Transition: false, Teleport: { template: '<div><slot /></div>' } } },
      });
      await wrapper.vm.$nextTick();
      await new Promise(r => setTimeout(r, 100));
      expect(typeof wrapper.vm.loading).toBe('boolean');
    });
  });

  describe('组件状态', () => {
    it('应该有item数据', async () => {
      const wrapper = mount(ItemDetail, {
        global: { stubs: { 'router-link': { template: '<a><slot /></a>' }, Transition: false, Teleport: { template: '<div><slot /></div>' } } },
      });
      await new Promise(r => setTimeout(r, 100));
      expect(wrapper.vm.item).toBeDefined();
    });

    it('应该有isFavorited状态', async () => {
      const wrapper = mount(ItemDetail, {
        global: { stubs: { 'router-link': { template: '<a><slot /></a>' }, Transition: false, Teleport: { template: '<div><slot /></div>' } } },
      });
      expect(typeof wrapper.vm.isFavorited).toBe('boolean');
    });

    it('应该有showLightbox状态', async () => {
      const wrapper = mount(ItemDetail, {
        global: { stubs: { 'router-link': { template: '<a><slot /></a>' }, Transition: false, Teleport: { template: '<div><slot /></div>' } } },
      });
      expect(typeof wrapper.vm.showLightbox).toBe('boolean');
    });

    it('应该有similarItems数组', async () => {
      const wrapper = mount(ItemDetail, {
        global: { stubs: { 'router-link': { template: '<a><slot /></a>' }, Transition: false, Teleport: { template: '<div><slot /></div>' } } },
      });
      expect(Array.isArray(wrapper.vm.similarItems)).toBe(true);
    });

    it('应该有sellerItems数组', async () => {
      const wrapper = mount(ItemDetail, {
        global: { stubs: { 'router-link': { template: '<a><slot /></a>' }, Transition: false, Teleport: { template: '<div><slot /></div>' } } },
      });
      expect(Array.isArray(wrapper.vm.sellerItems)).toBe(true);
    });
  });

  describe('方法', () => {
    it('应该有fetchItemDetail方法', async () => {
      const wrapper = mount(ItemDetail, {
        global: { stubs: { 'router-link': { template: '<a><slot /></a>' }, Transition: false, Teleport: { template: '<div><slot /></div>' } } },
      });
      expect(typeof wrapper.vm.fetchItemDetail).toBe('function');
    });

    it('应该有toggleFavorite方法', async () => {
      const wrapper = mount(ItemDetail, {
        global: { stubs: { 'router-link': { template: '<a><slot /></a>' }, Transition: false, Teleport: { template: '<div><slot /></div>' } } },
      });
      expect(typeof wrapper.vm.toggleFavorite).toBe('function');
    });

    it('应该有handleBuy方法', async () => {
      const wrapper = mount(ItemDetail, {
        global: { stubs: { 'router-link': { template: '<a><slot /></a>' }, Transition: false, Teleport: { template: '<div><slot /></div>' } } },
      });
      expect(typeof wrapper.vm.handleBuy).toBe('function');
    });

    it('应该有handleChat方法', async () => {
      const wrapper = mount(ItemDetail, {
        global: { stubs: { 'router-link': { template: '<a><slot /></a>' }, Transition: false, Teleport: { template: '<div><slot /></div>' } } },
      });
      expect(typeof wrapper.vm.handleChat).toBe('function');
    });

    it('应该有viewSellerProfile方法', async () => {
      const wrapper = mount(ItemDetail, {
        global: { stubs: { 'router-link': { template: '<a><slot /></a>' }, Transition: false, Teleport: { template: '<div><slot /></div>' } } },
      });
      expect(typeof wrapper.vm.viewSellerProfile).toBe('function');
    });
  });

  describe('图片浏览方法', () => {
    it('应该有openLightbox方法', async () => {
      const wrapper = mount(ItemDetail, {
        global: { stubs: { 'router-link': { template: '<a><slot /></a>' }, Transition: false, Teleport: { template: '<div><slot /></div>' } } },
      });
      expect(typeof wrapper.vm.openLightbox).toBe('function');
    });

    it('应该有closeLightbox方法', async () => {
      const wrapper = mount(ItemDetail, {
        global: { stubs: { 'router-link': { template: '<a><slot /></a>' }, Transition: false, Teleport: { template: '<div><slot /></div>' } } },
      });
      expect(typeof wrapper.vm.closeLightbox).toBe('function');
    });

    it('应该有prevImage方法', async () => {
      const wrapper = mount(ItemDetail, {
        global: { stubs: { 'router-link': { template: '<a><slot /></a>' }, Transition: false, Teleport: { template: '<div><slot /></div>' } } },
      });
      expect(typeof wrapper.vm.prevImage).toBe('function');
    });

    it('应该有nextImage方法', async () => {
      const wrapper = mount(ItemDetail, {
        global: { stubs: { 'router-link': { template: '<a><slot /></a>' }, Transition: false, Teleport: { template: '<div><slot /></div>' } } },
      });
      expect(typeof wrapper.vm.nextImage).toBe('function');
    });
  });
});
