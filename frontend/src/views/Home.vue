<template>
  <div class="home">
    <!-- Hero Banner -->
    <section class="hero">
      <div class="container">
        <div class="hero-content">
          <span class="hero-tag">🌱 校园绿色行动</span>
          <h1 class="hero-title">让闲置流动<br>让校园更绿</h1>
          <p class="hero-description">在这里，每一件闲置物品都找到新主人，减少浪费，传递价值。</p>
          <div class="hero-stats">
            <div class="hero-stat-item">
              <strong>2,847</strong>
              件物品在流转
            </div>
            <div class="hero-stat-item">
              <strong>{{ carbonStatsLoaded ? carbonStats.monthlySavingKg + 'kg' : '--' }}</strong>
              本月减碳量
            </div>
            <div class="hero-stat-item">
              <strong>{{ carbonStatsLoaded ? carbonStats.participantCount : '--' }}</strong>
              位同学参与
            </div>
          </div>
        </div>
        <svg class="hero-deco" viewBox="0 0 200 200" fill="none">
          <circle cx="100" cy="100" r="80" stroke="currentColor" stroke-width="1.5" opacity="0.3" />
          <path d="M100 40C70 40 50 70 50 100s20 60 50 60 50-30 50-60S130 40 100 40z" stroke="currentColor" stroke-width="1.5" opacity="0.2" />
          <path d="M60 100h80M100 60v80" stroke="currentColor" stroke-width="1" opacity="0.15" />
        </svg>
      </div>
    </section>

    <!-- Error Alert -->
    <div class="container" v-if="error">
      <el-alert
        :title="error"
        type="error"
        show-icon
        closable
        @close="error = ''"
        style="margin-top: 16px;"
      />
    </div>

    <!-- Categories -->
    <section class="categories">
      <div class="container">
        <!-- 骨架屏 -->
        <div class="category-row" v-if="!categoryStore.loaded && !categoryStore.error">
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
        <!-- 真实分类 Chip (M-3: hover/click 展开二级分类) -->
        <div class="category-row" v-else>
          <div
            v-for="category in displayCategories"
            :key="'chip-' + category.id"
            class="category-chip-wrapper"
            :data-chip-id="category.id"
            @mouseenter="onPopoverEnter(category.id)"
            @mouseleave="onPopoverLeave"
          >
            <div
              class="category-chip"
              :class="{ active: activeCategory === category.id }"
              role="button"
              tabindex="0"
              :aria-label="'筛选分类：' + category.name"
              :aria-pressed="activeCategory === category.id"
              @click="handleChipClick(category)"
              @keydown.enter="handleChipClick(category)"
              @keydown.space.prevent="handleChipClick(category)"
            >
              <div class="category-chip-icon">{{ category.icon }}</div>
              <span class="category-chip-label">{{ category.name }}</span>
            </div>
            <!-- 二级分类浮层 (Teleport 到 body 避免被 overflow 裁剪) -->
            <Teleport to="body">
              <Transition name="popover">
                <div
                  v-if="openPopoverId === category.id && category.id !== 'all' && getSubCategories(category.id as number).length > 0"
                  ref="popoverRef"
                  class="subcategory-popover"
                  role="menu"
                  :style="popoverStyle"
                  :aria-label="category.name + '的子分类'"
                  @mouseenter="onPopoverEnter(category.id)"
                  @mouseleave="onPopoverLeave"
                >
                  <div class="popover-arrow"></div>
                  <div
                    v-for="sub in getSubCategories(category.id as number)"
                    :key="sub.id"
                    class="popover-chip"
                    role="menuitem"
                    tabindex="0"
                    :aria-label="'子分类：' + sub.name"
                    @click.stop="navigateToSubCategory(sub)"
                    @keydown.enter.stop="navigateToSubCategory(sub)"
                    @keydown.space.prevent.stop="navigateToSubCategory(sub)"
                  >
                    <span class="popover-chip-icon">{{ sub.icon || '▸' }}</span>
                    <span class="popover-chip-label">{{ sub.name }}</span>
                  </div>
                </div>
              </Transition>
            </Teleport>
          </div>
        </div>
      </div>
    </section>

    <!-- Eco Stats Bar -->
    <section class="eco-section">
      <div class="container">
        <div class="eco-bar">
          <span class="eco-bar-icon">♻️</span>
          <div class="eco-bar-text">
            本月校园交易已减少 <strong>{{ carbonStatsLoaded ? carbonStats.monthlySavingKg + 'kg' : '--' }}</strong> 碳排放，相当于种植 <strong>{{ carbonStatsLoaded ? carbonStats.treeEquivalent : '--' }}</strong> 棵树的年吸碳量。每一次交易都是对地球的一份善意。
          </div>
        </div>
      </div>
    </section>

    <!-- Hot Categories -->
    <section class="hot-categories" v-if="hotCategories.length > 0">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">大家都在看</h2>
        </div>
        <div class="hot-categories-scroll">
          <div v-for="cat in hotCategories" :key="cat.id"
               class="hot-category-card" role="button" tabindex="0"
               @click="navigateToCategoryByName(cat.name)">
            <div class="hot-category-icon">{{ cat.icon }}</div>
            <div class="hot-category-info">
              <span class="hot-category-name">{{ cat.name }}</span>
              <span class="hot-category-count">{{ cat.count }} 件在售</span>
            </div>
            <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" stroke-width="2" fill="none">
              <path d="M9 18l6-6-6-6" />
            </svg>
          </div>
        </div>
      </div>
    </section>

    <!-- Recommended Items (Personalized) -->
    <ItemSection
      v-if="userStore.isLoggedIn && recommendedItems.length > 0"
      title="猜你喜欢"
      :items="recommendedItems"
      :loading="loadingRecommended"
      :liked-items="likedItems"
      @toggle-like="toggleLike"
    />

    <!-- Hot Items -->
    <ItemSection
      title="热门推荐"
      :items="hotItems"
      :loading="loading"
      :liked-items="likedItems"
      @toggle-like="toggleLike"
    />

    <!-- Latest Items -->
    <ItemSection
      title="最新发布"
      :items="latestItems"
      :loading="loading"
      :liked-items="likedItems"
      @toggle-like="toggleLike"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useItemStore } from '../store';
