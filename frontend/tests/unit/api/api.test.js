import api from '../api';

describe('API Module Tests', () => {
  test('API modules should be properly structured', () => {
    expect(api).toBeDefined();
    expect(api.auth).toBeDefined();
    expect(api.item).toBeDefined();
    expect(api.category).toBeDefined();
    expect(api.favorite).toBeDefined();
    expect(api.user).toBeDefined();
    expect(api.verification).toBeDefined();
    expect(api.review).toBeDefined();
    expect(api.order).toBeDefined();
    expect(api.admin).toBeDefined();
  });

  test('API should have token management methods', () => {
    expect(typeof api.setToken).toBe('function');
    expect(typeof api.getToken).toBe('function');
    expect(typeof api.clearToken).toBe('function');
  });

  test('API should have cache management methods', () => {
    expect(typeof api.clearCache).toBe('function');
    expect(typeof api.clearAllCache).toBe('function');
  });

  test('Auth API should have required methods', () => {
    expect(typeof api.auth.login).toBe('function');
    expect(typeof api.auth.register).toBe('function');
    expect(typeof api.auth.getCurrentUser).toBe('function');
    expect(typeof api.auth.refreshToken).toBe('function');
  });

  test('Item API should have required methods', () => {
    expect(typeof api.item.getItems).toBe('function');
    expect(typeof api.item.getHotItems).toBe('function');
    expect(typeof api.item.searchItems).toBe('function');
    expect(typeof api.item.getItem).toBe('function');
    expect(typeof api.item.createItem).toBe('function');
    expect(typeof api.item.updateItem).toBe('function');
    expect(typeof api.item.offShelf).toBe('function');
    expect(typeof api.item.uploadImage).toBe('function');
  });

  test('Category API should have required methods', () => {
    expect(typeof api.category.getCategories).toBe('function');
    expect(typeof api.category.getCategoryTree).toBe('function');
  });

  test('Order API should have required methods', () => {
    expect(typeof api.order.createOrder).toBe('function');
    expect(typeof api.order.getBuyerOrders).toBe('function');
    expect(typeof api.order.getSellerOrders).toBe('function');
    expect(typeof api.order.getOrder).toBe('function');
    expect(typeof api.order.payOrder).toBe('function');
    expect(typeof api.order.cancelOrder).toBe('function');
    expect(typeof api.order.shipOrder).toBe('function');
    expect(typeof api.order.confirmReceive).toBe('function');
  });

  test('Admin API should have required methods', () => {
    expect(api.admin).toBeDefined();
    expect(api.admin.statistics).toBeDefined();
    expect(typeof api.admin.statistics.getDashboard).toBe('function');
  });
});
