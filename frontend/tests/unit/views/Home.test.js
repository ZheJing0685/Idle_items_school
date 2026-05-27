import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { setActivePinia, createPinia } from 'pinia'

const mockGetCategoryTree = vi.fn().mockResolvedValue({
  data: [
    { id: 1, name: '电子产品', itemCount: 10, children: [] },
    { id: 2, name: '教材书籍', itemCount: 5, children: [] }
  ]
})

const mockFetchHotItems = vi.fn().mockResolvedValue([])

vi.mock('@/store', () => ({
  useItemStore: () => ({
    hotItems: [],
    fetchHotItems: mockFetchHotItems
  })
}))

vi.mock('@/api', () => ({
  default: {
    category: {
      getCategoryTree: mockGetCategoryTree
    }
  }
}))

vi.mock('lucide-vue-next', () => ({
  Search: { template: '<span class="icon-search"></span>' },
  CirclePlus: { template: '<span class="icon-plus"></span>' },
  ArrowRight: { template: '<span class="icon-arrow"></span>' },
  Smile: { template: '<span class="icon-smile"></span>' },
  Eye: { template: '<span class="icon-eye"></span>' },
  Image: { template: '<span class="icon-image"></span>' },
  Verified: { template: '<span class="icon-verified"></span>' },
  ShieldCheck: { template: '<span class="icon-shield"></span>' },
  MessageSquare: { template: '<span class="icon-message"></span>' },
  Leaf: { template: '<span class="icon-leaf"></span>' },
  Laptop: { template: '<span class="icon-laptop"></span>' },
  BookOpen: { template: '<span class="icon-book"></span>' },
  Watch: { template: '<span class="icon-watch"></span>' },
  Smartphone: { template: '<span class="icon-smartphone"></span>' },
  Home: { template: '<span class="icon-home"></span>' },
  Trophy: { template: '<span class="icon-trophy"></span>' },
  Shirt: { template: '<span class="icon-shirt"></span>' },
  Package: { template: '<span class="icon-package"></span>' }
}))

vi.mock('element-plus', () => ({
  ElMessage: { success: vi.fn(), error: vi.fn() },
  ElAvatar: { template: '<span class="el-avatar-stub"><slot /></span>' }
}))

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'Home', component: { template: '<div>Home</div>' } },
    { path: '/items', name: 'Items', component: { template: '<div>Items</div>' } },
    { path: '/publish', name: 'Publish', component: { template: '<div>Publish</div>' } },
    { path: '/register', name: 'Register', component: { template: '<div>Register</div>' } }
  ]
})

let Home
beforeAll(async () => {
  const mod = await import('@/views/Home.vue')
  Home = mod.default
})

beforeEach(() => {
  setActivePinia(createPinia())
  vi.clearAllMocks()
  mockGetCategoryTree.mockReset()
  mockGetCategoryTree.mockResolvedValue({
    data: [
      { id: 1, name: '电子产品', itemCount: 10, children: [] },
      { id: 2, name: '教材书籍', itemCount: 5, children: [] }
    ]
  })
  mockFetchHotItems.mockReset()
  mockFetchHotItems.mockResolvedValue([])
})

const mountHome = (options = {}) => {
  return mount(Home, {
    global: {
      plugins: [router],
      stubs: {
        'router-link': { template: '<a @click="$emit(\'click\')"><slot /></a>' },
        'el-avatar': { template: '<span class="el-avatar-stub"><slot /></span>' }
      }
    },
    ...options
  })
}

describe('Home View', () => {
  describe('组件渲染', () => {
    it('renders correctly', () => {
      const wrapper = mountHome()
      expect(wrapper.exists()).toBe(true)
    })

    it('renders hero section', async () => {
      const wrapper = mountHome()
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.hero').exists()).toBe(true)
    })

    it('displays hero title', async () => {
      const wrapper = mountHome()
      await wrapper.vm.$nextTick()
      expect(wrapper.text()).toContain('闲置不闲置')
    })

    it('displays hero description', async () => {
      const wrapper = mountHome()
      await wrapper.vm.$nextTick()
      expect(wrapper.text()).toContain('变废为宝')
    })

    it('renders categories section', async () => {
      const wrapper = mountHome()
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.categories').exists()).toBe(true)
    })

    it('renders featured section', async () => {
      const wrapper = mountHome()
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.featured').exists()).toBe(true)
    })

    it('renders trust section', async () => {
      const wrapper = mountHome()
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.trust').exists()).toBe(true)
    })
  })

  describe('数据加载', () => {
    it('has categories state', () => {
      const wrapper = mountHome()
      expect(Array.isArray(wrapper.vm.categories)).toBe(true)
    })

    it('has featuredItems state', () => {
      const wrapper = mountHome()
      expect(Array.isArray(wrapper.vm.featuredItems)).toBe(true)
    })

    it('has loading state', async () => {
      const wrapper = mountHome()
      await wrapper.vm.$nextTick()
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.loading).toBeDefined()
      expect(typeof wrapper.vm.loading.categories).toBe('boolean')
      expect(typeof wrapper.vm.loading.items).toBe('boolean')
    })
  })

  describe('功能方法', () => {
    it('has fetchCategories method', () => {
      const wrapper = mountHome()
      expect(typeof wrapper.vm.fetchCategories).toBe('function')
    })

    it('has getCategoryColor method', () => {
      const wrapper = mountHome()
      expect(typeof wrapper.vm.getCategoryColor).toBe('function')
      const color = wrapper.vm.getCategoryColor(1)
      expect(color).toBeTruthy()
    })

    it('has getCategoryIcon method', () => {
      const wrapper = mountHome()
      expect(typeof wrapper.vm.getCategoryIcon).toBe('function')
    })
  })
})
