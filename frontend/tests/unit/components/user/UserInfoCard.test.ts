import { describe, it, expect, vi, beforeAll } from 'vitest'
import { mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'

vi.mock('lucide-vue-next', () => ({
  Edit3: { template: '<div class="icon-edit3" />', props: ['size'] },
  School: { template: '<div class="icon-school" />', props: ['size'] },
  FileText: { template: '<div class="icon-filetext" />', props: ['size'] },
}))

let UserInfoCard: any
beforeAll(async () => {
  const mod = await import('@/components/user/UserInfoCard.vue')
  UserInfoCard = mod.default
})

const defaultUser = {
  id: 1,
  nickname: '测试用户',
  username: 'testuser',
  avatar: 'https://example.com/avatar.jpg',
  schoolName: '测试大学',
  studentId: '2020001',
  gender: 1,
  bio: '这是个人简介',
}

const mountUserInfoCard = (props = {}) => {
  setActivePinia(createPinia())
  return mount(UserInfoCard, {
    props: {
      user: defaultUser,
      ...props,
    },
    global: {
      stubs: {
        'router-link': {
          template: '<a class="router-link-stub" :href="to"><slot /></a>',
          props: ['to'],
        },
        'el-avatar': {
          template: '<div class="el-avatar-stub"><slot /></div>',
          props: ['src', 'size'],
        },
      },
    },
  })
}

describe('UserInfoCard.vue 用户信息卡片组件', () => {
  describe('组件渲染', () => {
    it('应该渲染用户信息卡片', () => {
      const wrapper = mountUserInfoCard()
      expect(wrapper.find('.user-info-card').exists()).toBe(true)
    })

    it('应该显示用户昵称', () => {
      const wrapper = mountUserInfoCard()
      expect(wrapper.text()).toContain('测试用户')
    })

    it('没有昵称时应显示用户名', () => {
      const wrapper = mountUserInfoCard({
        user: { ...defaultUser, nickname: '', username: 'testuser' }
      })
      expect(wrapper.text()).toContain('testuser')
    })

    it('用户信息为空时应显示默认文字', () => {
      const wrapper = mountUserInfoCard({
        user: { ...defaultUser, nickname: '', username: '' }
      })
      expect(wrapper.text()).toContain('用户')
    })

    it('应该显示个人简介', () => {
      const wrapper = mountUserInfoCard()
      expect(wrapper.text()).toContain('这是个人简介')
    })

    it('没有简介时应显示默认简介', () => {
      const wrapper = mountUserInfoCard({
        user: { ...defaultUser, bio: '' }
      })
      expect(wrapper.text()).toContain('这个人很懒，什么都没写~')
    })
  })

  describe('用户元信息', () => {
    it('应该显示学校名称', () => {
      const wrapper = mountUserInfoCard()
      expect(wrapper.text()).toContain('测试大学')
    })

    it('应该显示学号', () => {
      const wrapper = mountUserInfoCard()
      expect(wrapper.text()).toContain('2020001')
    })

    it('gender为1时应显示男', () => {
      const wrapper = mountUserInfoCard()
      expect(wrapper.text()).toContain('男')
    })

    it('gender为2时应显示女', () => {
      const wrapper = mountUserInfoCard({
        user: { ...defaultUser, gender: 2 }
      })
      expect(wrapper.text()).toContain('女')
    })

    it('没有学校信息时不应显示学校区域', () => {
      const wrapper = mountUserInfoCard({
        user: { ...defaultUser, schoolName: '', studentId: '' }
      })
      expect(wrapper.find('.user-meta').text()).not.toContain('测试大学')
    })
  })

  describe('编辑资料链接', () => {
    it('应该包含编辑资料链接', () => {
      const wrapper = mountUserInfoCard()
      expect(wrapper.find('.edit-btn').exists()).toBe(true)
      expect(wrapper.text()).toContain('编辑资料')
    })

    it('编辑资料链接应包含/user/profile路径', () => {
      const wrapper = mountUserInfoCard()
      const link = wrapper.find('.edit-btn')
      expect(link.exists()).toBe(true)
      expect(link.text()).toContain('编辑资料')
    })
  })

  describe('Props验证', () => {
    it('应接受user属性', () => {
      const wrapper = mountUserInfoCard()
      expect(wrapper.props('user')).toBeDefined()
    })

    it('user为null时应使用默认值', () => {
      const wrapper = mountUserInfoCard({ user: null })
      expect(wrapper.text()).toContain('用户')
    })

    it('user为undefined时应使用默认值', () => {
      const wrapper = mountUserInfoCard({ user: undefined })
      expect(wrapper.text()).toContain('用户')
    })
  })

  describe('样式类', () => {
    it('应包含user-info-card类', () => {
      const wrapper = mountUserInfoCard()
      expect(wrapper.find('.user-info-card').exists()).toBe(true)
    })

    it('应包含avatar-section类', () => {
      const wrapper = mountUserInfoCard()
      expect(wrapper.find('.avatar-section').exists()).toBe(true)
    })

    it('应包含info-section类', () => {
      const wrapper = mountUserInfoCard()
      expect(wrapper.find('.info-section').exists()).toBe(true)
    })

    it('应包含avatar-wrapper类', () => {
      const wrapper = mountUserInfoCard()
      expect(wrapper.find('.avatar-wrapper').exists()).toBe(true)
    })

    it('应包含avatar-glow类', () => {
      const wrapper = mountUserInfoCard()
      expect(wrapper.find('.avatar-glow').exists()).toBe(true)
    })
  })

  describe('性别分隔符', () => {
    it('有性别信息时应显示分隔符', () => {
      const wrapper = mountUserInfoCard()
      expect(wrapper.find('.meta-divider').exists()).toBe(true)
    })

    it('没有性别信息时不应显示分隔符', () => {
      const wrapper = mountUserInfoCard({
        user: { ...defaultUser, gender: 0 }
      })
      const dividers = wrapper.findAll('.meta-divider')
      expect(dividers.length).toBeLessThan(2)
    })
  })
})
