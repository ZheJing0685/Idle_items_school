import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { getAllStubs } from '../../helpers/elementPlusMock'
import OrderDetail from '../../../../src/views/user/OrderDetail.vue'

const stubs = {
  ...getAllStubs(),
  'router-link': { template: '<a class="router-link"><slot /></a>' },
}

describe('OrderDetail View', () => {
  it('应该渲染组件', () => {
    const wrapper = mount(OrderDetail, { global: { stubs } })
    expect(wrapper.exists()).toBe(true)
  })

  it('应该显示订单详情标题', () => {
    const wrapper = mount(OrderDetail, { global: { stubs } })
    expect(wrapper.text()).toContain('订单')
  })
})
