import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import Loading from '../../../src/components/common/Loading.vue'

describe('Loading Component', () => {
  it('renders when visible', () => {
    const wrapper = mount(Loading, {
      props: { visible: true },
    })
    expect(wrapper.find('.loading-overlay').exists()).toBe(true)
  })

  it('does not render when not visible', () => {
    const wrapper = mount(Loading, {
      props: { visible: false },
    })
    expect(wrapper.find('.loading-overlay').exists()).toBe(false)
  })

  it('renders with custom text', () => {
    const wrapper = mount(Loading, {
      props: { visible: true, text: 'Loading data...' },
    })
    expect(wrapper.text()).toContain('Loading data...')
  })

  it('renders default text', () => {
    const wrapper = mount(Loading, {
      props: { visible: true },
    })
    expect(wrapper.text()).toContain('加载中...')
  })
})
