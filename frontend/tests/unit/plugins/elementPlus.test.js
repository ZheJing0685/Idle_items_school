import { describe, it, expect } from 'vitest'
import { setupElementPlus } from '../../../src/plugins/element-plus'

describe('Element Plus Plugin', () => {
  it('应该导出 setupElementPlus 函数', () => {
    expect(typeof setupElementPlus).toBe('function')
  })
})
