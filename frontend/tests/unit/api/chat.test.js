import { describe, it, expect, vi, beforeEach } from 'vitest'
import chat from '../../../src/api/services/chat'

describe('Chat Service', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getChats 应该导出函数', () => {
    expect(typeof chat.getChats).toBe('function')
  })

  it('createChat 应该导出函数', () => {
    expect(typeof chat.createChat).toBe('function')
  })

  it('getMessages 应该导出函数', () => {
    expect(typeof chat.getMessages).toBe('function')
  })

  it('sendMessage 应该导出函数', () => {
    expect(typeof chat.sendMessage).toBe('function')
  })
})
