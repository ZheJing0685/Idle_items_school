import { test as base } from '@playwright/test';

// Mock data generators
const createMockItems = (count = 3) => ({
  code: 200,
  data: {
    content: Array.from({ length: count }, (_, i) => ({
      id: i + 1,
      title: `测试物品 ${i + 1}`,
      price: (i + 1) * 50,
      condition: 'LIKE_NEW',
      categoryName: '电子产品',
      coverImage: 'https://via.placeholder.com/200x150',
      createdAt: new Date().toISOString(),
      sellerId: 1,
      sellerNickname: '测试卖家',
    })),
    totalElements: count,
    totalPages: 1,
    number: 0,
    size: 20,
  },
});

const createMockOrders = () => ({
  code: 200,
  data: {
    content: [{
      id: 1,
      orderNo: 'ORD2026001',
      orderStatus: 'COMPLETED',
      price: 100,
      itemTitle: '测试物品',
      itemImage: 'https://via.placeholder.com/200x150',
      createdAt: new Date().toISOString(),
    }],
    totalElements: 1,
    totalPages: 1,
  },
});

const createMockCategories = () => ({
  code: 200,
  data: [
    { id: 1, name: '电子产品', icon: '💻', parentId: null },
    { id: 2, name: '书籍教材', icon: '📚', parentId: null },
    { id: 3, name: '生活用品', icon: '🏠', parentId: null },
  ],
});

const createMockProfile = () => ({
  code: 200,
  data: {
    id: 1,
    username: 'testuser',
    nickname: '测试用户',
    email: 'test@example.com',
    role: 'STUDENT',
    verified: true,
    creditScore: 85,
    totalItems: 5,
    soldItems: 20,
    completedDeals: 18,
    rating: 4.5,
    reviewCount: 10,
    memberSince: '2024-01-15T00:00:00Z',
    bio: '这是一个测试简介',
  },
});

const createMockSellerProfile = () => ({
  code: 200,
  data: {
    id: 1,
    nickname: '测试卖家',
    verified: true,
    schoolName: '测试大学',
    creditScore: 85,
    bio: '这是一个测试简介',
    totalItems: 5,
    soldItems: 20,
    completedDeals: 18,
    rating: 4.5,
    reviewCount: 10,
    memberSince: '2024-01-15T00:00:00Z',
  },
});

export const mockTest = base.extend({
  page: async ({ page }, use) => {
    // Intercept ALL /api/* requests before navigation
    await page.route('**/api/**', async (route) => {
      const url = route.request().url();
      const path = new URL(url).pathname;

      let responseData = { code: 200, data: null, message: 'ok' };

      if (path.includes('/items') && !path.includes('/seller')) {
        if (url.includes('/hot')) {
          responseData.data = [];
        } else if (url.includes('/recommended') || url.includes('/list') || url.includes('?')) {
          responseData = createMockItems();
        } else if (path.match(/\/items\/\d+/)) {
          responseData = {
            code: 200,
            data: {
              id: 1,
              title: '测试物品详情',
              price: 99.9,
              description: '这是一个测试物品的详细描述',
              condition: 'LIKE_NEW',
              coverImage: 'https://via.placeholder.com/400x300',
              images: ['https://via.placeholder.com/400x300'],
              categoryId: 1,
              categoryName: '电子产品',
              sellerId: 1,
              sellerNickname: '测试卖家',
              createdAt: new Date().toISOString(),
              viewCount: 100,
              likeCount: 10,
            },
          };
        } else {
          responseData = createMockItems();
        }
      } else if (path.includes('/auth/login')) {
        responseData = {
          code: 200,
          data: {
            token: 'mock-jwt-token',
            refreshToken: 'mock-refresh-token',
            user: { id: 1, username: 'testuser', role: 'STUDENT', verified: true },
          },
        };
      } else if (path.includes('/auth/me') || path.includes('/user/profile')) {
        responseData = createMockProfile();
      } else if (path.includes('/categories')) {
        responseData = createMockCategories();
      } else if (path.includes('/seller/profile')) {
        responseData = createMockSellerProfile();
      } else if (path.includes('/orders')) {
        responseData = createMockOrders();
      } else if (path.includes('/chat')) {
        responseData = { code: 200, data: [] };
      } else if (path.includes('/favorites')) {
        responseData = { code: 200, data: { content: [], totalElements: 0 } };
      } else if (path.includes('/notifications')) {
        responseData = { code: 200, data: { content: [], totalElements: 0 } };
      } else if (path.includes('/disputes')) {
        responseData = { code: 200, data: { content: [], totalElements: 0 } };
      } else if (path.includes('/feedbacks')) {
        responseData = { code: 200, data: { content: [], totalElements: 0 } };
      } else if (path.includes('/admin')) {
        responseData = { code: 200, data: { content: [], totalElements: 0 } };
      } else if (path.includes('/verify')) {
        responseData = { code: 200, data: { status: 'NOT_SUBMITTED' } };
      } else if (path.includes('/review')) {
        responseData = { code: 200, data: { content: [], totalElements: 0 } };
      } else if (path.includes('/carbon')) {
        responseData = { code: 200, data: null };
      } else {
        // Default: return empty but successful response
        responseData = { code: 200, data: [] };
      }

      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(responseData),
      });
    });

    await use(page);
  },
});
