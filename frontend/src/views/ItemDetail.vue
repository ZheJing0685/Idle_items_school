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
          <div class="detail-main-img" role="button" tabindex="0" :aria-label="'查看大图，共 ' + allImages.length + ' 张'" @click="openLightbox" @keydown.enter="openLightbox" @keydown.space.prevent="openLightbox">
            <img v-if="currentImage" :src="currentImage" :alt="item.title" class="main-img" loading="eager" />
            <div v-else class="img-placeholder" :style="{ background: getGalleryBg() }">
              {{ categoryStore.getCategoryIcon(item.categoryName) }}
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
          <div class="detail-thumbs" v-if="allImages.length > 1" role="tablist" aria-label="图片缩略图">
            <div
              v-for="(img, index) in allImages"
              :key="index"
              class="detail-thumb"
              :class="{ active: currentThumb === index }"
              role="tab"
              tabindex="0"
              :aria-selected="currentThumb === index"
              :aria-label="'图片 ' + (index + 1)"
              @click="currentThumb = index; currentImage = img"
              @keydown.enter="currentThumb = index; currentImage = img"
              @keydown.space.prevent="currentThumb = index; currentImage = img"
            >
              <img :src="img" :alt="'图片 ' + (index + 1)" loading="lazy" />
            </div>
          </div>
        </div>

        <!-- Lightbox -->
        <div class="lightbox" v-if="showLightbox" role="dialog" aria-modal="true" aria-label="图片灯箱" ref="lightboxRef" tabindex="-1" @click="closeLightbox" @keydown.escape="closeLightbox">
          <div class="lightbox-content" @click.stop>
            <button class="lightbox-close" @click="closeLightbox" aria-label="关闭灯箱" ref="closeBtnRef">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <path d="M18 6L6 18M6 6l12 12" />
              </svg>
            </button>
            <button class="lightbox-prev" v-if="allImages.length > 1" @click="prevImage" aria-label="上一张图片">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <path d="M15 18l-6-6 6-6" />
              </svg>
            </button>
            <img :src="currentImage" :alt="item.title" class="lightbox-img" loading="lazy" />
            <button class="lightbox-next" v-if="allImages.length > 1" @click="nextImage" aria-label="下一张图片">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <path d="M9 18l6-6-6-6" />
              </svg>
            </button>
            <div class="lightbox-counter" aria-live="polite">{{ currentThumb + 1 }} / {{ allImages.length }}</div>
          </div>
        </div>

        <!-- Info -->
        <div class="detail-info">
          <div>
            <div class="detail-title">{{ item.title }}</div>
            <div class="detail-tags" style="margin-top:10px">
              <span v-if="item.eco" class="tag tag-eco">环保优选</span>
              <span class="tag tag-condition">{{ getConditionText(item.condition) }}</span>
              <span class="tag tag-category">
              {{ categoryStore.getCategoryIcon(item.categoryName) }}
              <template v-if="categoryPath.length > 0">
                <span
                  v-for="(cat, idx) in categoryPath"
                  :key="cat.id"
                  class="category-path-segment"
                >
                  <router-link
                    v-if="idx === categoryPath.length - 1"
                    :to="`/items/category/${encodeURIComponent(cat.name)}`"
                    class="category-path-link"
                  >
                    {{ cat.name }}
                  </router-link>
                  <template v-else>
                    <router-link
                      :to="`/items/category/${encodeURIComponent(cat.name)}`"
                      class="category-path-link"
                    >
                      {{ cat.name }}
                    </router-link>
                    <span class="category-path-sep"> / </span>
                  </template>
                </span>
              </template>
              <template v-else>{{ item.categoryName }}</template>
            </span>
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
              <svg width="18" height="18" viewBox="0 0 24 24" :fill="isFavorited ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z" />
              </svg>
              {{ isFavorited ? '已收藏' : '收藏' }}
            </button>
            <button class="btn btn-secondary btn-lg" @click="handleChat">
              💬 聊天
            </button>
            <button class="btn btn-primary btn-lg" @click="handleBuy">
              🛒 购买
            </button>
          </div>
        </div>
      </div>

      <!-- Related Items -->
      <div class="related-section" v-if="similarItems.length > 0 || sellerItems.length > 0">
        <div class="related-block" v-if="similarItems.length > 0">
          <h3 class="related-title">相似推荐</h3>
          <div class="related-grid">
            <router-link
              v-for="r in similarItems"
              :key="r.id"
              :to="`/item/${r.id}`"
              class="related-card"
            >
              <div class="related-card-img">
                <img v-if="r.coverImage" :src="r.coverImage" :alt="r.title" loading="lazy" />
                <div v-else class="related-img-placeholder">{{ r.title?.charAt(0) || '物' }}</div>
              </div>
              <div class="related-card-body">
                <div class="related-card-title">{{ r.title }}</div>
                <div class="related-card-price">¥{{ r.price?.toLocaleString() }}</div>
                <div class="related-card-seller">{{ r.sellerNickname || '未知卖家' }}</div>
              </div>
            </router-link>
          </div>
        </div>
        <div class="related-block" v-if="sellerItems.length > 0">
          <h3 class="related-title">卖家其他好物</h3>
          <div class="related-grid">
            <router-link
              v-for="r in sellerItems"
              :key="r.id"
              :to="`/item/${r.id}`"
              class="related-card"
            >
              <div class="related-card-img">
                <img v-if="r.coverImage" :src="r.coverImage" :alt="r.title" loading="lazy" />
                <div v-else class="related-img-placeholder">{{ r.title?.charAt(0) || '物' }}</div>
              </div>
              <div class="related-card-body">
                <div class="related-card-title">{{ r.title }}</div>
                <div class="related-card-price">¥{{ r.price?.toLocaleString() }}</div>
                <div class="related-card-seller">{{ r.sellerNickname || '未知卖家' }}</div>
              </div>
            </router-link>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import api from '../api';
