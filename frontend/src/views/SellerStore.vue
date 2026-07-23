<template>
  <div class="seller-store">
    <!-- Loading State -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>加载卖家信息...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="error-container">
      <EmptyState type="error" :title="error" description="请检查卖家ID是否正确或稍后再试">
        <template #action>
          <button class="btn btn-primary" @click="goBack">返回首页</button>
        </template>
      </EmptyState>
    </div>

    <!-- Seller Store Content -->
    <div v-else-if="profile" class="store-container">
      <div class="store-header">
        <!-- Seller Info Card -->
        <div class="seller-info-card">
          <div class="seller-avatar-row">
            <div class="seller-avatar" :style="{ background: avatarColor }">
              {{ avatarLetter }}
            </div>
            <div class="seller-meta">
              <h1 class="seller-name">
                {{ profile.nickname }}
                <span v-if="profile.verified" class="verified-badge" title="已实名认证">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                    <path d="M9 12l2 2 4-4"/>
                  </svg>
                </span>
              </h1>
              <div class="seller-school" v-if="profile.schoolName">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M22 10v6M2 10l10-5 10 5-10 5z"/>
                  <path d="M6 12v5c3 3 9 3 12 0v-5"/>
                </svg>
                {{ profile.schoolName }}
              </div>
              <div class="seller-extra">
                <span class="credit-score" v-if="profile.creditScore">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="10"/>
                    <path d="M8 12l2 2 4-4"/>
                  </svg>
                  信用分 {{ profile.creditScore }}
                </span>
                <span class="member-since">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                    <line x1="16" y1="2" x2="16" y2="6"/>
                    <line x1="8" y1="2" x2="8" y2="6"/>
                    <line x1="3" y1="10" x2="21" y2="10"/>
                  </svg>
                  {{ memberSinceText }}
                </span>
              </div>
            </div>
          </div>
          <p class="seller-bio" v-if="profile.bio">{{ profile.bio }}</p>
          <p class="seller-bio empty-bio" v-else>这个卖家很懒，还没有填写个人简介</p>
        </div>

        <!-- Stats Section -->
        <div class="stats-row">
          <div class="stat-item">
            <span class="stat-value">{{ profile.totalItems }}</span>
            <span class="stat-label">在售</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ profile.soldItems }}</span>
            <span class="stat-label">已售</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ profile.completedDeals }}</span>
            <span class="stat-label">成交</span>
          </div>
          <div class="stat-item accent">
            <span class="stat-value">{{ ratingDisplay }}</span>
            <span class="stat-label">评分</span>
          </div>
        </div>
      </div>

      <!-- Tab Navigation -->
      <div class="tab-bar">
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'items' }"
          @click="activeTab = 'items'"
        >
          在售商品
          <span class="tab-count">{{ profile.totalItems }}</span>
        </button>
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'reviews' }"
          @click="activeTab = 'reviews'"
        >
          评价
          <span class="tab-count">{{ profile.reviewCount }}</span>
        </button>
      </div>

      <!-- Tab Content: Items Grid -->
      <div v-if="activeTab === 'items'" class="tab-content">
        <div v-if="itemsLoading" class="loading-container">
          <div class="loading-spinner"></div>
        </div>
        <div v-else-if="items.length === 0" class="empty-tab">
          <EmptyState type="default" title="暂无在售商品" description="卖家还没有发布任何商品" />
        </div>
        <div v-else class="items-grid">
          <div
            v-for="item in items"
            :key="item.id"
            class="item-card"
            @click="goToItem(item.id)"
          >
            <div class="item-card-image">
              <img
                :src="item.coverImage || defaultImage"
                :alt="item.title"
                loading="lazy"
                @error="handleImgError"
              />
            </div>
            <div class="item-card-content">
              <h3 class="item-card-title">{{ item.title }}</h3>
              <!-- 分类标签 -->
              <div class="item-card-category" v-if="item.categoryName || item.categoryId">
                <span class="category-dot">{{ categoryStore.getCategoryIcon(item.categoryName || '') }}</span>
                <span class="category-label">{{ getCategoryPathLabel(item) }}</span>
              </div>
              <div class="item-card-price">¥{{ formatPrice(item.price) }}</div>
              <div class="item-card-meta">
                <span class="item-condition">{{ conditionLabel(item.condition) }}</span>
                <span class="item-time">{{ timeAgo(item.createdAt) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Pagination for Items -->
        <div v-if="itemsTotalPages > 1" class="pagination">
          <button
            class="page-btn"
            :disabled="itemsPage <= 1"
            @click="loadItems(itemsPage - 1)"
          >
            上一页
          </button>
          <span class="page-info">{{ itemsPage }} / {{ itemsTotalPages }}</span>
          <button
            class="page-btn"
            :disabled="itemsPage >= itemsTotalPages"
            @click="loadItems(itemsPage + 1)"
          >
            下一页
          </button>
        </div>
      </div>

      <!-- Tab Content: Reviews List -->
      <div v-if="activeTab === 'reviews'" class="tab-content">
        <div v-if="reviewsLoading" class="loading-container">
          <div class="loading-spinner"></div>
        </div>
        <div v-else-if="reviews.length === 0" class="empty-tab">
          <EmptyState type="default" title="暂无评价" description="该卖家还没有收到任何评价" />
        </div>
        <div v-else class="reviews-list">
          <div v-for="review in reviews" :key="review.id" class="review-card">
            <div class="review-header">
              <div class="reviewer-avatar" :style="{ background: getReviewerColor(review) }">
                {{ review.isAnonymous ? '匿' : (review.reviewerNickname?.charAt(0) || '?' ) }}
              </div>
              <div class="reviewer-info">
                <span class="reviewer-name">{{ review.isAnonymous ? '匿名用户' : (review.reviewerNickname || '用户') }}</span>
                <span class="review-time">{{ timeAgo(review.createdAt) }}</span>
              </div>
              <div class="review-rating">
                <span v-for="n in 5" :key="n" class="star" :class="{ filled: n <= review.rating }">
                  <svg width="14" height="14" viewBox="0 0 24 24" :fill="n <= review.rating ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                  </svg>
                </span>
              </div>
            </div>
            <p class="review-content" v-if="review.content">{{ review.content }}</p>
          </div>
        </div>

        <!-- Pagination for Reviews -->
        <div v-if="reviewsTotalPages > 1" class="pagination">
          <button
            class="page-btn"
            :disabled="reviewsPage <= 1"
            @click="loadReviews(reviewsPage - 1)"
          >
            上一页
          </button>
          <span class="page-info">{{ reviewsPage }} / {{ reviewsTotalPages }}</span>
          <button
            class="page-btn"
            :disabled="reviewsPage >= reviewsTotalPages"
            @click="loadReviews(reviewsPage + 1)"
          >
            下一页
          </button>
        </div>
      </div>

      <!-- Contact Seller Button -->
      <div class="contact-bar">
        <button class="btn btn-primary btn-lg contact-btn" @click="contactSeller">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/>
          </svg>
          联系卖家
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import api from '../api';
import EmptyState from '../components/user/EmptyState.vue';
import { useCategoryStore } from '../store/category';
import type { SellerProfile as SellerProfileType, ReviewItem, ItemSummary } from '../types/api';

const route = useRoute();
const router = useRouter();
const categoryStore = useCategoryStore();

// State
const loading = ref(true);
const error = ref('');
const profile = ref<SellerProfileType | null>(null);
const activeTab = ref<'items' | 'reviews'>('items');

// Items state
const items = ref<ItemSummary[]>([]);
const itemsLoading = ref(false);
const itemsPage = ref(1);
const itemsTotalPages = ref(0);

// Reviews state
const reviews = ref<ReviewItem[]>([]);
const reviewsLoading = ref(false);
const reviewsPage = ref(1);
const reviewsTotalPages = ref(0);

const defaultImage = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="200" height="150" viewBox="0 0 200 150"%3E%3Crect fill="%23555" width="200" height="150"/%3E%3Cpath fill="%23777" d="M80 60h40v30h-40z"/%3E%3Ccircle cx="90" cy="50" r="8" fill="%23777"/%3E%3C/svg%3E';

// Computed
const sellerId = computed(() => Number(route.params.id));

const avatarLetter = computed(() => {
  if (!profile.value) return '?';
  return (profile.value.nickname || '?').charAt(0).toUpperCase();
});

const avatarColor = computed(() => {
  const colors = [
    'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
    'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)',
  ];
  const idx = (profile.value?.id || 0) % colors.length;
  return colors[idx];
});

const ratingDisplay = computed(() => {
  if (!profile.value) return '0.0';
  return profile.value.rating > 0 ? profile.value.rating.toFixed(1) : '暂无';
});

const memberSinceText = computed(() => {
  if (!profile.value?.memberSince) return '';
  return timeAgo(profile.value.memberSince, true);
});

/** 获取分类路径标签文本（如 "一级 > 二级"） */
const getCategoryPathLabel = (item: any): string => {
  if (!item.categoryId) return item.categoryName || '';
  const path = categoryStore.getCategoryPath(item.categoryId);
  if (path.length === 0) return item.categoryName || '';
  return path.map((c: any) => c.name).join(' > ');
};

const handleImgError = (e: Event) => {
  const img = e.target as HTMLImageElement;
  img.src = defaultImage;
};

// Methods
function formatPrice(price: number | string): string {
  const num = typeof price === 'string' ? parseFloat(price) : price;
  return num.toFixed(2);
}

function conditionLabel(condition: string): string {
  const map: Record<string, string> = {
    'NEW': '全新',
    'LIKE_NEW': '几乎全新',
    'GOOD': '良好',
    'FAIR': '一般',
    'POOR': '较差',
  };
  return map[condition] || condition;
}

function timeAgo(dateStr: string, absolute = false): string {
  if (!dateStr) return '';
  const now = new Date();
  const date = new Date(dateStr);
  const diffMs = now.getTime() - date.getTime();
  const diffSec = Math.floor(diffMs / 1000);
  const diffMin = Math.floor(diffSec / 60);
  const diffHour = Math.floor(diffMin / 60);
  const diffDay = Math.floor(diffHour / 24);

  if (absolute) {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
  }

  if (diffMin < 1) return '刚刚';
  if (diffMin < 60) return `${diffMin}分钟前`;
  if (diffHour < 24) return `${diffHour}小时前`;
  if (diffDay < 30) return `${diffDay}天前`;
  if (diffDay < 365) return `${Math.floor(diffDay / 30)}个月前`;
  return `${Math.floor(diffDay / 365)}年前`;
}

function getReviewerColor(review: ReviewItem): string {
  const colors = ['#667eea', '#f093fb', '#4facfe', '#43e97b', '#fa709a', '#a18cd1'];
  const idx = (review.isAnonymous ? 0 : (review.reviewerId || 0)) % colors.length;
  return colors[idx];
}

async function loadProfile() {
  loading.value = true;
  error.value = '';
  try {
    const res = await api.seller.getProfile(sellerId.value);
    if (res.code === 200 && res.data) {
      profile.value = res.data;
    } else {
      error.value = res.message || '卖家不存在';
    }
  } catch (err: unknown) {
    logger.error('加载卖家信息失败', err);
    error.value = '加载卖家信息失败';
  } finally {
    loading.value = false;
  }
}

async function loadItems(page = 1) {
  itemsLoading.value = true;
  itemsPage.value = page;
  try {
    const res = await api.seller.getItems(sellerId.value, page);
    if (res.code === 200 && res.data) {
      items.value = res.data.content || [];
      itemsTotalPages.value = res.data.totalPages || 0;
    } else {
      items.value = [];
    }
  } catch (err) {
    logger.error('加载商品列表失败', err);
    items.value = [];
  } finally {
    itemsLoading.value = false;
  }
}

async function loadReviews(page = 1) {
  reviewsLoading.value = true;
  reviewsPage.value = page;
  try {
    const res = await api.seller.getReviews(sellerId.value, page);
    if (res.code === 200 && res.data) {
      reviews.value = res.data.content || [];
      reviewsTotalPages.value = res.data.totalPages || 0;
    } else {
      reviews.value = [];
    }
  } catch (err) {
    logger.error('加载评价列表失败', err);
    reviews.value = [];
  } finally {
    reviewsLoading.value = false;
  }
}

async function contactSeller() {
  if (!profile.value) return;
  try {
    // 检查是否已登录
    const userStore = (await import('../store')).userStore;
    const store = userStore();
    if (!store.isLoggedIn) {
      ElMessage.warning('请先登录');
      router.push('/login');
      return;
    }
    const res: any = await api.chat.createChat(sellerId.value, null as any);
    if (res.code === 200 && res.data) {
      router.push(`/user/chat?chatId=${res.data.id}`);
    } else {
      ElMessage.error('创建聊天失败');
    }
  } catch (err: unknown) {
    logger.error('联系卖家失败', err);
    const apiErr = err as { response?: { data?: { message?: string } } };
    ElMessage.error(apiErr.response?.data?.message || '联系卖家失败，请稍后再试');
  }
}

function goToItem(itemId: number) {
  router.push(`/item/${itemId}`);
}

function goBack() {
  router.push('/');
}

// Watch tab change to load data
watch(activeTab, (tab) => {
  if (tab === 'items' && items.value.length === 0) {
    loadItems();
  }
  if (tab === 'reviews' && reviews.value.length === 0) {
    loadReviews();
  }
});

onMounted(async () => {
  await loadProfile();
  if (!error.value) {
    await loadItems();
    await loadReviews();
  }
});
</script>

<style scoped>
.seller-store {
  min-height: 100vh;
  background: var(--bg-body);
}

/* Loading & Error */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: var(--text-muted);
  gap: 16px;
}

.loading-spinner {
  width: 36px;
  height: 36px;
  border: 3px solid var(--border-subtle);
  border-top: 3px solid var(--primary-color);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-container {
  padding: 60px 20px;
}

/* Store Container */
.store-container {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px 16px 80px;
}

/* Seller Info Card */
.seller-info-card {
  background: var(--bg-surface);
  border-radius: 16px;
  padding: 28px 24px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-subtle);
  margin-bottom: 20px;
}

.seller-avatar-row {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-bottom: 16px;
}

.seller-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 700;
  color: var(--text-inverse);
  flex-shrink: 0;
  box-shadow: var(--shadow-md);
}

