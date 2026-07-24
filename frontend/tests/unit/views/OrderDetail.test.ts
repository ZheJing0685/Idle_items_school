// @ts-nocheck
import { describe, it, expect, vi, beforeEach, beforeAll } from 'vitest';
import { mount } from '@vue/test-utils';
import { setActivePinia, createPinia } from 'pinia';
import { ElMessage } from 'element-plus';

const mockRouteParams = { orderId: '123' };
const mockRouteQuery = {};
const mockRoute = {
  params: mockRouteParams,
  fullPath: '/user/orders/123',
  query: mockRouteQuery,
  path: '/user/orders/123',
  matched: [],
  meta: {},
  name: 'order-detail',
};
const mockPush = vi.fn();

vi.mock('vue-router', () => ({
  useRoute: () => mockRoute,
  useRouter: () => ({ push: mockPush, replace: vi.fn(), go: vi.fn() }),
  createRouter: vi.fn(() => ({
    beforeEach: vi.fn(),
    afterEach: vi.fn(),
    isReady: vi.fn(() => Promise.resolve()),
  })),
  createWebHistory: vi.fn(),
  RouteRecordRaw: null,
}));

vi.mock('element-plus', () => ({
  ElMessage: { success: vi.fn(), error: vi.fn(), warning: vi.fn(), info: vi.fn() },
}));

const mockGetOrder = vi.fn().mockResolvedValue({ code: 200, data: null });
const mockGetByOrder = vi.fn().mockResolvedValue({ code: 200, data: null });
const mockCanDispute = vi.fn().mockResolvedValue({ code: 200, data: null });

vi.mock('@/api', () => ({
  default: {
    order: { getOrder: mockGetOrder },
    user: {
      disputes: { getByOrder: mockGetByOrder, canDispute: mockCanDispute },
    },
  },
}));

let OrderDetail;
beforeAll(async () => {
  const mod = await import('@/views/user/OrderDetail.vue');
  OrderDetail = mod.default;
});

beforeEach(() => {
  setActivePinia(createPinia());
  vi.clearAllMocks();
  document.body.innerHTML = '';
  mockRoute.params = { orderId: '123' };
  mockPush.mockClear();
});

const mountOrderDetail = (overrides = {}) => {
  return mount(OrderDetail, {
    global: {
      stubs: {
        'router-link': '<a><slot /></a>',
        teleport: '<div><slot /></div>',
        PageHeader: '<div class="page-header"><slot /></div>',
        Transition: false,
        'el-button': '<button><slot /></button>',
        'el-loading': '<div><slot /></div>',
      },
      ...overrides,
    },
  });
};

