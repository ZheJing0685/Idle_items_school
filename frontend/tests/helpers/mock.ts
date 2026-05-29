import { vi } from 'vitest';

/**
 * 创建 Axios Mock
 */
export const createAxiosMock = () => {
  return {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
    patch: vi.fn(),
    interceptors: {
      request: { use: vi.fn() },
      response: { use: vi.fn() },
    },
  };
};

/**
 * 创建 Vue Router Mock
 */
export const createRouterMock = () => {
  return {
    push: vi.fn(),
    replace: vi.fn(),
    go: vi.fn(),
    back: vi.fn(),
    forward: vi.fn(),
    currentRoute: { value: { path: '/', params: {}, query: {} } },
  };
};

/**
 * 创建 Pinia Store Mock
 */
export const createStoreMock = (storeName: string, initialState: any = {}) => {
  return {
    ...initialState,
    $reset: vi.fn(),
    $patch: vi.fn(),
    $subscribe: vi.fn(),
  };
};

/**
 * 创建 Element Plus Mock
 */
export const createElMessageMock = () => ({
  success: vi.fn(),
  error: vi.fn(),
  warning: vi.fn(),
  info: vi.fn(),
});

export const createElMessageBoxMock = () => ({
  confirm: vi.fn().mockResolvedValue(true),
  alert: vi.fn(),
  prompt: vi.fn(),
});

/**
 * 创建 localStorage Mock
 */
export const createLocalStorageMock = () => {
  const store: Record<string, string> = {};
  return {
    getItem: vi.fn((key: string) => store[key] || null),
    setItem: vi.fn((key: string, value: string) => {
      store[key] = value;
    }),
    removeItem: vi.fn((key: string) => {
      delete store[key];
    }),
    clear: vi.fn(() => {
      Object.keys(store).forEach(key => delete store[key]);
    }),
    get length() {
      return Object.keys(store).length;
    },
    key: vi.fn((index: number) => Object.keys(store)[index] || null),
  };
};

/**
 * 创建 sessionStorage Mock
 */
export const createSessionStorageMock = () => createLocalStorageMock();
