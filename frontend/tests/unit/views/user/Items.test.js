import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { getAllStubs } from '../../helpers/elementPlusMock'
import Items from '../../../../src/views/user/Items.vue'

const stubs = {
  ...getAllStubs(),
  'router-link': { template: '<a class="router-link"><slot /></a>' },
}

describe('User Items View', () => {
  it('应该渲染组件', () => {
    const wrapper = mount(Items, { global: { stubs } })
    expect(wrapper.exists()).toBe(true)
  })

  it('应该显示我的发布标题', () => {
    const wrapper = mount(Items, { global: { stubs } })
    expect(wrapper.text()).toContain('发布')
  })
})
