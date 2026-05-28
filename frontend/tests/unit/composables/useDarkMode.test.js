import { describe, it, expect, vi, beforeEach } from 'vitest'

describe('useDarkMode', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // 清理 dark 类
    document.documentElement.classList.remove('dark')
    document.documentElement.style.colorScheme = ''
  })

  it('应该导出 useDarkMode 函数', async () => {
    const { useDarkMode } = await import('../../../src/composables/useDarkMode')
    expect(typeof useDarkMode).toBe('function')
  })

  it('应该返回 isDark 和 toggle 方法', async () => {
    const { useDarkMode } = await import('../../../src/composables/useDarkMode')
    const { isDark, toggle } = useDarkMode()

    expect(isDark).toBeDefined()
    expect(typeof toggle).toBe('function')
  })

  it('toggle 应该切换主题', async () => {
    const { useDarkMode } = await import('../../../src/composables/useDarkMode')
    const { isDark, toggle } = useDarkMode()

    const initial = isDark.value
    toggle()
    expect(isDark.value).toBe(!initial)
  })
})
