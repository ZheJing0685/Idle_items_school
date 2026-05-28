import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { getAllStubs } from '../../helpers/elementPlusMock'
import ChangePassword from '../../../../src/views/user/ChangePassword.vue'

const stubs = {
  ...getAllStubs(),
  'router-link': { template: '<a class="router-link"><slot /></a>' },
}

describe('ChangePassword View', () => {
  it('应该渲染组件', () => {
    const wrapper = mount(ChangePassword, { global: { stubs } })
    expect(wrapper.exists()).toBe(true)
  })

  it('应该显示修改密码标题', () => {
    const wrapper = mount(ChangePassword, { global: { stubs } })
    expect(wrapper.text()).toContain('密码')
  })
})
