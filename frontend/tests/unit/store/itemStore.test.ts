// @ts-nocheck
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';

// Mock API
const { mockGetItems, mockGetHotItems, mockSearchItems, mockGetItem, mockCreateItem, mockUpdateItem, mockOffShelf, mockUploadImage } = vi.hoisted(() => ({
  mockGetItems: vi.fn(),
  mockGetHotItems: vi.fn(),
  mockSearchItems: vi.fn(),
  mockGetItem: vi.fn(),
  mockCreateItem: vi.fn(),
  mockUpdateItem: vi.fn(),
  mockOffShelf: vi.fn(),
  mockUploadImage: vi.fn(),
}));

vi.mock('@/api', () => ({
  default: {
    item: {
      getItems: mockGetItems,
      getHotItems: mockGetHotItems,
      searchItems: mockSearchItems,
      getItem: mockGetItem,
      createItem: mockCreateItem,
      updateItem: mockUpdateItem,
      offShelf: mockOffShelf,
      uploadImage: mockUploadImage,
    },
    user: {
      getItems: vi.fn(),
    },
    clearCache: vi.fn(),
  },
}));

// Mock ErrorHandler
vi.mock('@/utils/error', () => ({
  ErrorHandler: {
    handle: vi.fn(),
  },
}));

describe('Item Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  describe('初始状态', () => {
    it('should export useItemStore', async () => {
      const { useItemStore } = await import('@/store/modules/item');
      expect(typeof useItemStore).toBe('function');
    });

    it('should have initial state', async () => {
      const { useItemStore } = await import('@/store/modules/item');
      const store = useItemStore();
      expect(store.items).toEqual([]);
      expect(store.total).toBe(0);
      expect(store.loading).toBe(false);
      expect(store.hotItems).toEqual([]);
      expect(store.currentItem).toBeNull();
    });
  });

  describe('fetchItems', () => {
    it('should have fetchItems method', async () => {
      const { useItemStore } = await import('@/store/modules/item');
      const store = useItemStore();
      expect(typeof store.fetchItems).toBe('function');
    });

    it('should call API fetchItems', async () => {
      mockGetItems.mockResolvedValue({
        data: { content: [{ id: 1, title: '测试物品' }], totalElements: 1 },
      });

      const { useItemStore } = await import('@/store/modules/item');
      const store = useItemStore();
      await store.fetchItems({ page: 1, size: 10 });

      expect(mockGetItems).toHaveBeenCalled();
    });

    it('should set items after fetch', async () => {
      mockGetItems.mockResolvedValue({
        data: { content: [{ id: 1, title: '测试物品' }], totalElements: 1 },
      });

      const { useItemStore } = await import('@/store/modules/item');
      const store = useItemStore();
      await store.fetchItems();

      expect(store.items).toEqual([{ id: 1, title: '测试物品' }]);
      expect(store.total).toBe(1);
    });

    it('should set loading during fetch', async () => {
      let resolveFetch: any;
      mockGetItems.mockImplementation(() => new Promise(resolve => { resolveFetch = resolve; }));

      const { useItemStore } = await import('@/store/modules/item');
      const store = useItemStore();

      const fetchPromise = store.fetchItems();
      expect(store.loading).toBe(true);

      resolveFetch({ data: { content: [], totalElements: 0 } });
      await fetchPromise;

      expect(store.loading).toBe(false);
    });
  });

  describe('fetchHotItems', () => {
    it('should have fetchHotItems method', async () => {
      const { useItemStore } = await import('@/store/modules/item');
      const store = useItemStore();
      expect(typeof store.fetchHotItems).toBe('function');
    });

    it('should call API fetchHotItems', async () => {
      mockGetHotItems.mockResolvedValue({
        code: 200,
        data: [{ id: 1, title: '热门物品' }],
      });

      const { useItemStore } = await import('@/store/modules/item');
      const store = useItemStore();
      await store.fetchHotItems();

      expect(mockGetHotItems).toHaveBeenCalled();
    });

    it('should set hotItems after fetch', async () => {
      mockGetHotItems.mockResolvedValue({
        code: 200,
        data: [{ id: 1, title: '热门物品' }],
      });

      const { useItemStore } = await import('@/store/modules/item');
      const store = useItemStore();
      await store.fetchHotItems();

      expect(store.hotItems).toEqual([{ id: 1, title: '热门物品' }]);
    });
  });

  describe('searchItems', () => {
    it('should have searchItems method', async () => {
      const { useItemStore } = await import('@/store/modules/item');
      const store = useItemStore();
      expect(typeof store.searchItems).toBe('function');
    });

    it('should call API searchItems', async () => {
      mockSearchItems.mockResolvedValue({
        code: 200,
        data: { content: [{ id: 1, title: '搜索结果' }], totalElements: 1 },
      });

      const { useItemStore } = await import('@/store/modules/item');
      const store = useItemStore();
      await store.searchItems('手机');

      expect(mockSearchItems).toHaveBeenCalledWith('手机', 1, 20, 'createdAt');
    });

    it('should set searchResults after search', async () => {
      mockSearchItems.mockResolvedValue({
        code: 200,
        data: { content: [{ id: 1, title: '搜索结果' }], totalElements: 1 },
      });

      const { useItemStore } = await import('@/store/modules/item');
      const store = useItemStore();
      await store.searchItems('手机');

      expect(store.searchResults).toEqual([{ id: 1, title: '搜索结果' }]);
      expect(store.searchTotal).toBe(1);
    });
  });

  describe('getItem', () => {
    it('should have getItem method', async () => {
      const { useItemStore } = await import('@/store/modules/item');
      const store = useItemStore();
      expect(typeof store.getItem).toBe('function');
    });

    it('should call API getItem', async () => {
      mockGetItem.mockResolvedValue({
        code: 200,
        data: { id: 1, title: '物品详情' },
      });

      const { useItemStore } = await import('@/store/modules/item');
      const store = useItemStore();
      await store.getItem(1);

      expect(mockGetItem).toHaveBeenCalledWith(1);
    });

    it('should set currentItem after fetch', async () => {
      mockGetItem.mockResolvedValue({
        code: 200,
        data: { id: 1, title: '物品详情' },
      });

      const { useItemStore } = await import('@/store/modules/item');
      const store = useItemStore();
      await store.getItem(1);

      expect(store.currentItem).toEqual({ id: 1, title: '物品详情' });
    });
  });

  describe('createItem', () => {
    it('should have createItem method', async () => {
      const { useItemStore } = await import('@/store/modules/item');
      const store = useItemStore();
      expect(typeof store.createItem).toBe('function');
    });

    it('should call API createItem', async () => {
      mockCreateItem.mockResolvedValue({
        code: 200,
        data: { id: 1, title: '新物品' },
      });

      const { useItemStore } = await import('@/store/modules/item');
      const store = useItemStore();
      const itemData = { title: '新物品', price: 100, categoryId: 1, description: '测试描述' };
      await store.createItem(itemData);

      expect(mockCreateItem).toHaveBeenCalledWith(itemData);
    });
  });

  describe('updateItem', () => {
    it('should have updateItem method', async () => {
      const { useItemStore } = await import('@/store/modules/item');
      const store = useItemStore();
      expect(typeof store.updateItem).toBe('function');
    });

    it('should call API updateItem', async () => {
      mockUpdateItem.mockResolvedValue({
        code: 200,
        data: { id: 1, title: '更新后的物品' },
      });

      const { useItemStore } = await import('@/store/modules/item');
      const store = useItemStore();
      await store.updateItem(1, { title: '更新后的物品' });

      expect(mockUpdateItem).toHaveBeenCalledWith(1, { title: '更新后的物品' });
    });
  });

  describe('offShelfItem', () => {
    it('should have offShelfItem method', async () => {
      const { useItemStore } = await import('@/store/modules/item');
      const store = useItemStore();
      expect(typeof store.offShelfItem).toBe('function');
    });

    it('should call API offShelf', async () => {
      mockOffShelf.mockResolvedValue({ code: 200 });

      const { useItemStore } = await import('@/store/modules/item');
      const store = useItemStore();
      await store.offShelfItem(1);

      expect(mockOffShelf).toHaveBeenCalledWith(1);
    });
  });

  describe('uploadImage', () => {
    it('should have uploadImage method', async () => {
      const { useItemStore } = await import('@/store/modules/item');
      const store = useItemStore();
      expect(typeof store.uploadImage).toBe('function');
    });

    it('should call API uploadImage', async () => {
      mockUploadImage.mockResolvedValue({
        code: 200,
        data: { url: 'https://example.com/image.jpg' },
      });

      const { useItemStore } = await import('@/store/modules/item');
      const store = useItemStore();
      const formData = new FormData();
      await store.uploadImage(formData);

      expect(mockUploadImage).toHaveBeenCalledWith(formData);
    });
  });
});
