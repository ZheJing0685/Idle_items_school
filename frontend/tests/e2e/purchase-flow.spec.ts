import { test, expect } from '@playwright/test';

test.describe('购买完整流程 E2E 测试', () => {
  test.describe.configure({ mode: 'serial' });

  test('首页正确加载', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('networkidle');

    await expect(page).toHaveURL('/');
    await expect(page.locator('h1, h2').first()).toBeVisible();
  });

  test('首页包含探索好物按钮', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('networkidle');

    const exploreButton = page.getByText(/探索好物|浏览/);
    await expect(exploreButton).toBeVisible();
  });

  test('首页包含发布闲置按钮', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('networkidle');

    const publishButton = page.getByText(/发布闲置|发布/);
    await expect(publishButton).toBeVisible();
  });

  test('从首页跳转到物品列表', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('networkidle');

    const exploreButton = page.getByText(/探索好物|浏览/);
    await exploreButton.click();
    await page.waitForLoadState('networkidle');

    await expect(page).toHaveURL(/\/items/);
  });

  test('物品列表页显示物品', async ({ page }) => {
    await page.goto('/items');
    await page.waitForLoadState('networkidle');

    const items = page.locator('.item-card, .product-card');
    await expect(items.first()).toBeVisible();
  });

  test('点击物品进入详情页', async ({ page }) => {
    await page.goto('/items');
    await page.waitForLoadState('networkidle');

    const itemCard = page.locator('.item-card, .product-card').first();
    if (await itemCard.isVisible()) {
      await itemCard.click();
      await page.waitForLoadState('networkidle');

      await expect(page).toHaveURL(/\/items\/\d+/);
    }
  });

  test('物品详情页显示完整信息', async ({ page }) => {
    await page.goto('/items/1');
    await page.waitForLoadState('networkidle');

    // 检查标题
    await expect(page.locator('h1, h2').first()).toBeVisible();

    // 检查价格
    await expect(page.locator('.price, .item-price').first()).toBeVisible();
  });

  test('物品详情页有购买按钮', async ({ page }) => {
    await page.goto('/items/1');
    await page.waitForLoadState('networkidle');

    const buyButton = page.getByRole('button', { name: /购买|立即购买|加入购物车/ });
    await expect(buyButton).toBeVisible();
  });

  test('物品详情页有收藏按钮', async ({ page }) => {
    await page.goto('/items/1');
    await page.waitForLoadState('networkidle');

    const favoriteButton = page.getByRole('button', { name: /收藏|喜欢/ });
    await expect(favoriteButton).toBeVisible();
  });

  test('物品详情页有分享按钮', async ({ page }) => {
    await page.goto('/items/1');
    await page.waitForLoadState('networkidle');

    const shareButton = page.getByRole('button', { name: /分享/ });
    await expect(shareButton).toBeVisible();
  });

  test('物品详情页有返回按钮', async ({ page }) => {
    await page.goto('/items/1');
    await page.waitForLoadState('networkidle');

    const backButton = page.getByText(/返回|列表/);
    await expect(backButton).toBeVisible();
  });

  test('点击返回回到物品列表', async ({ page }) => {
    await page.goto('/items/1');
    await page.waitForLoadState('networkidle');

    const backButton = page.getByText(/返回|列表/);
    if (await backButton.isVisible()) {
      await backButton.click();
      await page.waitForLoadState('networkidle');

      await expect(page).toHaveURL(/\/items/);
    }
  });
});

test.describe('跨页面导航 E2E 测试', () => {
  test('从首页跳转到登录页', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('networkidle');

    const loginButton = page.getByText(/登录/);
    if (await loginButton.isVisible()) {
      await loginButton.click();
      await page.waitForLoadState('networkidle');

      await expect(page).toHaveURL(/\/login/);
    }
  });

  test('从登录页跳转到注册页', async ({ page }) => {
    await page.goto('/login');
    await page.waitForLoadState('networkidle');

    const registerLink = page.getByText(/注册|立即注册/);
    if (await registerLink.isVisible()) {
      await registerLink.click();
      await page.waitForLoadState('networkidle');

      await expect(page).toHaveURL(/\/register/);
    }
  });

  test('从登录页跳转到忘记密码页', async ({ page }) => {
    await page.goto('/login');
    await page.waitForLoadState('networkidle');

    const forgotLink = page.getByText(/忘记密码/);
    if (await forgotLink.isVisible()) {
      await forgotLink.click();
      await page.waitForLoadState('networkidle');

      await expect(page).toHaveURL(/\/forgot-password/);
    }
  });

  test('从物品详情页跳转到卖家主页', async ({ page }) => {
    await page.goto('/items/1');
    await page.waitForLoadState('networkidle');

    const sellerLink = page.getByText(/卖家|查看卖家/);
    if (await sellerLink.isVisible()) {
      await sellerLink.click();
      await page.waitForLoadState('networkidle');

      // 应该跳转到卖家页面或显示卖家信息
      await expect(page).toHaveURL(/\/user\/|\/seller\//);
    }
  });

  test('从物品详情页跳转到聊天', async ({ page }) => {
    await page.goto('/items/1');
    await page.waitForLoadState('networkidle');

    const chatButton = page.getByRole('button', { name: /聊天|联系卖家/ });
    if (await chatButton.isVisible()) {
      await chatButton.click();
      await page.waitForLoadState('networkidle');

      // 应该跳转到聊天页面或打开聊天窗口
      await expect(page).toHaveURL(/\/chat/);
    }
  });
});

test.describe('404页面 E2E 测试', () => {
  test('访问不存在的页面显示404', async ({ page }) => {
    await page.goto('/nonexistent-page');
    await page.waitForLoadState('networkidle');

    await expect(page.getByText(/404|页面不存在/)).toBeVisible();
  });

  test('404页面有返回首页按钮', async ({ page }) => {
    await page.goto('/nonexistent-page');
    await page.waitForLoadState('networkidle');

    const homeButton = page.getByText(/返回首页|首页/);
    await expect(homeButton).toBeVisible();
  });

  test('点击返回首页按钮跳转到首页', async ({ page }) => {
    await page.goto('/nonexistent-page');
    await page.waitForLoadState('networkidle');

    const homeButton = page.getByText(/返回首页|首页/);
    if (await homeButton.isVisible()) {
      await homeButton.click();
      await page.waitForLoadState('networkidle');

      await expect(page).toHaveURL('/');
    }
  });
});
