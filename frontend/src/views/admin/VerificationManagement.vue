<template>
  <div class="verification-management">
    <div class="page-intro">
      <h2 class="section-title">实名认证</h2>
      <p class="section-desc">审核用户的实名认证申请，确保平台用户身份真实性</p>
    </div>

    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon stat-icon-total">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.5"
          >
            <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2" />
            <circle cx="9" cy="7" r="4" />
            <path d="M23 21v-2a4 4 0 00-3-3.87M16 3.13a4 4 0 010 7.75" />
          </svg>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.total }}</span>
          <span class="stat-label">申请总数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-pending">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.5"
          >
            <circle cx="12" cy="12" r="10" />
            <path d="M12 6v6l4 2" />
          </svg>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.pending }}</span>
          <span class="stat-label">待审核</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-approved">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.5"
          >
            <path d="M9 12l2 2 4-4" />
            <circle cx="12" cy="12" r="10" />
          </svg>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.approved }}</span>
          <span class="stat-label">已通过</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-rejected">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.5"
          >
            <circle cx="12" cy="12" r="10" />
            <path d="M15 9l-6 6M9 9l6 6" />
          </svg>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.rejected }}</span>
          <span class="stat-label">已拒绝</span>
        </div>
      </div>
    </div>

    <div class="content-card">
      <div class="card-header">
        <div class="header-left">
          <h3 class="card-title">认证申请</h3>
          <span class="data-range">共 {{ total }} 条记录</span>
        </div>
        <div class="header-actions">
          <button class="btn btn-primary" @click="handleRefresh" title="刷新">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
            >
              <path d="M21.5 2v6h-6M2.5 22v-6h6" />
              <path d="M2 12A10 10 0 1 0 22 12" />
            </svg>
            刷新
          </button>
        </div>
      </div>

      <div class="filters-bar">
        <div class="filter-search">
          <svg
            class="search-icon"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <circle cx="11" cy="11" r="8" />
            <path d="M21 21l-4.35-4.35" />
          </svg>
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索用户名、真实姓名或学号..."
            class="search-input"
            @keyup.enter="handleSearch"
          />
        </div>
        <div class="filter-selects">
          <select
            v-model="verificationStatus"
            class="filter-select"
            @change="handleSearch"
          >
            <option value="">全部状态</option>
            <option value="PENDING">待审核</option>
            <option value="APPROVED">已通过</option>
            <option value="REJECTED">已拒绝</option>
          </select>
          <select
            v-model="verificationType"
            class="filter-select"
            @change="handleSearch"
          >
            <option value="">全部类型</option>
            <option value="1">身份证认证</option>
            <option value="2">学生证认证</option>
            <option value="3">教师证认证</option>
          </select>
          <button class="btn btn-ghost btn-sm" @click="handleReset">
            重置
          </button>
        </div>
      </div>

      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th class="col-checkbox">
                <input
                  type="checkbox"
                  @change="handleSelectAll"
                  :checked="isAllSelected"
                />
              </th>
              <th class="col-user">用户</th>
              <th class="col-name">真实姓名</th>
              <th class="col-id">身份证号</th>
              <th class="col-student-id">学号</th>
              <th class="col-type">认证类型</th>
              <th class="col-id-card">证件照片</th>
              <th class="col-status">状态</th>
              <th class="col-submit">提交时间</th>
              <th class="col-actions">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="verification in verifications"
              :key="verification.id"
              class="table-row"
            >
              <td class="col-checkbox">
                <input
                  type="checkbox"
                  v-model="selectedItems"
                  :value="verification.id"
                />
              </td>
              <td class="col-user">
                <div class="user-cell">
                  <div class="user-avatar">
                    {{ verification.username?.charAt(0) || '用户' }}
                  </div>
                  <div class="user-info">
                    <span class="user-name">{{ verification.username }}</span>
                    <span class="user-id">ID: {{ verification.userId }}</span>
                  </div>
                </div>
              </td>
              <td class="col-name">
                <span class="name-value">{{ verification.realName }}</span>
              </td>
              <td class="col-id">
                <span class="id-value">{{
                  maskIdNumber(verification.idNumber)
                }}</span>
              </td>
              <td class="col-student-id">
                <span class="student-id-value">{{
                  verification.studentId || '-'
                }}</span>
              </td>
              <td class="col-type">
                <span class="type-value">{{
                  getTypeText(verification.verificationType)
                }}</span>
              </td>
              <td class="col-id-card">
                <div class="id-card-images">
                  <el-image
                    v-if="verification.idCardFront"
                    :src="verification.idCardFront"
                    class="id-card-thumb"
                    :preview-src-list="[verification.idCardFront]"
                    fit="cover"
                  />
                  <el-image
                    v-if="verification.idCardBack"
                    :src="verification.idCardBack"
                    class="id-card-thumb"
                    :preview-src-list="[verification.idCardBack]"
                    fit="cover"
                  />
                  <el-image
                    v-if="verification.studentCard"
                    :src="verification.studentCard"
                    class="id-card-thumb"
                    :preview-src-list="[verification.studentCard]"
                    fit="cover"
                  />
                  <el-image
                    v-if="verification.teacherCard"
                    :src="verification.teacherCard"
                    class="id-card-thumb"
                    :preview-src-list="[verification.teacherCard]"
                    fit="cover"
                  />
                </div>
              </td>
              <td class="col-status">
                <span
                  class="badge"
                  :class="getStatusClass(verification.status)"
                >
                  {{ getStatusText(verification.status) }}
                </span>
              </td>
              <td class="col-submit">
                <span class="date-value">{{
                  formatDate(verification.createdAt)
                }}</span>
              </td>
              <td class="col-actions">
                <div class="action-group">
                  <button
                    class="action-btn"
                    @click="handleView(verification)"
                    title="查看详情"
                  >
                    <svg
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="1.5"
                    >
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                      <circle cx="12" cy="12" r="3" />
                    </svg>
                  </button>
                  <button
                    v-if="verification.status === 'PENDING'"
                    class="action-btn action-success"
                    @click="handleApprove(verification)"
                    title="通过"
                  >
                    <svg
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="1.5"
                    >
                      <path d="M9 12l2 2 4-4" />
                      <circle cx="12" cy="12" r="10" />
                    </svg>
                  </button>
                  <button
                    v-if="verification.status === 'PENDING'"
                    class="action-btn action-danger"
                    @click="handleReject(verification)"
                    title="拒绝"
                  >
                    <svg
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="1.5"
                    >
                      <circle cx="12" cy="12" r="10" />
                      <path d="M15 9l-6 6M9 9l6 6" />
                    </svg>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div
        class="table-footer"
        v-if="selectedItems.length > 0 && hasPendingSelected"
      >
        <div class="selection-info">
          已选择 <strong>{{ selectedItems.length }}</strong> 项待审核申请
        </div>
        <div class="bulk-actions">
          <button class="btn btn-success" @click="handleBulkApprove">
            批量通过
          </button>
          <button class="btn btn-danger" @click="handleBulkReject">
            批量拒绝
          </button>
        </div>
      </div>

      <div class="pagination-wrapper">
        <div class="pagination-info">
          显示 {{ (page - 1) * pageSize + 1 }} -
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
            <option :value="100">100 条/页</option>
          </select>
          <div class="pagination-buttons">
            <button
              class="page-btn"
              :disabled="page === 1"
              @click="
                page--;
                fetchVerifications();
              "
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
              @click="
                page++;
                fetchVerifications();
              "
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

    <!-- 认证详情弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="认证详情"
      width="700px"
      :close-on-click-modal="false"
    >
      <div v-if="currentVerification" class="verification-detail">
        <div class="detail-section">
          <h4>基本信息</h4>
          <div class="detail-row">
            <span class="detail-label">用户ID：</span>
            <span class="detail-value">{{ currentVerification.userId }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">用户名：</span>
            <span class="detail-value">{{ currentVerification.username }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">真实姓名：</span>
            <span class="detail-value">{{ currentVerification.realName }}</span>
          </div>
          <!-- 身份证认证信息 -->
          <div
            v-if="
              currentVerification.verificationType == '1' ||
              currentVerification.verificationType == 1
            "
            class="detail-row"
          >
            <span class="detail-label">身份证号：</span>
            <span class="detail-value">{{
              currentVerification.idCard || '无'
            }}</span>
          </div>

          <!-- 学生证认证信息 -->
          <div
            v-if="
              currentVerification.verificationType == '2' ||
              currentVerification.verificationType == 2
            "
            class="detail-row"
          >
            <span class="detail-label">学号：</span>
            <span class="detail-value">{{
              currentVerification.studentId || '无'
            }}</span>
          </div>
          <div
            v-if="
              currentVerification.verificationType == '2' ||
              currentVerification.verificationType == 2
            "
            class="detail-row"
          >
            <span class="detail-label">学校：</span>
            <span class="detail-value">{{
              currentVerification.school || '无'
            }}</span>
          </div>

          <!-- 教师证认证信息 -->
          <div
            v-if="
              currentVerification.verificationType == '3' ||
              currentVerification.verificationType == 3
            "
            class="detail-row"
          >
            <span class="detail-label">教师证号：</span>
            <span class="detail-value">{{
              currentVerification.teacherId || '无'
            }}</span>
          </div>
          <div
            v-if="
              currentVerification.verificationType == '3' ||
              currentVerification.verificationType == 3
            "
            class="detail-row"
          >
            <span class="detail-label">学校：</span>
            <span class="detail-value">{{
              currentVerification.school || '无'
            }}</span>
          </div>

          <div class="detail-row">
            <span class="detail-label">认证类型：</span>
            <span class="detail-value">{{
              getTypeText(currentVerification.verificationType)
            }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">认证状态：</span>
            <span class="detail-value">
              <span
                class="badge"
                :class="getStatusClass(currentVerification.status)"
              >
                {{ getStatusText(currentVerification.status) }}
              </span>
            </span>
          </div>
          <div class="detail-row" v-if="currentVerification.rejectReason">
            <span class="detail-label">拒绝原因：</span>
            <span class="detail-value">{{
              currentVerification.rejectReason
            }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">提交时间：</span>
            <span class="detail-value">{{
              formatDateTime(currentVerification.createdAt)
            }}</span>
          </div>
          <div class="detail-row" v-if="currentVerification.updatedAt">
            <span class="detail-label">审核时间：</span>
            <span class="detail-value">{{
              formatDateTime(currentVerification.updatedAt)
            }}</span>
          </div>
        </div>
        <div class="detail-section" v-if="hasImages">
          <h4>证件照片</h4>
          <div class="image-grid">
            <!-- 身份证认证照片 -->
            <div
              v-if="
                currentVerification.verificationType == '1' ||
                currentVerification.verificationType == 1
              "
            >
              <div v-if="currentVerification.idCardFront" class="image-item">
                <el-image
                  :src="currentVerification.idCardFront"
                  class="detail-image"
                  :preview-src-list="[currentVerification.idCardFront]"
                  fit="cover"
                />
                <span class="image-caption">身份证正面</span>
              </div>
              <div v-if="currentVerification.idCardBack" class="image-item">
                <el-image
                  :src="currentVerification.idCardBack"
                  class="detail-image"
                  :preview-src-list="[currentVerification.idCardBack]"
                  fit="cover"
                />
                <span class="image-caption">身份证反面</span>
              </div>
            </div>

            <!-- 学生证认证照片 -->
            <div
              v-if="
                currentVerification.verificationType == '2' ||
                currentVerification.verificationType == 2
              "
            >
              <div v-if="currentVerification.studentCard" class="image-item">
                <el-image
                  :src="currentVerification.studentCard"
                  class="detail-image"
                  :preview-src-list="[currentVerification.studentCard]"
                  fit="cover"
                />
                <span class="image-caption">学生证</span>
              </div>
            </div>

            <!-- 教师证认证照片 -->
            <div
              v-if="
                currentVerification.verificationType == '3' ||
                currentVerification.verificationType == 3
              "
            >
              <div v-if="currentVerification.teacherCard" class="image-item">
                <el-image
                  :src="currentVerification.teacherCard"
                  class="detail-image"
                  :preview-src-list="[currentVerification.teacherCard]"
                  fit="cover"
                />
                <span class="image-caption">教师证</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import {
  ElMessage,
  ElMessageBox,
  ElDialog,
  ElImage,
  ElButton,
} from 'element-plus';
import api from '../../api';
import { useDictStore } from '../../store/dict.js';

const dictStore = useDictStore();
const searchKeyword = ref('');
const verificationStatus = ref('');
const verificationType = ref('');
const verifications = ref([]);
const selectedItems = ref([]);
const page = ref(1);
const pageSize = ref(20);
const total = ref(0);
const dialogVisible = ref(false);
const currentVerification = ref(null);

const stats = ref({ total: 0, pending: 0, approved: 0, rejected: 0 });

const totalPages = computed(() => Math.ceil(total.value / pageSize.value) || 1);
const isAllSelected = computed(
  () =>
    verifications.value.length > 0 &&
    selectedItems.value.length === verifications.value.length
);
const hasPendingSelected = computed(() =>
  selectedItems.value.some((id) =>
    verifications.value.find((v) => v.id === id && v.status === 'PENDING')
  )
);
const hasImages = computed(() => {
  if (!currentVerification.value) return false;

  const type = currentVerification.value.verificationType;
  if (type == '1' || type == 1) {
    // 身份证认证
    return !!(
      currentVerification.value.idCardFront ||
      currentVerification.value.idCardBack
    );
  } else if (type == '2' || type == 2) {
    // 学生证认证
    return !!currentVerification.value.studentCard;
  } else if (type == '3' || type == 3) {
    // 教师证认证
    return !!currentVerification.value.teacherCard;
  }
  return false;
});

const getStatusClass = (status) => {
  const map = {
    PENDING: 'badge-warning',
    APPROVED: 'badge-success',
    REJECTED: 'badge-danger',
  };
  return map[status] || 'badge-default';
};

const getStatusText = (status) => {
  return dictStore.getDictLabel('VERIFICATION_STATUS', status);
};

const getTypeText = (type) => {
  // 将数字类型转换为枚举值
  const typeMap = { 1: 'ID_CARD', 2: 'STUDENT_CARD', 3: 'TEACHER_CARD' };
  const typeEnum = typeMap[type] || type;
  return dictStore.getDictLabel('VERIFICATION_TYPE', typeEnum);
};

const maskIdNumber = (idNumber) => {
  if (!idNumber) return '-';
  return idNumber.replace(/(\d{4})\d+(\d{4})/, '$1**********$2');
};

const formatDate = (dateString) => {
  if (!dateString) return '-';
  return new Date(dateString).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
};

const formatDateTime = (dateString) => {
  if (!dateString) return '-';
  return new Date(dateString).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  });
};

const fetchVerifications = async () => {
  try {
    const params = { page: page.value, size: pageSize.value };
    if (searchKeyword.value) params.keyword = searchKeyword.value;
    if (verificationStatus.value) params.status = verificationStatus.value;
    if (verificationType.value) params.type = verificationType.value;

    const res = await api.admin.verifications.getVerifications(params);
    if (res.code === 200) {
      verifications.value = res.data.content || [];
      total.value = res.data.totalElements || 0;
    } else {
      ElMessage.error(res.message || '获取认证列表失败');
    }
  } catch (error) {
    ElMessage.error('网络错误，请稍后重试');
    console.error('Error fetching verifications:', error);
    verifications.value = [];
    total.value = 0;
  }
};

const fetchStats = async () => {
  try {
    const res = await api.admin.verifications.getStats();
    if (res.code === 200) {
      stats.value = res.data || {
        total: 0,
        pending: 0,
        approved: 0,
        rejected: 0,
      };
    }
  } catch (error) {
    console.error('Error fetching stats:', error);
    stats.value = { total: 0, pending: 0, approved: 0, rejected: 0 };
  }
};

const handleSearch = () => {
  page.value = 1;
  fetchVerifications();
};

const handleReset = () => {
  searchKeyword.value = '';
  verificationStatus.value = '';
  verificationType.value = '';
  page.value = 1;
  fetchVerifications();
};

const handleSizeChange = () => {
  page.value = 1;
  fetchVerifications();
};

const handleSelectAll = (e) => {
  selectedItems.value = e.target.checked
    ? verifications.value.map((v) => v.id)
    : [];
};

const handleRefresh = () => {
  fetchVerifications();
  fetchStats();
  ElMessage.success('已刷新数据');
};

const handleView = (verification) => {
  currentVerification.value = verification;
  dialogVisible.value = true;
};

const handleApprove = async (verification) => {
  try {
    await ElMessageBox.confirm(
      `通过用户 ${verification.username} 的认证申请？`,
      '确认通过',
      { type: 'success' }
    );

    const res = await api.admin.verifications.approve(verification.id);
    if (res.code === 200) {
      ElMessage.success('认证申请已通过');
      fetchVerifications();
      fetchStats();
    } else {
      ElMessage.error(res.message || '操作失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('网络错误，请稍后重试');
      console.error('Error approving verification:', error);
    }
  }
};

const handleReject = async (verification) => {
  try {
    const { value: reason } = await ElMessageBox.prompt(
      '请输入拒绝原因',
      '拒绝认证',
      {
        inputValidator: (v) => !!v || '原因不能为空',
      }
    );

    const res = await api.admin.verifications.reject(verification.id, reason);
    if (res.code === 200) {
      ElMessage.success('认证申请已拒绝');
      fetchVerifications();
      fetchStats();
    } else {
      ElMessage.error(res.message || '操作失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('网络错误，请稍后重试');
      console.error('Error rejecting verification:', error);
    }
  }
};

const handleBulkApprove = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要批量通过选中的 ${selectedItems.value.length} 个认证申请吗？`,
      '批量操作',
      { type: 'success' }
    );

    const res = await api.admin.verifications.batchApprove(selectedItems.value);
    if (res.code === 200) {
      ElMessage.success(`批量操作成功`);
      selectedItems.value = [];
      fetchVerifications();
      fetchStats();
    } else {
      ElMessage.error(res.message || '批量操作失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('网络错误，请稍后重试');
      console.error('Error in bulk approve:', error);
    }
  }
};

const handleBulkReject = async () => {
  try {
    const { value: reason } = await ElMessageBox.prompt(
      '请输入拒绝原因',
      '批量拒绝',
      {
        inputValidator: (v) => !!v || '原因不能为空',
      }
    );

    const res = await api.admin.verifications.batchReject(
      selectedItems.value,
      reason
    );
    if (res.code === 200) {
      ElMessage.success(`批量操作成功`);
      selectedItems.value = [];
      fetchVerifications();
      fetchStats();
    } else {
      ElMessage.error(res.message || '批量操作失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('网络错误，请稍后重试');
      console.error('Error in bulk reject:', error);
    }
  }
};

onMounted(async () => {
  // 加载字典数据
  await dictStore.preloadCommonDicts();
  fetchVerifications();
  fetchStats();
});
</script>

<style
  scoped
  src="../../styles/pages/admin-verification-management.css"
></style>
