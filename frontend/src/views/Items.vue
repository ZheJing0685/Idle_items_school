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
          <input type="text" placeholder="搜索物品名称、描述…" v-model="keyword" @keyup.enter="handleSearch" autocomplete="off" aria-label="搜索校园好物" />
        </div>
        <button class="filter-btn-mobile" @click="showDrawer = true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
            <path d="M4 21v-7m0-4V3m8 18v-9m0-4V3m8 18v-5m0-4V3M1 14h6M9 8h6M17 16h6" />
          </svg>
          筛选
        </button>
      </div>

      <!-- Search Category Suggestions (M-4) -->
      <div class="search-suggestions" v-if="keyword && keyword.trim() && searchCategorySuggestions.length > 0">
        <div class="suggestion-label">猜你想找分类</div>
        <div class="suggestion-chips">
          <span
            v-for="cat in searchCategorySuggestions"
            :key="cat.id"
            class="suggestion-chip"
            @click="selectCategorySuggestion(cat)"
            role="button"
            tabindex="0"
            :aria-label="'分类：' + cat.name"
          >
            <span class="suggestion-chip-icon">{{ cat.icon || '📂' }}</span>
            {{ cat.name }}
          </span>
        </div>
      </div>

      <!-- Level 1 Category Row -->
      <!-- 骨架屏 -->
      <div class="category-row" v-if="!categoryStore.loaded && !categoryStore.error" aria-label="分类加载中">
        <div v-for="n in 6" :key="n" class="category-chip-skeleton">
          <div class="skeleton-icon skeleton-shimmer" />
          <div class="skeleton-label skeleton-shimmer" />
        </div>
      </div>
      <!-- 错误 & 降级 -->
      <div class="category-row category-row-error" v-if="categoryStore.error && !categoryStore.loaded">
        <div class="category-chip active">
          <div class="category-chip-icon">🏠</div>
          <span class="category-chip-label">全部</span>
        </div>
        <button class="category-retry-btn" @click="categoryStore.fetchAll(true)">
          <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M1 4v6h6M23 20v-6h-6" />
            <path d="M20.49 9A9 9 0 0 0 5.64 5.64L1 10m22 4l-4.64 4.36A9 9 0 0 1 3.51 15" />
          </svg>
          加载失败，重试
        </button>
      </div>
      <!-- 真实一级分类 Chip -->
      <div class="category-row" v-else role="tablist" aria-label="商品一级分类筛选">
        <div
          v-for="category in level1Categories"
          :key="category.id"
          class="category-chip"
          :class="{ active: activeCategory === category.id }"
          role="tab"
          tabindex="0"
          :aria-selected="activeCategory === category.id"
          :aria-label="'分类：' + category.name"
          @click="selectCategory(category.id)"
          @keydown.enter="selectCategory(category.id)"
          @keydown.space.prevent="selectCategory(category.id)"
        >
          <div class="category-chip-icon">{{ category.icon }}</div>
          <span class="category-chip-label">{{ category.name }}</span>
        </div>
      </div>

      <!-- Level 2 Category Row（点击一级后展示） -->
      <div class="category-row sub-category-row" v-if="subCategories.length > 0" role="tablist" aria-label="商品二级分类筛选">
        <div
          v-for="sub in subCategories"
          :key="sub.id"
          class="category-chip sub-chip"
          :class="{ active: activeSubCategory === sub.id }"
          role="tab"
          tabindex="0"
          :aria-selected="activeSubCategory === sub.id"
          :aria-label="'子分类：' + sub.name"
          @click="selectSubCategory(sub.id)"
          @keydown.enter="selectSubCategory(sub.id)"
          @keydown.space.prevent="selectSubCategory(sub.id)"
        >
          <span class="sub-chip-label">{{ sub.name }}</span>
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
            :aria-pressed="sortBy === option.value"
            @click="sortBy = option.value; handleFilter()"
          >
            {{ option.label }}
          </button>
        </div>
        <span class="items-count" aria-live="polite">共 {{ total }} 件</span>
      </div>

      <!-- Items Grid -->
      <div class="items-grid" v-if="loadingItems">
        <div v-for="n in 12" :key="'skel-' + n" class="card item-card skeleton-card">
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
      <div class="items-grid" v-else-if="items.length > 0" role="list" aria-label="闲置物品列表">
        <div
          v-for="item in items"
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
            <div class="img-placeholder" :style="{ background: getItemColor(item) }">
              {{ categoryStore.getCategoryIcon(item.categoryName) }}
            </div>
            <span v-if="item.eco" class="tag tag-eco eco-badge">环保</span>
            <button class="fav-btn" :class="{ liked: likedItems.has(item.id) }" @click.stop="toggleLike(item.id)" :aria-label="likedItems.has(item.id) ? '取消收藏' : '收藏'">
              <svg viewBox="0 0 24 24" :fill="likedItems.has(item.id) ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
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
      <div class="empty-state" v-else aria-live="assertive">
        <template v-if="keyword.trim()">
          <div class="empty-state-icon">🔍</div>
          <div class="empty-state-title">没有匹配"{{ keyword }}"的物品</div>
          <div class="empty-state-desc">试试缩短或更换关键词，或者浏览全部分类看看</div>
          <button class="btn btn-secondary" @click="keyword = ''; handleSearch()" style="min-width: 140px;">清除搜索</button>
        </template>
        <template v-else-if="activeCategory !== 'all'">
          <div class="empty-state-icon">📭</div>
          <div class="empty-state-title">「{{ getCurrentCategoryName() }}」暂无物品</div>
          <div class="empty-state-desc">该分类还没有上架物品，看看其他分类吧</div>
          <button class="btn btn-secondary" @click="clearCategoryFilter" style="min-width: 140px;">查看全部物品</button>
        </template>
        <template v-else>
          <div class="empty-state-icon">📦</div>
          <div class="empty-state-title">这里什么都没有</div>
          <div class="empty-state-desc">还没有人发布闲置物品，快来当第一个吧！</div>
          <button class="btn btn-primary" @click="$router.push('/publish')" style="min-width: 140px;">发布物品</button>
        </template>
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
      <div class="drawer-title">{{ drawerTitle }}</div>
      <div class="drawer-category-tag" v-if="activeCategory !== 'all'">
        <span class="tag-icon">{{ getCurrentCategoryIcon() }}</span>
        <span class="tag-label">{{ getCurrentCategoryName() }}</span>
        <button class="tag-clear" @click="clearCategoryFilter" aria-label="清除分类筛选">×</button>
      </div>
      <!-- Subcategories Panel -->
      <div class="drawer-subcategories" v-if="hasSubCategories">
        <div class="subcategory-header" role="button" tabindex="0"
             aria-controls="drawer-subcategory-body"
             :aria-expanded="subcategoriesExpanded"
             @click="subcategoriesExpanded = !subcategoriesExpanded"
             @keydown.enter="subcategoriesExpanded = !subcategoriesExpanded">
          <span>细分分类</span>
          <svg viewBox="0 0 24 24" width="16" height="16"
               :style="{ transform: subcategoriesExpanded ? 'rotate(180deg)' : 'rotate(0deg)' }">
            <path d="M6 9l6 6 6-6" stroke="currentColor" stroke-width="2" fill="none"/>
          </svg>
        </div>
        <div id="drawer-subcategory-body" class="subcategory-body" v-show="subcategoriesExpanded">
          <span v-for="sub in subCategories" :key="sub.id"
                class="radio-pill subcategory-pill"
                :class="{ active: activeSubCategory === sub.id }"
                @click="selectSubCategory(sub.id)">
            {{ sub.name }}
          </span>
        </div>
      </div>
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
            role="radio"
            tabindex="0"
            :aria-checked="condition === option.value"
            :aria-label="'新旧程度：' + option.label"
            @click="condition = option.value"
            @keydown.enter="condition = option.value"
            @keydown.space.prevent="condition = option.value"
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
            role="radio"
            tabindex="0"
            :aria-checked="deliveryMethod === option.value"
            :aria-label="'交易方式：' + option.label"
            @click="deliveryMethod = option.value"
            @keydown.enter="deliveryMethod = option.value"
            @keydown.space.prevent="deliveryMethod = option.value"
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
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute } from 'vue-router';
import { useItemStore } from '../store';
import { useCategoryStore } from '../store/category';
import { getCategoryColorById, getTimeAgo } from '../utils/item-helper';
import { logger } from '@/utils/logger';

