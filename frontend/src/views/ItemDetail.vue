<template>
  <div class="item-detail-page">
    <div class="container">
      <!-- Back Button -->
      <router-link to="/items" class="detail-back">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
          <path d="M19 12H5m7-7l-7 7 7 7" />
        </svg>
        返回列表
      </router-link>

      <!-- Loading State -->
      <div v-if="loading" class="loading-state">
        <div class="loading-grid">
          <div class="loading-image loading-skeleton"></div>
          <div class="loading-info">
            <div class="loading-skeleton" style="height: 40px; width: 80%"></div>
            <div class="loading-skeleton" style="height: 60px; width: 50%; margin-top: 16px"></div>
            <div class="loading-skeleton" style="height: 120px; margin-top: 24px"></div>
          </div>
        </div>
      </div>

      <!-- Item Detail -->
      <div v-else-if="item" class="detail-layout">
        <!-- Gallery -->
        <div class="detail-gallery">
          <div class="detail-main-img" @click="openLightbox">
            <img v-if="currentImage" :src="currentImage" :alt="item.title" class="main-img" />
            <div v-else class="img-placeholder" :style="{ background: getGalleryBg() }">
              {{ getCategoryEmoji(item.categoryName) }}
            </div>
            <div class="img-count" v-if="allImages.length > 1">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <rect x="3" y="3" width="18" height="18" rx="2" />
                <circle cx="8.5" cy="8.5" r="1.5" />
                <path d="M21 15l-5-5L5 21" />
              </svg>
              {{ allImages.length }} 张图片
            </div>
          </div>
          <div class="detail-thumbs" v-if="allImages.length > 1">
            <div
              v-for="(img, index) in allImages"
              :key="index"
              class="detail-thumb"
              :class="{ active: currentThumb === index }"
              @click="currentThumb = index; currentImage = img"
            >
              <img :src="img" :alt="`图片 ${index + 1}`" />
            </div>
          </div>
        </div>

        <!-- Lightbox -->
        <div class="lightbox" v-if="showLightbox" @click="closeLightbox">
          <div class="lightbox-content" @click.stop>
            <button class="lightbox-close" @click="closeLightbox">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <path d="M18 6L6 18M6 6l12 12" />
              </svg>
            </button>
            <button class="lightbox-prev" v-if="allImages.length > 1" @click="prevImage">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <path d="M15 18l-6-6 6-6" />
              </svg>
            </button>
            <img :src="currentImage" :alt="item.title" class="lightbox-img" />
            <button class="lightbox-next" v-if="allImages.length > 1" @click="nextImage">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <path d="M9 18l6-6-6-6" />
              </svg>
            </button>
            <div class="lightbox-counter">{{ currentThumb + 1 }} / {{ allImages.length }}</div>
          </div>
        </div>

        <!-- Info -->
        <div class="detail-info">
          <div>
            <div class="detail-title">{{ item.title }}</div>
            <div class="detail-tags" style="margin-top:10px">
              <span v-if="item.eco" class="tag tag-eco">环保优选</span>
              <span class="tag tag-condition">{{ getConditionText(item.condition) }}</span>
              <span class="tag tag-category">{{ getCategoryEmoji(item.categoryName) }} {{ item.categoryName }}</span>
            </div>
          </div>

          <div class="detail-price-row">
            <div class="detail-price">
              <span class="unit">¥</span>{{ item.price?.toLocaleString() }}
            </div>
            <div v-if="item.originalPrice" class="detail-original-price">
              原价 ¥{{ item.originalPrice?.toLocaleString() }}
            </div>
          </div>

          <div class="detail-specs">
            <div class="detail-spec">
              <span class="detail-spec-label">新旧程度</span>
              <span class="detail-spec-value">{{ getConditionText(item.condition) }}</span>
            </div>
            <div class="detail-spec">
              <span class="detail-spec-label">发布时间</span>
              <span class="detail-spec-value">{{ getTimeAgo(item.createdAt) }}</span>
            </div>
            <div class="detail-spec">
              <span class="detail-spec-label">分类</span>
              <span class="detail-spec-value">{{ item.categoryName }}</span>
            </div>
            <div class="detail-spec">
              <span class="detail-spec-label">交易方式</span>
              <span class="detail-spec-value">{{ getDeliveryText(item.deliveryMethod) }}</span>
            </div>
          </div>

          <div class="detail-desc">{{ item.description || '暂无详细描述' }}</div>

          <!-- Seller Card -->
          <div class="seller-card">
            <div class="seller-avatar">{{ item.sellerNickname?.charAt(0) || '卖' }}</div>
            <div class="seller-info">
              <div class="seller-name">{{ item.sellerNickname || '匿名用户' }}</div>
              <div class="seller-school">{{ item.sellerSchool || '校园卖家' }}</div>
              <div class="seller-rating">⭐ {{ item.sellerRating?.toFixed(1) || '4.9' }} · 已售 {{ item.sellerItemsCount || 0 }} 件</div>
            </div>
            <button class="btn btn-ghost btn-sm" @click="viewSellerProfile">查看主页</button>
          </div>

          <!-- Actions -->
          <div class="detail-actions">
            <button class="btn btn-secondary btn-lg" @click="toggleFavorite">
              <svg width="18" height="18" viewBox="0 0 24 24" :fill="isFavorited ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2">
                <path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z" />
              </svg>
              {{ isFavorited ? '已收藏' : '收藏' }}
            </button>
            <button class="btn btn-primary btn-lg" @click="handleContact">
              💬 我想要
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import api from '../api';
import { useUserStore } from '../store/modules/user';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const loading = ref(true);
const item = ref(null);
const isFavorited = ref(false);
const currentThumb = ref(0);
const currentImage = ref('');
const showLightbox = ref(false);
const allImages = ref<string[]>([]);

