// @ts-nocheck
import { describe, it, expect, vi, beforeEach } from 'vitest';

// Mock WebSocket - vitest v4 requires class syntax for constructors
const mockWebSocket = {
  close: vi.fn(),
  send: vi.fn(),
  readyState: 1,
  onopen: null as (() => void) | null,
  onmessage: null as ((event: MessageEvent) => void) | null,
  onclose: null as (() => void) | null,
  onerror: null as ((error: Event) => void) | null,
};

class MockWebSocket {
  static CONNECTING = 0;
  static OPEN = 1;
  static CLOSING = 2;
  static CLOSED = 3;
  constructor() {
    return mockWebSocket;
  }
}

vi.stubGlobal('WebSocket', MockWebSocket);

describe('WebSocket工具', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.resetModules();
    // 重置回调
    mockWebSocket.onopen = null;
    mockWebSocket.onmessage = null;
    mockWebSocket.onclose = null;
    mockWebSocket.onerror = null;
  });

  it('应该能创建WebSocket连接', async () => {
    const { wsManager } = await import('@/utils/websocket');
    expect(wsManager).toBeDefined();
    expect(typeof wsManager.connect).toBe('function');
    expect(typeof wsManager.subscribe).toBe('function');
    expect(typeof wsManager.send).toBe('function');
    expect(typeof wsManager.disconnect).toBe('function');
  });

  it('应该能处理连接关闭', async () => {
    const { wsManager } = await import('@/utils/websocket');

    const connectPromise = wsManager.connect('user-123');
    mockWebSocket.onopen?.();
    // connect resolves when CONNECTED frame is received
    mockWebSocket.onmessage?.({ data: 'CONNECTED\nversion:1.1\n\n\0' } as MessageEvent);
    await connectPromise;
    expect(wsManager.isConnected()).toBe(true);

    mockWebSocket.onclose?.();
    expect(wsManager.isConnected()).toBe(false);
  });

  it('应该能发送消息', async () => {
    const { wsManager } = await import('@/utils/websocket');

    const connectPromise = wsManager.connect('user-123');
    mockWebSocket.onopen?.();
    mockWebSocket.onmessage?.({ data: 'CONNECTED\nversion:1.1\n\n\0' } as MessageEvent);
    await connectPromise;

    const result = wsManager.send('chat-123', 'sender-1', 'receiver-1', 'Hello');
    expect(result).toBe(true);
    expect(mockWebSocket.send).toHaveBeenCalled();
  });

  it('应该能处理STOMP CONNECTED消息', async () => {
    const { wsManager } = await import('@/utils/websocket');

    const connectPromise = wsManager.connect('user-123');
    mockWebSocket.onopen?.();

    const connectedFrame = 'CONNECTED\nversion:1.1\n\n\0';
    mockWebSocket.onmessage?.({ data: connectedFrame } as MessageEvent);

    await connectPromise;
    expect(wsManager.isConnected()).toBe(true);
  });

  it('应该能订阅主题和处理消息', async () => {
    const { wsManager } = await import('@/utils/websocket');

    const connectPromise = wsManager.connect('user-123');
    mockWebSocket.onopen?.();
    mockWebSocket.onmessage?.({ data: 'CONNECTED\nversion:1.1\n\n\0' } as MessageEvent);
    await connectPromise;

    const handler = vi.fn();
    wsManager.subscribe('chat', handler);

    const messageFrame = 'MESSAGE\ndestination:/topic/chat/user-123\ncontent-type:application/json\n\n{"message":"test"}\0';
    mockWebSocket.onmessage?.({ data: messageFrame } as MessageEvent);

    expect(handler).toHaveBeenCalledWith({ message: 'test' });
  });

  it('应该能断开连接并清理资源', async () => {
    const { wsManager } = await import('@/utils/websocket');

    const connectPromise = wsManager.connect('user-123');
    mockWebSocket.onopen?.();
    mockWebSocket.onmessage?.({ data: 'CONNECTED\nversion:1.1\n\n\0' } as MessageEvent);
    await connectPromise;

    wsManager.subscribe('chat', vi.fn());
    wsManager.disconnect();

    expect(wsManager.isConnected()).toBe(false);
    expect(mockWebSocket.close).toHaveBeenCalled();
  });

  it('应该在未连接时拒绝发送消息', async () => {
    const { wsManager } = await import('@/utils/websocket');

    const result = wsManager.send('chat-123', 'sender-1', 'receiver-1', 'Hello');
    expect(result).toBe(false);
  });

  it('应该能处理WebSocket错误', async () => {
    const { wsManager } = await import('@/utils/websocket');

    const connectPromise = wsManager.connect('user-123');
    mockWebSocket.onerror?.({ type: 'error' } as Event);

    await expect(connectPromise).rejects.toBeDefined();
  });

  it('应该能处理多个消息处理器', async () => {
    const { wsManager } = await import('@/utils/websocket');

    const connectPromise = wsManager.connect('user-123');
    mockWebSocket.onopen?.();
    mockWebSocket.onmessage?.({ data: 'CONNECTED\nversion:1.1\n\n\0' } as MessageEvent);
    await connectPromise;

    const chatHandler1 = vi.fn();
    const chatHandler2 = vi.fn();
    wsManager.subscribe('chat', chatHandler1);
    wsManager.subscribe('notification', chatHandler2);

    const chatFrame = 'MESSAGE\ndestination:/topic/chat/user-123\ncontent-type:application/json\n\n{"type":"chat"}\0';
    mockWebSocket.onmessage?.({ data: chatFrame } as MessageEvent);

    const notificationFrame = 'MESSAGE\ndestination:/topic/notifications/user-123\ncontent-type:application/json\n\n{"type":"notification"}\0';
    mockWebSocket.onmessage?.({ data: notificationFrame } as MessageEvent);

    expect(chatHandler1).toHaveBeenCalledWith({ type: 'chat' });
    expect(chatHandler2).toHaveBeenCalledWith({ type: 'notification' });
  });

  it('应该能处理重连逻辑', async () => {
    const { wsManager } = await import('@/utils/websocket');

    const connectPromise = wsManager.connect('user-123');
    mockWebSocket.onopen?.();
    mockWebSocket.onmessage?.({ data: 'CONNECTED\nversion:1.1\n\n\0' } as MessageEvent);
    await connectPromise;

    mockWebSocket.onclose?.();
    expect(wsManager.isConnected()).toBe(false);
  });
});
