<template>
  <div class="user-center-page">
    <div class="page-header">
      <div class="container">
        <div class="user-profile">
          <div class="profile-avatar">
            <el-avatar :size="80" :src="userInfo?.avatar">
              {{
                userInfo?.nickname?.charAt(0) ||
                userInfo?.username?.charAt(0) ||
                '用'
              }}
            </el-avatar>
            <span class="verified-badge" v-if="userInfo?.verified">
              <svg
                width="20"
                height="20"
                viewBox="0 0 24 24"
                fill="var(--secondary-color)"
              >
                <path
                  d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"
                />
              </svg>
            </span>
          </div>
          <div class="profile-info">
            <h1 class="profile-name">
              {{ userInfo?.nickname || userInfo?.username || '用户' }}
            </h1>
            <p class="profile-bio" v-if="userInfo?.bio">{{ userInfo.bio }}</p>
            <p class="profile-meta">
              <span v-if="userInfo?.schoolName"
                >@{{ userInfo.schoolName }}</span
              >
              <span
                class="meta-dot"
                v-if="userInfo?.schoolName && userInfo?.studentId"
                >·</span
              >
              <span v-if="userInfo?.studentId"
                >学号 {{ userInfo.studentId }}</span
              >
              <span class="meta-dot" v-if="userInfo?.gender">·</span>
              <span v-if="userInfo?.gender === 1">男</span>
              <span v-if="userInfo?.gender === 2">女</span>
            </p>
            <div class="profile-badges">
              <span class="badge badge-eco" v-if="userInfo?.verified">
                <svg
                  width="12"
                  height="12"
                  viewBox="0 0 24 24"
                  fill="currentColor"
                >
                  <path
                    d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z"
                  />
                </svg>
                已认证
              </span>
              <span class="badge badge-trading">
                {{ stats.completedDeals }} 交易完成
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="container">
      <div class="stats-grid">
        <div class="stat-card">
          <span class="stat-value">{{ stats.totalItems }}</span>
          <span class="stat-label">发布物品</span>
        </div>
        <div class="stat-card">
          <span class="stat-value">{{ stats.soldItems }}</span>
          <span class="stat-label">已售出</span>
        </div>
        <div class="stat-card">
          <span class="stat-value">{{ stats.completedDeals }}</span>
          <span class="stat-label">完成交易</span>
        </div>
        <div class="stat-card stat-card-highlight">
          <span class="stat-value">{{ stats.rating || '5.0' }}</span>
          <span class="stat-label">信用评分</span>
        </div>
      </div>

      <div class="user-content">
        <aside class="user-sidebar">
          <nav class="sidebar-nav">
            <router-link
              to="/user/profile"
              class="nav-item"
              active-class="active"
            >
              <svg
                width="20"
                height="20"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path
                  d="M20 21V19C20 16.7909 18.2091 15 16 15H8C5.79086 15 4 16.7909 4 19V21"
                />
                <circle cx="12" cy="7" r="4" />
              </svg>
              <span>个人资料</span>
            </router-link>
            <router-link
              to="/user/items"
              class="nav-item"
              active-class="active"
            >
              <svg
                width="20"
                height="20"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path d="M20 7L12 3L4 7" />
                <path d="M20 7L12 11L4 7" />
                <path d="M20 7V17L12 21L4 17V7" />
              </svg>
              <span>我的发布</span>
            </router-link>
            <router-link to="/orders" class="nav-item" active-class="active">
              <svg
                width="20"
                height="20"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path
                  d="M9 5H7C5.89543 5 5 5.89543 5 7V19C5 20.1046 5.89543 21 7 21H17C18.1046 21 19 20.1046 19 19V7C19 5.89543 18.1046 5 17 5H15"
                />
                <path
                  d="M9 5C9 3.89543 9.89543 3 11 3H13C14.1046 3 15 3.89543 15 5C15 6.10457 14.1046 7 13 7H11C9.89543 7 9 6.10457 9 5Z"
                />
              </svg>
              <span>我的订单</span>
            </router-link>
            <router-link
              to="/user/favorites"
              class="nav-item"
              active-class="active"
            >
              <svg
                width="20"
                height="20"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path
                  d="M20.84 4.61C20.3292 4.09924 19.7228 3.69397 19.0554 3.41708C18.3879 3.14019 17.6725 2.99756 16.95 2.99756C16.2275 2.99756 15.5121 3.14019 14.8446 3.41708C14.1772 3.69397 13.5708 4.09924 13.06 4.61L12 5.67L10.94 4.61C9.9083 3.57831 8.50903 2.99787 7.05 2.99787C5.59096 2.99787 4.19169 3.57831 3.16 4.61C2.1283 5.64169 1.54785 7.04097 1.54785 8.5C1.54785 9.95903 2.1283 11.3583 3.16 12.39L4.22 13.45L12 21.23L19.78 13.45L20.84 12.39C21.3508 11.8792 21.756 11.2728 22.0329 10.6054C22.3098 9.93789 22.4524 9.22248 22.4524 8.5C22.4524 7.77751 22.3098 7.0621 22.0329 6.39464C21.756 5.72718 21.3508 5.12075 20.84 4.61Z"
                />
              </svg>
              <span>我的收藏</span>
            </router-link>
            <router-link
              to="/user/verification"
              class="nav-item"
              active-class="active"
            >
              <svg
                width="20"
                height="20"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path
                  d="M9 11H7a2 2 0 0 0-2 2v8a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2v-8a2 2 0 0 0-2-2h-2"
                />
                <polyline points="9 22 9 11 15 11 15 22" />
                <path d="M12 7V3" />
                <path
                  d="M12 3a2 2 0 0 0-2 2v2a2 2 0 0 1-2 2 2 2 0 0 0-2 2v2a2 2 0 0 1-2 2 2 2 0 0 0-2 2v2a2 2 0 0 1-2 2"
                />
              </svg>
              <span>实名认证</span>
            </router-link>
            <router-link
              to="/user/settings"
              class="nav-item"
              active-class="active"
            >
              <svg
                width="20"
                height="20"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <circle cx="12" cy="12" r="3" />
                <path
                  d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"
                />
              </svg>
              <span>账号设置</span>
            </router-link>
          </nav>
        </aside>

        <main class="user-main">
          <router-view />
        </main>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { userStore } from '../store';

const store = userStore();

const userInfo = computed(() => store.user);

const stats = reactive({
  totalItems: 0,
  soldItems: 0,
  completedDeals: 0,
  rating: '100',
});

onMounted(() => {
  loadStats();
});

const loadStats = async () => {
  try {
    const response = await fetch('/api/users/stats');
    if (response.ok) {
      const data = await response.json();
      if (data.code === 200) {
        stats.totalItems = data.data.totalItems || data.data.total_sales || 0;
        stats.soldItems = data.data.soldItems || data.data.total_sales || 0;
        stats.completedDeals =
          data.data.completedDeals || data.data.total_transactions || 0;
        stats.rating = data.data.rating || data.data.credit_score || '100';
      }
    }
  } catch (error) {
    console.error('获取统计数据失败', error);
  }
};
</script>

<style scoped src="../styles/pages/user-center.css"></style>
