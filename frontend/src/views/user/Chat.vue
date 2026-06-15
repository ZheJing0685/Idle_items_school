<template>
  <div class="chat-page">
    <div class="chat-container">
      <!-- 左侧会话列表 -->
      <div class="chat-sidebar">
        <div class="sidebar-header">
          <h3>消息</h3>
        </div>
        <div class="chat-list">
          <div
            v-for="chat in chatList"
            :key="chat.id"
            class="chat-item"
            :class="{ active: currentChat?.id === chat.id }"
            @click="selectChat(chat)"
          >
            <div class="chat-avatar">
              <img v-if="getOtherUser(chat).avatar" :src="getOtherUser(chat).avatar" alt="" loading="lazy" />
              <el-avatar v-else :size="40">{{ getOtherUser(chat).nickname?.charAt(0) || getOtherUser(chat).username?.charAt(0) || '用' }}</el-avatar>
            </div>
            <div class="chat-info">
              <div class="chat-name">{{ getOtherUser(chat).nickname || getOtherUser(chat).username }}</div>
              <div class="chat-last-message">
                <span v-if="chat.lastMessageSenderId && Number(chat.lastMessageSenderId) !== Number(currentUserId)">{{ getOtherUser(chat).nickname || getOtherUser(chat).username }}：</span>{{ chat.lastMessage || '暂无消息' }}
              </div>
            </div>
            <div class="chat-time">{{ formatTime(chat.lastMessageTime || chat.updatedAt) }}</div>
          </div>
          <div v-if="chatList.length === 0" class="empty-chat">暂无聊天记录</div>
        </div>
      </div>

      <!-- 右侧聊天窗口 -->
      <div class="chat-main">
        <template v-if="currentChat">
          <div class="chat-header">
            <div class="header-info">
              <span class="user-name">{{ getOtherUser(currentChat).nickname || getOtherUser(currentChat).username }}</span>
            </div>
          </div>

          <div class="message-list" ref="messageList" @scroll="handleScroll">
            <div v-if="loadingMore" class="loading-more">加载中...</div>
            <template v-for="item in messagesWithSeparators" :key="item.key">
              <div v-if="item.type === 'separator'" class="date-separator">
                <span class="date-separator-text">{{ formatDateSeparator(item.date) }}</span>
              </div>
              <div
                v-else
                class="message-item"
                :class="{ 'is-mine': item.msg.senderId === currentUserId }"
              >
                <div class="message-avatar">
                  <img v-if="getMessageAvatar(item.msg)" :src="getMessageAvatar(item.msg)" alt="" loading="lazy" />
                  <el-avatar v-else :size="40">{{ item.msg.senderId === currentUserId ? (userStore.user?.nickname?.charAt(0) || userStore.user?.username?.charAt(0) || '我') : (getOtherUser(currentChat)?.nickname?.charAt(0) || getOtherUser(currentChat)?.username?.charAt(0) || '对') }}</el-avatar>
                </div>
                <div class="message-content">
                  <div v-if="item.msg.messageType === 'IMAGE'" class="message-image">
                    <img :src="item.msg.content" alt="图片" loading="lazy" @click="previewImage(item.msg.content)" />
                  </div>
                  <div v-else-if="item.msg.messageType === 'VIDEO'" class="message-video">
                    <video :src="item.msg.content" controls preload="metadata"></video>
                  </div>
                  <div v-else class="message-text">{{ item.msg.content }}</div>
                  <div class="message-time">{{ formatMessageTime(item.msg.createdAt) }}</div>
                </div>
              </div>
            </template>
          </div>

          <div class="message-input">
            <div class="input-toolbar">
              <el-upload
                :show-file-list="false"
                :before-upload="handleImageUpload"
                accept="image/jpeg,image/png,image/webp"
                :auto-upload="false"
              >
                <el-button class="toolbar-btn" title="发送图片">
                  <ImageIcon :size="18" stroke-width="1.5" />
                </el-button>
              </el-upload>
              <el-upload
                :show-file-list="false"
                :before-upload="handleVideoUpload"
                accept="video/mp4,video/quicktime,video/webm"
                :auto-upload="false"
              >
                <el-button class="toolbar-btn" title="发送视频">
                  <VideoIcon :size="18" stroke-width="1.5" />
                </el-button>
              </el-upload>
            </div>
            <el-input v-model="newMessage" placeholder="输入消息..." @keyup.enter="sendMessage">
              <template #append>
                <el-button @click="sendMessage" :disabled="!newMessage.trim()">发送</el-button>
              </template>
            </el-input>
          </div>
        </template>
        <template v-else>
          <div class="no-chat-selected">
            <MessageSquare :size="64" stroke-width="1.5" />
            <p>选择一个会话开始聊天</p>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { MessageSquare, Image as ImageIcon, Video as VideoIcon } from 'lucide-vue-next';
