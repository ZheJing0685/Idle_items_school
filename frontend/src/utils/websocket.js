// WebSocket服务封装
// 使用原生WebSocket + 简单的STOMP协议实现

class WebSocketService {
  constructor() {
    this.ws = null;
    this.connected = false;
    this.subscriptions = new Map();
    this.messageHandlers = new Map();
    this.reconnectTimer = null;
    this.userId = null;
    this.reconnectAttempt = 0;
  }

  connect(token, userId) {
    return new Promise((resolve, reject) => {
      this.userId = userId;
      
      // 根据当前页面URL动态构建WebSocket地址
      // 使用原生WebSocket端点 /ws-native
      const hostname = window.location.hostname;
      const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
      // 从环境变量读取WebSocket端口，默认为7000
      const wsPort = import.meta.env.VITE_WS_PORT || '7000';
      const wsUrl = `${protocol}//${hostname}:${wsPort}/ws-native`;
      
      console.log('Connecting to WebSocket:', wsUrl);
      
      try {
        this.ws = new WebSocket(wsUrl);
        
        this.ws.onopen = () => {
          console.log('WebSocket已连接，发送STOMP CONNECT...');
          this.connected = true;
          
          // 发送STOMP CONNECT帧
          this.sendStompFrame('CONNECT', {
            'accept-version': '1.1,1.0',
            'heart-beat': '4000,4000',
            'Authorization': `Bearer ${token}`
          });
          
          // 不在这里resolve，等收到CONNECTED帧后再resolve
        };
        
        this.ws.onmessage = (event) => {
          this.handleMessage(event.data, token, userId, resolve);
        };
        
        this.ws.onclose = () => {
          console.log('WebSocket已断开');
          this.connected = false;
          this.scheduleReconnect(token, userId);
        };
        
        this.ws.onerror = (error) => {
          console.error('WebSocket错误:', error);
          reject(error);
        };
      } catch (error) {
        reject(error);
      }
    });
  }

  handleMessage(data, token, userId, connectResolve) {
    // 解析STOMP帧
    const lines = data.split('\n');
    const headers = {};
    let body = '';
    let headerEnd = false;
    let command = '';
    
    // 第一行是命令
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
        // 正确解析STOMP头：找到第一个冒号的位置
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
    
    // 去除STOMP帧末尾的空字节
    body = body.replace(/\0/g, '');
    
    // 处理CONNECTED帧 - 连接成功后自动订阅
    if (command === 'CONNECTED') {
      console.log('STOMP连接成功，准备订阅...');
      this.subscribeToUserChannel(userId);
      if (connectResolve) connectResolve();
      return;
    }
    
    // 处理MESSAGE帧
    if (command === 'MESSAGE' || headers['content-type'] === 'application/json') {
      try {
        const message = JSON.parse(body);
        const destination = headers['destination'] || '';
        console.log('收到WebSocket消息:', destination, message);
        
        // 根据destination分发消息
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

  sendStompFrame(command, headers = {}, body = '') {
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

  subscribeToUserChannel(userId) {
    if (!this.connected) return;
    
    // 订阅聊天消息
    this.sendStompFrame('SUBSCRIBE', {
      'id': 'sub-chat-' + userId,
      'destination': `/topic/chat/${userId}`
    });
    
    // 订阅通知消息
    this.sendStompFrame('SUBSCRIBE', {
      'id': 'sub-notifications-' + userId,
      'destination': `/topic/notifications/${userId}`
    });
  }

  sendMessage(chatId, senderId, receiverId, content) {
    if (!this.connected) {
      console.error('WebSocket未连接');
      return false;
    }
    
    const message = JSON.stringify({
      chatId,
      senderId,
      receiverId,
      content,
      messageType: 'TEXT'
    });
    
    this.sendStompFrame('SEND', {
      'destination': '/app/chat/send',
      'content-type': 'application/json'
    }, message);
    
    return true;
  }

  onMessage(type, handler) {
    this.messageHandlers.set(type, handler);
  }

  scheduleReconnect(token, userId) {
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
    
    // 指数退避算法
    const delay = Math.min(baseDelay * Math.pow(2, this.reconnectAttempt), maxDelay);
    
    this.reconnectTimer = setTimeout(() => {
      console.log(`尝试重新连接WebSocket... (第${this.reconnectAttempt + 1}次)`);
      this.connect(token, userId).then(() => {
        this.subscribeToUserChannel(userId);
        this.reconnectAttempt = 0; // 重连成功，重置计数器
      }).catch(() => {
        this.reconnectAttempt++;
        this.scheduleReconnect(token, userId);
      });
    }, delay);
  }

  disconnect() {
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

  isConnected() {
    return this.connected;
  }
}

export const wsService = new WebSocketService();
export default wsService;
