import { describe, it, expect, vi, beforeEach } from 'vitest'
import admin from '../../../src/api/services/admin'

describe('Admin Service', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('statistics', () => {
    it('getDashboard 应该导出函数', () => {
      expect(typeof admin.statistics.getDashboard).toBe('function')
    })

    it('getOverview 应该导出函数', () => {
      expect(typeof admin.statistics.getOverview).toBe('function')
    })

    it('getMonthly 应该导出函数', () => {
      expect(typeof admin.statistics.getMonthly).toBe('function')
    })

    it('getCategories 应该导出函数', () => {
      expect(typeof admin.statistics.getCategories).toBe('function')
    })

    it('getHotItems 应该导出函数', () => {
      expect(typeof admin.statistics.getHotItems).toBe('function')
    })
  })

  describe('users', () => {
    it('getUsers 应该导出函数', () => {
      expect(typeof admin.users.getUsers).toBe('function')
    })

    it('getUser 应该导出函数', () => {
      expect(typeof admin.users.getUser).toBe('function')
    })

    it('getUserStats 应该导出函数', () => {
      expect(typeof admin.users.getUserStats).toBe('function')
    })

    it('createUser 应该导出函数', () => {
      expect(typeof admin.users.createUser).toBe('function')
    })

    it('updateUser 应该导出函数', () => {
      expect(typeof admin.users.updateUser).toBe('function')
    })

    it('updateStatus 应该导出函数', () => {
      expect(typeof admin.users.updateStatus).toBe('function')
    })

    it('batchUpdateStatus 应该导出函数', () => {
      expect(typeof admin.users.batchUpdateStatus).toBe('function')
    })

    it('deleteUsers 应该导出函数', () => {
      expect(typeof admin.users.deleteUsers).toBe('function')
    })

    it('exportUsers 应该导出函数', () => {
      expect(typeof admin.users.exportUsers).toBe('function')
    })
  })

  describe('items', () => {
    it('getItems 应该导出函数', () => {
      expect(typeof admin.items.getItems).toBe('function')
    })

    it('getItemStats 应该导出函数', () => {
      expect(typeof admin.items.getItemStats).toBe('function')
    })

    it('updateStatus 应该导出函数', () => {
      expect(typeof admin.items.updateStatus).toBe('function')
    })

    it('approveItem 应该导出函数', () => {
      expect(typeof admin.items.approveItem).toBe('function')
    })

    it('rejectItem 应该导出函数', () => {
      expect(typeof admin.items.rejectItem).toBe('function')
    })

    it('offShelfItem 应该导出函数', () => {
      expect(typeof admin.items.offShelfItem).toBe('function')
    })

    it('deleteItem 应该导出函数', () => {
      expect(typeof admin.items.deleteItem).toBe('function')
    })

    it('batchApprove 应该导出函数', () => {
      expect(typeof admin.items.batchApprove).toBe('function')
    })

    it('batchReject 应该导出函数', () => {
      expect(typeof admin.items.batchReject).toBe('function')
    })

    it('exportItems 应该导出函数', () => {
      expect(typeof admin.items.exportItems).toBe('function')
    })
  })

  describe('orders', () => {
    it('getOrders 应该导出函数', () => {
      expect(typeof admin.orders.getOrders).toBe('function')
    })

    it('cancelOrder 应该导出函数', () => {
      expect(typeof admin.orders.cancelOrder).toBe('function')
    })

    it('approveRefund 应该导出函数', () => {
      expect(typeof admin.orders.approveRefund).toBe('function')
    })

    it('batchCancel 应该导出函数', () => {
      expect(typeof admin.orders.batchCancel).toBe('function')
    })

    it('getStats 应该导出函数', () => {
      expect(typeof admin.orders.getStats).toBe('function')
    })

    it('getOrder 应该导出函数', () => {
      expect(typeof admin.orders.getOrder).toBe('function')
    })
  })

  describe('categories', () => {
    it('getCategories 应该导出函数', () => {
      expect(typeof admin.categories.getCategories).toBe('function')
    })

    it('createCategory 应该导出函数', () => {
      expect(typeof admin.categories.createCategory).toBe('function')
    })

    it('updateCategory 应该导出函数', () => {
      expect(typeof admin.categories.updateCategory).toBe('function')
    })

    it('deleteCategory 应该导出函数', () => {
      expect(typeof admin.categories.deleteCategory).toBe('function')
    })

    it('moveUp 应该导出函数', () => {
      expect(typeof admin.categories.moveUp).toBe('function')
    })

    it('moveDown 应该导出函数', () => {
      expect(typeof admin.categories.moveDown).toBe('function')
    })

    it('batchEnable 应该导出函数', () => {
      expect(typeof admin.categories.batchEnable).toBe('function')
    })

    it('batchDisable 应该导出函数', () => {
      expect(typeof admin.categories.batchDisable).toBe('function')
    })
  })

  describe('verifications', () => {
    it('getVerifications 应该导出函数', () => {
      expect(typeof admin.verifications.getVerifications).toBe('function')
    })

    it('approveVerification 应该导出函数', () => {
      expect(typeof admin.verifications.approveVerification).toBe('function')
    })

    it('rejectVerification 应该导出函数', () => {
      expect(typeof admin.verifications.rejectVerification).toBe('function')
    })

    it('batchApprove 应该导出函数', () => {
      expect(typeof admin.verifications.batchApprove).toBe('function')
    })

    it('batchReject 应该导出函数', () => {
      expect(typeof admin.verifications.batchReject).toBe('function')
    })

    it('getStats 应该导出函数', () => {
      expect(typeof admin.verifications.getStats).toBe('function')
    })
  })

  describe('logs', () => {
    it('getLogs 应该导出函数', () => {
      expect(typeof admin.logs.getLogs).toBe('function')
    })

    it('getLogStats 应该导出函数', () => {
      expect(typeof admin.logs.getLogStats).toBe('function')
    })

    it('getExport 应该导出函数', () => {
      expect(typeof admin.logs.getExport).toBe('function')
    })
  })

  describe('disputes', () => {
    it('list 应该导出函数', () => {
      expect(typeof admin.disputes.list).toBe('function')
    })

    it('stats 应该导出函数', () => {
      expect(typeof admin.disputes.stats).toBe('function')
    })

    it('handleDispute 应该导出函数', () => {
      expect(typeof admin.disputes.handleDispute).toBe('function')
    })

    it('batchApprove 应该导出函数', () => {
      expect(typeof admin.disputes.batchApprove).toBe('function')
    })

    it('batchClose 应该导出函数', () => {
      expect(typeof admin.disputes.batchClose).toBe('function')
    })

    it('export 应该导出函数', () => {
      expect(typeof admin.disputes.export).toBe('function')
    })
  })
})
