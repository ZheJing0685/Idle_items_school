// @ts-nocheck
import { describe, it, expect, vi, beforeEach, beforeAll } from 'vitest';
import { mount } from '@vue/test-utils';
import { setActivePinia, createPinia } from 'pinia';

// Mock route
const mockRoute = {
  params: { id: '1' },
  fullPath: '/item/1',
  query: {},
};

const mockPush = vi.fn();

vi.mock('vue-router', () => ({
  useRoute: () => mockRoute,
  useRouter: () => ({
    push: mockPush,
  }),
  createRouter: vi.fn(() => ({
    beforeEach: vi.fn(),
    afterEach: vi.fn(),
  })),
  createWebHistory: vi.fn(),
}));

vi.mock('@/api/config/axios', () => ({
  default: {
    interceptors: {
      request: { use: vi.fn() },
      response: { use: vi.fn() },
    },
  },
  clearAuthState: vi.fn(),
  setUnauthorizedHandler: vi.fn(),
  setToken: vi.fn(),
  getToken: vi.fn(() => ''),
  clearToken: vi.fn(),
}));

// Mock Element Plus
vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn(),
  },
}));

// Mock API
const mockGetItem = vi.fn();
const mockGetReviewsByItem = vi.fn();
const mockCheckFavorite = vi.fn();
const mockAddFavorite = vi.fn();
const mockRemoveFavorite = vi.fn();
const mockCreateOrder = vi.fn();
const mockCreateChat = vi.fn();
const mockOffShelf = vi.fn();

vi.mock('@/api', () => ({
  default: {
    item: {
      getItem: mockGetItem,
      offShelf: mockOffShelf,
    },
    review: {
      getReviewsByItem: mockGetReviewsByItem,
    },
    favorite: {
      checkFavorite: mockCheckFavorite,
      addFavorite: mockAddFavorite,
      removeFavorite: mockRemoveFavorite,
    },
    order: {
      createOrder: mockCreateOrder,
    },
    chat: {
      createChat: mockCreateChat,
    },
  },
}));

// Mock dict store
vi.mock('@/store/dict', () => ({
  useDictStore: () => ({
    getDictLabel: vi.fn().mockImplementation((type, value) => {
      const map: Record<string, string> = {
        NEW: '全新',
        LIKE_NEW: '九成新',
        GOOD: '八成新',
        FAIR: '七成新',
        POOR: '六成新及以下',
        LOCAL_DELIVERY: '自提',
        HOME_DELIVERY: '上门',
        EXPRESS: '快递',
        MAIL: '邮寄',
      };
      return map[value] || value;
    }),
  }),
}));

// Mock store
vi.mock('@/store', () => ({
  userStore: () => ({
    user: {
      id: 1,
      nickname: '测试用户',
    },
  }),
}));

let ItemDetail: any;
beforeAll(async () => {
  const mod = await import('@/views/ItemDetail.vue');
  ItemDetail = mod.default;
});

beforeEach(() => {
  setActivePinia(createPinia());
  vi.clearAllMocks();
  localStorage.clear();
  
  mockGetItem.mockResolvedValue({
    code: 200,
    data: {
      id: 1,
      title: '测试物品',
      price: 100,
      originalPrice: 200,
      description: '这是一个测试物品',
      condition: 'NEW',
      deliveryMethod: 'EXPRESS',
      categoryName: '电子产品',
      location: '校园南门',
      images: ['img1.jpg', 'img2.jpg'],
      coverImage: 'cover.jpg',
      viewCount: 100,
      favoriteCount: 50,
      isBargainAllowed: true,
      verified: true,
      userId: 1,
      sellerNickname: '卖家',
      sellerRating: 4.5,
      sellerItemsCount: 10,
      createdAt: new Date().toISOString(),
    },
  });

  mockGetReviewsByItem.mockResolvedValue({
    code: 200,
    data: {
      content: [
        {
          id: 1,
          rating: 5,
          content: '很好的物品',
          reviewerNickname: '买家',
          isAnonymous: false,
          createdAt: new Date().toISOString(),
        },
      ],
    },
  });

  mockCheckFavorite.mockResolvedValue({
    code: 200,
    data: false,
  });
});

const mountItemDetail = () => {
  return mount(ItemDetail, {
    global: {
      stubs: {
        'router-link': { template: '<a><slot /></a>' },
        'el-avatar': { template: '<div><slot /></div>', props: ['size'] },
        'el-button': { template: '<button><slot /></button>', props: ['type', 'size'] },
        'el-dialog': { template: '<div><slot /></div>', props: ['modelValue', 'title'] },
        'el-form': { template: '<form><slot /></form>', props: ['model'] },
        'el-form-item': { template: '<div><slot /></div>', props: ['label'] },
        'el-input': { template: '<input />', props: ['modelValue', 'placeholder', 'type'] },
      },
    },
  });
};

