<template>
  <div class="user-center-page">
    <div class="container">
      <!-- Profile Header -->
      <div class="profile-header">
        <div class="profile-avatar">{{ userInfo?.nickname?.charAt(0) || '陈' }}</div>
        <div class="profile-user-info">
          <div class="profile-name">{{ userInfo?.nickname || '陈同学' }}</div>
          <div class="profile-school">{{ userInfo?.school || '计算机科学与技术学院 · 大三' }}</div>
          <div class="profile-bio">{{ userInfo?.bio || '热爱环保的码农，闲置物品换新主人 ♻️' }}</div>
        </div>
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
          @click="handleTabClick(tab)"
        >
          {{ tab.name }}
        </button>
      </div>

      <!-- Tab Content or Router View -->
      <router-view v-if="isSubRoute" />
      <div v-else>
        <!-- Tab Content -->
        <div class="items-grid" v-if="activeTab === 'my-items'">
          <div
            v-for="(item, index) in myItems"
            :key="item.id"
            class="card card-clickable item-card"
            @click="$router.push(`/item/${item.id}`)"
          >
            <div class="item-card-img">
              <div class="img-placeholder" :style="{ background: getItemColor(index) }">
                📦
              </div>
            </div>
            <div class="item-card-body">
              <div class="item-card-title">{{ item.title }}</div>
              <div class="item-card-meta">
                <div class="item-card-price">
                  <span class="unit">¥</span>{{ item.price?.toLocaleString() }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="items-grid" v-else-if="activeTab === 'sold'">
          <div
            v-for="(item, index) in soldItems"
            :key="item.id"
            class="card card-clickable item-card"
            @click="$router.push(`/item/${item.id}`)"
          >
            <div class="item-card-img">
              <div class="img-placeholder" :style="{ background: getItemColor(index) }">
                📦
              </div>
            </div>
            <div class="item-card-body">
              <div class="item-card-title">{{ item.title }}</div>
              <div class="item-card-meta">
                <div class="item-card-price">
                  <span class="unit">¥</span>{{ item.price?.toLocaleString() }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="items-grid" v-else-if="activeTab === 'favorites'">
          <div
            v-for="(item, index) in favoriteItems"
            :key="item.id"
            class="card card-clickable item-card"
            @click="$router.push(`/item/${item.id}`)"
          >
            <div class="item-card-img">
              <div class="img-placeholder" :style="{ background: getItemColor(index) }">
                📦
              </div>
            </div>
            <div class="item-card-body">
              <div class="item-card-title">{{ item.title }}</div>
              <div class="item-card-meta">
                <div class="item-card-price">
                  <span class="unit">¥</span>{{ item.price?.toLocaleString() }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Menu (only show when not on sub-route) -->
      <div v-if="!isSubRoute" class="profile-section" style="margin-top: 24px;">
        <div class="profile-menu">
          <div class="profile-menu-item" @click="$router.push('/user/profile')">
            <div class="profile-menu-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <path d="M20 21V19C20 16.7909 18.2091 15 16 15H8C5.79086 15 4 16.7909 4 19V21" />
                <circle cx="12" cy="7" r="4" />
              </svg>
            </div>
            <span class="profile-menu-text">个人中心</span>
            <span class="profile-menu-arrow">›</span>
          </div>
          <div class="profile-menu-item" @click="$router.push('/user/items')">
            <div class="profile-menu-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <path d="M20 7L12 3L4 7" />
                <path d="M20 7L12 11L4 7" />
                <path d="M20 7V17L12 21L4 17V7" />
              </svg>
            </div>
            <span class="profile-menu-text">我的发布</span>
            <span class="profile-menu-arrow">›</span>
          </div>
          <div class="profile-menu-item" @click="$router.push('/user/orders')">
            <div class="profile-menu-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <path d="M6 2L3 6V20C3 20.5304 3.21071 21.0391 3.58579 21.4142C3.96086 21.7893 4.46957 22 5 22H19C19.5304 22 20.0391 21.7893 20.4142 21.4142C20.7893 21.0391 21 20.5304 21 20V6L18 2H6Z" />
                <path d="M3 6H21" />
              </svg>
            </div>
            <span class="profile-menu-text">我的订单</span>
            <span class="profile-menu-arrow">›</span>
          </div>
          <div class="profile-menu-item" @click="$router.push('/user/favorites')">
            <div class="profile-menu-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <path d="M20.84 4.61C20.3292 4.09924 19.7228 3.69397 19.0554 3.41708C18.3879 3.14019 17.6725 2.99756 16.95 2.99756C16.2275 2.99756 15.5121 3.14019 14.8446 3.41708C14.1772 3.69397 13.5708 4.09924 13.06 4.61L12 5.67L10.94 4.61C9.9083 3.57831 8.50903 2.99787 7.05 2.99787C5.59096 2.99787 4.19169 3.57831 3.16 4.61C2.1283 5.64169 1.54785 7.04097 1.54785 8.5C1.54785 9.95903 2.1283 11.3583 3.16 12.39L4.22 13.45L12 21.23L19.78 13.45L20.84 12.39C21.3508 11.8792 21.756 11.2728 22.0329 10.6054C22.3098 9.93789 22.4524 9.22248 22.4524 8.5C22.4524 7.77751 22.3098 7.0621 22.0329 6.39464C21.756 5.72718 21.3508 5.12075 20.84 4.61Z" />
              </svg>
            </div>
            <span class="profile-menu-text">我的收藏</span>
            <span class="profile-menu-arrow">›</span>
          </div>
          <div class="profile-menu-item" @click="$router.push('/user/chat')">
            <div class="profile-menu-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <path d="M21 15C21 15.5304 20.7893 16.0391 20.4142 16.4142C20.0391 16.7893 19.5304 17 19 17H7L3 21V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H16C16.5304 3 17.0391 3.21071 17.4142 3.58579C17.7893 3.96086 18 4.46957 18 5" />
              </svg>
            </div>
            <span class="profile-menu-text">消息中心</span>
            <span class="profile-menu-badge">3</span>
            <span class="profile-menu-arrow">›</span>
          </div>
          <div class="profile-menu-item" @click="$router.push('/user/notifications')">
            <div class="profile-menu-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
                <path d="M13.73 21a2 2 0 0 1-3.46 0" />
              </svg>
            </div>
            <span class="profile-menu-text">消息通知</span>
            <span class="profile-menu-arrow">›</span>
          </div>
          <div class="profile-menu-item" @click="$router.push('/user/change-password')">
            <div class="profile-menu-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" />
                <path d="M12 8V12" />
                <path d="M12 16H12.01" />
              </svg>
            </div>
            <span class="profile-menu-text">修改密码</span>
            <span class="profile-menu-arrow">›</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { userStore } from '../store';
import api from '../api';

const route = useRoute();
const router = useRouter();
const store = userStore();
const userInfo = computed(() => store.user);
const activeTab = ref('my-items');

// Check if we're on a sub-route
const isSubRoute = computed(() => {
  const path = route.path;
  return path !== '/user' && path.startsWith('/user/');
});

const tabs = [
  { id: 'my-items', name: '我的发布', route: '/user/items' },
  { id: 'sold', name: '已售出', route: '/user/orders' },
  { id: 'favorites', name: '收藏夹', route: '/user/favorites' },
];

const stats = reactive({
  totalItems: 12,
  soldItems: 8,
  favorites: 24,
  rating: 4.9,
});

const myItems = ref<any[]>([]);
const soldItems = ref<any[]>([]);
const favoriteItems = ref<any[]>([]);

const itemColors = ['#dce8f7', '#f5edd6', '#d8f0e0', '#e8d8f0', '#f0e0d0'];
const getItemColor = (index: number) => itemColors[index % itemColors.length];

const handleTabClick = (tab: any) => {
  activeTab.value = tab.id;
  router.push(tab.route);
};

// Watch route changes to update active tab
watch(() => route.path, (path) => {
  if (path === '/user/items') activeTab.value = 'my-items';
  else if (path === '/user/orders') activeTab.value = 'sold';
  else if (path === '/user/favorites') activeTab.value = 'favorites';
}, { immediate: true });

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
