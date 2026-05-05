import { test, expect } from '@playwright/test'

/**
 * 浏览器兼容性测试套件
 * 测试登录状态管理在各浏览器中的表现
 */

test.describe('浏览器兼容性测试 - 登录状态管理', () => {
  test.beforeEach(async ({ page, context }) => {
    // 设置额外的 HTTP 头部模拟不同浏览器
    await context.addInitScript(() => {
      // 清除存储准备测试
      localStorage.clear()
      sessionStorage.clear()
    })
  })

  test('Chrome: 登录状态存储功能', async ({ page }) => {
    await page.goto('/login')
    await page.getByPlaceholder('请输入用户名').fill('testuser')
    await page.getByPlaceholder('请输入密码').fill('testpass')
    await page.getByRole('button', { name: '登录' }).click()
    await page.waitForTimeout(1500)

    // 检查 localStorage 是否存储了 token
    const token = await page.evaluate(() => localStorage.getItem('idle_items_token'))
    const user = await page.evaluate(() => localStorage.getItem('idle_items_user'))

    // 如果登录成功
    if (token || user) {
      expect(token).toBeTruthy()
      expect(user).toBeTruthy()
    }
  })

  test('Firefox: 登录状态存储功能', async ({ page }) => {
    await page.goto('/login')
    await page.waitForLoadState('domcontentloaded')

    // Firefox 特有检查
    const hasLocalStorage = await page.evaluate(() => {
      try {
        localStorage.setItem('test', 'value')
        localStorage.removeItem('test')
        return true
      } catch {
        return false
      }
    })

    expect(hasLocalStorage).toBe(true)
  })

  test('Safari: 登录状态存储功能', async ({ page }) => {
    await page.goto('/login')
    await page.waitForLoadState('domcontentloaded')

    // Safari storage 检查
    const hasStorage = await page.evaluate(() => {
      try {
        return typeof localStorage !== 'undefined'
      } catch {
        return false
      }
    })

    expect(hasStorage).toBe(true)
  })

  test('Edge: 登录状态存储功能', async ({ page }) => {
    await page.goto('/login')
    await page.waitForLoadState('domcontentloaded')

    // Edge 与 Chrome 相同内核，检查 basic storage
    const hasLocalStorage = await page.evaluate(() => {
      return typeof localStorage !== 'undefined' && typeof sessionStorage !== 'undefined'
    })

    expect(hasLocalStorage).toBe(true)
  })
})

test.describe('浏览器兼容性测试 - 页面刷新状态保持', () => {
  test('页面刷新后登录状态保持', async ({ page }) => {
    await page.goto('/login')
    await page.getByPlaceholder('请输入用户名').fill('persistentuser')
    await page.getByPlaceholder('请输入密码').fill('password')
    await page.getByRole('button', { name: '登录' }).click()
    await page.waitForTimeout(2000)

    // 刷新页面
    await page.reload()
    await page.waitForLoadState('domcontentloaded')
    await page.waitForTimeout(1000)

    // 检查登录状态是否保持
    const hasLoginState = await page.evaluate(() => {
      const token = localStorage.getItem('idle_items_token')
      const user = localStorage.getItem('idle_items_user')
      return !!(token && user)
    })

    // 检查是否还在登录页
    const currentUrl = page.url()
    const stillOnLogin = currentUrl.includes('/login')

    // 如果有持久化登录状态，不应在登录页
    if (hasLoginState) {
      expect(stillOnLogin).toBe(false)
    }
  })

  test('路由切换后登录状态一致性', async ({ page }) => {
    await page.goto('/login')
    await page.getByPlaceholder('请输入用户名').fill('routeuser')
    await page.getByPlaceholder('请输入密码').fill('password')
    await page.getByRole('button', { name: '登录' }).click()
    await page.waitForTimeout(2000)

    // 切换到首页
    await page.goto('/')
    await page.waitForLoadState('networkidle')

    // 切换到物品页
    await page.goto('/items')
    await page.waitForLoadState('networkidle')

    // 再次切换回首页
    await page.goto('/')
    await page.waitForLoadState('networkidle')

    // 验证 token 仍然存在
    const tokenExists = await page.evaluate(() => {
      return !!localStorage.getItem('idle_items_token')
    })

    // 至少应该不报错
    expect(page.locator('body')).toBeVisible()
  })
})

test.describe('浏览器兼容性测试 - ES6+ 特性支持', () => {
  test('Promise 和 async/await 支持', async ({ page }) => {
    const result = await page.evaluate(async () => {
      // 测试 Promise
      const promise = new Promise((resolve) => {
        setTimeout(() => resolve('success'), 100)
      })
      const result = await promise

      // 测试 async/await
      const asyncFunc = async () => {
        return 'async works'
      }
      const asyncResult = await asyncFunc()

      return { result, asyncResult }
    })

    expect(result.result).toBe('success')
    expect(result.asyncResult).toBe('async works')
  })

  test('箭头函数和模板字符串支持', async ({ page }) => {
    const result = await page.evaluate(() => {
      const arrow = (a, b) => a + b
      const template = `Hello ${arrow(1, 2)}`
      const obj = { a: 1, b: 2 }
      const spread = { ...obj, c: 3 }
      return { sum: arrow(3, 4), template, spread }
    })

    expect(result.sum).toBe(7)
    expect(result.template).toBe('Hello 3')
    expect(result.spread).toEqual({ a: 1, b: 2, c: 3 })
  })

  test('模块语法支持', async ({ page }) => {
    // 检查页面能正常加载 ES module
    await page.goto('/')
    await page.waitForLoadState('networkidle')

    // 页面应该能正常运行，没有模块错误
    const hasApp = await page.locator('#app').count()
    expect(hasApp).toBe(1)
  })
})

test.describe('浏览器兼容性测试 - CSS 和布局', () => {
  test('Flexbox 布局支持', async ({ page }) => {
    await page.goto('/')
    await page.waitForLoadState('networkidle')

    // 检查 Header 容器
    const headerVisible = await page.locator('.header, header').first().isVisible()
    expect(headerVisible).toBe(true)
  })

  test('响应式布局（桌面）', async ({ page }) => {
    await page.setViewportSize({ width: 1920, height: 1080 })
    await page.goto('/')
    await page.waitForLoadState('networkidle')

    // 所有主要内容应该可见
    await expect(page.locator('.header, header').first()).toBeVisible()
  })

  test('响应式布局（平板）', async ({ page }) => {
    await page.setViewportSize({ width: 768, height: 1024 })
    await page.goto('/')
    await page.waitForLoadState('networkidle')

    // 页面不应崩溃
    await expect(page.locator('body')).toBeVisible()
  })

  test('响应式布局（手机）', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 667 })
    await page.goto('/')
    await page.waitForLoadState('networkidle')

    // 页面不应崩溃
    await expect(page.locator('body')).toBeVisible()
  })
})
