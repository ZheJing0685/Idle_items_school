<template>
  <div class="dispute-management">
    <div class="page-intro">
      <h2 class="section-title">纠纷管理</h2>
      <p class="section-desc">处理交易纠纷，保障买卖双方权益</p>
    </div>

    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon stat-icon-total">
          <AlertTriangle :size="24" />
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.total }}</span>
          <span class="stat-label">总纠纷数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-pending">
          <Clock :size="24" />
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.pending }}</span>
          <span class="stat-label">待处理</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-processing">
          <Loader :size="24" />
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.processing }}</span>
          <span class="stat-label">处理中</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-resolved">
          <CheckCircle :size="24" />
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
        <div class="header-actions">
          <button class="btn btn-ghost" @click="handleExport">
            <Download :size="16" />
            导出
          </button>
        </div>
      </div>

      <div class="filters-bar">
        <div class="filter-search">
          <Search :size="16" class="search-icon" />
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索纠纷编号、物品名称..."
            class="search-input"
            @keyup.enter="handleSearch"
          />
        </div>
        <div class="filter-selects">
          <el-select v-model="filterStatus" placeholder="全部状态" clearable @change="handleSearch">
            <el-option v-for="opt in disputeStatusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </div>
        <div class="filter-actions">
          <button class="btn btn-ghost btn-sm" @click="handleReset">重置</button>
        </div>
      </div>

      <div v-if="selectedDisputes.length > 0" class="batch-actions">
        <span class="batch-info">已选择 {{ selectedDisputes.length }} 项</span>
        <button class="btn btn-sm btn-primary" @click="handleBatchApprove" :disabled="batchLoading">
          <CheckCircle :size="14" />
          {{ batchLoading ? '处理中...' : '批量通过' }}
        </button>
        <button class="btn btn-sm btn-ghost" @click="handleBatchClose" :disabled="batchLoading">
          <XCircle :size="14" />
          {{ batchLoading ? '处理中...' : '批量关闭' }}
        </button>
      </div>

      <div class="table-wrapper">
        <el-table
          :data="disputes"
          row-key="id"
          stripe
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="50" />
          <el-table-column label="编号" width="90">
            <template #default="{ row }">
              <span class="id-value">#{{ row.id }}</span>
            </template>
          </el-table-column>
          <el-table-column label="物品" min-width="140">
            <template #default="{ row }">
              <span class="item-title">{{ row.itemTitle || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="买家" width="120">
            <template #default="{ row }">
              <span class="user-name">{{ row.buyerName || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="卖家" width="120">
            <template #default="{ row }">
              <span class="user-name">{{ row.sellerName || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="纠纷原因" min-width="160">
            <template #default="{ row }">
              <span class="reason-text">{{ truncateText(row.reason, 20) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <span class="badge" :class="getStatusClass(row.status)">
                {{ dictStore.getDictLabel('DISPUTE_STATUS', row.status) || getStatusLabel(row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="170">
            <template #default="{ row }">
              <span class="time-value">{{ formatDateTime(row.createdAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <div class="action-group">
                <button class="action-btn" @click="handleView(row)" title="查看详情" aria-label="查看详情">
                  <Eye :size="16" />
                </button>
                <button
                  v-if="row.status === 'PENDING'"
                  class="action-btn action-success"
                  @click="handleProcess(row)"
                  title="处理纠纷"
                  aria-label="处理纠纷"
                >
                  <CheckCircle :size="16" />
                </button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="pagination-wrapper">
        <div class="pagination-info">
          显示 {{ (page - 1) * pageSize + 1 }} - {{ Math.min(page * pageSize, total) }} 条，共 {{ total }} 条
        </div>
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="sizes, prev, pager, next, jumper"
          background
          small
          @current-change="fetchDisputes"
          @size-change="handleSizeChange"
        />
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

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import api from '../../api';
import { useDictStore } from '../../store/dict';
import {
  AlertTriangle, Clock, Loader, CheckCircle, Search, Eye, Download, XCircle,
} from 'lucide-vue-next';

const dictStore = useDictStore();

const searchKeyword = ref('');
const filterStatus = ref('');
const disputes = ref<any[]>([]);
const selectedDisputes = ref<any[]>([]);
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
const currentDispute = ref<any>(null);

const processDialogVisible = ref(false);
const processForm = ref({ result: '', remark: '' });
const processLoading = ref(false);
const batchLoading = ref(false);

const disputeStatusOptions = computed(() => dictStore.getDictOptions('DISPUTE_STATUS'));

const getStatusClass = (status: string) => {
  const map: Record<string, string> = {
    PENDING: 'badge-warning',
    PROCESSING: 'badge-primary',
    RESOLVED: 'badge-success',
    CLOSED: 'badge-default',
  };
  return map[status] || 'badge-default';
};

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    PENDING: '待处理',
    PROCESSING: '处理中',
    RESOLVED: '已解决',
    CLOSED: '已关闭',
  };
  return map[status] || status;
};

const formatDateTime = (dateString: string) => {
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

const truncateText = (text: string, length: number) => {
  if (!text) return '-';
  return text.length > length ? text.slice(0, length) + '...' : text;
};

const handleSelectionChange = (rows: any[]) => {
  selectedDisputes.value = rows;
};

const fetchDisputes = async () => {
  try {
    const params: Record<string, any> = { page: page.value, size: pageSize.value };
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

const handleView = (dispute: any) => {
  currentDispute.value = dispute;
  detailDialogVisible.value = true;
};

const handleProcess = (dispute: any) => {
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
    const response = await api.admin.disputes.handleDispute(currentDispute.value.id, {
      resolution: processForm.value.result,
      status: processForm.value.result === 'CLOSE' ? 'CLOSED' : 'RESOLVED',
    });
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

const handleBatchApprove = async () => {
  if (selectedDisputes.value.length === 0) return;
  try {
    await ElMessageBox.confirm(
      `确认批量通过选中的 ${selectedDisputes.value.length} 个纠纷？`,
      '批量通过',
      { type: 'warning' }
    );
  } catch {
    return;
  }
  batchLoading.value = true;
  try {
    const ids = selectedDisputes.value.map((d) => d.id);
    const response = await api.admin.disputes.batchApprove(ids);
    if (response.code === 200) {
      ElMessage.success(`已批量通过 ${selectedDisputes.value.length} 个纠纷`);
      selectedDisputes.value = [];
      fetchDisputes();
      fetchStats();
    } else {
      ElMessage.error(response.message || '批量处理失败');
    }
  } catch (error) {
    ElMessage.error('批量处理失败');
  } finally {
    batchLoading.value = false;
  }
};

const handleBatchClose = async () => {
  if (selectedDisputes.value.length === 0) return;
  try {
    await ElMessageBox.confirm(
      `确认批量关闭选中的 ${selectedDisputes.value.length} 个纠纷？`,
      '批量关闭',
      { type: 'warning' }
    );
  } catch {
    return;
  }
  batchLoading.value = true;
  try {
    const ids = selectedDisputes.value.map((d) => d.id);
    const response = await api.admin.disputes.batchClose(ids);
    if (response.code === 200) {
      ElMessage.success(`已批量关闭 ${selectedDisputes.value.length} 个纠纷`);
      selectedDisputes.value = [];
      fetchDisputes();
      fetchStats();
    } else {
      ElMessage.error(response.message || '批量关闭失败');
    }
  } catch (error) {
    ElMessage.error('批量关闭失败');
  } finally {
    batchLoading.value = false;
  }
};

const handleExport = async () => {
  try {
    const params: Record<string, any> = {};
    if (searchKeyword.value) params.keyword = searchKeyword.value;
    if (filterStatus.value) params.status = filterStatus.value;
    const blob = await api.admin.disputes.export(params);
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `纠纷列表_${new Date().toISOString().slice(0, 10)}.xlsx`;
    link.click();
    window.URL.revokeObjectURL(url);
    ElMessage.success('导出成功');
  } catch (error) {
    ElMessage.error('导出失败');
  }
};

onMounted(() => {
  fetchDisputes();
  fetchStats();
  dictStore.fetchDictByType('DISPUTE_STATUS');
});
</script>

<style src="../../styles/components/admin-filters.css"></style>
<style scoped src="../../styles/pages/admin-dispute-management.css"></style>
