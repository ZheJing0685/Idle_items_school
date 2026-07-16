import { get, post } from '../config/http';
import type { ChatInfo, ChatMessage } from '../../types/api';

const chat = {
  getChats: (params?: Record<string, unknown>) =>
    get<ChatInfo[] | { content: ChatInfo[]; totalElements: number }>('/chats', { params }),

  createChat: (sellerId: number | string, itemId: number | string) =>
    post<ChatInfo>('/chats', null, { params: { sellerId, itemId } }),

  getMessages: (chatId: number | string, params?: Record<string, unknown>) =>
    get<ChatMessage[] | { content: ChatMessage[]; totalElements: number }>(`/chats/${chatId}/messages`, { params }),

  sendMessage: (chatId: number | string, receiverId: number | string, content: string, messageType: string = 'TEXT') =>
    post<ChatMessage>(`/chats/${chatId}/messages`, null, {
      params: { receiverId, content, messageType },
    }),

  uploadChatMedia: (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return post<{ url: string; fileName: string; size: number; mediaType: string }>('/upload/chat-media', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
};

export default chat;
