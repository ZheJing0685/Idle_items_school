import { test, expect } from '@playwright/test'

test.describe('物品流程 E2E 测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/items')
    await page.waitForLoadState('networkidle')
  })

  test('物品列表页正确加载', async ({ page }) => {
    await expect(page).toHaveURL(/\/items/)
  })

  test('物品列表页标题存在', async ({ page }) => {
    await expect(page.locator('h1, h2').first()).toBeVisible()
  })

  test('搜索功能存在', async ({ page }) => {
    const searchInput = page.getByPlaceholder(/搜索/)
    await expect(searchInput).toBeVisible()
  })

  test('分类筛选存在', async ({ page }) => {
    await expect(page.locator('.category-filter, .filter-section').first()).toBeVisible()
  })

  test('物品卡片存在', async ({ page }) => {
    const items = page.locator('.item-card, .product-card')
    await expect(items.first()).toBeVisible()
  })

  test('点击物品卡片跳转详情页', async ({ page }) => {
    const itemCard = page.locator('.item-card, .product-card').first()
    if (await itemCard.isVisible()) {
      await itemCard.click()
      await page.waitForLoadState('networkidle')
      await expect(page).toHaveURL(/\/items\/\d+/)
    }
  })
})

test.describe('物品详情页 E2E 测试', () => {
  test('物品详情页正确加载', async ({ page }) => {
    await page.goto('/items/1')
    await page.waitForLoadState('networkidle')

    await expect(page.locator('.item-detail, .product-detail').first()).toBeVisible()
  })

  test('物品标题显示', async ({ page }) => {
    await page.goto('/items/1')
    await page.waitForLoadState('networkidle')

    await expect(page.locator('h1, h2').first()).toBeVisible()
  })

  test('物品价格显示', async ({ page }) => {
    await page.goto('/items/1')
    await page.waitForLoadState('networkidle')

    await expect(page.locator('.price, .item-price').first()).toBeVisible()
  })

  test('购买按钮存在', async ({ page }) => {
    await page.goto('/items/1')
    await page.waitForLoadState('networkidle')

    const buyButton = page.getByRole('button', { name: /购买|立即购买/ })
    await expect(buyButton).toBeVisible()
  })

  test('返回列表页链接', async ({ page }) => {
    await page.goto('/items/1')
    await page.waitForLoadState('networkidle')

    const backLink = page.getByText(/返回|列表/)
    if (await backLink.isVisible()) {
      await backLink.click()
      await expect(page).toHaveURL(/\/items/)
    }
  })
})

test.describe('发布物品 E2E 测试', () => {
  test('发布页面正确加载', async ({ page }) => {
    await page.goto('/publish')
    await page.waitForLoadState('networkidle')

    await expect(page).toHaveURL(/\/publish/)
  })

  test('发布表单存在', async ({ page }) => {
    await page.goto('/publish')
    await page.waitForLoadState('networkidle')

    await expect(page.locator('form, .el-form').first()).toBeVisible()
  })

  test('标题输入框存在', async ({ page }) => {
    await page.goto('/publish')
    await page.waitForLoadState('networkidle')

    const titleInput = page.getByPlaceholder(/标题|物品名称/)
    await expect(titleInput).toBeVisible()
  })

  test('价格输入框存在', async ({ page }) => {
    await page.goto('/publish')
    await page.waitForLoadState('networkidle')

    const priceInput = page.getByPlaceholder(/价格/)
    await expect(priceInput).toBeVisible()
  })

  test('提交按钮存在', async ({ page }) => {
    await page.goto('/publish')
    await page.waitForLoadState('networkidle')

    const submitButton = page.getByRole('button', { name: /发布|提交/ })
    await expect(submitButton).toBeVisible()
  })
})
