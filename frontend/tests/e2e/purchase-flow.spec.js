import { test, expect } from '@playwright/test'
import { ApiMock, createLoggedInPage } from './fixtures/api-mock.js'

test.describe('完整购买流程测试', () => {
  let mock;

  test.beforeEach(async ({ page }) => {
    mock = await createLoggedInPage(page, {
      id: 1,
      username: 'buyer',
      role: 'STUDENT',
      verified: true,
    });
  });

  test.afterEach(async () => {
    if (mock) {
      await mock.clearAll();
    }
  });

  test('完整购买流程：浏览→加购→下单→支付', async ({ page }) => {
    // 1. 浏览物品列表
    await mock.mockItems([
      { id: 1, title: 'iPhone 15', price: 5999 },
      { id: 2, title: 'MacBook Pro', price: 12999 },
    ]);

    await page.goto('/items');
    await expect(page.getByText('iPhone 15')).toBeVisible();
    await expect(page.getByText('MacBook Pro')).toBeVisible();

    // 2. 查看物品详情
    await mock.mockResponse('**/api/items/1', {
      code: 200,
      data: {
        id: 1,
        title: 'iPhone 15',
        price: 5999,
        description: '全新未拆封',
        seller: { id: 2, nickname: '卖家' },
      },
    });

    await page.getByText('iPhone 15').click();
    await expect(page.getByText('全新未拆封')).toBeVisible();

    // 3. 点击购买/联系卖家
    await mock.mockResponse('**/api/orders', {
      code: 200,
      message: '下单成功',
      data: {
        id: 1,
        orderNo: 'ORD2026001',
        orderStatus: 'PENDING_PAYMENT',
        price: 5999,
      },
    });

    await page.getByRole('button', { name: /购买|立即购买/ }).click();

    // 4. 填写订单信息
    await page.getByPlaceholder(/收货地址|地址/).fill('北京市海淀区北京大学');
    await page.getByPlaceholder(/联系电话|手机/).fill('13800138000');

    // 5. 提交订单
    await page.getByRole('button', { name: /提交订单|确认下单/ }).click();

    // 6. 支付订单
    await mock.mockResponse('**/api/orders/1/pay', {
      code: 200,
      message: '支付成功',
      data: {
        id: 1,
        orderStatus: 'PAID',
      },
    });

    await page.getByRole('button', { name: /立即支付|去支付/ }).click();

    // 7. 验证支付成功
    await expect(page.getByText(/支付成功|订单已支付/)).toBeVisible();
    await expect(page.getByText('ORD2026001')).toBeVisible();
  });

  test('购买流程异常处理：库存不足', async ({ page }) => {
    await mock.mockItems([{ id: 1, title: 'iPhone 15', price: 5999 }]);

    await page.goto('/items');
    await page.getByText('iPhone 15').click();

    // mock库存不足响应
    await mock.mockResponse('**/api/orders', {
      code: 400,
      message: '库存不足，无法购买',
    }, 400);

    await page.getByRole('button', { name: /购买|立即购买/ }).click();
    await page.getByPlaceholder(/收货地址|地址/).fill('北京市海淀区');
    await page.getByPlaceholder(/联系电话|手机/).fill('13800138000');
    await page.getByRole('button', { name: /提交订单|确认下单/ }).click();

    // 验证错误提示
    await expect(page.getByText(/库存不足/)).toBeVisible();
  });
});
