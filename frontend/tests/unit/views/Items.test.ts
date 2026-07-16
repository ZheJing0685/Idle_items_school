// @ts-nocheck
import { describe, it, expect, vi, beforeEach, beforeAll } from 'vitest';
import { mount } from '@vue/test-utils';
import { setActivePinia, createPinia } from 'pinia';

// Mock route
const mockRoute = {
  query: {},
  params: {},
  path: '/items',
  name: 'Items',
};

vi.mock('vue-router', () => ({
  useRoute: () => mockRoute,
}));

// Mock category store
const mockCategoryFetchAll = vi.fn().mockResolvedValue(undefined);
vi.mock('@/store/category', () => ({
  useCategoryStore: () => ({
    categories: [],
    flatCategories: [],
    categoryTree: [],
    loaded: false,
    error: undefined,
    fetchAll: mockCategoryFetchAll,
    getCategoryIcon: vi.fn().mockReturnValue('📂'),
    getCategoryColorById: vi.fn().mockReturnValue('#1890ff'),
  }),
}));

// Mock store
const mockFetchItems = vi.fn();
const mockSearchItems = vi.fn();
vi.mock('@/store', () => ({
  useItemStore: () => ({
    items: [
      { id: 1, title: '测试物品1', price: 100, coverImage: '' },
      { id: 2, title: '测试物品2', price: 200, coverImage: '' },
    ],
    total: 2,
    searchResults: [],
    searchTotal: 0,
    fetchItems: mockFetchItems,
    searchItems: mockSearchItems,
  }),
}));

// Mock dict store
vi.mock('@/store/dict.js', () => ({
  useDictStore: () => ({
    getDictOptions: vi.fn().mockReturnValue([]),
    getDictLabel: vi.fn().mockReturnValue(''),
    preloadCommonDicts: vi.fn().mockResolvedValue(undefined),
  }),
}));

// Mock API
const mockGetCategoryTree = vi.fn();
vi.mock('@/api', () => ({
  default: {
    category: {
      getCategoryTree: mockGetCategoryTree,
    },
  },
}));

let Items: any;
beforeAll(async () => {
  const mod = await import('@/views/Items.vue');
  Items = mod.default;
});

beforeEach(() => {
  setActivePinia(createPinia());
  vi.clearAllMocks();
  mockFetchItems.mockResolvedValue(undefined);
  mockSearchItems.mockResolvedValue(undefined);
  mockGetCategoryTree.mockResolvedValue({
    code: 200,
    data: [],
  });
  mockRoute.query = {};
});

const mountItems = () => {
  return mount(Items, {
    global: {
      stubs: {
        'router-link': { template: '<a><slot /></a>' },
        'el-cascader': { template: '<div />', props: ['modelValue', 'options'] },
        'el-select': { template: '<div />', props: ['modelValue', 'placeholder'] },
        'el-option': { template: '<div />', props: ['label', 'value'] },
        'el-input': { template: '<input />', props: ['modelValue', 'placeholder'] },
        'el-button': { template: '<button><slot /></button>' },
        'el-pagination': { template: '<div />', props: ['currentPage', 'pageSize', 'total'] },
      },
    },
  });
};

describe('Items Component', () => {
  describe('组件渲染', () => {
    it('应该渲染物品页面', () => {
      const wrapper = mountItems();
      expect(wrapper.find('.items-page').exists()).toBe(true);
    });

    it('应该渲染搜索栏', () => {
      const wrapper = mountItems();
      expect(wrapper.find('.items-header').exists()).toBe(true);
    });

    it('应该渲染分类行', () => {
      const wrapper = mountItems();
      expect(wrapper.find('.category-row').exists()).toBe(true);
    });

    it('应该渲染排序栏', () => {
      const wrapper = mountItems();
      expect(wrapper.find('.items-toolbar').exists()).toBe(true);
    });

    it('应该渲染物品网格或空状态', () => {
      const wrapper = mountItems();
      const hasGrid = wrapper.find('.items-grid').exists();
      const hasEmpty = wrapper.find('.empty-state').exists();
      expect(hasGrid || hasEmpty).toBe(true);
    });
  });

  describe('组件状态', () => {
    it('应该有keyword状态', () => {
      const wrapper = mountItems();
      expect(wrapper.vm.keyword).toBeDefined();
      expect(typeof wrapper.vm.keyword).toBe('string');
    });

    it('应该有currentPage状态', () => {
      const wrapper = mountItems();
      expect(wrapper.vm.currentPage).toBeDefined();
      expect(typeof wrapper.vm.currentPage).toBe('number');
    });

    it('应该有pageSize状态', () => {
      const wrapper = mountItems();
      expect(wrapper.vm.pageSize).toBeDefined();
      expect(typeof wrapper.vm.pageSize).toBe('number');
    });

    it('应该有items数组', () => {
      const wrapper = mountItems();
      expect(wrapper.vm.items).toBeDefined();
      expect(Array.isArray(wrapper.vm.items)).toBe(true);
    });

    it('应该有total状态', () => {
      const wrapper = mountItems();
      expect(wrapper.vm.total).toBeDefined();
      expect(typeof wrapper.vm.total).toBe('number');
    });
  });

  describe('方法', () => {
    it('应该有loadItems方法', () => {
      const wrapper = mountItems();
      expect(typeof wrapper.vm.loadItems).toBe('function');
    });

    it('应该有handleSearch方法', () => {
      const wrapper = mountItems();
      expect(typeof wrapper.vm.handleSearch).toBe('function');
    });

    it('应该有handleFilter方法', () => {
      const wrapper = mountItems();
      expect(typeof wrapper.vm.handleFilter).toBe('function');
    });

    it('应该有handleSizeChange方法', () => {
      const wrapper = mountItems();
      expect(typeof wrapper.vm.handleSizeChange).toBe('function');
    });

    it('应该有handleCurrentChange方法', () => {
      const wrapper = mountItems();
      expect(typeof wrapper.vm.handleCurrentChange).toBe('function');
    });
  });

  // 注意：Items.vue 未定义 isNew/getDiscount/getConditionText/getDeliveryText/parseTags 等方法
  // 这些方法由各自组件通过 props 或独立的 utils 实现
  // 因此对应的辅助方法测试已移除
});
