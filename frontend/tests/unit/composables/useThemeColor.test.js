import { describe, it, expect, vi, beforeEach } from 'vitest'

describe('useThemeColor', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('应该导出 useThemeColor 函数', async () => {
    const { useThemeColor } = await import('../../../src/composables/useThemeColor')
    expect(typeof useThemeColor).toBe('function')
  })

  it('应该返回 getCSSVar, chartColors, trendColors', async () => {
    const { useThemeColor } = await import('../../../src/composables/useThemeColor')
    const { getCSSVar, chartColors, trendColors } = useThemeColor()

    expect(typeof getCSSVar).toBe('function')
    expect(typeof chartColors).toBe('function')
    expect(typeof trendColors).toBe('function')
  })

  describe('chartColors', () => {
    it('应该返回7种图表颜色', async () => {
      const { useThemeColor } = await import('../../../src/composables/useThemeColor')
      const { chartColors } = useThemeColor()

      const colors = chartColors()

      expect(colors).toHaveLength(7)
    })
  })

  describe('trendColors', () => {
    it('应该返回趋势颜色', async () => {
      const { useThemeColor } = await import('../../../src/composables/useThemeColor')
      const { trendColors } = useThemeColor()

      const colors = trendColors()

      expect(colors).toHaveProperty('count')
      expect(colors).toHaveProperty('amount')
    })
  })
})
