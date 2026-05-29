import { describe, it, expect, vi, beforeEach } from 'vitest';

const { mockGet, mockPost } = vi.hoisted(() => ({
  mockGet: vi.fn(),
  mockPost: vi.fn(),
}));

vi.mock('@/api/config/http', () => ({
  get: (...args: any[]) => mockGet(...args),
  post: (...args: any[]) => mockPost(...args),
}));

vi.mock('@/utils/network/requestManager', () => ({
  default: {
    request: vi.fn((_url: string, requestFn: () => any) => requestFn()),
  },
}));

import orderApi from '@/api/services/order';

describe('Order API (TypeScript)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('createOrder', () => {
    it('should call post with correct url and data', async () => {
      const mockResponse = { code: 200, data: { id: 1, status: 'PENDING' } };
      mockPost.mockResolvedValue(mockResponse);

      const orderData = { itemId: 1, quantity: 1, message: '想要这个' };
      const result = await orderApi.createOrder(orderData);

      expect(mockPost).toHaveBeenCalledWith('/orders', orderData);
      expect(result).toEqual(mockResponse);
    });

    it('should throw error when create fails', async () => {
      const error = new Error('创建订单失败');
      mockPost.mockRejectedValue(error);

      await expect(orderApi.createOrder({})).rejects.toThrow('创建订单失败');
    });
  });

  describe('getBuyerOrders', () => {
    it('should call get with correct url and params', async () => {
      const mockResponse = { code: 200, data: { records: [], total: 0 } };
      mockGet.mockResolvedValue(mockResponse);

      const result = await orderApi.getBuyerOrders({ page: 1, size: 10 });

      expect(mockGet).toHaveBeenCalledWith('/orders', {
        params: { page: 1, size: 10 },
      });
      expect(result).toEqual(mockResponse);
    });

    it('should call get without params', async () => {
      const mockResponse = { code: 200, data: { records: [], total: 0 } };
      mockGet.mockResolvedValue(mockResponse);

      await orderApi.getBuyerOrders();

      expect(mockGet).toHaveBeenCalledWith('/orders', { params: undefined });
    });
  });

  describe('getSellerOrders', () => {
    it('should call get with correct url and params', async () => {
      const mockResponse = { code: 200, data: { records: [], total: 0 } };
      mockGet.mockResolvedValue(mockResponse);

      const result = await orderApi.getSellerOrders({ page: 1, size: 10 });

      expect(mockGet).toHaveBeenCalledWith('/orders/seller', {
        params: { page: 1, size: 10 },
      });
      expect(result).toEqual(mockResponse);
    });
  });

  describe('getOrder', () => {
    it('should call get with correct url', async () => {
      const mockResponse = { code: 200, data: { id: 1, status: 'PENDING' } };
      mockGet.mockResolvedValue(mockResponse);

      const result = await orderApi.getOrder(1);

      expect(mockGet).toHaveBeenCalledWith('/orders/1');
      expect(result).toEqual(mockResponse);
    });

    it('should handle string id', async () => {
      const mockResponse = { code: 200, data: { id: 'order-abc' } };
      mockGet.mockResolvedValue(mockResponse);

      await orderApi.getOrder('order-abc');

      expect(mockGet).toHaveBeenCalledWith('/orders/order-abc');
    });
  });

  describe('payOrder', () => {
    it('should call post with correct url and payment method', async () => {
      const mockResponse = { code: 200, data: null };
      mockPost.mockResolvedValue(mockResponse);

      const result = await orderApi.payOrder(1, 'WECHAT');

      expect(mockPost).toHaveBeenCalledWith('/orders/1/pay', null, {
        params: { paymentMethod: 'WECHAT' },
      });
      expect(result).toEqual(mockResponse);
    });

    it('should default to OFFLINE payment method', async () => {
      const mockResponse = { code: 200, data: null };
      mockPost.mockResolvedValue(mockResponse);

      await orderApi.payOrder(1);

      expect(mockPost).toHaveBeenCalledWith('/orders/1/pay', null, {
        params: { paymentMethod: 'OFFLINE' },
      });
    });
  });

  describe('cancelOrder', () => {
    it('should call post with correct url and reason', async () => {
      const mockResponse = { code: 200, data: null };
      mockPost.mockResolvedValue(mockResponse);

      const result = await orderApi.cancelOrder(1, '不想买了');

      expect(mockPost).toHaveBeenCalledWith('/orders/1/cancel', {
        reason: '不想买了',
      });
      expect(result).toEqual(mockResponse);
    });

    it('should default to user cancellation reason', async () => {
      const mockResponse = { code: 200, data: null };
      mockPost.mockResolvedValue(mockResponse);

      await orderApi.cancelOrder(1);

      expect(mockPost).toHaveBeenCalledWith('/orders/1/cancel', {
        reason: '用户主动取消',
      });
    });
  });

  describe('shipOrder', () => {
    it('should call post with correct url', async () => {
      const mockResponse = { code: 200, data: null };
      mockPost.mockResolvedValue(mockResponse);

      const result = await orderApi.shipOrder(1);

      expect(mockPost).toHaveBeenCalledWith('/orders/1/ship');
      expect(result).toEqual(mockResponse);
    });
  });

  describe('confirmReceive', () => {
    it('should call post with correct url', async () => {
      const mockResponse = { code: 200, data: null };
      mockPost.mockResolvedValue(mockResponse);

      const result = await orderApi.confirmReceive(1);

      expect(mockPost).toHaveBeenCalledWith('/orders/1/confirm-receive');
      expect(result).toEqual(mockResponse);
    });
  });

  describe('applyRefund', () => {
    it('should call post with correct url and refund data', async () => {
      const mockResponse = { code: 200, data: null };
      mockPost.mockResolvedValue(mockResponse);

      const refundData = {
        reason: '商品与描述不符',
        description: '有明显划痕',
        evidenceImages: ['img1.jpg'],
      };
      const result = await orderApi.applyRefund(1, refundData);

      expect(mockPost).toHaveBeenCalledWith('/orders/1/refund', refundData);
      expect(result).toEqual(mockResponse);
    });
  });

  describe('error handling', () => {
    it('should propagate errors from http methods', async () => {
      const error = new Error('Network error');
      mockGet.mockRejectedValue(error);

      await expect(orderApi.getBuyerOrders()).rejects.toThrow('Network error');
    });

    it('should propagate errors from post', async () => {
      const error = new Error('Server error');
      mockPost.mockRejectedValue(error);

      await expect(orderApi.createOrder({})).rejects.toThrow('Server error');
    });
  });
});
