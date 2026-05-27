<template>
  <div class="item-detail-page">
    <div class="container">
      <div class="breadcrumb">
        <router-link to="/" class="breadcrumb-link">首页</router-link>
        <svg
          width="16"
          height="16"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
        >
          <path d="M9 18L15 12L9 6" />
        </svg>
        <router-link to="/items" class="breadcrumb-link">发现好物</router-link>
        <svg
          width="16"
          height="16"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
        >
          <path d="M9 18L15 12L9 6" />
        </svg>
        <span class="breadcrumb-current">物品详情</span>
      </div>

      <div v-if="loading" class="loading-state">
        <div class="loading-grid">
          <div class="loading-image loading-skeleton"></div>
          <div class="loading-info">
            <div
              class="loading-skeleton"
              style="height: 40px; width: 80%"
            ></div>
            <div
              class="loading-skeleton"
              style="height: 60px; width: 50%; margin-top: 16px"
            ></div>
            <div
              class="loading-skeleton"
              style="height: 120px; margin-top: 24px"
            ></div>
          </div>
        </div>
      </div>

      <div v-else-if="item" class="item-detail">
        <div class="detail-grid">
          <div class="detail-gallery">
            <div class="gallery-main">
              <img
                :src="
                  currentImage ||
                  'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=placeholder%20item&image_size=square'
                "
                :alt="item.title"
                class="main-image"
              />
              <div class="gallery-badges">
                <span class="gallery-badge badge-new" v-if="isNew">新品</span>
                <span class="gallery-badge badge-eco" v-if="item.verified">
                  <svg
                    width="14"
                    height="14"
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
                  </svg>
                  环保认证
                </span>
              </div>
            </div>
            <div class="gallery-thumbs" v-if="item.images?.length > 1">
              <div
                v-for="(img, index) in item.images"
                :key="index"
                class="thumb-item"
                :class="{ active: currentImage === img }"
                @click="currentImage = img" @keydown.enter="currentImage = img" @keydown.space.prevent="currentImage = img" tabindex="0" role="button" :aria-label="`查看第 ${index + 1} 张图片`"
              >
                <img :src="img" :alt="`图片 ${index + 1}`" />
              </div>
            </div>
          </div>

          <div class="detail-info">
            <h1 class="item-title">{{ item.title }}</h1>

            <div class="price-section">
              <div class="price-main">
                <span class="price-symbol">¥</span>
                <span class="price-value">{{ item.price }}</span>
              </div>
              <div class="price-original" v-if="item.originalPrice">
                <span class="original-label">原价</span>
                <span class="original-value">¥{{ item.originalPrice }}</span>
                <span class="discount-badge" v-if="discountPercent">
                  {{ discountPercent }}折
                </span>
              </div>
            </div>

            <div class="meta-section">
              <div class="meta-tags">
                <span class="meta-tag" v-if="item.condition">
                  {{ getConditionText(item.condition) }}
                </span>
                <span class="meta-tag" v-if="item.categoryName">
                  {{ item.categoryName }}
                </span>
                <span class="meta-tag" v-if="item.deliveryMethod">
                  {{ getDeliveryText(item.deliveryMethod) }}
                </span>
                <span class="meta-tag" v-if="item.isBargainAllowed">
                  可议价
                </span>
              </div>
              <div class="meta-stats">
                <span class="meta-stat">
                  <svg
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <path d="M1 12S5 4 12 4s11 8 11 8-4 8-11 8S1 12 1 12z" />
                    <circle cx="12" cy="12" r="3" />
                  </svg>
                  {{ item.viewCount || 0 }} 次浏览
                </span>
                <span class="meta-stat">
                  <svg
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <path
                      d="M20.84 4.61C20.3292 4.09924 19.7228 3.69397 19.0554 3.41708C18.3879 3.14019 17.6725 2.99756 16.95 2.99756C16.2275 2.99756 15.5121 3.14019 14.8446 3.41708C14.1772 3.69397 13.5708 4.09924 13.06 4.61L12 5.67L10.94 4.61C9.9083 3.57831 8.50903 2.99787 7.05 2.99787C5.59096 2.99787 4.19169 3.57831 3.16 4.61C2.1283 5.64169 1.54785 7.04097 1.54785 8.5C1.54785 9.95903 2.1283 11.3583 3.16 12.39L4.22 13.45L12 21.23L19.78 13.45L20.84 12.39C21.3508 11.8792 21.756 11.2728 22.0329 10.6054C22.3098 9.93789 22.4524 9.22248 22.4524 8.5C22.4524 7.77751 22.3098 7.0621 22.0329 6.39464C21.756 5.72718 21.3508 5.12075 20.84 4.61Z"
                    />
                  </svg>
                  {{ item.favoriteCount || 0 }} 收藏
                </span>
              </div>
            </div>

            <div class="divider"></div>

            <div class="description-section">
              <h3 class="section-label">商品描述</h3>
              <p class="description">
                {{ item.description || '暂无详细描述' }}
              </p>
            </div>

            <div class="location-section" v-if="item.location">
              <h3 class="section-label">交易地点</h3>
              <div class="location-info">
                <svg
                  width="18"
                  height="18"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="var(--primary-color)"
                  stroke-width="2"
                >
                  <path
                    d="M21 10C21 17 12 23 12 23C12 23 3 17 3 10C3 7.61305 3.94821 5.32387 5.63604 3.63604C7.32387 1.94821 9.61305 1 12 1C14.3869 1 16.6761 1.94821 18.364 3.63604C20.0518 5.32387 21 7.61305 21 10Z"
                  />
                  <circle cx="12" cy="10" r="3" />
                </svg>
                <span>{{ item.location }}</span>
              </div>
            </div>

            <div class="divider"></div>

            <div class="seller-section">
              <h3 class="section-label">卖家信息</h3>
              <div class="seller-card">
                <div class="seller-avatar">
                  <el-avatar :size="56">
                    {{ item.sellerNickname?.charAt(0) || '卖' }}
                  </el-avatar>
                  <span class="verified-badge" v-if="item.sellerVerified">
                    <svg
                      width="16"
                      height="16"
                      viewBox="0 0 24 24"
                      fill="var(--secondary-color)"
                    >
                      <path
                        d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"
                      />
                    </svg>
                  </span>
                </div>
                <div class="seller-details">
                  <div class="seller-name-row">
                    <span class="seller-name">{{
                      item.sellerNickname || '匿名用户'
                    }}</span>
                    <span class="seller-rating" v-if="item.sellerRating">
                      <svg
                        width="14"
                        height="14"
                        viewBox="0 0 24 24"
                        fill="var(--accent-color)"
                      >
                        <path
                          d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"
                        />
                      </svg>
                      {{ item.sellerRating.toFixed(1) }}
                    </span>
                  </div>
                  <p class="seller-meta">
                    已发布 {{ item.sellerItemsCount || 0 }} 件物品
                  </p>
                </div>
              </div>
            </div>

            <div class="action-section">
              <template v-if="isOwner">
                <el-button
                  size="large"
                  @click="editItem"
                  class="action-btn action-btn-secondary"
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
                      d="M11 4H4C2.89543 4 2 4.89543 2 6V20C2 21.1046 2.89543 22 4 22H18C19.1046 22 20 21.1046 20 20V13"
                    />
                    <path
                      d="M18.5 2.5C19.3284 1.67157 20.6716 1.67157 21.5 2.5C22.3284 3.32843 22.3284 4.67157 21.5 5.5L12 15L8 16L9 12L18.5 2.5Z"
                    />
                  </svg>
                  编辑物品
                </el-button>
                <el-button
                  size="large"
                  @click="offShelf"
                  class="action-btn action-btn-danger"
                >
                  下架物品
                </el-button>
              </template>
              <template v-else>
                <el-button
                  size="large"
                  @click="handleContact"
                  class="action-btn action-btn-secondary"
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
                      d="M21 11.5C21 16.1944 16.9706 20 12 20C10.5 20 9.15 19.6 8 18.9L3 20L4.1 15C3.4 13.85 3 12.5 3 11C3 6.02944 7.02944 2 12 2C16.9706 2 21 6.02944 21 11.5Z"
                    />
                    <circle cx="12" cy="11" r="3" />
                  </svg>
                  联系卖家
                </el-button>
                <el-button
                  size="large"
                  :type="isFavorited ? 'danger' : 'default'"
                  @click="toggleFavorite"
                  class="action-btn"
                  :class="{ 'action-btn-favorited': isFavorited }"
                >
                  <svg
                    width="18"
                    height="18"
                    viewBox="0 0 24 24"
                    :fill="isFavorited ? 'currentColor' : 'none'"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <path
                      d="M20.84 4.61C20.3292 4.09924 19.7228 3.69397 19.0554 3.41708C18.3879 3.14019 17.6725 2.99756 16.95 2.99756C16.2275 2.99756 15.5121 3.14019 14.8446 3.41708C14.1772 3.69397 13.5708 4.09924 13.06 4.61L12 5.67L10.94 4.61C9.9083 3.57831 8.50903 2.99787 7.05 2.99787C5.59096 2.99787 4.19169 3.57831 3.16 4.61C2.1283 5.64169 1.54785 7.04097 1.54785 8.5C1.54785 9.95903 2.1283 11.3583 3.16 12.39L4.22 13.45L12 21.23L19.78 13.45L20.84 12.39C21.3508 11.8792 21.756 11.2728 22.0329 10.6054C22.3098 9.93789 22.4524 9.22248 22.4524 8.5C22.4524 7.77751 22.3098 7.0621 22.0329 6.39464C21.756 5.72718 21.3508 5.12075 20.84 4.61Z"
                    />
                  </svg>
                  {{ isFavorited ? '已收藏' : '收藏' }}
                </el-button>
                <el-button
                  size="large"
                  type="primary"
                  @click="handleBuy"
                  class="action-btn action-btn-primary"
                >
                  <svg
                    width="18"
                    height="18"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <circle cx="12" cy="12" r="10" />
                    <path d="M12 6V12L16 14" />
                  </svg>
                  立即购买
                </el-button>
              </template>
            </div>
          </div>
        </div>

        <div class="reviews-section" v-if="reviews.length > 0 || true">
          <div class="section-header">
            <h2 class="section-title">商品评价</h2>
            <span class="section-count">{{ reviews.length }} 条评价</span>
          </div>
          <div class="reviews-list" v-if="reviews.length > 0">
            <div v-for="review in reviews" :key="review.id" class="review-card">
              <div class="review-header">
                <div class="reviewer-info">
                  <el-avatar :size="36">{{
                    review.reviewerNickname?.charAt(0) || '评'
                  }}</el-avatar>
                  <div class="reviewer-details">
                    <span class="reviewer-name">
                      {{
                        review.isAnonymous
                          ? '匿名用户'
                          : review.reviewerNickname
                      }}
                    </span>
                    <div class="review-rating">
                      <svg
                        v-for="i in 5"
                        :key="i"
                        width="14"
                        height="14"
                        viewBox="0 0 24 24"
                        :fill="
                          i <= review.rating
                            ? 'var(--accent-color)'
                            : 'var(--border-default)'
                        "
                      >
                        <path
                          d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"
                        />
                      </svg>
                    </div>
                  </div>
                </div>
                <span class="review-time">{{
                  formatTime(review.createdAt)
                }}</span>
              </div>
              <p class="review-content">{{ review.content }}</p>
            </div>
          </div>
          <div class="reviews-empty" v-else>
            <svg
              width="48"
              height="48"
              viewBox="0 0 24 24"
              fill="none"
              stroke="var(--text-muted)"
              stroke-width="1.5"
            >
              <path
                d="M21 11.5C21 16.1944 16.9706 20 12 20C10.5 20 9.15 19.6 8 18.9L3 20L4.1 15C3.4 13.85 3 12.5 3 11C3 6.02944 7.02944 2 12 2C16.9706 2 21 6.02944 21 11.5Z"
              />
              <circle cx="12" cy="11" r="3" />
            </svg>
            <p>暂无评价，成为第一个评价的人吧</p>
          </div>
        </div>
      </div>
    </div>

    <el-dialog
      v-model="showBuyDialog"
      title="确认购买"
      width="480px"
      class="buy-dialog"
    >
      <div class="buy-summary">
        <img
          :src="
            item?.coverImage ||
            'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=placeholder&image_size=square'
          "
          class="buy-image"
        />
        <div class="buy-details">
          <h4>{{ item?.title }}</h4>
          <p class="buy-price">¥{{ item?.price }}</p>
        </div>
      </div>
      <el-form :model="orderForm" label-position="top" class="buy-form">
        <el-form-item label="收货人">
          <el-input
            v-model="orderForm.buyerName"
            placeholder="请输入收货人姓名"
          />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input
            v-model="orderForm.buyerPhone"
            placeholder="请输入联系电话"
          />
        </el-form-item>
        <el-form-item label="收货地址">
          <el-input
            v-model="orderForm.buyerAddress"
            type="textarea"
            :rows="3"
            placeholder="请输入详细收货地址"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBuyDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmBuy" class="confirm-btn"
          >确认购买</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import api from '../api';
