import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { getAllStubs } from '../../helpers/elementPlusMock'
import Notifications from '../../../../src/views/user/Notifications.vue'

const stubs = {
  ...getAllStubs(),
  'router-link': { template: '<a class="router-link"><slot /></a>' },
}

describe('Notifications View', () => {
  it('应该渲染组件', () => {
    const wrapper = mount(Notifications, { global: { stubs } })
    expect(wrapper.exists()).toBe(true)
  })

  it('应该显示通知标题', () => {
    const wrapper = mount(Notifications, { global: { stubs } })
    expect(wrapper.text()).toContain('通知')
  })
})
