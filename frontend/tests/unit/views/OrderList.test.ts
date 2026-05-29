import { describe, it, expect, vi, beforeEach, beforeAll } from 'vitest';
import { mount } from '@vue/test-utils';
import { setActivePinia, createPinia } from 'pinia';

// Mock route
const mockRoute = {
  query: {},
  path: '/user/orders',
};

const mockReplace = vi.fn();

vi.mock('vue-router', () => ({
  useRoute: () => mockRoute,
  useRouter: () => ({
    replace: mockReplace,
  }),
}));

// Mock Element Plus
vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn(),
    info: vi.fn(),
  },
  ElMessageBox: {
    confirm: vi.fn().mockResolvedValue(undefined),
    prompt: vi.fn().mockResolvedValue({ value: '退款原因' }),
  },
}));

// Mock API
const mockGetBuyerOrders = vi.fn();
const mockGetSellerOrders = vi.fn();
const mockPayOrder = vi.fn();
const mockCancelOrder = vi.fn();
const mockShipOrder = vi.fn();
const mockConfirmReceive = vi.fn();
const mockApplyRefund = vi.fn();
const mockCreateReview = vi.fn();

vi.mock('@/api', () => ({
  default: {
    order: {
      getBuyerOrders: mockGetBuyerOrders,
      getSellerOrders: mockGetSellerOrders,
      payOrder: mockPayOrder,
      cancelOrder: mockCancelOrder,
      shipOrder: mockShipOrder,
      confirmReceive: mockConfirmReceive,
      applyRefund: mockApplyRefund,
    },
    review: {
      createReview: mockCreateReview,
    },
  },
}));

// Mock orderFlow utils
vi.mock('@/utils/business/orderFlow', () => ({
  getOrderActions: vi.fn().mockReturnValue([]),
  getOrderHint: vi.fn().mockReturnValue(''),
  getOrderStatusClass: vi.fn().mockReturnValue('status-pending'),
  getOrderStatusOptions: vi.fn().mockReturnValue([
    { value: 'ALL', label: '全部' },
    { value: 'PENDING_PAYMENT', label: '待支付' },
  ]),
  getOrderStatusText: vi.fn().mockReturnValue('待支付'),
  normalizeOrder: vi.fn().mockImplementation((order) => order),
  sanitizeOrderStatus: vi.fn().mockImplementation((status) => status || 'ALL'),
  sanitizeOrderView: vi.fn().mockImplementation((view) => view || 'buyer'),
}));

// Mock child components
vi.mock('@/components/user/PageHeader.vue', () => ({
  default: {
    template: '<div class="page-header"><slot name="action" /><slot /></div>',
    props: ['title', 'subtitle'],
  },
}));

vi.mock('@/components/user/FilterTabs.vue', () => ({
  default: {
    template: '<div class="filter-tabs"><slot /></div>',
    props: ['modelValue', 'tabs'],
    emits: ['update:modelValue', 'change'],
  },
}));

vi.mock('@/components/user/EmptyState.vue', () => ({
  default: {
    template: '<div class="empty-state"><slot name="action" /><slot /></div>',
    props: ['title', 'description'],
  },
}));

let OrderList: any;
beforeAll(async () => {
  const mod = await import('@/views/OrderList.vue');
  OrderList = mod.default;
});

beforeEach(() => {
  setActivePinia(createPinia());
  vi.clearAllMocks();
  localStorage.clear();
  mockRoute.query = {};
  
  mockGetBuyerOrders.mockResolvedValue({
    code: 200,
    data: {
      content: [
        {
          id: 1,
          orderNo: 'ORD20240101001',
          itemId: 1,
          itemTitle: '测试物品',
          itemCover: '',
          price: 100,
          orderStatus: 'PENDING_PAYMENT',
          createdAt: new Date().toISOString(),
        },
      ],
      totalElements: 1,
    },
  });

  mockGetSellerOrders.mockResolvedValue({
    code: 200,
    data: {
      content: [],
      totalElements: 0,
    },
  });
});

const mountOrderList = () => {
  return mount(OrderList, {
    global: {
      stubs: {
        'router-link': { template: '<a><slot /></a>' },
        'el-pagination': { template: '<div />', props: ['currentPage', 'pageSize', 'total'] },
        'el-button': { template: '<button><slot /></button>', props: ['type', 'size', 'plain'] },
        'el-dialog': { template: '<div><slot /></div>', props: ['modelValue', 'title'] },
        'el-input': { template: '<input />', props: ['modelValue', 'type', 'rows'] },
      },
    },
  });
};

