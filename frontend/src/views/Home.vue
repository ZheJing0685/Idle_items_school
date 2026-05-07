<template>
  <div class="home">
    <section class="hero">
      <div class="container">
        <div class="hero-content">
          <div class="hero-text">
            <div class="hero-badge">
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
              </svg>
              <span>校园绿色交易平台</span>
            </div>
            <h1 class="hero-title">
              闲置不闲置<br />
              <span class="hero-title-accent">变废为宝</span>
            </h1>
            <p class="hero-description">
              让闲置物品找到新主人，让资源得到充分利用。<br />
              绿色校园，你我共创。
            </p>
            <div class="hero-actions">
              <router-link to="/items" class="hero-btn hero-btn-primary">
                <svg
                  width="20"
                  height="20"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <circle cx="11" cy="11" r="8" />
                  <path d="M21 21L16.65 16.65" />
                </svg>
                探索好物
              </router-link>
              <router-link to="/publish" class="hero-btn hero-btn-secondary">
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
                发布闲置
              </router-link>
            </div>
          </div>
          <div class="hero-visual">
            <div class="hero-card-stack">
              <div class="hero-card hero-card-1">
                <img
                  src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=second%20hand%20laptop%20for%20student&image_size=square"
                  alt="二手笔记本"
                />
              </div>
              <div class="hero-card hero-card-2">
                <img
                  src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=college%20textbooks%20second%20hand&image_size=square"
                  alt="二手教材"
                />
              </div>
              <div class="hero-card hero-card-3">
                <img
                  src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=fitness%20tracker%20electronics&image_size=square"
                  alt="运动手环"
                />
              </div>
            </div>
            <div class="hero-eco-badge">
              <svg
                width="24"
                height="24"
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
              <span>已节省 128.5 吨碳排放</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="stats">
      <div class="container">
        <div class="stats-grid">
          <div class="stat-card">
            <span class="stat-value">12,847</span>
            <span class="stat-label">成功交易</span>
          </div>
          <div class="stat-card">
            <span class="stat-value">8,234</span>
            <span class="stat-label">注册用户</span>
          </div>
          <div class="stat-card">
            <span class="stat-value">45,231</span>
            <span class="stat-label">发布物品</span>
          </div>
          <div class="stat-card stat-card-highlight">
            <span class="stat-value">128.5</span>
            <span class="stat-label">吨碳减排</span>
          </div>
        </div>
      </div>
    </section>

    <section class="categories">
      <div class="container">
        <div class="section-header">
          <div class="section-title-group">
            <h2 class="section-title">分类浏览</h2>
            <p class="section-subtitle">找到你需要的闲置好物</p>
          </div>
        </div>
        <div class="categories-grid">
          <div
            v-for="(category, index) in categories"
            :key="category.id"
            class="category-card-wrapper"
          >
            <router-link
              :to="`/items?category=${category.id}`"
              class="category-card"
              :style="{ animationDelay: `${index * 0.1}s` }"
            >
              <div
                class="category-icon"
                :style="{ background: category.bgColor }"
              >
                {{ getCategoryIcon(category.id) }}
              </div>
              <div class="category-info">
                <h3 class="category-name">{{ category.name }}</h3>
                <span class="category-count">{{ category.count }} 件物品</span>
              </div>
              <svg
                class="category-arrow"
                width="20"
                height="20"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path d="M5 12H19" />
                <path d="M12 5L19 12L12 19" />
              </svg>
            </router-link>
            <div
              class="category-submenu"
              v-if="category.children && category.children.length > 0"
            >
              <router-link
                v-for="child in category.children"
                :key="child.id"
                :to="`/items?category=${child.id}`"
                class="submenu-item"
              >
                <span class="submenu-name">{{ child.name }}</span>
                <span class="submenu-count">{{ child.count }} 件</span>
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="featured">
      <div class="container">
        <div class="section-header">
          <div class="section-title-group">
            <h2 class="section-title">热门好物</h2>
            <p class="section-subtitle">精选优质闲置，抢手好货</p>
          </div>
          <router-link to="/items" class="section-link">
            查看更多
            <svg
              width="16"
              height="16"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <path d="M5 12H19" />
              <path d="M12 5L19 12L12 19" />
            </svg>
          </router-link>
        </div>
        <div class="items-grid">
          <article
            v-for="(item, index) in featuredItems"
            :key="item.id"
            class="item-card"
            :style="{ animationDelay: `${index * 0.08}s` }"
            @click="$router.push(`/item/${item.id}`)"
          >
            <div class="item-image">
              <img
                :src="
                  item.coverImage ||
                  'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=placeholder%20item&image_size=square'
                "
                :alt="item.title"
                loading="lazy"
              />
              <div class="item-overlay">
                <button class="item-action">
                  <svg
                    width="20"
                    height="20"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <path d="M1 12S5 4 12 4s11 8 11 8-4 8-11 8S1 12 1 12z" />
                    <circle cx="12" cy="12" r="3" />
                  </svg>
                  查看详情
                </button>
              </div>
              <span class="item-badge" v-if="item.isNew">新品</span>
            </div>
            <div class="item-content">
              <h3 class="item-title">{{ item.title }}</h3>
              <div class="item-price-row">
                <span class="item-price">¥{{ item.price }}</span>
                <span class="item-original" v-if="item.originalPrice"
                  >¥{{ item.originalPrice }}</span
                >
              </div>
              <div class="item-meta">
                <div class="item-seller">
                  <el-avatar :size="20">{{
                    item.sellerName?.charAt(0) || '卖'
                  }}</el-avatar>
                  <span>{{ item.sellerName }}</span>
                </div>
                <div class="item-stats">
                  <span class="item-views">
                    <svg
                      width="12"
                      height="12"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                    >
                      <path d="M1 12S5 4 12 4s11 8 11 8-4 8-11 8S1 12 1 12z" />
                      <circle cx="12" cy="12" r="3" />
                    </svg>
                    {{ item.viewCount }}
                  </span>
                </div>
              </div>
            </div>
          </article>
        </div>
      </div>
    </section>

    <section class="promo">
      <div class="container">
        <div class="promo-card">
          <div class="promo-content">
            <div class="promo-badge">新用户专享</div>
            <h2 class="promo-title">注册即送100积分</h2>
            <p class="promo-description">首单立减10元，让交易更划算</p>
            <router-link to="/register" class="promo-btn">
              立即注册
              <svg
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path d="M5 12H19" />
                <path d="M12 5L19 12L12 19" />
              </svg>
            </router-link>
          </div>
          <div class="promo-visual">
            <img
              src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=student%20discount%20illustration%20eco%20friendly&image_size=landscape_4_3"
              alt="新用户优惠"
            />
          </div>
        </div>
      </div>
    </section>

    <section class="trust">
      <div class="container">
        <div class="trust-grid">
          <div class="trust-item">
            <div class="trust-icon">
              <svg
                width="32"
                height="32"
                viewBox="0 0 24 24"
                fill="none"
                stroke="var(--primary-color)"
                stroke-width="2"
              >
                <path
                  d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z"
                />
                <path d="M9 12L11 14L15 10" />
              </svg>
            </div>
            <h3 class="trust-title">实名认证</h3>
            <p class="trust-desc">所有用户经过学生身份认证，交易更放心</p>
          </div>
          <div class="trust-item">
            <div class="trust-icon">
              <svg
                width="32"
                height="32"
                viewBox="0 0 24 24"
                fill="none"
                stroke="var(--primary-color)"
                stroke-width="2"
              >
                <path
                  d="M21 15C21 15.5304 20.7893 16.0391 20.4142 16.4142C20.0391 16.7893 19.5304 17 19 17H7L3 21V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H16C16.5304 3 17.0391 3.21071 17.4142 3.58579C17.7893 3.96086 18 4.46957 18 5"
                />
              </svg>
            </div>
            <h3 class="trust-title">快捷发布</h3>
            <p class="trust-desc">拍照上传，简单几步即可发布你的闲置</p>
          </div>
          <div class="trust-item">
            <div class="trust-icon">
              <svg
                width="32"
                height="32"
                viewBox="0 0 24 24"
                fill="none"
                stroke="var(--primary-color)"
                stroke-width="2"
              >
                <path
                  d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z"
                />
                <path
                  d="M7 13C7 13 8 15 12 15C16 15 17 13 17 13"
                  stroke="var(--primary-color)"
                  stroke-width="2"
                  stroke-linecap="round"
                />
                <path
                  d="M12 9C12 9 9 10 9 12"
                  stroke="var(--primary-color)"
                  stroke-width="2"
                  stroke-linecap="round"
                />
              </svg>
            </div>
            <h3 class="trust-title">环保交易</h3>
            <p class="trust-desc">减少资源浪费，为绿色校园贡献力量</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import itemStore from '../store/item';
