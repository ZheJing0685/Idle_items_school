// @ts-nocheck
import { describe, it, expect, vi, beforeEach, beforeAll } from 'vitest';
import { mount } from '@vue/test-utils';
import { setActivePinia, createPinia } from 'pinia';

const mockRoute = { params: { id: '42' }, fullPath: '/seller/42', query: {}, path: '/seller/42', matched: [] };
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
}));

const mockSellerGetProfile = vi.fn();
const mockSellerGetItems = vi.fn();
const mockSellerGetReviews = vi.fn();
const mockCreateChat = vi.fn();

vi.mock('@/api', () => ({
  default: {
    seller: {
      getProfile: mockSellerGetProfile,
      getItems: mockSellerGetItems,
      getReviews: mockSellerGetReviews,
    },
    chat: {
      createChat: mockCreateChat,
    },
  },
}));

const mockCategoryStore = vi.hoisted(() => ({
  mockGetCategoryIcon: vi.fn().mockReturnValue(''),
  mockGetCategoryPath: vi.fn().mockReturnValue([]),
}));

vi.mock('@/store/category', () => ({
  useCategoryStore: () => ({
    categories: [],
    flatCategories: [],
    categoryTree: [],
    loaded: true,
    error: null,
    getCategoryIcon: mockCategoryStore.mockGetCategoryIcon,
    getCategoryPath: mockCategoryStore.mockGetCategoryPath,
  }),
}));

vi.mock('@/utils/logger', () => ({
  logger: { error: vi.fn() },
}));

const mockUserStore = vi.hoisted(() => ({
  mockIsLoggedIn: true,
}));

vi.mock('@/store', () => ({
  userStore: () => ({
    isLoggedIn: mockUserStore.mockIsLoggedIn,
  }),
}));

let SellerStore;
beforeAll(async () => {
  const mod = await import('@/views/SellerStore.vue');
  SellerStore = mod.default;
});

const mockProfile = {
  id: 1,
  nickname: '测试卖家',
  verified: true,
  schoolName: '测试大学',
  creditScore: 85,
  bio: '这是一个测试简介',
  totalItems: 5,
  soldItems: 20,
  completedDeals: 18,
  rating: 4.5,
  reviewCount: 10,
  memberSince: '2024-01-15T00:00:00Z',
};

beforeEach(() => {
  setActivePinia(createPinia());
  vi.clearAllMocks();
  document.body.innerHTML = '';
  mockRoute.params = { id: '42' };

  mockSellerGetProfile.mockResolvedValue({ code: 200, data: mockProfile });
  mockSellerGetItems.mockResolvedValue({
    code: 200,
    data: { content: [], totalPages: 0 },
  });
  mockSellerGetReviews.mockResolvedValue({
    code: 200,
    data: { content: [], totalPages: 0 },
  });
});

const mountSellerStore = () => {
  return mount(SellerStore, {
    global: {
      stubs: {
        'router-link': '<a><slot /></a>',
        EmptyState: '<div class="empty-state"><slot name="title" /><slot name="action" /><slot /></div>',
        Transition: false,
        'el-button': true,
        'el-icon': true,
      },
    },
  });
};

