<template>
  <header class="header">
    <div class="header-container">
      <div class="container">
        <div class="header-content">
          <router-link to="/" class="logo">
            <div class="logo-icon">
              <svg width="32" height="32" viewBox="0 0 36 36" fill="none">
                <circle cx="18" cy="18" r="16" fill="var(--primary-color)" />
                <path
                  d="M12 18C12 14.6863 14.6863 12 18 12C21.3137 12 24 14.6863 24 18"
                  stroke="white"
                  stroke-width="2.5"
                  stroke-linecap="round"
                />
                <path
                  d="M18 18V24"
                  stroke="white"
                  stroke-width="2.5"
                  stroke-linecap="round"
                />
                <circle cx="18" cy="14" r="2" fill="white" />
              </svg>
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
              <svg
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                v-html="iconMap[item.icon]"
              />
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
                    <svg
                      class="search-icon"
                      width="16"
                      height="16"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                    >
                      <circle cx="11" cy="11" r="8" />
                      <path d="M21 21L16.65 16.65" />
                    </svg>
                  </template>
                </el-input>
              </div>
            </div>

            <div class="nav-actions">
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
                          <svg
                            width="10"
                            height="10"
                            viewBox="0 0 24 24"
                            fill="var(--secondary-color)"
                          >
                            <path
                              d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"
                            />
                          </svg>
                        </span>
                      </div>
                      <span class="user-name">{{ getUserName() }}</span>
                      <svg
                        class="dropdown-arrow"
                        :class="{ open: dropdownOpen }"
                        width="14"
                        height="14"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        stroke-width="2"
                      >
                        <path d="M6 9L12 15L18 9" />
                      </svg>
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
                            <svg
                              width="16"
                              height="16"
                              viewBox="0 0 24 24"
                              fill="none"
                              stroke="currentColor"
                              stroke-width="2"
                              v-html="iconMap[menuItem.icon]"
                            />
                            <span>{{ menuItem.name }}</span>
                          </router-link>
                        </el-dropdown-item>
                        <el-dropdown-item divided @click="handleLogout">
                          <div class="menu-link menu-link-logout">
                            <svg
                              width="16"
                              height="16"
                              viewBox="0 0 24 24"
                              fill="none"
                              stroke="currentColor"
                              stroke-width="2"
                            >
                              <path
                                d="M9 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H9"
                              />
                              <path d="M16 17L21 12L16 7" />
                              <path d="M21 12H9" />
                            </svg>
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

<script setup>
import { ref, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessageBox, ElMessage } from 'element-plus';
import { userStore } from '../store';
import { getNavigationItems, getUserMenuItems, iconMap } from '../config/navigation';

const route = useRoute();
const router = useRouter();
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
const isActiveRoute = (path) => {
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
  if (store.user) {
    if (store.user.nickname?.length > 0) return store.user.nickname.charAt(0);
    if (store.user.username?.length > 0) return store.user.username.charAt(0);
  }
  return '我';
};

const getUserName = () => {
  if (store.user) {
    if (store.user.nickname?.length > 0) return store.user.nickname;
    if (store.user.username?.length > 0) return store.user.username;
  }
  return '用户';
};
</script>

<style scoped src="../styles/components/header.css"></style>
