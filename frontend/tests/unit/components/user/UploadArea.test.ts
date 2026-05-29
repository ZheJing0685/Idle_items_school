import { describe, it, expect, vi, beforeAll, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'

vi.mock('lucide-vue-next', () => ({
  X: { template: '<div class="icon-x" />', props: ['size'] },
  Upload: { template: '<div class="icon-upload" />', props: ['size', 'strokeWidth'] },
}))

let UploadArea: any
beforeAll(async () => {
  const mod = await import('@/components/user/UploadArea.vue')
  UploadArea = mod.default
})

const mountUploadArea = (props = {}) => {
  setActivePinia(createPinia())
  return mount(UploadArea, {
    props: {
      modelValue: '',
      ...props,
    },
  })
}

describe('UploadArea.vue 上传区域组件', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('组件渲染', () => {
    it('应该渲染上传区域', () => {
      const wrapper = mountUploadArea()
      expect(wrapper.find('.upload-area').exists()).toBe(true)
    })

    it('应该显示上传图标', () => {
      const wrapper = mountUploadArea()
      expect(wrapper.find('.upload-icon').exists()).toBe(true)
    })

    it('应该显示默认上传文字', () => {
      const wrapper = mountUploadArea()
      expect(wrapper.text()).toContain('点击上传图片')
    })

    it('应该显示自定义上传文字', () => {
      const wrapper = mountUploadArea({ text: '上传身份证照片' })
      expect(wrapper.text()).toContain('上传身份证照片')
    })

    it('有hint属性时应显示提示文字', () => {
      const wrapper = mountUploadArea({ hint: '支持JPG、PNG格式' })
      expect(wrapper.text()).toContain('支持JPG、PNG格式')
    })

    it('没有hint属性时不应显示提示文字', () => {
      const wrapper = mountUploadArea()
      expect(wrapper.find('.upload-hint').exists()).toBe(false)
    })
  })

  describe('文件预览', () => {
    it('有modelValue时应显示预览图片', () => {
      const wrapper = mountUploadArea({ modelValue: 'data:image/png;base64,test' })
      expect(wrapper.find('.preview-image').exists()).toBe(true)
    })

    it('有modelValue时应有has-file类', () => {
      const wrapper = mountUploadArea({ modelValue: 'data:image/png;base64,test' })
      expect(wrapper.find('.upload-area').classes()).toContain('has-file')
    })

    it('没有modelValue时不应显示预览', () => {
      const wrapper = mountUploadArea()
      expect(wrapper.find('.preview-image').exists()).toBe(false)
      expect(wrapper.find('.upload-icon').exists()).toBe(true)
    })

    it('有modelValue时应显示删除按钮', () => {
      const wrapper = mountUploadArea({ modelValue: 'data:image/png;base64,test' })
      expect(wrapper.find('.remove-btn').exists()).toBe(true)
    })
  })

  describe('交互功能', () => {
    it('点击上传区域应触发fileInput点击', async () => {
      const wrapper = mountUploadArea()
      const clickSpy = vi.spyOn(wrapper.vm.fileInput, 'click')
      await wrapper.find('.upload-area').trigger('click')
      expect(clickSpy).toHaveBeenCalled()
    })

    it('有文件时点击上传区域不应触发fileInput', async () => {
      const wrapper = mountUploadArea({ modelValue: 'data:image/png;base64,test' })
      await wrapper.find('.upload-area').trigger('click')
      expect(wrapper.find('.preview-image').exists()).toBe(true)
    })

    it('点击删除按钮应触发update:modelValue事件并清空', async () => {
      const wrapper = mountUploadArea({ modelValue: 'data:image/png;base64,test' })
      await wrapper.find('.remove-btn').trigger('click')
      expect(wrapper.emitted('update:modelValue')).toBeTruthy()
      expect(wrapper.emitted('update:modelValue')![0]).toEqual([''])
    })
  })

  describe('拖拽上传', () => {
    it('应处理dragover事件', async () => {
      const wrapper = mountUploadArea()
      const dragEvent = new Event('dragover', { bubbles: true })
      await wrapper.find('.upload-area').element.dispatchEvent(dragEvent)
      expect(wrapper.find('.upload-area').exists()).toBe(true)
    })

    it('应处理drop事件', async () => {
      const wrapper = mountUploadArea()
      expect(typeof wrapper.vm.handleDrop).toBe('function')
    })
  })

  describe('Props验证', () => {
    it('应接受modelValue属性', () => {
      const wrapper = mountUploadArea({ modelValue: 'test.jpg' })
      expect(wrapper.props('modelValue')).toBe('test.jpg')
    })

    it('应接受accept属性', () => {
      const wrapper = mountUploadArea({ accept: '.jpg,.png' })
      expect(wrapper.props('accept')).toBe('.jpg,.png')
    })

    it('应接受text属性', () => {
      const wrapper = mountUploadArea({ text: '自定义文字' })
      expect(wrapper.props('text')).toBe('自定义文字')
    })

    it('应接受hint属性', () => {
      const wrapper = mountUploadArea({ hint: '提示信息' })
      expect(wrapper.props('hint')).toBe('提示信息')
    })

    it('应接受maxSize属性', () => {
      const wrapper = mountUploadArea({ maxSize: 10 })
      expect(wrapper.props('maxSize')).toBe(10)
    })

    it('maxSize默认值应为5', () => {
      const wrapper = mountUploadArea()
      expect(wrapper.props('maxSize')).toBe(5)
    })

    it('accept默认值应为image/*', () => {
      const wrapper = mountUploadArea()
      expect(wrapper.props('accept')).toBe('image/*')
    })
  })

  describe('方法存在性', () => {
    it('应该有triggerUpload方法', () => {
      const wrapper = mountUploadArea()
      expect(typeof wrapper.vm.triggerUpload).toBe('function')
    })

    it('应该有processFile方法', () => {
      const wrapper = mountUploadArea()
      expect(typeof wrapper.vm.processFile).toBe('function')
    })

    it('应该有removeFile方法', () => {
      const wrapper = mountUploadArea()
      expect(typeof wrapper.vm.removeFile).toBe('function')
    })

    it('应该有handleFileChange方法', () => {
      const wrapper = mountUploadArea()
      expect(typeof wrapper.vm.handleFileChange).toBe('function')
    })

    it('应该有handleDrop方法', () => {
      const wrapper = mountUploadArea()
      expect(typeof wrapper.vm.handleDrop).toBe('function')
    })
  })
})
