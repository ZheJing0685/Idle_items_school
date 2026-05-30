import { get, post, del } from '../config/http';
import type { NotificationInfo } from '../../types/api';

const notification = {
  getNotifications: (params?: Record<string, any>) =>
    get<{ content: NotificationInfo[]; totalElements: number }>('/notifications', { params }),

  getUnreadCount: () =>
    get<{ count: number }>('/notifications/unread-count'),

  markAsRead: (id: number | string) =>
    post<null>(`/notifications/${id}/read`),

  markAllAsRead: () =>
    post<null>('/notifications/read-all'),

  deleteNotification: (id: number | string) =>
    del<null>(`/notifications/${id}`),
};

export default notification;
