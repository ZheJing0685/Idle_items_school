import { describe, it, expect, vi, beforeAll } from 'vitest'
import { mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'

let Footer: any
beforeAll(async () => {
  const mod = await import('@/components/Footer.vue')
  Footer = mod.default
})

const mountFooter = () => {
  setActivePinia(createPinia())
  return mount(Footer, {
    global: {
      stubs: {
        'router-link': { template: '<a><slot /></a>' }
      }
    }
  })
}

describe('Footer Component', () => {
  describe('组件渲染', () => {
    it('should render footer', () => {
      const wrapper = mountFooter()
      expect(wrapper.find('footer').exists()).toBe(true)
    })

    it('should render brand name', () => {
      const wrapper = mountFooter()
      expect(wrapper.text()).toContain('闲置好物')
    })

    it('should render brand tagline', () => {
      const wrapper = mountFooter()
      expect(wrapper.text()).toContain('校园绿色交易平台')
    })

    it('should render description', () => {
      const wrapper = mountFooter()
      expect(wrapper.text()).toContain('让闲置物品找到新主人')
    })
  })

  describe('快速链接', () => {
    it('should render quick links section', () => {
      const wrapper = mountFooter()
      expect(wrapper.text()).toContain('快速链接')
    })

    it('should render home link', () => {
      const wrapper = mountFooter()
      expect(wrapper.text()).toContain('首页')
    })

    it('should render items link', () => {
      const wrapper = mountFooter()
      expect(wrapper.text()).toContain('发现好物')
    })

    it('should render publish link', () => {
      const wrapper = mountFooter()
      expect(wrapper.text()).toContain('发布闲置')
    })

    it('should render orders link', () => {
      const wrapper = mountFooter()
      expect(wrapper.text()).toContain('我的订单')
    })
  })

  describe('分类浏览', () => {
    it('should render categories section', () => {
      const wrapper = mountFooter()
      expect(wrapper.text()).toContain('分类浏览')
    })

    it('should render digital products category', () => {
      const wrapper = mountFooter()
      expect(wrapper.text()).toContain('数码产品')
    })

    it('should render books category', () => {
      const wrapper = mountFooter()
      expect(wrapper.text()).toContain('书籍教材')
    })

    it('should render daily necessities category', () => {
      const wrapper = mountFooter()
      expect(wrapper.text()).toContain('生活用品')
    })

    it('should render sports category', () => {
      const wrapper = mountFooter()
      expect(wrapper.text()).toContain('运动器材')
    })

    it('should render clothing category', () => {
      const wrapper = mountFooter()
      expect(wrapper.text()).toContain('服装鞋帽')
    })
  })

  describe('帮助与支持', () => {
    it('should render help section', () => {
      const wrapper = mountFooter()
      expect(wrapper.text()).toContain('帮助与支持')
    })

    it('should render FAQ link', () => {
      const wrapper = mountFooter()
      expect(wrapper.text()).toContain('常见问题')
    })

    it('should render trading guide link', () => {
      const wrapper = mountFooter()
      expect(wrapper.text()).toContain('交易指南')
    })

    it('should render contact support link', () => {
      const wrapper = mountFooter()
      expect(wrapper.text()).toContain('联系客服')
    })

    it('should render feedback link', () => {
      const wrapper = mountFooter()
      expect(wrapper.text()).toContain('意见反馈')
    })
  })

  describe('联系我们', () => {
    it('should render contact section', () => {
      const wrapper = mountFooter()
      expect(wrapper.text()).toContain('联系我们')
    })

    it('should render email', () => {
      const wrapper = mountFooter()
      expect(wrapper.text()).toContain('contact@xianhaowu.com')
    })

    it('should render phone number', () => {
      const wrapper = mountFooter()
      expect(wrapper.text()).toContain('400-888-6666')
    })

    it('should render address', () => {
      const wrapper = mountFooter()
      expect(wrapper.text()).toContain('校园大学生活动中心')
    })
  })

  describe('版权信息', () => {
    it('should render copyright', () => {
      const wrapper = mountFooter()
      expect(wrapper.text()).toContain('© 2026 闲置好物')
    })

    it('should render eco message', () => {
      const wrapper = mountFooter()
      expect(wrapper.text()).toContain('已帮助 12,847 件物品找到新主人')
    })

    it('should render eco badge', () => {
      const wrapper = mountFooter()
      expect(wrapper.text()).toContain('环保交易 · 减少浪费')
    })
  })

  describe('页面结构', () => {
    it('should have footer-main section', () => {
      const wrapper = mountFooter()
      expect(wrapper.find('.footer-main').exists()).toBe(true)
    })

    it('should have footer-bottom section', () => {
      const wrapper = mountFooter()
      expect(wrapper.find('.footer-bottom').exists()).toBe(true)
    })

    it('should have footer-grid', () => {
      const wrapper = mountFooter()
      expect(wrapper.find('.footer-grid').exists()).toBe(true)
    })

    it('should have footer-brand', () => {
      const wrapper = mountFooter()
      expect(wrapper.find('.footer-brand').exists()).toBe(true)
    })

    it('should have footer-links-group', () => {
      const wrapper = mountFooter()
      const groups = wrapper.findAll('.footer-links-group')
      expect(groups.length).toBeGreaterThanOrEqual(3)
    })

    it('should have footer-contact', () => {
      const wrapper = mountFooter()
      expect(wrapper.find('.footer-contact').exists()).toBe(true)
    })
  })

  describe('链接数量', () => {
    it('should have multiple navigation links', () => {
      const wrapper = mountFooter()
      const links = wrapper.findAll('a, router-link-stub')
      expect(links.length).toBeGreaterThanOrEqual(10)
    })
  })
})