import { useDictStore } from '../store/dict';

const route = useRoute();
const router = useRouter();

const dictStore = useDictStore();
const loading = ref(true);
const item = ref(null);
const reviews = ref([]);
const isFavorited = ref(false);
const currentImage = ref(null);

const orderForm = ref({
  buyerName: '',
  buyerPhone: '',
  buyerAddress: '',
});
const showBuyDialog = ref(false);

const getStoredUser = () => {
  try {
    return JSON.parse(localStorage.getItem('user') || 'null');
  } catch (error) {
    return null;
  }
};

const parseImages = (images) => {
  if (Array.isArray(images)) {
    return images.filter(Boolean);
  }

  if (typeof images === 'string' && images.trim()) {
    try {
      const parsedImages = JSON.parse(images);
      return Array.isArray(parsedImages) ? parsedImages.filter(Boolean) : [];
    } catch (error) {
      return [images];
    }
  }

  return [];
};

const currentUserId = computed(() => {
  return getStoredUser()?.id || null;
});

const isOwner = computed(() => {
  return (
    currentUserId.value &&
    item.value &&
    item.value.userId === currentUserId.value
  );
});

const isNew = computed(() => {
  if (!item.value?.createdAt) return false;
  const created = new Date(item.value.createdAt);
  const now = new Date();
  const diffDays = (now - created) / (1000 * 60 * 60 * 24);
  return diffDays < 7;
});

