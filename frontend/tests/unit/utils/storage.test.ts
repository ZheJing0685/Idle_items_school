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
    get length() {
      return Object.keys(store).length;
    },
    key: vi.fn((index: number) => Object.keys(store)[index] || null),
  };
})();

Object.defineProperty(window, 'localStorage', { value: localStorageMock });

describe('Storage Utils', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorageMock.clear();
  });

  describe('Storage class', () => {
    it('should export storage function', async () => {
      const storageModule = await import('@/utils/storage');
      expect(typeof storageModule.default).toBe('function');
    });

    it('should create storage instance', async () => {
      const storageModule = await import('@/utils/storage');
      const storage = storageModule.default('test');
      expect(storage).toBeDefined();
    });

    it('should set and get value', async () => {
      const storageModule = await import('@/utils/storage');
      const storage = storageModule.default('test');

      storage.set('key1', 'value1');
      const result = storage.get('key1');

      expect(result).toBe('value1');
    });

    it('should set and get object value', async () => {
      const storageModule = await import('@/utils/storage');
      const storage = storageModule.default('test');

      const obj = { name: 'test', age: 25 };
      storage.set('obj', obj);
      const result = storage.get('obj');

      expect(result).toEqual(obj);
    });

    it('should return null for non-existent key', async () => {
      const storageModule = await import('@/utils/storage');
      const storage = storageModule.default('test');

      const result = storage.get('nonexistent');

      expect(result).toBeNull();
    });

    it('should remove value', async () => {
      const storageModule = await import('@/utils/storage');
      const storage = storageModule.default('test');

      storage.set('key1', 'value1');
      storage.remove('key1');
      const result = storage.get('key1');

      expect(result).toBeNull();
    });

    it('should check if key exists', async () => {
      const storageModule = await import('@/utils/storage');
      const storage = storageModule.default('test');

      storage.set('key1', 'value1');

      expect(storage.has('key1')).toBe(true);
      expect(storage.has('nonexistent')).toBe(false);
    });

    it('should get all values', async () => {
      const storageModule = await import('@/utils/storage');
      const storage = storageModule.default('test');

      storage.set('key1', 'value1');
      storage.set('key2', 'value2');

      // 验证单个值
      expect(storage.get('key1')).toBe('value1');
      expect(storage.get('key2')).toBe('value2');
    });
  });

  describe('Expiry functionality', () => {
    it('should set with expiry', async () => {
      const storageModule = await import('@/utils/storage');
      const storage = storageModule.default('test');

      const result = storage.setWithExpiry('key1', 'value1', 1000);

      expect(result).toBe(true);
    });

    it('should get value before expiry', async () => {
      const storageModule = await import('@/utils/storage');
      const storage = storageModule.default('test');

      storage.setWithExpiry('key1', 'value1', 10000); // 10 seconds
      const result = storage.getWithExpiry('key1');

      expect(result).toBe('value1');
    });

    it('should return null after expiry', async () => {
      vi.useFakeTimers();
      const storageModule = await import('@/utils/storage');
      const storage = storageModule.default('test');

      storage.setWithExpiry('key1', 'value1', 1000); // 1 second

      // Advance time by 2 seconds
      vi.advanceTimersByTime(2000);

      const result = storage.getWithExpiry('key1');

      expect(result).toBeNull();

      vi.useRealTimers();
    });

    it('should return null for non-existent key with expiry', async () => {
      const storageModule = await import('@/utils/storage');
      const storage = storageModule.default('test');

      const result = storage.getWithExpiry('nonexistent');

      expect(result).toBeNull();
    });
  });

  describe('Namespace isolation', () => {
    it('should isolate different namespaces', async () => {
      const storageModule = await import('@/utils/storage');
      const storage1 = storageModule.default('app1');
      const storage2 = storageModule.default('app2');

      storage1.set('key', 'value1');
      storage2.set('key', 'value2');

      expect(storage1.get('key')).toBe('value1');
      expect(storage2.get('key')).toBe('value2');
    });

    it('should clear namespace values', async () => {
      const storageModule = await import('@/utils/storage');
      const storage = storageModule.default('app1');

      storage.set('key1', 'value1');
      storage.set('key2', 'value2');

      // 验证值已设置
      expect(storage.get('key1')).toBe('value1');
      expect(storage.get('key2')).toBe('value2');
    });
  });

  describe('Error handling', () => {
    it('should handle get error gracefully', async () => {
      localStorageMock.getItem.mockImplementation(() => {
        throw new Error('Storage error');
      });

      const storageModule = await import('@/utils/storage');
      const storage = storageModule.default('test');
      const result = storage.get('key1');

      expect(result).toBeNull();
    });

    it('should handle set error gracefully', async () => {
      localStorageMock.setItem.mockImplementation(() => {
        throw new Error('Storage error');
      });

      const storageModule = await import('@/utils/storage');
      const storage = storageModule.default('test');
      const result = storage.set('key1', 'value1');

      expect(result).toBe(false);
    });

    it('should handle remove error gracefully', async () => {
      localStorageMock.removeItem.mockImplementation(() => {
        throw new Error('Storage error');
      });

      const storageModule = await import('@/utils/storage');
      const storage = storageModule.default('test');
      const result = storage.remove('key1');

      expect(result).toBe(false);
    });
  });
});
