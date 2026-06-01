<template>
  <div class="items-page">
    <div class="container">
      <!-- Search and Filter Header -->
      <div class="items-header">
        <div class="items-search-mobile">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
            <circle cx="11" cy="11" r="8" />
            <path d="M21 21l-4.35-4.35" />
          </svg>
          <input type="text" placeholder="搜索物品名称、描述…" v-model="keyword" @keyup.enter="handleSearch" />
        </div>
        <button class="filter-btn-mobile" @click="showDrawer = true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
            <path d="M4 21v-7m0-4V3m8 18v-9m0-4V3m8 18v-5m0-4V3M1 14h6M9 8h6M17 16h6" />
          </svg>
          筛选
        </button>
      </div>

      <!-- Category Row -->
      <div class="category-row">
        <div
          v-for="category in categories"
          :key="category.id"
          class="category-chip"
          :class="{ active: activeCategory === category.id }"
          @click="selectCategory(category.id)"
        >
          <div class="category-chip-icon">{{ category.icon }}</div>
          <span class="category-chip-label">{{ category.name }}</span>
        </div>
      </div>

      <!-- Sort Toolbar -->
      <div class="items-toolbar">
        <div class="items-sort">
          <button
            v-for="option in sortOptions"
            :key="option.value"
            class="sort-option"
            :class="{ active: sortBy === option.value }"
            @click="sortBy = option.value; handleFilter()"
          >
            {{ option.label }}
          </button>
        </div>
        <span class="items-count">共 {{ total }} 件</span>
      </div>

      <!-- Items Grid -->
      <div class="items-grid" v-if="items.length > 0">
        <div
          v-for="(item, index) in items"
          :key="item.id"
          class="card card-clickable item-card"
          @click="$router.push(`/item/${item.id}`)"
        >
          <div class="item-card-img">
            <div class="img-placeholder" :style="{ background: getItemColor(item.categoryName, index) }">
              {{ getCategoryEmoji(item.categoryName) }}
            </div>
            <span v-if="item.eco" class="tag tag-eco eco-badge">环保</span>
            <button class="fav-btn" :class="{ liked: likedItems.has(item.id) }" @click.stop="toggleLike(item.id)">
              <svg viewBox="0 0 24 24" :fill="likedItems.has(item.id) ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2">
                <path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z" />
              </svg>
            </button>
          </div>
          <div class="item-card-body">
            <div class="item-card-title">{{ item.title }}</div>
            <div class="item-card-meta">
              <div class="item-card-price">
                <span class="unit">¥</span>{{ item.price?.toLocaleString() }}
                <span v-if="item.originalPrice" class="original">¥{{ item.originalPrice?.toLocaleString() }}</span>
              </div>
            </div>
          </div>
          <div class="item-card-seller">
            <span class="mini-avatar">{{ item.sellerNickname?.charAt(0) || '卖' }}</span>
            <span>{{ item.sellerNickname || '未知卖家' }}</span>
            <span style="margin-left:auto">{{ getTimeAgo(item.createdAt) }}</span>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div class="empty-state" v-else>
        <div class="empty-state-icon">📦</div>
        <div class="empty-state-text">暂无物品</div>
      </div>

      <!-- Pagination -->
      <div class="pagination-wrap" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[12, 24, 36, 48]"
          layout="prev, pager, next, sizes"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- Filter Drawer -->
    <div class="overlay" :class="{ open: showDrawer }" @click="showDrawer = false"></div>
    <div class="drawer" :class="{ open: showDrawer }">
      <div class="drawer-handle"></div>
      <div class="drawer-title">筛选条件</div>
      <div class="form-group">
        <label class="form-label">价格范围</label>
        <div class="form-row">
          <input class="form-input" type="number" placeholder="最低价" v-model="priceMin" />
          <input class="form-input" type="number" placeholder="最高价" v-model="priceMax" />
        </div>
      </div>
      <div class="form-group">
        <label class="form-label">新旧程度</label>
        <div class="radio-group">
          <span
            v-for="option in conditionOptions"
            :key="option.value"
            class="radio-pill"
            :class="{ active: condition === option.value }"
            @click="condition = option.value"
          >
            {{ option.label }}
          </span>
        </div>
      </div>
      <div class="form-group">
        <label class="form-label">交易方式</label>
        <div class="radio-group">
          <span
            v-for="option in deliveryOptions"
            :key="option.value"
            class="radio-pill"
            :class="{ active: deliveryMethod === option.value }"
            @click="deliveryMethod = option.value"
          >
            {{ option.label }}
          </span>
        </div>
      </div>
      <div style="display: flex; gap: 12px; margin-top: 24px;">
        <button class="btn btn-secondary btn-block" @click="resetFilters">重置</button>
        <button class="btn btn-primary btn-block" @click="applyFilters">确认筛选</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useRoute } from 'vue-router';