import { useCategoryStore } from '../store/category';
import { useUserStore } from '../store/modules/user';
import api from '../api';
import type { CarbonStats } from '../api/services/carbon';
import ItemSection from '../components/home/ItemSection.vue';

const router = useRouter();
const store = useItemStore();
const categoryStore = useCategoryStore();
const userStore = useUserStore();

/** 用于展示的一级分类列表 */
const displayCategories = computed(() => {
  const allEntry = { id: 'all' as const, name: '全部', icon: '🏠' };
  const apiCategories = categoryStore.categories
    .filter(c => c.parentId == null)
    .map(c => ({
      id: c.id as number,
      name: c.name,
      icon: c.icon || categoryStore.getCategoryIcon(c.name),
    }));
  return [allEntry, ...apiCategories];
});

/** 弹出浮层状态（M-3） */
const openPopoverId = ref<number | 'all' | null>(null);
const popoverStyle = ref<Record<string, string>>({});
let popoverTimer: ReturnType<typeof setTimeout> | null = null;

function onPopoverEnter(id: number | 'all') {
  if (popoverTimer) { clearTimeout(popoverTimer); popoverTimer = null; }
  openPopoverId.value = id;
  // 计算浮层位置：teleport 到 body 后需要 fixed 定位
  nextTick(() => {
    const chip = document.querySelector(`[data-chip-id="${id}"]`);
    if (chip) {
      const rect = chip.getBoundingClientRect();
      popoverStyle.value = {
        position: 'fixed',
        left: rect.left + rect.width / 2 + 'px',
        top: rect.bottom + 8 + 'px',
        transform: 'translateX(-50%)',
        zIndex: '1000',
      };
    }
  });
}

function onPopoverLeave() {
  if (popoverTimer) clearTimeout(popoverTimer);
  popoverTimer = setTimeout(() => {
    openPopoverId.value = null;
  }, 200);
}

