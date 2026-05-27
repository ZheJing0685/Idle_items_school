<template>
  <div class="log-management">
    <div class="page-intro">
      <h2 class="section-title">操作日志</h2>
      <p class="section-desc">记录管理员在系统中的所有操作，便于审计追踪</p>
    </div>

    <div class="content-card">
      <div class="card-header">
        <div class="header-left">
          <h3 class="card-title">日志列表</h3>
          <span class="data-range">共 {{ total }} 条记录</span>
        </div>
        <div class="header-actions">
          <button class="btn btn-ghost" @click="handleRefresh" title="刷新">
            <RefreshCw :size="16" />
            刷新
          </button>
          <button class="btn btn-primary" @click="handleExport">
            <Download :size="16" />
            导出日志
          </button>
        </div>
      </div>

      <div class="filters-bar">
        <div class="filter-search">
          <Search :size="16" class="search-icon" />
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索操作内容、管理员或IP..."
            class="search-input"
            @keyup.enter="handleSearch"
          />
        </div>
        <div class="filter-selects">
          <select v-model="operationType" class="filter-select">
            <option value="">全部类型</option>
            <option value="USER">用户操作</option>
            <option value="ITEM">物品操作</option>
            <option value="ORDER">订单操作</option>
            <option value="CATEGORY">分类操作</option>
            <option value="VERIFICATION">认证操作</option>
            <option value="SYSTEM">系统操作</option>
          </select>
          <select v-model="logType" class="filter-select">
            <option value="">全部日志</option>
            <option value="ADMIN">管理员日志</option>
            <option value="OPERATION">操作日志</option>
          </select>
          <button class="btn btn-ghost btn-sm" @click="handleReset">
            重置
          </button>
        </div>
      </div>

      <div class="date-range-bar">
        <div class="date-range">
          <span class="date-label">时间范围：</span>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            class="filter-date-picker"
          />
        </div>
      </div>

      <div class="table-wrapper">
        <el-table
          :data="logs"
          style="width: 100%"
          @selection-change="handleSelectionChange"
          row-key="id"
        >
          <el-table-column type="selection" width="50" />
          <el-table-column label="时间" width="160">
            <template #default="{ row }">
              <span class="time-value">{{ formatDateTime(row.createdAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作人" width="120">
            <template #default="{ row }">
              <div class="admin-cell">
                <div class="admin-avatar">
                  {{ row.adminName?.charAt(0) || row.userName?.charAt(0) || '系' }}
                </div>
                <span class="admin-name">{{ row.adminName || row.userName || '系统' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="类型" width="80">
            <template #default="{ row }">
              <span class="badge" :class="getTypeClass(row.operationType)">
                {{ getTypeText(row.operationType) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <span class="action-text">{{ row.action }}</span>
            </template>
          </el-table-column>
          <el-table-column label="目标" width="130">
            <template #default="{ row }">
              <span class="target-text">{{ row.targetType }} #{{ row.targetId }}</span>
            </template>
          </el-table-column>
          <el-table-column label="详情" min-width="150">
            <template #default="{ row }">
              <button class="detail-btn" @click="handleViewDetails(row)">
                {{ truncateText(row.details, 30) }}
              </button>
            </template>
          </el-table-column>
          <el-table-column label="IP地址" width="130">
            <template #default="{ row }">
              <span class="ip-value">{{ row.ipAddress }}</span>
            </template>
          </el-table-column>
          <el-table-column label="用户代理" min-width="150">
            <template #default="{ row }">
              <el-popover placement="top" width="300" trigger="hover">
                <template #reference>
                  <span class="user-agent-preview">{{ truncateText(row.userAgent, 20) }}</span>
                </template>
                <span class="user-agent-full">{{ row.userAgent }}</span>
              </el-popover>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" fixed="right">
            <template #default="{ row }">
              <button
                class="action-btn"
                @click="handleViewDetails(row)"
                title="查看详情"
                aria-label="查看详情"
              >
                <Eye :size="16" />
              </button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[20, 50, 100, 200]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="fetchLogs"
        />
      </div>
    </div>

    <!-- 日志详情弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="日志详情"
      width="700px"
      :close-on-click-modal="false"
    >
      <div v-if="currentLog" class="log-detail">
        <div class="detail-section">
          <h4>基本信息</h4>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label">时间：</span>
              <span class="detail-value">{{
                formatDateTime(currentLog.createdAt)
              }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">操作人：</span>
              <span class="detail-value">{{
                currentLog.adminName || currentLog.userName || '系统'
              }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">操作类型：</span>
              <span class="detail-value">{{
                getTypeText(currentLog.operationType)
              }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">操作：</span>
              <span class="detail-value">{{ currentLog.action }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">目标：</span>
              <span class="detail-value"
                >{{ currentLog.targetType }} #{{ currentLog.targetId }}</span
              >
            </div>
            <div class="detail-item">
              <span class="detail-label">IP地址：</span>
              <span class="detail-value">{{ currentLog.ipAddress }}</span>
            </div>
          </div>
        </div>
        <div class="detail-section">
          <h4>操作详情</h4>
          <pre class="detail-content">{{
            formatDetails(currentLog.details)
          }}</pre>
        </div>
        <div class="detail-section" v-if="currentLog.userAgent">
          <h4>用户代理</h4>
          <div class="user-agent-content">{{ currentLog.userAgent }}</div>
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
import { ref, computed, onMounted, watch } from 'vue';
import {
  ElMessage,
  ElDialog,
  ElPopover,
  ElButton,
  ElDatePicker,
} from 'element-plus';
import api from '../../api';
import { RefreshCw, Download, Search, Eye } from 'lucide-vue-next';

const searchKeyword = ref('');
const operationType = ref('');
const logType = ref('');
const startDate = ref('');
const endDate = ref('');
const dateRange = ref(null);

watch(dateRange, (val) => {
  if (val) {
    startDate.value = val[0];
    endDate.value = val[1];
  } else {
    startDate.value = '';
    endDate.value = '';
  }
});
const logs = ref<any[]>([]);
const page = ref(1);
const pageSize = ref(50);
const total = ref(0);
const dialogVisible = ref(false);
const currentLog = ref<any>(null);
const selectedLogs = ref<any[]>([]);

const handleSelectionChange = (selection: any[]) => {
  selectedLogs.value = selection;
};

const totalPages = computed(() => Math.ceil(total.value / pageSize.value) || 1);

const getTypeClass = (type: string) => {
  const map: Record<string, string> = {
    USER: 'badge-primary',
    ITEM: 'badge-success',
    ORDER: 'badge-warning',
    CATEGORY: 'badge-info',
    VERIFICATION: 'badge-secondary',
    SYSTEM: 'badge-default',
  };
  return map[type] || 'badge-default';
};

const getTypeText = (type: string) => {
  const map: Record<string, string> = {
    USER: '用户',
    ITEM: '物品',
    ORDER: '订单',
    CATEGORY: '分类',
    VERIFICATION: '认证',
    SYSTEM: '系统',
  };
  return map[type] || type;
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
    second: '2-digit',
  });
};

const truncateText = (text: string, length: number) => {
  if (!text) return '-';
  return text.length > length ? text.slice(0, length) + '...' : text;
};

const formatDetails = (details: any) => {
  try {
    const parsed = typeof details === 'string' ? JSON.parse(details) : details;
    return JSON.stringify(parsed, null, 2);
  } catch {
    return details;
  }
};

const fetchLogs = async () => {
  try {
    const params: Record<string, any> = { page: page.value, size: pageSize.value };
    if (searchKeyword.value) params.keyword = searchKeyword.value;
    if (operationType.value) params.type = operationType.value;
    if (logType.value) params.logType = logType.value;
    if (startDate.value) params.startDate = startDate.value;
    if (endDate.value) params.endDate = endDate.value;

    const res = await api.admin.logs.getLogs(params);
    if (res.code === 200) {
      logs.value = res.data.content || [];
      total.value = res.data.totalElements || 0;
    } else {
      ElMessage.error(res.message || '获取日志列表失败');
    }
  } catch {
    ElMessage.error('网络错误，请稍后重试');
    logs.value = [];
    total.value = 0;
  }
};

const handleSearch = () => {
  page.value = 1;
  fetchLogs();
};

const handleDateSearch = () => {
  page.value = 1;
  fetchLogs();
};

const handleReset = () => {
  searchKeyword.value = '';
  operationType.value = '';
  logType.value = '';
  startDate.value = '';
  endDate.value = '';
  page.value = 1;
  fetchLogs();
};

const handleSizeChange = () => {
  page.value = 1;
  fetchLogs();
};

const handleRefresh = () => {
  fetchLogs();
  ElMessage.success('已刷新数据');
};

const handleViewDetails = (log: any) => {
  currentLog.value = log;
  dialogVisible.value = true;
};

const handleExport = async () => {
  try {
    const params: Record<string, any> = {};
    if (searchKeyword.value) params.keyword = searchKeyword.value;
    if (operationType.value) params.type = operationType.value;
    if (logType.value) params.logType = logType.value;
    if (startDate.value) params.startDate = startDate.value;
    if (endDate.value) params.endDate = endDate.value;

    const blob = await api.admin.logs.getExport(params);
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `logs_${new Date().toISOString().slice(0, 10)}.csv`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
    ElMessage.success('日志导出成功');
  } catch {
    ElMessage.error('导出失败，请稍后重试');
  }
};

onMounted(() => {
  fetchLogs();
});
</script>

<style scoped src="../../styles/pages/admin-log-management.css"></style>
