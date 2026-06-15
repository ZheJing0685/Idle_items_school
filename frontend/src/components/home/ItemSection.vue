<template>
  <section class="home-section">
    <div class="container">
      <div class="section-header">
        <h2 class="section-title">{{ title }}</h2>
        <router-link to="/items" class="section-more">查看更多 →</router-link>
      </div>

      <!-- Skeleton Loading -->
      <div v-if="loading" class="items-grid" role="list" :aria-label="title + '加载中'">
        <div v-for="n in 8" :key="'skeleton-' + title + '-' + n" class="card item-card skeleton-card">
          <div class="item-card-img skeleton-img"></div>
          <div class="item-card-body">
            <div class="skeleton-line skeleton-title"></div>
            <div class="skeleton-line skeleton-price"></div>
          </div>
          <div class="item-card-seller">
            <div class="skeleton-line skeleton-seller"></div>
          </div>
        </div>
      </div>

      <!-- Items Grid -->
      <div v-else class="items-grid" role="list" :aria-label="title + '列表'">
        <div
          v-for="(item, index) in items"
          :key="item.id"
          class="card card-clickable item-card"
          role="button"
          tabindex="0"
          :aria-label="'查看 ' + item.title"
          @click="$router.push(`/item/${item.id}`)"
          @keydown.enter="$router.push(`/item/${item.id}`)"
          @keydown.space.prevent="$router.push(`/item/${item.id}`)"
        >
          <div class="item-card-img">
            <img v-if="item.coverImage" :src="item.coverImage" :alt="item.title" class="item-img" loading="lazy" />
            <div v-else class="img-placeholder" :style="{ background: getItemColor(item.category, index) }">
              {{ categoryStore.getCategoryIcon(item.categoryName) }}
            </div>
            <span class="tag tag-category category-badge" v-if="getCategoryPathLabel(item)">{{ getCategoryPathLabel(item) }}</span>
            <span v-if="item.eco" class="tag tag-eco eco-badge">环保</span>
            <button
              class="fav-btn"
              :class="{ liked: likedItems.has(item.id) }"
              :aria-label="likedItems.has(item.id) ? '取消收藏' : '收藏'"
              @click.stop="toggleLike(item.id)"
            >
              <svg viewBox="0 0 24 24" :fill="likedItems.has(item.id) ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z" />
              </svg>
            </button>
          </div>
          <div class="item-card-body">
            <div class="item-card-title">{{ item.title }}</div>
            <div class="item-card-meta">
              <div class="item-card-price">
                <span class="unit">¥</span>{{ item.price.toLocaleString() }}
                <span v-if="item.originalPrice" class="original">¥{{ item.originalPrice.toLocaleString() }}</span>
              </div>
            </div>
          </div>
          <div class="item-card-seller">
            <span class="mini-avatar">{{ item.sellerName?.charAt(0) || '卖' }}</span>
            <span>{{ item.sellerName }}</span>
            <span style="margin-left:auto">{{ item.time || '刚刚' }}</span>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { useCategoryStore } from '../../store/category';

interface ItemSectionItem {
  id: number
  title: string
  price: number
  originalPrice?: number
  coverImage?: string
  categoryId?: number
  category: string
  categoryName?: string
  sellerName?: string
  eco?: boolean
  time?: string
  [key: string]: unknown
}

defineProps<{
  title: string
  items: ItemSectionItem[]
  loading?: boolean
  likedItems: Set<number>
}>();

const emit = defineEmits<{
  toggleLike: [id: number]
}>();

const categoryStore = useCategoryStore();

/** 获取分类路径标签文本，格式：'一级 > 二级' 或仅 '一级' */
const getCategoryPathLabel = (item: ItemSectionItem): string => {
  if (!item.categoryId) return '';
  const path = categoryStore.getCategoryPath(item.categoryId);
  if (path.length === 0) return item.categoryName || '';
  const names = path.map(c => c.name);
  return names.join(' > ');
};

const IMG_COLORS: Record<string, string[]> = {
  digital: ['#dce8f7', '#c4d8f0', '#b0cce8'],
  books: ['#f5edd6', '#ebe0c4', '#e0d3b2'],
  living: ['#d8f0e0', '#c4e8d0', '#b0e0c0'],
  clothing: ['#e8d8f0', '#dcc4e8', '#d0b0e0'],
  sports: ['#f0e0d0', '#e8d4c0', '#e0c8b0'],
  furniture: ['#e0e8d8', '#d4e0c8', '#c8d8b8'],
  other: ['#e8e8e8', '#dcdcdc', '#d0d0d0'],
};

const getItemColor = (category: string, index: number): string => {
  const colors = IMG_COLORS[category] || IMG_COLORS.other;
  return colors[index % colors.length];
};

const toggleLike = (id: number): void => {
  emit('toggleLike', id);
};
</script>

<style scoped>
@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

.skeleton-card {
  pointer-events: none;
}

.skeleton-img {
  height: 180px;
  background: linear-gradient(90deg, var(--surface-alt, #f0f0f0) 25%, var(--surface-hover, #e8e8e8) 50%, var(--surface-alt, #f0f0f0) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s ease-in-out infinite;
  border-radius: 8px 8px 0 0;
}

.skeleton-line {
  height: 14px;
  background: linear-gradient(90deg, var(--surface-alt, #f0f0f0) 25%, var(--surface-hover, #e8e8e8) 50%, var(--surface-alt, #f0f0f0) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s ease-in-out infinite;
  border-radius: 4px;
  margin: 8px 0;
}

.skeleton-title { width: 70%; height: 16px; }
.skeleton-price { width: 40%; height: 18px; }
.skeleton-seller { width: 50%; height: 12px; }

.category-badge {
  position: absolute;
  bottom: 8px;
  left: 8px;
  background: rgba(0, 0, 0, 0.55);
  backdrop-filter: blur(8px);
  color: #fff;
  font-size: 0.75rem;
  padding: 2px 10px;
  border-radius: 8px;
  white-space: nowrap;
  max-width: calc(100% - 16px);
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Reduced Motion — 动效降级 */
@media (prefers-reduced-motion: reduce) {
  .skeleton-img { animation: none; }
  .skeleton-line { animation: none; }
}
</style>
