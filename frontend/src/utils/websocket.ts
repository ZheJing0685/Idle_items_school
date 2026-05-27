interface StompHeaders {
  [key: string]: string
}

interface StompFrame {
  command: string
  headers: StompHeaders
  body: string
}

type MessageHandler = (message: any) => void

class WebSocketService {
  private ws: WebSocket | null = null;
  private connected: boolean = false;
  private subscriptions: Map<string, string> = new Map();
  private messageHandlers: Map<string, MessageHandler> = new Map();
  private reconnectTimer: ReturnType<typeof setTimeout> | null = null;
  private userId: string | null = null;
  private reconnectAttempt: number = 0;

  connect(token: string, userId: string): Promise<void> {
    return new Promise((resolve, reject) => {
      this.userId = userId;

      const hostname = window.location.hostname;
      const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
      const wsPort = import.meta.env.VITE_WS_PORT || '7000';
      const wsUrl = `${protocol}//${hostname}:${wsPort}/ws-native`;

      console.log('Connecting to WebSocket:', wsUrl);

      try {
        this.ws = new WebSocket(wsUrl);

        this.ws.onopen = () => {
          console.log('WebSocket已连接，发送STOMP CONNECT...');
          this.connected = true;

          this.sendStompFrame('CONNECT', {
            'accept-version': '1.1,1.0',
            'heart-beat': '4000,4000',
            'Authorization': `Bearer ${token}`,
          });
        };

        this.ws.onmessage = (event: MessageEvent) => {
          this.handleMessage(event.data, token, userId, resolve);
        };

        this.ws.onclose = () => {
          console.log('WebSocket已断开');
          this.connected = false;
          this.scheduleReconnect(token, userId);
        };

        this.ws.onerror = (error: Event) => {
          console.error('WebSocket错误:', error);
          reject(error);
        };
      } catch (error) {
        reject(error);
      }
    });
  }

  handleMessage(
    data: string,
    token: string,
    userId: string,
    connectResolve?: (value: void | PromiseLike<void>) => void,
  ): void {
    const lines = data.split('\n');
    const headers: StompHeaders = {};
    let body = '';
    let headerEnd = false;
    let command = '';

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
        const colonIndex = line.indexOf(':');
        if (colonIndex > 0) {
          const key = line.substring(0, colonIndex).trim();
          const value = line.substring(colonIndex + 1).trim();
          headers[key] = value;
        }
      } else {
        body += line;
      }
    }

    body = body.replace(/\0/g, '');

    if (command === 'CONNECTED') {
      console.log('STOMP连接成功，准备订阅...');
      this.subscribeToUserChannel(userId);
      if (connectResolve) connectResolve();
      return;
    }

    if (command === 'MESSAGE' || headers['content-type'] === 'application/json') {
      try {
        const message = JSON.parse(body);
        const destination = headers['destination'] || '';
        console.log('收到WebSocket消息:', destination, message);

        if (destination.includes('/topic/chat/')) {
          const handler = this.messageHandlers.get('chat');
          if (handler) handler(message);
        } else if (destination.includes('/topic/notifications/')) {
          const handler = this.messageHandlers.get('notification');
          if (handler) handler(message);
        }
      } catch (e) {
        console.error('解析消息失败:', e);
      }
    }
  }

  sendStompFrame(command: string, headers: StompHeaders = {}, body: string = ''): void {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
      console.error('WebSocket未连接');
      return;
    }

    let frame = command + '\n';
    for (const [key, value] of Object.entries(headers)) {
      frame += `${key}:${value}\n`;
    }
    frame += '\n';
    if (body) {
      frame += body;
    }
    frame += '\0';

    this.ws.send(frame);
  }

  subscribeToUserChannel(userId: string): void {
    if (!this.connected) return;

    this.sendStompFrame('SUBSCRIBE', {
      'id': 'sub-chat-' + userId,
      'destination': `/topic/chat/${userId}`,
    });

    this.sendStompFrame('SUBSCRIBE', {
      'id': 'sub-notifications-' + userId,
      'destination': `/topic/notifications/${userId}`,
    });
  }

  sendMessage(chatId: string, senderId: string, receiverId: string, content: string): boolean {
    if (!this.connected) {
      console.error('WebSocket未连接');
      return false;
    }

    const message = JSON.stringify({
      chatId,
      senderId,
      receiverId,
      content,
      messageType: 'TEXT',
    });

    this.sendStompFrame('SEND', {
      'destination': '/app/chat/send',
      'content-type': 'application/json',
    }, message);

    return true;
  }

  onMessage(type: string, handler: MessageHandler): void {
    this.messageHandlers.set(type, handler);
  }

  scheduleReconnect(token: string, userId: string): void {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
    }

    const maxAttempts = 10;
    const baseDelay = 1000;
    const maxDelay = 30000;

    if (this.reconnectAttempt >= maxAttempts) {
      console.log('达到最大重连次数，停止重连');
      return;
    }

    const delay = Math.min(baseDelay * Math.pow(2, this.reconnectAttempt), maxDelay);

    this.reconnectTimer = setTimeout(() => {
      console.log(`尝试重新连接WebSocket... (第${this.reconnectAttempt + 1}次)`);
      this.connect(token, userId).then(() => {
        this.subscribeToUserChannel(userId);
        this.reconnectAttempt = 0;
      }).catch(() => {
        this.reconnectAttempt++;
        this.scheduleReconnect(token, userId);
      });
    }, delay);
  }

  disconnect(): void {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
    }

    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }

    this.connected = false;
    this.subscriptions.clear();
    this.messageHandlers.clear();
    this.reconnectAttempt = 0;
  }

  isConnected(): boolean {
    return this.connected;
  }
}

export const wsService = new WebSocketService();
export default wsService;
