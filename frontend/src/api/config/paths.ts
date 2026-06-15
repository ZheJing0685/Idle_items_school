export const API_PATHS = {
  AUTH: {
    LOGIN: '/auth/login',
    REGISTER: '/auth/register',
    ME: '/auth/me',
    REFRESH: '/auth/refresh',
    FORGOT_PASSWORD: '/auth/forgot-password',
    VERIFY_CODE: '/auth/verify-code',
    RESET_PASSWORD: '/auth/reset-password',
    CHANGE_PASSWORD: '/auth/change-password',
    LOGOUT: '/auth/logout',
  },

  ITEM: {
    LIST: '/items',
    HOT: '/items/hot',
    SEARCH: '/items/search',
    DETAIL: (id: number | string) => `/items/${id}`,
    CREATE: '/items',
    UPDATE: (id: number | string) => `/items/${id}`,
    OFF_SHELF: (id: number | string) => `/items/${id}/off-shelf`,
    ON_SHELF: (id: number | string) => `/items/${id}/on-shelf`,
    UPLOAD: '/items/upload',
    UPLOAD_CHUNK: '/items/upload/chunk',
    UPLOAD_COMPLETE: '/items/upload/complete',
    UPLOAD_CHECK: '/items/upload/check',
    ORDERS: (id: number | string) => `/items/${id}/orders`,
    ACTIVE_ORDERS: (id: number | string) => `/items/${id}/active-orders`,
    RELATED: (id: number | string) => `/items/${id}/related`,
    RECOMMENDED: '/items/recommended',
  },

  CATEGORY: {
    LIST: '/categories',
    TREE: '/categories/tree',
    SEARCH: '/categories/search',
    FEEDBACK: '/categories/feedback',
    MY_FEEDBACK: '/categories/feedback/my',
  },

  FAVORITE: {
    LIST: '/favorites',
    ADD: '/favorites',
    REMOVE: (id: number | string) => `/favorites/${id}`,
  },

  ORDER: {
    CREATE: '/orders',
    LIST: '/orders',
    DETAIL: (id: number | string) => `/orders/${id}`,
    CANCEL: (id: number | string) => `/orders/${id}/cancel`,
    PAY: (id: number | string) => `/orders/${id}/pay`,
    SHIP: (id: number | string) => `/orders/${id}/ship`,
    CONFIRM: (id: number | string) => `/orders/${id}/confirm-receive`,
    REFUND: (id: number | string) => `/orders/${id}/refund`,
  },

  REVIEW: {
    CREATE: '/reviews',
    LIST: (itemId: number | string) => `/reviews/item/${itemId}`,
    USER: '/reviews/user',
  },

  USER: {
    PROFILE: '/user/profile',
    UPDATE: '/user/profile',
    VERIFICATION: '/user/verification',
    STATS: '/user/stats',
  },

  CARBON: {
    STATS: '/carbon/stats',
  },

  ADMIN: {
    DASHBOARD: '/admin/dashboard',
    USERS: '/admin/users',
    CREATE_USER: '/admin/users',
    UPDATE_USER: (id: number | string) => `/admin/users/${id}`,
    EXPORT_USERS: '/admin/users/export',
    DELETE_USER: (id: number | string) => `/admin/users/${id}`,
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
    HANDLE_DISPUTE: (id: number | string) => `/admin/disputes/${id}/handle`,
  },
} as const;

export default API_PATHS;
