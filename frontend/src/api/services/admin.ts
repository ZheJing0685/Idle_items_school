import { get, post, put, del, getBlob } from '../config/http';
import type { UserInfo, CategoryInfo, OrderInfo } from '../../types/api';

const admin = {
  statistics: {
    getDashboard: (params?: Record<string, any>) =>
      get<any>('/admin/statistics/dashboard', { params }),
    getOverview: (params?: Record<string, any>) =>
      get<any>('/admin/statistics/overview', { params }),
    getMonthly: (params?: Record<string, any>) =>
      get<any>('/admin/statistics/monthly', { params }),
    getCategories: () => get<any[]>('/admin/statistics/categories'),
    getHotItems: (params?: Record<string, any>) =>
      get<any[]>('/admin/statistics/hot-items', { params }),
  },
  users: {
    getUsers: (params?: Record<string, any>) =>
      get<any>('/admin/users', { params }),
    getUser: (userId: number | string) =>
      get<UserInfo>(`/admin/users/${userId}`),
    getUserStats: () => get<{ total: number; active: number; banned: number; verified: number; newThisWeek: number; todayNew?: number }>('/admin/users/stats'),
    createUser: (data: Partial<UserInfo>) => post<UserInfo>('/admin/users', data),
    updateUser: (userId: number | string, data: Partial<UserInfo>) =>
      put<UserInfo>(`/admin/users/${userId}`, data),
    updateStatus: (userId: number | string, status: string) =>
      post<null>(`/admin/users/${userId}/status`, { status }),
    batchUpdateStatus: (userIds: number[], status: string) =>
      post<null>('/admin/batch/users/status', { userIds, status }),
    deleteUsers: (userIds: number[]) =>
      post<null>('/admin/batch/delete', userIds),
    batchDelete: (userIds: number[]) =>
      post<null>('/admin/batch/delete', userIds),
    exportUsers: (params?: Record<string, any>) =>
      getBlob('/admin/users/export', { params }),
  },
  items: {
    getItems: (params?: Record<string, any>) =>
      get<any>('/admin/items', { params }),
    getItemStats: () => get<any>('/admin/items/stats'),
    updateStatus: (itemId: number | string, status: string) =>
      put<null>(`/admin/items/${itemId}/status`, { status }),
    approveItem: (itemId: number | string) =>
      post<null>(`/admin/items/${itemId}/approve`),
    rejectItem: (itemId: number | string, reason: string) =>
      post<null>(`/admin/items/${itemId}/reject`, { reason }),
    offShelfItem: (itemId: number | string, reason?: string) =>
      post<null>(`/admin/items/${itemId}/off-shelf`, { reason }),
    deleteItem: (itemId: number | string) =>
      del<null>(`/admin/items/${itemId}`),
    batchApprove: (itemIds: number[]) =>
      post<null>('/admin/batch/items/approve', { itemIds }),
    batchReject: (itemIds: number[], reason: string) =>
      post<null>('/admin/batch/items/reject', { itemIds, reason }),
    batchOffShelf: (itemIds: number[], reason?: string) =>
      post<null>('/admin/batch/items/off-shelf', { itemIds, reason }),
    exportItems: (params?: Record<string, any>) =>
      getBlob('/admin/items/export', { params }),
  },
  orders: {
    getOrders: (params?: Record<string, any>) =>
      get<any>('/admin/orders', { params }),
    cancelOrder: (orderId: number | string, reason: string) =>
      post<null>(`/admin/orders/${orderId}/cancel`, { reason }),
    approveRefund: (orderId: number | string) =>
      post<null>(`/admin/orders/${orderId}/refund/approve`),
    batchCancel: (orderIds: number[], reason: string) =>
      post<null>('/admin/batch/orders/cancel', { orderIds, reason }),
    getStats: () => get<any>('/admin/orders/stats'),
    getOrder: (orderId: number | string) =>
      get<OrderInfo>('/admin/orders/' + orderId),
  },
  categories: {
    getCategories: (params?: Record<string, any>) =>
      get<any>('/admin/categories', { params }),
    createCategory: (data: Partial<CategoryInfo>) =>
      post<CategoryInfo>('/admin/categories', data),
    updateCategory: (categoryId: number | string, data: Partial<CategoryInfo>) =>
      put<CategoryInfo>(`/admin/categories/${categoryId}`, data),
    updateStatus: (categoryId: number | string, status: boolean) =>
      post<null>(`/admin/categories/${categoryId}/status`, { status }),
    moveUp: (categoryId: number | string) =>
      post<null>(`/admin/categories/${categoryId}/move-up`),
    moveDown: (categoryId: number | string) =>
      post<null>(`/admin/categories/${categoryId}/move-down`),
    deleteCategory: (categoryId: number | string) =>
      del<null>(`/admin/categories/${categoryId}`),
    batchEnable: (categoryIds: number[]) =>
      post<null>('/admin/categories/batch/enable', { categoryIds }),
    batchDisable: (categoryIds: number[]) =>
      post<null>('/admin/categories/batch/disable', { categoryIds }),
    getFeedbacks: (params?: Record<string, any>) =>
      get<any>('/admin/categories/feedback', { params }),
    reviewFeedback: (feedbackId: number | string, data: Record<string, any>) =>
      post<null>(`/admin/categories/feedback/${feedbackId}/review`, data),
    exportCategories: () =>
      getBlob('/admin/categories/export'),
    getCategoryStats: () =>
      get<any>('/admin/categories/stats'),
    getChangeLogs: (params?: Record<string, any>) =>
      get<any>('/admin/categories/change-logs', { params }),
    batchDelete: (categoryIds: number[]) =>
      post<null>('/admin/categories/batch', categoryIds),
    importCategories: (formData: FormData) =>
      post<any>('/admin/categories/import', formData),
  },
  verifications: {
    getVerifications: (params?: Record<string, any>) =>
      get<any>('/admin/verifications', { params }),
    approveVerification: (verificationId: number | string) =>
      post<null>(`/admin/verifications/${verificationId}/approve`),
    rejectVerification: (verificationId: number | string, reason: string) =>
      post<null>(`/admin/verifications/${verificationId}/reject`, { reason }),
    batchApprove: (verificationIds: number[]) =>
      post<null>('/admin/verifications/batch/approve', verificationIds),
    batchReject: (verificationIds: number[], reason: string) =>
      post<null>('/admin/verifications/batch/reject', { verificationIds, reason }),
    getStats: () => get<any>('/admin/verifications/stats'),
  },
  logs: {
    getLogs: (params?: Record<string, any>) =>
      get<any>('/admin/logs', { params }),
    getLogStats: () => get<any>('/admin/logs/analysis'),
    getExport: (params?: Record<string, any>) =>
      getBlob('/admin/logs/export', { params }),
  },
  disputes: {
    list: (params?: Record<string, any>) =>
      get<any>('/admin/disputes', { params }),
    stats: () => get<any>('/admin/disputes/stats'),
    handleDispute: (disputeId: number | string, data: Record<string, any>) =>
      post<null>(`/admin/disputes/${disputeId}/handle`, data),
    batchApprove: (disputeIds: number[]) =>
      post<null>('/admin/disputes/batch/approve', { disputeIds }),
    batchClose: (disputeIds: number[]) =>
      post<null>('/admin/disputes/batch/close', { disputeIds }),
    export: (params?: Record<string, any>) =>
      getBlob('/admin/disputes/export', { params }),
  },
};

export default admin;
