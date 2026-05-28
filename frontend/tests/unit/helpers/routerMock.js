import { vi } from 'vitest'
import { ref } from 'vue'

// 创建路由 mock
export const createRouteMock = (overrides = {}) => ({
  path: '/',
  name: 'Home',
  params: {},
  query: {},
  meta: {},
  ...overrides,
})

// 创建路由器 mock
export const createRouterMock = (overrides = {}) => ({
  push: vi.fn(),
  replace: vi.fn(),
  back: vi.fn(),
  forward: vi.fn(),
  currentRoute: ref(createRouteMock()),
  ...overrides,
})

// Vue Router 插件 mock
export const routerMockPlugin = {
  install(app) {
    const routerMock = createRouterMock()
    app.config.globalProperties.$router = routerMock
    app.config.globalProperties.$route = createRouteMock()
    app.provide('router', routerMock)
    app.provide('route', createRouteMock())
  },
}
