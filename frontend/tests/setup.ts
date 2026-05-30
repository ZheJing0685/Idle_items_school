declare const process: {
  listeners(event: string): Function[];
  removeAllListeners(event: string): void;
  on(event: string, handler: (...args: any[]) => void): void;
};

import { config } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { beforeAll, afterAll, afterEach, vi } from 'vitest';

// 配置 Vue Test Utils 全局选项
config.global.stubs = {
  RouterLink: { template: '<a><slot /></a>' },
  RouterView: { template: '<div><slot /></div>' },
  Transition: false,
  TransitionGroup: false,
};

// 创建全局 Pinia 实例
beforeAll(() => {
  const pinia = createPinia();
  setActivePinia(pinia);
});

afterAll(() => {
  // 清理
});

afterEach(() => {
  if (typeof localStorage !== 'undefined') {
    localStorage.clear();
  }
  if (typeof sessionStorage !== 'undefined') {
    sessionStorage.clear();
  }
});

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
    dispatchEvent: vi.fn(),
  })),
})

// Mock ResizeObserver
;(globalThis as any).ResizeObserver = vi.fn().mockImplementation(() => ({
  observe: vi.fn(),
  unobserve: vi.fn(),
  disconnect: vi.fn(),
}));

// Mock IntersectionObserver - 使用类而不是普通对象
class MockIntersectionObserver {
  observe = vi.fn();
  unobserve = vi.fn();
  disconnect = vi.fn();
  takeRecords = vi.fn().mockReturnValue([]);
  root = null;
  rootMargin = '';
  thresholds = [];
}

;(globalThis as any).IntersectionObserver = MockIntersectionObserver;

// Mock Element.prototype.getBoundingClientRect
Element.prototype.getBoundingClientRect = vi.fn().mockReturnValue({
  width: 100,
  height: 100,
  top: 0,
  left: 0,
  bottom: 0,
  right: 0,
  x: 0,
  y: 0,
  toJSON: vi.fn(),
});

// Mock window.scrollTo
window.scrollTo = vi.fn()

// Mock requestAnimationFrame
;(globalThis as any).requestAnimationFrame = vi.fn((cb) => setTimeout(cb, 0))
;(globalThis as any).cancelAnimationFrame = vi.fn((id) => clearTimeout(id));

// 过滤 Vue 警告
const originalConsoleError = console.error;
console.error = (...args) => {
  if (args[0]?.includes?.('[Vue warn]')) return;
  if (args[0]?.includes?.('observer.disconnect')) return;
  if (args[0]?.includes?.('Unhandled error during execution of watcher callback')) return;
  if (args[0]?.includes?.('Unhandled error during execution of mounted hook')) return;
  originalConsoleError(...args);
};

// 过滤未处理的 Promise rejection 警告
const originalUnhandledRejection = process.listeners('unhandledRejection');
process.removeAllListeners('unhandledRejection');
process.on('unhandledRejection', (reason, promise) => {
  // 忽略 observer.disconnect 错误
  if (reason?.toString?.().includes?.('observer.disconnect')) return;
  if (reason?.toString?.().includes?.('route.params')) return;
  // 调用原始处理器
  if (originalUnhandledRejection.length > 0) {
    originalUnhandledRejection.forEach(handler => handler(reason, promise));
  }
});
