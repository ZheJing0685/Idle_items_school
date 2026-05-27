<template>
  <div class="home">
    <section class="hero">
      <div class="container">
        <div class="hero-content">
          <div class="hero-text">
            <div class="hero-badge">
              <Smile :size="16" fill="var(--secondary-color)" color="var(--secondary-color)" />
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
                <Search :size="20" />
                探索好物
              </router-link>
              <router-link to="/publish" class="hero-btn hero-btn-secondary">
                <CirclePlus :size="20" />
                发布闲置
              </router-link>
            </div>
          </div>
          <div class="hero-visual">
            <div class="hero-card-stack">
              <div class="hero-card hero-card-1">
                <div class="hero-card-content">
                  <Laptop :size="28" class="hero-card-icon" stroke-width="1.5" />
                  <span class="hero-card-label">二手笔记本</span>
                  <span class="hero-card-price">省 ¥2,000+</span>
                </div>
              </div>
              <div class="hero-card hero-card-2">
                <div class="hero-card-content">
                  <BookOpen :size="28" class="hero-card-icon" stroke-width="1.5" />
                  <span class="hero-card-label">二手教材</span>
                  <span class="hero-card-price">低至 3 折</span>
                </div>
              </div>
              <div class="hero-card hero-card-3">
                <div class="hero-card-content">
                  <Watch :size="28" class="hero-card-icon" stroke-width="1.5" />
                  <span class="hero-card-label">运动手环</span>
                  <span class="hero-card-price">几乎全新</span>
                </div>
              </div>
            </div>
            <div class="hero-stats-row">
              <div class="hero-stat-item">
                <span class="hero-stat-number">12,847</span>
                <span class="hero-stat-label">成功交易</span>
              </div>
              <div class="hero-stat-divider"></div>
              <div class="hero-stat-item">
                <span class="hero-stat-number">98.6%</span>
                <span class="hero-stat-label">好评率</span>
              </div>
              <div class="hero-stat-divider"></div>
              <div class="hero-stat-item">
                <span class="hero-stat-number">24h</span>
                <span class="hero-stat-label">快速响应</span>
              </div>
            </div>
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
                <component :is="getCategoryIcon(category.id)" :size="24" stroke-width="1.5" />
              </div>
              <div class="category-info">
                <h3 class="category-name">{{ category.name }}</h3>
                <span class="category-count">{{ category.count }} 件物品</span>
              </div>
              <ArrowRight class="category-arrow" :size="20" />
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
             <ArrowRight :size="16" />
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
                :src="item.coverImage"
                :alt="item.title"
                loading="lazy"
              />
              <div v-if="!item.coverImage" class="item-image-placeholder">
                <Image :size="32" />
                <span>暂无图片</span>
              </div>
              <div class="item-overlay">
                <button class="item-action">
                  <Eye :size="20" />
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
                    <Eye :size="12" />
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
               <ArrowRight :size="18" />
            </router-link>
          </div>
          <div class="promo-visual">
              <div class="promo-visual-placeholder">
                <Verified :size="48" />
                <span>学生认证即享优惠</span>
              </div>
          </div>
        </div>
      </div>
    </section>

    <section class="trust">
      <div class="container">
        <div class="trust-grid">
          <div class="trust-item">
            <div class="trust-icon">
              <ShieldCheck :size="32" color="var(--primary-color)" />
            </div>
            <h3 class="trust-title">实名认证</h3>
            <p class="trust-desc">所有用户经过学生身份认证，交易更放心</p>
          </div>
          <div class="trust-item">
            <div class="trust-icon">
              <MessageSquare :size="32" color="var(--primary-color)" />
            </div>
            <h3 class="trust-title">快捷发布</h3>
            <p class="trust-desc">拍照上传，简单几步即可发布你的闲置</p>
          </div>
          <div class="trust-item">
            <div class="trust-icon">
              <Leaf :size="32" color="var(--primary-color)" />
            </div>
            <h3 class="trust-title">环保交易</h3>
            <p class="trust-desc">减少资源浪费，为绿色校园贡献力量</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useItemStore } from '../store';
import api from '../api';
import { Search, CirclePlus, ArrowRight, Smile, Eye, Image, Verified, ShieldCheck, MessageSquare, Leaf, Laptop, BookOpen, Watch, Smartphone, Home, Trophy, Shirt, Package } from 'lucide-vue-next';

const store = useItemStore();

const categories = ref<any[]>([]);
const featuredItems = ref<any[]>([]);
const loading = ref({
  categories: false,
  items: false,
});

const fetchCategories = async () => {
  loading.value.categories = true;
  try {
    const response = await api.category.getCategoryTree();
    categories.value = response.data.map((category: any) => ({
      id: category.id,
      name: category.name,
      count: category.itemCount || 0,
      bgColor: getCategoryColor(category.id),
      children: (category.children || []).map((child: any) => ({
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

const getCategoryColor = (id: number) => {
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

const getCategoryIcon = (id: number) => {
  const icons = [Smartphone, BookOpen, Home, Trophy, Shirt, Package];
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
      featuredItems.value = store.hotItems.slice(0, 8).map((item: any) => ({
        id: item.id,
        title: item.title,
        price: item.price,
        originalPrice: item.originalPrice,
        coverImage: item.coverImage,
        viewCount: item.viewCount,
        sellerName: item.sellerNickname || '未知卖家',
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
