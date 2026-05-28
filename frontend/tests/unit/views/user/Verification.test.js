import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { getAllStubs } from '../../helpers/elementPlusMock'
import Verification from '../../../../src/views/user/Verification.vue'

const stubs = {
  ...getAllStubs(),
  'router-link': { template: '<a class="router-link"><slot /></a>' },
}

describe('Verification View', () => {
  it('应该渲染组件', () => {
    const wrapper = mount(Verification, { global: { stubs } })
    expect(wrapper.exists()).toBe(true)
  })

  it('应该显示认证标题', () => {
    const wrapper = mount(Verification, { global: { stubs } })
    expect(wrapper.text()).toContain('认证')
  })
})
