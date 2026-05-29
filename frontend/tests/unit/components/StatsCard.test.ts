import { describe, it, expect, vi, beforeAll } from 'vitest';
import { mount } from '@vue/test-utils';
import { setActivePinia, createPinia } from 'pinia';

let StatsCard: any;
beforeAll(async () => {
  const mod = await import('@/components/user/StatsCard.vue');
  StatsCard = mod.default;
});

const mountStatsCard = (props = {}) => {
  setActivePinia(createPinia());
  return mount(StatsCard, {
    props: {
      stats: [
        { value: 10, label: '发布物品' },
        { value: 5, label: '成功交易' },
        { value: 3, label: '收到评价' },
        { value: 100, label: '积分', accent: true },
      ],
      ...props,
    },
  });
};

describe('StatsCard Component', () => {
  describe('组件渲染', () => {
    it('should render stats card', () => {
      const wrapper = mountStatsCard();
      expect(wrapper.find('.stats-card').exists()).toBe(true);
    });

    it('should render correct number of stat items', () => {
      const wrapper = mountStatsCard();
      const items = wrapper.findAll('.stat-item');
      expect(items.length).toBe(4);
    });

    it('should render stat values', () => {
      const wrapper = mountStatsCard();
      expect(wrapper.text()).toContain('10');
      expect(wrapper.text()).toContain('5');
      expect(wrapper.text()).toContain('3');
      expect(wrapper.text()).toContain('100');
    });

    it('should render stat labels', () => {
      const wrapper = mountStatsCard();
      expect(wrapper.text()).toContain('发布物品');
      expect(wrapper.text()).toContain('成功交易');
      expect(wrapper.text()).toContain('收到评价');
      expect(wrapper.text()).toContain('积分');
    });
  });

  describe('Props 验证', () => {
    it('should accept valid stats array', () => {
      const wrapper = mountStatsCard();
      expect(wrapper.props('stats')).toBeDefined();
      expect(Array.isArray(wrapper.props('stats'))).toBe(true);
    });

    it('should render accent items', () => {
      const wrapper = mountStatsCard();
      const accentItem = wrapper.find('.stat-item.accent');
      expect(accentItem.exists()).toBe(true);
    });

    it('should handle empty stats array', () => {
      const wrapper = mountStatsCard({ stats: [] });
      const items = wrapper.findAll('.stat-item');
      expect(items.length).toBe(0);
    });

    it('should handle single stat', () => {
      const wrapper = mountStatsCard({
        stats: [{ value: 42, label: '测试' }],
      });
      const items = wrapper.findAll('.stat-item');
      expect(items.length).toBe(1);
      expect(wrapper.text()).toContain('42');
      expect(wrapper.text()).toContain('测试');
    });
  });

  describe('样式类', () => {
    it('should have stat-item class', () => {
      const wrapper = mountStatsCard();
      expect(wrapper.find('.stat-item').exists()).toBe(true);
    });

    it('should have stat-value class', () => {
      const wrapper = mountStatsCard();
      expect(wrapper.find('.stat-value').exists()).toBe(true);
    });

    it('should have stat-label class', () => {
      const wrapper = mountStatsCard();
      expect(wrapper.find('.stat-label').exists()).toBe(true);
    });

    it('should apply accent class when accent prop is true', () => {
      const wrapper = mountStatsCard();
      const accentItems = wrapper.findAll('.stat-item.accent');
      expect(accentItems.length).toBe(1);
    });
  });

  describe('数据类型', () => {
    it('should handle string values', () => {
      const wrapper = mountStatsCard({
        stats: [{ value: '99+', label: '消息' }],
      });
      expect(wrapper.text()).toContain('99+');
    });

    it('should handle number values', () => {
      const wrapper = mountStatsCard({
        stats: [{ value: 123, label: '数字' }],
      });
      expect(wrapper.text()).toContain('123');
    });
  });
});
