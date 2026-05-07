<template>
  <div class="items-page">
    <div class="page-hero">
      <div class="container">
        <div class="hero-content">
          <h1 class="page-title">发现闲置好物</h1>
          <p class="page-subtitle">
            浏览来自校园的优质二手物品，让闲置找到新主人
          </p>
        </div>
      </div>
    </div>

    <div class="container">
      <div class="filter-bar">
        <div class="filter-left">
          <el-cascader
            v-model="categoryPath"
            :options="categoryTreeOptions"
            :props="{ value: 'id', label: 'name', children: 'children', checkStrictly: true }"
            placeholder="全部分类"
            clearable
            @change="handleCategoryChange"
            class="filter-select"
          />

          <el-select
            v-model="condition"
            placeholder="成色"
            @change="handleFilter"
            class="filter-select"
          >
            <el-option label="全部" value="" />
            <el-option label="全新" value="1" />
            <el-option label="九成新" value="2" />
            <el-option label="八成新" value="3" />
            <el-option label="七成新" value="4" />
            <el-option label="六成新及以下" value="5" />
          </el-select>

          <el-select
            v-model="deliveryMethod"
            placeholder="配送方式"
            @change="handleFilter"
            class="filter-select"
          >
            <el-option label="全部" value="" />
            <el-option label="自提" value="1" />
            <el-option label="快递" value="2" />
            <el-option label="两者皆可" value="3" />
          </el-select>

          <el-select
            v-model="sortBy"
            placeholder="排序"
            @change="handleFilter"
            class="filter-select"
          >
            <el-option label="最新发布" value="createdAt" />
            <el-option label="价格 ↑" value="priceAsc" />
            <el-option label="价格 ↓" value="priceDesc" />
            <el-option label="浏览最多" value="viewCount" />
            <el-option label="收藏最多" value="favoriteCount" />
          </el-select>
        </div>

        <div class="filter-right">
          <el-input
            v-model="keyword"
            placeholder="搜索你想要的..."
            @keyup.enter="handleSearch"
            class="search-input"
          >
            <template #prefix>
              <svg
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <circle cx="11" cy="11" r="8" />
                <path d="M21 21L16.65 16.65" />
              </svg>
            </template>
            <template #append>
              <el-button @click="handleSearch" class="search-btn"
                >搜索</el-button
              >
            </template>
          </el-input>
        </div>
      </div>

      <div class="items-grid" v-if="items.length > 0">
        <article
          v-for="(item, index) in items"
          :key="item.id"
          class="item-card"
          :style="{ animationDelay: `${index * 0.05}s` }"
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
              <button class="view-btn">
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
            <span class="item-badge item-badge-new" v-if="isNew(item.createdAt)"
              >新品</span
            >
            <span
              class="item-badge item-badge-discount"
              v-if="getDiscount(item.price, item.originalPrice)"
              >{{ getDiscount(item.price, item.originalPrice) }}%</span
            >
            <span
              class="item-badge item-badge-bargain"
              v-if="item.isBargainAllowed"
              >可议价</span
            >
          </div>
          <div class="item-content">
            <h3 class="item-title">{{ item.title }}</h3>
            <div class="item-tags" v-if="parseTags(item.tags).length > 0">
              <span
                class="item-tag"
                v-for="(tag, index) in parseTags(item.tags).slice(0, 3)"
                :key="index"
              >{{ tag }}</span>
            </div>
            <div class="item-price-row">
              <span class="item-price">¥{{ item.price }}</span>
              <span class="item-original" v-if="item.originalPrice"
                >¥{{ item.originalPrice }}</span
              >
            </div>
            <div class="item-meta">
              <div class="item-info">
                <span class="item-condition" v-if="item.condition">{{
                  getConditionText(item.condition)
                }}</span>
                <span class="item-delivery" v-if="item.deliveryMethod">{{
                  getDeliveryText(item.deliveryMethod)
                }}</span>
              </div>
              <div class="item-stats">
                <span class="stat">
                  <svg
                    width="14"
                    height="14"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <path d="M1 12S5 4 12 4s11 8 11 8-4 8-11 8S1 12 1 12z" />
                    <circle cx="12" cy="12" r="3" />
                  </svg>
                  {{ item.viewCount || 0 }}
                </span>
                <span class="stat">
                  <svg
                    width="14"
                    height="14"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <path
                      d="M20.84 4.61C20.3292 4.09924 19.7228 3.69397 19.0554 3.41708C18.3879 3.14019 17.6725 2.99756 16.95 2.99756C16.2275 2.99756 15.5121 3.14019 14.8446 3.41708C14.1772 3.69397 13.5708 4.09924 13.06 4.61L12 5.67L10.94 4.61C9.9083 3.57831 8.50903 2.99787 7.05 2.99787C5.59096 2.99787 4.19169 3.57831 3.16 4.61C2.1283 5.64169 1.54785 7.04097 1.54785 8.5C1.54785 9.95903 2.1283 11.3583 3.16 12.39L4.22 13.45L12 21.23L19.78 13.45L20.84 12.39C21.3508 11.8792 21.756 11.2728 22.0329 10.6054C22.3098 9.93789 22.4524 9.22248 22.4524 8.5C22.4524 7.77751 22.3098 7.0621 22.0329 6.39464C21.756 5.72718 21.3508 5.12075 20.84 4.61Z"
                    />
                  </svg>
                  {{ item.favoriteCount || 0 }}
                </span>
              </div>
            </div>
          </div>
        </article>
      </div>

      <div class="empty-state" v-else>
        <div class="empty-icon">
          <svg
            width="64"
            height="64"
            viewBox="0 0 24 24"
            fill="none"
            stroke="var(--text-muted)"
            stroke-width="1.5"
          >
            <rect x="3" y="3" width="7" height="7" />
            <rect x="14" y="3" width="7" height="7" />
            <rect x="14" y="14" width="7" height="7" />
            <rect x="3" y="14" width="7" height="7" />
          </svg>
        </div>
        <h3 class="empty-title">暂无物品</h3>
        <p class="empty-desc">暂时没有找到符合条件的物品</p>
        <router-link to="/publish" class="empty-action"
          >发布你的第一个闲置</router-link
        >
      </div>

      <div class="pagination-wrap" v-if="total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[12, 24, 36, 48]"
          layout="prev, pager, next, sizes"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          class="custom-pagination"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue';
