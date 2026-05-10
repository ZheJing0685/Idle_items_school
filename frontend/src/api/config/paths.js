// API路径统一管理
export const API_PATHS = {
  // 认证相关
  AUTH: {
    LOGIN: '/auth/login',
    REGISTER: '/auth/register',
    ME: '/auth/me',
    REFRESH: '/auth/refresh',
  },

  // 物品相关
  ITEM: {
    LIST: '/items',
    HOT: '/items/hot',
    SEARCH: '/items/search',
    DETAIL: (id) => `/items/${id}`,
    CREATE: '/items',
    UPDATE: (id) => `/items/${id}`,
    OFF_SHELF: (id) => `/items/${id}/off-shelf`,
    ON_SHELF: (id) => `/items/${id}/on-shelf`,
    UPLOAD: '/items/upload',
    UPLOAD_CHUNK: '/items/upload/chunk',
    UPLOAD_COMPLETE: '/items/upload/complete',
    UPLOAD_CHECK: '/items/upload/check',
    ORDERS: (id) => `/items/${id}/orders`,
    ACTIVE_ORDERS: (id) => `/items/${id}/active-orders`,
  },

  // 分类相关
  CATEGORY: {
    LIST: '/categories',
    TREE: '/categories/tree',
    SEARCH: '/categories/search',
    FEEDBACK: '/categories/feedback',
    MY_FEEDBACK: '/categories/feedback/my',
  },

  // 收藏相关
  FAVORITE: {
    LIST: '/favorites',
    ADD: '/favorites',
    REMOVE: (id) => `/favorites/${id}`,
  },

  // 订单相关
  ORDER: {
    CREATE: '/orders',
    LIST: '/orders',
    DETAIL: (id) => `/orders/${id}`,
    CANCEL: (id) => `/orders/${id}/cancel`,
    PAY: (id) => `/orders/${id}/pay`,
    SHIP: (id) => `/orders/${id}/ship`,
    CONFIRM: (id) => `/orders/${id}/confirm-receive`,
    REFUND: (id) => `/orders/${id}/refund`,
  },

  // 评价相关
  REVIEW: {
    CREATE: '/reviews',
    LIST: (itemId) => `/reviews/item/${itemId}`,
    USER: '/reviews/user',
  },

  // 用户相关
  USER: {
    PROFILE: '/user/profile',
    UPDATE: '/user/profile',
    VERIFICATION: '/user/verification',
  },

  // 管理员相关
  ADMIN: {
    DASHBOARD: '/admin/dashboard',
    USERS: '/admin/users',
    CREATE_USER: '/admin/users',
    UPDATE_USER: (id) => `/admin/users/${id}`,
    EXPORT_USERS: '/admin/users/export',
    DELETE_USER: (id) => `/admin/users/${id}`,
    BATCH_DELETE_USERS: '/admin/batch/users/delete',
    ITEMS: '/admin/items',
    EXPORT_ITEMS: '/admin/items/export',
    ORDERS: '/admin/orders',
    CATEGORIES: '/admin/categories',
    STATISTICS: '/admin/statistics',
    LOGS: '/admin/logs',
    EXPORT_LOGS: '/admin/logs/export',
    DISPUTES: '/admin/disputes',
    DISPUTE_STATS: '/admin/disputes/stats',
    HANDLE_DISPUTE: (id) => `/admin/disputes/${id}/handle`,
  },
};

export default API_PATHS;
