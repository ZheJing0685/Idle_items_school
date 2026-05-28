import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { getAllStubs } from '../../helpers/elementPlusMock'
import CreateDispute from '../../../../src/views/user/CreateDispute.vue'

const stubs = {
  ...getAllStubs(),
  'router-link': { template: '<a class="router-link"><slot /></a>' },
}

describe('CreateDispute View', () => {
  it('应该渲染组件', () => {
    const wrapper = mount(CreateDispute, { global: { stubs } })
    expect(wrapper.exists()).toBe(true)
  })

  it('应该显示创建纠纷标题', () => {
    const wrapper = mount(CreateDispute, { global: { stubs } })
    expect(wrapper.text()).toContain('纠纷')
  })
})
