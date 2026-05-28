import { describe, it, expect, vi, beforeEach } from 'vitest'
import notification from '../../../src/api/services/notification'

describe('Notification Service', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getNotifications 应该导出函数', () => {
    expect(typeof notification.getNotifications).toBe('function')
  })

  it('getUnreadCount 应该导出函数', () => {
    expect(typeof notification.getUnreadCount).toBe('function')
  })

  it('markAsRead 应该导出函数', () => {
    expect(typeof notification.markAsRead).toBe('function')
  })

  it('markAllAsRead 应该导出函数', () => {
    expect(typeof notification.markAllAsRead).toBe('function')
  })

  it('deleteNotification 应该导出函数', () => {
    expect(typeof notification.deleteNotification).toBe('function')
  })
})
