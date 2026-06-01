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
              <strong>128kg</strong>
              本月减碳量
            </div>
            <div class="hero-stat-item">
              <strong>1,206</strong>
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

    <!-- Categories -->
    <section class="categories">
      <div class="container">
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
      </div>
    </section>

    <!-- Eco Stats Bar -->
    <section class="eco-section">
      <div class="container">
        <div class="eco-bar">
          <span class="eco-bar-icon">♻️</span>
          <div class="eco-bar-text">
            本月校园交易已减少 <strong>128kg</strong> 碳排放，相当于种植 <strong>7</strong> 棵树的年吸碳量。每一次交易都是对地球的一份善意。
          </div>
        </div>
      </div>
    </section>

    <!-- Hot Items -->
    <section class="home-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">热门推荐</h2>
          <router-link to="/items" class="section-more">查看更多 →</router-link>
        </div>
        <div class="items-grid">
          <div
            v-for="(item, index) in hotItems"
            :key="item.id"
            class="card card-clickable item-card"
            @click="$router.push(`/item/${item.id}`)"
          >
            <div class="item-card-img">
              <img v-if="item.coverImage" :src="item.coverImage" :alt="item.title" class="item-img" />
              <div v-else class="img-placeholder" :style="{ background: getItemColor(item.category, index) }">
                {{ getCategoryEmoji(item.category) }}
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

    <!-- Latest Items -->
    <section class="home-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">最新发布</h2>
          <router-link to="/items" class="section-more">查看更多 →</router-link>
        </div>
        <div class="items-grid">
          <div
            v-for="(item, index) in latestItems"
            :key="item.id"
            class="card card-clickable item-card"
            @click="$router.push(`/item/${item.id}`)"
          >
            <div class="item-card-img">
              <img v-if="item.coverImage" :src="item.coverImage" :alt="item.title" class="item-img" />
              <div v-else class="img-placeholder" :style="{ background: getItemColor(item.category, index) }">
                {{ getCategoryEmoji(item.category) }}
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useItemStore } from '../store';
import api from '../api';

const router = useRouter();
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

const activeCategory = ref('all');
const hotItems = ref<any[]>([]);
const latestItems = ref<any[]>([]);
const likedItems = ref(new Set<number>());

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
  const colors = IMG_COLORS[category] || IMG_COLORS.other;
  return colors[index % colors.length];
};

const getCategoryEmoji = (category: string) => {
  const cat = categories.value.find(c => c.id === category);
  return cat?.icon || '📦';
};

const selectCategory = (id: string) => {
  activeCategory.value = id;
  if (id === 'all') {
    router.push('/items');
  } else {
    router.push({ path: '/items', query: { category: id } });
  }
};

const toggleLike = (id: number) => {
  if (likedItems.value.has(id)) {
    likedItems.value.delete(id);
  } else {
    likedItems.value.add(id);
  }
};

onMounted(async () => {
  try {
    await store.fetchHotItems();
    if (store.hotItems?.length > 0) {
      hotItems.value = store.hotItems.slice(0, 8).map((item: any) => ({
        id: item.id,
        title: item.title,
        price: item.price,
        originalPrice: item.originalPrice,
        coverImage: item.coverImage,
        category: item.categoryName?.toLowerCase() || 'other',
        sellerName: item.sellerNickname || '未知卖家',
        eco: item.price < 100,
        time: '2小时前',
      }));
      latestItems.value = store.hotItems.slice(4, 12).map((item: any) => ({
        id: item.id,
        title: item.title,
        price: item.price,
        originalPrice: item.originalPrice,
        coverImage: item.coverImage,
        category: item.categoryName?.toLowerCase() || 'other',
        sellerName: item.sellerNickname || '未知卖家',
        eco: item.price < 100,
        time: '5小时前',
      }));
    }
  } catch (error) {
    console.error('获取热门物品失败', error);
  }
});
</script>

<style scoped src="../styles/pages/home.css"></style>
