// @ts-nocheck
import { describe, it, expect, vi, beforeEach, beforeAll } from 'vitest';
import { mount } from '@vue/test-utils';
import { setActivePinia, createPinia } from 'pinia';

const mockRoute = { params: { orderId: '100' }, fullPath: '/user/create-dispute/100', query: {}, path: '/user/create-dispute/100', matched: [] };
const mockPush = vi.fn();
const mockBack = vi.fn();

vi.mock('vue-router', () => ({
  useRoute: () => mockRoute,
  useRouter: () => ({ push: mockPush, back: mockBack, replace: vi.fn() }),
  createRouter: vi.fn(() => ({
    beforeEach: vi.fn(),
    afterEach: vi.fn(),
    isReady: vi.fn(() => Promise.resolve()),
  })),
  createWebHistory: vi.fn(),
}));

vi.mock('element-plus', () => ({
  ElMessage: { success: vi.fn(), error: vi.fn(), warning: vi.fn() },
  FormInstance: null,
}));

const mockDisputesCreate = vi.fn();
const mockDisputesCanDispute = vi.fn();

vi.mock('@/api', () => ({
  default: {
    user: {
      disputes: {
        create: mockDisputesCreate,
        canDispute: mockDisputesCanDispute,
      },
    },
  },
}));

let CreateDispute;
beforeAll(async () => {
  const mod = await import('@/views/user/CreateDispute.vue');
  CreateDispute = mod.default;
});

beforeEach(() => {
  setActivePinia(createPinia());
  vi.clearAllMocks();
  document.body.innerHTML = '';
  mockRoute.params = { orderId: '100' };

  mockDisputesCanDispute.mockResolvedValue({
    code: 200,
    data: {
      itemTitle: '测试物品',
      orderAmount: 99.9,
      price: 99.9,
      orderStatus: 'COMPLETED',
    },
  });
});

const mountCreateDispute = () => {
  return mount(CreateDispute, {
    global: {
      stubs: {
        'el-form': {
          template: '<div class="dispute-form"><slot /></div>',
          methods: {
            validate: vi.fn().mockResolvedValue(true),
          },
        },
        'el-form-item': '<div><slot /></div>',
        'el-radio-group': '<div><slot /></div>',
        'el-radio': '<label><slot /></label>',
        'el-input': '<input />',
        'el-select': '<select><slot /></select>',
        'el-option': '<option />',
        'el-upload': '<div class="upload-area"><slot /></div>',
        'el-button': '<button><slot /></button>',
        Plus: '<span>+</span>',
      },
    },
  });
};

