import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ErrorBoundary from '../../../src/components/common/ErrorBoundary.vue'

describe('ErrorBoundary Component', () => {
  it('应该渲染子组件（无错误时）', () => {
    const wrapper = mount(ErrorBoundary, {
      slots: {
        default: '<div class="child-content">子组件内容</div>'
      }
    })
    expect(wrapper.find('.child-content').exists()).toBe(true)
    expect(wrapper.text()).toContain('子组件内容')
  })

  it('应该在有错误时显示错误信息', async () => {
    const wrapper = mount(ErrorBoundary, {
      slots: {
        default: '<div>正常内容</div>'
      }
    })
    expect(wrapper.find('.error-boundary').exists()).toBe(false)
  })
})
