/**
 * WebSocket 管理器
 * 单例模式，提供 subscribe/unsubscribe 接口
 * 支持自动重连（指数退避，最多 5 次）
 */

interface StompHeaders {
  [key: string]: string
}

type MessageHandler = (message: any) => void

/** 订阅记录 */
interface Subscription {
  topic: string
  handler: MessageHandler
}

class WebSocketManager {
  private static instance: WebSocketManager;

  private ws: WebSocket | null = null;
  private connected = false;
  private token = '';
  private userId = '';
  private connectionState: 'connecting' | 'connected' | 'disconnected' | 'reconnecting' = 'disconnected';

  /** 订阅列表（topic -> handler[]） */
  private subscriptions = new Map<string, Set<MessageHandler>>();
  /** 已发送的 STOMP 订阅 ID 集合（用于去重） */
  private stompSubscribedTopics = new Set<string>();

  // 重连相关
  private reconnectAttempt = 0;
  private maxReconnectAttempts = 5;
  private baseDelay = 1000;
  private timer: ReturnType<typeof setTimeout> | null = null;

  private constructor() {}

  static getInstance(): WebSocketManager {
    if (!WebSocketManager.instance) {
      WebSocketManager.instance = new WebSocketManager();
    }
    return WebSocketManager.instance;
  }

  /** 建立 WebSocket 连接 */
  connect(token: string, userId?: string): Promise<void> {
    this.token = token;
    if (userId) this.userId = userId;
    this.connectionState = 'connecting';

    return new Promise((resolve, reject) => {
      const hostname = window.location.hostname;
      const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
      const wsPort = import.meta.env.VITE_WS_PORT || '7000';
      const wsUrl = `${protocol}//${hostname}:${wsPort}/ws-native`;

      try {
        this.ws = new WebSocket(wsUrl);

        this.ws.onopen = () => {
          this.connected = true;
          this.connectionState = 'connected';
          this.reconnectAttempt = 0;

          const headers: StompHeaders = {
            'accept-version': '1.1,1.0',
            'heart-beat': '4000,4000',
          };
          if (token) {
            headers['Authorization'] = `Bearer ${token}`;
          }
          this.sendFrame('CONNECT', headers);
        };

        this.ws.onmessage = (event: MessageEvent) => {
          this.handleIncoming(event.data, resolve);
        };

        this.ws.onclose = () => {
          this.connected = false;
          this.connectionState = 'disconnected';
          this.stompSubscribedTopics.clear();
          this.scheduleReconnect();
        };

        this.ws.onerror = () => {
          this.connectionState = 'disconnected';
          reject(new Error('WebSocket connection error'));
        };
      } catch (err) {
        reject(err);
      }
    });
  }

  /** 订阅主题 */
  subscribe(topic: string, handler: MessageHandler): void {
    if (!this.subscriptions.has(topic)) {
      this.subscriptions.set(topic, new Set());
    }
    this.subscriptions.get(topic)!.add(handler);

    // 如果已连接且未发送过该主题的 STOMP SUBSCRIBE，立即发送
    if (this.connected && this.userId && !this.stompSubscribedTopics.has(topic)) {
      this.sendStompSubscribe(topic);
    }
  }

  /** 取消订阅 */
  unsubscribe(topic: string, handler: MessageHandler): void {
    const handlers = this.subscriptions.get(topic);
    if (handlers) {
      handlers.delete(handler);
      if (handlers.size === 0) {
        this.subscriptions.delete(topic);
      }
    }
  }

  /** 发送消息 */
  send(chatId: string, senderId: string, receiverId: string, content: string, messageType: string = 'TEXT'): boolean {
    if (!this.connected) return false;

    const payload = JSON.stringify({
      chatId, senderId, receiverId, content,
      messageType,
    });

    this.sendFrame('SEND', {
      'destination': '/app/chat/send',
      'content-type': 'application/json',
    }, payload);

    return true;
  }

  /** 断开连接 */
  disconnect(): void {
    this.clearTimer();
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
    this.connected = false;
    this.connectionState = 'disconnected';
    this.subscriptions.clear();
    this.stompSubscribedTopics.clear();
    this.reconnectAttempt = 0;
  }

  isConnected(): boolean {
    return this.connected;
  }