import { useItemStore } from '../store';
import api from '../api';

const route = useRoute();
const store = useItemStore();

const categories = ref([
  { id: 'all', name: '全部', icon: '🏠' },
  { id: 'digital', name: '数码电子', icon: '💻' },
  { id: 'books', name: '教材书籍', icon: '📚' },
  { id: 'living', name: '生活用品', icon: '🧴' },
  { id: 'clothing', name: '服饰鞋包', icon: '👟' },
  { id: 'sports', name: '运动户外', icon: '⚽' },
  { id: 'furniture', name: '家具家电', icon: '🪑' },
  { id: 'other', name: '其他', icon: '📦' },
]);

const sortOptions = [
  { value: 'newest', label: '最新' },
  { value: 'price-asc', label: '价格↑' },
  { value: 'price-desc', label: '价格↓' },
];

const conditionOptions = [
  { value: '', label: '不限' },
  { value: 'NEW', label: '全新' },
  { value: 'LIKE_NEW', label: '九成新以上' },
  { value: 'GOOD', label: '八成新以上' },
];

const deliveryOptions = [
  { value: '', label: '不限' },
  { value: 'LOCAL_DELIVERY', label: '出售' },
  { value: 'EXCHANGE', label: '交换' },
  { value: 'FREE', label: '免费送' },
];

const activeCategory = ref('all');
const sortBy = ref('newest');
const keyword = ref('');
const condition = ref('');
const deliveryMethod = ref('');
const priceMin = ref('');
const priceMax = ref('');
const currentPage = ref(1);
const pageSize = ref(24);
const total = ref(0);
const items = ref<any[]>([]);
const likedItems = ref(new Set<number>());
const showDrawer = ref(false);

const IMG_COLORS: Record<string, string[]> = {
  digital: ['#dce8f7', '#c4d8f0', '#b0cce8'],
  books: ['#f5edd6', '#ebe0c4', '#e0d3b2'],
  living: ['#d8f0e0', '#c4e8d0', '#b0e0c0'],
  clothing: ['#e8d8f0', '#dcc4e8', '#d0b0e0'],
  sports: ['#f0e0d0', '#e8d4c0', '#e0c8b0'],
  furniture: ['#e0e8d8', '#d4e0c8', '#c8d8b8'],
  other: ['#e8e8e8', '#dcdcdc', '#d0d0d0'],
};

const getItemColor = (category: string, index: number) => {
  const cat = category?.toLowerCase() || 'other';
  const colors = IMG_COLORS[cat] || IMG_COLORS.other;
  return colors[index % colors.length];
};

const getCategoryEmoji = (category: string) => {
  const cat = categories.value.find(c => c.name === category || c.id === category?.toLowerCase());
  return cat?.icon || '📦';
};

const getTimeAgo = (dateStr: string) => {
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

const selectCategory = (id: string) => {
  activeCategory.value = id;
  currentPage.value = 1;
  loadItems();
};

const toggleLike = (id: number) => {
  if (likedItems.value.has(id)) {
    likedItems.value.delete(id);
  } else {
    likedItems.value.add(id);
  }
};

const resetFilters = () => {
  condition.value = '';
  deliveryMethod.value = '';
  priceMin.value = '';
  priceMax.value = '';
  showDrawer.value = false;
  handleFilter();
};

const applyFilters = () => {
  showDrawer.value = false;
  handleFilter();
};

const loadItems = async () => {
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      sortBy: sortBy.value,
      keyword: keyword.value || undefined,
      condition: condition.value || undefined,
      deliveryMethod: deliveryMethod.value || undefined,
    };
    await store.fetchItems(params);
    items.value = store.items || [];
    total.value = store.total || 0;
  } catch (error) {
    console.error('获取物品失败', error);
  }
};

const handleFilter = () => {
  currentPage.value = 1;
  loadItems();
};

const handleSearch = () => {
  currentPage.value = 1;
  loadItems();
};

const handleSizeChange = (size: number) => {
  pageSize.value = size;
  currentPage.value = 1;
  loadItems();
};

const handleCurrentChange = (page: number) => {
  currentPage.value = page;
  loadItems();
};

watch(() => route.query, (newQuery) => {
  if (newQuery.keyword) {
    keyword.value = newQuery.keyword as string;
  }
  if (newQuery.category) {
    activeCategory.value = newQuery.category as string;
  }
  loadItems();
}, { deep: true });

onMounted(() => {
  if (route.query.keyword) {
    keyword.value = route.query.keyword as string;
  }
  if (route.query.category) {
    activeCategory.value = route.query.category as string;
  }
  loadItems();
});
</script>

<style scoped src="../styles/pages/items.css"></style>