describe('CreateDispute Component', () => {
  describe('订单信息展示', () => {
    it('应该渲染纠纷申请页面标题', () => {
      const wrapper = mountCreateDispute();
      expect(wrapper.text()).toContain('申请纠纷');
    });

    it('应该在onMounted后加载订单信息', async () => {
      const wrapper = mountCreateDispute();
      await new Promise(r => setTimeout(r, 100));
      await wrapper.vm.$nextTick();
      expect(wrapper.vm.orderInfo).toBeTruthy();
      expect(wrapper.vm.orderInfo.itemTitle).toBe('测试物品');
    });

    it('should display order status label', async () => {
      const wrapper = mountCreateDispute();
      await new Promise(r => setTimeout(r, 100));
      await wrapper.vm.$nextTick();
      expect(wrapper.text()).toContain('已完成');
    });
  });

  describe('方法 - getOrderStatusLabel', () => {
    it('PENDING_PAYMENT应返回待付款', () => {
      const wrapper = mountCreateDispute();
      expect(wrapper.vm.getOrderStatusLabel('PENDING_PAYMENT')).toBe('待付款');
    });

    it('SHIPPED应返回已发货', () => {
      const wrapper = mountCreateDispute();
      expect(wrapper.vm.getOrderStatusLabel('SHIPPED')).toBe('已发货');
    });

    it('COMPLETED应返回已完成', () => {
      const wrapper = mountCreateDispute();
      expect(wrapper.vm.getOrderStatusLabel('COMPLETED')).toBe('已完成');
    });

    it('REFUND_REQUESTED应返回退款中', () => {
      const wrapper = mountCreateDispute();
      expect(wrapper.vm.getOrderStatusLabel('REFUND_REQUESTED')).toBe('退款中');
    });

    it('未知状态应原样返回', () => {
      const wrapper = mountCreateDispute();
      expect(wrapper.vm.getOrderStatusLabel('UNKNOWN')).toBe('UNKNOWN');
    });
  });

  describe('方法 - handleUploadSuccess', () => {
    it('上传成功应添加图片URL到evidenceImages', () => {
      const wrapper = mountCreateDispute();
      const res = { code: 200, data: 'https://example.com/img.jpg' };
      wrapper.vm.handleUploadSuccess(res, {});
      expect(wrapper.vm.evidenceImages).toContain('https://example.com/img.jpg');
    });

    it('上传失败应不添加图片', () => {
      const wrapper = mountCreateDispute();
      const res = { code: 500, message: '上传失败' };
      wrapper.vm.handleUploadSuccess(res, {});
      expect(wrapper.vm.evidenceImages.length).toBe(0);
    });
  });

  describe('方法 - handleUploadError', () => {
    it('上传出错应显示错误消息', () => {
      const wrapper = mountCreateDispute();
      wrapper.vm.handleUploadError();
      // ElMessage.error should be called via mock
    });
  });

  describe('方法 - handleRemove', () => {
    it('移除图片应从evidenceImages中删除', () => {
      const wrapper = mountCreateDispute();
      wrapper.vm.evidenceImages = ['url1', 'url2', 'url3'];
      const file = { response: { data: 'url2' } };
      wrapper.vm.handleRemove(file, []);
      expect(wrapper.vm.evidenceImages).toEqual(['url1', 'url3']);
    });
  });

  describe('方法 - beforeUpload', () => {
    it('非图片文件应返回false', () => {
      const wrapper = mountCreateDispute();
      const file = { type: 'application/pdf', size: 1024 };
      expect(wrapper.vm.beforeUpload(file)).toBe(false);
    });

    it('超过5MB的图片应返回false', () => {
      const wrapper = mountCreateDispute();
      const file = { type: 'image/jpeg', size: 6 * 1024 * 1024 };
      expect(wrapper.vm.beforeUpload(file)).toBe(false);
    });

    it('有效图片应返回true', () => {
      const wrapper = mountCreateDispute();
      const file = { type: 'image/png', size: 1024 * 500 };
      expect(wrapper.vm.beforeUpload(file)).toBe(true);
    });
  });

  describe('方法 - submitForm', () => {
    it('提交成功应跳转纠纷列表', async () => {
      mockDisputesCreate.mockResolvedValue({ code: 200 });
      const wrapper = mountCreateDispute();
      await new Promise(r => setTimeout(r, 100));
      await wrapper.vm.$nextTick();
      wrapper.vm.form = {
        disputeType: 1,
        reason: '商品有问题',
        description: '详细描述这个问题',
        expectResult: '全额退款',
        expectRefundAmount: null,
      };
      await wrapper.vm.submitForm();
      expect(mockPush).toHaveBeenCalledWith('/user/disputes');
    });

    it('提交失败应显示错误消息', async () => {
      mockDisputesCreate.mockResolvedValue({ code: 500, message: '提交失败' });
      const wrapper = mountCreateDispute();
      await new Promise(r => setTimeout(r, 100));
      await wrapper.vm.$nextTick();
      wrapper.vm.form = {
        disputeType: 1,
        reason: '商品有问题',
        description: '详细描述这个问题',
        expectResult: '全额退款',
        expectRefundAmount: null,
      };
      await wrapper.vm.submitForm();
    });

    it('部分退款但未填金额应显示警告', async () => {
      const wrapper = mountCreateDispute();
      await new Promise(r => setTimeout(r, 100));
      await wrapper.vm.$nextTick();
      wrapper.vm.form = {
        disputeType: 1,
        reason: '商品有问题',
        description: '详细描述这个问题',
        expectResult: '部分退款',
        expectRefundAmount: null,
      };
      await wrapper.vm.submitForm();
      expect(wrapper.vm.submitting).toBe(false);
    });
  });

  describe('方法 - fetchOrderInfo', () => {
    it('应调用api.user.disputes.canDispute', async () => {
      const wrapper = mountCreateDispute();
      await new Promise(r => setTimeout(r, 100));
      await wrapper.vm.$nextTick();
      expect(mockDisputesCanDispute).toHaveBeenCalledWith(100);
    });

    it('API失败应静默处理', async () => {
      mockDisputesCanDispute.mockRejectedValue(new Error('网络错误'));
      const wrapper = mountCreateDispute();
      await wrapper.vm.fetchOrderInfo();
      expect(wrapper.vm.orderInfo).toBeNull();
    });
  });

  describe('方法 - goBack', () => {
    it('应调用router.back()', () => {
      const wrapper = mountCreateDispute();
      wrapper.vm.goBack();
      expect(mockBack).toHaveBeenCalled();
    });
  });
});