import { useUserStore } from '../store/modules/user';
import { getCategoryColorById, getConditionText, getDeliveryText, getTimeAgo } from '../utils/item-helper';
import { useCategoryStore } from '../store/category';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const categoryStore = useCategoryStore();

const loading = ref(true);
const item = ref<any>(null);
const isFavorited = ref(false);
const currentThumb = ref(0);
const currentImage = ref('');
const showLightbox = ref(false);
const allImages = ref([] as string[]);
const lightboxRef = ref<HTMLElement | null>(null);
const closeBtnRef = ref<HTMLElement | null>(null);
const similarItems = ref<any[]>([]);
const sellerItems = ref<any[]>([]);

/** 当前物品的分类路径（一级 > 二级） */
const categoryPath = computed(() => {
  if (!item.value?.categoryId) return [];
  return categoryStore.getCategoryPath(item.value.categoryId);
});

const getGalleryBg = () => {
  const id = item.value?.categoryId ?? 0;
  return getCategoryColorById(id);
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

    // Fetch related items
    fetchRelatedItems(itemId);
  } catch (error) {
    ElMessage.error(error.message || '获取物品详情失败');
    router.push('/');
  } finally {
    loading.value = false;
  }
};

const fetchRelatedItems = async (itemId: number | string) => {
  try {
    const res = await api.item.getRelatedItems(itemId);
    if (res.data) {
      similarItems.value = (res.data.similarItems || []).slice(0, 6);
      sellerItems.value = (res.data.sellerItems || []).slice(0, 4);
    }
  } catch {
    // Non-critical data, silent fail
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

const handleChat = async () => {
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

/** 购买弹窗 - 分字段输入收货信息 */
const handleBuy = async () => {
  if (!userStore.isLoggedIn) {
    // 将购买意图存入 sessionStorage，登录后恢复
    sessionStorage.setItem('pendingPurchase', JSON.stringify({ itemId: item.value?.id }));
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  try {
    const { ElMessageBox } = await import('element-plus');

    // 使用自定义 HTML 实现分字段输入
    const htmlContent = `
      <div style="display:flex;flex-direction:column;gap:16px;">
        <div>
          <label style="font-size:13px;font-weight:600;color:var(--text-primary,#333);display:block;margin-bottom:4px;">收货人姓名</label>
          <input id="buyer-name-input" class="buyer-field" placeholder="请输入收货人姓名" style="width:100%;padding:10px 12px;border:1.5px solid var(--border,#e0e0e0);border-radius:8px;font-size:14px;box-sizing:border-box;outline:none;transition:border-color .15s;" />
        </div>
        <div>
          <label style="font-size:13px;font-weight:600;color:var(--text-primary,#333);display:block;margin-bottom:4px;">联系电话</label>
          <input id="buyer-phone-input" class="buyer-field" placeholder="请输入手机号" style="width:100%;padding:10px 12px;border:1.5px solid var(--border,#e0e0e0);border-radius:8px;font-size:14px;box-sizing:border-box;outline:none;transition:border-color .15s;" />
        </div>
        <div>
          <label style="font-size:13px;font-weight:600;color:var(--text-primary,#333);display:block;margin-bottom:4px;">收货地址</label>
          <input id="buyer-address-input" class="buyer-field" placeholder="请输入详细地址（校区、宿舍楼等）" style="width:100%;padding:10px 12px;border:1.5px solid var(--border,#e0e0e0);border-radius:8px;font-size:14px;box-sizing:border-box;outline:none;transition:border-color .15s;" />
        </div>
      </div>
    `;

    await ElMessageBox({
      title: '确认购买',
      message: htmlContent,
      dangerouslyUseHTMLString: true,
      confirmButtonText: '确认下单',
      cancelButtonText: '取消',
      closeOnClickModal: false,
      beforeClose: async (action: string, instance: any, done: () => void) => {
        if (action === 'confirm') {
          const nameEl = document.getElementById('buyer-name-input') as HTMLInputElement;
          const phoneEl = document.getElementById('buyer-phone-input') as HTMLInputElement;
          const addrEl = document.getElementById('buyer-address-input') as HTMLInputElement;

          const name = nameEl?.value?.trim();
          const phone = phoneEl?.value?.trim();
          const address = addrEl?.value?.trim();

          if (!name) { ElMessage.warning('请输入收货人姓名'); return; }
          if (!phone) { ElMessage.warning('请输入联系电话'); return; }
          if (!address) { ElMessage.warning('请输入收货地址'); return; }
          if (!/^1[3-9]\d{9}$/.test(phone)) { ElMessage.warning('请输入正确的手机号'); return; }

          try {
            const response = await api.order.createOrder({
              itemId: item.value!.id,
              buyerName: name,
              buyerPhone: phone,
              buyerAddress: address,
            });
            if (response.code === 200) {
              ElMessage.success('下单成功');
              router.push('/user/orders');
              done();
            } else {
              ElMessage.error(response.message || '下单失败');
            }
          } catch (err: any) {
            ElMessage.error(err.message || '下单失败');
          }
        } else {
          done();
        }
      },
    });
  } catch (error) {
    // 取消操作不处理
  }
};

const viewSellerProfile = () => {
  if (item.value?.userId) {
    router.push(`/seller/${item.value.userId}`);
  }
};

const openLightbox = () => {
  if (allImages.value.length > 0) {
    showLightbox.value = true;
    document.body.style.overflow = 'hidden';
    nextTick(() => {
      closeBtnRef.value?.focus();
    });
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

<style scoped>
.category-path-link {
  color: inherit;
  text-decoration: none;
  transition: color 0.2s;
}
.category-path-link:hover {
  color: var(--accent, #4f46e5);
  text-decoration: underline;
}
.category-path-sep {
  margin: 0 2px;
  opacity: 0.5;
}

.related-section {
  margin-top: 48px;
  padding-top: 32px;
  border-top: 1px solid var(--border, #e5e7eb);
}

.related-block {
  margin-bottom: 32px;
}

.related-title {
  font-size: 1.125rem;
  font-weight: 600;
  margin-bottom: 16px;
  color: var(--text-primary, #111827);
}

.related-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 16px;
}

.related-card {
  text-decoration: none;
  color: inherit;
  border-radius: 10px;
  overflow: hidden;
  background: var(--surface, #fff);
  border: 1px solid var(--border, #e5e7eb);
  transition: transform 0.15s, box-shadow 0.15s;
}

.related-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.related-card-img {
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
  background: var(--surface-alt, #f3f4f6);
}

.related-card-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.related-img-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  color: var(--text-tertiary, #9ca3af);
  background: var(--surface-alt, #f3f4f6);
}

.related-card-body {
  padding: 10px 12px 12px;
}

.related-card-title {
  font-size: 0.8125rem;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--text-primary, #111827);
}

.related-card-price {
  font-size: 1rem;
  font-weight: 700;
  color: var(--accent, #4f46e5);
  margin-top: 4px;
}

.related-card-seller {
  font-size: 0.75rem;
  color: var(--text-tertiary, #9ca3af);
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