import { useUserStore } from '@/store';
import { wsManager } from '@/utils/websocket';
import chatApi from '@/api/services/chat';

const route = useRoute();
const userStore = useUserStore();
const currentUserId = ref(userStore.user?.id);

const chatList = ref<any[]>([]);
const currentChat = ref<any>(null);
const messages = ref<any[]>([]);
const newMessage = ref('');
const messageList = ref<HTMLDivElement | null>(null);

// 分页状态
const messagePage = ref(0);
const hasMoreMessages = ref(false);
const loadingMore = ref(false);
const PAGE_SIZE = 50;

// 消息缓存：chatId -> messages[]
const messageCache = reactive<Map<string, any[]>>(new Map());
// 已加载过的聊天ID集合
const loadedChatIds = reactive<Set<string>>(new Set());

const loadChatList = async () => {
  try {
    console.log('Loading chat list...');
    const res = await chatApi.getChats();
    console.log('Chat list response:', res);

    // 后端返回格式: {code: 200, data: [...]} 或 {code: 200, data: {content: [...]}}
    let chatData: any[] = [];

    if (res && res.data) {
      if (Array.isArray(res.data)) {
        // 新格式: data直接是数组
        chatData = res.data;
      } else if (res.data.content && Array.isArray(res.data.content)) {
        // 旧格式: data.content是数组
        chatData = res.data.content;
      }
    }

    chatList.value = chatData;
    console.log('Chat list:', chatList.value);
    console.log('Chat list length:', chatList.value.length);

    // 检查URL参数中是否有chatId，如果有则自动选中
    const targetChatId = route.query.chatId;
    console.log('Target chat ID from URL:', targetChatId);

    if (targetChatId && chatList.value.length > 0) {
      const targetChat = chatList.value.find(chat => String(chat.id) === String(targetChatId));
      console.log('Found target chat:', targetChat);
      if (targetChat) {
        await selectChat(targetChat);
      } else {
        console.log('Target chat not found in list');
      }
    }
  } catch (error) {
    console.error('加载聊天列表失败:', error);
  }
};

const selectChat = async (chat: any) => {
  if (currentChat.value && currentChat.value.id === chat.id) {
    return;
  }

  // 重置分页状态
  messagePage.value = 0;
  hasMoreMessages.value = true;
  loadingMore.value = false;

  currentChat.value = chat;
  const chatId = String(chat.id);

  if (messageCache.has(chatId) && loadedChatIds.has(chatId)) {
    messages.value = messageCache.get(chatId) || [];
    scrollToBottom();
    return;
  }

  await loadMessages(chatId);
};

const loadMessages = async (chatId: string) => {
  try {
    messagePage.value = 0;
    hasMoreMessages.value = true;

    const res = await chatApi.getMessages(chatId, { page: 0, size: PAGE_SIZE });
    let msgData: any[] = [];

    if (Array.isArray(res.data)) {
      msgData = res.data;
    } else if (res.data?.content && Array.isArray(res.data.content)) {
      msgData = res.data.content;
    } else if (Array.isArray(res)) {
      msgData = res as any;
    }

    // DESC（最新在前）→ 翻转后 ASC（最旧在前，最新在底）
    const reversed = [...msgData].reverse();
    messages.value = reversed;
    hasMoreMessages.value = msgData.length === PAGE_SIZE;

    messageCache.set(chatId, reversed);
    loadedChatIds.add(chatId);

    scrollToBottom();
  } catch (error) {
    console.error('加载消息失败:', error);
  }
};

