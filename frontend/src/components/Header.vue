<template>
  <!-- Top accent bar -->
  <div class="top-accent-bar"></div>

  <!-- Desktop/Tablet Top Nav -->
  <nav class="top-nav">
    <div class="nav-inner">
      <router-link to="/" class="nav-logo">
        <Leaf :size="24" stroke-width="2" />
        GreenLoop
      </router-link>
      <div class="nav-links">
        <router-link to="/" class="nav-link" :class="{ active: $route.path === '/' }">首页</router-link>
        <router-link to="/items" class="nav-link" :class="{ active: $route.path === '/items' }">发现</router-link>
        <router-link to="/publish" class="nav-link" :class="{ active: $route.path === '/publish' }">发布</router-link>
      </div>
      <div class="nav-search">
        <Search :size="18" stroke-width="2" />
        <input type="text" placeholder="搜索校园好物…" v-model="searchKeyword" @keyup.enter="handleSearch" />
      </div>
      <div class="nav-actions">
        <button class="theme-toggle" @click="toggleTheme" :title="isDark ? '切换到亮色' : '切换到暗色'" aria-label="切换主题">
          <Moon class="icon-moon" :size="20" stroke-width="2" />
          <Sun class="icon-sun" :size="20" stroke-width="2" />
        </button>
        <button class="nav-icon-btn" title="消息通知" @click="handleNavigate('/user/notifications')">
          <Bell :size="20" stroke-width="2" />
          <span class="dot" v-if="hasNotifications"></span>
        </button>
        <template v-if="store.isLoggedIn">
          <div class="nav-avatar-wrapper" @mouseenter="showUserMenu = true" @mouseleave="showUserMenu = false">
            <div class="nav-avatar" :title="getUserName()">
              <img v-if="store.user?.avatar" :src="store.user.avatar" alt="头像" class="avatar-img" />
              <span v-else>{{ getAvatarText() }}</span>
            </div>
            <transition name="dropdown">
              <div v-if="showUserMenu" class="avatar-dropdown">
                <!-- 用户信息头部 -->
                <div class="dropdown-user-header">
                  <div class="dropdown-avatar">
                    <img v-if="store.user?.avatar" :src="store.user.avatar" alt="头像" class="dropdown-avatar-img" />
                    <span v-else>{{ getAvatarText() }}</span>
                  </div>
                  <div class="dropdown-user-info">
                    <div class="dropdown-nickname">{{ getUserName() }}</div>
                    <div class="dropdown-credit">信用分 {{ store.user?.creditScore || 100 }}</div>
                  </div>
                </div>
                <div class="dropdown-divider"></div>
                <!-- 菜单项 -->
                <router-link to="/user/profile" class="dropdown-item" @click="showUserMenu = false">
                  <User :size="18" stroke-width="2" />
                  个人中心
                </router-link>
                <router-link to="/user/items" class="dropdown-item" @click="showUserMenu = false">
                  <Package :size="18" stroke-width="2" />
                  我的发布
                </router-link>
                <router-link to="/user/orders" class="dropdown-item" @click="showUserMenu = false">
                  <ClipboardList :size="18" stroke-width="2" />
                  我的订单
                </router-link>
                <router-link to="/user/favorites" class="dropdown-item" @click="showUserMenu = false">
                  <Heart :size="18" stroke-width="2" />
                  我的收藏
                </router-link>
                <router-link to="/user/chat" class="dropdown-item" @click="showUserMenu = false">
                  <MessageSquare :size="18" stroke-width="2" />
                  消息中心
                </router-link>
                <div class="dropdown-divider"></div>
                <!-- 退出登录 -->
                <div class="dropdown-item dropdown-logout" @click="handleLogout">
                  <LogOut :size="18" stroke-width="2" />
                  退出登录
                </div>
              </div>
            </transition>
          </div>
        </template>
        <template v-else>
          <router-link to="/login" class="nav-link">登录</router-link>
        </template>
      </div>
    </div>
  </nav>

  <!-- Mobile Header -->
  <header class="mobile-header">
    <div class="mobile-header-inner">
      <router-link to="/" class="mobile-logo">
        <Leaf :size="24" stroke-width="2" />
        GreenLoop
      </router-link>
      <div class="mobile-header-actions">
        <button class="theme-toggle" @click="toggleTheme" :title="isDark ? '切换到亮色' : '切换到暗色'" aria-label="切换主题">
          <Moon class="icon-moon" :size="20" stroke-width="2" />
          <Sun class="icon-sun" :size="20" stroke-width="2" />
        </button>
        <button @click="focusSearch" title="搜索">
          <Search :size="20" stroke-width="2" />
        </button>
        <button title="消息通知" @click="handleNavigate('/user/notifications')">
          <Bell :size="20" stroke-width="2" />
        </button>
      </div>
    </div>
  </header>

  <!-- Bottom Tab Bar (Mobile) -->
  <nav class="bottom-bar">
    <router-link to="/" class="tab-item" :class="{ active: $route.path === '/' }">
      <House :size="22" stroke-width="2" />
      <span>首页</span>
    </router-link>
    <router-link to="/items" class="tab-item" :class="{ active: $route.path === '/items' }">
      <Compass :size="22" stroke-width="2" />
      <span>发现</span>
    </router-link>
    <router-link to="/publish" class="tab-item tab-publish">
      <div class="tab-publish-btn">
        <Plus :size="22" stroke-width="2.5" />
      </div>
    </router-link>
    <router-link to="/user/notifications" class="tab-item" :class="{ active: $route.path === '/user/notifications' }">
      <Bell :size="22" stroke-width="2" />
      <span>消息</span>
    </router-link>
    <router-link to="/user/profile" class="tab-item" :class="{ active: $route.path.startsWith('/user') }">
      <User :size="22" stroke-width="2" />
      <span>我的</span>
    </router-link>
    <div v-if="store.isLoggedIn" class="tab-item tab-logout" @click="handleLogout">
      <LogOut :size="22" stroke-width="2" />
      <span>退出</span>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { userStore } from '../store';