describe('OrderList Component', () => {
  describe('组件渲染', () => {
    it('应该渲染订单列表页面', () => {
      const wrapper = mountOrderList();
      expect(wrapper.find('.orders-page').exists()).toBe(true);
    });

    it('应该渲染视图切换按钮', () => {
      const wrapper = mountOrderList();
      expect(wrapper.text()).toContain('我买到的');
      expect(wrapper.text()).toContain('我卖出的');
    });

    it('应该渲染订单面板', () => {
      const wrapper = mountOrderList();
      expect(wrapper.find('.orders-panel').exists()).toBe(true);
    });
  });

  describe('组件状态', () => {
    it('应该有currentView状态', () => {
      const wrapper = mountOrderList();
      expect(wrapper.vm.currentView).toBeDefined();
      expect(typeof wrapper.vm.currentView).toBe('string');
    });

    it('应该有currentTab状态', () => {
      const wrapper = mountOrderList();
      expect(wrapper.vm.currentTab).toBeDefined();
      expect(typeof wrapper.vm.currentTab).toBe('string');
    });

    it('应该有currentPage状态', () => {
      const wrapper = mountOrderList();
      expect(wrapper.vm.currentPage).toBeDefined();
      expect(typeof wrapper.vm.currentPage).toBe('number');
    });

    it('应该有orders数组', () => {
      const wrapper = mountOrderList();
      expect(wrapper.vm.orders).toBeDefined();
      expect(Array.isArray(wrapper.vm.orders)).toBe(true);
    });

    it('应该有loading状态', () => {
      const wrapper = mountOrderList();
      expect(wrapper.vm.loading).toBeDefined();
      expect(typeof wrapper.vm.loading).toBe('boolean');
    });

    it('应该有showReviewDialog状态', () => {
      const wrapper = mountOrderList();
      expect(wrapper.vm.showReviewDialog).toBeDefined();
      expect(typeof wrapper.vm.showReviewDialog).toBe('boolean');
    });

    it('应该有reviewRating状态', () => {
      const wrapper = mountOrderList();
      expect(wrapper.vm.reviewRating).toBeDefined();
      expect(typeof wrapper.vm.reviewRating).toBe('number');
    });

    it('应该有reviewContent状态', () => {
      const wrapper = mountOrderList();
      expect(wrapper.vm.reviewContent).toBeDefined();
      expect(typeof wrapper.vm.reviewContent).toBe('string');
    });
  });

  describe('方法', () => {
    it('应该有loadOrders方法', () => {
      const wrapper = mountOrderList();
      expect(typeof wrapper.vm.loadOrders).toBe('function');
    });

    it('应该有setView方法', () => {
      const wrapper = mountOrderList();
      expect(typeof wrapper.vm.setView).toBe('function');
    });

    it('应该有handleTabChange方法', () => {
      const wrapper = mountOrderList();
      expect(typeof wrapper.vm.handleTabChange).toBe('function');
    });

    it('应该有handlePageChange方法', () => {
      const wrapper = mountOrderList();
      expect(typeof wrapper.vm.handlePageChange).toBe('function');
    });

    it('应该有handleAction方法', () => {
      const wrapper = mountOrderList();
      expect(typeof wrapper.vm.handleAction).toBe('function');
    });

    it('应该有getActions方法', () => {
      const wrapper = mountOrderList();
      expect(typeof wrapper.vm.getActions).toBe('function');
    });

    it('应该有getOrderHint方法', () => {
      const wrapper = mountOrderList();
      expect(typeof wrapper.vm.getOrderHint).toBe('function');
    });

    it('应该有viewDetail方法', () => {
      const wrapper = mountOrderList();
      expect(typeof wrapper.vm.viewDetail).toBe('function');
    });

    it('应该有submitReview方法', () => {
      const wrapper = mountOrderList();
      expect(typeof wrapper.vm.submitReview).toBe('function');
    });

    it('应该有refreshOrders方法', () => {
      const wrapper = mountOrderList();
      expect(typeof wrapper.vm.refreshOrders).toBe('function');
    });
  });

  describe('辅助方法', () => {
    it('应该有formatTime方法', () => {
      const wrapper = mountOrderList();
      expect(typeof wrapper.vm.formatTime).toBe('function');
    });

    it('应该有formatPrice方法', () => {
      const wrapper = mountOrderList();
      expect(typeof wrapper.vm.formatPrice).toBe('function');
    });
  });

  describe('辅助方法功能', () => {
    it('formatTime应该格式化时间', () => {
      const wrapper = mountOrderList();
      const now = new Date().toISOString();
      expect(wrapper.vm.formatTime(now)).toBeTruthy();
    });

    it('formatTime空值应该返回空字符串', () => {
      const wrapper = mountOrderList();
      expect(wrapper.vm.formatTime('')).toBe('');
    });

    it('formatPrice应该格式化价格', () => {
      const wrapper = mountOrderList();
      expect(wrapper.vm.formatPrice(100)).toBe('100');
      expect(wrapper.vm.formatPrice(99.99)).toBe('99.99');
    });

    it('formatPrice应该处理空值', () => {
      const wrapper = mountOrderList();
      expect(wrapper.vm.formatPrice(null)).toBe('0');
      expect(wrapper.vm.formatPrice(undefined)).toBe('0');
    });
  });

  describe('计算属性', () => {
    it('应该有pageSubtitle计算属性', () => {
      const wrapper = mountOrderList();
      expect(wrapper.vm.pageSubtitle).toBeDefined();
    });

    it('应该有currentViewLabel计算属性', () => {
      const wrapper = mountOrderList();
      expect(wrapper.vm.currentViewLabel).toBeDefined();
    });

    it('应该有tabs计算属性', () => {
      const wrapper = mountOrderList();
      expect(wrapper.vm.tabs).toBeDefined();
      expect(Array.isArray(wrapper.vm.tabs)).toBe(true);
    });

    it('应该有emptyTitle计算属性', () => {
      const wrapper = mountOrderList();
      expect(wrapper.vm.emptyTitle).toBeDefined();
    });

    it('应该有emptyDesc计算属性', () => {
      const wrapper = mountOrderList();
      expect(wrapper.vm.emptyDesc).toBeDefined();
    });
  });

  describe('数据加载', () => {
    it('应该有loadOrders方法', async () => {
      const wrapper = mountOrderList();
      expect(typeof wrapper.vm.loadOrders).toBe('function');
    });

    it('应该有refreshOrders方法', async () => {
      const wrapper = mountOrderList();
      expect(typeof wrapper.vm.refreshOrders).toBe('function');
    });
  });
});
