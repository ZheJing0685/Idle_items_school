<template>
  <div class="items-page">
    <!-- 页面标题和操作 -->
    <div class="page-header">
      <div>
        <h2>我的发布</h2>
        <p class="subtitle">您发布的物品会显示在这里，方便您管理</p>
      </div>
      <el-button type="primary" @click="$router.push('/publish')" class="publish-button">
        <el-icon><Plus /></el-icon>
        发布新物品
      </el-button>
    </div>

    <!-- 状态筛选 -->
    <div class="filter-section">
      <el-radio-group v-model="statusFilter" @change="handleStatusChange">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="ON_SALE">在售</el-radio-button>
        <el-radio-button value="SOLD">已售出</el-radio-button>
        <el-radio-button value="PENDING">审核中</el-radio-button>
        <el-radio-button value="OFF_SHELF">已下架</el-radio-button>
      </el-radio-group>
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
      v-else-if="items.length === 0"
      description="您还没有发布任何物品"
      class="empty-state"
    >
      <el-button type="primary" @click="$router.push('/publish')">去发布物品</el-button>
    </el-empty>

    <!-- 物品列表 -->
    <div v-else class="items-grid">
      <el-card
        v-for="item in items"
        :key="item.id"
        class="item-card"
        hover
      >
        <!-- 物品图片 -->
        <div class="card-image">
          <el-image
            :src="item.images && item.images.length > 0 ? item.images[0] : 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=placeholder%20image%20for%20secondhand%20item&image_size=square'"
            fit="cover"
            @click="goToItemDetail(item.id)"
          />
        </div>

        <!-- 物品信息 -->
        <div class="card-content">
          <h3 class="item-title" @click="goToItemDetail(item.id)">{{ item.title }}</h3>
          <div class="item-price">¥{{ item.price }}</div>
          <div class="item-meta">
            <span class="view-count"><el-icon><View /></el-icon> {{ item.viewCount || 0 }}浏览</span>
            <span class="created-time">{{ formatDate(item.createdAt) }}</span>
          </div>
          <div class="item-status">
            <el-tag :type="getStatusType(item.status)">
              {{ getStatusText(item.status) }}
            </el-tag>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="card-actions">
          <el-button
            type="primary"
            size="small"
            @click="goToItemDetail(item.id)"
            class="view-button"
          >
            查看详情
          </el-button>
          <el-button
            type="info"
            size="small"
            @click="editItem(item.id)"
            class="edit-button"
            :disabled="item.status === 'SOLD'"
          >
            编辑
          </el-button>
          <el-button
            type="warning"
            size="small"
            @click="toggleShelf(item)"
            class="shelf-button"
          >
            {{ item.status === 'ON_SALE' ? '下架' : '上架' }}
          </el-button>
          <el-button
            type="danger"
            size="small"
            @click="deleteItem(item.id)"
            class="delete-button"
          >
            删除
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 分页 -->
    <div v-if="!loading && !error && items.length > 0" class="pagination">
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
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, View } from '@element-plus/icons-vue';
import api from '../../api';

// 响应式数据
const items = ref([]);
const loading = ref(false);
const error = ref('');
const currentPage = ref(1);
const pageSize = ref(12);
const total = ref(0);
const statusFilter = ref('');

// 加载物品列表
const loadItems = async () => {
  loading.value = true;
  error.value = '';
  try {
    const response = await api.user.getItems(statusFilter.value || undefined, currentPage.value, pageSize.value);
    if (response.code === 200) {
      items.value = response.data.content || [];
      total.value = response.data.totalElements || 0;
    } else {
      error.value = response.message || '加载物品失败';
    }
  } catch (err) {
    error.value = '网络错误，请稍后重试';
    console.error('加载物品失败:', err);
  } finally {
    loading.value = false;
  }
};

// 编辑物品
const editItem = (id) => {
  window.location.href = `/publish?id=${id}`;
};

// 删除物品
const deleteItem = async (id) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个物品吗？删除后无法恢复。',
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );
    
    // 这里添加删除物品的API调用
    // const response = await api.item.deleteItem(id);
    // if (response.code === 0) {
      // 从列表中移除
      items.value = items.value.filter(item => item.id !== id);
      total.value--;
      ElMessage.success('删除成功');
    // } else {
    //   ElMessage.error(response.message || '删除失败');
    // }
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('删除失败');
      console.error('删除物品失败:', err);
    }
  }
};

// 上下架操作
const toggleShelf = async (item) => {
  try {
    const newStatus = item.status === 'ON_SALE' ? 'OFF_SHELF' : 'ON_SALE';
    const action = newStatus === 'ON_SALE' ? '上架' : '下架';
    
    await ElMessageBox.confirm(
      `确定要${action}这个物品吗？`,
      `${action}确认`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info',
      }
    );
    
    // 这里添加上下架的API调用
    // const response = await api.item.updateStatus(item.id, newStatus);
    // if (response.code === 0) {
      // 更新本地状态
      item.status = newStatus;
      ElMessage.success(`${action}成功`);
    // } else {
    //   ElMessage.error(response.message || `${action}失败`);
    // }
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('操作失败');
      console.error('上下架操作失败:', err);
    }
  }
};

// 跳转到物品详情
const goToItemDetail = (id) => {
  window.location.href = `/item/${id}`;
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

// 状态筛选变化
const handleStatusChange = () => {
  currentPage.value = 1;
  loadItems();
};

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size;
  currentPage.value = 1;
  loadItems();
};

const handleCurrentChange = (page) => {
  currentPage.value = page;
  loadItems();
};

// 页面挂载时加载数据
onMounted(() => {
  loadItems();
});
</script>

<style scoped src="../../styles/pages/user-items.css"></style>