import { useDarkMode } from '../composables/useDarkMode';
import { Leaf, Search, Bell, MessageSquare, House, Compass, Plus, User, LogOut, Heart, ClipboardList, Package, Moon, Sun } from 'lucide-vue-next';
import { logger } from '@/utils/logger';

const router = useRouter();
const store = userStore();
const searchKeyword = ref('');
const hasNotifications = ref(true);
const showUserMenu = ref(false);

const { isDark, toggle: toggleTheme } = useDarkMode();

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/items', query: { keyword: searchKeyword.value } });
  }
};

const handleNavigate = (path: string) => {
  if (store.isLoggedIn) {
    router.push(path);
  } else {
    localStorage.setItem('redirectPath', path);
    router.push('/login');
  }
};

const focusSearch = () => {
  const input = document.querySelector('.nav-search input') as HTMLInputElement;
  input?.focus();
};

const getAvatarText = () => {
  const u = store.user;
  if (u) {
    if (u.nickname && u.nickname.length > 0) return u.nickname.charAt(0);
    if (u.username && u.username.length > 0) return u.username.charAt(0);
  }
  return '陈';
};

const getUserName = () => {
  const u = store.user;
  if (u) {
    if (u.nickname && u.nickname.length > 0) return u.nickname;
    if (u.username && u.username.length > 0) return u.username;
  }
  return '用户';
};

const handleLogout = async () => {
  showUserMenu.value = false;
  try {
    await store.logout();
    router.push('/');
    ElMessage.success('已退出登录');
  } catch (error) {
    logger.error('退出登录失败', error);
    router.push('/');
  }
};
</script>

<style scoped src="../styles/components/header.css"></style>

<style scoped>
/* 头像下拉菜单容器 */
.nav-avatar-wrapper {
  position: relative;
  cursor: pointer;
}

/* 下拉菜单面板 */
.avatar-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  z-index: 1000;
  background: var(--bg-surface, #fff);
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  min-width: 220px;
  padding: 8px 0;
}

/* 用户信息头部 */
.dropdown-user-header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  gap: 12px;
}

.dropdown-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--primary-color);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
  overflow: hidden;
}

.dropdown-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.dropdown-user-info {
  flex: 1;
  min-width: 0;
}

.dropdown-nickname {
  font-weight: 600;
  font-size: 14px;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dropdown-credit {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 2px;
}

/* 菜单项 */
.dropdown-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  font-size: 14px;
  color: var(--text-primary);
  text-decoration: none;
  cursor: pointer;
  transition: background 0.15s;
}

.dropdown-item:hover {
  background: var(--bg-muted);
}

.dropdown-item svg {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}

.dropdown-logout:hover {
  color: #e74c3c;
}

/* 分割线 */
.dropdown-divider {
  height: 1px;
  background: var(--border-subtle);
  margin: 4px 0;
}

/* 过渡动画 */
.dropdown-enter-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}

.dropdown-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}

.dropdown-enter-from {
  opacity: 0;
  transform: translateY(-4px);
}

.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

/* 移动端退出按钮 */
.tab-logout {
  cursor: pointer;
}
</style>
