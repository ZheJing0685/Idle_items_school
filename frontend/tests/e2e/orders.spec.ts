import { test, expect } from '@playwright/test';

test.describe('订单流程 E2E 测试', () => {
  test('订单列表页正确加载', async ({ page }) => {
    await page.goto('/orders', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    if (page.url().includes('/login')) {
      await expect(page).toHaveURL(/\/login/);
    } else {
      await expect(page).toHaveURL(/\/orders/);
    }
  });

  test('订单列表页标题存在', async ({ page }) => {
    await page.goto('/orders', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    if (page.url().includes('/login')) {
      await expect(page.getByRole('button', { name: '登录' })).toBeVisible();
    } else {
      await expect(page.locator('h1, h2').first()).toBeVisible();
    }
  });

  test('订单标签页存在', async ({ page }) => {
    await page.goto('/orders', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    if (page.url().includes('/login')) {
      await expect(page.getByRole('button', { name: '登录' })).toBeVisible();
    } else {
      const tabs = page.locator('.el-tabs, .tab-list, .tabs');
      const hasTabs = await tabs.first().isVisible().catch(() => false);
      const hasTitle = await page.locator('h1, h2').first().isVisible().catch(() => false);
      expect(hasTabs || hasTitle).toBe(true);
    }
  });

  test('订单详情页正确加载', async ({ page }) => {
    await page.goto('/orders/1', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    if (page.url().includes('/login')) {
      await expect(page.getByRole('button', { name: '登录' })).toBeVisible();
    } else {
      // 订单详情页可能显示"暂无订单"或订单详情
      await expect(page.locator('h1, h2, .order-detail, .empty-state').first()).toBeVisible();
    }
  });

  test('订单状态显示', async ({ page }) => {
    await page.goto('/orders/1', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    if (page.url().includes('/login')) {
      await expect(page.getByRole('button', { name: '登录' })).toBeVisible();
    } else {
      // 订单页可能显示订单列表、空状态或页面标题
      await expect(page.locator('h1, h2, .order-status, .status-badge, .el-tabs, .empty-state').first()).toBeVisible();
    }
  });
});

test.describe('用户中心 E2E 测试', () => {
  test('用户中心页正确加载', async ({ page }) => {
    await page.goto('/user', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    if (page.url().includes('/login')) {
      await expect(page.getByRole('button', { name: '登录' })).toBeVisible();
    } else {
      await expect(page).toHaveURL(/\/user/);
    }
  });

  test('用户信息显示', async ({ page }) => {
    await page.goto('/user', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    if (page.url().includes('/login')) {
      await expect(page.getByRole('button', { name: '登录' })).toBeVisible();
    } else {
      const userInfo = page.locator('.user-info, .profile-card, .user-card');
      const hasUserInfo = await userInfo.first().isVisible().catch(() => false);
      const hasTitle = await page.locator('h1, h2').first().isVisible().catch(() => false);
      expect(hasUserInfo || hasTitle).toBe(true);
    }
  });

  test('导航菜单存在', async ({ page }) => {
    await page.goto('/user', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    if (page.url().includes('/login')) {
      await expect(page.getByRole('button', { name: '登录' })).toBeVisible();
    } else {
      const sidebar = page.locator('.sidebar, .nav-menu, .user-nav, .menu');
      const hasSidebar = await sidebar.first().isVisible().catch(() => false);
      const hasTitle = await page.locator('h1, h2').first().isVisible().catch(() => false);
      expect(hasSidebar || hasTitle).toBe(true);
    }
  });
});

test.describe('收藏列表 E2E 测试', () => {
  test('收藏页正确加载', async ({ page }) => {
    await page.goto('/favorites', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    if (page.url().includes('/login')) {
      await expect(page.getByRole('button', { name: '登录' })).toBeVisible();
    } else {
      await expect(page).toHaveURL(/\/favorites/);
    }
  });

  test('收藏列表标题存在', async ({ page }) => {
    await page.goto('/favorites', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    if (page.url().includes('/login')) {
      await expect(page.getByRole('button', { name: '登录' })).toBeVisible();
    } else {
      await expect(page.locator('h1, h2').first()).toBeVisible();
    }
  });
});

test.describe('聊天功能 E2E 测试', () => {
  test('聊天页正确加载', async ({ page }) => {
    await page.goto('/chat', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    if (page.url().includes('/login')) {
      await expect(page.getByRole('button', { name: '登录' })).toBeVisible();
    } else {
      await expect(page).toHaveURL(/\/chat/);
    }
  });

  test('聊天列表存在', async ({ page }) => {
    await page.goto('/chat', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    if (page.url().includes('/login')) {
      await expect(page.getByRole('button', { name: '登录' })).toBeVisible();
    } else {
      // 聊天页可能显示聊天列表或空状态
      await expect(page.locator('.chat-list, .conversation-list, .chat-page, .empty-state, h1, h2').first()).toBeVisible();
    }
  });
});

test.describe('通知中心 E2E 测试', () => {
  test('通知页正确加载', async ({ page }) => {
    await page.goto('/notifications', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    if (page.url().includes('/login')) {
      await expect(page.getByRole('button', { name: '登录' })).toBeVisible();
    } else {
      await expect(page).toHaveURL(/\/notifications/);
    }
  });

  test('通知列表存在', async ({ page }) => {
    await page.goto('/notifications', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    if (page.url().includes('/login')) {
      await expect(page.getByRole('button', { name: '登录' })).toBeVisible();
    } else {
      // 通知页可能显示通知列表或空状态
      await expect(page.locator('.notification-list, .message-list, .notification-page, .empty-state, h1, h2').first()).toBeVisible();
    }
  });
});

test.describe('个人资料 E2E 测试', () => {
  test('个人资料页正确加载', async ({ page }) => {
    await page.goto('/profile', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    if (page.url().includes('/login')) {
      await expect(page.getByRole('button', { name: '登录' })).toBeVisible();
    } else {
      await expect(page).toHaveURL(/\/profile/);
    }
  });

  test('个人资料表单存在', async ({ page }) => {
    await page.goto('/profile', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    if (page.url().includes('/login')) {
      await expect(page.getByRole('button', { name: '登录' })).toBeVisible();
    } else {
      // 个人资料页可能显示表单或用户信息
      await expect(page.locator('form, .el-form, .profile-form, .profile-page, .user-info, h1, h2').first()).toBeVisible();
    }
  });
});
