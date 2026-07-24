// @ts-nocheck
import { describe, it, expect, vi, beforeEach, beforeAll } from 'vitest';
import { mount } from '@vue/test-utils';
import { setActivePinia, createPinia } from 'pinia';
import { ElMessage } from 'element-plus';

const mockRoute = {
  params: {},
  fullPath: '/admin/category-feedback',
  query: {},
  path: '/admin/category-feedback',
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
  ElMessage: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn(),
    info: vi.fn(),
  },
  ElDialog: { __esModule: true, default: { template: '<div><slot /></div>' } },
  ElPagination: { __esModule: true, default: '<div class="el-pagination"><slot /></div>' },
  ElTable: { __esModule: true, default: '<table class="el-table"><slot /></table>' },
  ElTableColumn: { __esModule: true, default: '<col><slot />' },
  ElButton: { __esModule: true, default: '<button><slot /></button>' },
  ElRadioGroup: { __esModule: true, default: '<div class="el-radio-group"><slot /></div>' },
  ElRadio: { __esModule: true, default: '<label class="el-radio"><slot /></label>' },
  ElInput: { __esModule: true, default: '<textarea><slot /></textarea>' },
}));

const mockGetDictLabel = vi.fn((typeCode, itemValue) => itemValue);
const mockPreloadCommonDicts = vi.fn().mockResolvedValue(undefined);

vi.mock('@/store/dict.js', () => ({
  useDictStore: () => ({
    dicts: {},
    loading: false,
    error: null,
    getDictLabel: mockGetDictLabel,
    preloadCommonDicts: mockPreloadCommonDicts,
  }),
}));

const mockGetFeedbacks = vi.fn().mockResolvedValue({
  code: 200,
  data: { content: [], totalElements: 0 },
});

const mockReviewFeedback = vi.fn().mockResolvedValue({
  code: 200,
  message: '操作成功',
});

vi.mock('@/api', () => ({
  default: {
    admin: {
      categories: {
        getFeedbacks: mockGetFeedbacks,
        reviewFeedback: mockReviewFeedback,
      },
    },
  },
}));

let CategoryFeedbackManagement;
beforeAll(async () => {
  const mod = await import('@/views/admin/CategoryFeedbackManagement.vue');
  CategoryFeedbackManagement = mod.default;
});

beforeEach(() => {
  setActivePinia(createPinia());
  vi.clearAllMocks();
  mockGetDictLabel.mockImplementation((typeCode, itemValue) => itemValue);
  mockGetFeedbacks.mockResolvedValue({
    code: 200,
    data: { content: [], totalElements: 0 },
  });
  mockReviewFeedback.mockResolvedValue({
    code: 200,
    message: '操作成功',
  });
  document.body.innerHTML = '';
});

const mountComponent = (options = {}) => {
  return mount(CategoryFeedbackManagement, {
    global: {
      stubs: {
        'router-link': '<a><slot /></a>',
        teleport: '<div><slot /></div>',
        'el-dialog': false,
        'el-pagination': '<div class="el-pagination"><slot /></div>',
        'el-table': '<table class="el-table"><slot /></table>',
        'el-table-column': '<col><slot />',
        'el-button': '<button><slot /></button>',
        'el-radio-group': '<div class="el-radio-group"><slot /></div>',
        'el-radio': '<label class="el-radio"><slot /></label>',
        'el-input': '<textarea><slot /></textarea>',
      },
      ...options.global,
    },
  });
};

