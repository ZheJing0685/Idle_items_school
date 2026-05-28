import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'

// Mock WebSocket
const mockWebSocket = {
  close: vi.fn(),
  send: vi.fn(),
  readyState: 1,
  onopen: null,
  onmessage: null,
  onclose: null,
  onerror: null
}

vi.stubGlobal('WebSocket', vi.fn(() => mockWebSocket))

describe('WebSocket工具', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('应该能创建WebSocket连接', async () => {
    const wsService = await import('@/utils/websocket')
    const service = wsService.default
    
    expect(service).toBeDefined()
    expect(typeof service.connect).toBe('function')
  })

  it('应该能处理连接关闭', async () => {
    const wsService = await import('@/utils/websocket')
    const service = wsService.default
    
    service.connect('test-token', 'user-123')
    mockWebSocket.onclose?.({ code: 1000, reason: 'Normal closure' })
    
    // onclose回调被调用后，connected应该为false
    expect(service.isConnected()).toBe(false)
  })
})
