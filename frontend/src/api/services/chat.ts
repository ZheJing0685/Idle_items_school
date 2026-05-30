import { get, post } from '../config/http';
import type { ChatInfo, ChatMessage } from '../../types/api';

const chat = {
  getChats: (params?: Record<string, any>) =>
    get<ChatInfo[] | { content: ChatInfo[]; totalElements: number }>('/chats', { params }),

  createChat: (sellerId: number | string, itemId: number | string) =>
    post<ChatInfo>('/chats', null, { params: { sellerId, itemId } }),

  getMessages: (chatId: number | string, params?: Record<string, any>) =>
    get<ChatMessage[] | { content: ChatMessage[]; totalElements: number }>(`/chats/${chatId}/messages`, { params }),

  sendMessage: (chatId: number | string, receiverId: number | string, content: string) =>
    post<ChatMessage>(`/chats/${chatId}/messages`, null, {
      params: { receiverId, content },
    }),
};

export default chat;
