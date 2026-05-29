import { describe, it, expect, vi, beforeAll } from 'vitest';
import { mount } from '@vue/test-utils';
import { setActivePinia, createPinia } from 'pinia';

let NotFound: any;
beforeAll(async () => {
  const mod = await import('@/views/NotFound.vue');
  NotFound = mod.default;
});

const mountNotFound = () => {
  return mount(NotFound, {
    global: {
      stubs: {
        'router-link': { template: '<a><slot /></a>' },
      },
    },
  });
};

describe('NotFound Component (TypeScript)', () => {
  describe('组件渲染', () => {
    it('should render 404 page', () => {
      setActivePinia(createPinia());
      const wrapper = mountNotFound();
      expect(wrapper.text().length).toBeGreaterThan(0);
    });

    it('should render 404 text', () => {
      setActivePinia(createPinia());
      const wrapper = mountNotFound();
      expect(wrapper.text()).toContain('404');
    });

    it('should render page not found message', () => {
      setActivePinia(createPinia());
      const wrapper = mountNotFound();
      expect(wrapper.text()).toContain('页面不存在');
    });

    it('should render description', () => {
      setActivePinia(createPinia());
      const wrapper = mountNotFound();
      expect(wrapper.text()).toContain('您访问的页面不存在');
    });

    it('should render eco message', () => {
      setActivePinia(createPinia());
      const wrapper = mountNotFound();
      expect(wrapper.text()).toContain('每一次浏览都在为环保贡献力量');
    });
  });

  describe('导航链接', () => {
    it('should render home link', () => {
      setActivePinia(createPinia());
      const wrapper = mountNotFound();
      expect(wrapper.text()).toContain('返回首页');
    });

    it('should render items link', () => {
      setActivePinia(createPinia());
      const wrapper = mountNotFound();
      expect(wrapper.text()).toContain('浏览好物');
    });
  });

  describe('图形元素', () => {
    it('should render SVG graphic', () => {
      setActivePinia(createPinia());
      const wrapper = mountNotFound();
      expect(wrapper.find('svg').exists()).toBe(true);
    });

    it('should have not-found-graphic section', () => {
      setActivePinia(createPinia());
      const wrapper = mountNotFound();
      expect(wrapper.find('.not-found-graphic').exists()).toBe(true);
    });

    it('should have not-found-content section', () => {
      setActivePinia(createPinia());
      const wrapper = mountNotFound();
      expect(wrapper.find('.not-found-content').exists()).toBe(true);
    });
  });

  describe('页面结构', () => {
    it('should have not-found-page root', () => {
      setActivePinia(createPinia());
      const wrapper = mountNotFound();
      expect(wrapper.find('.not-found-page').exists()).toBe(true);
    });

    it('should have not-found-container', () => {
      setActivePinia(createPinia());
      const wrapper = mountNotFound();
      expect(wrapper.find('.not-found-container').exists()).toBe(true);
    });

    it('should have not-found-actions', () => {
      setActivePinia(createPinia());
      const wrapper = mountNotFound();
      expect(wrapper.find('.not-found-actions').exists()).toBe(true);
    });

    it('should have eco-message section', () => {
      setActivePinia(createPinia());
      const wrapper = mountNotFound();
      expect(wrapper.find('.eco-message').exists()).toBe(true);
    });
  });
});