const route = useRoute();
const store = useItemStore();
const categoryStore = useCategoryStore();

/** 用于展示的一级分类列表 */
const level1Categories = computed(() => {
  const allEntry = { id: 'all' as const, name: '全部', icon: '🏠' };
  const apiCategories = categoryStore.categories
    .filter(c => c.level === 1)
    .map(c => ({
      id: c.id as number,
      name: c.name,
      icon: c.icon || categoryStore.getCategoryIcon(c.name),
    }));
  return [allEntry, ...apiCategories];
});

/** 当前选中一级分类下的二级分类列表 */
const subCategories = computed(() => {
  if (activeCategory.value === 'all') return [];
  const parent = categoryStore.categoryTree.find(c => c.id === activeCategory.value);
  if (!parent?.children?.length) return [];
  return parent.children.map(c => ({
    id: c.id as number,
    name: c.name,
    icon: c.icon || categoryStore.getCategoryIcon(c.name),
  }));
});

const sortOptions = [
  { value: 'createdAt', label: '最新' },
  { value: 'priceAsc', label: '价格↑' },
  { value: 'priceDesc', label: '价格↓' },
  { value: 'viewCount', label: '浏览量' },
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

const activeCategory = ref<number | 'all'>('all');
const activeSubCategory = ref<number | null>(null);
const subcategoriesExpanded = ref(true);
const hasSubCategories = computed(() => subCategories.value.length > 0);
const sortBy = ref('createdAt');
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
const loadingItems = ref(false);
const searchCategorySuggestions = computed(() => {
  if (!keyword.value || keyword.value.trim().length === 0) return [];
  const kw = keyword.value.toLowerCase().trim();
  return categoryStore.flatCategories
    .filter(c => c.name.toLowerCase().includes(kw))
    .slice(0, 3);
});

const selectCategorySuggestion = (cat: any) => {
  if (cat.parentId != null && cat.level === 2) {
    activeCategory.value = cat.parentId;
    activeSubCategory.value = cat.id;
  } else {
    activeCategory.value = cat.id;
    activeSubCategory.value = null;
  }
  keyword.value = '';
  currentPage.value = 1;
  loadItems();
};

const getItemColor = (item: any) => {
  return getCategoryColorById(item.categoryId ?? 1);
};

const selectCategory = (id: number | 'all') => {
  activeCategory.value = id;
  activeSubCategory.value = null; // 切换一级时清除二级选择
  currentPage.value = 1;
  loadItems();
};

const selectSubCategory = (id: number) => {
  activeSubCategory.value = id;
  currentPage.value = 1;
  loadItems();
};

const drawerTitle = computed(() => {
  if (activeCategory.value === 'all') return '筛选条件';
  const name = getCurrentCategoryName();
  return `在「${name}」中筛选`;
});

function getCurrentCategoryIcon(): string {
  const cat = categoryStore.flatCategories.find(c => c.id === activeCategory.value);
  return cat?.icon || categoryStore.getCategoryIcon(cat?.name || '') || '📂';
}

function getCurrentCategoryName(): string {
  const cat = categoryStore.flatCategories.find(c => c.id === activeCategory.value);
  return cat?.name || '';
}

const clearCategoryFilter = () => {
  activeCategory.value = 'all';
  activeSubCategory.value = null;
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
  loadingItems.value = true;
  try {
    const params: Record<string, unknown> = {
      page: currentPage.value,
      size: pageSize.value,
      sortBy: sortBy.value,
      keyword: keyword.value || undefined,
      condition: condition.value || undefined,
      deliveryMethod: deliveryMethod.value || undefined,
    };
    if (activeSubCategory.value !== null) {
      // 选中二级分类时，只查该二级
      params.categoryId = activeSubCategory.value;
    } else if (activeCategory.value !== 'all') {
      // 选中一级分类时，查该一级+所有子级（后端自动 IN 展开）
      params.categoryId = activeCategory.value;
    }
    await store.fetchItems(params);
    items.value = store.items || [];
    total.value = store.total || 0;
  } catch (error) {
    logger.error('获取物品失败', error);
  } finally {
    loadingItems.value = false;
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

/** 根据分类名称查找分类 ID */
function resolveCategoryByName(name: string): { categoryId: number | 'all'; subCategoryId: number | null } {
  const decodedName = decodeURIComponent(name);
  const found = categoryStore.flatCategories.find(c => c.name === decodedName);
  if (!found) return { categoryId: 'all', subCategoryId: null };
  if (found.level === 1) {
    return { categoryId: found.id, subCategoryId: null };
  }
  // 二级分类：选中其父级 + 自身
  return { categoryId: found.parentId ?? found.id, subCategoryId: found.id };
}

/** 更新 meta.title 为当前分类名称 */
function updateTitle(categoryName: string | undefined) {
  const brand = '闲置物品交易平台';
  if (categoryName) {
    document.title = `${decodeURIComponent(categoryName)} - ${brand}`;
  } else if (route.name === 'ItemsByCategory') {
    document.title = `分类浏览 - ${brand}`;
  }
}

/** 统一的路由参数处理 */
function applyRouteParams() {
  // 优先处理路径参数（SEO URL）
  const categoryName = route.params.categoryName as string | undefined;
  if (categoryName) {
    const resolved = resolveCategoryByName(categoryName);
    activeCategory.value = resolved.categoryId;
    activeSubCategory.value = resolved.subCategoryId;
    updateTitle(categoryName);
  } else {
    // 兜底：查询参数（传统方式）
    if (route.query.keyword) {
      keyword.value = route.query.keyword as string;
    }
    if (route.query.category) {
      const cat = route.query.category as string;
      activeCategory.value = cat === 'all' ? 'all' : Number(cat);
      activeSubCategory.value = null;
    }
    if (route.query.subCategory) {
      activeSubCategory.value = Number(route.query.subCategory);
    }
    updateTitle(undefined);
  }
}

/** 监听查询参数变化 */
watch(() => route.query, () => {
  applyRouteParams();
  if (!route.params.categoryName) {
    loadItems();
  }
}, { deep: true });

/** 监听路径参数变化（SEO URL） */
watch(() => route.params, () => {
  if (route.params.categoryName) {
    applyRouteParams();
    loadItems();
  }
}, { deep: true });

onMounted(() => {
  categoryStore.fetchAll().then(() => {
    applyRouteParams();
    loadItems();
  });
});
</script>

<style scoped src="../styles/pages/items.css"></style>

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

/* Sub-category row */
.sub-category-row {
  margin-top: -8px;
  padding-bottom: 4px;
}

.sub-chip {
  font-size: var(--text-sm, 0.875rem);
  padding: 4px 14px;
  background: var(--surface-alt, #f5f5f5);
  border-radius: 16px;
  color: var(--text-secondary, #666);
  transition: all 0.2s ease;
  cursor: pointer;
  white-space: nowrap;
}

.sub-chip:hover {
  background: var(--surface-hover, #e8e8e8);
}

.sub-chip.active {
  background: var(--accent, #4f46e5);
  color: #fff;
  font-weight: 500;
}

.sub-chip-label {
  font-size: 0.8125rem;
}

/* Search Suggestions (M-4) */
.search-suggestions {
  margin: -8px 0 4px;
  padding: 10px 14px;
  background: var(--bg-surface, #fff);
  border: 1px solid var(--border-subtle, #e5e5e5);
  border-radius: 12px;
  box-shadow: var(--shadow-sm, 0 1px 3px rgba(0,0,0,0.08));
}

.suggestion-label {
  font-size: 12px;
  color: var(--text-muted, #999);
  margin-bottom: 8px;
  font-weight: 500;
}

.suggestion-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.suggestion-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 5px 12px;
  border-radius: 9999px;
  border: 1.5px solid var(--border-default, #ddd);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s ease;
  background: var(--bg-surface, #fff);
  color: var(--text-primary, #333);
}

.suggestion-chip:hover {
  border-color: var(--primary-color, #4f46e5);
  background: var(--primary-alpha-10, rgba(79,70,229,0.06));
  color: var(--primary-color, #4f46e5);
}

.suggestion-chip-icon {
  font-size: 15px;
}
</style>