const discountPercent = computed(() => {
  if (!item.value?.price || !item.value?.originalPrice) return null;
  const discount = Math.round(
    (item.value.price / item.value.originalPrice) * 10
  );
  return discount;
});

const getConditionText = (condition) => {
  const label = dictStore.getDictLabel('ITEM_CONDITION', condition);
  if (label && label !== condition) return label;
  const fallbackMap = {
    NEW: '全新',
    LIKE_NEW: '九成新',
    GOOD: '八成新',
    FAIR: '七成新',
    POOR: '六成新及以下',
  };
  return fallbackMap[condition] || condition;
};

const getDeliveryText = (method) => {
  const label = dictStore.getDictLabel('DELIVERY_METHOD', method);
  if (label && label !== method) return label;
  const fallbackMap = {
    LOCAL_DELIVERY: '自提',
    HOME_DELIVERY: '上门',
    EXPRESS: '快递',
    MAIL: '邮寄',
  };
  return fallbackMap[method] || method;
};

const formatTime = (time) => {
  if (!time) return '';
  const date = new Date(time);
  const now = new Date();
  const diff = now - date;
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));
  if (days === 0) return '今天';
  if (days === 1) return '昨天';
  if (days < 7) return `${days}天前`;
  return date.toLocaleDateString();
};

