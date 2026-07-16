<template>
  <div class="verification-management">
    <div class="page-intro">
      <h2 class="section-title">实名认证</h2>
      <p class="section-desc">审核用户的实名认证申请，确保平台用户身份真实性</p>
    </div>

    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon stat-icon-total">
          <Users :size="24" />
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.total }}</span>
          <span class="stat-label">申请总数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-pending">
          <Clock :size="24" />
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.pending }}</span>
          <span class="stat-label">待审核</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-approved">
          <CheckCircle :size="24" />
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.approved }}</span>
          <span class="stat-label">已通过</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-rejected">
          <XCircle :size="24" />
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
            <RefreshCw :size="16" />
            刷新
          </button>
        </div>
      </div>

      <div class="filters-bar">
        <div class="filter-search">
          <Search :size="16" class="search-icon" />
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索用户名、真实姓名或学号..."
            class="search-input"
            @keyup.enter="handleSearch"
          />
        </div>
        <div class="filter-selects">
          <el-select v-model="verificationStatus" placeholder="全部状态" clearable @change="handleSearch">
            <el-option value="PENDING" label="待审核" />
            <el-option value="APPROVED" label="已通过" />
            <el-option value="REJECTED" label="已拒绝" />
          </el-select>
          <el-select v-model="verificationType" placeholder="全部类型" clearable @change="handleSearch">
            <el-option value="1" label="身份证认证" />
            <el-option value="2" label="学生证认证" />
            <el-option value="3" label="教师证认证" />
          </el-select>
        </div>
        <div class="filter-actions">
          <button class="btn btn-ghost btn-sm" @click="handleReset">重置</button>
        </div>
      </div>

      <el-table
        :data="verifications"
        row-key="id"
        @selection-change="handleSelectionChange"
        stripe
        empty-text="暂无认证记录"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column label="用户" width="160">
          <template #default="{ row }">
            <div class="user-cell">
              <div class="user-avatar">
                {{ row.username?.charAt(0) || '用户' }}
              </div>
              <div class="user-info">
                <span class="user-name">{{ row.username }}</span>
                <span class="user-id">ID: {{ row.userId }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="真实姓名" width="110" prop="realName" />
        <el-table-column label="身份证号" width="180">
          <template #default="{ row }">
            <span class="id-value">{{ maskIdCard(row.idCard) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="学号" width="120" prop="studentId">
          <template #default="{ row }">
            {{ row.studentId || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="认证类型" width="110">
          <template #default="{ row }">
            <span class="type-value">{{ getTypeText(row.verificationType) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="证件照片" width="180">
          <template #default="{ row }">
            <div class="id-card-images">
              <el-image v-if="row.idCardFront" :src="row.idCardFront" class="id-card-thumb" :preview-src-list="[row.idCardFront]" fit="cover" />
              <el-image v-if="row.idCardBack" :src="row.idCardBack" class="id-card-thumb" :preview-src-list="[row.idCardBack]" fit="cover" />
              <el-image v-if="row.studentCard" :src="row.studentCard" class="id-card-thumb" :preview-src-list="[row.studentCard]" fit="cover" />
              <el-image v-if="row.teacherCard" :src="row.teacherCard" class="id-card-thumb" :preview-src-list="[row.teacherCard]" fit="cover" />
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <span class="badge" :class="getStatusClass(row.status)">
              {{ getStatusText(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="105">
          <template #default="{ row }">
            <span class="date-value">{{ formatDate(row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <div class="action-group">
              <button class="action-btn" @click="handleView(row)" title="查看详情" aria-label="查看详情">
                <Eye :size="16" />
              </button>
              <button v-if="row.status === 'PENDING'" class="action-btn action-success" @click="handleApprove(row)" title="通过" aria-label="通过">
                <CheckCircle :size="16" />
              </button>
              <button v-if="row.status === 'PENDING'" class="action-btn action-danger" @click="handleReject(row)" title="拒绝" aria-label="拒绝">
                <XCircle :size="16" />
              </button>
            </div>
          </template>
        </el-table-column>
      </el-table>

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
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="fetchVerifications"
          @size-change="handleSizeChange"
        />
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

<script setup lang="ts">
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
import { Users, Clock, CheckCircle, XCircle, RefreshCw, Search, Eye } from 'lucide-vue-next';

const dictStore = useDictStore();
const searchKeyword = ref('');
const verificationStatus = ref('');
const verificationType = ref('');
const verifications = ref<any[]>([]);
const selectedItems = ref<any[]>([]);
const page = ref(1);
const pageSize = ref(20);
const total = ref(0);
const dialogVisible = ref(false);
const currentVerification = ref<any>(null);

const stats = ref({ total: 0, pending: 0, approved: 0, rejected: 0 });

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

const getStatusClass = (status: string) => {
  const map = {
    PENDING: 'badge-warning',
    APPROVED: 'badge-success',
    REJECTED: 'badge-danger',
  };
  return (map as Record<string, string>)[status] || 'badge-default';
};

const getStatusText = (status: string) => {
  return dictStore.getDictLabel('VERIFICATION_STATUS', status);
};

const getTypeText = (type: string) => {
  // 将数字类型转换为枚举值
  const typeMap: Record<string, string> = { '1': 'ID_CARD', '2': 'STUDENT_CARD', '3': 'TEACHER_CARD' };
  const typeEnum = typeMap[type] || type;
  return dictStore.getDictLabel('VERIFICATION_TYPE', typeEnum);
};

const maskIdCard = (idCard: string) => {
  if (!idCard) return '-';
  if (idCard.length >= 14) return idCard.replace(/(\d{4})\d{10}(\d{4})/, '$1****$2');
  return idCard;
};

const formatDate = (dateString: string) => {
  if (!dateString) return '-';
  return new Date(dateString).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
};

const formatDateTime = (dateString: string) => {
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
    const params: Record<string, unknown> = { page: page.value, size: pageSize.value };
    if (searchKeyword.value) params.keyword = searchKeyword.value;
    if (verificationStatus.value) params.status = verificationStatus.value;
    if (verificationType.value) params.type = verificationType.value;

    const res = await api.admin.verifications.getVerifications(params);
    if (res.code === 200) {
      const data = res.data as any;
      verifications.value = data.content || [];
      total.value = data.totalElements || 0;
    } else {
      ElMessage.error(res.message || '获取认证列表失败');
    }
  } catch (error: any) {
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
      stats.value = (res.data as any) || {
        total: 0,
        pending: 0,
        approved: 0,
        rejected: 0,
      };
    }
  } catch (error: any) {
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

const handleSelectionChange = (selection: any[]) => {
  selectedItems.value = selection.map((v: any) => v.id);
};

const handleRefresh = () => {
  fetchVerifications();
  fetchStats();
  ElMessage.success('已刷新数据');
};

const handleView = (verification: any) => {
  currentVerification.value = verification;
  dialogVisible.value = true;
};

const handleApprove = async (verification: any) => {
  try {
    await ElMessageBox.confirm(
      `通过用户 ${verification.username} 的认证申请？`,
      '确认通过',
      { type: 'success' }
    );

    const res = await api.admin.verifications.approveVerification(verification.id);
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

const handleReject = async (verification: any) => {
  try {
    const { value: reason } = await ElMessageBox.prompt(
      '请输入拒绝原因',
      '拒绝认证',
      {
        inputValidator: (v) => !!v || '原因不能为空',
      }
    );

    const res = await api.admin.verifications.rejectVerification(verification.id, reason);
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

<style src="../../styles/components/admin-filters.css"></style>
<style
  scoped
  src="../../styles/pages/admin-verification-management.css"
></style>
