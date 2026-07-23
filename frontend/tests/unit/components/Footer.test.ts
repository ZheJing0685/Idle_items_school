import { describe, it, expect, beforeAll } from 'vitest';
import { mount } from '@vue/test-utils';
import { setActivePinia, createPinia } from 'pinia';

let Footer: any;
beforeAll(async () => {
  const mod = await import('@/components/Footer.vue');
  Footer = mod.default;
});

const mountFooter = () => {
  setActivePinia(createPinia());
  return mount(Footer, {
    global: {
      stubs: {
        'router-link': { template: '<a><slot /></a>' },
      },
    },
  });
};

describe('Footer Component', () => {
  describe('组件渲染', () => {
    it('should render footer', () => {
      const wrapper = mountFooter();
      expect(wrapper.find('footer').exists()).toBe(true);
    });

    it('should render brand name', () => {
      const wrapper = mountFooter();
      expect(wrapper.text()).toContain('闲置好物');
    });

    it('should render brand tagline', () => {
      const wrapper = mountFooter();
      expect(wrapper.text()).toContain('校园绿色交易平台');
    });
  });

  describe('帮助与支持', () => {
    it('should render help section', () => {
      const wrapper = mountFooter();
      expect(wrapper.text()).toContain('帮助与支持');
    });

    it('should render FAQ link', () => {
      const wrapper = mountFooter();
      expect(wrapper.text()).toContain('常见问题');
    });

    it('should render trading guide link', () => {
      const wrapper = mountFooter();
      expect(wrapper.text()).toContain('交易指南');
    });

    it('should render contact support link', () => {
      const wrapper = mountFooter();
      expect(wrapper.text()).toContain('联系客服');
    });

    it('should render feedback link', () => {
      const wrapper = mountFooter();
      expect(wrapper.text()).toContain('意见反馈');
    });
  });

  describe('联系方式', () => {
    it('should render email', () => {
      const wrapper = mountFooter();
      expect(wrapper.text()).toContain('contact@xianhaowu.com');
    });

    it('should render phone number', () => {
      const wrapper = mountFooter();
      expect(wrapper.text()).toContain('400-888-6666');
    });
  });

  describe('版权信息', () => {
    it('should render copyright', () => {
      const wrapper = mountFooter();
      expect(wrapper.text()).toContain('© 2026 闲置好物');
    });
  });

  describe('页面结构', () => {
    it('should have footer-main section', () => {
      const wrapper = mountFooter();
      expect(wrapper.find('.footer-main').exists()).toBe(true);
    });

    it('should have footer-bottom section', () => {
      const wrapper = mountFooter();
      expect(wrapper.find('.footer-bottom').exists()).toBe(true);
    });

    it('should have footer-brand', () => {
      const wrapper = mountFooter();
      expect(wrapper.find('.footer-brand').exists()).toBe(true);
    });

    it('should have footer-links-group', () => {
      const wrapper = mountFooter();
      expect(wrapper.find('.footer-links-group').exists()).toBe(true);
    });

    it('should have footer-contact', () => {
      const wrapper = mountFooter();
      expect(wrapper.find('.footer-contact').exists()).toBe(true);
    });
  });

  describe('链接数量', () => {
    it('should have navigation links', () => {
      const wrapper = mountFooter();
      const links = wrapper.findAll('a, router-link-stub');
      expect(links.length).toBeGreaterThanOrEqual(4);
    });
  });
});