.seller-meta {
  flex: 1;
  min-width: 0;
}

.seller-name {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 4px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.verified-badge {
  color: var(--color-success);
  display: inline-flex;
  align-items: center;
}

.seller-school {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

.seller-extra {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.credit-score,
.member-since {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-muted);
}

.seller-bio {
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-secondary);
  margin: 0;
  padding: 12px 0 0;
  border-top: 1px solid var(--border-subtle);
}

.seller-bio.empty-bio {
  color: var(--text-muted);
  font-style: italic;
}

/* Stats Row */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 24px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 16px 12px;
  background: var(--bg-surface);
  border-radius: 12px;
  border: 1px solid var(--border-subtle);
  box-shadow: var(--shadow-sm);
  transition: all 0.2s;
}

.stat-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-hover);
}

.stat-item.accent {
  background: linear-gradient(135deg, var(--accent-alpha-10) 0%, var(--accent-light) 100%);
  border-color: var(--accent-subtle);
}

.stat-value {
  font-size: 24px;
  font-weight: 800;
  color: var(--text-primary);
  font-family: var(--font-display);
}

.stat-item.accent .stat-value {
  color: var(--accent);
}

.stat-label {
  font-size: 12px;
  color: var(--text-muted);
}

/* Tab Bar */
.tab-bar {
  display: flex;
  gap: 4px;
  background: var(--bg-surface);
  border-radius: 12px;
  padding: 4px;
  margin-bottom: 20px;
  border: 1px solid var(--border-subtle);
  box-shadow: var(--shadow-sm);
}