describe('ItemDetail Component', () => {
  describe('组件渲染', () => {
    it('应该渲染物品详情页面', () => {
      const wrapper = mountItemDetail();
      expect(wrapper.find('.item-detail-page').exists()).toBe(true);
    });

    it('应该渲染面包屑导航', () => {
      const wrapper = mountItemDetail();
      expect(wrapper.find('.breadcrumb').exists()).toBe(true);
      expect(wrapper.text()).toContain('首页');
      expect(wrapper.text()).toContain('发现好物');
      expect(wrapper.text()).toContain('物品详情');
    });
  });

  describe('加载状态', () => {
    it('应该有loading状态', () => {
      const wrapper = mountItemDetail();
      expect(wrapper.vm.loading).toBeDefined();
      expect(typeof wrapper.vm.loading).toBe('boolean');
    });

    it('初始应该处于加载状态', () => {
      const wrapper = mountItemDetail();
      expect(wrapper.vm.loading).toBe(true);
    });
  });

  describe('组件状态', () => {
    it('应该有item数据', () => {
      const wrapper = mountItemDetail();
      expect(wrapper.vm.item).toBeDefined();
    });

    it('应该有reviews数组', () => {
      const wrapper = mountItemDetail();
      expect(wrapper.vm.reviews).toBeDefined();
      expect(Array.isArray(wrapper.vm.reviews)).toBe(true);
    });

    it('应该有isFavorited状态', () => {
      const wrapper = mountItemDetail();
      expect(wrapper.vm.isFavorited).toBeDefined();
      expect(typeof wrapper.vm.isFavorited).toBe('boolean');
    });

    it('应该有showBuyDialog状态', () => {
      const wrapper = mountItemDetail();
      expect(wrapper.vm.showBuyDialog).toBeDefined();
      expect(typeof wrapper.vm.showBuyDialog).toBe('boolean');
    });

    it('应该有orderForm数据', () => {
      const wrapper = mountItemDetail();
      expect(wrapper.vm.orderForm).toBeDefined();
      expect(wrapper.vm.orderForm).toHaveProperty('buyerName');
      expect(wrapper.vm.orderForm).toHaveProperty('buyerPhone');
      expect(wrapper.vm.orderForm).toHaveProperty('buyerAddress');
    });
  });

  describe('方法', () => {
    it('应该有fetchItemDetail方法', () => {
      const wrapper = mountItemDetail();
      expect(typeof wrapper.vm.fetchItemDetail).toBe('function');
    });

    it('应该有fetchReviews方法', () => {
      const wrapper = mountItemDetail();
      expect(typeof wrapper.vm.fetchReviews).toBe('function');
    });

    it('应该有toggleFavorite方法', () => {
      const wrapper = mountItemDetail();
      expect(typeof wrapper.vm.toggleFavorite).toBe('function');
    });

    it('应该有handleBuy方法', () => {
      const wrapper = mountItemDetail();
      expect(typeof wrapper.vm.handleBuy).toBe('function');
    });

    it('应该有confirmBuy方法', () => {
      const wrapper = mountItemDetail();
      expect(typeof wrapper.vm.confirmBuy).toBe('function');
    });

    it('应该有handleContact方法', () => {
      const wrapper = mountItemDetail();
      expect(typeof wrapper.vm.handleContact).toBe('function');
    });

    it('应该有editItem方法', () => {
      const wrapper = mountItemDetail();
      expect(typeof wrapper.vm.editItem).toBe('function');
    });

    it('应该有offShelf方法', () => {
      const wrapper = mountItemDetail();
      expect(typeof wrapper.vm.offShelf).toBe('function');
    });
  });

  describe('辅助方法', () => {
    it('应该有getConditionText方法', () => {
      const wrapper = mountItemDetail();
      expect(typeof wrapper.vm.getConditionText).toBe('function');
    });

    it('应该有getDeliveryText方法', () => {
      const wrapper = mountItemDetail();
      expect(typeof wrapper.vm.getDeliveryText).toBe('function');
    });

    it('应该有formatTime方法', () => {
      const wrapper = mountItemDetail();
      expect(typeof wrapper.vm.formatTime).toBe('function');
    });

    it('应该有parseImages方法', () => {
      const wrapper = mountItemDetail();
      expect(typeof wrapper.vm.parseImages).toBe('function');
    });
  });

  describe('辅助方法功能', () => {
    it('getConditionText应该返回中文条件', () => {
      const wrapper = mountItemDetail();
      expect(wrapper.vm.getConditionText('NEW')).toBe('全新');
      expect(wrapper.vm.getConditionText('LIKE_NEW')).toBe('九成新');
      expect(wrapper.vm.getConditionText('GOOD')).toBe('八成新');
    });

    it('getDeliveryText应该返回中文配送方式', () => {
      const wrapper = mountItemDetail();
      expect(wrapper.vm.getDeliveryText('LOCAL_DELIVERY')).toBe('自提');
      expect(wrapper.vm.getDeliveryText('HOME_DELIVERY')).toBe('上门');
      expect(wrapper.vm.getDeliveryText('EXPRESS')).toBe('快递');
    });

    it('formatTime应该格式化时间', () => {
      const wrapper = mountItemDetail();
      const now = new Date().toISOString();
      expect(wrapper.vm.formatTime(now)).toBe('今天');
    });

    it('formatTime空值应该返回空字符串', () => {
      const wrapper = mountItemDetail();
      expect(wrapper.vm.formatTime('')).toBe('');
      expect(wrapper.vm.formatTime(null)).toBe('');
    });

    it('parseImages应该解析数组格式图片', () => {
      const wrapper = mountItemDetail();
      const result = wrapper.vm.parseImages(['img1.jpg', 'img2.jpg']);
      expect(result).toEqual(['img1.jpg', 'img2.jpg']);
    });

    it('parseImages应该解析JSON字符串格式图片', () => {
      const wrapper = mountItemDetail();
      const result = wrapper.vm.parseImages('["img1.jpg","img2.jpg"]');
      expect(result).toEqual(['img1.jpg', 'img2.jpg']);
    });

    it('parseImages空值应该返回空数组', () => {
      const wrapper = mountItemDetail();
      expect(wrapper.vm.parseImages(null)).toEqual([]);
      expect(wrapper.vm.parseImages('')).toEqual([]);
    });
  });

  // 计算属性测试已移除：isOwner/isNew/discountPercent 已从组件中移除
});
