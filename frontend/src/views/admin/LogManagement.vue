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
          <button class="btn btn-primary" @click="handleExport">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
            >
              <path
                d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4M7 10l5 5 5-5M12 15V3"
              />
            </svg>
            导出日志
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
        <table class="data-table">
          <thead>
            <tr>
              <th class="col-time">时间</th>
              <th class="col-admin">操作人</th>
              <th class="col-type">类型</th>
              <th class="col-action">操作</th>
              <th class="col-target">目标</th>
              <th class="col-detail">详情</th>
              <th class="col-ip">IP地址</th>
              <th class="col-user-agent">用户代理</th>
              <th class="col-actions">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="log in logs" :key="log.id" class="table-row">
              <td class="col-time">
                <span class="time-value">{{
                  formatDateTime(log.createdAt)
                }}</span>
              </td>
              <td class="col-admin">
                <div class="admin-cell">
                  <div class="admin-avatar">
                    {{
                      log.adminName?.charAt(0) ||
                      log.userName?.charAt(0) ||
                      '系'
                    }}
                  </div>
                  <span class="admin-name">{{
                    log.adminName || log.userName || '系统'
                  }}</span>
                </div>
              </td>
              <td class="col-type">
                <span class="badge" :class="getTypeClass(log.operationType)">
                  {{ getTypeText(log.operationType) }}
                </span>
              </td>
              <td class="col-action">
                <span class="action-text">{{ log.action }}</span>
              </td>
              <td class="col-target">
                <span class="target-text"
                  >{{ log.targetType }} #{{ log.targetId }}</span
                >
              </td>
              <td class="col-detail">
                <button class="detail-btn" @click="handleViewDetails(log)">
                  {{ truncateText(log.details, 30) }}
                </button>
              </td>
              <td class="col-ip">
                <span class="ip-value">{{ log.ipAddress }}</span>
              </td>
              <td class="col-user-agent">
                <el-popover placement="top" width="300" trigger="hover">
                  <template #reference>
                    <span class="user-agent-preview">{{
                      truncateText(log.userAgent, 20)
                    }}</span>
                  </template>
                  <span class="user-agent-full">{{ log.userAgent }}</span>
                </el-popover>
              </td>
              <td class="col-actions">
                <button
                  class="action-btn"
                  @click="handleViewDetails(log)"
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
              </td>
            </tr>
          </tbody>
        </table>
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
            <option :value="20">20 条/页</option>
            <option :value="50">50 条/页</option>
            <option :value="100">100 条/页</option>
            <option :value="200">200 条/页</option>
          </select>
          <div class="pagination-buttons">
            <button
              class="page-btn"
              :disabled="page === 1"
              @click="
                page--;
                fetchLogs();
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
                fetchLogs();
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

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { ElMessage, ElDialog, ElPopover, ElButton, ElDatePicker } from 'element-plus';
import api from '../../api';

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
const logs = ref([]);
const page = ref(1);
const pageSize = ref(50);
const total = ref(0);
const dialogVisible = ref(false);
const currentLog = ref(null);

const totalPages = computed(() => Math.ceil(total.value / pageSize.value) || 1);

const getTypeClass = (type) => {
  const map = {
    USER: 'badge-primary',
    ITEM: 'badge-success',
    ORDER: 'badge-warning',
    CATEGORY: 'badge-info',
    VERIFICATION: 'badge-secondary',
    SYSTEM: 'badge-default',
  };
  return map[type] || 'badge-default';
};

const getTypeText = (type) => {
  const map = {
    USER: '用户',
    ITEM: '物品',
    ORDER: '订单',
    CATEGORY: '分类',
    VERIFICATION: '认证',
    SYSTEM: '系统',
  };
  return map[type] || type;
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
    second: '2-digit',
  });
};

const truncateText = (text, length) => {
  if (!text) return '-';
  return text.length > length ? text.slice(0, length) + '...' : text;
};

const formatDetails = (details) => {
  try {
    const parsed = typeof details === 'string' ? JSON.parse(details) : details;
    return JSON.stringify(parsed, null, 2);
  } catch {
    return details;
  }
};

const fetchLogs = async () => {
  try {
    const params = { page: page.value, size: pageSize.value };
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

const handleViewDetails = (log) => {
  currentLog.value = log;
  dialogVisible.value = true;
};

const handleExport = async () => {
  try {
    const params = {};
    if (searchKeyword.value) params.keyword = searchKeyword.value;
    if (operationType.value) params.type = operationType.value;
    if (logType.value) params.logType = logType.value;
    if (startDate.value) params.startDate = startDate.value;
    if (endDate.value) params.endDate = endDate.value;

    const response = await api.admin.logs.getExport(params);
    const blob = response.data;
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
