<template>
  <div class="admin-shell">
    <aside class="admin-sidebar" :class="{ 'is-collapsed': isCollapsed }">
      <div class="sidebar-brand">
        <div class="brand-mark">
          <svg viewBox="0 0 32 32" fill="none">
            <rect width="32" height="32" rx="8" fill="currentColor" />
            <path
              d="M8 12h16M8 16h12M8 20h8"
              stroke="white"
              stroke-width="2"
              stroke-linecap="round"
            />
          </svg>
        </div>
        <transition name="label-fade">
          <div v-if="!isCollapsed" class="brand-text">
            <span class="brand-name">闲置物品平台</span>
            <span class="brand-tagline">管理后台</span>
          </div>
        </transition>
        <button
          class="collapse-btn"
          @click="isCollapsed = !isCollapsed"
          :aria-label="isCollapsed ? '展开侧边栏' : '收起侧边栏'"
        >
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <path v-if="!isCollapsed" d="M15 18l-6-6 6-6" />
            <path v-else d="M9 18l6-6-6-6" />
          </svg>
        </button>
      </div>

      <nav class="sidebar-nav">
        <div class="nav-group">
          <span v-if="!isCollapsed" class="nav-label">主要导航</span>
          <router-link
            to="/admin"
            class="nav-item"
            exact-active-class="is-active"
          >
            <svg
              class="nav-icon"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
            >
              <rect x="3" y="3" width="7" height="9" rx="1" />
              <rect x="14" y="3" width="7" height="5" rx="1" />
              <rect x="14" y="12" width="7" height="9" rx="1" />
              <rect x="3" y="16" width="7" height="5" rx="1" />
            </svg>
            <span v-if="!isCollapsed" class="nav-text">控制台</span>
          </router-link>
        </div>

        <div class="nav-group">
          <span v-if="!isCollapsed" class="nav-label">用户与内容</span>
          <router-link
            to="/admin/users"
            class="nav-item"
            active-class="is-active"
          >
            <svg
              class="nav-icon"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
            >
              <circle cx="12" cy="8" r="4" />
              <path d="M4 21v-2a4 4 0 014-4h8a4 4 0 014 4v2" />
            </svg>
            <span v-if="!isCollapsed" class="nav-text">用户管理</span>
          </router-link>
          <router-link
            to="/admin/verification"
            class="nav-item"
            active-class="is-active"
          >
            <svg
              class="nav-icon"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
            >
              <path d="M9 12l2 2 4-4" />
              <circle cx="12" cy="12" r="9" />
            </svg>
            <span v-if="!isCollapsed" class="nav-text">实名认证</span>
          </router-link>
          <router-link
            to="/admin/items"
            class="nav-item"
            active-class="is-active"
          >
            <svg
              class="nav-icon"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
            >
              <path
                d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"
              />
            </svg>
            <span v-if="!isCollapsed" class="nav-text">物品管理</span>
          </router-link>
          <router-link
            to="/admin/categories"
            class="nav-item"
            active-class="is-active"
          >
            <svg
              class="nav-icon"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
            >
              <path d="M4 6h16M4 12h16M4 18h16" />
            </svg>
            <span v-if="!isCollapsed" class="nav-text">分类管理</span>
          </router-link>
        </div>

        <div class="nav-group">
          <span v-if="!isCollapsed" class="nav-label">交易与数据</span>
          <router-link
            to="/admin/orders"
            class="nav-item"
            active-class="is-active"
          >
            <svg
              class="nav-icon"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
            >
              <rect x="3" y="4" width="18" height="18" rx="2" />
              <path d="M7 8h10M7 12h10M7 16h6" />
            </svg>
            <span v-if="!isCollapsed" class="nav-text">订单管理</span>
          </router-link>
          <router-link
            to="/admin/statistics"
            class="nav-item"
            active-class="is-active"
          >
            <svg
              class="nav-icon"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
            >
              <path d="M3 3v18h18" />
              <path d="M7 14l4-4 4 4 5-5" />
            </svg>
            <span v-if="!isCollapsed" class="nav-text">数据统计</span>
          </router-link>
        </div>

        <div class="nav-group">
          <span v-if="!isCollapsed" class="nav-label">系统</span>
          <router-link
            to="/admin/logs"
            class="nav-item"
            active-class="is-active"
          >
            <svg
              class="nav-icon"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
            >
              <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z" />
              <path d="M14 2v6h6M16 13H8M16 17H8M10 9H8" />
            </svg>
            <span v-if="!isCollapsed" class="nav-text">操作日志</span>
          </router-link>
        </div>
      </nav>

      <div class="sidebar-footer">
        <div class="user-card" v-if="!isCollapsed">
          <div class="user-avatar">
            {{ userStore.user?.nickname?.charAt(0) || '管' }}
          </div>
          <div class="user-info">
            <span class="user-name">{{
              userStore.user?.nickname || '管理员'
            }}</span>
            <span class="user-role">{{ getRoleText() }}</span>
          </div>
        </div>
        <button class="logout-btn" @click="handleLogout">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.5"
          >
            <path
              d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4M16 17l5-5-5-5M21 12H9"
            />
          </svg>
          <span v-if="!isCollapsed">退出登录</span>
        </button>
      </div>
    </aside>

    <div class="admin-main">
      <header class="admin-header">
        <div class="header-left">
          <h1 class="page-title">{{ currentPageTitle }}</h1>
        </div>
        <div class="header-right">
          <div class="header-search">
            <svg
              class="search-icon"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <circle cx="11" cy="11" r="8" />
              <path d="M21 21l-4.35-4.35" />
            </svg>
            <input type="text" placeholder="搜索功能..." class="search-input" />
          </div>
          <button class="header-action" aria-label="通知">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
            >
              <path
                d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9M13.73 21a2 2 0 01-3.46 0"
              />
            </svg>
            <span class="notification-badge">3</span>
          </button>
        </div>
      </header>

      <main class="admin-content">
        <router-view v-slot="{ Component }">
          <transition name="page-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { userStore } from '../../store';

const route = useRoute();
const router = useRouter();
const isCollapsed = ref(false);
const userStoreInstance = userStore();

const pageTitles = {
  '/admin': '控制台',
  '/admin/users': '用户管理',
  '/admin/verification': '实名认证',
  '/admin/items': '物品管理',
  '/admin/categories': '分类管理',
  '/admin/orders': '订单管理',
  '/admin/statistics': '数据统计',
  '/admin/logs': '操作日志',
};

const currentPageTitle = computed(() => {
  return pageTitles[route.path] || '管理后台';
});

const getRoleText = () => {
  if (userStoreInstance.user?.role) {
    const roleMap = {
      ADMIN: '超级管理员',
      admin: '管理员',
      ROLE_ADMIN: '系统管理员',
    };
    return roleMap[userStoreInstance.user.role] || '管理员';
  }
  return '管理员';
};

const handleLogout = () => {
  userStoreInstance.logout();
  router.push('/login');
  ElMessage.success('已安全退出');
};

const handleResize = () => {
  if (window.innerWidth < 1024) {
    isCollapsed.value = true;
  }
};

onMounted(() => {
  window.addEventListener('resize', handleResize);
  handleResize();
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
});
</script>

<style scoped src="../../styles/pages/admin.css"></style>
