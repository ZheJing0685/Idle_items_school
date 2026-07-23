<template>
  <div class="user-center-page">
    <div class="container">
      <BreadcrumbNav />
      <!-- Profile Header -->
      <div class="profile-header">
        <div class="profile-avatar">
          <img v-if="userInfo?.avatar" :src="userInfo.avatar" alt="头像" class="avatar-img" loading="lazy" />
          <span v-else>{{ userInfo?.nickname?.charAt(0) || '陈' }}</span>
        </div>
        <div class="profile-user-info">
          <div class="profile-name">{{ userInfo?.nickname || '陈同学' }}</div>
          <div class="profile-school">{{ [userInfo?.schoolName, userInfo?.department].filter(Boolean).join(' · ') || userInfo?.school || '未知学校' }}{{ userInfo?.grade ? ' · ' + userInfo.grade : '' }}</div>
          <div class="profile-bio">{{ userInfo?.bio || '热爱环保的码农，闲置物品换新主人 ♻️' }}</div>
        </div>
        <router-link v-if="store.isAdmin" to="/admin" class="admin-btn">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
            <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" />
            <path d="M12 8V12" />
            <path d="M12 16H12.01" />
          </svg>
          管理后台
        </router-link>
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
      <div class="profile-tabs" role="tablist" aria-label="个人中心导航">
        <router-link
          v-for="tab in tabs"
          :key="tab.id"
          :to="`/user/${tab.route}`"
          class="profile-tab"
          :class="{ active: route.path === `/user/${tab.route}` }"
          role="tab"
          :aria-selected="route.path === `/user/${tab.route}`"
        >
          {{ tab.name }}
        </router-link>
      </div>

      <!-- Tab Content via Router -->
      <div class="tab-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { userStore } from '../store';
import api from '../api';

// Import child components
import BreadcrumbNav from '../components/common/BreadcrumbNav.vue';

const store = userStore();
const route = useRoute();
const userInfo = computed(() => store.user);

const tabs = [
  { id: 'profile', name: '个人信息', route: 'profile' },
  { id: 'verification', name: '实名认证', route: 'verification' },
  { id: 'my-items', name: '我的发布', route: 'items' },
  { id: 'orders', name: '我的订单', route: 'orders' },
  { id: 'favorites', name: '收藏夹', route: 'favorites' },
  { id: 'chat', name: '消息中心', route: 'chat' },
  { id: 'notifications', name: '消息通知', route: 'notifications' },
  { id: 'change-password', name: '修改密码', route: 'change-password' },
];

const stats = reactive({
  totalItems: 0,
  soldItems: 0,
  favorites: 0,
  rating: 0,
});
const statsLoading = ref(true);

onMounted(async () => {
  statsLoading.value = true;
  try {
    const res = await api.user.getStats();
    if (res.code === 200) {
      stats.totalItems = res.data.totalItems ?? 0;
      stats.soldItems = res.data.soldItems ?? 0;
      stats.favorites = res.data.favorites ?? 0;
      stats.rating = res.data.rating ?? 0;
    }
  } catch (error: unknown) {
    logger.error('获取统计数据失败', error);
  } finally {
    statsLoading.value = false;
  }
});
</script>

<style scoped src="../styles/pages/user-center.css"></style>