const loadMoreMessages = async () => {
  if (loadingMore.value || !hasMoreMessages.value || !currentChat.value) return;

  loadingMore.value = true;
  try {
    const nextPage = messagePage.value + 1;
    const chatId = String(currentChat.value.id);
    const res = await chatApi.getMessages(chatId, { page: nextPage, size: PAGE_SIZE });
    let olderMsgs: any[] = [];

    if (Array.isArray(res.data)) {
      olderMsgs = res.data;
    } else if (res.data?.content && Array.isArray(res.data.content)) {
      olderMsgs = res.data.content;
    } else if (Array.isArray(res)) {
      olderMsgs = res as any;
    }

    if (olderMsgs.length === 0) {
      hasMoreMessages.value = false;
    } else {
      const el = messageList.value;
      const prevScrollHeight = el?.scrollHeight || 0;

      // DESC分页：下一页 = 更早的消息，翻转后前置
      const reversedOlder = [...olderMsgs].reverse();
      messages.value = [...reversedOlder, ...messages.value];
      messagePage.value = nextPage;
      hasMoreMessages.value = olderMsgs.length === PAGE_SIZE;

      messageCache.set(chatId, messages.value);

      if (el) {
        await new Promise(r => setTimeout(r, 0));
        const newScrollHeight = el.scrollHeight;
        el.scrollTop += newScrollHeight - prevScrollHeight;
      }
    }
  } catch (error) {
    console.error('加载历史消息失败:', error);
  } finally {
    loadingMore.value = false;
  }
};

const handleScroll = () => {
  const el = messageList.value;
  if (el && el.scrollTop < 100) {
    loadMoreMessages();
  }
};

const sendMessage = async () => {
  if (!newMessage.value.trim() || !currentChat.value) return;
  const content = newMessage.value.trim();
  try {
    await chatApi.sendMessage(currentChat.value.id, String(currentChat.value.buyerId === currentUserId.value ? currentChat.value.sellerId : currentChat.value.buyerId), content);
    newMessage.value = '';
  } catch (error) {
    ElMessage.error('发送消息失败');
  }
};

const getReceiverId = () => {
  if (!currentChat.value) return '';
  return String(currentChat.value.buyerId === currentUserId.value ? currentChat.value.sellerId : currentChat.value.buyerId);
};

const sendMediaMessage = async (file: File, messageType: string) => {
  if (!currentChat.value) return;
  try {
    const res = await chatApi.uploadChatMedia(file);
    const url = (res.data as any).url;
    if (!url) {
      ElMessage.error('文件上传失败');
      return;
    }
    await chatApi.sendMessage(currentChat.value.id, getReceiverId(), url, messageType);
  } catch (error) {
    ElMessage.error('文件发送失败');
  }
};

const handleImageUpload = (file: File) => {
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 10MB');
    return false;
  }
  sendMediaMessage(file, 'IMAGE');
  return false;
};

const handleVideoUpload = (file: File) => {
  if (file.size > 100 * 1024 * 1024) {
    ElMessage.error('视频大小不能超过 100MB');
    return false;
  }
  sendMediaMessage(file, 'VIDEO');
  return false;
};

const previewImage = (url: string) => {
  window.open(url, '_blank');
};

const getOtherUser = (chat: any) => {
  if (!chat) return {};
  return chat.buyerId === currentUserId.value
    ? { id: chat.sellerId, nickname: chat.sellerNickname, username: chat.sellerUsername, avatar: chat.sellerAvatar }
    : { id: chat.buyerId, nickname: chat.buyerNickname, username: chat.buyerUsername, avatar: chat.buyerAvatar };
};

const getMessageAvatar = (msg: any) => {
  if (msg.senderId === currentUserId.value) {
    return userStore.user?.avatar || null;
  }
  // 对方的消息，从 currentChat 获取对方头像
  const other = getOtherUser(currentChat.value);
  return other.avatar || null;
};

const formatTime = (time: any) => {
  if (!time) return '';
  const date = typeof time === 'number' ? new Date(time) : new Date(time);
  if (isNaN(date.getTime())) return '';
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  if (diff < 0) return '刚刚';
  if (diff < 60000) return '刚刚';
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前';
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前';
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const msgDay = new Date(date.getFullYear(), date.getMonth(), date.getDate());
  if (today.getTime() === msgDay.getTime()) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
  }
  const yesterday = new Date(today);
  yesterday.setDate(yesterday.getDate() - 1);
  if (msgDay.getTime() === yesterday.getTime()) {
    return '昨天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
  }
  return date.toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' }) + ' ' +
         date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
};

const formatMessageTime = (time: string) => {
  if (!time) return '';
  const date = new Date(time);
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
};

const getMessageDateKey = (time: string): string => {
  const date = new Date(time);
  return `${date.getFullYear()}-${date.getMonth()}-${date.getDate()}`;
};