import { useRoute } from 'vue-router';
import itemStore from '../store/item';
import api from '../api';

const route = useRoute();
const store = itemStore();

const categoryId = ref('');
const categoryPath = ref([]);
const categoryTreeOptions = ref([]);
const condition = ref('');
const deliveryMethod = ref('');
const sortBy = ref('createdAt');
const keyword = ref('');
const currentPage = ref(1);
const pageSize = ref(24);
const total = ref(0);
const items = ref([]);

// 监听路由参数变化
watch(
  () => route.query,
  (newQuery) => {
    if (newQuery.category !== categoryId.value) {
      categoryId.value = newQuery.category || '';
      categoryPath.value = categoryId.value ? findCategoryPath(categoryTreeOptions.value, Number(categoryId.value)) : [];
      currentPage.value = 1;
      loadItems();
    }
    if (newQuery.keyword !== keyword.value) {
      keyword.value = newQuery.keyword || '';
      currentPage.value = 1;
      loadItems();
    }
  },
  { deep: true }
);

const isNew = (date) => {
  if (!date) return false;
  const created = new Date(date);
  const now = new Date();
  const diffDays = (now - created) / (1000 * 60 * 60 * 24);
  return diffDays < 7;
};

const getDiscount = (price, originalPrice) => {
  if (!price || !originalPrice || originalPrice <= price) return null;
  return Math.round((1 - price / originalPrice) * 100);
};

const getConditionText = (condition) => {
  const map = {
    1: '全新',
    2: '九成新',
    3: '八成新',
    4: '七成新',
    5: '六成新及以下',
  };
  return map[condition] || condition;
};

const getDeliveryText = (method) => {
  const map = {
    1: '自提',
    2: '快递',
    3: '两者皆可',
  };
  return map[method] || method;
};

const loadCategories = async () => {
  try {
    const response = await api.category.getCategoryTree();
    if (response.code === 200) {
      categoryTreeOptions.value = response.data || [];
    }
  } catch (error) {
    console.error('获取分类失败', error);
  }
};

const findCategoryPath = (nodes, targetId, path = []) => {
  for (const node of nodes) {
    if (node.id === targetId) return [...path, node.id];
    if (node.children && node.children.length > 0) {
      const found = findCategoryPath(node.children, targetId, [...path, node.id]);
      if (found.length > 0) return found;
    }
  }
  return [];
};

const handleCategoryChange = (val) => {
  if (val && val.length > 0) {
    categoryId.value = val[val.length - 1].toString();
  } else {
    categoryId.value = '';
    categoryPath.value = [];
  }
  handleFilter();
};

// 解析标签（从JSON字符串到数组）
const parseTags = (tagsStr) => {
  if (!tagsStr) return [];
  try {
    const tags = JSON.parse(tagsStr);
    return Array.isArray(tags) ? tags : [];
  } catch (e) {
    return [];
  }
};

const loadItems = async () => {
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      categoryId: categoryId.value,
      condition: condition.value,
      deliveryMethod: deliveryMethod.value,
      sortBy: sortBy.value,
      keyword: keyword.value,
    };
    await store.fetchItems(params);
    items.value = store.items;
    total.value = store.total;

    await nextTick();
    window.scrollTo({ top: 0, behavior: 'smooth' });
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

const handleSizeChange = (size) => {
  pageSize.value = size;
  currentPage.value = 1;
  loadItems();
};

const handleCurrentChange = (page) => {
  currentPage.value = page;
  loadItems();
};

onMounted(async () => {
  await loadCategories();
  if (route.query.category) {
    categoryId.value = route.query.category;
    categoryPath.value = findCategoryPath(categoryTreeOptions.value, Number(categoryId.value));
  }
  if (route.query.keyword) {
    keyword.value = route.query.keyword;
  }
  loadItems();
});
</script>

<style scoped src="../styles/pages/items.css"></style>