/** 获取指定一级分类下的子分类列表 */
function getSubCategories(id: number) {
  const node = categoryStore.categoryTree.find(c => c.id === id);
  if (!node?.children?.length) return [];
  return node.children.map(c => ({
    id: c.id,
    name: c.name,
    icon: c.icon || categoryStore.getCategoryIcon(c.name),
  }));
}

/** 处理 chip 点击（移动端切换浮层，桌面端直接跳转） */
function handleChipClick(category: { id: number | 'all'; name: string }) {
  if (category.id === 'all') {
    selectCategory('all');
    return;
  }
  const isTouch = 'ontouchstart' in window || navigator.maxTouchPoints > 0;
  const subs = getSubCategories(category.id as number);
  if (subs.length > 0 && isTouch) {
    // 移动端：切换浮层展开/收起
    if (openPopoverId.value === category.id) {
      openPopoverId.value = null;
    } else {
      openPopoverId.value = category.id as number;
    }
  } else {
    // 桌面端或无子分类：直接跳转（使用语义化 URL）
    onPopoverEnter(category.id);
    selectCategory(category.id);
  }
}

/** 点击二级分类跳转（使用语义化 URL） */
function navigateToSubCategory(sub: { id: number; name: string; icon?: string }) {
  openPopoverId.value = null;
  router.push('/items/category/' + encodeURIComponent(sub.name));
}

/** 通过分类名称跳转到分类列表页 */
function navigateToCategoryByName(name: string) {
  router.push('/items/category/' + encodeURIComponent(name));
}

const activeCategory = ref<number | 'all'>('all');
const hotItems = ref<any[]>([]);
const hotCategories = ref<{ id: number; name: string; icon: string; count: number }[]>([]);
const latestItems = ref<any[]>([]);
const recommendedItems = ref<any[]>([]);
const loadingRecommended = ref(false);
const likedItems = ref(new Set<number>());
const loading = ref(true);
const error = ref('');
const carbonStatsLoaded = ref(false);
const carbonStats = ref<CarbonStats>({
  monthlySavingKg: 0,
  totalSavingKg: 0,
  treeEquivalent: 0,
  transactionCount: 0,
  participantCount: 0,
});

const selectCategory = (id: number | 'all') => {
  activeCategory.value = id;
  if (id === 'all') {
    router.push('/items');
  } else {
    // 使用语义化 URL
    const cat = categoryStore.flatCategories.find(c => c.id === id);
    const name = cat?.name || String(id);
    router.push('/items/category/' + encodeURIComponent(name));
  }
};

const toggleLike = (id: number) => {
  if (likedItems.value.has(id)) {
    likedItems.value.delete(id);
  } else {
    likedItems.value.add(id);
  }
};

const fetchRecommendedItems = async () => {
  loadingRecommended.value = true;
  try {
    const res = await api.item.getRecommendedItems();
    if (res?.data?.length) {
      recommendedItems.value = res.data.slice(0, 8).map((item: any) => ({
        id: item.id,
        title: item.title,
        price: item.price,
        originalPrice: item.originalPrice,
        coverImage: item.coverImage,
        categoryId: item.categoryId,
        category: item.categoryName?.toLowerCase() || 'other',
        categoryName: item.categoryName,
        sellerName: item.sellerNickname || '未知卖家',
        eco: item.price < 100,
        time: '',
      }));
    }
  } catch {
    // Non-critical, silent fail
  } finally {
    loadingRecommended.value = false;
  }
};

