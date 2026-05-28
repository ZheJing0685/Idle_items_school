import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { getAllStubs } from '../../helpers/elementPlusMock'
import MyDisputes from '../../../../src/views/user/MyDisputes.vue'

const stubs = {
  ...getAllStubs(),
  'router-link': { template: '<a class="router-link"><slot /></a>' },
}

describe('MyDisputes View', () => {
  it('应该渲染组件', () => {
    const wrapper = mount(MyDisputes, { global: { stubs } })
    expect(wrapper.exists()).toBe(true)
  })

  it('应该显示纠纷标题', () => {
    const wrapper = mount(MyDisputes, { global: { stubs } })
    expect(wrapper.text()).toContain('纠纷')
  })
})
