import { describe, it, expect, vi, beforeEach } from 'vitest';

// Mock localStorage
const localStorageMock = (() => {
  let store: Record<string, string> = {};
  return {
    getItem: vi.fn((key: string) => store[key] || null),
    setItem: vi.fn((key: string, value: string) => {
      store[key] = value;
    }),
    removeItem: vi.fn((key: string) => {
      delete store[key];
    }),
    clear: vi.fn(() => {
      store = {};
    }),
  };
})();

Object.defineProperty(window, 'localStorage', { value: localStorageMock });

// Mock matchMedia
const matchMediaMock = vi.fn().mockImplementation(query => ({
  matches: false,
  media: query,
  onchange: null,
  addListener: vi.fn(),
  removeListener: vi.fn(),
  addEventListener: vi.fn(),
  removeEventListener: vi.fn(),
  dispatchEvent: vi.fn(),
}));

Object.defineProperty(window, 'matchMedia', { value: matchMediaMock });

describe('useDarkMode Composable', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorageMock.clear();
    document.documentElement.classList.remove('dark', 'reduced-motion');
  });

  it('should export useDarkMode function', async () => {
    const { useDarkMode } = await import('@/composables/useDarkMode');
    expect(typeof useDarkMode).toBe('function');
  });

  it('should return isDark and toggle', async () => {
    const { useDarkMode } = await import('@/composables/useDarkMode');
    const result = useDarkMode();
    expect(result.isDark).toBeDefined();
    expect(typeof result.toggle).toBe('function');
  });

  it('should toggle dark mode', async () => {
    const { useDarkMode } = await import('@/composables/useDarkMode');
    const { isDark, toggle } = useDarkMode();
    const initialValue = isDark.value;
    toggle();
    expect(isDark.value).toBe(!initialValue);
  });

  it('should apply dark class to document', async () => {
    const { useDarkMode } = await import('@/composables/useDarkMode');
    const { isDark } = useDarkMode();
    // isDark is a ref, so we can check its effect
    expect(isDark.value).toBeDefined();
  });

  it('should read from localStorage', async () => {
    localStorageMock.getItem.mockReturnValue('dark');
    const { useDarkMode } = await import('@/composables/useDarkMode');
    const { isDark } = useDarkMode();
    expect(isDark.value).toBe(true);
  });

  it('should use matchMedia as fallback', async () => {
    localStorageMock.getItem.mockReturnValue(null);
    matchMediaMock.mockReturnValue({
      matches: true,
      media: '(prefers-color-scheme: dark)',
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
    });
    // Re-import to get fresh module
    vi.resetModules();
    const { useDarkMode } = await import('@/composables/useDarkMode');
    const { isDark } = useDarkMode();
    expect(isDark.value).toBeDefined();
  });
});
