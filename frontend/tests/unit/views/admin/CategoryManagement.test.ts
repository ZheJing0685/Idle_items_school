import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import CategoryManagement from '@/views/admin/CategoryManagement.vue'
import { elementPlusStubs, lucideIconsStub } from '../../helpers/elementPlusMock'

// Mock API
const mockGetCategories = vi.fn()
const mockGetCategoryStats = vi.fn()
const mockGetChangeLogs = vi.fn()
const mockCreateCategory = vi.fn()
const mockUpdateCategory = vi.fn()
const mockDeleteCategory = vi.fn()
const mockBatchEnable = vi.fn()
const mockBatchDisable = vi.fn()
const mockBatchDelete = vi.fn()
const mockExportCategories = vi.fn()
const mockImportCategories = vi.fn()

vi.mock('@/api', () => ({
  default: {
    admin: {
      categories: {
        getCategories: (...args: any[]) => mockGetCategories(...args),
        getCategoryStats: (...args: any[]) => mockGetCategoryStats(...args),
        getChangeLogs: (...args: any[]) => mockGetChangeLogs(...args),
        createCategory: (...args: any[]) => mockCreateCategory(...args),
        updateCategory: (...args: any[]) => mockUpdateCategory(...args),
        deleteCategory: (...args: any[]) => mockDeleteCategory(...args),
        batchEnable: (...args: any[]) => mockBatchEnable(...args),
        batchDisable: (...args: any[]) => mockBatchDisable(...args),
        batchDelete: (...args: any[]) => mockBatchDelete(...args),
        exportCategories: (...args: any[]) => mockExportCategories(...args),
        importCategories: (...args: any[]) => mockImportCategories(...args),
      },
    },
  },
}))

// Mock Element Plus
vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn(),
  },
  ElMessageBox: {
    confirm: vi.fn().mockResolvedValue(undefined),
    prompt: vi.fn().mockResolvedValue({ value: '原因' }),
  },
}))

// Mock lucide icons
const lucideIcons = [
  'List', 'Clock', 'Layers', 'Grid', 'ChevronUp', 'ChevronDown',
  'RefreshCw', 'Search', 'Plus', 'Edit3', 'Trash2', 'X', 'Upload',
  'XCircle', 'Package', 'Table', 'Download', 'ChevronLeft', 'ChevronRight',
]
const lucideStubs = Object.fromEntries(
  lucideIcons.map((name) => [name, { template: '<div class="icon" />', props: ['size', 'strokeWidth'] }])
)

const stubs = {
  ...elementPlusStubs,
  ...lucideStubs,
  'el-table-column': {
    template: '<div class="el-table-column"><slot :row="{}" /></div>',
    props: ['label', 'prop', 'width', 'minWidth', 'fixed', 'type', 'selectable'],
  },
  'el-tree': {
    template: '<div class="el-tree"><slot v-for="(item, idx) in treeData" :key="item.id || idx" :data="item" /></div>',
    props: ['data', 'props', 'nodeKey', 'defaultExpandAll', 'expandOnClickNode', 'filterNodeMethod', 'highlightCurrent', 'showCheckbox', 'checkStrictly'],
    computed: {
      treeData() { return this.data || []; }
    },
    methods: {
      filter: vi.fn(),
      getCheckedKeys: vi.fn().mockReturnValue([]),
      setCheckedKeys: vi.fn(),
    },
  },
}

