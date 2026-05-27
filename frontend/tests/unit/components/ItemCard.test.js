import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { setActivePinia, createPinia } from 'pinia'

vi.mock('lucide-vue-next', () => ({
  Eye: { template: '<span class="icon-eye"></span>' }
}))

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'Home', component: { template: '<div>Home</div>' } },
    { path: '/item/:id', name: 'ItemDetail', component: { template: '<div>ItemDetail</div>' } }
  ]
})

let ItemCard
beforeAll(async () => {
  const mod = await import('@/components/common/ItemCard.vue')
  ItemCard = mod.default
})

beforeEach(() => {
  setActivePinia(createPinia())
  vi.clearAllMocks()
})

const defaultItem = {
  id: 1,
  title: '测试商品',
  price: 99,
  originalPrice: 199,
  coverImage: 'https://example.com/image.jpg',
  sellerNickname: '测试卖家',
  viewCount: 100,
  createdAt: new Date().toISOString(),
  isBargainAllowed: true,
  tags: '["标签1","标签2"]'
}

const mountItemCard = (itemOverrides = {}, options = {}) => {
  return mount(ItemCard, {
    props: {
      item: { ...defaultItem, ...itemOverrides }
    },
    global: {
      plugins: [router],
      stubs: {
        'router-link': { template: '<a @click="$emit(\'click\')"><slot /></a>' }
      }
    },
    ...options
  })
}

describe('ItemCard Component', () => {
  describe('组件渲染', () => {
    it('renders correctly', () => {
      const wrapper = mountItemCard()
      expect(wrapper.exists()).toBe(true)
    })

    it('renders item title', () => {
      const wrapper = mountItemCard()
      expect(wrapper.text()).toContain('测试商品')
    })

    it('renders item price', () => {
      const wrapper = mountItemCard()
      expect(wrapper.text()).toContain('¥99')
    })

    it('renders original price when provided', () => {
      const wrapper = mountItemCard()
      expect(wrapper.text()).toContain('¥199')
    })

    it('renders seller nickname', () => {
      const wrapper = mountItemCard()
      expect(wrapper.text()).toContain('测试卖家')
    })

    it('renders view count', () => {
      const wrapper = mountItemCard()
      expect(wrapper.text()).toContain('100')
    })

    it('renders bargain badge when isBargainAllowed is true', () => {
      const wrapper = mountItemCard()
      expect(wrapper.find('.bargain-badge').exists()).toBe(true)
    })

    it('does not render bargain badge when isBargainAllowed is false', () => {
      const wrapper = mountItemCard({ isBargainAllowed: false })
      expect(wrapper.find('.bargain-badge').exists()).toBe(false)
    })

    it('renders tags when provided', () => {
      const wrapper = mountItemCard()
      expect(wrapper.find('.item-tags').exists()).toBe(true)
    })

    it('does not render tags when not provided', () => {
      const wrapper = mountItemCard({ tags: null })
      expect(wrapper.find('.item-tags').exists()).toBe(false)
    })
  })

  describe('Props处理', () => {
    it('uses default seller name when sellerNickname is empty', () => {
      const wrapper = mountItemCard({ sellerNickname: '' })
      expect(wrapper.text()).toContain('未知卖家')
    })

    it('uses default view count when viewCount is 0', () => {
      const wrapper = mountItemCard({ viewCount: 0 })
      expect(wrapper.text()).toContain('0')
    })

    it('uses cover image when provided', () => {
      const wrapper = mountItemCard()
      const img = wrapper.find('img')
      expect(img.attributes('src')).toBe('https://example.com/image.jpg')
    })

    it('uses default image when coverImage is not provided', () => {
      const wrapper = mountItemCard({ coverImage: null })
      const img = wrapper.find('img')
      expect(img.attributes('src')).toBe('/placeholder-item.svg')
    })
  })

  describe('功能方法', () => {
    it('has formatTime method', () => {
      const wrapper = mountItemCard()
      expect(typeof wrapper.vm.formatTime).toBe('function')
    })

    it('has parseTags method', () => {
      const wrapper = mountItemCard()
      expect(typeof wrapper.vm.parseTags).toBe('function')
    })

    it('has navigateToDetail method', () => {
      const wrapper = mountItemCard()
      expect(typeof wrapper.vm.navigateToDetail).toBe('function')
    })

    it('parseTags returns parsed tags array', () => {
      const wrapper = mountItemCard()
      const result = wrapper.vm.parseTags('["tag1","tag2"]')
      expect(result).toEqual(['tag1', 'tag2'])
    })

    it('parseTags returns empty array for invalid JSON', () => {
      const wrapper = mountItemCard()
      const result = wrapper.vm.parseTags('invalid json')
      expect(result).toEqual([])
    })

    it('parseTags returns empty array for null input', () => {
      const wrapper = mountItemCard()
      const result = wrapper.vm.parseTags(null)
      expect(result).toEqual([])
    })

    it('formatTime returns formatted time string', () => {
      const wrapper = mountItemCard()
      const result = wrapper.vm.formatTime(new Date().toISOString())
      expect(typeof result).toBe('string')
    })

    it('formatTime returns empty string for null input', () => {
      const wrapper = mountItemCard()
      const result = wrapper.vm.formatTime(null)
      expect(result).toBe('')
    })
  })

  describe('交互行为', () => {
    it('navigates to detail on click', async () => {
      const wrapper = mountItemCard()
      const pushSpy = vi.spyOn(router, 'push')
      await wrapper.find('.item-card').trigger('click')
      expect(pushSpy).toHaveBeenCalledWith('/item/1')
    })

    it('has tabindex attribute for accessibility', () => {
      const wrapper = mountItemCard()
      expect(wrapper.find('.item-card').attributes('tabindex')).toBe('0')
    })

    it('has role attribute for accessibility', () => {
      const wrapper = mountItemCard()
      expect(wrapper.find('.item-card').attributes('role')).toBe('button')
    })
  })
})
