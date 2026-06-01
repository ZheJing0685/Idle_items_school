<template>
  <!-- Top accent bar -->
  <div class="top-accent-bar"></div>

  <!-- Desktop/Tablet Top Nav -->
  <nav class="top-nav">
    <div class="nav-inner">
      <router-link to="/" class="nav-logo">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M17 8C8 10 5.9 16.17 3.82 21.34l1.89.66.95-2.3c.48.17.98.3 1.34.3C19 20 22 3 22 3c-1 2-8 2.25-13 3.25S2 11.5 2 13.5s1.75 3.75 1.75 3.75C7 8 17 8 17 8z" />
        </svg>
        GreenLoop
      </router-link>
      <div class="nav-links">
        <router-link to="/" class="nav-link" :class="{ active: $route.path === '/' }">首页</router-link>
        <router-link to="/items" class="nav-link" :class="{ active: $route.path === '/items' }">发现</router-link>
        <router-link to="/publish" class="nav-link" :class="{ active: $route.path === '/publish' }">发布</router-link>
      </div>
      <div class="nav-search">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
          <circle cx="11" cy="11" r="8" />
          <path d="M21 21l-4.35-4.35" />
        </svg>
        <input type="text" placeholder="搜索校园好物…" v-model="searchKeyword" @keyup.enter="handleSearch" />
      </div>
      <div class="nav-actions">
        <button class="nav-icon-btn" title="消息" @click="$router.push('/user/chat')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
            <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z" />
          </svg>
          <span class="dot" v-if="hasNotifications"></span>
        </button>
        <template v-if="store.isLoggedIn">
          <div class="nav-avatar" @click="$router.push('/user/profile')" :title="getUserName()">
            {{ getAvatarText() }}
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
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M17 8C8 10 5.9 16.17 3.82 21.34l1.89.66.95-2.3c.48.17.98.3 1.34.3C19 20 22 3 22 3c-1 2-8 2.25-13 3.25S2 11.5 2 13.5s1.75 3.75 1.75 3.75C7 8 17 8 17 8z" />
        </svg>
        GreenLoop
      </router-link>
      <div class="mobile-header-actions">
        <button @click="focusSearch" title="搜索">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
            <circle cx="11" cy="11" r="8" />
            <path d="M21 21l-4.35-4.35" />
          </svg>
        </button>
        <button title="消息" @click="$router.push('/user/chat')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
            <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z" />
          </svg>
        </button>
      </div>
    </div>
  </header>

  <!-- Bottom Tab Bar (Mobile) -->
  <nav class="bottom-bar">
    <router-link to="/" class="tab-item" :class="{ active: $route.path === '/' }">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
        <path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z" />
        <polyline points="9,22 9,12 15,12 15,22" />
      </svg>
      <span>首页</span>
    </router-link>
    <router-link to="/items" class="tab-item" :class="{ active: $route.path === '/items' }">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
        <circle cx="11" cy="11" r="8" />
        <path d="M21 21l-4.35-4.35" />
      </svg>
      <span>发现</span>
    </router-link>
    <router-link to="/publish" class="tab-item tab-publish">
      <div class="tab-publish-btn">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
          <path d="M12 5v14m-7-7h14" />
        </svg>
      </div>
    </router-link>
    <router-link to="/user/chat" class="tab-item" :class="{ active: $route.path === '/user/chat' }">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
        <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z" />
      </svg>
      <span>消息</span>
    </router-link>
    <router-link to="/user/profile" class="tab-item" :class="{ active: $route.path.startsWith('/user') }">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
        <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2" />
        <circle cx="12" cy="7" r="4" />
      </svg>
      <span>我的</span>
    </router-link>
  </nav>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { userStore } from '../store';

const router = useRouter();
const store = userStore();
const searchKeyword = ref('');
const hasNotifications = ref(true);

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/items', query: { keyword: searchKeyword.value } });
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
</script>

<style scoped src="../styles/components/header.css"></style>
