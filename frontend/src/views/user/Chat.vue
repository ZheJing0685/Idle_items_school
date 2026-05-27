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
              <img v-if="getOtherUser(chat).avatar" :src="getOtherUser(chat).avatar" alt="" />
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

          <div class="message-list" ref="messageList">
            <div
              v-for="msg in messages"
              :key="msg.id"
              class="message-item"
              :class="{ 'is-mine': msg.senderId === currentUserId }"
            >
              <div class="message-avatar">
                <img v-if="getMessageAvatar(msg)" :src="getMessageAvatar(msg)" alt="" />
                <el-avatar v-else :size="40">{{ msg.senderId === currentUserId ? (userStore.user?.nickname?.charAt(0) || userStore.user?.username?.charAt(0) || '我') : (getOtherUser(currentChat)?.nickname?.charAt(0) || getOtherUser(currentChat)?.username?.charAt(0) || '对') }}</el-avatar>
              </div>
              <div class="message-content">
                <div class="message-text">{{ msg.content }}</div>
                <div class="message-time">{{ formatMessageTime(msg.createdAt) }}</div>
              </div>
            </div>
          </div>

          <div class="message-input">
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
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { MessageSquare } from 'lucide-vue-next';
import { useUserStore } from '@/store';
import { wsService } from '@/utils/websocket';
import { getToken } from '@/api/config/axios';
import chatApi from '@/api/services/chat';

const route = useRoute();
const userStore = useUserStore();
const currentUserId = ref(userStore.user?.id);

const chatList = ref<any[]>([]);
const currentChat = ref<any>(null);
const messages = ref<any[]>([]);
const newMessage = ref('');
const messageList = ref<HTMLDivElement | null>(null);

const loadChatList = async () => {
  try {
    console.log('Loading chat list...');
    const res = await chatApi.getChats();
    console.log('Chat list response:', res);

    // 后端返回格式: {code: 200, data: [...]} 或 {code: 200, data: {content: [...]}}
    let chatData = [];

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
  currentChat.value = chat;
  await loadMessages(chat.id);
};

const loadMessages = async (chatId: string) => {
  try {
    const res = await chatApi.getMessages(chatId, { page: 0, size: 50 });
    messages.value = res.data.content || [];
    scrollToBottom();
  } catch (error) {
    console.error('加载消息失败:', error);
  }
};

const sendMessage = async () => {
  if (!newMessage.value.trim() || !currentChat.value) return;
  const otherUser = getOtherUser(currentChat.value);
  const content = newMessage.value.trim();
  try {
    await chatApi.sendMessage(currentChat.value.id, String(otherUser.id), content);
    newMessage.value = '';
    await loadMessages(currentChat.value.id);
  } catch (error) {
    ElMessage.error('发送消息失败');
  }
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
  // 支持epoch毫秒(Long)和ISO字符串
  const date = typeof time === 'number' ? new Date(time) : new Date(time);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  if (diff < 0) return '刚刚';
  if (diff < 60000) return '刚刚';
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前';
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前';
  return date.toLocaleDateString('zh-CN');
};

const formatMessageTime = (time: string) => {
  if (!time) return '';
  const date = new Date(time);
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
};

const scrollToBottom = () => {
  nextTick(() => {
    if (messageList.value) {
      messageList.value.scrollTop = messageList.value.scrollHeight;
    }
  });
};

const handleNewMessage = (msg: any) => {
  if (currentChat.value && msg.chatId === currentChat.value.id) {
    // 避免重复添加消息
    const exists = messages.value.some(m => m.id === msg.id);
    if (!exists) {
      messages.value.push(msg);
      scrollToBottom();
    }
  }
};

onMounted(() => {
  // 先注册消息处理器
  wsService.onMessage('chat', handleNewMessage);

  loadChatList();
  if (currentUserId.value) {
    const token = getToken();
    if (token) {
      wsService.connect(token, String(currentUserId.value)).catch((err) => {
        console.error('WebSocket连接失败:', err);
      });
    }
  }
});

onUnmounted(() => {
  wsService.disconnect();
});
</script>

<style scoped src="../../styles/pages/chat.css"></style>