describe('OrderDetail Component', () => {
  it('should be defined', () => {
    expect(OrderDetail).toBeDefined();
  });

  it('should render page header', async () => {
    mockGetOrder.mockResolvedValueOnce({ code: 200, data: null });
    mockGetByOrder.mockResolvedValueOnce({ code: 200, data: null });
    mockCanDispute.mockResolvedValueOnce({ code: 200, data: null });
    const wrapper = mountOrderDetail();
    await new Promise(r => setTimeout(r, 100));
    expect(wrapper.find('.page-header').exists()).toBe(true);
  });

  it('should show loading state initially', async () => {
    let resolveFn;
    const promise = new Promise(resolve => { resolveFn = resolve; });
    mockGetOrder.mockReturnValueOnce(promise);
    mockGetByOrder.mockReturnValueOnce(promise);
    mockCanDispute.mockReturnValueOnce(promise);
    const wrapper = mountOrderDetail();
    expect(wrapper.vm.loading).toBe(true);
    resolveFn();
    await wrapper.vm.$nextTick();
    await new Promise(r => setTimeout(r, 50));
  });

  it('should render order info when order data is loaded', async () => {
    mockGetOrder.mockResolvedValueOnce({
      code: 200,
      data: {
        id: 123,
        orderNo: 'ORD202401010001',
        orderStatus: 'COMPLETED',
        price: 99,
        itemTitle: '测试商品',
        itemImage: 'https://example.com/img.jpg',
        itemId: 456,
        createdAt: '2024-01-01T10:00:00Z',
      },
    });
    mockGetByOrder.mockResolvedValueOnce({ code: 200, data: null });
    mockCanDispute.mockResolvedValueOnce({ code: 200, data: null });
    const wrapper = mountOrderDetail();
    await new Promise(r => setTimeout(r, 100));
    await wrapper.vm.$nextTick();
    expect(wrapper.text()).toContain('ORD202401010001');
    expect(wrapper.text()).toContain('测试商品');
  });

  it('should render empty state when order not found', async () => {
    mockGetOrder.mockResolvedValueOnce({ code: 404, data: null });
    mockGetByOrder.mockResolvedValueOnce({ code: 200, data: null });
    mockCanDispute.mockResolvedValueOnce({ code: 200, data: null });
    const wrapper = mountOrderDetail();
    await new Promise(r => setTimeout(r, 100));
    await wrapper.vm.$nextTick();
    expect(wrapper.vm.order).toEqual({ code: 404, data: null });
  });

  it('formatTime should format date correctly', () => {
    const wrapper = mountOrderDetail();
    const result = wrapper.vm.formatTime('2024-01-15T08:30:00Z');
    expect(result).toContain('2024');
    expect(result).toContain('01');
    expect(result).toContain('15');
  });

  it('formatTime should return empty string for invalid input', () => {
    const wrapper = mountOrderDetail();
    expect(wrapper.vm.formatTime(null)).toBe('');
    expect(wrapper.vm.formatTime(undefined)).toBe('');
    expect(wrapper.vm.formatTime('')).toBe('');
  });

  it('formatPrice should format integer prices without decimals', () => {
    const wrapper = mountOrderDetail();
    expect(wrapper.vm.formatPrice(99)).toBe('99');
    expect(wrapper.vm.formatPrice(0)).toBe('0');
  });

  it('formatPrice should format decimal prices with two digits', () => {
    const wrapper = mountOrderDetail();
    expect(wrapper.vm.formatPrice(99.9)).toBe('99.90');
    expect(wrapper.vm.formatPrice(99.99)).toBe('99.99');
    expect(wrapper.vm.formatPrice(100.5)).toBe('100.50');
  });

  it('formatPrice should handle string prices', () => {
    const wrapper = mountOrderDetail();
    expect(wrapper.vm.formatPrice('49.5')).toBe('49.50');
  });

  it('computed statusText should return correct Chinese text', async () => {
    mockGetOrder.mockResolvedValueOnce({
      code: 200,
      data: { id: 1, orderNo: 'ORD1', orderStatus: 'COMPLETED', price: 10, itemTitle: '', createdAt: '' },
    });
    mockGetByOrder.mockResolvedValueOnce({ code: 200, data: null });
    mockCanDispute.mockResolvedValueOnce({ code: 200, data: null });
    const wrapper = mountOrderDetail();
    await new Promise(r => setTimeout(r, 100));
    await wrapper.vm.$nextTick();
    expect(wrapper.vm.statusText).toBe('已完成');
  });

  it('computed statusClass should return correct CSS class', async () => {
    mockGetOrder.mockResolvedValueOnce({
      code: 200,
      data: { id: 1, orderNo: 'ORD1', orderStatus: 'SHIPPED', price: 10, itemTitle: '', createdAt: '' },
    });
    mockGetByOrder.mockResolvedValueOnce({ code: 200, data: null });
    mockCanDispute.mockResolvedValueOnce({ code: 200, data: null });
    const wrapper = mountOrderDetail();
    await new Promise(r => setTimeout(r, 100));
    await wrapper.vm.$nextTick();
    expect(wrapper.vm.statusClass).toBe('status-shipped');
  });

  it('computed disputeStatusText should return correct text when dispute exists', async () => {
    mockGetOrder.mockResolvedValueOnce({
      code: 200,
      data: { id: 1, orderNo: 'ORD1', orderStatus: 'COMPLETED', price: 10, itemTitle: '', createdAt: '' },
    });
    mockGetByOrder.mockResolvedValueOnce({
      code: 200,
      data: { id: 1, disputeNo: 'DSP001', disputeStatus: 'PROCESSING', reason: '商品不符', createdAt: '2024-01-02T00:00:00Z' },
    });
    mockCanDispute.mockResolvedValueOnce({ code: 200, data: null });
    const wrapper = mountOrderDetail();
    await new Promise(r => setTimeout(r, 100));
    await wrapper.vm.$nextTick();
    expect(wrapper.vm.disputeStatusText).toBe('处理中');
  });

  it('computed disputeStatusClass should return correct CSS class', async () => {
    mockGetOrder.mockResolvedValueOnce({
      code: 200,
      data: { id: 1, orderNo: 'ORD1', orderStatus: 'COMPLETED', price: 10, itemTitle: '', createdAt: '' },
    });
    mockGetByOrder.mockResolvedValueOnce({
      code: 200,
      data: { id: 1, disputeNo: 'DSP001', disputeStatus: 'ESCALATED', reason: '商品不符', createdAt: '2024-01-02T00:00:00Z' },
    });
    mockCanDispute.mockResolvedValueOnce({ code: 200, data: null });
    const wrapper = mountOrderDetail();
    await new Promise(r => setTimeout(r, 100));
    await wrapper.vm.$nextTick();
    expect(wrapper.vm.disputeStatusClass).toBe('badge-danger');
  });

  it('handleCreateDispute should navigate to create-dispute page', async () => {
    mockGetOrder.mockResolvedValueOnce({
      code: 200,
      data: { id: 123, orderNo: 'ORD1', orderStatus: 'COMPLETED', price: 10, itemTitle: '', createdAt: '' },
    });
    mockGetByOrder.mockResolvedValueOnce({ code: 200, data: null });
    mockCanDispute.mockResolvedValueOnce({ code: 200, data: null });
    const wrapper = mountOrderDetail();
    await new Promise(r => setTimeout(r, 100));
    await wrapper.vm.$nextTick();
    wrapper.vm.handleCreateDispute();
    expect(mockPush).toHaveBeenCalledWith('/user/create-dispute/123');
  });

  it('should display dispute card when dispute exists', async () => {
    mockGetOrder.mockResolvedValueOnce({
      code: 200,
      data: { id: 1, orderNo: 'ORD1', orderStatus: 'COMPLETED', price: 10, itemTitle: '', createdAt: '' },
    });
    mockGetByOrder.mockResolvedValueOnce({
      code: 200,
      data: { id: 1, disputeNo: 'DSP001', disputeStatus: 'PENDING', reason: '描述不符', createdAt: '2024-01-02T00:00:00Z' },
    });
    mockCanDispute.mockResolvedValueOnce({ code: 200, data: null });
    const wrapper = mountOrderDetail();
    await new Promise(r => setTimeout(r, 100));
    await wrapper.vm.$nextTick();
    expect(wrapper.text()).toContain('DSP001');
    expect(wrapper.text()).toContain('描述不符');
  });

  it('should show dispute action card when canDispute is true', async () => {
    mockGetOrder.mockResolvedValueOnce({
      code: 200,
      data: { id: 1, orderNo: 'ORD1', orderStatus: 'COMPLETED', price: 10, itemTitle: '', createdAt: '' },
    });
    mockGetByOrder.mockResolvedValueOnce({ code: 200, data: null });
    mockCanDispute.mockResolvedValueOnce({ code: 200, data: { canDispute: true } });
    const wrapper = mountOrderDetail();
    await new Promise(r => setTimeout(r, 100));
    await wrapper.vm.$nextTick();
    expect(wrapper.vm.canDispute).not.toBeNull();
    expect(wrapper.vm.canDispute.canDispute).toBe(true);
  });

  it('should show no-dispute card when canDispute is false with reason', async () => {
    mockGetOrder.mockResolvedValueOnce({
      code: 200,
      data: { id: 1, orderNo: 'ORD1', orderStatus: 'COMPLETED', price: 10, itemTitle: '', createdAt: '' },
    });
    mockGetByOrder.mockResolvedValueOnce({ code: 200, data: null });
    mockCanDispute.mockResolvedValueOnce({ code: 200, data: { canDispute: false, reason: '已超申请期限' } });
    const wrapper = mountOrderDetail();
    await new Promise(r => setTimeout(r, 100));
    await wrapper.vm.$nextTick();
    expect(wrapper.vm.canDispute.canDispute).toBe(false);
  });

  it('should call ElMessage.error on API failure', async () => {
    mockGetOrder.mockRejectedValueOnce(new Error('网络错误'));
    mockGetByOrder.mockRejectedValueOnce(new Error('网络错误'));
    mockCanDispute.mockRejectedValueOnce(new Error('网络错误'));
    const wrapper = mountOrderDetail();
    await new Promise(r => setTimeout(r, 100));
    await wrapper.vm.$nextTick();
    expect(vi.mocked(ElMessage.error).mock.calls.length).toBeGreaterThan(0);
  });

  it('should set loading to false after fetch completes', async () => {
    mockGetOrder.mockResolvedValueOnce({
      code: 200,
      data: { id: 1, orderNo: 'ORD1', orderStatus: 'COMPLETED', price: 10, itemTitle: '', createdAt: '' },
    });
    mockGetByOrder.mockResolvedValueOnce({ code: 200, data: null });
    mockCanDispute.mockResolvedValueOnce({ code: 200, data: null });
    const wrapper = mountOrderDetail();
    expect(wrapper.vm.loading).toBe(true);
    await new Promise(r => setTimeout(r, 100));
    expect(wrapper.vm.loading).toBe(false);
  });

  it('should skip fetch when orderId is missing', async () => {
    mockRoute.params = {};
    mockGetOrder.mockClear();
    const wrapper = mountOrderDetail();
    await new Promise(r => setTimeout(r, 50));
    expect(mockGetOrder).not.toHaveBeenCalled();
    expect(wrapper.vm.loading).toBe(false);
  });

  it('statusText should return unknown status fallback', async () => {
    mockGetOrder.mockResolvedValueOnce({
      code: 200,
      data: { id: 1, orderNo: 'ORD1', orderStatus: 'UNKNOWN_STATUS', price: 10, itemTitle: '', createdAt: '' },
    });
    mockGetByOrder.mockResolvedValueOnce({ code: 200, data: null });
    mockCanDispute.mockResolvedValueOnce({ code: 200, data: null });
    const wrapper = mountOrderDetail();
    await new Promise(r => setTimeout(r, 100));
    await wrapper.vm.$nextTick();
    expect(wrapper.vm.statusText).toBe('未知状态');
  });

  it('disputeStatusText should return empty string when no dispute', async () => {
    mockGetOrder.mockResolvedValueOnce({
      code: 200,
      data: { id: 1, orderNo: 'ORD1', orderStatus: 'COMPLETED', price: 10, itemTitle: '', createdAt: '' },
    });
    mockGetByOrder.mockResolvedValueOnce({ code: 200, data: null });
    mockCanDispute.mockResolvedValueOnce({ code: 200, data: null });
    const wrapper = mountOrderDetail();
    await new Promise(r => setTimeout(r, 100));
    await wrapper.vm.$nextTick();
    expect(wrapper.vm.disputeStatusText).toBe('');
  });
});
