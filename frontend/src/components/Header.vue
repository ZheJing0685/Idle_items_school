<template>
  <header class="header">
    <div class="header-container">
      <div class="container">
        <div class="header-content">
          <router-link to="/" class="logo">
            <div class="logo-icon">
              <Package :size="32" stroke-width="1.5" color="var(--primary-color)" />
            </div>
            <div class="logo-text">
              <span class="logo-title">闲置好物</span>
              <span class="logo-tagline">校园绿色交易</span>
            </div>
          </router-link>

          <nav class="nav-menu">
            <router-link
              v-for="item in navigationItems"
              :key="item.path"
              :to="item.path"
              class="nav-item"
              :class="{ active: isActiveRoute(item.path) }"
            >
              <component :is="navIconMap[item.icon]" :size="18" />
              <span>{{ item.name }}</span>
            </router-link>
          </nav>

          <div class="header-right">
            <div class="search-section">
              <div class="search-box">
                <el-input
                  v-model="searchKeyword"
                  placeholder="搜索闲置好物..."
                  @keyup.enter="handleSearch"
                  class="search-input"
                >
                  <template #prefix>
                    <Search class="search-icon" :size="16" />
                  </template>
                </el-input>
              </div>
            </div>

            <div class="nav-actions">
              <button class="theme-toggle" @click="toggleDark" :title="isDark ? '切换亮色模式' : '切换暗色模式'" :aria-label="isDark ? '切换亮色模式' : '切换暗色模式'">
                <Sun v-if="isDark" :size="20" />
                <Moon v-else :size="20" />
              </button>
              <template v-if="store.isLoggedIn">
                <div class="user-section">
                  <el-dropdown
                    trigger="click"
                    @visible-change="dropdownOpen = $event"
                  >
                    <div class="user-trigger">
                      <div class="user-avatar-wrap">
                        <el-avatar :size="36" class="user-avatar">
                          {{ getAvatarText() }}
                        </el-avatar>
                        <span class="user-badge" v-if="store.user?.verified">
                          <Star :size="10" fill="var(--secondary-color)" color="var(--secondary-color)" />
                        </span>
                      </div>
                      <span class="user-name">{{ getUserName() }}</span>
                      <ChevronDown
                        class="dropdown-arrow"
                        :class="{ open: dropdownOpen }"
                        :size="14"
                      />
                    </div>
                    <template #dropdown>
                      <el-dropdown-menu class="user-menu">
                        <div class="menu-header">
                          <span class="menu-greeting">你好，</span>
                          <span class="menu-name">{{
                            store.user?.nickname || store.user?.username
                          }}</span>
                        </div>
                        <el-dropdown-item
                          v-for="menuItem in userMenuItems"
                          :key="menuItem.path"
                          :divided="menuItem.divided"
                        >
                          <router-link :to="menuItem.path" class="menu-link">
                            <component :is="navIconMap[menuItem.icon]" :size="16" />
                            <span>{{ menuItem.name }}</span>
                          </router-link>
                        </el-dropdown-item>
                        <el-dropdown-item divided @click="handleLogout">
                          <div class="menu-link menu-link-logout">
                            <LogOut :size="16" />
                            <span>退出登录</span>
                          </div>
                        </el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </template>
              <template v-else>
                <router-link to="/login" class="auth-link auth-link-login">
                  登录
                </router-link>
                <router-link to="/register" class="auth-link auth-link-register">
                  注册
                </router-link>
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessageBox, ElMessage } from 'element-plus';
import { userStore } from '../store';
import { getNavigationItems, getUserMenuItems } from '../config/navigation';
import { useDarkMode } from '../composables/useDarkMode';
import {
  Package, Search, Sun, Moon, Star, ChevronDown, LogOut,
  Home, Grid, CirclePlus, Shield, User, ShoppingBag, Heart, MessageSquare, Bell
} from 'lucide-vue-next';

const navIconMap: Record<string, any> = {
  home: Home,
  grid: Grid,
  'plus-circle': CirclePlus,
  shield: Shield,
  user: User,
  box: Package,
  'shopping-bag': ShoppingBag,
  heart: Heart,
  message: MessageSquare,
  bell: Bell,
};

const route = useRoute();
const router = useRouter();
const { isDark, toggle: toggleDark } = useDarkMode();
const searchKeyword = ref('');
const dropdownOpen = ref(false);
const store = userStore();

// 计算用户角色
const userRole = computed(() => {
  if (!store.isLoggedIn) return 'guest';
  if (store.isAdmin) return 'admin';
  return 'user';
});

// 计算导航项
const navigationItems = computed(() => {
  return getNavigationItems(userRole.value);
});

// 计算用户菜单项
const userMenuItems = computed(() => {
  const items = getUserMenuItems(store.isAdmin);
  return items.map((item, index) => ({
    ...item,
    divided: index === 0 || index === items.length - 1,
  }));
});

// 判断路由是否激活
const isActiveRoute = (path: string) => {
  if (path === '/') {
    return route.path === '/';
  }
  return route.path.startsWith(path);
};

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/items', query: { keyword: searchKeyword.value } });
  }
};

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(() => {
      store.logout();
      ElMessage.success('已退出登录');
      router.push('/');
    })
    .catch(() => {});
};

const getAvatarText = () => {
  const u = store.user;
  if (u) {
    if (u.nickname && u.nickname.length > 0) return u.nickname.charAt(0);
    if (u.username && u.username.length > 0) return u.username.charAt(0);
  }
  return '我';
};

const getUserName = () => {
  const u = store.user;
  if (u) {
    if (u.nickname && u.nickname.length > 0) return u.nickname;
    if (u.username && u.username.length > 0) return u.username;
  }
  return '用户';
};
</script>

<style scoped src="../styles/components/header.css"></style>