describe('CategoryManagement.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    mockGetCategories.mockResolvedValue({
      code: 200,
      data: {
        content: [
          { id: 1, name: '电子产品', level: 1, status: 1, sort: 1, itemCount: 50, parentId: 0, createdAt: '2024-01-01T00:00:00' },
          { id: 2, name: '手机', level: 2, status: 1, sort: 1, itemCount: 20, parentId: 1, createdAt: '2024-01-02T00:00:00' },
        ],
        totalElements: 2,
      },
    })
    mockGetCategoryStats.mockResolvedValue({
      code: 200,
      data: { total: 50, active: 45, level1: 10, level2: 40 },
    })
    mockGetChangeLogs.mockResolvedValue({
      code: 200,
      data: { content: [], totalElements: 0 },
    })
  })

  const mountCategoryManagement = (options = {}) => {
    return mount(CategoryManagement, {
      global: {
        plugins: [createPinia()],
        stubs,
        ...options,
      },
    })
  }

  it('应该正确渲染分类管理页面', () => {
    const wrapper = mountCategoryManagement()
    expect(wrapper.find('.category-management').exists()).toBe(true)
  })

  it('应该显示页面标题和描述', () => {
    const wrapper = mountCategoryManagement()
    expect(wrapper.find('.section-title').text()).toBe('分类管理')
    expect(wrapper.find('.section-desc').text()).toContain('管理平台物品分类')
  })

  it('应该包含分类统计卡片', () => {
    const wrapper = mountCategoryManagement()
    const statCards = wrapper.findAll('.stat-card')
    expect(statCards.length).toBe(4)
  })

  it('应该显示分类总数', () => {
    const wrapper = mountCategoryManagement()
    expect(wrapper.find('.stat-icon-total').exists()).toBe(true)
  })

  it('应该显示活跃分类数', () => {
    const wrapper = mountCategoryManagement()
    expect(wrapper.find('.stat-icon-active').exists()).toBe(true)
  })

  it('应该显示一级分类数', () => {
    const wrapper = mountCategoryManagement()
    expect(wrapper.find('.stat-icon-level1').exists()).toBe(true)
  })

  it('应该显示二级分类数', () => {
    const wrapper = mountCategoryManagement()
    expect(wrapper.find('.stat-icon-level2').exists()).toBe(true)
  })

  it('应该包含树形面板', () => {
    const wrapper = mountCategoryManagement()
    expect(wrapper.find('.tree-panel').exists()).toBe(true)
  })

  it('应该包含详情面板', () => {
    const wrapper = mountCategoryManagement()
    expect(wrapper.find('.detail-panel').exists()).toBe(true)
  })

  it('应该包含搜索输入框', () => {
    const wrapper = mountCategoryManagement()
    const searchInputs = wrapper.findAll('.search-input')
    expect(searchInputs.length).toBeGreaterThan(0)
  })

  it('应该包含添加分类按钮', () => {
    const wrapper = mountCategoryManagement()
    expect(wrapper.find('.tree-toolbar').exists()).toBe(true)
  })

  it('应该包含标签页', () => {
    const wrapper = mountCategoryManagement()
    expect(wrapper.find('.management-tabs').exists()).toBe(true)
  })

  it('应该默认显示管理标签页', () => {
    const wrapper = mountCategoryManagement()
    const vm = wrapper.vm as any
    expect(vm.activeTab).toBe('management')
  })

  it('handleExpandAll 应切换全部展开/收起', async () => {
    const wrapper = mountCategoryManagement()
    const vm = wrapper.vm as any
    expect(vm.allExpanded).toBe(true)
    vm.handleExpandAll()
    await wrapper.vm.$nextTick()
    expect(vm.allExpanded).toBe(false)
  })

  it('handleRefreshTree 应刷新分类数据', async () => {
    const wrapper = mountCategoryManagement()
    const vm = wrapper.vm as any
    vi.clearAllMocks()
    vm.handleRefreshTree()
    await wrapper.vm.$nextTick()
    expect(mockGetCategories).toHaveBeenCalled()
    expect(mockGetCategoryStats).toHaveBeenCalled()
  })

  it('handleNodeClick 应设置当前分类并切换到详情模式', () => {
    const wrapper = mountCategoryManagement()
    const vm = wrapper.vm as any
    const nodeData = { id: 1, name: '电子产品', level: 1 }
    vm.handleNodeClick(nodeData)
    expect(vm.currentCategory.name).toBe('电子产品')
    expect(vm.panelMode).toBe('detail')
  })

  it('handleAddRoot 应初始化新增表单', () => {
    const wrapper = mountCategoryManagement()
    const vm = wrapper.vm as any
    vm.handleAddRoot()
    expect(vm.editForm.name).toBe('')
    expect(vm.editForm.parentId).toBe('0')
    expect(vm.panelMode).toBe('create')
  })

  it('handleCancelEdit 应取消编辑', () => {
    const wrapper = mountCategoryManagement()
    const vm = wrapper.vm as any
    vm.panelMode = 'edit'
    vm.handleCancelEdit()
    expect(vm.panelMode).toBe('empty')
    expect(vm.editForm).toBeNull()
  })

  it('handleSave 名称为空时应提示警告', async () => {
    const wrapper = mountCategoryManagement()
    const vm = wrapper.vm as any
    vm.panelMode = 'create'
    vm.editForm = { name: '', parentId: '0', sort: 1, status: 1 }
    await vm.handleSave()
    const { ElMessage } = await import('element-plus')
    expect(ElMessage.warning).toHaveBeenCalledWith('请输入分类名称')
  })

  it('handleDeleteNode 有物品时应提示无法删除', async () => {
    const wrapper = mountCategoryManagement()
    const vm = wrapper.vm as any
    vm.handleDeleteNode({ id: 1, name: '测试', itemCount: 5 })
    const { ElMessage } = await import('element-plus')
    expect(ElMessage.warning).toHaveBeenCalledWith('该分类下有物品，无法删除')
  })

  it('getLogTypeClass 应返回正确的CSS类', () => {
    const wrapper = mountCategoryManagement()
    const vm = wrapper.vm as any
    expect(vm.getLogTypeClass('创建')).toContain('success')
    expect(vm.getLogTypeClass('编辑')).toContain('primary')
    expect(vm.getLogTypeClass('删除')).toContain('danger')
    expect(vm.getLogTypeClass('未知')).toContain('info')
  })

  it('formatDate 应格式化日期', () => {
    const wrapper = mountCategoryManagement()
    const vm = wrapper.vm as any
    expect(vm.formatDate('')).toBe('-')
    expect(vm.formatDate('2024-01-15T10:00:00')).toContain('2024')
  })

  it('formatDateTime 应格式化日期时间', () => {
    const wrapper = mountCategoryManagement()
    const vm = wrapper.vm as any
    expect(vm.formatDateTime('')).toBe('-')
    expect(vm.formatDateTime('2024-01-15T10:00:00')).toContain('2024')
  })

  it('onMounted 应加载所有数据', async () => {
    mountCategoryManagement()
    await vi.waitFor(() => {
      expect(mockGetCategoryStats).toHaveBeenCalled()
      expect(mockGetCategories).toHaveBeenCalled()
      expect(mockGetChangeLogs).toHaveBeenCalled()
    })
  })
})
