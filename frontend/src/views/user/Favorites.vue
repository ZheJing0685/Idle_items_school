<template>
  <div class="favorites-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>我的收藏</h2>
      <p class="subtitle">收藏的物品会显示在这里，方便您随时查看</p>
    </div>

    <!-- 加载状态 -->
    <el-skeleton v-if="loading" :rows="6" animated>
      <template #template>
        <el-skeleton-item variant="p" style="width: 100%" />
      </template>
    </el-skeleton>

    <!-- 错误提示 -->
    <el-alert
      v-else-if="error"
      :title="error"
      type="error"
      show-icon
      class="error-alert"
    />

    <!-- 空状态 -->
    <el-empty
      v-else-if="favorites.length === 0"
      description="您还没有收藏任何物品"
      class="empty-state"
    >
      <el-button type="primary" @click="$router.push('/')">去浏览物品</el-button>
    </el-empty>

    <!-- 收藏列表 -->
    <div v-else class="favorites-grid">
      <el-card
        v-for="item in favorites"
        :key="item.item.id"
        class="favorite-card"
        hover
      >
        <!-- 物品图片 -->
        <div class="card-image">
          <el-image
            :src="item.item.images && item.item.images.length > 0 ? item.item.images[0] : 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=placeholder%20image%20for%20secondhand%20item&image_size=square'"
            fit="cover"
            @click="goToItemDetail(item.item.id)"
          />
        </div>

        <!-- 物品信息 -->
        <div class="card-content">
          <h3 class="item-title" @click="goToItemDetail(item.item.id)">{{ item.item.title }}</h3>
          <div class="item-price">¥{{ item.item.price }}</div>
          <div class="item-meta">
            <span class="seller">{{ item.item.sellerName || '未知卖家' }}</span>
            <span class="favorited-time">{{ formatDate(item.createdAt) }}</span>
          </div>
          <div class="item-status">
            <el-tag :type="getStatusType(item.item.status)">
              {{ getStatusText(item.item.status) }}
            </el-tag>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="card-actions">
          <el-button
            type="primary"
            size="small"
            @click="goToItemDetail(item.item.id)"
            class="view-button"
          >
            查看详情
          </el-button>
          <el-button
            type="danger"
            size="small"
            @click="removeFavorite(item.item.id)"
            class="remove-button"
          >
            取消收藏
          </el-button>
        </div>
      </el-card>
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

<script setup>
import { ref, onMounted, computed } from 'vue';
import { ElMessage } from 'element-plus';
import api from '../../api';

// 响应式数据
const favorites = ref([]);
const loading = ref(false);
const error = ref('');
const currentPage = ref(1);
const pageSize = ref(12);
const total = ref(0);

// 加载收藏列表
const loadFavorites = async () => {
  loading.value = true;
  error.value = '';
  try {
    const response = await api.favorite.getFavorites(currentPage.value, pageSize.value);
    if (response.code === 200) {
      favorites.value = response.data.content || [];
      total.value = response.data.totalElements || 0;
    } else {
      error.value = response.message || '加载收藏失败';
    }
  } catch (err) {
    error.value = '网络错误，请稍后重试';
    console.error('加载收藏失败:', err);
  } finally {
    loading.value = false;
  }
};

// 取消收藏
const removeFavorite = async (itemId) => {
  try {
    const response = await api.favorite.removeFavorite(itemId);
    if (response.code === 200) {
      // 从列表中移除
      favorites.value = favorites.value.filter(fav => fav.item.id !== itemId);
      total.value--;
      // 显示成功提示
      ElMessage.success('取消收藏成功');
    } else {
      ElMessage.error(response.message || '取消收藏失败');
    }
  } catch (err) {
    ElMessage.error('网络错误，请稍后重试');
    console.error('取消收藏失败:', err);
  }
};

// 跳转到物品详情
const goToItemDetail = (itemId) => {
  window.location.href = `/item/${itemId}`;
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 'ON_SALE':
      return 'success';
    case 'SOLD':
      return 'info';
    case 'PENDING':
      return 'warning';
    case 'OFF_SHELF':
      return 'danger';
    default:
      return 'info';
  }
};

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'ON_SALE':
      return '在售';
    case 'SOLD':
      return '已售出';
    case 'PENDING':
      return '审核中';
    case 'OFF_SHELF':
      return '已下架';
    default:
      return status;
  }
};

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size;
  currentPage.value = 1;
  loadFavorites();
};

const handleCurrentChange = (page) => {
  currentPage.value = page;
  loadFavorites();
};

// 页面挂载时加载数据
onMounted(() => {
  loadFavorites();
});
</script>

<style scoped src="../../styles/pages/user-favorites.css"></style>
