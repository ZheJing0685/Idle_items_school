/**
 * 测试辅助工具
 * 提供通用的测试工具函数
 */

/**
 * 等待指定时间
 * @param {number} ms - 等待时间（毫秒）
 */
export const wait = (ms) => new Promise(resolve => setTimeout(resolve, ms))

/**
 * 生成随机字符串
 * @param {number} length - 字符串长度
 */
export const randomString = (length = 10) => {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
  let result = ''
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  return result
}

/**
 * 生成随机邮箱
 */
export const randomEmail = () => `test_${randomString(8)}@example.com`

/**
 * 生成随机手机号
 */
export const randomPhone = () => {
  const prefixes = ['138', '139', '150', '151', '152', '158', '159', '188', '189']
  const prefix = prefixes[Math.floor(Math.random() * prefixes.length)]
  const suffix = Math.floor(Math.random() * 100000000).toString().padStart(8, '0')
  return prefix + suffix
}

/**
 * 生成随机价格
 * @param {number} min - 最小价格
 * @param {number} max - 最大价格
 */
export const randomPrice = (min = 1, max = 9999) => {
  return Math.floor(Math.random() * (max - min + 1)) + min
}

/**
 * 格式化日期
 * @param {Date} date - 日期对象
 */
export const formatDate = (date = new Date()) => {
  return date.toISOString().split('T')[0]
}

/**
 * 格式化时间
 * @param {Date} date - 日期对象
 */
export const formatTime = (date = new Date()) => {
  return date.toTimeString().split(' ')[0]
}

/**
 * 模拟API响应
 * @param {Object} data - 响应数据
 * @param {number} code - 状态码
 * @param {string} message - 消息
 */
export const mockApiResponse = (data = null, code = 200, message = 'success') => {
  return {
    code,
    message,
    data,
    timestamp: new Date().toISOString()
  }
}

/**
 * 模拟分页响应
 * @param {Array} content - 数据内容
 * @param {number} page - 当前页码
 * @param {number} size - 每页大小
 * @param {number} total - 总记录数
 */
export const mockPageResponse = (content = [], page = 1, size = 10, total = 0) => {
  return {
    content,
    page,
    size,
    total,
    totalPages: Math.ceil(total / size),
    first: page === 1,
    last: page === Math.ceil(total / size)
  }
}

/**
 * 模拟延迟
 * @param {number} ms - 延迟时间（毫秒）
 */
export const delay = (ms = 1000) => {
  return new Promise(resolve => setTimeout(resolve, ms))
}

/**
 * 重试执行函数
 * @param {Function} fn - 要执行的函数
 * @param {number} maxRetries - 最大重试次数
 * @param {number} retryDelay - 重试间隔（毫秒）
 */
export const retry = async (fn, maxRetries = 3, retryDelay = 1000) => {
  let lastError
  for (let i = 0; i < maxRetries; i++) {
    try {
      return await fn()
    } catch (error) {
      lastError = error
      if (i < maxRetries - 1) {
        await delay(retryDelay)
      }
    }
  }
  throw lastError
}

/**
 * 检查元素是否可见
 * @param {import('@playwright/test').Page} page - Playwright页面对象
 * @param {string} selector - 选择器
 * @param {number} timeout - 超时时间（毫秒）
 */
export const isElementVisible = async (page, selector, timeout = 5000) => {
  try {
    await page.waitForSelector(selector, { timeout })
    return true
  } catch {
    return false
  }
}

/**
 * 安全点击元素
 * @param {import('@playwright/test').Page} page - Playwright页面对象
 * @param {string} selector - 选择器
 * @param {number} timeout - 超时时间（毫秒）
 */
export const safeClick = async (page, selector, timeout = 5000) => {
  const visible = await isElementVisible(page, selector, timeout)
  if (visible) {
    await page.click(selector)
    return true
  }
  return false
}

/**
 * 安全填写输入框
 * @param {import('@playwright/test').Page} page - Playwright页面对象
 * @param {string} selector - 选择器
 * @param {string} value - 填写值
 * @param {number} timeout - 超时时间（毫秒）
 */
export const safeFill = async (page, selector, value, timeout = 5000) => {
  const visible = await isElementVisible(page, selector, timeout)
  if (visible) {
    await page.fill(selector, value)
    return true
  }
  return false
}

/**
 * 截图并保存
 * @param {import('@playwright/test').Page} page - Playwright页面对象
 * @param {string} name - 截图名称
 */
export const takeScreenshot = async (page, name) => {
  const timestamp = new Date().toISOString().replace(/[:.]/g, '-')
  await page.screenshot({
    path: `./tests/reports/screenshots/${name}_${timestamp}.png`,
    fullPage: true
  })
}

/**
 * 获取页面性能指标
 * @param {import('@playwright/test').Page} page - Playwright页面对象
 */
export const getPerformanceMetrics = async (page) => {
  return await page.evaluate(() => {
    const performance = window.performance
    const timing = performance.timing

    return {
      // 页面加载时间
      pageLoadTime: timing.loadEventEnd - timing.navigationStart,
      // DOM加载时间
      domLoadTime: timing.domContentLoadedEventEnd - timing.navigationStart,
      // 首次渲染时间
      firstPaint: performance.getEntriesByType('paint')[0]?.startTime || 0,
      // 首次内容渲染时间
      firstContentfulPaint: performance.getEntriesByType('paint')[1]?.startTime || 0,
      // 资源加载时间
      resourceLoadTime: timing.loadEventEnd - timing.domContentLoadedEventEnd
    }
  })
}

export default {
  wait,
  randomString,
  randomEmail,
  randomPhone,
  randomPrice,
  formatDate,
  formatTime,
  mockApiResponse,
  mockPageResponse,
  delay,
  retry,
  isElementVisible,
  safeClick,
  safeFill,
  takeScreenshot,
  getPerformanceMetrics
}
