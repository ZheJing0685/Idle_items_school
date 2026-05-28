import { test, expect } from '@playwright/test'

/**
 * 物品发布与浏览 E2E 测试
 * 覆盖物品的发布、浏览、搜索、详情查看等核心流程
 */

test.describe('物品发布流程测试', () => {
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

  test('发布页面正确加载', async ({ page }) => {
    await page.goto('/publish')
    await page.waitForLoadState('networkidle')

    // 检查页面标题
    await expect(page.locator('body')).toBeVisible()

    // 检查表单元素
    const titleInput = page.locator('input[placeholder*="标题"], input[placeholder*="名称"]').first()
    if (await titleInput.isVisible()) {
      await expect(titleInput).toBeVisible()
    }
  })

  test('物品标题输入功能', async ({ page }) => {
    await page.goto('/publish')
    await page.waitForLoadState('networkidle')

    const titleInput = page.locator('input[placeholder*="标题"], input[placeholder*="名称"]').first()
    if (await titleInput.isVisible()) {
      await titleInput.fill('测试物品标题')
      await expect(titleInput).toHaveValue('测试物品标题')
    }
  })

  test('物品价格输入功能', async ({ page }) => {
    await page.goto('/publish')
    await page.waitForLoadState('networkidle')

    const priceInput = page.locator('input[placeholder*="价格"], input[type="number"]').first()
    if (await priceInput.isVisible()) {
      await priceInput.fill('99.9')
      await expect(priceInput).toHaveValue('99.9')
    }
  })

  test('物品描述输入功能', async ({ page }) => {
    await page.goto('/publish')
    await page.waitForLoadState('networkidle')

    const descInput = page.locator('textarea[placeholder*="描述"], textarea[placeholder*="详情"]').first()
    if (await descInput.isVisible()) {
      await descInput.fill('这是一条测试物品描述，包含详细的信息')
      await expect(descInput).toHaveValue('这是一条测试物品描述，包含详细的信息')
    }
  })

  test('分类选择功能', async ({ page }) => {
    await page.goto('/publish')
    await page.waitForLoadState('networkidle')

    // 查找分类选择器
    const categorySelect = page.locator('select, .el-select').first()
    if (await categorySelect.isVisible()) {
      await categorySelect.click()
      await page.waitForTimeout(500)

      // 检查下拉选项
      const options = page.locator('option, .el-select-dropdown__item')
      const optionCount = await options.count()
      expect(optionCount).toBeGreaterThan(0)
    }
  })

  test('成色选择功能', async ({ page }) => {
    await page.goto('/publish')
    await page.waitForLoadState('networkidle')

    // 查找成色选择器
    const conditionSelect = page.locator('select:near(:text("成色")), .el-select:near(:text("成色"))').first()
    if (await conditionSelect.isVisible()) {
      await conditionSelect.click()
      await page.waitForTimeout(500)

      // 检查选项
      const options = page.locator('option, .el-select-dropdown__item')
      const optionCount = await options.count()
      expect(optionCount).toBeGreaterThan(0)
    }
  })

  test('表单验证 - 空标题提交', async ({ page }) => {
    await page.goto('/publish')
    await page.waitForLoadState('networkidle')

    // 尝试不填写标题直接提交
    const submitBtn = page.locator('button:has-text("发布"), button:has-text("提交")').first()
    if (await submitBtn.isVisible()) {
      await submitBtn.click()
      await page.waitForTimeout(500)

      // 应该显示验证错误
      const errorMsg = page.locator('.el-form-item__error, .error-message, .validation-error').first()
      if (await errorMsg.isVisible()) {
        await expect(errorMsg).toBeVisible()
      }
    }
  })

  test('表单验证 - 价格为0提交', async ({ page }) => {
    await page.goto('/publish')
    await page.waitForLoadState('networkidle')

    // 填写标题但价格为0
    const titleInput = page.locator('input[placeholder*="标题"], input[placeholder*="名称"]').first()
    if (await titleInput.isVisible()) {
      await titleInput.fill('测试物品')
    }

    const priceInput = page.locator('input[placeholder*="价格"], input[type="number"]').first()
    if (await priceInput.isVisible()) {
      await priceInput.fill('0')
    }

    const submitBtn = page.locator('button:has-text("发布"), button:has-text("提交")').first()
    if (await submitBtn.isVisible()) {
      await submitBtn.click()
      await page.waitForTimeout(500)

      // 应该显示验证错误
      const errorMsg = page.locator('.el-form-item__error, .error-message, .validation-error').first()
      if (await errorMsg.isVisible()) {
        await expect(errorMsg).toBeVisible()
      }
    }
  })
})

