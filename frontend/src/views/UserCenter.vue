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
          @click="activeTab = tab.id"
        >
          {{ tab.name }}
        </button>
      </div>

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

      <!-- Menu -->
      <div class="profile-section" style="margin-top: 24px;">
        <div class="profile-menu">
          <div class="profile-menu-item" @click="$router.push('/user/chat')">
            <div class="profile-menu-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z" />
              </svg>
            </div>
            <span class="profile-menu-text">消息通知</span>
            <span class="profile-menu-badge">3</span>
            <span class="profile-menu-arrow">›</span>
          </div>
          <div class="profile-menu-item" @click="$router.push('/user/history')">
            <div class="profile-menu-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4m7 14l5-5-5-5m5 5H9" />
              </svg>
            </div>
            <span class="profile-menu-text">浏览历史</span>
            <span class="profile-menu-arrow">›</span>
          </div>
          <div class="profile-menu-item" @click="$router.push('/user/profile')">
            <div class="profile-menu-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <circle cx="12" cy="12" r="3" />
                <path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 01-2.83 2.83l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-4 0v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83-2.83l.06-.06A1.65 1.65 0 004.68 15a1.65 1.65 0 00-1.51-1H3a2 2 0 010-4h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 012.83-2.83l.06.06A1.65 1.65 0 009 4.68a1.65 1.65 0 001-1.51V3a2 2 0 014 0v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 2.83l-.06.06A1.65 1.65 0 0019.4 9a1.65 1.65 0 001.51 1H21a2 2 0 010 4h-.09a1.65 1.65 0 00-1.51 1z" />
              </svg>
            </div>
            <span class="profile-menu-text">账号设置</span>
            <span class="profile-menu-arrow">›</span>
          </div>
          <div class="profile-menu-item" @click="$router.push('/help')">
            <div class="profile-menu-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <circle cx="12" cy="12" r="10" />
                <path d="M12 16v-4m0-4h.01" />
              </svg>
            </div>
            <span class="profile-menu-text">帮助与反馈</span>
            <span class="profile-menu-arrow">›</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { userStore } from '../store';
import api from '../api';

const store = userStore();
const userInfo = computed(() => store.user);
const activeTab = ref('my-items');

const tabs = [
  { id: 'my-items', name: '我的发布' },
  { id: 'sold', name: '已售出' },
  { id: 'favorites', name: '收藏夹' },
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
