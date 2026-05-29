import { describe, it, expect, vi, beforeEach } from 'vitest';

const { mockGet, mockPost } = vi.hoisted(() => ({
  mockGet: vi.fn(),
  mockPost: vi.fn(),
}));

vi.mock('@/api/config/http', () => ({
  get: (...args: any[]) => mockGet(...args),
  post: (...args: any[]) => mockPost(...args),
}));

import chatApi from '@/api/services/chat';

describe('Chat API', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('getChats', () => {
    it('should call get with correct url', async () => {
      const mockResponse = {
        code: 200,
        data: [{ id: 1, sellerName: '卖家', lastMessage: '你好' }],
      };
      mockGet.mockResolvedValue(mockResponse);

      const result = await chatApi.getChats();

      expect(mockGet).toHaveBeenCalledWith('/chats', { params: undefined });
      expect(result).toEqual(mockResponse);
    });

    it('should call get with params', async () => {
      const mockResponse = { code: 200, data: [] };
      mockGet.mockResolvedValue(mockResponse);

      await chatApi.getChats({ page: 1, size: 10 });

      expect(mockGet).toHaveBeenCalledWith('/chats', { params: { page: 1, size: 10 } });
    });

    it('should throw error when fetch fails', async () => {
      const error = new Error('获取聊天列表失败');
      mockGet.mockRejectedValue(error);

      await expect(chatApi.getChats()).rejects.toThrow('获取聊天列表失败');
    });
  });

  describe('createChat', () => {
    it('should call post with correct url and params', async () => {
      const mockResponse = {
        code: 200,
        data: { id: 1, sellerId: 2, itemId: 3 },
      };
      mockPost.mockResolvedValue(mockResponse);

      const result = await chatApi.createChat(2, 3);

      expect(mockPost).toHaveBeenCalledWith('/chats', null, { params: { sellerId: 2, itemId: 3 } });
      expect(result).toEqual(mockResponse);
    });

    it('should handle string ids', async () => {
      const mockResponse = { code: 200, data: { id: 1 } };
      mockPost.mockResolvedValue(mockResponse);

      await chatApi.createChat('seller-abc', 'item-abc');

      expect(mockPost).toHaveBeenCalledWith('/chats', null, {
        params: { sellerId: 'seller-abc', itemId: 'item-abc' },
      });
    });

    it('should throw error when create fails', async () => {
      const error = new Error('创建聊天失败');
      mockPost.mockRejectedValue(error);

      await expect(chatApi.createChat(1, 1)).rejects.toThrow('创建聊天失败');
    });
  });

  describe('getMessages', () => {
    it('should call get with correct url', async () => {
      const mockResponse = {
        code: 200,
        data: [{ id: 1, content: '你好', senderId: 1 }],
      };
      mockGet.mockResolvedValue(mockResponse);

      const result = await chatApi.getMessages(1);

      expect(mockGet).toHaveBeenCalledWith('/chats/1/messages', { params: undefined });
      expect(result).toEqual(mockResponse);
    });

    it('should handle string chat id', async () => {
      const mockResponse = { code: 200, data: [] };
      mockGet.mockResolvedValue(mockResponse);

      await chatApi.getMessages('chat-abc');

      expect(mockGet).toHaveBeenCalledWith('/chats/chat-abc/messages', { params: undefined });
    });

    it('should call get with params', async () => {
      const mockResponse = { code: 200, data: [] };
      mockGet.mockResolvedValue(mockResponse);

      await chatApi.getMessages(1, { page: 1, size: 20 });

      expect(mockGet).toHaveBeenCalledWith('/chats/1/messages', { params: { page: 1, size: 20 } });
    });

    it('should throw error when fetch fails', async () => {
      const error = new Error('获取消息失败');
      mockGet.mockRejectedValue(error);

      await expect(chatApi.getMessages(1)).rejects.toThrow('获取消息失败');
    });
  });

  describe('sendMessage', () => {
    it('should call post with correct url and params', async () => {
      const mockResponse = {
        code: 200,
        data: { id: 1, content: '你好', senderId: 1 },
      };
      mockPost.mockResolvedValue(mockResponse);

      const result = await chatApi.sendMessage(1, 2, '你好');

      expect(mockPost).toHaveBeenCalledWith('/chats/1/messages', null, {
        params: { receiverId: 2, content: '你好' },
      });
      expect(result).toEqual(mockResponse);
    });

    it('should handle string ids', async () => {
      const mockResponse = { code: 200, data: { id: 1 } };
      mockPost.mockResolvedValue(mockResponse);

      await chatApi.sendMessage('chat-abc', 'receiver-abc', '测试消息');

      expect(mockPost).toHaveBeenCalledWith('/chats/chat-abc/messages', null, {
        params: { receiverId: 'receiver-abc', content: '测试消息' },
      });
    });

    it('should throw error when send fails', async () => {
      const error = new Error('发送消息失败');
      mockPost.mockRejectedValue(error);

      await expect(chatApi.sendMessage(1, 2, '你好')).rejects.toThrow('发送消息失败');
    });
  });
});