.tab-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px 16px;
  border: none;
  background: transparent;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 500;
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.2s;
}

.tab-btn:hover {
  color: var(--text-primary);
  background: var(--bg-muted);
}

.tab-btn.active {
  color: var(--primary-color);
  background: var(--primary-alpha-10);
  font-weight: 600;
}

.tab-count {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  background: var(--bg-muted);
  color: var(--text-muted);
}

.tab-btn.active .tab-count {
  background: var(--primary-alpha-15);
  color: var(--primary-color);
}

/* Tab Content */
.tab-content {
  min-height: 200px;
}

.empty-tab {
  padding: 40px 0;
}

/* Items Grid */
.items-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
  gap: 16px;
}

.item-card {
  background: var(--bg-surface);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-subtle);
  cursor: pointer;
  transition: all 0.25s;
}

.item-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-hover);
}

.item-card-image {
  aspect-ratio: 16/9;
  overflow: hidden;
  background: var(--bg-muted);
}

.item-card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.item-card:hover .item-card-image img {
  transform: scale(1.05);
}

.item-card-content {
  padding: 12px 14px;
}

.item-card-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 6px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.item-card-category {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 6px;
  font-size: 12px;
  color: var(--text-muted);
  line-height: 1.4;
}

.category-dot {
  font-size: 14px;
  flex-shrink: 0;
}

