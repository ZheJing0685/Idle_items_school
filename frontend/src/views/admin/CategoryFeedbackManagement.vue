<template>
  <div class="category-feedback-management">
    <div class="page-intro">
      <h2 class="section-title">分类反馈管理</h2>
      <p class="section-desc">查看并处理用户提交的分类反馈</p>
    </div>

    <div class="content-card">
      <div class="card-header">
        <div class="header-left">
          <h3 class="card-title">反馈列表</h3>
          <span class="data-range">共 {{ total }} 条记录</span>
        </div>
      </div>

      <div class="filters-bar">
        <div class="status-tabs">
          <button
            v-for="tab in statusTabs"
            :key="tab.value"
            class="status-tab"
            :class="{ active: currentStatus === tab.value }"
            @click="handleStatusChange(tab.value)"
          >
            {{ tab.label }}
            <span v-if="tab.count !== null" class="tab-count">{{
              tab.count
            }}</span>
          </button>
        </div>
      </div>

      <div class="table-wrapper">
        <el-table
          :data="feedbacks"
          style="width: 100%"
          @selection-change="handleSelectionChange"
          row-key="id"
        >
          <el-table-column type="selection" width="50" />
          <el-table-column label="用户" min-width="120">
            <template #default="{ row }">
              <span class="user-name">{{ row.username || row.userId }}</span>
            </template>
          </el-table-column>
          <el-table-column label="反馈类型" width="120">
            <template #default="{ row }">
              <span class="type-tag" :class="getTypeClass(row.type)">
                {{ getTypeLabel(row.type) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="关联分类" min-width="120">
            <template #default="{ row }">
              <span class="category-name">{{ row.categoryName || '无' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="描述内容" min-width="200">
            <template #default="{ row }">
              <span class="desc-text" :title="row.description">
                {{ truncateText(row.description, 40) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <span class="status-badge" :class="getStatusClass(row.status)">
                {{ getStatusLabel(row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="管理员回复" min-width="150">
            <template #default="{ row }">
              <span class="reply-text" :title="row.adminReply">
                {{ row.adminReply || '-' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="提交时间" width="160">
            <template #default="{ row }">
              <span class="create-time">{{ formatDate(row.createdAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" fixed="right">
            <template #default="{ row }">
              <button
                v-if="row.status === 'PENDING'"
                class="action-btn action-primary"
                @click="handleReview(row)"
                title="审核"
                aria-label="审核"
              >
                <ClipboardCheck :size="16" />
              </button>
            </template>
          </el-table-column>
          <template #empty>
            <div class="empty-state">
              <ClipboardCheck :size="48" />
              <span>暂无反馈数据</span>
            </div>
          </template>
        </el-table>
      </div>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="fetchFeedbacks"
        />
      </div>
    </div>

    <el-dialog
      v-model="reviewDialogVisible"
      title="审核反馈"
      width="560px"
      class="review-dialog"
    >
      <div class="review-detail" v-if="currentFeedback">
        <div class="detail-section">
          <h4 class="detail-title">反馈详情</h4>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">反馈类型</span>
              <span class="info-value">
                <span
                  class="type-tag"
                  :class="getTypeClass(currentFeedback.type)"
                >
                  {{ getTypeLabel(currentFeedback.type) }}
                </span>
              </span>
            </div>
            <div class="info-item">
              <span class="info-label">关联分类</span>
              <span class="info-value">{{
                currentFeedback.categoryName || '无'
              }}</span>
            </div>
            <div class="info-item info-item-full">
              <span class="info-label">描述内容</span>
              <span class="info-value">{{
                currentFeedback.description || '无'
              }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h4 class="detail-title">审核操作</h4>
          <div class="review-form">
            <div class="form-item">
              <label class="form-label">审核结果</label>
              <el-radio-group v-model="reviewForm.status">
                <el-radio value="ACCEPTED">采纳</el-radio>
                <el-radio value="REJECTED">拒绝</el-radio>
              </el-radio-group>
            </div>
            <div class="form-item">
              <label class="form-label">回复内容</label>
              <el-input
                v-model="reviewForm.adminReply"
                type="textarea"
                :rows="4"
                maxlength="500"
                show-word-limit
                placeholder="请输入回复内容（可选）"
              />
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="reviewDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            @click="handleSubmitReview"
            :loading="submitting"
          >
            确认
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import api from '../../api';
import { useDictStore } from '../../store/dict.js';
import { ClipboardCheck } from 'lucide-vue-next';

const dictStore = useDictStore();
const currentStatus = ref('');
const feedbacks = ref<any[]>([]);
const page = ref(1);
const pageSize = ref(20);
const total = ref(0);
const reviewDialogVisible = ref(false);
const currentFeedback = ref<{ id: number; status: string; type: string; categoryName?: string; description?: string; adminReply?: string; username?: string; userId?: number; createdAt?: string } | null>(null);
const submitting = ref(false);
const selectedFeedbacks = ref<any[]>([]);

const handleSelectionChange = (selection: any[]) => {
  selectedFeedbacks.value = selection;
};

const reviewForm = ref({
  status: 'ACCEPTED',
  adminReply: '',
});

const statusTabs = computed(() => [
  { label: '全部', value: '', count: null },
  { label: '待处理', value: 'PENDING', count: null },
  { label: '已采纳', value: 'ACCEPTED', count: null },
  { label: '已拒绝', value: 'REJECTED', count: null },
]);

const totalPages = computed(() => Math.ceil(total.value / pageSize.value) || 1);

const feedbackTypeMap = {
  INVALID_CATEGORY: { label: '分类无效', class: 'type-warning' },
  MISSING_CATEGORY: { label: '缺少分类', class: 'type-info' },
  OTHER: { label: '其他', class: 'type-default' },
};

const statusMap = {
  PENDING: { label: '待处理', class: 'status-pending' },
  ACCEPTED: { label: '已采纳', class: 'status-accepted' },
  REJECTED: { label: '已拒绝', class: 'status-rejected' },
};

const getTypeLabel = (type: string) => {
  // 将前端枚举值转换为后端枚举值
  const typeMap = {
    INVALID_CATEGORY: 'INVALID',
    MISSING_CATEGORY: 'MISSING',
  };
  const backendType = (typeMap as Record<string, string>)[type] || type;
  return dictStore.getDictLabel('CATEGORY_FEEDBACK_TYPE', backendType);
};
const getTypeClass = (type: string) => (feedbackTypeMap as Record<string, {class: string}>)[type]?.class || 'type-default';
const getStatusLabel = (status: string) => {
  return dictStore.getDictLabel('VERIFICATION_STATUS', status);
};
const getStatusClass = (status: string) => (statusMap as Record<string, {class: string}>)[status]?.class || '';

const truncateText = (text: string, length: number) => {
  if (!text) return '';
  return text.length > length ? text.slice(0, length) + '...' : text;
};

const formatDate = (dateString: string) => {
  if (!dateString) return '-';
  return new Date(dateString).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
};

const fetchFeedbacks = async () => {
  try {
    const params: Record<string, any> = { page: page.value, size: pageSize.value };
    if (currentStatus.value) params.status = currentStatus.value;

    const res = await api.admin.categories.getFeedbacks(params);
    if (res.code === 200) {
      feedbacks.value = res.data.content || [];
      total.value = res.data.totalElements || 0;
    }
  } catch {
    ElMessage.error('网络错误');
  }
};

const handleStatusChange = (status: string) => {
  currentStatus.value = status;
  page.value = 1;
  fetchFeedbacks();
};

const handleSizeChange = () => {
  page.value = 1;
  fetchFeedbacks();
};

const handleReview = (feedback: any) => {
  currentFeedback.value = { ...feedback };
  reviewForm.value = {
    status: 'ACCEPTED',
    adminReply: '',
  };
  reviewDialogVisible.value = true;
};

const handleSubmitReview = async () => {
  submitting.value = true;
  try {
    const res = await api.admin.categories.reviewFeedback(
      currentFeedback.value!.id,
      {
        status: reviewForm.value.status,
        reply: reviewForm.value.adminReply,
      }
    );
    if (res.code === 200) {
      ElMessage.success('审核完成');
      reviewDialogVisible.value = false;
      fetchFeedbacks();
    } else {
      ElMessage.error(res.message || '操作失败');
    }
  } catch {
    ElMessage.error('网络错误');
  } finally {
    submitting.value = false;
  }
};

onMounted(async () => {
  // 加载字典数据
  await dictStore.preloadCommonDicts();
  fetchFeedbacks();
});
</script>

<style scoped src="../../styles/pages/admin-feedback-management.css"></style>
