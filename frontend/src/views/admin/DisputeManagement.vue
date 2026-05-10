<template>
  <div class="dispute-management">
    <div class="page-intro">
      <h2 class="section-title">纠纷管理</h2>
      <p class="section-desc">处理交易纠纷，保障买卖双方权益</p>
    </div>

    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon stat-icon-total">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.total }}</span>
          <span class="stat-label">总纠纷数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-pending">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <circle cx="12" cy="12" r="10" />
            <path d="M12 6v6l4 2" />
          </svg>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.pending }}</span>
          <span class="stat-label">待处理</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-processing">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M12 2v4m0 12v4M4.93 4.93l2.83 2.83m8.48 8.48l2.83 2.83M2 12h4m12 0h4M4.93 19.07l2.83-2.83m8.48-8.48l2.83-2.83" />
          </svg>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.processing }}</span>
          <span class="stat-label">处理中</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-resolved">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M9 12l2 2 4-4" />
            <circle cx="12" cy="12" r="10" />
          </svg>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.resolved }}</span>
          <span class="stat-label">已解决</span>
        </div>
      </div>
    </div>

    <div class="content-card">
      <div class="card-header">
        <div class="header-left">
          <h3 class="card-title">纠纷列表</h3>
          <span class="data-range">共 {{ total }} 条记录</span>
        </div>
      </div>

      <div class="filters-bar">
        <div class="filter-search">
          <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8" />
            <path d="M21 21l-4.35-4.35" />
          </svg>
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索纠纷编号、物品名称..."
            class="search-input"
            @keyup.enter="handleSearch"
          />
        </div>
        <div class="filter-selects">
          <select v-model="filterStatus" class="filter-select" @change="handleSearch">
            <option value="">全部状态</option>
            <option value="PENDING">待处理</option>
            <option value="PROCESSING">处理中</option>
            <option value="RESOLVED">已解决</option>
            <option value="CLOSED">已关闭</option>
          </select>
          <button class="btn btn-ghost btn-sm" @click="handleReset">重置</button>
        </div>
      </div>

      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th class="col-id">编号</th>
              <th class="col-item">物品</th>
              <th class="col-buyer">买家</th>
              <th class="col-seller">卖家</th>
              <th class="col-reason">纠纷原因</th>
              <th class="col-status">状态</th>
              <th class="col-time">创建时间</th>
              <th class="col-actions">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="dispute in disputes" :key="dispute.id" class="table-row">
              <td class="col-id">
                <span class="id-value">#{{ dispute.id }}</span>
              </td>
              <td class="col-item">
                <span class="item-title">{{ dispute.itemTitle || '-' }}</span>
              </td>
              <td class="col-buyer">
                <span class="user-name">{{ dispute.buyerName || '-' }}</span>
              </td>
              <td class="col-seller">
                <span class="user-name">{{ dispute.sellerName || '-' }}</span>
              </td>
              <td class="col-reason">
                <span class="reason-text">{{ truncateText(dispute.reason, 20) }}</span>
              </td>
              <td class="col-status">
                <span class="badge" :class="getStatusClass(dispute.status)">
                  {{ getStatusLabel(dispute.status) }}
                </span>
              </td>
              <td class="col-time">
                <span class="time-value">{{ formatDateTime(dispute.createdAt) }}</span>
              </td>
              <td class="col-actions">
                <div class="action-group">
                  <button class="action-btn" @click="handleView(dispute)" title="查看详情">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                      <circle cx="12" cy="12" r="3" />
                    </svg>
                  </button>
                  <button
                    v-if="dispute.status === 'PENDING'"
                    class="action-btn action-success"
                    @click="handleProcess(dispute)"
                    title="处理纠纷"
                  >
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                      <path d="M9 12l2 2 4-4" />
                      <circle cx="12" cy="12" r="10" />
                    </svg>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="pagination-wrapper">
        <div class="pagination-info">
          显示 {{ (page - 1) * pageSize + 1 }} - {{ Math.min(page * pageSize, total) }} 条，共 {{ total }} 条
        </div>
        <div class="pagination-controls">
          <select v-model="pageSize" class="page-size-select" @change="handleSizeChange">
            <option :value="10">10 条/页</option>
            <option :value="20">20 条/页</option>
            <option :value="50">50 条/页</option>
          </select>
          <div class="pagination-buttons">
            <button class="page-btn" :disabled="page === 1" @click="page--; fetchDisputes()">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M15 18l-6-6 6-6" />
              </svg>
            </button>
            <span class="page-indicator">{{ page }} / {{ totalPages }}</span>
            <button class="page-btn" :disabled="page >= totalPages" @click="page++; fetchDisputes()">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M9 18l6-6-6-6" />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 纠纷详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="纠纷详情" width="600px" :close-on-click-modal="false">
      <div v-if="currentDispute" class="dispute-detail">
        <div class="detail-grid">
          <div class="detail-item">
            <span class="detail-label">纠纷编号</span>
            <span class="detail-value">#{{ currentDispute.id }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">状态</span>
            <span class="detail-value">
              <span class="badge" :class="getStatusClass(currentDispute.status)">
                {{ getStatusLabel(currentDispute.status) }}
              </span>
            </span>
          </div>
          <div class="detail-item">
            <span class="detail-label">物品名称</span>
            <span class="detail-value">{{ currentDispute.itemTitle || '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">买家</span>
            <span class="detail-value">{{ currentDispute.buyerName || '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">卖家</span>
            <span class="detail-value">{{ currentDispute.sellerName || '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">创建时间</span>
            <span class="detail-value">{{ formatDateTime(currentDispute.createdAt) }}</span>
          </div>
          <div class="detail-item detail-full">
            <span class="detail-label">纠纷原因</span>
            <span class="detail-value">{{ currentDispute.reason || '-' }}</span>
          </div>
          <div class="detail-item detail-full" v-if="currentDispute.description">
            <span class="detail-label">详细描述</span>
            <span class="detail-value">{{ currentDispute.description }}</span>
          </div>
          <div class="detail-item detail-full" v-if="currentDispute.handleResult">
            <span class="detail-label">处理结果</span>
            <span class="detail-value">{{ currentDispute.handleResult }}</span>
          </div>
          <div class="detail-item detail-full" v-if="currentDispute.handleRemark">
            <span class="detail-label">处理说明</span>
            <span class="detail-value">{{ currentDispute.handleRemark }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
          v-if="currentDispute && currentDispute.status === 'PENDING'"
          type="primary"
          @click="handleProcess(currentDispute)"
        >处理纠纷</el-button>
      </template>
    </el-dialog>

    <!-- 处理纠纷对话框 -->
    <el-dialog v-model="processDialogVisible" title="处理纠纷" width="500px" :close-on-click-modal="false">
      <el-form :model="processForm" label-width="100px">
        <el-form-item label="处理结果" required>
          <el-select v-model="processForm.result" placeholder="请选择处理结果">
            <el-option label="同意退款" value="APPROVE_REFUND" />
            <el-option label="驳回" value="REJECT" />
            <el-option label="关闭" value="CLOSE" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理说明">
          <el-input v-model="processForm.remark" type="textarea" :rows="4" placeholder="请输入处理说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="processDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitProcess" :loading="processLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import api from '../../api';

const searchKeyword = ref('');
const filterStatus = ref('');
const disputes = ref([]);
const page = ref(1);
const pageSize = ref(20);
const total = ref(0);

const stats = ref({
  total: 0,
  pending: 0,
  processing: 0,
  resolved: 0,
});

const detailDialogVisible = ref(false);
const currentDispute = ref(null);

const processDialogVisible = ref(false);
const processForm = ref({ result: '', remark: '' });
const processLoading = ref(false);

const totalPages = computed(() => Math.ceil(total.value / pageSize.value) || 1);

const getStatusClass = (status) => {
  const map = {
    PENDING: 'badge-warning',
    PROCESSING: 'badge-primary',
    RESOLVED: 'badge-success',
    CLOSED: 'badge-default',
  };
  return map[status] || 'badge-default';
};

const getStatusLabel = (status) => {
  const map = {
    PENDING: '待处理',
    PROCESSING: '处理中',
    RESOLVED: '已解决',
    CLOSED: '已关闭',
  };
  return map[status] || status;
};

const formatDateTime = (dateString) => {
  if (!dateString) return '-';
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
};

const truncateText = (text, length) => {
  if (!text) return '-';
  return text.length > length ? text.slice(0, length) + '...' : text;
};

const fetchDisputes = async () => {
  try {
    const params = { page: page.value, size: pageSize.value };
    if (searchKeyword.value) params.keyword = searchKeyword.value;
    if (filterStatus.value) params.status = filterStatus.value;

    const response = await api.admin.disputes.list(params);
    if (response.code === 200) {
      disputes.value = response.data.content || [];
      total.value = response.data.totalElements || 0;
    } else {
      ElMessage.error(response.message || '获取纠纷列表失败');
    }
  } catch (error) {
    ElMessage.error('网络错误，请稍后重试');
    disputes.value = [];
    total.value = 0;
  }
};

const fetchStats = async () => {
  try {
    const response = await api.admin.disputes.stats();
    if (response.code === 200) {
      stats.value = response.data;
    }
  } catch (error) {
    stats.value = { total: 0, pending: 0, processing: 0, resolved: 0 };
  }
};

const handleSearch = () => {
  page.value = 1;
  fetchDisputes();
};

const handleReset = () => {
  searchKeyword.value = '';
  filterStatus.value = '';
  page.value = 1;
  fetchDisputes();
};

const handleSizeChange = () => {
  page.value = 1;
  fetchDisputes();
};

const handleView = (dispute) => {
  currentDispute.value = dispute;
  detailDialogVisible.value = true;
};

const handleProcess = (dispute) => {
  currentDispute.value = dispute;
  processForm.value = { result: '', remark: '' };
  processDialogVisible.value = true;
};

const submitProcess = async () => {
  if (!processForm.value.result) {
    ElMessage.warning('请选择处理结果');
    return;
  }
  processLoading.value = true;
  try {
    const response = await api.admin.disputes.handle(currentDispute.value.id, processForm.value);
    if (response.code === 200) {
      ElMessage.success('处理成功');
      processDialogVisible.value = false;
      detailDialogVisible.value = false;
      fetchDisputes();
      fetchStats();
    } else {
      ElMessage.error(response.message || '处理失败');
    }
  } catch (error) {
    ElMessage.error('处理失败');
  } finally {
    processLoading.value = false;
  }
};

onMounted(() => {
  fetchDisputes();
  fetchStats();
});
</script>

<style scoped src="../../styles/pages/admin-dispute-management.css"></style>
