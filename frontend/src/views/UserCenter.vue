<template>
  <div class="user-center-page">
    <div class="container">
      <!-- Profile Header -->
      <div class="profile-header">
        <div class="profile-avatar">
          <img v-if="userInfo?.avatar" :src="userInfo.avatar" alt="头像" class="avatar-img" />
          <span v-else>{{ userInfo?.nickname?.charAt(0) || '陈' }}</span>
        </div>
        <div class="profile-user-info">
          <div class="profile-name">{{ userInfo?.nickname || '陈同学' }}</div>
          <div class="profile-school">{{ userInfo?.school || '计算机科学与技术学院 · 大三' }}</div>
          <div class="profile-bio">{{ userInfo?.bio || '热爱环保的码农，闲置物品换新主人 ♻️' }}</div>
        </div>
        <a v-if="store.isAdmin" href="/admin" class="admin-btn">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
            <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" />
            <path d="M12 8V12" />
            <path d="M12 16H12.01" />
          </svg>
          管理后台
        </a>
      </div>

      <!-- Stats -->
      <div class="profile-stats">
        <div class="profile-stat">
          <div class="profile-stat-num">{{ stats.totalItems }}</div>
          <div class="profile-stat-label">发布</div>
        </div>
        <div class="profile-stat">
          <div class="profile-stat-num">{{ stats.soldItems }}</div>
          <div class="profile-stat-label">已售</div>
        </div>
        <div class="profile-stat">
          <div class="profile-stat-num">{{ stats.favorites }}</div>
          <div class="profile-stat-label">收藏</div>
        </div>
        <div class="profile-stat">
          <div class="profile-stat-num">{{ stats.rating }}</div>
          <div class="profile-stat-label">评分</div>
        </div>
      </div>

      <!-- Profile Tabs -->
      <div class="profile-tabs">
        <button
          v-for="tab in tabs"
          :key="tab.id"
          class="profile-tab"
          :class="{ active: activeTab === tab.id }"
          @click="activeTab = tab.id"
        >
          {{ tab.name }}
        </button>
      </div>

      <!-- Tab Content: 个人信息 -->
      <div v-if="activeTab === 'profile'" class="tab-content">
        <Profile @change-tab="activeTab = $event" />
      </div>

      <!-- Tab Content: 实名认证 -->
      <div v-else-if="activeTab === 'verification'" class="tab-content">
        <Verification />
      </div>

      <!-- Tab Content: 我的发布 -->
      <div v-else-if="activeTab === 'my-items'" class="tab-content">
        <UserItems />
      </div>

      <!-- Tab Content: 我的订单 -->
      <div v-else-if="activeTab === 'orders'" class="tab-content">
        <OrderList />
      </div>

      <!-- Tab Content: 收藏夹 -->
      <div v-else-if="activeTab === 'favorites'" class="tab-content">
        <Favorites />
      </div>

      <!-- Tab Content: 消息中心 -->
      <div v-else-if="activeTab === 'chat'" class="tab-content">
        <Chat />
      </div>

      <!-- Tab Content: 消息通知 -->
      <div v-else-if="activeTab === 'notifications'" class="tab-content">
        <Notifications />
      </div>

      <!-- Tab Content: 修改密码 -->
      <div v-else-if="activeTab === 'change-password'" class="tab-content">
        <ChangePassword />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { userStore } from '../store';
import api from '../api';

// Import child components
import Profile from './user/Profile.vue';
import Verification from './user/Verification.vue';
import UserItems from './user/Items.vue';
import OrderList from './OrderList.vue';
import Favorites from './user/Favorites.vue';
import Chat from './user/Chat.vue';
import Notifications from './user/Notifications.vue';
import ChangePassword from './user/ChangePassword.vue';

const store = userStore();
const userInfo = computed(() => store.user);
const activeTab = ref('profile');

const tabs = [
  { id: 'profile', name: '个人信息' },
  { id: 'verification', name: '实名认证' },
  { id: 'my-items', name: '我的发布' },
  { id: 'orders', name: '我的订单' },
  { id: 'favorites', name: '收藏夹' },
  { id: 'chat', name: '消息中心' },
  { id: 'notifications', name: '消息通知' },
];

const stats = reactive({
  totalItems: 12,
  soldItems: 8,
  favorites: 24,
  rating: 4.9,
});

onMounted(async () => {
  try {
    const res = await api.user.getStats();
    if (res.code === 200) {
      stats.totalItems = res.data.totalItems ?? 12;
      stats.soldItems = res.data.soldItems ?? 8;
      stats.favorites = res.data.favorites ?? 24;
      stats.rating = res.data.rating ?? 4.9;
    }
  } catch (error) {
    console.error('获取统计数据失败', error);
  }
});
</script>

<style scoped src="../styles/pages/user-center.css"></style>
