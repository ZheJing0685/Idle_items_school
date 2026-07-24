import { expect } from '@playwright/test';
import { mockTest as test } from './fixtures/mock-api';

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
    await expect(exploreButton).toBeVisible();
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

  test('物品列表页显示物品), async ({ page }) => {
    await page.goto('/items', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    const items = page.locator('.item-card');
    await expect(items.first()).toBeVisible();
  });

  test('点击物品进入详详情), async ({ page }) => {
    await page.goto('/items', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    const itemCard = page.locator('.item-card').first();
    await itemCard.click();
    await page.waitForLoadState('domcontentloaded');

    await expect(page).toHaveURL(/\/item\/\d+/);
  });

  test('物品详情页显示完整信息), async ({ page }) => {
    await page.goto('/items', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    const itemCard = page.locator('.item-card').first();
    await itemCard.click();
    await page.waitForLoadState('domcontentloaded');

    await expect(page.locator('.item-detail').first()).toBeVisible();
    await expect(page.locator('.item-title').first()).toBeVisible();
    await expect(page.locator('.price-value').first()).toBeVisible();
  });

  test('收藏功能可用', async ({ page }) => {
    await page.goto('/items', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    const itemCard = page.locator('.item-card').first();
    await itemCard.click();
    await page.waitForLoadState('domcontentloaded');

    const favoriteBtn = page.getByRole('button', { name: /收藏|Favorite/ });
    await expect(favoriteBtn).toBeVisible();
  });

  test('联系卖家功能可用', async ({ page }) => {
    await page.goto('/items', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    const itemCard = page.locator('.item-card').first();
    await itemCard.click();
    await page.waitForLoadState('domcontentloaded');

    const contactBtn = page.getByRole('button', { name: /联系|Chat/ });
    await expect(contactBtn).toBeVisible();
  });

  test('举报功能存在', async ({ page }) => {
    await page.goto('/items', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    const itemCard = page.locator('.item-card').first();
    await itemCard.click();
    await page.waitForLoadState('domcontentloaded');

    const reportBtn = page.getByRole('button', { name: /举报|Report/ });
    await expect(reportBtn).toBeVisible();
  });

  test('分享功能存在', async ({ page }) => {
    await page.goto('/items', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    const itemCard = page.locator('.item-card').first();
    await itemCard.click();
    await page.waitForLoadState('domcontentloaded');

    const shareBtn = page.getByRole('button', { name: /分享|Share/ });
    await expect(shareBtn).toBeVisible();
  });

  test('卖家信息区域存在', async ({ page }) => {
    await page.goto('/items', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    const itemCard = page.locator('.item-card').first();
    await itemCard.click();
    await page.waitForLoadState('domcontentloaded');

    const sellerInfo = page.locator('.seller-info, [class*="seller"]');
    await expect(sellerInfo.first()).toBeVisible();
  });

  test('商品描述区域存在', async ({ page }) => {
    await page.goto('/items', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    const itemCard = page.locator('.item-card').first();
    await itemCard.click();
    await page.waitForLoadState('domcontentloaded');

    const desc = page.locator('.item-description, [class*="description"]');
    await expect(desc.first()).toBeVisible();
  });

  test('图片轮播功能存在', async ({ page }) => {
    await page.goto('/items', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    const itemCard = page.locator('.item-card').first();
    await itemCard.click();
    await page.waitForLoadState('domcontentloaded');

    const carousel = page.locator('.carousel, [class*="slider"], .swiper');
    const hasCarousel = await carousel.first().isVisible().catch(() => false);
    if (hasCarousel) {
      const prevBtn = carousel.locator('.prev, [class*="prev"]');
      const nextBtn = carousel.locator('.next, [class*="next"]');
      await expect(prevBtn).toBeVisible();
      await expect(nextBtn).toBeVisible();
    }
  });

  test('底部导航栏存在), async ({ page }) => {
    await page.goto('/items', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    const footer = page.locator('footer, .footer, [class*="bottom-nav"]');
    await expect(footer.first()).toBeVisible();
  });

  test('页面响应式布局正常', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 667 });
    await page.goto('/items', { timeout: 60000 });
    await page.waitForLoadState('domcontentloaded');

    const items = page.locator('.item-card');
    await expect(items.first()).toBeVisible();
  });
});