describe('CategoryFeedbackManagement Component', () => {
  describe('getTypeLabel', () => {
    it('should return dict label for INVALID_CATEGORY type', async () => {
      const wrapper = mountComponent();
      mockGetDictLabel.mockReturnValue('分类无效');
      const result = wrapper.vm.getTypeLabel('INVALID_CATEGORY');
      expect(mockGetDictLabel).toHaveBeenCalledWith('CATEGORY_FEEDBACK_TYPE', 'INVALID');
      expect(result).toBe('分类无效');
    });

    it('should return dict label for MISSING_CATEGORY type', async () => {
      const wrapper = mountComponent();
      mockGetDictLabel.mockReturnValue('缺少分类');
      const result = wrapper.vm.getTypeLabel('MISSING_CATEGORY');
      expect(mockGetDictLabel).toHaveBeenCalledWith('CATEGORY_FEEDBACK_TYPE', 'MISSING');
      expect(result).toBe('缺少分类');
    });

    it('should return dict label for OTHER type', async () => {
      const wrapper = mountComponent();
      mockGetDictLabel.mockReturnValue('其他');
      const result = wrapper.vm.getTypeLabel('OTHER');
      expect(mockGetDictLabel).toHaveBeenCalledWith('CATEGORY_FEEDBACK_TYPE', 'OTHER');
      expect(result).toBe('其他');
    });

    it('should pass through unknown type values', async () => {
      const wrapper = mountComponent();
      mockGetDictLabel.mockReturnValue('UNKNOWN_TYPE');
      const result = wrapper.vm.getTypeLabel('UNKNOWN_TYPE');
      expect(mockGetDictLabel).toHaveBeenCalledWith('CATEGORY_FEEDBACK_TYPE', 'UNKNOWN_TYPE');
      expect(result).toBe('UNKNOWN_TYPE');
    });
  });

  describe('getTypeClass', () => {
    it('should return type-warning for INVALID_CATEGORY', () => {
      const wrapper = mountComponent();
      expect(wrapper.vm.getTypeClass('INVALID_CATEGORY')).toBe('type-warning');
    });

    it('should return type-info for MISSING_CATEGORY', () => {
      const wrapper = mountComponent();
      expect(wrapper.vm.getTypeClass('MISSING_CATEGORY')).toBe('type-info');
    });

    it('should return type-default for OTHER', () => {
      const wrapper = mountComponent();
      expect(wrapper.vm.getTypeClass('OTHER')).toBe('type-default');
    });

    it('should return type-default for unknown types', () => {
      const wrapper = mountComponent();
      expect(wrapper.vm.getTypeClass('UNKNOWN')).toBe('type-default');
    });
  });

  describe('getStatusLabel', () => {
    it('should return dict label for PENDING status', async () => {
      const wrapper = mountComponent();
      mockGetDictLabel.mockReturnValue('待处理');
      const result = wrapper.vm.getStatusLabel('PENDING');
      expect(mockGetDictLabel).toHaveBeenCalledWith('VERIFICATION_STATUS', 'PENDING');
      expect(result).toBe('待处理');
    });

    it('should return dict label for ACCEPTED status', async () => {
      const wrapper = mountComponent();
      mockGetDictLabel.mockReturnValue('已采纳');
      const result = wrapper.vm.getStatusLabel('ACCEPTED');
      expect(mockGetDictLabel).toHaveBeenCalledWith('VERIFICATION_STATUS', 'ACCEPTED');
      expect(result).toBe('已采纳');
    });

    it('should return dict label for REJECTED status', async () => {
      const wrapper = mountComponent();
      mockGetDictLabel.mockReturnValue('已拒绝');
      const result = wrapper.vm.getStatusLabel('REJECTED');
      expect(mockGetDictLabel).toHaveBeenCalledWith('VERIFICATION_STATUS', 'REJECTED');
      expect(result).toBe('已拒绝');
    });

    it('should pass through unknown status values', async () => {
      const wrapper = mountComponent();
      mockGetDictLabel.mockReturnValue('UNKNOWN_STATUS');
      const result = wrapper.vm.getStatusLabel('UNKNOWN_STATUS');
      expect(mockGetDictLabel).toHaveBeenCalledWith('VERIFICATION_STATUS', 'UNKNOWN_STATUS');
      expect(result).toBe('UNKNOWN_STATUS');
    });
  });

  describe('getStatusClass', () => {
    it('should return status-pending for PENDING', () => {
      const wrapper = mountComponent();
      expect(wrapper.vm.getStatusClass('PENDING')).toBe('status-pending');
    });

    it('should return status-accepted for ACCEPTED', () => {
      const wrapper = mountComponent();
      expect(wrapper.vm.getStatusClass('ACCEPTED')).toBe('status-accepted');
    });

    it('should return status-rejected for REJECTED', () => {
      const wrapper = mountComponent();
      expect(wrapper.vm.getStatusClass('REJECTED')).toBe('status-rejected');
    });

    it('should return empty string for unknown status', () => {
      const wrapper = mountComponent();
      expect(wrapper.vm.getStatusClass('UNKNOWN')).toBe('');
    });
  });

  describe('truncateText', () => {
    it('should truncate text longer than specified length', () => {
      const wrapper = mountComponent();
      const result = wrapper.vm.truncateText('这是一段非常长的文本内容，超过了限制长度', 10);
      expect(result).toBe('这是一段非常长的文本...');
    });

    it('should return full text when shorter than specified length', () => {
      const wrapper = mountComponent();
      const result = wrapper.vm.truncateText('短文本', 10);
      expect(result).toBe('短文本');
    });

    it('should return empty string for null input', () => {
      const wrapper = mountComponent();
      expect(wrapper.vm.truncateText(null, 10)).toBe('');
    });

    it('should return empty string for undefined input', () => {
      const wrapper = mountComponent();
      expect(wrapper.vm.truncateText(undefined, 10)).toBe('');
    });

    it('should return empty string for empty string input', () => {
      const wrapper = mountComponent();
      expect(wrapper.vm.truncateText('', 10)).toBe('');
    });

    it('should truncate text at exact length boundary', () => {
      const wrapper = mountComponent();
      const result = wrapper.vm.truncateText('123456789012345', 15);
      expect(result).toBe('123456789012345');
    });

    it('should truncate text just over length boundary', () => {
      const wrapper = mountComponent();
      const result = wrapper.vm.truncateText('1234567890123456', 15);
      expect(result).toBe('123456789012345...');
    });
  });

  describe('formatDate', () => {
    it('should format a valid ISO date string', async () => {
      const wrapper = mountComponent();
      const result = wrapper.vm.formatDate('2024-01-15T10:30:00.000Z');
      expect(result).toContain('2024');
      expect(result).toContain('01');
      expect(result).toContain('15');
    });

    it('should return dash for null input', () => {
      const wrapper = mountComponent();
      expect(wrapper.vm.formatDate(null)).toBe('-');
    });

    it('should return dash for undefined input', () => {
      const wrapper = mountComponent();
      expect(wrapper.vm.formatDate(undefined)).toBe('-');
    });

    it('should return dash for empty string input', () => {
      const wrapper = mountComponent();
      expect(wrapper.vm.formatDate('')).toBe('-');
    });

    it('should format date with hour and minute', async () => {
      const wrapper = mountComponent();
      const result = wrapper.vm.formatDate('2024-06-20T14:25:00.000Z');
      expect(result).toMatch(/\d{2}:\d{2}/);
    });
  });

  describe('fetchFeedbacks', () => {
    it('should fetch feedbacks with pagination params', async () => {
      const wrapper = mountComponent();
      await wrapper.vm.$nextTick();
      await new Promise(r => setTimeout(r, 50));

      mockGetFeedbacks.mockResolvedValue({
        code: 200,
        data: { content: [{ id: 1, username: 'test' }], totalElements: 1 },
      });

      await wrapper.vm.fetchFeedbacks();
      expect(mockGetFeedbacks).toHaveBeenCalledWith({ page: 1, size: 20 });
      expect(wrapper.vm.feedbacks.length).toBe(1);
      expect(wrapper.vm.total).toBe(1);
    });

    it('should filter by currentStatus when set', async () => {
      const wrapper = mountComponent();
      await wrapper.vm.$nextTick();
      await new Promise(r => setTimeout(r, 50));

      wrapper.vm.currentStatus = 'PENDING';
      mockGetFeedbacks.mockResolvedValue({
        code: 200,
        data: { content: [], totalElements: 0 },
      });

      await wrapper.vm.fetchFeedbacks();
      expect(mockGetFeedbacks).toHaveBeenCalledWith({
        page: 1,
        size: 20,
        status: 'PENDING',
      });
    });

    it('should show error message on failure', async () => {
      const wrapper = mountComponent();
      await wrapper.vm.$nextTick();
      await new Promise(r => setTimeout(r, 50));

      mockGetFeedbacks.mockRejectedValue(new Error('Network Error'));
      await wrapper.vm.fetchFeedbacks();
      expect(ElMessage.error).toHaveBeenCalledWith('网络错误');
    });

    it('should not update when response code is not 200', async () => {
      const wrapper = mountComponent();
      await wrapper.vm.$nextTick();
      await new Promise(r => setTimeout(r, 50));

      mockGetFeedbacks.mockResolvedValue({
        code: 500,
        message: '服务器错误',
      });

      await wrapper.vm.fetchFeedbacks();
      expect(wrapper.vm.feedbacks.length).toBe(0);
      expect(wrapper.vm.total).toBe(0);
    });

    it('should handle empty content array', async () => {
      const wrapper = mountComponent();
      await wrapper.vm.$nextTick();
      await new Promise(r => setTimeout(r, 50));

      mockGetFeedbacks.mockResolvedValue({
        code: 200,
        data: { content: [], totalElements: 0 },
      });

      await wrapper.vm.fetchFeedbacks();
      expect(wrapper.vm.feedbacks).toEqual([]);
      expect(wrapper.vm.total).toBe(0);
    });
  });

  describe('handleStatusChange', () => {
    it('should reset page to 1 and refetch feedbacks', async () => {
      const wrapper = mountComponent();
      await wrapper.vm.$nextTick();
      await new Promise(r => setTimeout(r, 50));

      wrapper.vm.page = 5;
      mockGetFeedbacks.mockResolvedValue({
        code: 200,
        data: { content: [], totalElements: 0 },
      });

      await wrapper.vm.handleStatusChange('ACCEPTED');
      expect(wrapper.vm.currentStatus).toBe('ACCEPTED');
      expect(wrapper.vm.page).toBe(1);
      expect(mockGetFeedbacks).toHaveBeenCalled();
    });

    it('should switch to empty status for showing all', async () => {
      const wrapper = mountComponent();
      await wrapper.vm.$nextTick();
      await new Promise(r => setTimeout(r, 50));

      wrapper.vm.currentStatus = 'PENDING';
      mockGetFeedbacks.mockResolvedValue({
        code: 200,
        data: { content: [], totalElements: 0 },
      });

      await wrapper.vm.handleStatusChange('');
      expect(wrapper.vm.currentStatus).toBe('');
      expect(mockGetFeedbacks).toHaveBeenCalled();
    });
  });

  describe('handleSizeChange', () => {
    it('should reset page to 1 and refetch feedbacks', async () => {
      const wrapper = mountComponent();
      await wrapper.vm.$nextTick();
      await new Promise(r => setTimeout(r, 50));

      wrapper.vm.page = 3;
      mockGetFeedbacks.mockResolvedValue({
        code: 200,
        data: { content: [], totalElements: 0 },
      });

      await wrapper.vm.handleSizeChange();
      expect(wrapper.vm.page).toBe(1);
      expect(mockGetFeedbacks).toHaveBeenCalled();
    });

    it('should use current pageSize value', async () => {
      const wrapper = mountComponent();
      await wrapper.vm.$nextTick();
      await new Promise(r => setTimeout(r, 50));

      wrapper.vm.pageSize = 50;
      mockGetFeedbacks.mockResolvedValue({
        code: 200,
        data: { content: [], totalElements: 0 },
      });

      await wrapper.vm.handleSizeChange();
      expect(mockGetFeedbacks).toHaveBeenCalledWith({ page: 1, size: 50 });
    });
  });

  describe('handleReview', () => {
    it('should open dialog with feedback data', async () => {
      const wrapper = mountComponent();
      await wrapper.vm.$nextTick();
      await new Promise(r => setTimeout(r, 50));

      const feedback = {
        id: 1,
        type: 'INVALID_CATEGORY',
        status: 'PENDING',
        categoryName: '书籍',
        description: '这个分类不对',
        adminReply: null,
        username: 'user123',
        userId: 42,
        createdAt: '2024-01-15T10:30:00.000Z',
      };

      wrapper.vm.handleReview(feedback);
      expect(wrapper.vm.reviewDialogVisible).toBe(true);
      expect(wrapper.vm.currentFeedback.id).toBe(1);
      expect(wrapper.vm.currentFeedback.categoryName).toBe('书籍');
    });

    it('should reset review form to defaults', async () => {
      const wrapper = mountComponent();
      await wrapper.vm.$nextTick();
      await new Promise(r => setTimeout(r, 50));

      const feedback = { id: 1, type: 'OTHER', status: 'PENDING' };
      wrapper.vm.handleReview(feedback);
      expect(wrapper.vm.reviewForm.status).toBe('ACCEPTED');
      expect(wrapper.vm.reviewForm.adminReply).toBe('');
    });

    it('should handle feedback without optional fields', async () => {
      const wrapper = mountComponent();
      await wrapper.vm.$nextTick();
      await new Promise(r => setTimeout(r, 50));

      const feedback = { id: 2, type: 'MISSING_CATEGORY', status: 'PENDING' };
      wrapper.vm.handleReview(feedback);
      expect(wrapper.vm.currentFeedback.id).toBe(2);
      expect(wrapper.vm.currentFeedback.categoryName).toBeUndefined();
    });
  });

  describe('handleSubmitReview', () => {
    it('should submit accepted review successfully', async () => {
      const wrapper = mountComponent();
      await wrapper.vm.$nextTick();
      await new Promise(r => setTimeout(r, 50));

      const feedback = { id: 1, type: 'INVALID_CATEGORY', status: 'PENDING' };
      wrapper.vm.handleReview(feedback);
      wrapper.vm.reviewForm.status = 'ACCEPTED';
      wrapper.vm.reviewForm.adminReply = '已确认分类需要修改';

      mockReviewFeedback.mockResolvedValue({ code: 200, message: '操作成功' });
      mockGetFeedbacks.mockResolvedValue({
        code: 200,
        data: { content: [], totalElements: 0 },
      });

      await wrapper.vm.handleSubmitReview();
      expect(mockReviewFeedback).toHaveBeenCalledWith(1, {
        status: 'ACCEPTED',
        reply: '已确认分类需要修改',
      });
      expect(ElMessage.success).toHaveBeenCalledWith('审核完成');
      expect(wrapper.vm.reviewDialogVisible).toBe(false);
    });

    it('should submit rejected review successfully', async () => {
      const wrapper = mountComponent();
      await wrapper.vm.$nextTick();
      await new Promise(r => setTimeout(r, 50));

      const feedback = { id: 2, type: 'OTHER', status: 'PENDING' };
      wrapper.vm.handleReview(feedback);
      wrapper.vm.reviewForm.status = 'REJECTED';
      wrapper.vm.reviewForm.adminReply = '不符合采纳条件';

      mockReviewFeedback.mockResolvedValue({ code: 200, message: '操作成功' });
      mockGetFeedbacks.mockResolvedValue({
        code: 200,
        data: { content: [], totalElements: 0 },
      });

      await wrapper.vm.handleSubmitReview();
      expect(mockReviewFeedback).toHaveBeenCalledWith(2, {
        status: 'REJECTED',
        reply: '不符合采纳条件',
      });
      expect(ElMessage.success).toHaveBeenCalledWith('审核完成');
    });

    it('should submit review with empty reply', async () => {
      const wrapper = mountComponent();
      await wrapper.vm.$nextTick();
      await new Promise(r => setTimeout(r, 50));

      const feedback = { id: 3, type: 'MISSING_CATEGORY', status: 'PENDING' };
      wrapper.vm.handleReview(feedback);
      wrapper.vm.reviewForm.status = 'ACCEPTED';
      wrapper.vm.reviewForm.adminReply = '';

      mockReviewFeedback.mockResolvedValue({ code: 200, message: '操作成功' });
      mockGetFeedbacks.mockResolvedValue({
        code: 200,
        data: { content: [], totalElements: 0 },
      });

      await wrapper.vm.handleSubmitReview();
      expect(mockReviewFeedback).toHaveBeenCalledWith(3, {
        status: 'ACCEPTED',
        reply: '',
      });
      expect(ElMessage.success).toHaveBeenCalledWith('审核完成');
    });

    it('should show error message on API failure', async () => {
      const wrapper = mountComponent();
      await wrapper.vm.$nextTick();
      await new Promise(r => setTimeout(r, 50));

      const feedback = { id: 4, type: 'INVALID_CATEGORY', status: 'PENDING' };
      wrapper.vm.handleReview(feedback);

      mockReviewFeedback.mockResolvedValue({
        code: 400,
        message: '审核失败',
      });

      await wrapper.vm.handleSubmitReview();
      expect(ElMessage.error).toHaveBeenCalledWith('审核失败');
      expect(wrapper.vm.reviewDialogVisible).toBe(true);
    });

    it('should show network error on exception', async () => {
      const wrapper = mountComponent();
      await wrapper.vm.$nextTick();
      await new Promise(r => setTimeout(r, 50));

      const feedback = { id: 5, type: 'OTHER', status: 'PENDING' };
      wrapper.vm.handleReview(feedback);

      mockReviewFeedback.mockRejectedValue(new Error('Network Error'));

      await wrapper.vm.handleSubmitReview();
      expect(ElMessage.error).toHaveBeenCalledWith('网络错误');
      expect(wrapper.vm.reviewDialogVisible).toBe(true);
    });

    it('should set submitting state during submission', async () => {
      let resolveFn;
      const promise = new Promise(resolve => { resolveFn = resolve; });
      mockReviewFeedback.mockReturnValue(promise);

      const wrapper = mountComponent();
      await wrapper.vm.$nextTick();
      await new Promise(r => setTimeout(r, 50));

      const feedback = { id: 6, type: 'INVALID_CATEGORY', status: 'PENDING' };
      wrapper.vm.handleReview(feedback);

      const submitPromise = wrapper.vm.handleSubmitReview();
      expect(wrapper.vm.submitting).toBe(true);

      resolveFn({ code: 200, message: '操作成功' });
      await submitPromise;
      expect(wrapper.vm.submitting).toBe(false);
    });
  });

  describe('statusTabs computed', () => {
    it('should have four status tabs', async () => {
      const wrapper = mountComponent();
      await wrapper.vm.$nextTick();
      await new Promise(r => setTimeout(r, 50));
      expect(wrapper.vm.statusTabs.length).toBe(4);
    });

    it('should include all status tab labels', async () => {
      const wrapper = mountComponent();
      await wrapper.vm.$nextTick();
      await new Promise(r => setTimeout(r, 50));
      const labels = wrapper.vm.statusTabs.map(t => t.label);
      expect(labels).toContain('全部');
      expect(labels).toContain('待处理');
      expect(labels).toContain('已采纳');
      expect(labels).toContain('已拒绝');
    });

    it('should include correct tab values', async () => {
      const wrapper = mountComponent();
      await wrapper.vm.$nextTick();
      await new Promise(r => setTimeout(r, 50));
      const values = wrapper.vm.statusTabs.map(t => t.value);
      expect(values).toContain('');
      expect(values).toContain('PENDING');
      expect(values).toContain('ACCEPTED');
      expect(values).toContain('REJECTED');
    });
  });

  describe('initial state', () => {
    it('should have correct initial reactive state', async () => {
      const wrapper = mountComponent();
      await wrapper.vm.$nextTick();
      await new Promise(r => setTimeout(r, 50));
      expect(wrapper.vm.currentStatus).toBe('');
      expect(wrapper.vm.feedbacks).toEqual([]);
      expect(wrapper.vm.page).toBe(1);
      expect(wrapper.vm.pageSize).toBe(20);
      expect(wrapper.vm.total).toBe(0);
      expect(wrapper.vm.reviewDialogVisible).toBe(false);
      expect(wrapper.vm.submitting).toBe(false);
    });

    it('should have reviewForm with default values', async () => {
      const wrapper = mountComponent();
      await wrapper.vm.$nextTick();
      await new Promise(r => setTimeout(r, 50));
      expect(wrapper.vm.reviewForm.status).toBe('ACCEPTED');
      expect(wrapper.vm.reviewForm.adminReply).toBe('');
    });

    it('should render page intro section', async () => {
      const wrapper = mountComponent();
      await wrapper.vm.$nextTick();
      await new Promise(r => setTimeout(r, 50));
      expect(wrapper.text()).toContain('分类反馈管理');
    });

    it('should render feedback list card title', async () => {
      const wrapper = mountComponent();
      await wrapper.vm.$nextTick();
      await new Promise(r => setTimeout(r, 50));
      expect(wrapper.text()).toContain('反馈列表');
    });

    it('should render status tabs', async () => {
      const wrapper = mountComponent();
      await wrapper.vm.$nextTick();
      await new Promise(r => setTimeout(r, 50));
      const text = wrapper.text();
      expect(text).toContain('全部');
      expect(text).toContain('待处理');
      expect(text).toContain('已采纳');
      expect(text).toContain('已拒绝');
    });
  });
});
