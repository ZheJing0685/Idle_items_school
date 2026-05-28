import { test, expect } from '@playwright/test'

test.describe('订单流程 E2E 测试', () => {
  test('订单列表页正确加载', async ({ page }) => {
    await page.goto('/orders')
    await page.waitForLoadState('networkidle')

    await expect(page).toHaveURL(/\/orders/)
  })

  test('订单列表页标题存在', async ({ page }) => {
    await page.goto('/orders')
    await page.waitForLoadState('networkidle')

    await expect(page.locator('h1, h2').first()).toBeVisible()
  })

  test('订单标签页存在', async ({ page }) => {
    await page.goto('/orders')
    await page.waitForLoadState('networkidle')

    await expect(page.locator('.el-tabs, .tab-list').first()).toBeVisible()
  })

  test('订单详情页正确加载', async ({ page }) => {
    await page.goto('/orders/1')
    await page.waitForLoadState('networkidle')

    await expect(page.locator('.order-detail, .order-info').first()).toBeVisible()
  })

  test('订单状态显示', async ({ page }) => {
    await page.goto('/orders/1')
    await page.waitForLoadState('networkidle')

    await expect(page.locator('.order-status, .status-badge').first()).toBeVisible()
  })
})

test.describe('用户中心 E2E 测试', () => {
  test('用户中心页正确加载', async ({ page }) => {
    await page.goto('/user')
    await page.waitForLoadState('networkidle')

    await expect(page).toHaveURL(/\/user/)
  })

  test('用户信息显示', async ({ page }) => {
    await page.goto('/user')
    await page.waitForLoadState('networkidle')

    await expect(page.locator('.user-info, .profile-card').first()).toBeVisible()
  })

  test('导航菜单存在', async ({ page }) => {
    await page.goto('/user')
    await page.waitForLoadState('networkidle')

    await expect(page.locator('.sidebar, .nav-menu').first()).toBeVisible()
  })
})

test.describe('收藏列表 E2E 测试', () => {
  test('收藏页正确加载', async ({ page }) => {
    await page.goto('/favorites')
    await page.waitForLoadState('networkidle')

    await expect(page).toHaveURL(/\/favorites/)
  })

  test('收藏列表标题存在', async ({ page }) => {
    await page.goto('/favorites')
    await page.waitForLoadState('networkidle')

    await expect(page.locator('h1, h2').first()).toBeVisible()
  })
})

test.describe('聊天功能 E2E 测试', () => {
  test('聊天页正确加载', async ({ page }) => {
    await page.goto('/chat')
    await page.waitForLoadState('networkidle')

    await expect(page).toHaveURL(/\/chat/)
  })

  test('聊天列表存在', async ({ page }) => {
    await page.goto('/chat')
    await page.waitForLoadState('networkidle')

    await expect(page.locator('.chat-list, .conversation-list').first()).toBeVisible()
  })
})

test.describe('通知中心 E2E 测试', () => {
  test('通知页正确加载', async ({ page }) => {
    await page.goto('/notifications')
    await page.waitForLoadState('networkidle')

    await expect(page).toHaveURL(/\/notifications/)
  })

  test('通知列表存在', async ({ page }) => {
    await page.goto('/notifications')
    await page.waitForLoadState('networkidle')

    await expect(page.locator('.notification-list, .message-list').first()).toBeVisible()
  })
})

test.describe('个人资料 E2E 测试', () => {
  test('个人资料页正确加载', async ({ page }) => {
    await page.goto('/profile')
    await page.waitForLoadState('networkidle')

    await expect(page).toHaveURL(/\/profile/)
  })

  test('个人资料表单存在', async ({ page }) => {
    await page.goto('/profile')
    await page.waitForLoadState('networkidle')

    await expect(page.locator('form, .el-form, .profile-form').first()).toBeVisible()
  })
})
