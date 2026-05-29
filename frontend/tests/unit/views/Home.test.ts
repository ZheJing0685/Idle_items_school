import { describe, it, expect, vi, beforeEach, beforeAll } from 'vitest';
import { mount } from '@vue/test-utils';
import { setActivePinia, createPinia } from 'pinia';

// Mock API
const { mockGetCategoryTree } = vi.hoisted(() => ({
  mockGetCategoryTree: vi.fn(),
}));

vi.mock('@/api', () => ({
  default: {
    category: {
      getCategoryTree: mockGetCategoryTree,
    },
  },
}));

let Home: any;
beforeAll(async () => {
  const mod = await import('@/views/Home.vue');
  Home = mod.default;
});

beforeEach(() => {
  setActivePinia(createPinia());
  vi.clearAllMocks();
  mockGetCategoryTree.mockReset();
  mockGetCategoryTree.mockResolvedValue({ data: [] });
});

const mountHome = () => {
  return mount(Home, {
    global: {
      stubs: {
        'router-link': { template: '<a><slot /></a>' },
      },
    },
  });
};

describe('Home Component (TypeScript)', () => {
  describe('组件渲染', () => {
    it('should render home page', () => {
      const wrapper = mountHome();
      expect(wrapper.text().length).toBeGreaterThan(0);
    });

    it('should render hero section', () => {
      const wrapper = mountHome();
      expect(wrapper.text()).toContain('闲置不闲置');
    });

    it('should render hero badge', () => {
      const wrapper = mountHome();
      expect(wrapper.text()).toContain('校园绿色交易平台');
    });

    it('should render hero actions', () => {
      const wrapper = mountHome();
      expect(wrapper.text()).toContain('探索好物');
      expect(wrapper.text()).toContain('发布闲置');
    });

    it('should render stats', () => {
      const wrapper = mountHome();
      expect(wrapper.text()).toContain('12,847');
      expect(wrapper.text()).toContain('98.6%');
    });
  });

  describe('分类部分', () => {
    it('should render categories section', () => {
      const wrapper = mountHome();
      expect(wrapper.text()).toContain('分类浏览');
    });

    it('should have categories ref', () => {
      const wrapper = mountHome();
      expect(wrapper.vm.categories).toBeDefined();
      expect(Array.isArray(wrapper.vm.categories)).toBe(true);
    });
  });

  describe('热门物品', () => {
    it('should have featuredItems ref', () => {
      const wrapper = mountHome();
      expect(wrapper.vm.featuredItems).toBeDefined();
      expect(Array.isArray(wrapper.vm.featuredItems)).toBe(true);
    });
  });

  describe('加载状态', () => {
    it('should have loading state', () => {
      const wrapper = mountHome();
      expect(wrapper.vm.loading).toBeDefined();
      expect(typeof wrapper.vm.loading.categories).toBe('boolean');
      expect(typeof wrapper.vm.loading.items).toBe('boolean');
    });
  });

  describe('方法', () => {
    it('should have fetchCategories method', () => {
      const wrapper = mountHome();
      expect(typeof wrapper.vm.fetchCategories).toBe('function');
    });

    it('should have getCategoryColor method', () => {
      const wrapper = mountHome();
      expect(typeof wrapper.vm.getCategoryColor).toBe('function');
    });

    it('should have getCategoryIcon method', () => {
      const wrapper = mountHome();
      expect(typeof wrapper.vm.getCategoryIcon).toBe('function');
    });
  });

  describe('导航链接', () => {
    it('should have items link', () => {
      const wrapper = mountHome();
      expect(wrapper.text()).toContain('探索好物');
    });

    it('should have publish link', () => {
      const wrapper = mountHome();
      expect(wrapper.text()).toContain('发布闲置');
    });
  });

  describe('辅助方法', () => {
    it('should return color for category id', () => {
      const wrapper = mountHome();
      const color = wrapper.vm.getCategoryColor(1);
      expect(color).toBeDefined();
      expect(typeof color).toBe('string');
    });

    it('should return icon for category id', () => {
      const wrapper = mountHome();
      const icon = wrapper.vm.getCategoryIcon(1);
      expect(icon).toBeDefined();
    });
  });
});
