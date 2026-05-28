import { describe, it, expect } from 'vitest'
import { useColumnWidth } from '../../../src/composables/useColumnWidth'

describe('useColumnWidth', () => {
  const { defaultConfig, columnConfigs, calculateTextWidth, calculateColumnWidth, fitColumnsWidth, getColumnWidth } = useColumnWidth()

  describe('defaultConfig', () => {
    it('应该有默认配置', () => {
      expect(defaultConfig.minWidth).toBe(60)
      expect(defaultConfig.maxWidth).toBe(400)
      expect(defaultConfig.defaultWidth).toBe(120)
    })
  })

  describe('columnConfigs', () => {
    it('应该有 id 列配置', () => {
      expect(columnConfigs.id).toBeDefined()
      expect(columnConfigs.id.minWidth).toBe(60)
    })

    it('应该有 username 列配置', () => {
      expect(columnConfigs.username).toBeDefined()
    })
  })

  describe('calculateTextWidth', () => {
    it('应该计算中文字符宽度', () => {
      const width = calculateTextWidth('你好')
      expect(width).toBeGreaterThan(0)
    })

    it('应该计算英文字符宽度', () => {
      const width = calculateTextWidth('hello')
      expect(width).toBeGreaterThan(0)
    })

    it('应该计算数字宽度', () => {
      const width = calculateTextWidth('12345')
      expect(width).toBeGreaterThan(0)
    })

    it('空字符串应返回默认宽度', () => {
      const width = calculateTextWidth('')
      expect(width).toBe(defaultConfig.defaultWidth)
    })

    it('null 应返回默认宽度', () => {
      const width = calculateTextWidth(null)
      expect(width).toBe(defaultConfig.defaultWidth)
    })
  })

  describe('calculateColumnWidth', () => {
    it('空数据应返回默认宽度', () => {
      expect(calculateColumnWidth('username', [])).toBe(columnConfigs.username.defaultWidth)
    })

    it('应该计算数据宽度', () => {
      const data = [{ username: '张三' }, { username: '李四' }]
      const width = calculateColumnWidth('username', data)
      expect(width).toBeGreaterThanOrEqual(columnConfigs.username.minWidth)
    })

    it('应该限制最大宽度', () => {
      const data = [{ username: '很长的名字'.repeat(20) }]
      const width = calculateColumnWidth('username', data)
      expect(width).toBeLessThanOrEqual(columnConfigs.username.maxWidth)
    })

    it('应该处理布尔值', () => {
      const data = [{ verified: true }, { verified: false }]
      const width = calculateColumnWidth('verified', data)
      expect(width).toBeGreaterThan(0)
    })

    it('应该处理数字', () => {
      const data = [{ id: 12345 }, { id: 67890 }]
      const width = calculateColumnWidth('id', data)
      expect(width).toBeGreaterThan(0)
    })

    it('应该跳过 null/undefined', () => {
      const data = [{ username: null }, { username: undefined }]
      const width = calculateColumnWidth('username', data)
      expect(width).toBe(columnConfigs.username.defaultWidth)
    })

    it('未知列名应使用默认配置', () => {
      const data = [{ unknown: 'test' }]
      const width = calculateColumnWidth('unknown', data)
      expect(width).toBeGreaterThanOrEqual(defaultConfig.minWidth)
    })
  })

  describe('fitColumnsWidth', () => {
    it('应该计算所有列宽度', () => {
      const columns = [{ prop: 'username' }, { prop: 'email' }]
      const data = [{ username: 'test', email: 'test@example.com' }]
      const result = fitColumnsWidth(columns, data)
      expect(result.username).toBeDefined()
      expect(result.email).toBeDefined()
    })

    it('应该跳过 selection 列', () => {
      const columns = [{ prop: 'selection' }, { prop: 'username' }]
      const data = [{ username: 'test' }]
      const result = fitColumnsWidth(columns, data)
      expect(result.selection).toBeUndefined()
      expect(result.username).toBeDefined()
    })

    it('应该跳过 operation 列', () => {
      const columns = [{ prop: 'operation' }]
      const data = [{}]
      const result = fitColumnsWidth(columns, data)
      expect(result.operation).toBeUndefined()
    })

    it('应该支持 field 属性', () => {
      const columns = [{ field: 'name' }]
      const data = [{ name: 'test' }]
      const result = fitColumnsWidth(columns, data)
      expect(result.name).toBeDefined()
    })
  })

  describe('getColumnWidth', () => {
    it('应该返回计算后的宽度', () => {
      const widths = { username: 150 }
      expect(getColumnWidth('username', widths)).toBe(150)
    })

    it('应该回退到默认宽度', () => {
      expect(getColumnWidth('username', {})).toBe(columnConfigs.username.defaultWidth)
    })

    it('未知列应返回全局默认宽度', () => {
      expect(getColumnWidth('unknown', {})).toBe(defaultConfig.defaultWidth)
    })
  })
})
