import instance from '../config/axios';

const admin = {
  statistics: {
    getDashboard: (params) => instance.get('/admin/statistics/dashboard', { params }),
    getOverview: (params) => instance.get('/admin/statistics/overview', { params }),
    getMonthly: (params) => instance.get('/admin/statistics/monthly', { params }),
    getCategories: () => instance.get('/admin/statistics/categories'),
    getHotItems: (params) => instance.get('/admin/statistics/hot-items', { params }),
  },
  users: {
    getUsers: (params) => instance.get('/admin/users', { params }),
    getUser: (userId) => instance.get(`/admin/users/${userId}`),
    getUserStats: () => instance.get('/admin/users/stats'),
    updateStatus: (userId, status) => instance.put(`/admin/users/${userId}/status`, { status }),
    deleteUser: (userId) => instance.delete(`/admin/users/${userId}`),
    batchUpdateStatus: (userIds, status) => instance.put('/admin/users/batch/status', { userIds, status }),
    batchDelete: (userIds) => instance.post('/admin/users/batch/delete', { userIds }),
  },
  items: {
    getItems: (params) => instance.get('/admin/items', { params }),
    getItemStats: () => instance.get('/admin/items/stats'),
    updateStatus: (itemId, status) => instance.put(`/admin/items/${itemId}/status`, { status }),
    approve: (itemId) => instance.put(`/admin/items/${itemId}/approve`),
    reject: (itemId, reason) => instance.put(`/admin/items/${itemId}/reject`, { reason }),
    offShelf: (itemId, reason) => instance.put(`/admin/items/${itemId}/off-shelf`, { reason }),
    deleteItem: (itemId) => instance.delete(`/admin/items/${itemId}`),
    batchApprove: (itemIds) => instance.put('/admin/items/batch/approve', { itemIds }),
    batchReject: (itemIds, reason) => instance.put('/admin/items/batch/reject', { itemIds, reason }),
    batchOffShelf: (itemIds, reason) => instance.put('/admin/items/batch/off-shelf', { itemIds, reason }),
  },
  orders: {
    getOrders: (params) => instance.get('/admin/orders', { params }),
    getStats: () => instance.get('/admin/orders/stats'),
    getOrder: (orderId) => instance.get(`/admin/orders/${orderId}`),
    cancelOrder: (orderId, reason) => instance.put(`/admin/orders/${orderId}/cancel`, { reason }),
    approveRefund: (orderId) => instance.put(`/admin/orders/${orderId}/refund/approve`),
    batchCancelOrders: (orderIds, reason) => instance.put('/admin/orders/batch/cancel', { orderIds, reason }),
  },
  categories: {
    getCategories: (params) => instance.get('/admin/categories', { params }),
    getCategory: (categoryId) => instance.get(`/admin/categories/${categoryId}`),
    getCategoryStats: () => instance.get('/admin/categories/stats'),
    create: (data) => instance.post('/admin/categories', data),
    update: (categoryId, data) => instance.put(`/admin/categories/${categoryId}`, data),
    updateStatus: (categoryId, status) => instance.put(`/admin/categories/${categoryId}/status`, { status }),
    moveUp: (categoryId) => instance.put(`/admin/categories/${categoryId}/move-up`),
    moveDown: (categoryId) => instance.put(`/admin/categories/${categoryId}/move-down`),
    deleteCategory: (categoryId) => instance.delete(`/admin/categories/${categoryId}`),
    batchEnable: (categoryIds) => instance.put('/admin/categories/batch/enable', { categoryIds }),
    batchDisable: (categoryIds) => instance.put('/admin/categories/batch/disable', { categoryIds }),
    batchDelete: (categoryIds) => instance.post('/admin/categories/batch/delete', { categoryIds }),
  },
  verifications: {
    getVerifications: (params) => instance.get('/admin/verifications', { params }),
    getStats: () => instance.get('/admin/verifications/stats'),
    approve: (verificationId) => instance.put(`/admin/verifications/${verificationId}/approve`),
    reject: (verificationId, reason) => instance.put(`/admin/verifications/${verificationId}/reject`, { reason }),
    batchApprove: (verificationIds) => instance.put('/admin/verifications/batch/approve', { verificationIds }),
    batchReject: (verificationIds, reason) => instance.put('/admin/verifications/batch/reject', { verificationIds, reason }),
  },
  logs: {
    getLogs: (params) => instance.get('/admin/logs', { params }),
    getLog: (logId) => instance.get(`/admin/logs/${logId}`),
    getExport: (params) => instance.get('/admin/logs/export', { params, responseType: 'blob' }),
  },
};

export default admin;