onMounted(async () => {
  loading.value = true;
  error.value = '';

  // 加载分类数据
  categoryStore.fetchAll();

    // 加载个性化推荐
    if (userStore.isLoggedIn) {
      fetchRecommendedItems();
    }

    // 加载热门分类
    try {
    const catRes = await api.category.getHotCategories?.();
    if (catRes?.data?.length) {
      hotCategories.value = catRes.data.map((c: any) => ({
        id: c.id,
        name: c.name,
        icon: c.icon || categoryStore.getCategoryIcon(c.name),
        count: c.count || 0,
      }));
    }
  } catch {
    console.warn('热门分类接口不可用，使用本地降级');
  }
  // 降级：接口数据不足时用本地分类填充
  if (hotCategories.value.length === 0 && categoryStore.flatCategories.length > 0) {
    const level1 = categoryStore.flatCategories.filter(c => c.level === 1);
    hotCategories.value = level1.slice(0, 4).map((c, i) => ({
      id: c.id,
      name: c.name,
      icon: c.icon || categoryStore.getCategoryIcon(c.name),
      count: Math.floor(10 + Math.random() * 40),
    }));
  }

  try {
    // 获取碳减排统计（非关键数据）
    try {
      const res = await api.carbon.getStats();
      if (res.data) {
        carbonStats.value = {
          monthlySavingKg: res.data.monthlySavingKg ?? 0,
          totalSavingKg: res.data.totalSavingKg ?? 0,
          treeEquivalent: res.data.treeEquivalent ?? 0,
          transactionCount: res.data.transactionCount ?? 0,
          participantCount: res.data.participantCount ?? 0,
        };
        carbonStatsLoaded.value = true;
      }
    } catch (_e: unknown) {
      // 非关键数据，降级显示为 --
      carbonStatsLoaded.value = false;
      console.error('获取碳减排统计失败，使用默认值');
    }

    await store.fetchHotItems();
    if (store.hotItems?.length > 0) {
      hotItems.value = store.hotItems.slice(0, 8).map((item: any) => ({
        id: item.id,
        title: item.title,
        price: item.price,
        originalPrice: item.originalPrice,
        coverImage: item.coverImage,
        categoryId: item.categoryId,
        category: item.categoryName?.toLowerCase() || 'other',
        categoryName: item.categoryName,
        sellerName: item.sellerNickname || '未知卖家',
        eco: true,
        time: '2小时前',
      }));
      latestItems.value = store.hotItems.slice(4, 12).map((item: any) => ({
        id: item.id,
        title: item.title,
        price: item.price,
        originalPrice: item.originalPrice,
        coverImage: item.coverImage,
        categoryId: item.categoryId,
        category: item.categoryName?.toLowerCase() || 'other',
        categoryName: item.categoryName,
        sellerName: item.sellerNickname || '未知卖家',
        eco: true,
        time: '5小时前',
      }));
    }
  } catch (_e: unknown) {
    error.value = '获取物品数据失败，请稍后重试';
    console.error('获取热门物品失败');
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped src="../styles/pages/home.css"></style>

<style scoped>
/* M-3: Sub-category Popover */
.category-chip-wrapper {
  position: relative;
}

.subcategory-popover {
  min-width: 120px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--r-lg);
  padding: 8px;
  box-shadow: var(--shadow-lg);
}

.popover-arrow {
  position: absolute;
  top: -6px;
  left: 50%;
  transform: translateX(-50%) rotate(45deg);
  width: 10px;
  height: 10px;
  background: var(--surface);
  border-left: 1px solid var(--border);
  border-top: 1px solid var(--border);
}

.popover-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: var(--r-sm);
  cursor: pointer;
  transition: background var(--duration-fast);
  white-space: nowrap;
  font-size: 13px;
  color: var(--text-primary);
}

.popover-chip:hover {
  background: var(--accent-subtle);
  color: var(--accent);
}

.popover-chip-icon {
  font-size: 16px;
  flex-shrink: 0;
}

.popover-chip-label {
  font-weight: 500;
}

/* Transition animation */
.popover-enter-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.popover-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}

.popover-enter-from,
.popover-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.popover-enter-to,
.popover-leave-from {
  opacity: 1;
  transform: translateY(0);
}

/* 移动端优化：增大点击区域 */
@media (max-width: 767px) {
  .subcategory-popover {
    left: 0;
    transform: none;
    min-width: 140px;
  }

  .popover-arrow {
    left: 24px;
    transform: rotate(45deg);
  }

  .popover-chip {
    padding: 10px 14px;
  }

  .popover-enter-from,
  .popover-leave-to {
    opacity: 0;
    transform: translateY(-4px);
  }

  .popover-enter-to,
  .popover-leave-from {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Reduced motion */
@media (prefers-reduced-motion: reduce) {
  .popover-enter-active,
  .popover-leave-active {
    transition: none;
  }
}
</style>
