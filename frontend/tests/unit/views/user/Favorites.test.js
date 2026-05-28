import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { getAllStubs } from '../../helpers/elementPlusMock'
import Favorites from '../../../../src/views/user/Favorites.vue'

const stubs = {
  ...getAllStubs(),
  'router-link': { template: '<a class="router-link"><slot /></a>' },
}

describe('Favorites View', () => {
  it('应该渲染组件', () => {
    const wrapper = mount(Favorites, { global: { stubs } })
    expect(wrapper.exists()).toBe(true)
  })

  it('应该显示收藏标题', () => {
    const wrapper = mount(Favorites, { global: { stubs } })
    expect(wrapper.text()).toContain('收藏')
  })
})
