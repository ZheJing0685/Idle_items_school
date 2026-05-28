import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import UserInfoCard from '../../../src/components/user/UserInfoCard.vue'

describe('UserInfoCard Component', () => {
  const mockUser = {
    id: 1,
    username: 'testuser',
    nickname: '测试用户',
    avatar: 'https://example.com/avatar.jpg',
    schoolName: '测试大学',
    studentId: '2024001',
    gender: 1,
    bio: '这是用户简介'
  }

  it('应该渲染组件', () => {
    const wrapper = mount(UserInfoCard, {
      props: { user: mockUser },
      global: {
        stubs: {
          'el-avatar': { template: '<div class="avatar"><slot /></div>' },
          'router-link': { template: '<a><slot /></a>' },
          'Edit3': { template: '<div />' },
          'School': { template: '<div />' },
          'FileText': { template: '<div />' }
        }
      }
    })
    expect(wrapper.exists()).toBe(true)
  })

  it('应该显示用户昵称', () => {
    const wrapper = mount(UserInfoCard, {
      props: { user: mockUser },
      global: {
        stubs: {
          'el-avatar': { template: '<div class="avatar"><slot /></div>' },
          'router-link': { template: '<a><slot /></a>' },
          'Edit3': { template: '<div />' },
          'School': { template: '<div />' },
          'FileText': { template: '<div />' }
        }
      }
    })
    expect(wrapper.text()).toContain('测试用户')
  })

  it('应该显示学校名称', () => {
    const wrapper = mount(UserInfoCard, {
      props: { user: mockUser },
      global: {
        stubs: {
          'el-avatar': { template: '<div class="avatar"><slot /></div>' },
          'router-link': { template: '<a><slot /></a>' },
          'Edit3': { template: '<div />' },
          'School': { template: '<div />' },
          'FileText': { template: '<div />' }
        }
      }
    })
    expect(wrapper.text()).toContain('测试大学')
  })

  it('应该显示学号', () => {
    const wrapper = mount(UserInfoCard, {
      props: { user: mockUser },
      global: {
        stubs: {
          'el-avatar': { template: '<div class="avatar"><slot /></div>' },
          'router-link': { template: '<a><slot /></a>' },
          'Edit3': { template: '<div />' },
          'School': { template: '<div />' },
          'FileText': { template: '<div />' }
        }
      }
    })
    expect(wrapper.text()).toContain('2024001')
  })

  it('应该显示用户简介', () => {
    const wrapper = mount(UserInfoCard, {
      props: { user: mockUser },
      global: {
        stubs: {
          'el-avatar': { template: '<div class="avatar"><slot /></div>' },
          'router-link': { template: '<a><slot /></a>' },
          'Edit3': { template: '<div />' },
          'School': { template: '<div />' },
          'FileText': { template: '<div />' }
        }
      }
    })
    expect(wrapper.text()).toContain('这是用户简介')
  })

  it('没有用户时应该显示默认文本', () => {
    const wrapper = mount(UserInfoCard, {
      props: { user: null },
      global: {
        stubs: {
          'el-avatar': { template: '<div class="avatar"><slot /></div>' },
          'router-link': { template: '<a><slot /></a>' },
          'Edit3': { template: '<div />' },
          'School': { template: '<div />' },
          'FileText': { template: '<div />' }
        }
      }
    })
    expect(wrapper.text()).toContain('用户')
  })
})