describe('SellerStore Component', () => {
  describe('组件渲染', () => {
    it('应该渲染卖家店铺页面', async () => {
      const wrapper = mountSellerStore();
      await new Promise(r => setTimeout(r, 100));
      await wrapper.vm.$nextTick();
      expect(wrapper.find('.store-container').exists()).toBe(true);
    });

    it('应该在加载时显示加载状态', async () => {
      mockSellerGetProfile.mockResolvedValue(new Promise(() => {}));
      const wrapper = mountSellerStore();
      await new Promise(r => setTimeout(r, 50));
      expect(wrapper.find('.loading-container').exists()).toBe(true);
    });

    it('应该在加载失败时显示错误状态', async () => {
      mockSellerGetProfile.mockResolvedValue({ code: 500, message: '卖家不存在' });
      const wrapper = mountSellerStore();
      await new Promise(r => setTimeout(r, 100));
      await wrapper.vm.$nextTick();
      expect(wrapper.find('.error-container').exists()).toBe(true);
    });

    it('应该渲染卖家头像和名称', async () => {
      const wrapper = mountSellerStore();
      await new Promise(r => setTimeout(r, 100));
      await wrapper.vm.$nextTick();
      expect(wrapper.text()).toContain('测试卖家');
    });

    it('应该渲染统计信息区域', async () => {
      const wrapper = mountSellerStore();
      await new Promise(r => setTimeout(r, 100));
      await wrapper.vm.$nextTick();
      expect(wrapper.text()).toContain('在售');
      expect(wrapper.text()).toContain('已售');
      expect(wrapper.text()).toContain('成交');
      expect(wrapper.text()).toContain('评分');
    });

    it('应该渲染Tab导航', async () => {
      const wrapper = mountSellerStore();
      await new Promise(r => setTimeout(r, 100));
      await wrapper.vm.$nextTick();
      expect(wrapper.text()).toContain('在售商品');
      expect(wrapper.text()).toContain('评价');
    });
  });

  describe('computed属性', () => {
    it('avatarLetter应该返回昵称首字母大写', async () => {
      const wrapper = mountSellerStore();
      await new Promise(r => setTimeout(r, 100));
      await wrapper.vm.$nextTick();
      expect(wrapper.vm.avatarLetter).toBe('测');
    });

    it('avatarLetter在profile为null时返回?', async () => {
      mockSellerGetProfile.mockResolvedValue({ code: 500, message: '不存在' });
      const wrapper = mountSellerStore();
      await new Promise(r => setTimeout(r, 100));
      await wrapper.vm.$nextTick();
      expect(wrapper.vm.avatarLetter).toBe('?');
    });

    it('avatarColor应该返回渐变色字符串', async () => {
      const wrapper = mountSellerStore();
      await new Promise(r => setTimeout(r, 100));
      await wrapper.vm.$nextTick();
      expect(typeof wrapper.vm.avatarColor).toBe('string');
      expect(wrapper.vm.avatarColor).toContain('gradient');
    });

    it('ratingDisplay应该返回格式化评分', async () => {
      const wrapper = mountSellerStore();
      await new Promise(r => setTimeout(r, 100));
      await wrapper.vm.$nextTick();
      expect(wrapper.vm.ratingDisplay).toBe('4.5');
    });

    it('ratingDisplay在评分为0时返回暂无', async () => {
      mockProfile.rating = 0;
      mockSellerGetProfile.mockResolvedValue({ code: 200, data: mockProfile });
      const wrapper = mountSellerStore();
      await new Promise(r => setTimeout(r, 100));
      await wrapper.vm.$nextTick();
      expect(wrapper.vm.ratingDisplay).toBe('暂无');
    });

    it('memberSinceText应该返回时间文本', async () => {
      const wrapper = mountSellerStore();
      await new Promise(r => setTimeout(r, 100));
      await wrapper.vm.$nextTick();
      expect(typeof wrapper.vm.memberSinceText).toBe('string');
    });

    it('sellerId应该从路由参数获取', async () => {
      const wrapper = mountSellerStore();
      await wrapper.vm.$nextTick();
      expect(wrapper.vm.sellerId).toBe(42);
    });
  });

  describe('方法 - formatPrice', () => {
    it('应该将数字格式化为两位小数', () => {
      const wrapper = mountSellerStore();
      expect(wrapper.vm.formatPrice(10)).toBe('10.00');
      expect(wrapper.vm.formatPrice(99.9)).toBe('99.90');
      expect(wrapper.vm.formatPrice(123.456)).toBe('123.46');
    });

    it('应该处理字符串类型价格', () => {
      const wrapper = mountSellerStore();
      expect(wrapper.vm.formatPrice('19.9')).toBe('19.90');
    });
  });

  describe('方法 - conditionLabel', () => {
    it('应该将条件枚举映射为中文', () => {
      const wrapper = mountSellerStore();
      expect(wrapper.vm.conditionLabel('NEW')).toBe('全新');
      expect(wrapper.vm.conditionLabel('LIKE_NEW')).toBe('几乎全新');
      expect(wrapper.vm.conditionLabel('GOOD')).toBe('良好');
      expect(wrapper.vm.conditionLabel('FAIR')).toBe('一般');
      expect(wrapper.vm.conditionLabel('POOR')).toBe('较差');
    });

    it('未知条件应原样返回', () => {
      const wrapper = mountSellerStore();
      expect(wrapper.vm.conditionLabel('UNKNOWN')).toBe('UNKNOWN');
    });
  });

  describe('方法 - timeAgo', () => {
    it('应该返回相对时间文本', () => {
      const wrapper = mountSellerStore();
      const result = wrapper.vm.timeAgo(new Date().toISOString());
      expect(result).toBeTruthy();
    });

    it('absolute为true时应返回日期字符串', () => {
      const wrapper = mountSellerStore();
      const result = wrapper.vm.timeAgo('2024-06-15T00:00:00Z', true);
      expect(result).toContain('2024');
      expect(result).toContain('06');
      expect(result).toContain('15');
    });

    it('空值应返回空字符串', () => {
      const wrapper = mountSellerStore();
      expect(wrapper.vm.timeAgo('')).toBe('');
    });
  });

  describe('方法 - getReviewerColor', () => {
    it('应该根据reviewerId返回颜色', () => {
      const wrapper = mountSellerStore();
      const color = wrapper.vm.getReviewerColor({ reviewerId: 1, isAnonymous: false, rating: 5 });
      expect(color).toBeTruthy();
      expect(color.length).toBe(7); // #RRGGBB
    });

    it('匿名用户应有不同颜色', () => {
      const wrapper = mountSellerStore();
      const anonymousColor = wrapper.vm.getReviewerColor({ reviewerId: 1, isAnonymous: true, rating: 5 });
      const userColor = wrapper.vm.getReviewerColor({ reviewerId: 1, isAnonymous: false, rating: 5 });
      expect(anonymousColor).not.toBe(userColor);
    });
  });

  describe('方法 - getCategoryPathLabel', () => {
    it('没有categoryId时应返回categoryName', () => {
      const wrapper = mountSellerStore();
      mockCategoryStore.mockGetCategoryPath.mockReturnValue([]);
      const label = wrapper.vm.getCategoryPathLabel({ categoryName: '电子产品' });
      expect(label).toBe('电子产品');
    });

    it('有categoryId时应返回路径', () => {
      const wrapper = mountSellerStore();
      mockCategoryStore.mockGetCategoryPath.mockReturnValue([{ name: '一级分类' }, { name: '二级分类' }]);
      const label = wrapper.vm.getCategoryPathLabel({ categoryId: 1, categoryName: '子分类' });
      expect(label).toBe('一级分类 > 二级分类');
    });
  });

  describe('方法 - loadProfile', () => {
    it('加载成功应设置profile', async () => {
      const wrapper = mountSellerStore();
      await new Promise(r => setTimeout(r, 100));
      await wrapper.vm.$nextTick();
      expect(wrapper.vm.profile).toBeTruthy();
      expect(wrapper.vm.profile.nickname).toBe('测试卖家');
    });

    it('加载失败应设置error', async () => {
      mockSellerGetProfile.mockResolvedValue({ code: 404, message: '未找到' });
      const wrapper = mountSellerStore();
      await new Promise(r => setTimeout(r, 100));
      await wrapper.vm.$nextTick();
      expect(wrapper.vm.error).toBeTruthy();
    });
  });

  describe('方法 - loadItems', () => {
    it('加载商品列表应设置items', async () => {
      const mockItems = [
        { id: 1, title: '商品1', price: 100, condition: 'NEW', createdAt: new Date().toISOString() },
      ];
      mockSellerGetItems.mockResolvedValue({ code: 200, data: { content: mockItems, totalPages: 1 } });
      const wrapper = mountSellerStore();
      await new Promise(r => setTimeout(r, 100));
      await wrapper.vm.loadItems(1);
      expect(wrapper.vm.items.length).toBe(1);
      expect(wrapper.vm.items[0].title).toBe('商品1');
    });

    it('加载失败应清空items', async () => {
      mockSellerGetItems.mockRejectedValue(new Error('网络错误'));
      const wrapper = mountSellerStore();
      await wrapper.vm.loadItems(1);
      expect(wrapper.vm.items.length).toBe(0);
    });
  });

  describe('方法 - loadReviews', () => {
    it('加载评价列表应设置reviews', async () => {
      const mockReviews = [
        { id: 1, reviewerNickname: '用户A', rating: 5, content: '很好', createdAt: new Date().toISOString() },
      ];
      mockSellerGetReviews.mockResolvedValue({ code: 200, data: { content: mockReviews, totalPages: 1 } });
      const wrapper = mountSellerStore();
      await new Promise(r => setTimeout(r, 100));
      await wrapper.vm.loadReviews(1);
      expect(wrapper.vm.reviews.length).toBe(1);
    });

    it('加载失败应清空reviews', async () => {
      mockSellerGetReviews.mockRejectedValue(new Error('网络错误'));
      const wrapper = mountSellerStore();
      await wrapper.vm.loadReviews(1);
      expect(wrapper.vm.reviews.length).toBe(0);
    });
  });

  describe('方法 - contactSeller', () => {
    it('未登录时应提示并跳转登录', async () => {
      mockUserStore.mockIsLoggedIn = false;
      const wrapper = mountSellerStore();
      await new Promise(r => setTimeout(r, 100));
      await wrapper.vm.$nextTick();
      await wrapper.vm.contactSeller();
      expect(mockPush).toHaveBeenCalledWith('/login');
      mockUserStore.mockIsLoggedIn = true;
    });

    it('创建聊天成功应跳转到聊天页面', async () => {
      mockCreateChat.mockResolvedValue({ code: 200, data: { id: 100 } });
      const wrapper = mountSellerStore();
      await new Promise(r => setTimeout(r, 100));
      await wrapper.vm.$nextTick();
      await wrapper.vm.contactSeller();
      expect(mockPush).toHaveBeenCalledWith('/user/chat?chatId=100');
    });

    it('创建聊天失败应显示错误消息', async () => {
      mockCreateChat.mockResolvedValue({ code: 500, message: '失败' });
      const wrapper = mountSellerStore();
      await new Promise(r => setTimeout(r, 100));
      await wrapper.vm.$nextTick();
      await wrapper.vm.contactSeller();
    });
  });

  describe('方法 - goToItem', () => {
    it('应该跳转到对应商品详情页', () => {
      const wrapper = mountSellerStore();
      wrapper.vm.goToItem(123);
      expect(mockPush).toHaveBeenCalledWith('/item/123');
    });
  });

  describe('方法 - goBack', () => {
    it('应该返回首页', () => {
      const wrapper = mountSellerStore();
      wrapper.vm.goBack();
      expect(mockPush).toHaveBeenCalledWith('/');
    });
  });

  describe('Tab切换', () => {
    it('切换到评价Tab时应加载评价数据', async () => {
      const mockReviews = [
        { id: 1, reviewerNickname: '用户A', rating: 5, content: '好评', createdAt: new Date().toISOString() },
      ];
      mockSellerGetReviews.mockResolvedValue({ code: 200, data: { content: mockReviews, totalPages: 1 } });
      const wrapper = mountSellerStore();
      await new Promise(r => setTimeout(r, 100));
      await wrapper.vm.$nextTick();
      wrapper.vm.activeTab = 'reviews';
      await new Promise(r => setTimeout(r, 100));
      await wrapper.vm.$nextTick();
      expect(wrapper.vm.reviews.length).toBe(1);
    });

    it('已加载的商品不应重复加载', async () => {
      mockSellerGetItems.mockResolvedValue({ code: 200, data: { content: [{ id: 1, title: '商品1', price: 50 }], totalPages: 1 } });
      const wrapper = mountSellerStore();
      await new Promise(r => setTimeout(r, 100));
      await wrapper.vm.$nextTick();
      expect(mockSellerGetItems).toHaveBeenCalledTimes(1);
    });
  });
});
