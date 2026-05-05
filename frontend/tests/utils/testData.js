/**
 * Playwright E2E 测试数据
 * 集中管理测试账号、测试数据
 */

export const testData = {
  // 测试用户账号
  validUser: {
    username: 'testuser',
    password: 'test123'
  },

  // 测试物品数据
  testItem: {
    title: '测试闲置物品',
    description: '这是一条测试用的物品描述',
    price: 99.9,
    category: '电子产品',
    condition: '九成新'
  },

  // 测试注册数据
  testRegister: {
    username: `testuser_${Date.now()}`,
    password: 'Test123456',
    email: `test${Date.now()}@test.com`,
    phone: '13800138000',
    nickname: '测试用户'
  },

  // 搜索关键词
  searchKeywords: ['手机', '电脑', '书籍', '数码', '相机'],

  // 浏览器测试配置
  browsers: {
    chrome: { name: 'Chrome', width: 1920, height: 1080 },
    firefox: { name: 'Firefox', width: 1920, height: 1080 },
    safari: { name: 'Safari', width: 1920, height: 1080 },
    edge: { name: 'Edge', width: 1920, height: 1080 }
  },

  // 响应式断点
  viewports: {
    desktop: { width: 1920, height: 1080 },
    tablet: { width: 768, height: 1024 },
    mobile: { width: 375, height: 667 }
  },

  // 测试超时配置
  timeouts: {
    short: 5000,
    medium: 10000,
    long: 30000,
    veryLong: 60000
  },

  // API 端点（供 Mock 使用）
  apiEndpoints: {
    baseURL: 'http://localhost:7000/api',
    login: '/auth/login',
    register: '/auth/register',
    items: '/items',
    orders: '/orders',
    user: '/auth/me'
  }
}

export default testData
