<template>
  <div class="my-feedbacks">
    <div class="page-header">
      <h2 class="page-title">我的反馈</h2>
      <el-button type="primary" @click="router.push('/feedback')">
        提交新反馈
      </el-button>
    </div>

    <div v-loading="loading" class="feedback-table-card">
      <el-table
        :data="feedbacks"
        stripe
        style="width: 100%"
        empty-text="暂无反馈记录"
      >
        <el-table-column label="反馈类型" width="120">
          <template #default="{ row }">
            <el-tag :type="feedbackTypeTag(row.feedbackType)" size="small">
              {{ feedbackTypeText(row.feedbackType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="关联分类" width="140">
          <template #default="{ row }">
            {{ row.categoryName || '无' }}
          </template>
        </el-table-column>
        <el-table-column label="描述" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.description }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="管理员回复"
          min-width="180"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            {{ row.adminReply || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>

      <div v-if="total > pageSize" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="loadFeedbacks"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import api from '../../api';

const router = useRouter();
const loading = ref(false);
const feedbacks = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);

const feedbackTypeText = (type) => {
  const map = { INVALID: '分类无效', MISSING: '缺少分类', OTHER: '其他' };
  return map[type] || type;
};

const feedbackTypeTag = (type) => {
  const map = { INVALID: 'danger', MISSING: 'warning', OTHER: 'info' };
  return map[type] || 'info';
};

const statusText = (status) => {
  const map = { PENDING: '待处理', ACCEPTED: '已采纳', REJECTED: '已拒绝' };
  return map[status] || status;
};

const statusTag = (status) => {
  const map = { PENDING: 'warning', ACCEPTED: 'success', REJECTED: 'danger' };
  return map[status] || 'info';
};

const formatTime = (time) => {
  if (!time) return '-';
  return new Date(time).toLocaleString('zh-CN');
};

const loadFeedbacks = async () => {
  loading.value = true;
  try {
    const res = await api.category.getMyFeedbacks({
      page: currentPage.value,
      size: pageSize.value,
    });
    if (res.code === 200) {
      feedbacks.value = res.data.content || res.data || [];
      total.value = res.data.totalElements || feedbacks.value.length || 0;
    }
  } catch {
    feedbacks.value = [];
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadFeedbacks();
});
</script>

<style scoped>
.my-feedbacks {
  padding: var(--space-6);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-6);
}

.page-title {
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.feedback-table-card {
  background: var(--bg-surface);
  border-radius: var(--radius-xl);
  padding: var(--space-6);
  box-shadow: var(--shadow-sm);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: var(--space-6);
}
</style>
