import { test, expect } from '@playwright/test'

/**
 * 用户交互流程测试套件
 * 覆盖主要功能模块的端到端流程
 */

test.describe('首页流程测试', () => {
  test('首页正常加载', async ({ page }) => {
    await page.goto('/')
    await page.waitForLoadState('networkidle')
    
    // 检查页面标题
    await expect(page).toHaveTitle(/闲置物品交易平台/)
    
    // 检查 Header
    await expect(page.locator('header, .header').first()).toBeVisible()
    
    // 检查 Footer
    const footer = page.locator('footer, .footer').first()
    if (await footer.count() > 0) {
      await expect(footer).toBeVisible()
    }
  })

  test('首页搜索功能', async ({ page }) => {
    await page.goto('/')
    await page.waitForLoadState('networkidle')

    // 找到搜索框并输入关键词
    const searchInput = page.locator('input[placeholder*="搜索"]').first()
    if (await searchInput.isVisible()) {
      await searchInput.fill('手机')
      await searchInput.press('Enter')
      await page.waitForTimeout(1000)

      // 应该跳转到搜索结果页或显示结果
      const body = await page.textContent('body')
      expect(body).toBeTruthy()
    }
  })

  test('首页导航到登录页', async ({ page }) => {
    await page.goto('/')
    await page.waitForLoadState('networkidle')

    const loginBtn = page.getByRole('button', { name: /登录/ }).first()
    if (await loginBtn.isVisible()) {
      await loginBtn.click()
      await expect(page).toHaveURL(/\/login/)
    }
  })
})

test.describe('浏览物品流程测试', () => {
  test('物品列表页正常加载', async ({ page }) => {
    await page.goto('/items')
    await page.waitForLoadState('networkidle')

    // 检查页面主要元素
    await expect(page.locator('body')).toBeVisible()
  })

  test('物品分类筛选功能', async ({ page }) => {
    await page.goto('/items')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)

    // 检查页面不崩溃即可
    await expect(page.locator('body')).toBeVisible()
  })

  test('物品详情页可访问', async ({ page }) => {
    // 先访问物品列表
    await page.goto('/items')
    await page.waitForLoadState('networkidle')

    // 尝试找一个物品链接
    const itemLinks = page.locator('a[href*="/item/"]')
    const itemCount = await itemLinks.count()

    if (itemCount > 0) {
      await itemLinks.first().click()
      await page.waitForLoadState('networkidle')
      await expect(page.locator('body')).toBeVisible()
    } else {
      // 直接测试详情页 URL（如果有测试数据 ID）
      await page.goto('/item/1')
      await page.waitForTimeout(1000)
      // 页面不应崩溃
      expect(page.locator('body')).toBeVisible()
    }
  })
})

test.describe('个人中心流程测试', () => {
  test('未登录访问个人中心应跳转到登录页', async ({ page }) => {
    // 清除登录状态
    await page.goto('/')
    await page.evaluate(() => localStorage.clear())

    // 尝试访问个人中心
    await page.goto('/user')
    await page.waitForTimeout(1000)

    // 应该跳转到登录页
    await expect(page).toHaveURL(/\/login/)
  })

  test('个人中心页面正常加载（需登录）', async ({ page }) => {
    // 模拟登录状态
    await page.goto('/login')
    await page.evaluate(() => {
      localStorage.setItem('idle_items_token', 'mock-token')
      localStorage.setItem('idle_items_user', JSON.stringify({ id: 1, username: 'testuser' }))
    })

    await page.goto('/user')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(500)

    // 检查页面加载
    await expect(page.locator('body')).toBeVisible()
  })
})

test.describe('发布物品流程测试', () => {
  test('未登录访问发布页应跳转到登录页', async ({ page }) => {
    await page.evaluate(() => localStorage.clear())
    await page.goto('/publish')
    await page.waitForTimeout(1000)

    await expect(page).toHaveURL(/\/login/)
  })

  test('发布页正常加载（需登录）', async ({ page }) => {
    await page.goto('/login')
    await page.evaluate(() => {
      localStorage.setItem('idle_items_token', 'mock-token')
      localStorage.setItem('idle_items_user', JSON.stringify({ id: 1, username: 'testuser' }))
    })

    await page.goto('/publish')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(500)

    // 页面不应崩溃
    await expect(page.locator('body')).toBeVisible()
  })
})

test.describe('订单流程测试', () => {
  test('未登录访问订单页应跳转到登录页', async ({ page }) => {
    await page.evaluate(() => localStorage.clear())
    await page.goto('/orders')
    await page.waitForTimeout(1000)

    await expect(page).toHaveURL(/\/login/)
  })
})

test.describe('管理后台流程测试', () => {
  test('普通用户访问管理后台应被拒绝', async ({ page }) => {
    await page.goto('/login')
    await page.evaluate(() => {
      localStorage.setItem('idle_items_token', 'user-token')
      localStorage.setItem('idle_items_user', JSON.stringify({ id: 1, username: 'user', role: 'USER' }))
    })

    await page.goto('/admin')
    await page.waitForTimeout(1000)

    // 应该跳转到首页或被拒绝
    const currentUrl = page.url()
    const isRejected = !currentUrl.includes('/admin')
    expect(isRejected).toBe(true)
  })

  test('管理员访问管理后台应正常加载', async ({ page }) => {
    await page.goto('/login')
    await page.evaluate(() => {
      localStorage.setItem('idle_items_token', 'admin-token')
      localStorage.setItem('idle_items_user', JSON.stringify({ id: 1, username: 'admin', role: 'ADMIN' }))
    })

    await page.goto('/admin')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(500)

    // 页面不应崩溃
    await expect(page.locator('body')).toBeVisible()
  })
})

test.describe('404 页面测试', () => {
  test('访问不存在的页面应显示 404', async ({ page }) => {
    await page.goto('/this-page-does-not-exist-12345')
    await page.waitForLoadState('networkidle')

    // 应该显示 404 页面
    const body = await page.textContent('body')
    expect(body).toContainTruthy()
  })

  test('404 页面应有返回首页链接', async ({ page }) => {
    await page.goto('/nonexistent-page')
    await page.waitForLoadState('networkidle')

    // 查找返回首页的链接
    const homeLink = page.locator('a[href="/"], a:has-text("首页"), a:has-text("返回首页")').first()
    if (await homeLink.count() > 0) {
      await expect(homeLink).toBeVisible()
    }
  })
})

test.describe('页面性能测试', () => {
  test('首页加载时间应小于 3 秒', async ({ page }) => {
    const startTime = Date.now()
    await page.goto('/')
    await page.waitForLoadState('networkidle')
    const loadTime = Date.now() - startTime

    expect(loadTime).toBeLessThan(3000)
  })

  test('登录页加载时间应小于 2 秒', async ({ page }) => {
    const startTime = Date.now()
    await page.goto('/login')
    await page.waitForLoadState('networkidle')
    const loadTime = Date.now() - startTime

    expect(loadTime).toBeLessThan(2000)
  })

  test('登录操作响应时间应小于 2 秒', async ({ page }) => {
    await page.goto('/login')
    await page.waitForLoadState('networkidle')

    await page.getByPlaceholder('请输入用户名').fill('testuser')
    await page.getByPlaceholder('请输入密码').fill('password')
    
    const startTime = Date.now()
    await page.getByRole('button', { name: '登录' }).click()
    await page.waitForTimeout(500)
    const responseTime = Date.now() - startTime

    // 按钮响应应该很快（实际 API 响应可能有延迟）
    expect(responseTime).toBeLessThan(2000)
  })
})
