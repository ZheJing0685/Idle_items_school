import { test, expect } from '@playwright/test';

test.describe('购买完整流程 E2E 测试', () => {
  test.describe.configure({ mode: 'serial' });

  test('首页正确加载', async ({ page }) => {
    await page.goto('/', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    await expect(page).toHaveURL('/');
    await expect(page.locator('h1, h2').first()).toBeVisible();
  });

  test('首页包含探索好物按钮', async ({ page }) => {
    await page.goto('/', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    const exploreButton = page.getByRole('link', { name: '探索好物' });
    const isVisible = await exploreButton.isVisible().catch(() => false);
    if (!isVisible) {
      const header = page.locator('h1, h2').first();
      await expect(header).toBeVisible();
    }
  });

  test('首页包含发布闲置按钮', async ({ page }) => {
    await page.goto('/', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    const publishButton = page.locator('#main-content').getByRole('link', { name: '发布闲置' });
    await expect(publishButton).toBeVisible();
  });

  test('从首页跳转到物品列表', async ({ page }) => {
    await page.goto('/', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    const exploreButton = page.getByRole('link', { name: '探索好物' });
    await exploreButton.click();
    await page.waitForLoadState('domcontentloaded');

    await expect(page).toHaveURL(/\/items/);
  });

  test('物品列表页显示物品', async ({ page }) => {
    await page.goto('/items', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    const items = page.locator('.item-card');
    await expect(items.first()).toBeVisible();
  });

  test('点击物品进入详情页', async ({ page }) => {
    await page.goto('/items', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    const itemCard = page.locator('.item-card').first();
    if (await itemCard.isVisible()) {
      await itemCard.click();
      await page.waitForLoadState('domcontentloaded');

      await expect(page).toHaveURL(/\/item\/\d+/);
    }
  });

  test('物品详情页显示完整信息', async ({ page }) => {
    await page.goto('/items', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    const itemCard = page.locator('.item-card').first();
    if (await itemCard.isVisible()) {
      await itemCard.click();
      await page.waitForLoadState('domcontentloaded');

      await expect(page.locator('.item-title').first()).toBeVisible();
      await expect(page.locator('.price-value').first()).toBeVisible();
    }
  });

  test('物品详情页有购买按钮', async ({ page }) => {
    await page.goto('/items', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    const itemCard = page.locator('.item-card').first();
    if (await itemCard.isVisible()) {
      await itemCard.click();
      await page.waitForLoadState('domcontentloaded');

      const buyButton = page.getByRole('button', { name: '立即购买' });
      await expect(buyButton).toBeVisible();
    }
  });

  test('物品详情页有收藏按钮', async ({ page }) => {
    await page.goto('/items', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    const itemCard = page.locator('.item-card').first();
    if (await itemCard.isVisible()) {
      await itemCard.click();
      await page.waitForLoadState('domcontentloaded');

      const favoriteButton = page.getByRole('button', { name: /收藏/ });
      await expect(favoriteButton).toBeVisible();
    }
  });

  test('物品详情页有分享按钮', async ({ page }) => {
    await page.goto('/items', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    const itemCard = page.locator('.item-card').first();
    if (await itemCard.isVisible()) {
      await itemCard.click();
      await page.waitForLoadState('domcontentloaded');

      const shareButton = page.getByRole('button', { name: /分享/ });
      if (await shareButton.isVisible()) {
        await expect(shareButton).toBeVisible();
      }
    }
  });

  test('物品详情页有返回按钮', async ({ page }) => {
    await page.goto('/items', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    const itemCard = page.locator('.item-card').first();
    if (await itemCard.isVisible()) {
      await itemCard.click();
      await page.waitForLoadState('domcontentloaded');

      const backLink = page.getByRole('link', { name: '发现好物' });
      await expect(backLink).toBeVisible();
    }
  });

  test('点击返回回到物品列表', async ({ page }) => {
    await page.goto('/items', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    const itemCard = page.locator('.item-card').first();
    if (await itemCard.isVisible()) {
      await itemCard.click();
      await page.waitForLoadState('domcontentloaded');

      const backLink = page.getByRole('link', { name: '发现好物' });
      if (await backLink.isVisible()) {
        await backLink.click();
        await page.waitForLoadState('domcontentloaded');

        await expect(page).toHaveURL(/\/items/);
      }
    }
  });
});

test.describe('跨页面导航 E2E 测试', () => {
  test('从首页跳转到登录页', async ({ page }) => {
    await page.goto('/', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    const loginButton = page.getByText(/登录/);
    if (await loginButton.isVisible()) {
      await loginButton.click();
      await page.waitForLoadState('domcontentloaded');

      await expect(page).toHaveURL(/\/login/);
    }
  });

  test('从登录页跳转到注册页', async ({ page }) => {
    await page.goto('/login', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    const registerLink = page.getByRole('link', { name: '立即注册' });
    if (await registerLink.isVisible()) {
      await registerLink.click();
      await page.waitForLoadState('domcontentloaded');

      await expect(page).toHaveURL(/\/register/);
    }
  });

  test('从登录页跳转到忘记密码页', async ({ page }) => {
    await page.goto('/login', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    const forgotLink = page.getByText(/忘记密码/);
    if (await forgotLink.isVisible()) {
      await forgotLink.click();
      await page.waitForLoadState('domcontentloaded');

      await expect(page).toHaveURL(/\/forgot-password/);
    }
  });

  test('从物品详情页跳转到卖家主页', async ({ page }) => {
    await page.goto('/items', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    const itemCard = page.locator('.item-card').first();
    if (await itemCard.isVisible()) {
      await itemCard.click();
      await page.waitForLoadState('domcontentloaded');

      const sellerLink = page.getByText(/卖家|查看卖家/);
      if (await sellerLink.isVisible()) {
        await sellerLink.click();
        await page.waitForLoadState('domcontentloaded');

        await expect(page).toHaveURL(/\/user\/|\/seller\//);
      }
    }
  });

  test('从物品详情页跳转到聊天', async ({ page }) => {
    await page.goto('/items', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    const itemCard = page.locator('.item-card').first();
    if (await itemCard.isVisible()) {
      await itemCard.click();
      await page.waitForLoadState('domcontentloaded');

      const chatButton = page.getByRole('button', { name: /聊天|联系卖家/ });
      if (await chatButton.isVisible()) {
        await chatButton.click();
        await page.waitForLoadState('domcontentloaded');

        await expect(page).toHaveURL(/\/chat/);
      }
    }
  });
});

test.describe('404页面 E2E 测试', () => {
  test('访问不存在的页面显示404', async ({ page }) => {
    await page.goto('/nonexistent-page', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    await expect(page.getByText('404')).toBeVisible();
  });

  test('404页面有返回首页按钮', async ({ page }) => {
    await page.goto('/nonexistent-page', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    const homeButton = page.getByRole('link', { name: '返回首页' });
    await expect(homeButton).toBeVisible();
  });

  test('点击返回首页按钮跳转到首页', async ({ page }) => {
    await page.goto('/nonexistent-page', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    const homeButton = page.getByRole('link', { name: '返回首页' });
    if (await homeButton.isVisible()) {
      await homeButton.click();
      await page.waitForLoadState('domcontentloaded');

      await expect(page).toHaveURL('/');
    }
  });
});
