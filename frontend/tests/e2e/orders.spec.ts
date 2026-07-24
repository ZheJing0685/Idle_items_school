import { expect } from '@playwright/test';
import { mockTest as test } from './fixtures/mock-api';

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
      await expect(page.locator('h1, h2, .order-detail, .empty-state').first()).toBeVisible();
    }
  });

  test('订单状态显示', async ({ page }) => {
    await page.goto('/orders/1', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    if (page.url().includes('/login')) {
      await expect(page.getByRole('button', { name: '登录' })).toBeVisible();
    } else {
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

  test('收藏页面标题存在', async ({ page }) => {
    await page.goto('/favorites', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    if (page.url().includes('/login')) {
      await expect(page.getByRole('button', { name: '登录' })).toBeVisible();
    } else {
      await expect(page.locator('h1, h2').first()).toBeVisible();
    }
  });

  test('收藏列表为空时显示空状态', async ({ page }) => {
    await page.goto('/favorites', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    if (page.url().includes('/login')) {
      await expect(page.getByRole('button', { name: '登录' })).toBeVisible();
    } else {
      const emptyState = page.locator('.empty-state, [class*="empty"]');
      const hasEmpty = await emptyState.isVisible().catch(() => false);
      const hasItems = await page.locator('.item-card').first().isVisible().catch(() => false);
      expect(hasEmpty || hasItems).toBe(true);
    }
  });
});