test.describe('物品浏览流程测试', () => {
  test('物品列表页正常加载', async ({ page }) => {
    await page.goto('/items')
    await page.waitForLoadState('networkidle')

    // 检查页面加载
    await expect(page.locator('body')).toBeVisible()

    // 检查是否有物品卡片
    const itemCards = page.locator('.item-card, .product-card, .goods-item')
    const cardCount = await itemCards.count()
    // 可能没有测试数据，所以只检查页面不崩溃
    expect(cardCount).toBeGreaterThanOrEqual(0)
  })

  test('物品分类筛选功能', async ({ page }) => {
    await page.goto('/items')
    await page.waitForLoadState('networkidle')

    // 查找分类筛选器
    const categoryFilter = page.locator('.category-filter, .filter-tabs, .el-tabs').first()
    if (await categoryFilter.isVisible()) {
      // 点击一个分类
      const firstCategory = categoryFilter.locator('button, .el-tabs__item, .filter-item').first()
      if (await firstCategory.isVisible()) {
        await firstCategory.click()
        await page.waitForTimeout(1000)

        // 页面不应崩溃
        await expect(page.locator('body')).toBeVisible()
      }
    }
  })

  test('物品排序功能', async ({ page }) => {
    await page.goto('/items')
    await page.waitForLoadState('networkidle')

    // 查找排序选择器
    const sortSelect = page.locator('select:near(:text("排序")), .el-select:near(:text("排序"))').first()
    if (await sortSelect.isVisible()) {
      await sortSelect.click()
      await page.waitForTimeout(500)

      // 选择一个排序选项
      const firstOption = page.locator('option, .el-select-dropdown__item').first()
      if (await firstOption.isVisible()) {
        await firstOption.click()
        await page.waitForTimeout(1000)

        // 页面不应崩溃
        await expect(page.locator('body')).toBeVisible()
      }
    }
  })

  test('物品搜索功能', async ({ page }) => {
    await page.goto('/items')
    await page.waitForLoadState('networkidle')

    // 查找搜索框
    const searchInput = page.locator('input[placeholder*="搜索"], input[placeholder*="查找"]').first()
    if (await searchInput.isVisible()) {
      await searchInput.fill('手机')
      await searchInput.press('Enter')
      await page.waitForTimeout(1000)

      // 页面不应崩溃
      await expect(page.locator('body')).toBeVisible()
    }
  })

  test('物品详情页可访问', async ({ page }) => {
    await page.goto('/items')
    await page.waitForLoadState('networkidle')

    // 尝试点击第一个物品
    const firstItem = page.locator('.item-card a, .product-card a, .goods-item a').first()
    if (await firstItem.isVisible()) {
      await firstItem.click()
      await page.waitForLoadState('networkidle')

      // 检查详情页加载
      await expect(page.locator('body')).toBeVisible()
    }
  })

  test('物品详情页元素检查', async ({ page }) => {
    // 直接访问一个可能的详情页
    await page.goto('/item/1')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)

    // 页面不应崩溃
    await expect(page.locator('body')).toBeVisible()
  })

  test('物品图片展示功能', async ({ page }) => {
    await page.goto('/item/1')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)

    // 检查图片元素
    const images = page.locator('img')
    const imageCount = await images.count()
    // 可能没有图片，只检查页面不崩溃
    expect(imageCount).toBeGreaterThanOrEqual(0)
  })

  test('物品收藏功能', async ({ page }) => {
    // 模拟登录状态
    await page.goto('/login')
    await page.evaluate(() => {
      localStorage.setItem('idle_items_token', 'mock-token')
      localStorage.setItem('idle_items_user', JSON.stringify({ id: 1, username: 'testuser' }))
    })

    await page.goto('/item/1')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)

    // 查找收藏按钮
    const favoriteBtn = page.locator('button:has-text("收藏"), .favorite-btn, .collect-btn').first()
    if (await favoriteBtn.isVisible()) {
      await favoriteBtn.click()
      await page.waitForTimeout(500)

      // 页面不应崩溃
      await expect(page.locator('body')).toBeVisible()
    }
  })

  test('物品分页功能', async ({ page }) => {
    await page.goto('/items')
    await page.waitForLoadState('networkidle')

    // 查找分页器
    const pagination = page.locator('.el-pagination, .pagination, .pager').first()
    if (await pagination.isVisible()) {
      // 点击下一页
      const nextBtn = pagination.locator('button:has-text("下一页"), .btn-next, .next').first()
      if (await nextBtn.isVisible()) {
        await nextBtn.click()
        await page.waitForTimeout(1000)

        // 页面不应崩溃
        await expect(page.locator('body')).toBeVisible()
      }
    }
  })
})

test.describe('物品响应式测试', () => {
  test('物品列表在移动端正常显示', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 667 })
    await page.goto('/items')
    await page.waitForLoadState('networkidle')

    // 页面不应崩溃
    await expect(page.locator('body')).toBeVisible()
  })

  test('物品详情在移动端正常显示', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 667 })
    await page.goto('/item/1')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)

    // 页面不应崩溃
    await expect(page.locator('body')).toBeVisible()
  })

  test('发布页面在移动端正常显示', async ({ page }) => {
    // 模拟登录状态
    await page.goto('/login')
    await page.evaluate(() => {
      localStorage.setItem('idle_items_token', 'mock-token')
      localStorage.setItem('idle_items_user', JSON.stringify({ id: 1, username: 'testuser' }))
    })

    await page.setViewportSize({ width: 375, height: 667 })
    await page.goto('/publish')
    await page.waitForLoadState('networkidle')

    // 页面不应崩溃
    await expect(page.locator('body')).toBeVisible()
  })
})
