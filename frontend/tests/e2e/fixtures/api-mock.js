/**
 * API Mock工具
 * 用于模拟后端API响应，消除E2E测试对真实后端的依赖
 */

export class ApiMock {
  constructor(page) {
    this.page = page;
    this.routes = new Map();
  }

  /**
   * mock API响应
   * @param {string} urlPattern - URL模式（支持通配符）
   * @param {object} response - 响应数据
   * @param {number} status - HTTP状态码
   */
  async mockResponse(urlPattern, response, status = 200) {
    await this.page.route(urlPattern, async (route) => {
      await route.fulfill({
        status,
        contentType: 'application/json',
        body: JSON.stringify(response),
      });
    });
    this.routes.set(urlPattern, { response, status });
  }

  /**
   * mock登录API
   * @param {object} userData - 用户数据
   */
  async mockLogin(userData = {}) {
    const defaultUser = {
      id: 1,
      username: 'testuser',
      role: 'STUDENT',
      verified: true,
      ...userData,
    };

    await this.mockResponse('**/api/auth/login', {
      code: 200,
      message: '登录成功',
      data: {
        token: 'mock-jwt-token',
        refreshToken: 'mock-refresh-token',
        user: defaultUser,
      },
    });

    await this.mockResponse('**/api/auth/me', {
      code: 200,
      data: defaultUser,
    });
  }

  /**
   * mock物品列表API
   * @param {array} items - 物品数据
   */
  async mockItems(items = []) {
    const defaultItems = [
      {
        id: 1,
        title: '测试物品1',
        price: 100,
        status: 'ACTIVE',
        ...items[0],
      },
      {
        id: 2,
        title: '测试物品2',
        price: 200,
        status: 'ACTIVE',
        ...items[1],
      },
    ];

    await this.mockResponse('**/api/items**', {
      code: 200,
      data: {
        content: defaultItems,
        totalElements: defaultItems.length,
        totalPages: 1,
      },
    });
  }

  /**
   * mock订单API
   * @param {array} orders - 订单数据
   */
  async mockOrders(orders = []) {
    const defaultOrders = [
      {
        id: 1,
        orderNo: 'ORD2026001',
        orderStatus: 'PENDING_PAYMENT',
        price: 100,
        ...orders[0],
      },
    ];

    await this.mockResponse('**/api/orders**', {
      code: 200,
      data: {
        content: defaultOrders,
        totalElements: defaultOrders.length,
        totalPages: 1,
      },
    });
  }

  /**
   * 清除所有mock
   */
  async clearAll() {
    for (const urlPattern of this.routes.keys()) {
      await this.page.unroute(urlPattern);
    }
    this.routes.clear();
  }
}

/**
 * 创建已登录状态的页面
 * @param {import('@playwright/test').Page} page
 * @param {object} userData - 用户数据
 */
export async function createLoggedInPage(page, userData = {}) {
  const mock = new ApiMock(page);
  await mock.mockLogin(userData);
  
  // 设置localStorage模拟登录状态
  await page.evaluate((user) => {
    localStorage.setItem('idle_items_token', 'mock-jwt-token');
    localStorage.setItem('idle_items_user', JSON.stringify(user));
  }, userData);

  return mock;
}
