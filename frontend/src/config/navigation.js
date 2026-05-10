/**
 * 导航配置 - 根据用户角色动态生成导航项
 */

// 导航项图标SVG
export const iconMap = {
  home: `<path d="M3 9L12 2L21 9V20C21 20.5304 20.7893 21.0391 20.4142 21.4142C20.0391 21.7893 19.5304 22 19 22H5C4.46957 22 3.96086 21.7893 3.58579 21.4142C3.21071 21.0391 3 20.5304 3 20V9Z" /><path d="M9 22V12H15V22" />`,
  grid: `<rect x="3" y="3" width="7" height="7" /><rect x="14" y="3" width="7" height="7" /><rect x="14" y="14" width="7" height="7" /><rect x="3" y="14" width="7" height="7" />`,
  'plus-circle': `<circle cx="12" cy="12" r="10" /><path d="M12 8V16" /><path d="M8 12H16" />`,
  shield: `<path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" /><path d="M12 8V12" /><path d="M12 16H12.01" />`,
  user: `<path d="M20 21V19C20 16.7909 18.2091 15 16 15H8C5.79086 15 4 16.7909 4 19V21" /><circle cx="12" cy="7" r="4" />`,
  box: `<path d="M20 7L12 3L4 7" /><path d="M20 7L12 11L4 7" /><path d="M20 7V17L12 21L4 17V7" />`,
  'shopping-bag': `<path d="M6 2L3 6V20C3 20.5304 3.21071 21.0391 3.58579 21.4142C3.96086 21.7893 4.46957 22 5 22H19C19.5304 22 20.0391 21.7893 20.4142 21.4142C20.7893 21.0391 21 20.5304 21 20V6L18 2H6Z" /><path d="M3 6H21" /><path d="M16 10C16 12.2091 14.2091 14 12 14C9.79086 14 8 12.2091 8 10" />`,
  heart: `<path d="M20.84 4.61C20.3292 4.09924 19.7228 3.69397 19.0554 3.41708C18.3879 3.14019 17.6725 2.99756 16.95 2.99756C16.2275 2.99756 15.5121 3.14019 14.8446 3.41708C14.1772 3.69397 13.5708 4.09924 13.06 4.61L12 5.67L10.94 4.61C9.9083 3.57831 8.50903 2.99787 7.05 2.99787C5.59096 2.99787 4.19169 3.57831 3.16 4.61C2.1283 5.64169 1.54785 7.04097 1.54785 8.5C1.54785 9.95903 2.1283 11.3583 3.16 12.39L4.22 13.45L12 21.23L19.78 13.45L20.84 12.39C21.3508 11.8792 21.756 11.2728 22.0329 10.6054C22.3098 9.93789 22.4524 9.22248 22.4524 8.5C22.4524 7.77751 22.3098 7.0621 22.0329 6.39464C21.756 5.72718 21.3508 5.12075 20.84 4.61Z" />`,
  message: `<path d="M21 15C21 15.5304 20.7893 16.0391 20.4142 16.4142C20.0391 16.7893 19.5304 17 19 17H7L3 21V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H16C16.5304 3 17.0391 3.21071 17.4142 3.58579C17.7893 3.96086 18 4.46957 18 5" />`,
  bell: `<path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" /><path d="M13.73 21a2 2 0 0 1-3.46 0" />`,
};

// 导航项配置
export const navigationConfig = {
  // 游客模式
  guest: [
    { name: '首页', path: '/', icon: 'home' },
    { name: '浏览好物', path: '/items', icon: 'grid' },
  ],
  
  // 普通用户模式
  user: [
    { name: '首页', path: '/', icon: 'home' },
    { name: '浏览好物', path: '/items', icon: 'grid' },
    { name: '发布闲置', path: '/publish', icon: 'plus-circle', requiresAuth: true },
    { name: '我的订单', path: '/user/orders', icon: 'shopping-bag', requiresAuth: true },
    { name: '消息中心', path: '/user/chat', icon: 'message', requiresAuth: true },
  ],
  
  // 管理员模式
  admin: [
    { name: '首页', path: '/', icon: 'home' },
    { name: '浏览好物', path: '/items', icon: 'grid' },
    { name: '发布闲置', path: '/publish', icon: 'plus-circle', requiresAuth: true },
    { name: '我的订单', path: '/user/orders', icon: 'shopping-bag', requiresAuth: true },
    { name: '消息中心', path: '/user/chat', icon: 'message', requiresAuth: true },
    { name: '管理后台', path: '/admin', icon: 'shield', requiresAdmin: true },
  ],
};

// 用户菜单配置
export const userMenuConfig = {
  items: [
    { name: '个人中心', path: '/user/profile', icon: 'user' },
    { name: '我的发布', path: '/user/items', icon: 'box' },
    { name: '我的订单', path: '/user/orders', icon: 'shopping-bag' },
    { name: '我的收藏', path: '/user/favorites', icon: 'heart' },
    { name: '消息中心', path: '/user/chat', icon: 'message' },
    { name: '消息通知', path: '/user/notifications', icon: 'bell' },
  ],
  adminItem: { name: '管理后台', path: '/admin', icon: 'shield' },
};

/**
 * 根据用户角色获取导航项
 * @param {string} userRole - 用户角色: 'guest' | 'user' | 'admin'
 * @returns {Array} 导航项数组
 */
export function getNavigationItems(userRole) {
  return navigationConfig[userRole] || navigationConfig.guest;
}

/**
 * 获取用户菜单项
 * @param {boolean} isAdmin - 是否是管理员
 * @returns {Array} 菜单项数组
 */
export function getUserMenuItems(isAdmin) {
  const items = [...userMenuConfig.items];
  if (isAdmin) {
    items.push(userMenuConfig.adminItem);
  }
  return items;
}
