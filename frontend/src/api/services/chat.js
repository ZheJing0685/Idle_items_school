import axios from '../config/axios';

const chat = {
  // 获取聊天会话列表
  getChats: (params) => axios.get('/chats', { params }),
  
  // 创建聊天会话
  createChat: (sellerId, itemId) => axios.post('/chats', null, { 
    params: { sellerId, itemId } 
  }),
  
  // 获取聊天消息列表
  getMessages: (chatId, params) => axios.get(`/chats/${chatId}/messages`, { params }),
  
  // 发送消息（HTTP方式）
  sendMessage: (chatId, receiverId, content) => axios.post(`/chats/${chatId}/messages`, null, {
    params: { receiverId, content }
  })
};

export default chat;
