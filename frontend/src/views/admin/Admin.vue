<template>
  <div class="admin-shell">
    <aside class="admin-sidebar" :class="{ 'is-collapsed': isCollapsed }">
      <div class="sidebar-brand">
        <div
          class="brand-mark"
          @click="isCollapsed = !isCollapsed"
          :class="{ clickable: true }"
        >
          <LayoutDashboard :size="32" />
        </div>
        <transition name="label-fade">
          <div v-if="!isCollapsed" class="brand-text">
            <span class="brand-name">闲置物品平台</span>
            <span class="brand-tagline">管理后台</span>
          </div>
        </transition>
      </div>

      <nav class="sidebar-nav">
        <div class="nav-group">
          <span v-if="!isCollapsed" class="nav-label">主要导航</span>
          <router-link
            to="/admin"
            class="nav-item"
            exact-active-class="is-active"
          >
            <LayoutDashboard class="nav-icon" :size="20" stroke-width="1.5" />
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
            <Users class="nav-icon" :size="20" stroke-width="1.5" />
            <span v-if="!isCollapsed" class="nav-text">用户管理</span>
          </router-link>
          <router-link
            to="/admin/verification"
            class="nav-item"
            active-class="is-active"
          >
            <CheckCircle class="nav-icon" :size="20" stroke-width="1.5" />
            <span v-if="!isCollapsed" class="nav-text">实名认证</span>
          </router-link>
          <router-link
            to="/admin/items"
            class="nav-item"
            active-class="is-active"
          >
            <Package class="nav-icon" :size="20" stroke-width="1.5" />
            <span v-if="!isCollapsed" class="nav-text">物品管理</span>
          </router-link>
          <router-link
            to="/admin/categories"
            class="nav-item"
            active-class="is-active"
          >
            <Menu class="nav-icon" :size="20" stroke-width="1.5" />
            <span v-if="!isCollapsed" class="nav-text">分类管理</span>
          </router-link>
          <router-link
            to="/admin/category-feedbacks"
            class="nav-item"
            active-class="is-active"
          >
            <MessageSquare class="nav-icon" :size="20" stroke-width="1.5" />
            <span v-if="!isCollapsed" class="nav-text">分类反馈</span>
          </router-link>
        </div>

        <div class="nav-group">
          <span v-if="!isCollapsed" class="nav-label">交易与数据</span>
          <router-link
            to="/admin/orders"
            class="nav-item"
            active-class="is-active"
          >
            <ClipboardList class="nav-icon" :size="20" stroke-width="1.5" />
            <span v-if="!isCollapsed" class="nav-text">订单管理</span>
          </router-link>
          <router-link
            to="/admin/disputes"
            class="nav-item"
            active-class="is-active"
          >
            <AlertTriangle class="nav-icon" :size="20" stroke-width="1.5" />
            <span v-if="!isCollapsed" class="nav-text">纠纷管理</span>
          </router-link>
          <router-link
            to="/admin/statistics"
            class="nav-item"
            active-class="is-active"
          >
            <TrendingUp class="nav-icon" :size="20" stroke-width="1.5" />
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
            <FileText class="nav-icon" :size="20" stroke-width="1.5" />
            <span v-if="!isCollapsed" class="nav-text">操作日志</span>
          </router-link>
        </div>
      </nav>

      <div class="sidebar-footer">
        <div class="user-card" v-if="!isCollapsed">
          <div class="user-avatar">
            {{ userStoreInstance.user?.nickname?.charAt(0) || '管' }}
          </div>
          <div class="user-info">
            <span class="user-name">{{
              userStoreInstance.user?.nickname || '管理员'
            }}</span>
            <span class="user-role">{{ getRoleText() }}</span>
          </div>
        </div>
        <button class="logout-btn" @click="handleLogout">
          <LogOut :size="20" stroke-width="1.5" />
          <span v-if="!isCollapsed">退出登录</span>
        </button>
      </div>
    </aside>

    <div class="admin-main" :class="{ 'sidebar-collapsed': isCollapsed }">
      <header class="admin-header">
        <div class="header-left">
          <h1 class="page-title">{{ currentPageTitle }}</h1>
        </div>
        <div class="header-right">
          <router-link to="/" class="back-home-btn">
            <Home :size="18" />
            <span>返回首页</span>
          </router-link>
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

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { userStore } from '../../store';
import { LayoutDashboard, Users, CheckCircle, Package, Menu, MessageSquare, ClipboardList, AlertTriangle, TrendingUp, FileText, LogOut, Home } from 'lucide-vue-next';

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
  '/admin/category-feedbacks': '分类反馈管理',
  '/admin/orders': '订单管理',
  '/admin/statistics': '数据统计',
  '/admin/logs': '操作日志',
};

const currentPageTitle = computed(() => {
  return (pageTitles as Record<string, string>)[route.path] || '管理后台';
});

const getRoleText = () => {
  if (userStoreInstance.user?.role === 'ADMIN') {
    return '管理员';
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
