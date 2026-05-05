// API路径统一管理
export const API_PATHS = {
  // 认证相关
  AUTH: {
    LOGIN: '/auth/login',
    REGISTER: '/auth/register',
    ME: '/auth/me',
    REFRESH: '/auth/refresh'
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
    UPLOAD: '/items/upload',
    UPLOAD_CHUNK: '/items/upload/chunk',
    UPLOAD_COMPLETE: '/items/upload/complete',
    UPLOAD_CHECK: '/items/upload/check'
  },
  
  // 分类相关
  CATEGORY: {
    LIST: '/categories',
    TREE: '/categories/tree'
  },
  
  // 收藏相关
  FAVORITE: {
    LIST: '/favorites',
    ADD: '/favorites',
    REMOVE: (id) => `/favorites/${id}`
  },
  
  // 订单相关
  ORDER: {
    CREATE: '/orders',
    LIST: '/orders',
    DETAIL: (id) => `/orders/${id}`,
    CANCEL: (id) => `/orders/${id}/cancel`,
    PAY: (id) => `/orders/${id}/pay`,
    SHIP: (id) => `/orders/${id}/ship`,
    CONFIRM: (id) => `/orders/${id}/confirm`,
    REFUND: (id) => `/orders/${id}/refund`
  },
  
  // 评价相关
  REVIEW: {
    CREATE: '/reviews',
    LIST: (itemId) => `/reviews/item/${itemId}`,
    USER: '/reviews/user'
  },
  
  // 用户相关
  USER: {
    PROFILE: '/user/profile',
    UPDATE: '/user/profile',
    VERIFICATION: '/user/verification'
  },
  
  // 管理员相关
  ADMIN: {
    DASHBOARD: '/admin/dashboard',
    USERS: '/admin/users',
    ITEMS: '/admin/items',
    ORDERS: '/admin/orders',
    CATEGORIES: '/admin/categories',
    STATISTICS: '/admin/statistics'
  }
};

export default API_PATHS;