const IMG_COLORS = {
  digital: ['#dce8f7', '#c4d8f0', '#b0cce8'],
  books: ['#f5edd6', '#ebe0c4', '#e0d3b2'],
  living: ['#d8f0e0', '#c4e8d0', '#b0e0c0'],
  clothing: ['#e8d8f0', '#dcc4e8', '#d0b0e0'],
  sports: ['#f0e0d0', '#e8d4c0', '#e0c8b0'],
  furniture: ['#e0e8d8', '#d4e0c8', '#c8d8b8'],
  other: ['#e8e8e8', '#dcdcdc', '#d0d0d0'],
};

const getCategoryEmoji = (category) => {
  const map = {
    '数码电子': '💻', '教材书籍': '📚', '生活用品': '🧴',
    '服饰鞋包': '👟', '运动户外': '⚽', '家具家电': '🪑', '其他': '📦',
  };
  return map[category] || '📦';
};

const getGalleryBg = () => {
  const cat = item.value?.categoryName || '其他';
  const colors = IMG_COLORS[cat] || IMG_COLORS.other;
  return colors[0];
};

const getGalleryColors = () => {
  const cat = item.value?.categoryName || '其他';
  return IMG_COLORS[cat] || IMG_COLORS.other;
};

const getConditionText = (condition) => {
  const map = { NEW: '全新', LIKE_NEW: '九五新', GOOD: '九成新', FAIR: '八成新', POOR: '七成新' };
  return map[condition] || condition || '未知';
};

const getDeliveryText = (method) => {
  const map = { LOCAL_DELIVERY: '面交', HOME_DELIVERY: '上门', EXPRESS: '快递', MAIL: '邮寄' };
  return map[method] || method || '面交';
};

const getTimeAgo = (dateStr) => {
  if (!dateStr) return '刚刚';
  const date = new Date(dateStr);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);
  if (minutes < 60) return `${minutes}分钟前`;
  if (hours < 24) return `${hours}小时前`;
  return `${days}天前`;
};

const fetchItemDetail = async () => {
  try {
    loading.value = true;
    const itemId = route.params.id;
    const response = await api.item.getItem(itemId);
    item.value = response.data;
    item.value.eco = item.value.price < 100;
    
    // Parse images
    if (item.value.images) {
      try {
        const images = typeof item.value.images === 'string' ? JSON.parse(item.value.images) : item.value.images;
        item.value.images = Array.isArray(images) ? images.filter(Boolean) : [];
      } catch {
        item.value.images = [];
      }
    } else {
      item.value.images = [];
    }
    
    // Build all images array (coverImage + other images)
    const imgs: string[] = [];
    if (item.value.coverImage) {
      imgs.push(item.value.coverImage);
    }
    item.value.images.forEach((img: string) => {
      if (img && !imgs.includes(img)) {
        imgs.push(img);
      }
    });
    allImages.value = imgs;
    
    // Set current image
    currentImage.value = allImages.value[0] || '';
  } catch (error) {
    ElMessage.error(error.message || '获取物品详情失败');
    router.push('/');
  } finally {
    loading.value = false;
  }
};

const toggleFavorite = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  try {
    if (isFavorited.value) {
      await api.favorite.removeFavorite(route.params.id);
      isFavorited.value = false;
      ElMessage.success('已取消收藏');
    } else {
      await api.favorite.addFavorite(route.params.id);
      isFavorited.value = true;
      ElMessage.success('收藏成功');
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败');
  }
};

const handleContact = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  try {
    const sellerId = item.value?.userId;
    if (!sellerId) {
      ElMessage.error('无法获取卖家信息');
      return;
    }
    const response = await api.chat.createChat(sellerId, item.value.id);
    if (response.code === 200 && response.data) {
      const chatId = response.data.id || response.data;
      router.push(`/user/chat?chatId=${chatId}`);
    }
  } catch (error) {
    ElMessage.error(error.message || '联系卖家失败');
  }
};

const viewSellerProfile = () => {
  if (item.value?.userId) {
    router.push(`/user/${item.value.userId}`);
  }
};

const openLightbox = () => {
  if (allImages.value.length > 0) {
    showLightbox.value = true;
    document.body.style.overflow = 'hidden';
  }
};

const closeLightbox = () => {
  showLightbox.value = false;
  document.body.style.overflow = '';
};

const prevImage = () => {
  currentThumb.value = (currentThumb.value - 1 + allImages.value.length) % allImages.value.length;
  currentImage.value = allImages.value[currentThumb.value];
};

const nextImage = () => {
  currentThumb.value = (currentThumb.value + 1) % allImages.value.length;
  currentImage.value = allImages.value[currentThumb.value];
};

onMounted(() => {
  fetchItemDetail();
});
</script>

<style scoped src="../styles/pages/item-detail.css"></style>
