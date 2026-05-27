<template>
  <div class="favorites-page">
    <PageHeader title="我的收藏" subtitle="收藏的物品会显示在这里，方便您随时查看" />

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-grid">
      <div v-for="i in 6" :key="i" class="skeleton-card">
        <div class="skeleton-image"></div>
        <div class="skeleton-content">
          <div class="skeleton-title"></div>
          <div class="skeleton-price"></div>
        </div>
      </div>
    </div>

    <!-- 错误提示 -->
    <el-alert v-else-if="error" :title="error" type="error" show-icon class="error-alert" />

    <!-- 空状态 -->
    <EmptyState v-else-if="favorites.length === 0" title="您还没有收藏任何物品" description="浏览物品时点击收藏，感兴趣的物品会显示在这里">
      <template #action>
        <el-button type="primary" @click="$router.push('/')">去浏览物品</el-button>
      </template>
    </EmptyState>

    <!-- 收藏列表 -->
    <div v-else class="favorites-grid">
      <ItemCard
        v-for="item in favorites"
        :key="item.id"
        :id="item.itemId"
        :title="item.title"
        :price="item.price"
        :coverImage="item.coverImage"
        :status="item.status"
        :statusText="getStatusText(item.status)"
        :time="formatDate(item.createdAt)"
        @click="goToItemDetail(item.itemId)"
      >
        <template #actions>
          <el-button type="primary" size="small" @click.stop="goToItemDetail(item.itemId)">查看详情</el-button>
          <el-button type="danger" size="small" @click.stop="removeFavorite(item.itemId)">取消收藏</el-button>
        </template>
      </ItemCard>
    </div>

    <!-- 分页 -->
    <div v-if="!loading && !error && favorites.length > 0" class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[12, 24, 36]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import api from '../../api';
import { useDictStore } from '../../store/dict.js';
import PageHeader from '../../components/user/PageHeader.vue';
import ItemCard from '../../components/user/ItemCard.vue';
import EmptyState from '../../components/user/EmptyState.vue';

const router = useRouter();
const dictStore = useDictStore();

const favorites = ref<any[]>([]);
const loading = ref(false);
const error = ref('');
const currentPage = ref(1);
const pageSize = ref(12);
const total = ref(0);

const loadFavorites = async () => {
  loading.value = true;
  error.value = '';
  try {
    const response = await api.favorite.getFavorites(
      currentPage.value,
      pageSize.value
    );
    if (response.code === 200) {
      favorites.value = response.data.content || [];
      total.value = response.data.totalElements || 0;
    } else {
      error.value = response.message || '加载收藏失败';
    }
  } catch (err) {
    error.value = '网络错误，请稍后重试';
  } finally {
    loading.value = false;
  }
};

const removeFavorite = async (itemId: string) => {
  try {
    const response = await api.favorite.removeFavorite(itemId);
    if (response.code === 200) {
      favorites.value = favorites.value.filter((fav) => String(fav.itemId) !== itemId);
      total.value--;
      ElMessage.success('取消收藏成功');
    } else {
      ElMessage.error(response.message || '取消收藏失败');
    }
  } catch (err) {
    ElMessage.error('网络错误，请稍后重试');
  }
};

const goToItemDetail = (itemId: string) => {
  router.push(`/item/${itemId}`);
};

const formatDate = (dateString: string) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
};

const getStatusText = (status: string) => {
  return dictStore.getDictLabel('ITEM_STATUS', status) || status;
};

const handleSizeChange = (size: number) => {
  pageSize.value = size;
  currentPage.value = 1;
  loadFavorites();
};

const handleCurrentChange = (page: number) => {
  currentPage.value = page;
  loadFavorites();
};

onMounted(async () => {
  await dictStore.preloadCommonDicts();
  loadFavorites();
});
</script>

<style scoped src="../../styles/pages/user-favorites.css"></style>
