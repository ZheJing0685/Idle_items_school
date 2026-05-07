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
            <span v-if="tab.count !== null" class="tab-count">{{ tab.count }}</span>
          </button>
        </div>
      </div>

      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th class="col-user">用户</th>
              <th class="col-type">反馈类型</th>
              <th class="col-category">关联分类</th>
              <th class="col-desc">描述内容</th>
              <th class="col-status">状态</th>
              <th class="col-reply">管理员回复</th>
              <th class="col-date">提交时间</th>
              <th class="col-actions">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="feedback in feedbacks"
              :key="feedback.id"
              class="table-row"
            >
              <td class="col-user">
                <span class="user-name">{{ feedback.username || feedback.userId }}</span>
              </td>
              <td class="col-type">
                <span class="type-tag" :class="getTypeClass(feedback.type)">
                  {{ getTypeLabel(feedback.type) }}
                </span>
              </td>
              <td class="col-category">
                <span class="category-name">{{ feedback.categoryName || '无' }}</span>
              </td>
              <td class="col-desc">
                <span class="desc-text" :title="feedback.description">
                  {{ truncateText(feedback.description, 40) }}
                </span>
              </td>
              <td class="col-status">
                <span class="status-badge" :class="getStatusClass(feedback.status)">
                  {{ getStatusLabel(feedback.status) }}
                </span>
              </td>
              <td class="col-reply">
                <span class="reply-text" :title="feedback.adminReply">
                  {{ feedback.adminReply || '-' }}
                </span>
              </td>
              <td class="col-date">
                <span class="create-time">{{ formatDate(feedback.createdAt) }}</span>
              </td>
              <td class="col-actions">
                <div class="action-group">
                  <button
                    v-if="feedback.status === 'PENDING'"
                    class="action-btn action-primary"
                    @click="handleReview(feedback)"
                    title="审核"
                  >
                    <svg
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="1.5"
                    >
                      <path d="M9 11l3 3L22 4" />
                      <path d="M21 12v7a2 2 0 01-2 2H5a2 2 0 01-2-2V5a2 2 0 012-2h11" />
                    </svg>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="feedbacks.length === 0">
              <td colspan="8" class="empty-cell">
                <div class="empty-state">
                  <svg
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.5"
                  >
                    <path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2" />
                    <rect x="9" y="3" width="6" height="4" rx="1" />
                    <path d="M9 14l2 2 4-4" />
                  </svg>
                  <span>暂无反馈数据</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="pagination-wrapper">
        <div class="pagination-info">
          显示 {{ total === 0 ? 0 : (page - 1) * pageSize + 1 }} -
          {{ Math.min(page * pageSize, total) }} 条，共 {{ total }} 条
        </div>
        <div class="pagination-controls">
          <select
            v-model="pageSize"
            class="page-size-select"
            @change="handleSizeChange"
          >
            <option :value="10">10 条/页</option>
            <option :value="20">20 条/页</option>
            <option :value="50">50 条/页</option>
          </select>
          <div class="pagination-buttons">
            <button
              class="page-btn"
              :disabled="page === 1"
              @click="page--; fetchFeedbacks();"
            >
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path d="M15 18l-6-6 6-6" />
              </svg>
            </button>
            <span class="page-indicator">{{ page }} / {{ totalPages }}</span>
            <button
              class="page-btn"
              :disabled="page >= totalPages"
              @click="page++; fetchFeedbacks();"
            >
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path d="M9 18l6-6-6-6" />
              </svg>
            </button>
          </div>
        </div>
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
                <span class="type-tag" :class="getTypeClass(currentFeedback.type)">
                  {{ getTypeLabel(currentFeedback.type) }}
                </span>
              </span>
            </div>
            <div class="info-item">
              <span class="info-label">关联分类</span>
              <span class="info-value">{{ currentFeedback.categoryName || '无' }}</span>
            </div>
            <div class="info-item info-item-full">
              <span class="info-label">描述内容</span>
              <span class="info-value">{{ currentFeedback.description || '无' }}</span>
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

<script setup>
import { ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import api from '../../api';

const currentStatus = ref('');
const feedbacks = ref([]);
const page = ref(1);
const pageSize = ref(20);
const total = ref(0);
const reviewDialogVisible = ref(false);
const currentFeedback = ref(null);
const submitting = ref(false);

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

const getTypeLabel = (type) => feedbackTypeMap[type]?.label || type || '未知';
const getTypeClass = (type) => feedbackTypeMap[type]?.class || 'type-default';
const getStatusLabel = (status) => statusMap[status]?.label || status || '未知';
const getStatusClass = (status) => statusMap[status]?.class || '';

const truncateText = (text, length) => {
  if (!text) return '';
  return text.length > length ? text.slice(0, length) + '...' : text;
};

const formatDate = (dateString) => {
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
    const params = { page: page.value, size: pageSize.value };
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

const handleStatusChange = (status) => {
  currentStatus.value = status;
  page.value = 1;
  fetchFeedbacks();
};

const handleSizeChange = () => {
  page.value = 1;
  fetchFeedbacks();
};

const handleReview = (feedback) => {
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
      currentFeedback.value.id,
      {
        status: reviewForm.value.status,
        adminReply: reviewForm.value.adminReply,
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

onMounted(() => {
  fetchFeedbacks();
});
</script>

<style scoped src="../../styles/pages/admin-feedback-management.css"></style>
