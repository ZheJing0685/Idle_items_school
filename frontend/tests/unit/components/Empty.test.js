import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import Empty from '../../../src/components/common/Empty.vue'

describe('Empty Component', () => {
  it('renders with default title', () => {
    const wrapper = mount(Empty)
    expect(wrapper.text()).toContain('暂无数据')
  })

  it('renders with custom title', () => {
    const wrapper = mount(Empty, {
      props: { title: 'No items found' },
    })
    expect(wrapper.text()).toContain('No items found')
  })

  it('renders with description', () => {
    const wrapper = mount(Empty, {
      props: { description: 'Try adding some items' },
    })
    expect(wrapper.text()).toContain('Try adding some items')
  })

  it('renders default description', () => {
    const wrapper = mount(Empty)
    expect(wrapper.text()).toContain('这里还没有内容')
  })
})