.category-label {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-card-price {
  font-size: 18px;
  font-weight: 700;
  color: var(--error-color);
  margin-bottom: 6px;
}

.item-card-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: var(--text-muted);
}

.item-condition {
  padding: 2px 8px;
  border-radius: 4px;
  background: var(--bg-muted);
}

/* Reviews List */
.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.review-card {
  background: var(--bg-surface);
  border-radius: 12px;
  padding: 18px 20px;
  border: 1px solid var(--border-subtle);
  box-shadow: var(--shadow-sm);
}

.review-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.reviewer-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  color: var(--text-inverse);
  flex-shrink: 0;
}

.reviewer-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.reviewer-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.review-time {
  font-size: 12px;
  color: var(--text-muted);
}

.review-rating {
  display: flex;
  gap: 2px;
}

.star {
  color: var(--text-muted);
}

.star.filled {
  color: var(--color-warning);
}

.review-content {
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-secondary);
  margin: 0;
}

/* Pagination */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 24px 0;
}

.page-btn {
  padding: 8px 20px;
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
  background: var(--bg-surface);
  color: var(--text-primary);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: var(--text-muted);
}

/* Contact Bar */
.contact-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 16px;
  padding-bottom: calc(env(safe-area-inset-bottom, 0px) + 12px);
  background: var(--bg-surface);
  border-top: 1px solid var(--border-subtle);
  box-shadow: var(--shadow-sm-up);
  z-index: 10;
}

