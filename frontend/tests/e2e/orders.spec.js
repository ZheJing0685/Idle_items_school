import { test, expect } from '@playwright/test'

/**
 * 订单流程 E2E 测试
 * 覆盖订单的创建、支付、发货、收货等核心流程
 */

test.describe('订单创建流程测试', () => {
  test.beforeEach(async ({ page }) => {
    // 模拟登录状态
    await page.goto('/login')
    await page.evaluate(() => {
      localStorage.setItem('idle_items_token', 'mock-token')
      localStorage.setItem('idle_items_user', JSON.stringify({
        id: 1,
        username: 'testuser',
        role: 'STUDENT',
        verified: true
      }))
    })
  })

  test('未登录访问订单页应跳转到登录页', async ({ page }) => {
    // 清除登录状态
    await page.evaluate(() => localStorage.clear())

    await page.goto('/orders')
    await page.waitForTimeout(1000)

    // 应该跳转到登录页
    await expect(page).toHaveURL(/\/login/)
  })

  test('订单列表页正常加载', async ({ page }) => {
    await page.goto('/orders')
    await page.waitForLoadState('networkidle')

    // 检查页面加载
    await expect(page.locator('body')).toBeVisible()
  })

  test('订单状态筛选功能', async ({ page }) => {
    await page.goto('/orders')
    await page.waitForLoadState('networkidle')

    // 查找状态筛选器
    const statusFilter = page.locator('.status-filter, .el-tabs, .filter-tabs').first()
    if (await statusFilter.isVisible()) {
      // 点击一个状态
      const firstStatus = statusFilter.locator('button, .el-tabs__item, .filter-item').first()
      if (await firstStatus.isVisible()) {
        await firstStatus.click()
        await page.waitForTimeout(1000)

        // 页面不应崩溃
        await expect(page.locator('body')).toBeVisible()
      }
    }
  })

  test('订单详情页可访问', async ({ page }) => {
    await page.goto('/orders')
    await page.waitForLoadState('networkidle')

    // 尝试点击第一个订单
    const firstOrder = page.locator('.order-card, .order-item, .order-list-item').first()
    if (await firstOrder.isVisible()) {
      await firstOrder.click()
      await page.waitForLoadState('networkidle')

      // 检查详情页加载
      await expect(page.locator('body')).toBeVisible()
    }
  })
})

test.describe('订单操作流程测试', () => {
  test.beforeEach(async ({ page }) => {
    // 模拟登录状态
    await page.goto('/login')
    await page.evaluate(() => {
      localStorage.setItem('idle_items_token', 'mock-token')
      localStorage.setItem('idle_items_user', JSON.stringify({
        id: 1,
        username: 'testuser',
        role: 'STUDENT',
        verified: true
      }))
    })
  })

  test('从物品详情页创建订单', async ({ page }) => {
    // 访问物品详情页
    await page.goto('/item/1')
    await page.waitForLoadState('networkidle')
    await expect(page.locator('.order-status, .success-message')).toBeVisible({ timeout: 5000 })

    // 查找购买按钮
    const buyBtn = page.locator('button:has-text("购买"), button:has-text("立即购买"), .buy-btn').first()
    if (await buyBtn.isVisible()) {
      await buyBtn.click()
      await page.waitForTimeout(1000)

      // 应该跳转到订单确认页或显示确认对话框
      const confirmDialog = page.locator('.el-dialog, .confirm-dialog, .order-confirm')
      const orderPage = page.locator('body')

      const hasDialog = await confirmDialog.isVisible().catch(() => false)
      const isOnOrderPage = page.url().includes('/order')

      expect(hasDialog || isOnOrderPage).toBeTruthy()
    }
  })

  test('订单确认页元素检查', async ({ page }) => {
    // 直接访问订单确认页
    await page.goto('/order/confirm')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)

    // 页面不应崩溃
    await expect(page.locator('body')).toBeVisible()
  })

  test('订单支付页面可访问', async ({ page }) => {
    // 直接访问订单支付页
    await page.goto('/order/pay/1')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)

    // 页面不应崩溃
    await expect(page.locator('body')).toBeVisible()
  })

  test('订单支付方式选择', async ({ page }) => {
    await page.goto('/order/pay/1')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)

    // 查找支付方式选择器
    const paymentMethods = page.locator('.payment-method, .el-radio, input[name="payment"]')
    const methodCount = await paymentMethods.count()

    if (methodCount > 0) {
      // 选择第一个支付方式
      await paymentMethods.first().click()
      await page.waitForTimeout(500)

      // 页面不应崩溃
      await expect(page.locator('body')).toBeVisible()
    }
  })

  test('订单取消功能', async ({ page }) => {
    await page.goto('/orders')
    await page.waitForLoadState('networkidle')

    // 查找取消按钮
    const cancelBtn = page.locator('button:has-text("取消"), .cancel-btn').first()
    if (await cancelBtn.isVisible()) {
      await cancelBtn.click()
      await page.waitForTimeout(500)

      // 应该显示确认对话框
      const confirmDialog = page.locator('.el-dialog, .confirm-dialog, .el-message-box')
      if (await confirmDialog.isVisible()) {
        // 确认取消
        const confirmBtn = confirmDialog.locator('button:has-text("确定"), button:has-text("确认")').first()
        if (await confirmBtn.isVisible()) {
          await confirmBtn.click()
          await page.waitForTimeout(1000)
        }
      }

      // 页面不应崩溃
      await expect(page.locator('body')).toBeVisible()
    }
  })
})

