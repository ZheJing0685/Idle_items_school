import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('@/api/config/http', () => ({
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  del: vi.fn(),
  getBlob: vi.fn()
}))

describe('Admin API 服务', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('应该导出admin对象', async () => {
    const { default: admin } = await import('@/api/services/admin')
    expect(admin).toBeDefined()
  })

  it('应该有statistics方法', async () => {
    const { default: admin } = await import('@/api/services/admin')
    expect(admin.statistics).toBeDefined()
    expect(typeof admin.statistics.getDashboard).toBe('function')
    expect(typeof admin.statistics.getOverview).toBe('function')
    expect(typeof admin.statistics.getMonthly).toBe('function')
    expect(typeof admin.statistics.getCategories).toBe('function')
    expect(typeof admin.statistics.getHotItems).toBe('function')
  })

  it('应该有users方法', async () => {
    const { default: admin } = await import('@/api/services/admin')
    expect(admin.users).toBeDefined()
    expect(typeof admin.users.getUsers).toBe('function')
    expect(typeof admin.users.getUser).toBe('function')
    expect(typeof admin.users.getUserStats).toBe('function')
    expect(typeof admin.users.createUser).toBe('function')
    expect(typeof admin.users.updateUser).toBe('function')
    expect(typeof admin.users.updateStatus).toBe('function')
    expect(typeof admin.users.batchUpdateStatus).toBe('function')
    expect(typeof admin.users.deleteUsers).toBe('function')
    expect(typeof admin.users.batchDelete).toBe('function')
    expect(typeof admin.users.exportUsers).toBe('function')
  })

  it('应该有items方法', async () => {
    const { default: admin } = await import('@/api/services/admin')
    expect(admin.items).toBeDefined()
    expect(typeof admin.items.getItems).toBe('function')
    expect(typeof admin.items.getItemStats).toBe('function')
    expect(typeof admin.items.updateStatus).toBe('function')
    expect(typeof admin.items.approveItem).toBe('function')
    expect(typeof admin.items.rejectItem).toBe('function')
    expect(typeof admin.items.offShelfItem).toBe('function')
    expect(typeof admin.items.deleteItem).toBe('function')
    expect(typeof admin.items.batchApprove).toBe('function')
    expect(typeof admin.items.batchReject).toBe('function')
    expect(typeof admin.items.batchOffShelf).toBe('function')
    expect(typeof admin.items.exportItems).toBe('function')
  })

  it('应该有orders方法', async () => {
    const { default: admin } = await import('@/api/services/admin')
    expect(admin.orders).toBeDefined()
    expect(typeof admin.orders.getOrders).toBe('function')
    expect(typeof admin.orders.cancelOrder).toBe('function')
    expect(typeof admin.orders.approveRefund).toBe('function')
    expect(typeof admin.orders.batchCancel).toBe('function')
    expect(typeof admin.orders.getStats).toBe('function')
    expect(typeof admin.orders.getOrder).toBe('function')
  })

  it('应该有categories方法', async () => {
    const { default: admin } = await import('@/api/services/admin')
    expect(admin.categories).toBeDefined()
    expect(typeof admin.categories.getCategories).toBe('function')
    expect(typeof admin.categories.createCategory).toBe('function')
    expect(typeof admin.categories.updateCategory).toBe('function')
    expect(typeof admin.categories.updateStatus).toBe('function')
    expect(typeof admin.categories.moveUp).toBe('function')
    expect(typeof admin.categories.moveDown).toBe('function')
    expect(typeof admin.categories.deleteCategory).toBe('function')
    expect(typeof admin.categories.batchEnable).toBe('function')
    expect(typeof admin.categories.batchDisable).toBe('function')
    expect(typeof admin.categories.getFeedbacks).toBe('function')
    expect(typeof admin.categories.reviewFeedback).toBe('function')
    expect(typeof admin.categories.exportCategories).toBe('function')
    expect(typeof admin.categories.getCategoryStats).toBe('function')
    expect(typeof admin.categories.getChangeLogs).toBe('function')
    expect(typeof admin.categories.batchDelete).toBe('function')
    expect(typeof admin.categories.importCategories).toBe('function')
  })

  it('应该有verifications方法', async () => {
    const { default: admin } = await import('@/api/services/admin')
    expect(admin.verifications).toBeDefined()
    expect(typeof admin.verifications.getVerifications).toBe('function')
    expect(typeof admin.verifications.approveVerification).toBe('function')
    expect(typeof admin.verifications.rejectVerification).toBe('function')
    expect(typeof admin.verifications.batchApprove).toBe('function')
    expect(typeof admin.verifications.batchReject).toBe('function')
    expect(typeof admin.verifications.getStats).toBe('function')
  })

  it('应该有logs方法', async () => {
    const { default: admin } = await import('@/api/services/admin')
    expect(admin.logs).toBeDefined()
    expect(typeof admin.logs.getLogs).toBe('function')
    expect(typeof admin.logs.getLogStats).toBe('function')
    expect(typeof admin.logs.getExport).toBe('function')
  })

  it('应该有disputes方法', async () => {
    const { default: admin } = await import('@/api/services/admin')
    expect(admin.disputes).toBeDefined()
    expect(typeof admin.disputes.list).toBe('function')
    expect(typeof admin.disputes.stats).toBe('function')
    expect(typeof admin.disputes.handleDispute).toBe('function')
    expect(typeof admin.disputes.batchApprove).toBe('function')
    expect(typeof admin.disputes.batchClose).toBe('function')
    expect(typeof admin.disputes.export).toBe('function')
  })
})
