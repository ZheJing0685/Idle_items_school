// @ts-nocheck
import { describe, it, expect, vi, beforeEach } from 'vitest';

// Mock WebSocket
const mockWebSocket = {
  close: vi.fn(),
  send: vi.fn(),
  readyState: 1,
  onopen: null as (() => void) | null,
  onmessage: null as ((event: MessageEvent) => void) | null,
  onclose: null as (() => void) | null,
  onerror: null as ((error: Event) => void) | null,
};

// 添加WebSocket常量
const MockWebSocket = vi.fn(() => mockWebSocket);
MockWebSocket.CONNECTING = 0;
MockWebSocket.OPEN = 1;
MockWebSocket.CLOSING = 2;
MockWebSocket.CLOSED = 3;

vi.stubGlobal('WebSocket', MockWebSocket);

describe('WebSocket工具', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    // 重置回调
    mockWebSocket.onopen = null;
    mockWebSocket.onmessage = null;
    mockWebSocket.onclose = null;
    mockWebSocket.onerror = null;
  });

  it('应该能创建WebSocket连接', async () => {
    const wsService = await import('@/utils/websocket');
    const service = wsService.default;

    expect(service).toBeDefined();
    expect(typeof service.connect).toBe('function');
  });

  it('应该能处理连接关闭', async () => {
    const wsService = await import('@/utils/websocket');
    const service = wsService.default;

    service.connect('test-token', 'user-123');
    mockWebSocket.onclose?.();

    // onclose回调被调用后，connected应该为false
    expect(service.isConnected()).toBe(false);
  });

  it('应该能发送消息', async () => {
    const wsService = await import('@/utils/websocket');
    const service = wsService.default;

    // 先建立连接
    service.connect('test-token', 'user-123');
    mockWebSocket.onopen?.();

    // 发送消息
    const result = service.sendMessage('chat-123', 'sender-1', 'receiver-1', 'Hello');

    expect(result).toBe(true);
    expect(mockWebSocket.send).toHaveBeenCalled();
  });

  it('应该能处理STOMP CONNECTED消息', async () => {
    const wsService = await import('@/utils/websocket');
    const service = wsService.default;

    // 建立连接
    service.connect('test-token', 'user-123');
    mockWebSocket.onopen?.();

    // 模拟STOMP CONNECTED响应
    const connectedFrame = 'CONNECTED\nversion:1.1\n\n\0';
    mockWebSocket.onmessage?.({ data: connectedFrame } as MessageEvent);

    // 验证连接状态
    expect(service.isConnected()).toBe(true);
  });

  it('应该能处理STOMP MESSAGE消息', async () => {
    const wsService = await import('@/utils/websocket');
    const service = wsService.default;

    // 建立连接
    service.connect('test-token', 'user-123');
    mockWebSocket.onopen?.();

    // 注册消息处理器
    const chatHandler = vi.fn();
    service.onMessage('chat', chatHandler);

    // 模拟STOMP MESSAGE响应
    const messageFrame = 'MESSAGE\ndestination:/topic/chat/user-123\ncontent-type:application/json\n\n{"message":"test"}\0';
    mockWebSocket.onmessage?.({ data: messageFrame } as MessageEvent);

    // 验证处理器被调用
    expect(chatHandler).toHaveBeenCalledWith({ message: 'test' });
  });

  it('应该能断开连接并清理资源', async () => {
    const wsService = await import('@/utils/websocket');
    const service = wsService.default;

    // 建立连接
    service.connect('test-token', 'user-123');
    mockWebSocket.onopen?.();

    // 注册处理器
    service.onMessage('chat', vi.fn());

    // 断开连接
    service.disconnect();

    // 验证清理
    expect(service.isConnected()).toBe(false);
    expect(mockWebSocket.close).toHaveBeenCalled();
  });

  it('应该在未连接时拒绝发送消息', async () => {
    const wsService = await import('@/utils/websocket');
    const service = wsService.default;

    // 不建立连接，直接尝试发送
    const result = service.sendMessage('chat-123', 'sender-1', 'receiver-1', 'Hello');

    expect(result).toBe(false);
  });

  it('应该能处理WebSocket错误', async () => {
    const wsService = await import('@/utils/websocket');
    const service = wsService.default;

    // 尝试连接
    const connectPromise = service.connect('test-token', 'user-123');

    // 模拟错误
    mockWebSocket.onerror?.({ type: 'error' } as Event);

    // 应该拒绝Promise
    await expect(connectPromise).rejects.toBeDefined();
  });

  it('应该能处理多个消息处理器', async () => {
    const wsService = await import('@/utils/websocket');
    const service = wsService.default;

    // 建立连接
    service.connect('test-token', 'user-123');
    mockWebSocket.onopen?.();

    // 注册多个处理器
    const chatHandler1 = vi.fn();
    const chatHandler2 = vi.fn();
    service.onMessage('chat', chatHandler1);
    service.onMessage('notification', chatHandler2);

    // 模拟聊天消息
    const chatFrame = 'MESSAGE\ndestination:/topic/chat/user-123\ncontent-type:application/json\n\n{"type":"chat"}\0';
    mockWebSocket.onmessage?.({ data: chatFrame } as MessageEvent);

    // 模拟通知消息
    const notificationFrame = 'MESSAGE\ndestination:/topic/notifications/user-123\ncontent-type:application/json\n\n{"type":"notification"}\0';
    mockWebSocket.onmessage?.({ data: notificationFrame } as MessageEvent);

    // 验证处理器被调用
    expect(chatHandler1).toHaveBeenCalledWith({ type: 'chat' });
    expect(chatHandler2).toHaveBeenCalledWith({ type: 'notification' });
  });

  it('应该能订阅用户频道', async () => {
    const wsService = await import('@/utils/websocket');
    const service = wsService.default;

    // 建立连接
    service.connect('test-token', 'user-123');
    mockWebSocket.onopen?.();

    // 模拟CONNECTED响应触发订阅
    const connectedFrame = 'CONNECTED\nversion:1.1\n\n\0';
    mockWebSocket.onmessage?.({ data: connectedFrame } as MessageEvent);

    // 验证发送了订阅帧
    expect(mockWebSocket.send).toHaveBeenCalledTimes(3); // CONNECT + 2个SUBSCRIBE
  });

  it('应该能处理重连逻辑', async () => {
    const wsService = await import('@/utils/websocket');
    const service = wsService.default;

    // 建立连接
    service.connect('test-token', 'user-123');
    mockWebSocket.onopen?.();

    // 模拟断开连接
    mockWebSocket.onclose?.();

    // 验证重连调度（通过检查connected状态）
    expect(service.isConnected()).toBe(false);
  });
});