.contact-btn {
  width: 100%;
  max-width: 400px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px 24px;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

/* Utility Button Styles */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
  text-decoration: none;
  font-family: inherit;
}

.btn-primary {
  background: var(--btn-primary-bg);
  color: var(--btn-primary-text);
  border-radius: var(--btn-radius);
  padding: 10px 24px;
  font-size: 15px;
  font-weight: var(--btn-font-weight);
}

.btn-primary:hover {
  opacity: 0.9;
  transform: translateY(-1px);
  box-shadow: var(--btn-primary-shadow);
}

.btn-lg {
  padding: 14px 28px;
  font-size: 16px;
}

/* Responsive */
@media (max-width: 768px) {
  .store-container {
    padding: 16px 12px 80px;
  }

  .seller-avatar {
    width: 52px;
    height: 52px;
    font-size: 22px;
  }

  .seller-name {
    font-size: 18px;
  }

  .stats-row {
    gap: 8px;
  }

  .stat-item {
    padding: 12px 8px;
  }

  .stat-value {
    font-size: 20px;
  }

  .items-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
}

@media (max-width: 375px) {
  .items-grid {
    grid-template-columns: 1fr;
  }
}

/* Reduced Motion — 动效降级 */
@media (prefers-reduced-motion: reduce) {
  .loading-spinner { animation: none; }
  .item-card { transition: none; }
  .item-card-image img { transition: none; }
  .stat-item { transition: none; }
  .tab-btn { transition: none; }
  .page-btn { transition: none; }
  .contact-btn { transition: none; }
  .btn { transition: none; }
}
</style>
