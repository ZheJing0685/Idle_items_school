import axios from '../config/axios';

const notification = {
  // 获取通知列表
  getNotifications: (params) => axios.get('/notifications', { params }),
  
  // 获取未读通知数量
  getUnreadCount: () => axios.get('/notifications/unread-count'),
  
  // 标记单条为已读
  markAsRead: (id) => axios.put(`/notifications/${id}/read`),
  
  // 标记所有为已读
  markAllAsRead: () => axios.put('/notifications/read-all'),
  
  // 删除通知
  deleteNotification: (id) => axios.delete(`/notifications/${id}`)
};

export default notification;