import api from '../api';

const store = itemStore();

const categories = ref([]);
const featuredItems = ref([]);
const loading = ref({
  categories: false,
  items: false,
});

const fetchCategories = async () => {
  loading.value.categories = true;
  try {
    const response = await api.category.getCategoryTree();
    categories.value = response.data.map((category) => ({
      id: category.id,
      name: category.name,
      count: category.itemCount || 0,
      bgColor: getCategoryColor(category.id),
      children: (category.children || []).map((child) => ({
        id: child.id,
        name: child.name,
        count: child.itemCount || 0,
      })),
    }));
  } catch (error) {
    console.error('获取分类失败', error);
  } finally {
    loading.value.categories = false;
  }
};

const getCategoryColor = (id) => {
  const colors = [
    'oklch(70% 0.16 38 / 0.15)',
    'oklch(62% 0.14 195 / 0.12)',
    'oklch(62% 0.12 158 / 0.12)',
    'oklch(75% 0.14 85 / 0.12)',
    'oklch(60% 0.20 25 / 0.10)',
    'oklch(62% 0.14 250 / 0.10)',
  ];
  return colors[(id - 1) % colors.length];
};

const getCategoryIcon = (id) => {
  const icons = ['📱', '📚', '🏠', '⚽', '👔', '📦'];
  return icons[(id - 1) % icons.length];
};

onMounted(async () => {
  // 获取分类
  await fetchCategories();

  // 获取热门物品
  loading.value.items = true;
  try {
    await store.fetchHotItems();
    if (store.hotItems?.length > 0) {
      featuredItems.value = store.hotItems.slice(0, 8).map((item) => ({
        id: item.id,
        title: item.title,
        price: item.price,
        originalPrice: item.originalPrice,
        coverImage: item.coverImage,
        viewCount: item.viewCount,
        sellerName: item.seller?.username || '未知卖家',
        isNew: item.isNew,
      }));
    }
  } catch (error) {
    console.error('获取热门物品失败', error);
  } finally {
    loading.value.items = false;
  }
});
</script>

<style scoped src="../styles/pages/home.css"></style>
