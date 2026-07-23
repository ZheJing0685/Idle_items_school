// @ts-nocheck
import { describe, it, expect, vi, beforeEach, beforeAll } from 'vitest';
import { mount } from '@vue/test-utils';
import { setActivePinia, createPinia } from 'pinia';

// Mock route
const mockRoute = {
  query: {},
  path: '/publish',
};

const mockPush = vi.fn();
const mockBack = vi.fn();

vi.mock('vue-router', () => ({
  useRoute: () => mockRoute,
  useRouter: () => ({
    push: mockPush,
    back: mockBack,
  }),
}));

// Mock Element Plus
vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn(),
  },
  ElForm: {
    template: '<form><slot /></form>',
  },
}));

// Mock API
const mockGetCategoryTree = vi.fn();
const mockCreateItem = vi.fn();
const mockUpdateItem = vi.fn();
const mockGetItem = vi.fn();
const mockUploadImage = vi.fn();

vi.mock('@/api', () => ({
  default: {
    category: {
      getCategoryTree: mockGetCategoryTree,
    },
    item: {
      createItem: mockCreateItem,
      updateItem: mockUpdateItem,
      getItem: mockGetItem,
      uploadImage: mockUploadImage,
    },
  },
}));

// Mock validator
vi.mock('@/utils/validator', () => ({
  itemRules: {},
}));

// Mock dict store
vi.mock('@/store/dict.js', () => ({
  useDictStore: () => ({
    getDictOptions: vi.fn().mockImplementation((type) => {
      const options: Record<string, any[]> = {
        ITEM_CONDITION: [
          { value: 'NEW', label: '全新' },
          { value: 'LIKE_NEW', label: '九成新' },
        ],
        DELIVERY_METHOD: [
          { value: 'LOCAL_DELIVERY', label: '自提' },
          { value: 'EXPRESS', label: '快递' },
        ],
        CONTACT_TYPE: [
          { value: '1', label: '平台内消息' },
          { value: '2', label: '微信' },
          { value: '3', label: 'QQ' },
        ],
      };
      return options[type] || [];
    }),
    preloadCommonDicts: vi.fn().mockResolvedValue(undefined),
  }),
}));

let Publish: any;
beforeAll(async () => {
  const mod = await import('@/views/Publish.vue');
  Publish = mod.default;
});

beforeEach(() => {
  setActivePinia(createPinia());
  vi.clearAllMocks();
  localStorage.clear();
  mockRoute.query = {};
  
  mockGetCategoryTree.mockResolvedValue({
    code: 200,
    data: [
      { id: 1, name: '电子产品', children: [] },
      { id: 2, name: '书籍', children: [] },
    ],
  });

  mockCreateItem.mockResolvedValue({
    code: 200,
    message: '发布成功',
  });

  mockUpdateItem.mockResolvedValue({
    code: 200,
    message: '修改成功',
  });

  mockGetItem.mockResolvedValue({
    code: 200,
    data: {
      id: 1,
      title: '测试物品',
      description: '测试描述',
      price: 100,
      originalPrice: 200,
      categoryId: 1,
      condition: 'NEW',
      deliveryMethod: 'EXPRESS',
      contactType: '1',
      isBargainAllowed: true,
      location: '校园南门',
      brand: '测试品牌',
      tags: ['标签1', '标签2'],
      contactName: '张三',
      contactPhone: '13800138000',
      images: ['img1.jpg'],
    },
  });

  mockUploadImage.mockResolvedValue({
    code: 200,
    data: { url: 'uploaded.jpg' },
  });
});

const mountPublish = () => {
  return mount(Publish, {
    global: {
      stubs: {
        'router-link': { template: '<a><slot /></a>' },
        'el-form': { template: '<form><slot /></form>', props: ['model', 'rules', 'ref'] },
        'el-form-item': { template: '<div><label>{{ label }}</label><slot /></div>', props: ['label', 'prop'] },
        'el-input': { template: '<input />', props: ['modelValue', 'placeholder', 'type', 'rows', 'disabled'] },
        'el-select': { template: '<div />', props: ['modelValue', 'placeholder'] },
        'el-option': { template: '<div />', props: ['label', 'value'] },
        'el-cascader': { template: '<div />', props: ['modelValue', 'options'] },
        'el-switch': { template: '<input type="checkbox" />', props: ['modelValue'] },
        'el-date-picker': { template: '<input type="date" />', props: ['modelValue', 'type'] },
        'el-tag': { template: '<span><slot /></span>', props: ['closable'] },
        'el-button': { template: '<button><slot /></button>', props: ['type', 'size', 'loading'] },
      },
    },
  });
};