const formatDateSeparator = (time: string): string => {
  const date = new Date(time);
  const now = new Date();
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const msgDay = new Date(date.getFullYear(), date.getMonth(), date.getDate());

  if (today.getTime() === msgDay.getTime()) {
    return '今天';
  }

  const yesterday = new Date(today);
  yesterday.setDate(yesterday.getDate() - 1);
  if (msgDay.getTime() === yesterday.getTime()) {
    return '昨天';
  }

  if (date.getFullYear() === now.getFullYear()) {
    return `${date.getMonth() + 1}月${date.getDate()}日`;
  }

  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`;
};

const messagesWithSeparators = computed(() => {
  const sorted = [...messages.value].sort((a, b) => {
    return new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
  });

  const result: Array<{ type: 'separator'; date: string; key: string } | { type: 'message'; msg: any; key: string }> = [];
  let lastDateKey = '';

  for (const msg of sorted) {
    const dateKey = getMessageDateKey(msg.createdAt);
    if (dateKey !== lastDateKey) {
      result.push({ type: 'separator', date: msg.createdAt, key: `sep-${dateKey}` });
      lastDateKey = dateKey;
    }
    result.push({ type: 'message', msg, key: `msg-${msg.id}` });
  }

  return result;
});

const scrollToBottom = () => {
  const doScroll = (retries: number) => {
    if (!messageList.value) return;
    const el = messageList.value;
    el.scrollTop = el.scrollHeight;
    if (retries > 0) {
      setTimeout(() => doScroll(retries - 1), 100);
    }
  };
  doScroll(5);
};

const handleNewMessage = (msg: any) => {
  const chatInList = chatList.value.find(c => c.id === msg.chatId);
  if (chatInList) {
    chatInList.lastMessage = msg.messageType === 'IMAGE' ? '[图片]' : msg.messageType === 'VIDEO' ? '[视频]' : msg.content;
    chatInList.lastMessageTime = msg.createdAt;
    chatInList.lastMessageSenderId = msg.senderId;
    const idx = chatList.value.indexOf(chatInList);
    if (idx > 0) {
      chatList.value.splice(idx, 1);
      chatList.value.unshift(chatInList);
    }
  }

  const chatId = String(msg.chatId);
  if (messageCache.has(chatId)) {
    const cachedMessages = messageCache.get(chatId)!;
    const exists = cachedMessages.some(m => m.id === msg.id);
    if (!exists) {
      cachedMessages.push(msg);
    }
  }

  if (currentChat.value && msg.chatId === currentChat.value.id) {
    const exists = messages.value.some(m => m.id === msg.id);
    if (!exists) {
      messages.value = [...messages.value, msg];
      scrollToBottom();
    }
  }
};

let pollingTimer: ReturnType<typeof setInterval> | null = null;

/** 轮询获取最新消息（作为 WebSocket 的 fallback） */
const startPolling = () => {
  stopPolling();
  pollingTimer = setInterval(async () => {
    // 仅在 WebSocket 断开时使用轮询
    if (wsManager.isConnected()) return;
    if (!currentChat.value) return;

    try {
      const chatId = String(currentChat.value.id);
      const res = await chatApi.getMessages(chatId, { page: 0, size: 10 });
      let newMsgs: any[] = [];
      if (Array.isArray(res.data)) {
        newMsgs = res.data;
      } else if (res.data?.content && Array.isArray(res.data.content)) {
        newMsgs = res.data.content;
      }

      // 检查是否有新消息
      for (const msg of newMsgs) {
        if (!messages.value.some(m => m.id === msg.id)) {
          handleNewMessage(msg);
        }
      }
    } catch {
      // 静默失败
    }
  }, 5000); // 每5秒轮询一次
};

const stopPolling = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer);
    pollingTimer = null;
  }
};

onMounted(() => {
  // 订阅聊天消息
  wsManager.subscribe('chat', handleNewMessage);

  loadChatList();
  if (currentUserId.value) {
    wsManager.connect('', String(currentUserId.value)).catch((err) => {
      console.error('WebSocket连接失败:', err);
    });
  }

  // 启动轮询 fallback
  startPolling();
});

onUnmounted(() => {
  wsManager.unsubscribe('chat', handleNewMessage);
  wsManager.disconnect();
  stopPolling();
});
</script>

<style scoped src="../../styles/pages/chat.css"></style>
