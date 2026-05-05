<template>
  <header class="header">
    <div class="header-main">
      <div class="container">
        <div class="header-content">
          <router-link to="/" class="logo">
            <div class="logo-icon">
              <svg width="36" height="36" viewBox="0 0 36 36" fill="none">
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

          <div class="search-section">
            <div class="search-box">
              <el-input
                v-model="searchKeyword"
                placeholder="搜索你想要的闲置好物..."
                @keyup.enter="handleSearch"
                class="search-input"
              >
                <template #prefix>
                  <svg
                    class="search-icon"
                    width="18"
                    height="18"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <circle cx="11" cy="11" r="8" />
                    <path d="M21 21L16.65 16.65" />
                  </svg>
                </template>
                <template #append>
                  <el-button @click="handleSearch" class="search-btn">
                    搜索
                  </el-button>
                </template>
              </el-input>
            </div>
          </div>

          <nav class="nav-actions">
            <router-link to="/items" class="nav-link">
              <svg
                width="20"
                height="20"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <rect x="3" y="3" width="7" height="7" rx="1" />
                <rect x="14" y="3" width="7" height="7" rx="1" />
                <rect x="3" y="14" width="7" height="7" rx="1" />
                <rect x="14" y="14" width="7" height="7" rx="1" />
              </svg>
              <span>发现</span>
            </router-link>

            <router-link
              to="/publish"
              class="nav-link nav-link-primary"
              v-if="store.isLoggedIn"
            >
              <svg
                width="20"
                height="20"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <circle cx="12" cy="12" r="10" />
                <path d="M12 8V16" />
                <path d="M8 12H16" />
              </svg>
              <span>发布</span>
            </router-link>

            <div class="user-section" v-if="store.isLoggedIn">
              <el-dropdown
                trigger="click"
                @visible-change="dropdownOpen = $event"
              >
                <div class="user-trigger">
                  <div class="user-avatar-wrap">
                    <el-avatar :size="40" class="user-avatar">
                      {{ getAvatarText() }}
                    </el-avatar>
                    <span class="user-badge" v-if="store.user?.verified">
                      <svg
                        width="12"
                        height="12"
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
                    width="16"
                    height="16"
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
                    <el-dropdown-item divided>
                      <router-link to="/user/profile" class="menu-link">
                        <svg
                          width="18"
                          height="18"
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
                        <span>个人中心</span>
                      </router-link>
                    </el-dropdown-item>
                    <el-dropdown-item>
                      <router-link to="/user/items" class="menu-link">
                        <svg
                          width="18"
                          height="18"
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
                    </el-dropdown-item>
                    <el-dropdown-item>
                      <router-link to="/orders" class="menu-link">
                        <svg
                          width="18"
                          height="18"
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
                          <path d="M9 12H15" />
                          <path d="M9 16H13" />
                        </svg>
                        <span>我的订单</span>
                      </router-link>
                    </el-dropdown-item>
                    <el-dropdown-item>
                      <router-link to="/user/favorites" class="menu-link">
                        <svg
                          width="18"
                          height="18"
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
                    </el-dropdown-item>
                    <el-dropdown-item v-if="store.isAdmin" divided>
                      <router-link
                        to="/admin"
                        class="menu-link menu-link-admin"
                      >
                        <svg
                          width="18"
                          height="18"
                          viewBox="0 0 24 24"
                          fill="none"
                          stroke="currentColor"
                          stroke-width="2"
                        >
                          <path
                            d="M12 15C15.866 15 19 11.866 19 8C19 4.13401 15.866 1 12 1C8.13401 1 5 4.13401 5 8C5 11.866 8.13401 15 12 15Z"
                          />
                          <path d="M8.21 13.89L7 23L12 20L17 23L15.79 13.88" />
                        </svg>
                        <span>管理后台</span>
                      </router-link>
                    </el-dropdown-item>
                    <el-dropdown-item divided @click="handleLogout">
                      <div class="menu-link menu-link-logout">
                        <svg
                          width="18"
                          height="18"
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

            <template v-else>
              <router-link to="/login" class="auth-link auth-link-login">
                登录
              </router-link>
              <router-link to="/register" class="auth-link auth-link-register">
                注册
              </router-link>
            </template>
          </nav>
        </div>
      </div>
    </div>

    <nav class="header-nav">
      <div class="container">
        <div class="nav-menu">
          <router-link to="/" class="nav-item" exact-active-class="active">
            <svg
              width="18"
              height="18"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <path
                d="M3 9L12 2L21 9V20C21 20.5304 20.7893 21.0391 20.4142 21.4142C20.0391 21.7893 19.5304 22 19 22H5C4.46957 22 3.96086 21.7893 3.58579 21.4142C3.21071 21.0391 3 20.5304 3 20V9Z"
              />
              <path d="M9 22V12H15V22" />
            </svg>
            <span>首页</span>
          </router-link>
          <router-link
            to="/items"
            class="nav-item"
            :class="{ active: isItemsActive && !route.query.category }"
          >
            <svg
              width="18"
              height="18"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <rect x="3" y="3" width="7" height="7" />
              <rect x="14" y="3" width="7" height="7" />
              <rect x="14" y="14" width="7" height="7" />
              <rect x="3" y="14" width="7" height="7" />
            </svg>
            <span>浏览好物</span>
          </router-link>
          <router-link
            v-for="category in categories"
            :key="category.id"
            :to="`/items?category=${category.id}`"
            class="nav-item"
            :class="{ active: route.query.category === category.id.toString() }"
          >
            <span class="nav-tag" :class="{ 'nav-tag-new': category.id === 1 }">
              {{ getCategoryShortName(category.name) }}
            </span>
          </router-link>
          <div class="nav-eco">
            <svg
              width="16"
              height="16"
              viewBox="0 0 24 24"
              fill="var(--secondary-color)"
            >
              <path
                d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z"
              />
              <path
                d="M7 13C7 13 8 15 12 15C16 15 17 13 17 13"
                stroke="white"
                stroke-width="2"
                stroke-linecap="round"
              />
              <path
                d="M12 9C12 9 9 10 9 12"
                stroke="white"
                stroke-width="2"
                stroke-linecap="round"
              />
            </svg>
            <span>绿色校园 · 环保交易</span>
          </div>
        </div>
      </div>
    </nav>
  </header>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessageBox, ElMessage } from 'element-plus';
import { userStore } from '../store';
import api from '../api';

const route = useRoute();
const router = useRouter();
const searchKeyword = ref('');
const dropdownOpen = ref(false);
const store = userStore();
const categories = ref([]);

// 计算浏览好物导航项的激活状态
const isItemsActive = computed(() => {
  return route.path === '/items';
});

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

const getCategoryShortName = (name) => {
  const nameMap = {
    数码产品: '数码',
    书籍教材: '书籍',
    服饰鞋包: '穿搭',
    生活用品: '生活',
    运动户外: '运动',
    虚拟物品: '虚拟',
    其他: '其他',
  };
  return nameMap[name] || name;
};

// 获取分类数据
const loadCategories = async () => {
  try {
    const response = await api.category.getCategories();
    if (response.code === 200) {
      categories.value = response.data.filter((cat) => cat.parentId === null);
    }
  } catch (error) {
    console.error('获取分类失败:', error);
  }
};

onMounted(() => {
  loadCategories();
});
</script>

<style scoped src="../styles/components/header.css"></style>