describe('Publish Component', () => {
  describe('组件渲染', () => {
    it('应该渲染发布页面', () => {
      const wrapper = mountPublish();
      expect(wrapper.find('.publish-page').exists()).toBe(true);
    });

    it('应该渲染页面标题', () => {
      const wrapper = mountPublish();
      expect(wrapper.text()).toContain('发布闲置');
    });

    it('应该渲染页面副标题', () => {
      const wrapper = mountPublish();
      expect(wrapper.text()).toContain('让你的闲置物品找到新主人');
    });

    it('应该渲染表单区域', () => {
      const wrapper = mountPublish();
      expect(wrapper.find('.publish-form').exists()).toBe(true);
    });
  });

  describe('表单字段', () => {
    it('应该有表单引用', () => {
      const wrapper = mountPublish();
      expect(wrapper.vm.formRef).toBeDefined();
    });

    it('应该有表单模型数据', () => {
      const wrapper = mountPublish();
      expect(wrapper.vm.form).toHaveProperty('title');
      expect(wrapper.vm.form).toHaveProperty('categoryId');
      expect(wrapper.vm.form).toHaveProperty('condition');
      expect(wrapper.vm.form).toHaveProperty('price');
      expect(wrapper.vm.form).toHaveProperty('location');
      expect(wrapper.vm.form).toHaveProperty('description');
      expect(wrapper.vm.form).toHaveProperty('images');
    });

    it('应该有表单验证规则', () => {
      const wrapper = mountPublish();
      expect(wrapper.vm.rules).toBeDefined();
      expect(wrapper.vm.rules).toHaveProperty('title');
      expect(wrapper.vm.rules).toHaveProperty('price');
      expect(wrapper.vm.rules).toHaveProperty('categoryId');
      expect(wrapper.vm.rules).toHaveProperty('phone');
    });

    it('应该包含交易地点区域', () => {
      const wrapper = mountPublish();
      expect(wrapper.text()).toContain('交易地点');
    });
  });

  describe('组件状态', () => {
    it('应该有submitting状态', () => {
      const wrapper = mountPublish();
      expect(wrapper.vm.submitting).toBeDefined();
      expect(typeof wrapper.vm.submitting).toBe('boolean');
    });

    it('应该有form数据', () => {
      const wrapper = mountPublish();
      expect(wrapper.vm.form).toBeDefined();
      expect(wrapper.vm.form).toHaveProperty('title');
      expect(wrapper.vm.form).toHaveProperty('description');
      expect(wrapper.vm.form).toHaveProperty('price');
      expect(wrapper.vm.form).toHaveProperty('categoryId');
      expect(wrapper.vm.form).toHaveProperty('condition');
      expect(wrapper.vm.form).toHaveProperty('images');
    });

    it('应该有isEdit计算属性', () => {
      const wrapper = mountPublish();
      expect(wrapper.vm.isEdit).toBeDefined();
      expect(typeof wrapper.vm.isEdit).toBe('boolean');
    });
  });

  describe('方法', () => {
    it('应该有handleSubmit方法', () => {
      const wrapper = mountPublish();
      expect(typeof wrapper.vm.handleSubmit).toBe('function');
    });

    it('应该有triggerUpload方法', () => {
      const wrapper = mountPublish();
      expect(typeof wrapper.vm.triggerUpload).toBe('function');
    });

    it('应该有handleFileChange方法', () => {
      const wrapper = mountPublish();
      expect(typeof wrapper.vm.handleFileChange).toBe('function');
    });

    it('应该有removeImage方法', () => {
      const wrapper = mountPublish();
      expect(typeof wrapper.vm.removeImage).toBe('function');
    });
  });

  describe('图片功能', () => {
    it('removeImage应该删除指定图片', async () => {
      const wrapper = mountPublish();
      wrapper.vm.form.images = ['img1.jpg', 'img2.jpg'];
      wrapper.vm.removeImage(0);
      expect(wrapper.vm.form.images).toEqual(['img2.jpg']);
    });
  });

  describe('编辑模式', () => {
    it('编辑模式下页面标题应该显示编辑物品', async () => {
      mockRoute.query = { edit: '1' };
      const wrapper = mountPublish();
      expect(wrapper.text()).toContain('编辑物品');
    });

    it('非编辑模式下页面标题应该显示发布闲置', async () => {
      mockRoute.query = {};
      const wrapper = mountPublish();
      expect(wrapper.text()).toContain('发布闲置');
    });
  });
});