test.describe('卖家订单管理测试', () => {
  test.beforeEach(async ({ page }) => {
    // 模拟卖家登录状态
    await page.goto('/login')
    await page.evaluate(() => {
      localStorage.setItem('idle_items_token', 'mock-seller-token')
      localStorage.setItem('idle_items_user', JSON.stringify({
        id: 2,
        username: 'seller',
        role: 'STUDENT',
        verified: true
      }))
    })
  })

  test('卖家订单列表页正常加载', async ({ page }) => {
    await page.goto('/orders/seller')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)

    // 页面不应崩溃
    await expect(page.locator('body')).toBeVisible()
  })

  test('卖家发货功能', async ({ page }) => {
    await page.goto('/orders/seller')
    await page.waitForLoadState('networkidle')

    // 查找发货按钮
    const shipBtn = page.locator('button:has-text("发货"), .ship-btn').first()
    if (await shipBtn.isVisible()) {
      await shipBtn.click()
      await page.waitForTimeout(500)

      // 应该显示发货对话框或跳转到发货页
      const dialog = page.locator('.el-dialog, .ship-dialog')
      if (await dialog.isVisible()) {
        // 填写物流信息
        const trackingInput = dialog.locator('input[placeholder*="物流"], input[placeholder*="单号"]').first()
        if (await trackingInput.isVisible()) {
          await trackingInput.fill('SF1234567890')
        }

        // 确认发货
        const confirmBtn = dialog.locator('button:has-text("确定"), button:has-text("确认发货")').first()
        if (await confirmBtn.isVisible()) {
          await confirmBtn.click()
          await page.waitForTimeout(1000)
        }
      }

      // 页面不应崩溃
      await expect(page.locator('body')).toBeVisible()
    }
  })
})

test.describe('订单响应式测试', () => {
  test.beforeEach(async ({ page }) => {
    // 模拟登录状态
    await page.goto('/login')
    await page.evaluate(() => {
      localStorage.setItem('idle_items_token', 'mock-token')
      localStorage.setItem('idle_items_user', JSON.stringify({
        id: 1,
        username: 'testuser',
        role: 'STUDENT',
        verified: true
      }))
    })
  })

  test('订单列表在移动端正常显示', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 667 })
    await page.goto('/orders')
    await page.waitForLoadState('networkidle')

    // 页面不应崩溃
    await expect(page.locator('body')).toBeVisible()
  })

  test('订单详情在移动端正常显示', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 667 })
    await page.goto('/order/1')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)

    // 页面不应崩溃
    await expect(page.locator('body')).toBeVisible()
  })

  test('订单支付在移动端正常显示', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 667 })
    await page.goto('/order/pay/1')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)

    // 页面不应崩溃
    await expect(page.locator('body')).toBeVisible()
  })
})

test.describe('订单性能测试', () => {
  test('订单列表页加载时间应小于 3 秒', async ({ page }) => {
    // 模拟登录状态
    await page.goto('/login')
    await page.evaluate(() => {
      localStorage.setItem('idle_items_token', 'mock-token')
      localStorage.setItem('idle_items_user', JSON.stringify({
        id: 1,
        username: 'testuser',
        role: 'STUDENT',
        verified: true
      }))
    })

    const startTime = Date.now()
    await page.goto('/orders')
    await page.waitForLoadState('networkidle')
    const loadTime = Date.now() - startTime

    expect(loadTime).toBeLessThan(3000)
  })

  test('订单详情页加载时间应小于 2 秒', async ({ page }) => {
    // 模拟登录状态
    await page.goto('/login')
    await page.evaluate(() => {
      localStorage.setItem('idle_items_token', 'mock-token')
      localStorage.setItem('idle_items_user', JSON.stringify({
        id: 1,
        username: 'testuser',
        role: 'STUDENT',
        verified: true
      }))
    })

    const startTime = Date.now()
    await page.goto('/order/1')
    await page.waitForLoadState('networkidle')
    const loadTime = Date.now() - startTime

    expect(loadTime).toBeLessThan(2000)
  })
})
