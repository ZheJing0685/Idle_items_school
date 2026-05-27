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
            :props="{
              value: 'id',
              label: 'name',
              children: 'children',
              checkStrictly: true,
            }"
            placeholder="全部分类"
            clearable
            @change="handleCategoryChange"
            class="filter-select filter-select--wide"
          />

          <el-select
            v-model="condition"
            placeholder="成色"
            @change="handleFilter"
            class="filter-select filter-select--narrow"
          >
            <el-option label="全部" value="" />
            <el-option
              v-for="option in conditionOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>

          <el-select
            v-model="deliveryMethod"
            placeholder="配送方式"
            @change="handleFilter"
            class="filter-select filter-select--narrow"
          >
            <el-option label="全部" value="" />
            <el-option
              v-for="option in deliveryMethodOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
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
              <Search :size="18" />
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
                <Eye :size="20" />
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
                >{{ tag }}</span
              >
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
                  <Eye :size="14" />
                  {{ item.viewCount || 0 }}
                </span>
                <span class="stat">
                  <Heart :size="14" />
                  {{ item.favoriteCount || 0 }}
                </span>
              </div>
            </div>
          </div>
        </article>
      </div>

      <div class="empty-state" v-else>
        <div class="empty-icon">
          <Grid :size="64" color="var(--text-muted)" stroke-width="1.5" />
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

<script setup lang="ts">
import { ref, onMounted, nextTick, watch, computed } from 'vue';
import { useRoute } from 'vue-router';
import { useItemStore } from '../store';
import api from '../api';
import { useDictStore } from '../store/dict.js';
import { Search, Eye, Heart, Grid } from 'lucide-vue-next';

const route = useRoute();
const store = useItemStore();
const dictStore = useDictStore();

// 获取字典选项
const conditionOptions = computed(() => {
  const options = dictStore.getDictOptions('ITEM_CONDITION');
  if (options.length > 0) return options;
  return [
    { value: 'NEW', label: '全新' },
    { value: 'LIKE_NEW', label: '九成新' },
    { value: 'GOOD', label: '八成新' },
    { value: 'FAIR', label: '七成新' },
    { value: 'POOR', label: '六成新及以下' },
  ];
});
const deliveryMethodOptions = computed(() =>
  dictStore.getDictOptions('DELIVERY_METHOD')
);

const categoryId = ref('');
const categoryPath = ref<any[]>([]);
const categoryTreeOptions = ref<any[]>([]);
const condition = ref('');
const deliveryMethod = ref('');
const sortBy = ref('createdAt');
const keyword = ref('');
const currentPage = ref(1);
const pageSize = ref(24);
const total = ref(0);
const items = ref<any[]>([]);

// 监听路由参数变化
watch(
  () => route.query,
  (newQuery) => {
    if (newQuery.category !== categoryId.value) {
      categoryId.value = (newQuery.category as string) || '';
      categoryPath.value = categoryId.value
        ? findCategoryPath(categoryTreeOptions.value, Number(categoryId.value))
        : [];
      currentPage.value = 1;
      loadItems();
    }
    if (newQuery.keyword !== keyword.value) {
      keyword.value = (newQuery.keyword as string) || '';
      currentPage.value = 1;
      loadItems();
    }
  },
  { deep: true }
);

const isNew = (date: string) => {
  if (!date) return false;
  const created = new Date(date);
  const now = new Date();
  const diffDays = (now.getTime() - created.getTime()) / (1000 * 60 * 60 * 24);
  return diffDays < 7;
};

const getDiscount = (price: number, originalPrice: number) => {
  if (!price || !originalPrice || originalPrice <= price) return null;
  return Math.round((1 - price / originalPrice) * 100);
};

const getConditionText = (condition: string) => {
  const label = dictStore.getDictLabel('ITEM_CONDITION', condition);
  if (label && label !== condition) return label;
  const fallbackMap: Record<string, string> = {
    NEW: '全新',
    LIKE_NEW: '九成新',
    GOOD: '八成新',
    FAIR: '七成新',
    POOR: '六成新及以下',
  };
  return fallbackMap[condition] || condition;
};

const getDeliveryText = (method: string) => {
  const label = dictStore.getDictLabel('DELIVERY_METHOD', method);
  if (label && label !== method) return label;
  const fallbackMap: Record<string, string> = {
    LOCAL_DELIVERY: '自提',
    HOME_DELIVERY: '上门',
    EXPRESS: '快递',
    MAIL: '邮寄',
  };
  return fallbackMap[method] || method;
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

const findCategoryPath = (nodes: any[], targetId: number, path: any[] = []): any[] => {
  for (const node of nodes) {
    if (node.id === targetId) return [...path, node.id];
    if (node.children && node.children.length > 0) {
      const found = findCategoryPath(node.children, targetId, [
        ...path,
        node.id,
      ]);
      if (found.length > 0) return found;
    }
  }
  return [];
};

const handleCategoryChange = (val: any) => {
  if (val && val.length > 0) {
    categoryId.value = val[val.length - 1].toString();
  } else {
    categoryId.value = '';
    categoryPath.value = [];
  }
  handleFilter();
};

// 解析标签（支持JSON字符串和逗号分隔两种格式）
const parseTags = (tagsStr: string) => {
  if (!tagsStr) return [];
  if (tagsStr.includes('[') || tagsStr.includes('{')) {
    try {
      const tags = JSON.parse(tagsStr);
      return Array.isArray(tags) ? tags : [];
    } catch {
      return [];
    }
  }
  return tagsStr.split(',').map(t => t.trim()).filter(Boolean);
};

const loadItems = async () => {
  try {
    if (keyword.value) {
      await store.searchItems(keyword.value, currentPage.value, pageSize.value, sortBy.value);
      items.value = store.searchResults;
      total.value = store.searchTotal;
    } else {
      const params = {
        page: currentPage.value,
        size: pageSize.value,
        categoryId: categoryId.value || undefined,
        condition: condition.value || undefined,
        deliveryMethod: deliveryMethod.value || undefined,
        sortBy: sortBy.value,
      };
      await store.fetchItems(params);
      items.value = store.items;
      total.value = store.total;
    }

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

const handleSizeChange = (size: number) => {
  pageSize.value = size;
  currentPage.value = 1;
  loadItems();
};

const handleCurrentChange = (page: number) => {
  currentPage.value = page;
  loadItems();
};

onMounted(async () => {
  // 加载字典数据
  await dictStore.preloadCommonDicts();
  await loadCategories();
  if (route.query.category) {
    categoryId.value = route.query.category as string;
    categoryPath.value = findCategoryPath(
      categoryTreeOptions.value,
      Number(categoryId.value)
    );
  }
  if (route.query.keyword) {
    keyword.value = route.query.keyword as string;
  }
  loadItems();
});
</script>

<style scoped src="../styles/pages/items.css"></style>

<style>
.items-page .filter-left > .el-cascader.filter-select--wide {
  width: 180px;
  min-width: 0;
  overflow: hidden;
}
</style>
