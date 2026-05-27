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

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { userStore } from '../store';
import api from '../api';
import { userMenuConfig } from '../config/navigation';
import Sidebar from '../components/user/Sidebar.vue';
import UserInfoCard from '../components/user/UserInfoCard.vue';
import StatsCard from '../components/user/StatsCard.vue';
import QuickActions from '../components/user/QuickActions.vue';

const route = useRoute();
const store = userStore();
const userInfo = computed<any>(() => store.user);
const sidebarCollapsed = ref(false);

const stats = reactive({
  totalItems: 0,
  soldItems: 0,
  completedDeals: 0,
  rating: 100,
});

const statsData = computed(() => [
  { value: stats.totalItems, label: '发布' },
  { value: stats.soldItems, label: '已售' },
  { value: stats.completedDeals, label: '成交' },
  { value: stats.rating, label: '信用分', accent: true }
]);

const menuItems = [
  ...userMenuConfig.items.map(item => ({
    name: item.name,
    path: item.path,
    icon: item.icon,
  })),
  {
    name: '实名认证',
    path: '/user/verification',
    icon: 'check',
  },
];

const quickActions = [
  {
    name: '发布闲置',
    path: '/publish',
    icon: 'plus',
  },
  {
    name: '查看订单',
    path: '/user/orders',
    icon: 'shopping-bag',
  },
  {
    name: '消息中心',
    path: '/user/chat',
    icon: 'message',
  },
];

onMounted(() => {
  loadStats();
});

const loadStats = async () => {
  try {
    const res = await api.user.getStats();
    if (res.code === 200) {
      stats.totalItems = res.data.totalItems ?? 0;
      stats.soldItems = res.data.soldItems ?? 0;
      stats.completedDeals = res.data.completedDeals ?? 0;
      stats.rating = res.data.rating ?? 100;
    }
  } catch (error) {
    console.error('获取统计数据失败', error);
  }
};
</script>

<style scoped src="../styles/pages/user-center.css"></style>
