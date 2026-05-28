import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { getAllStubs } from '../../helpers/elementPlusMock'
import Profile from '../../../../src/views/user/Profile.vue'

const stubs = {
  ...getAllStubs(),
  'router-link': { template: '<a class="router-link"><slot /></a>' },
}

describe('Profile View', () => {
  it('应该渲染组件', () => {
    const wrapper = mount(Profile, { global: { stubs } })
    expect(wrapper.exists()).toBe(true)
  })

  it('应该显示个人信息标题', () => {
    const wrapper = mount(Profile, { global: { stubs } })
    expect(wrapper.text()).toContain('个人')
  })
})