  getConnectionState(): string {
    return this.connectionState;
  }

  // ==================== 内部方法 ====================

  private sendFrame(command: string, headers: StompHeaders = {}, body = ''): void {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) return;

    let frame = command + '\n';
    for (const [k, v] of Object.entries(headers)) {
      frame += `${k}:${v}\n`;
    }
    frame += '\n';
    if (body) frame += body;
    frame += '\0';

    this.ws.send(frame);
  }

  /** 发送 STOMP SUBSCRIBE 帧 */
  private sendStompSubscribe(topic: string): void {
    const dest = this.getDestinationForTopic(topic);
    if (!dest) return;

    this.sendFrame('SUBSCRIBE', {
      'id': `sub-${topic}-${this.userId}`,
      'destination': dest,
    });
    this.stompSubscribedTopics.add(topic);
  }

  /** 根据主题获取 STOMP 目标地址 */
  private getDestinationForTopic(topic: string): string {
    if (topic === 'chat' && this.userId) return `/topic/chat/${this.userId}`;
    if (topic === 'notification' && this.userId) return `/topic/notifications/${this.userId}`;
    return '';
  }

  /** 处理收到的消息 */
  private handleIncoming(data: string, connectResolve?: (value: void) => void): void {
    const lines = data.split('\n');
    const headers: StompHeaders = {};
    let command = '';
    let body = '';
    let headerEnd = false;

    if (lines.length > 0) {
      command = lines[0].trim();
    }

    for (let i = 1; i < lines.length; i++) {
      const line = lines[i];
      if (line.trim() === '' || line.trim() === '\0') {
        headerEnd = true;
        continue;
      }
      if (!headerEnd) {
        const idx = line.indexOf(':');
        if (idx > 0) {
          headers[line.substring(0, idx).trim()] = line.substring(idx + 1).trim();
        }
      } else {
        body += line;
      }
    }

    body = body.replace(/\0/g, '');

    if (command === 'CONNECTED') {
      // STOMP 连接成功，重新订阅所有本地订阅
      this.subscriptions.forEach((_handlers, topic) => {
        this.sendStompSubscribe(topic);
      });
      if (connectResolve) connectResolve();
      return;
    }

    if (command === 'MESSAGE' || headers['content-type'] === 'application/json') {
      try {
        const message = JSON.parse(body);
        const destination = headers['destination'] || '';

        // 根据 destination 路由到对应的 topic
        let matchedTopic = '';
        if (destination.includes('/topic/chat/')) matchedTopic = 'chat';
        else if (destination.includes('/topic/notifications/')) matchedTopic = 'notification';

        if (matchedTopic) {
          const handlers = this.subscriptions.get(matchedTopic);
          if (handlers) {
            handlers.forEach(h => {
              try { h(message); } catch (e) { console.error('WebSocket 处理器异常:', e); }
            });
          }
        }
      } catch {
        // 解析失败，忽略
      }
    }
  }

  /** 自动重连（指数退避，最多 5 次） */
  private scheduleReconnect(): void {
    this.clearTimer();

    if (this.reconnectAttempt >= this.maxReconnectAttempts) {
      console.error('WebSocket 重连已达上限，停止重连');
      return;
    }

    const delay = Math.min(
      this.baseDelay * Math.pow(2, this.reconnectAttempt) + Math.random() * 1000,
      30000,
    );

    console.log(`WebSocket 将在 ${Math.round(delay)}ms 后重连（第 ${this.reconnectAttempt + 1}/${this.maxReconnectAttempts} 次）`);

    this.timer = setTimeout(() => {
      this.reconnectAttempt++;
      this.connectionState = 'reconnecting';
      this.connect(this.token, this.userId)
        .then(() => {
          console.log('WebSocket 重连成功');
          this.reconnectAttempt = 0;
        })
        .catch(() => {
          this.scheduleReconnect();
        });
    }, delay);
  }

  private clearTimer(): void {
    if (this.timer) {
      clearTimeout(this.timer);
      this.timer = null;
    }
  }
}

/** 单例导出 */
export const wsManager = WebSocketManager.getInstance();

/** 向后兼容：保留旧的 wsService 导出 */
export const wsService = wsManager;

export default wsManager;
