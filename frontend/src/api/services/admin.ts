import { get, post, put, del, getBlob } from '../config/http';
import type { UserInfo, CategoryInfo, OrderInfo } from '../../types/api';

const admin = {
  statistics: {
    getDashboard: (params?: Record<string, unknown>) =>
      get<unknown>('/admin/statistics/dashboard', { params }),
    getOverview: (params?: Record<string, unknown>) =>
      get<unknown>('/admin/statistics/overview', { params }),
    getMonthly: (params?: Record<string, unknown>) =>
      get<unknown>('/admin/statistics/monthly', { params }),
    getCategories: () => get<unknown[]>('/admin/statistics/categories'),
    getHotItems: (params?: Record<string, unknown>) =>
      get<unknown[]>('/admin/statistics/hot-items', { params }),
  },
  users: {
    getUsers: (params?: Record<string, unknown>) =>
      get<unknown>('/admin/users', { params }),
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
    exportUsers: (params?: Record<string, unknown>) =>
      getBlob('/admin/users/export', { params }),
  },
  items: {
    getItems: (params?: Record<string, unknown>) =>
      get<unknown>('/admin/items', { params }),
    getItemStats: () => get<unknown>('/admin/items/stats'),
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
    exportItems: (params?: Record<string, unknown>) =>
      getBlob('/admin/items/export', { params }),
  },
  orders: {
    getOrders: (params?: Record<string, unknown>) =>
      get<unknown>('/admin/orders', { params }),
    cancelOrder: (orderId: number | string, reason: string) =>
      post<null>(`/admin/orders/${orderId}/cancel`, { reason }),
    approveRefund: (orderId: number | string) =>
      post<null>(`/admin/orders/${orderId}/refund/approve`),
    batchCancel: (orderIds: number[], reason: string) =>
      post<null>('/admin/batch/orders/cancel', { orderIds, reason }),
    getStats: () => get<unknown>('/admin/orders/stats'),
    getOrder: (orderId: number | string) =>
      get<OrderInfo>('/admin/orders/' + orderId),
  },
  categories: {
    getCategories: (params?: Record<string, unknown>) =>
      get<unknown>('/admin/categories', { params }),
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
    getFeedbacks: (params?: Record<string, unknown>) =>
      get<unknown>('/admin/categories/feedback', { params }),
    reviewFeedback: (feedbackId: number | string, data: Record<string, unknown>) =>
      post<null>(`/admin/categories/feedback/${feedbackId}/review`, data),
    exportCategories: () =>
      getBlob('/admin/categories/export'),
    getCategoryStats: () =>
      get<unknown>('/admin/categories/stats'),
    getChangeLogs: (params?: Record<string, unknown>) =>
      get<unknown>('/admin/categories/change-logs', { params }),
    batchDelete: (categoryIds: number[]) =>
      post<null>('/admin/categories/batch', categoryIds),
    importCategories: (formData: FormData) =>
      post<unknown>('/admin/categories/import', formData),
  },
  verifications: {
    getVerifications: (params?: Record<string, unknown>) =>
      get<unknown>('/admin/verifications', { params }),
    approveVerification: (verificationId: number | string) =>
      post<null>(`/admin/verifications/${verificationId}/approve`),
    rejectVerification: (verificationId: number | string, reason: string) =>
      post<null>(`/admin/verifications/${verificationId}/reject`, null, { params: { reason } }),
    batchApprove: (verificationIds: number[]) =>
      post<null>('/admin/verifications/batch/approve', verificationIds),
    batchReject: (verificationIds: number[], reason: string) =>
      post<null>('/admin/verifications/batch/reject', { verificationIds, reason }),
    getStats: () => get<unknown>('/admin/verifications/stats'),
  },
  logs: {
    getLogs: (params?: Record<string, unknown>) =>
      get<unknown>('/admin/logs', { params }),
    getLogStats: () => get<unknown>('/admin/logs/analysis'),
    getExport: (params?: Record<string, unknown>) =>
      getBlob('/admin/logs/export', { params }),
  },
  disputes: {
    list: (params?: Record<string, unknown>) =>
      get<unknown>('/admin/disputes', { params }),
    stats: () => get<unknown>('/admin/disputes/stats'),
    handleDispute: (disputeId: number | string, data: Record<string, unknown>) =>
      post<null>(`/admin/disputes/${disputeId}/handle`, data),
    batchApprove: (disputeIds: number[]) =>
      post<null>('/admin/disputes/batch/approve', { disputeIds }),
    batchClose: (disputeIds: number[]) =>
      post<null>('/admin/disputes/batch/close', { disputeIds }),
    export: (params?: Record<string, unknown>) =>
      getBlob('/admin/disputes/export', { params }),
  },
};

export default admin;
