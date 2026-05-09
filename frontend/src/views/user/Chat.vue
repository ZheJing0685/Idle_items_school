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
              <img :src="getOtherUser(chat).avatar || '/default-avatar.png'" alt="" />
            </div>
            <div class="chat-info">
              <div class="chat-name">{{ getOtherUser(chat).nickname || getOtherUser(chat).username }}</div>
              <div class="chat-last-message">{{ chat.lastMessage || '暂无消息' }}</div>
            </div>
            <div class="chat-time">{{ formatTime(chat.updatedAt) }}</div>
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
                <img :src="getMessageAvatar(msg)" alt="" />
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
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/>
            </svg>
            <p>选择一个会话开始聊天</p>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/store';
import { wsService } from '@/utils/websocket';
import chatApi from '@/api/services/chat';

const userStore = useUserStore();
const currentUserId = ref(userStore.user?.id);

const chatList = ref([]);
const currentChat = ref(null);
const messages = ref([]);
const newMessage = ref('');
const messageList = ref(null);

const loadChatList = async () => {
  try {
    const res = await chatApi.getChats({ page: 1, size: 50 });
    chatList.value = res.data.content || [];
  } catch (error) {
    console.error('加载聊天列表失败:', error);
  }
};

const selectChat = async (chat) => {
  currentChat.value = chat;
  await loadMessages(chat.id);
};

const loadMessages = async (chatId) => {
  try {
    const res = await chatApi.getMessages(chatId, { page: 1, size: 50 });
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
    await chatApi.sendMessage(currentChat.value.id, otherUser.id, content);
    newMessage.value = '';
    await loadMessages(currentChat.value.id);
  } catch (error) {
    ElMessage.error('发送消息失败');
  }
};

const getOtherUser = (chat) => {
  if (!chat) return {};
  return chat.buyerId === currentUserId.value
    ? { id: chat.sellerId, nickname: chat.sellerNickname, username: chat.sellerUsername }
    : { id: chat.buyerId, nickname: chat.buyerNickname, username: chat.buyerUsername };
};

const getMessageAvatar = (msg) => {
  return msg.senderId === currentUserId.value ? userStore.user?.avatar : '/default-avatar.png';
};

const formatTime = (time) => {
  if (!time) return '';
  const date = new Date(time);
  const now = new Date();
  const diff = now - date;
  if (diff < 60000) return '刚刚';
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前';
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前';
  return date.toLocaleDateString();
};

const formatMessageTime = (time) => {
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

const handleNewMessage = (msg) => {
  if (currentChat.value && msg.chatId === currentChat.value.id) {
    messages.value.push(msg);
    scrollToBottom();
  }
};

onMounted(() => {
  loadChatList();
  if (currentUserId.value) {
    const token = document.cookie.match(/user_token=([^;]+)/)?.[1];
    if (token) {
      wsService.connect(token, currentUserId.value);
      wsService.onMessage('chat', handleNewMessage);
    }
  }
});

onUnmounted(() => {
  wsService.disconnect();
});
</script>

<style scoped src="../../styles/pages/chat.css"></style>