const fetchItemDetail = async () => {
  try {
    loading.value = true;
    const itemId = route.params.id;
    const response = await api.item.getItem(itemId);
    item.value = response.data;

    item.value.images = parseImages(item.value.images);
    currentImage.value =
      item.value.coverImage ||
      item.value.images[0] ||
      'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=placeholder%20item&image_size=square';

    if (currentUserId.value) {
      await checkFavoriteStatus();
    }
  } catch (error) {
    ElMessage.error(error.message || '获取物品详情失败');
    router.push('/');
  } finally {
    loading.value = false;
  }
};

const fetchReviews = async () => {
  try {
    const response = await api.review.getReviewsByItem(route.params.id);
    reviews.value = response.data.content || [];
  } catch (error) {
    console.error('获取评价失败', error);
  }
};

const checkFavoriteStatus = async () => {
  try {
    const response = await api.favorite.checkFavorite(route.params.id);
    isFavorited.value = response.data;
  } catch (error) {
    console.error('检查收藏状态失败', error);
  }
};

const toggleFavorite = async () => {
  if (!localStorage.getItem('token')) {
    ElMessage.warning('请先登录');
    localStorage.setItem('redirectPath', route.fullPath);
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

const handleBuy = () => {
  if (!localStorage.getItem('token')) {
    ElMessage.warning('请先登录');
    localStorage.setItem('redirectPath', route.fullPath);
    router.push('/login');
    return;
  }
  showBuyDialog.value = true;
};

const confirmBuy = async () => {
  try {
    await api.order.createOrder({
      itemId: item.value.id,
      ...orderForm.value,
    });
    ElMessage.success('购买成功，请在订单中完成支付');
    showBuyDialog.value = false;
    router.push({
      path: '/user/orders',
      query: {
        status: 'PENDING_PAYMENT',
      },
    });
  } catch (error) {
    ElMessage.error(error.message || '购买失败');
  }
};

const handleContact = async () => {
  if (!localStorage.getItem('token')) {
    ElMessage.warning('请先登录');
    localStorage.setItem('redirectPath', route.fullPath);
    router.push('/login');
    return;
  }
  
  try {
    // 获取卖家ID - 直接使用item.value.userId
    const sellerId = item.value?.userId;
    console.log('Item data:', item.value);
    console.log('Seller ID:', sellerId);
    
    if (!sellerId) {
      ElMessage.error('无法获取卖家信息');
      return;
    }
    
    // 检查是否是自己的商品
    const { userStore } = await import('../store');
    const currentUser = userStore().user;
    console.log('Current user:', currentUser);
    
    if (currentUser && currentUser.id === sellerId) {
      ElMessage.warning('不能联系自己');
      return;
    }
    
    // 创建或获取聊天会话
    console.log('Creating chat with sellerId:', sellerId, 'itemId:', item.value.id);
    const response = await api.chat.createChat(sellerId, item.value.id);
    console.log('Chat response:', response);
    
    if (response.code === 200 && response.data) {
      const chatId = response.data.id || response.data;
      console.log('Chat ID:', chatId);
      // 跳转到聊天页面
      router.push(`/user/chat?chatId=${chatId}`);
    } else {
      ElMessage.error(response.message || '创建聊天失败');
    }
  } catch (error) {
    console.error('联系卖家失败:', error);
    ElMessage.error(error.message || '联系卖家失败');
  }
};

const editItem = () => {
  router.push(`/publish?edit=${item.value.id}`);
};

const offShelf = async () => {
  try {
    await api.item.offShelf(item.value.id);
    ElMessage.success('物品已下架');
    await fetchItemDetail();
  } catch (error) {
    ElMessage.error(error.message || '操作失败');
  }
};

onMounted(() => {
  fetchItemDetail();
  fetchReviews();
});
</script>

<style scoped src="../styles/pages/item-detail.css"></style>
