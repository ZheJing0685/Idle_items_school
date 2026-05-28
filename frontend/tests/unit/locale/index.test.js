import { describe, it, expect } from 'vitest'
import i18n, { } from '../../../src/locale/index'

describe('Locale i18n', () => {
  it('应该导出 i18n 实例', () => {
    expect(i18n).toBeDefined()
  })

  it('应该有 global 属性', () => {
    expect(i18n.global).toBeDefined()
  })

  it('默认 locale 应该是 zh-CN', () => {
    expect(i18n.global.locale.value).toBe('zh-CN')
  })

  it('应该有 t 函数', () => {
    expect(typeof i18n.global.t).toBe('function')
  })
})
