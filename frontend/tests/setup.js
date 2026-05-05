import { config } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { beforeAll, afterAll, afterEach, vi } from 'vitest'

// 配置 Vue Test Utils 全局选项
config.global.stubs = {
  RouterLink: { template: '<a><slot /></a>' },
  RouterView: { template: '<div><slot /></div>' }
}

// 创建全局 Pinia 实例
beforeAll(() => {
  const pinia = createPinia()
  setActivePinia(pinia)
})

afterAll(() => {
  // 清理
})

afterEach(() => {
  if (typeof localStorage !== 'undefined') {
    localStorage.clear()
  }
  if (typeof sessionStorage !== 'undefined') {
    sessionStorage.clear()
  }
})

// Mock window.matchMedia
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: vi.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn()
  }))
})

// Mock ResizeObserver
global.ResizeObserver = vi.fn().mockImplementation(() => ({
  observe: vi.fn(),
  unobserve: vi.fn(),
  disconnect: vi.fn()
}))

// Mock IntersectionObserver
global.IntersectionObserver = vi.fn().mockImplementation(() => ({
  observe: vi.fn(),
  unobserve: vi.fn(),
  disconnect: vi.fn()
}))

// 过滤 Vue 警告
const originalConsoleError = console.error
console.error = (...args) => {
  if (args[0]?.includes?.('[Vue warn]')) return
  originalConsoleError(...args)
}
