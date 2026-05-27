import { describe, it, expect, vi, beforeEach } from 'vitest';

const mockGet = vi.fn();
const mockPost = vi.fn();
const mockPut = vi.fn();
const mockDel = vi.fn();

vi.mock('@/api/config/http', () => ({
  get: (...args) => mockGet(...args),
  post: (...args) => mockPost(...args),
  put: (...args) => mockPut(...args),
  del: (...args) => mockDel(...args),
}));

vi.mock('@/utils/network/requestManager', () => ({
  default: {
    request: vi.fn((_url, requestFn) => requestFn()),
  },
}));

import itemApi from '@/api/services/item';

describe('Item API', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('getItems', () => {
    it('should call get with correct url and params', async () => {
      const mockResponse = { code: 200, data: { records: [], total: 0 } };
      mockGet.mockResolvedValue(mockResponse);

      const result = await itemApi.getItems({ page: 1, size: 10 });

      expect(mockGet).toHaveBeenCalledWith('/items', {
        params: { page: 1, size: 10 },
      });
      expect(result).toEqual(mockResponse);
    });

    it('should call get without params', async () => {
      const mockResponse = { code: 200, data: { records: [], total: 0 } };
      mockGet.mockResolvedValue(mockResponse);

      await itemApi.getItems();

      expect(mockGet).toHaveBeenCalledWith('/items', { params: undefined });
    });
  });

  describe('getHotItems', () => {
    it('should call get with correct url', async () => {
      const mockResponse = { code: 200, data: [{ id: 1, title: 'Hot Item' }] };
      mockGet.mockResolvedValue(mockResponse);

      const result = await itemApi.getHotItems();

      expect(mockGet).toHaveBeenCalledWith('/items/hot');
      expect(result).toEqual(mockResponse);
    });
  });

  describe('searchItems', () => {
    it('should call get with correct url and search params', async () => {
      const mockResponse = { code: 200, data: { records: [], total: 0 } };
      mockGet.mockResolvedValue(mockResponse);

      const result = await itemApi.searchItems('手机', 1, 10, 'price');

      expect(mockGet).toHaveBeenCalledWith('/items/search', {
        params: { keyword: '手机', page: 1, size: 10, sortBy: 'price' },
      });
      expect(result).toEqual(mockResponse);
    });
  });

  describe('getItem', () => {
    it('should call get with correct url for item detail', async () => {
      const mockResponse = { code: 200, data: { id: 1, title: 'Test Item' } };
      mockGet.mockResolvedValue(mockResponse);

      const result = await itemApi.getItem(1);

      expect(mockGet).toHaveBeenCalledWith('/items/1');
      expect(result).toEqual(mockResponse);
    });

    it('should handle string id', async () => {
      const mockResponse = {
        code: 200,
        data: { id: 'abc', title: 'Test Item' },
      };
      mockGet.mockResolvedValue(mockResponse);

      await itemApi.getItem('abc');

      expect(mockGet).toHaveBeenCalledWith('/items/abc');
    });
  });

  describe('createItem', () => {
    it('should call post with correct url and data', async () => {
      const mockResponse = { code: 200, data: { id: 1, title: 'New Item' } };
      mockPost.mockResolvedValue(mockResponse);

      const itemData = { title: 'New Item', price: 99.9, categoryId: 1 };
      const result = await itemApi.createItem(itemData);

      expect(mockPost).toHaveBeenCalledWith('/items', itemData);
      expect(result).toEqual(mockResponse);
    });
  });

  describe('updateItem', () => {
    it('should call put with correct url and data', async () => {
      const mockResponse = { code: 200, data: { id: 1, title: 'Updated' } };
      mockPut.mockResolvedValue(mockResponse);

      const updateData = { title: 'Updated', price: 88.8 };
      const result = await itemApi.updateItem(1, updateData);

      expect(mockPut).toHaveBeenCalledWith('/items/1', updateData);
      expect(result).toEqual(mockResponse);
    });
  });

  describe('offShelf', () => {
    it('should call post with correct url', async () => {
      const mockResponse = { code: 200, data: null };
      mockPost.mockResolvedValue(mockResponse);

      const result = await itemApi.offShelf(1);

      expect(mockPost).toHaveBeenCalledWith('/items/1/off-shelf');
      expect(result).toEqual(mockResponse);
    });
  });

  describe('onShelf', () => {
    it('should call post with correct url', async () => {
      const mockResponse = { code: 200, data: null };
      mockPost.mockResolvedValue(mockResponse);

      const result = await itemApi.onShelf(1);

      expect(mockPost).toHaveBeenCalledWith('/items/1/on-shelf');
      expect(result).toEqual(mockResponse);
    });
  });

  describe('uploadImage', () => {
    it('should call post with FormData', async () => {
      const mockResponse = {
        code: 200,
        data: { url: 'https://example.com/image.jpg' },
      };
      mockPost.mockResolvedValue(mockResponse);

      const formData = new FormData();
      formData.append('file', 'test.jpg');
      const result = await itemApi.uploadImage(formData);

      expect(mockPost).toHaveBeenCalledWith('/items/upload', formData);
      expect(result).toEqual(mockResponse);
    });
  });

  describe('uploadChunk', () => {
    it('should call post with chunk data', async () => {
      const mockResponse = { code: 200, data: { uploaded: true } };
      mockPost.mockResolvedValue(mockResponse);

      const formData = new FormData();
      const result = await itemApi.uploadChunk(formData);

      expect(mockPost).toHaveBeenCalledWith('/items/upload/chunk', formData);
      expect(result).toEqual(mockResponse);
    });
  });

  describe('completeUpload', () => {
    it('should call post with upload completion data', async () => {
      const mockResponse = {
        code: 200,
        data: { url: 'https://example.com/file.pdf' },
      };
      mockPost.mockResolvedValue(mockResponse);

      const uploadData = {
        fileHash: 'abc123',
        uploadId: 'upload-1',
        fileName: 'file.pdf',
        totalChunks: 5,
      };
      const result = await itemApi.completeUpload(uploadData);

      expect(mockPost).toHaveBeenCalledWith(
        '/items/upload/complete',
        uploadData,
      );
      expect(result).toEqual(mockResponse);
    });
  });

  describe('checkUploadedChunks', () => {
    it('should call get with correct params', async () => {
      const mockResponse = { code: 200, data: { uploadedChunks: [0, 1, 2] } };
      mockGet.mockResolvedValue(mockResponse);

      const result = await itemApi.checkUploadedChunks('abc123', 'upload-1');

      expect(mockGet).toHaveBeenCalledWith('/items/upload/check', {
        params: { fileHash: 'abc123', uploadId: 'upload-1' },
      });
      expect(result).toEqual(mockResponse);
    });
  });

  describe('getItemOrders', () => {
    it('should call get with correct url', async () => {
      const mockResponse = { code: 200, data: [{ id: 1 }] };
      mockGet.mockResolvedValue(mockResponse);

      const result = await itemApi.getItemOrders(1);

      expect(mockGet).toHaveBeenCalledWith('/items/1/orders');
      expect(result).toEqual(mockResponse);
    });
  });

  describe('getItemActiveOrders', () => {
    it('should call get with correct url', async () => {
      const mockResponse = { code: 200, data: [{ id: 1 }] };
      mockGet.mockResolvedValue(mockResponse);

      const result = await itemApi.getItemActiveOrders(1);

      expect(mockGet).toHaveBeenCalledWith('/items/1/active-orders');
      expect(result).toEqual(mockResponse);
    });
  });

  describe('deleteItem', () => {
    it('should call del with correct url', async () => {
      const mockResponse = { code: 200, data: null };
      mockDel.mockResolvedValue(mockResponse);

      const result = await itemApi.deleteItem(1);

      expect(mockDel).toHaveBeenCalledWith('/items/1');
      expect(result).toEqual(mockResponse);
    });
  });

  describe('error handling', () => {
    it('should propagate errors from http methods', async () => {
      const error = new Error('Network error');
      mockGet.mockRejectedValue(error);

      await expect(itemApi.getItems()).rejects.toThrow('Network error');
    });
  });
});
