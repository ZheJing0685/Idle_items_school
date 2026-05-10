<template>
  <div class="user-center-page">
    <Sidebar
      :collapsed="sidebarCollapsed"
      :menuItems="menuItems"
      @toggle="sidebarCollapsed = !sidebarCollapsed"
    />
    
    <main class="main-content">
      <div class="content-wrapper">
        <UserInfoCard :user="userInfo" />
        <StatsCard :stats="statsData" />
        <QuickActions :actions="quickActions" />
        
        <div class="router-view-container">
          <router-view />
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { userStore } from '../store';
import api from '../api';
import Sidebar from '../components/user/Sidebar.vue';
import UserInfoCard from '../components/user/UserInfoCard.vue';
import StatsCard from '../components/user/StatsCard.vue';
import QuickActions from '../components/user/QuickActions.vue';

const route = useRoute();
const store = userStore();
const userInfo = computed(() => store.user);
const sidebarCollapsed = ref(false);

const stats = reactive({
  totalItems: 0,
  soldItems: 0,
  completedDeals: 0,
  rating: '100',
});

const statsData = computed(() => [
  { value: stats.totalItems, label: '发布' },
  { value: stats.soldItems, label: '已售' },
  { value: stats.completedDeals, label: '成交' },
  { value: stats.rating, label: '信用分', accent: true }
]);

const menuItems = [
  { 
    name: '个人资料', 
    path: '/user/profile',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21V19C20 16.7909 18.2091 15 16 15H8C5.79086 15 4 16.7909 4 19V21"/><circle cx="12" cy="7" r="4"/></svg>'
  },
  { 
    name: '我的发布', 
    path: '/user/items',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 7L12 3L4 7M20 7L12 11L4 7M20 7V17L12 21L4 17V7"/></svg>'
  },
  { 
    name: '我的订单', 
    path: '/user/orders',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 2L3 6V20C3 20.5304 3.21071 21.0391 3.58579 21.4142C3.96086 21.7893 4.46957 22 5 22H19C19.5304 22 20.0391 21.7893 20.4142 21.4142C20.7893 21.0391 21 20.5304 21 20V6L18 2H6Z"/><path d="M3 6H21"/></svg>'
  },
  { 
    name: '我的收藏', 
    path: '/user/favorites',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"/></svg>'
  },
  { 
    name: '实名认证', 
    path: '/user/verification',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z"/><path d="M9 12L11 14L15 10"/></svg>'
  },
  { 
    name: '消息通知', 
    path: '/user/notifications',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 01-3.46 0"/></svg>'
  },
  { 
    name: '消息中心', 
    path: '/user/chat',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/></svg>'
  }
];

const quickActions = [
  {
    name: '发布闲置',
    path: '/publish',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>'
  },
  {
    name: '查看订单',
    path: '/user/orders',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 2L3 6V20C3 20.5304 3.21071 21.0391 3.58579 21.4142C3.96086 21.7893 4.46957 22 5 22H19C19.5304 22 20.0391 21.7893 20.4142 21.4142C20.7893 21.0391 21 20.5304 21 20V6L18 2H6Z"/><path d="M3 6H21"/></svg>'
  },
  {
    name: '消息中心',
    path: '/user/chat',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/></svg>'
  }
];

onMounted(() => {
  loadStats();
});

const loadStats = async () => {
  try {
    const res = await api.user.getStats();
    if (res.code === 200) {
      stats.totalItems = res.data.totalItems || 0;
      stats.soldItems = res.data.soldItems || 0;
      stats.completedDeals = res.data.completedDeals || 0;
      stats.rating = res.data.rating ?? '100';
    }
  } catch (error) {
    console.error('获取统计数据失败', error);
  }
};
</script>

<style scoped src="../styles/pages/user-center.css"></style>
