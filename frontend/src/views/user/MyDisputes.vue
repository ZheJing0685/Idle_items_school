<template>
  <div class="my-disputes">
    <div class="page-header">
      <div class="header-info">
        <h2 class="page-title">我的纠纷</h2>
        <p class="page-desc">查看和管理您的交易纠纷</p>
      </div>
    </div>

    <div class="stats-cards">
      <div class="stat-item" :class="{ active: filterStatus === '' }" @click="filterStatus = ''; fetchDisputes()">
        <span class="stat-num">{{ stats.total || 0 }}</span>
        <span class="stat-label">全部纠纷</span>
      </div>
      <div class="stat-item pending" :class="{ active: filterStatus === 'PENDING' }" @click="filterStatus = 'PENDING'; fetchDisputes()">
        <span class="stat-num">{{ stats.pending || 0 }}</span>
        <span class="stat-label">待处理</span>
      </div>
      <div class="stat-item processing" :class="{ active: filterStatus === 'PROCESSING' }" @click="filterStatus = 'PROCESSING'; fetchDisputes()">
        <span class="stat-num">{{ stats.processing || 0 }}</span>
        <span class="stat-label">处理中</span>
      </div>
      <div class="stat-item resolved" :class="{ active: filterStatus === 'RESOLVED' }" @click="filterStatus = 'RESOLVED'; fetchDisputes()">
        <span class="stat-num">{{ stats.resolved || 0 }}</span>
        <span class="stat-label">已解决</span>
      </div>
    </div>

    <div class="dispute-list">
      <div v-if="loading" class="loading-state">
        <span class="loading-text">加载中...</span>
      </div>
      <div v-else-if="disputes.length === 0" class="empty-state">
        <el-empty description="暂无纠纷记录" />
      </div>
      <div v-else>
        <div v-for="dispute in disputes" :key="dispute.id" class="dispute-card" @click="viewDetail(dispute)">
          <div class="card-header">
            <span class="dispute-no">{{ dispute.disputeNo || '#' + dispute.id }}</span>
            <span class="dispute-status" :class="getStatusClass(dispute.disputeStatus)">
              {{ getStatusLabel(dispute.disputeStatus) }}
            </span>
          </div>
          <div class="card-body">
            <div class="order-info">
              <span class="order-label">订单</span>
              <span class="order-no">#{{ dispute.orderId }}</span>
              <span v-if="dispute.itemTitle" class="item-title">{{ dispute.itemTitle }}</span>
            </div>
            <div class="reason">
              <span class="reason-label">纠纷原因</span>
              <span class="reason-text">{{ dispute.reason }}</span>
            </div>
          </div>
          <div class="card-footer">
            <span class="time">{{ formatTime(dispute.createdAt) }}</span>
            <span class="action-hint">点击查看详情</span>
          </div>
        </div>
      </div>
    </div>

    <div v-if="total > 0" class="pagination">
      <el-pagination
        v-model:current-page="page"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="fetchDisputes"
      />
    </div>

    <el-dialog v-model="detailVisible" title="纠纷详情" width="650px" :close-on-click-modal="false">
      <div v-if="currentDispute" class="detail-content">
        <div class="detail-header">
          <span class="detail-no">{{ currentDispute.disputeNo || '#' + currentDispute.id }}</span>
          <span class="detail-status" :class="getStatusClass(currentDispute.disputeStatus)">
            {{ getStatusLabel(currentDispute.disputeStatus) }}
          </span>
        </div>

        <div class="detail-section">
          <h4 class="section-title">基本信息</h4>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">订单编号</span>
              <span class="info-value">#{{ currentDispute.orderId }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">纠纷类型</span>
              <span class="info-value">{{ getDisputeTypeLabel(currentDispute.disputeType) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">创建时间</span>
              <span class="info-value">{{ formatDateTime(currentDispute.createdAt) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">期望结果</span>
              <span class="info-value">{{ currentDispute.expectResult || '-' }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h4 class="section-title">纠纷描述</h4>
          <div class="description-box">{{ currentDispute.reason }}</div>
          <div v-if="currentDispute.description" class="description-box mt-10">{{ currentDispute.description }}</div>
        </div>

        <div v-if="currentDispute.result" class="detail-section">
          <h4 class="section-title">处理结果</h4>
          <div class="result-box">{{ currentDispute.result }}</div>
        </div>

        <div v-if="currentDispute.processLogs" class="detail-section">
          <h4 class="section-title">处理日志</h4>
          <div class="logs-box">
            <div v-for="(log, index) in parseLogs(currentDispute.processLogs)" :key="index" class="log-item">
              <span class="log-time">{{ log.time }}</span>
              <span class="log-content">{{ log.content }}</span>
            </div>
          </div>
        </div>

        <div v-if="currentDispute.satisfaction" class="detail-section">
          <h4 class="section-title">满意度评价</h4>
          <div class="satisfaction-box">
            <el-rate v-model="currentDispute.satisfaction" disabled text-color="var(--color-warning)" />
            <span v-if="currentDispute.satisfactionRemark" class="satisfaction-remark">
              {{ currentDispute.satisfactionRemark }}
            </span>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailVisible = false">关闭</el-button>
          <el-button v-if="currentDispute && canReply(currentDispute)" type="primary" @click="showReplyDialog">
            回复
          </el-button>
          <el-button v-if="currentDispute && canEvaluate(currentDispute)" type="success" @click="showEvaluateDialog">
            评价
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="replyVisible" title="回复纠纷" width="450px">
      <el-form :model="replyForm" label-width="80px">
        <el-form-item label="回复内容" required>
          <el-input v-model="replyForm.content" type="textarea" :rows="4" placeholder="请输入回复内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" :loading="replyLoading" @click="submitReply">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="evaluateVisible" title="满意度评价" width="450px">
      <el-form :model="evaluateForm" label-width="80px">
        <el-form-item label="满意度" required>
          <el-rate v-model="evaluateForm.score" show-text :texts="['非常不满意', '不满意', '一般', '满意', '非常满意']" />
        </el-form-item>
        <el-form-item label="评价备注">
          <el-input v-model="evaluateForm.remark" type="textarea" :rows="3" placeholder="请输入评价备注（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="evaluateVisible = false">取消</el-button>
        <el-button type="primary" :loading="evaluateLoading" @click="submitEvaluate">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import api from '../../api';

const disputes = ref([]);
const loading = ref(false);
const page = ref(1);
const pageSize = ref(10);
const total = ref(0);
const filterStatus = ref('');

const stats = ref({ total: 0, pending: 0, processing: 0, resolved: 0 });

const detailVisible = ref(false);
const currentDispute = ref(null);

const replyVisible = ref(false);
const replyForm = ref({ content: '' });
const replyLoading = ref(false);

const evaluateVisible = ref(false);
const evaluateForm = ref({ score: 5, remark: '' });
const evaluateLoading = ref(false);

const getStatusClass = (status) => {
  const map = {
    PENDING: 'status-pending',
    ASSIGNED: 'status-assigned',
    PROCESSING: 'status-processing',
    ESCALATED: 'status-escalated',
    RESOLVED: 'status-resolved',
    CLOSED: 'status-closed',
    CANCELLED: 'status-cancelled',
  };
  return map[status] || 'status-default';
};

const getStatusLabel = (status) => {
  const map = {
    PENDING: '待处理',
    ASSIGNED: '已分配',
    PROCESSING: '处理中',
    ESCALATED: '已升级',
    RESOLVED: '已解决',
    CLOSED: '已关闭',
    CANCELLED: '已取消',
  };
  return map[status] || status;
};

const getDisputeTypeLabel = (type) => {
  const map = { 1: '商品问题', 2: '物流问题', 3: '退款问题', 4: '其他' };
  return map[type] || '其他';
};

const formatTime = (dateStr) => {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  const now = new Date();
  const diff = now - date;
  if (diff < 60000) return '刚刚';
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前';
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前';
  return Math.floor(diff / 86400000) + '天前';
};

const formatDateTime = (dateStr) => {
  if (!dateStr) return '-';
  return new Date(dateStr).toLocaleString('zh-CN');
};

const parseLogs = (logsStr) => {
  try {
    return JSON.parse(logsStr);
  } catch {
    return [];
  }
};

const canReply = (dispute) => {
  return ['PENDING', 'ASSIGNED', 'PROCESSING'].includes(dispute.disputeStatus);
};

const canEvaluate = (dispute) => {
  return ['RESOLVED', 'CLOSED'].includes(dispute.disputeStatus) && !dispute.satisfaction;
};

const fetchDisputes = async () => {
  loading.value = true;
  try {
    const params = { page: page.value, size: pageSize.value };
    if (filterStatus.value) params.status = filterStatus.value;
    const res = await api.user.disputes.list(params);
    if (res.code === 200) {
      disputes.value = res.data.content || [];
      total.value = res.data.totalElements || 0;
    } else {
      ElMessage.error(res.message || '获取纠纷列表失败');
    }
  } catch (e) {
    ElMessage.error('网络错误');
  } finally {
    loading.value = false;
  }
};

const fetchStats = async () => {
  try {
    const res = await api.user.disputes.list({ size: 1 });
    if (res.code === 200) {
      stats.value.total = res.data.totalElements || 0;
    }
  } catch {}
  try {
    const res = await api.user.disputes.list({ status: 'PENDING', size: 1 });
    if (res.code === 200) {
      stats.value.pending = res.data.totalElements || 0;
    }
  } catch {}
  try {
    const res = await api.user.disputes.list({ status: 'PROCESSING', size: 1 });
    if (res.code === 200) {
      stats.value.processing = res.data.totalElements || 0;
    }
  } catch {}
  try {
    const res = await api.user.disputes.list({ status: 'RESOLVED', size: 1 });
    if (res.code === 200) {
      stats.value.resolved = res.data.totalElements || 0;
    }
  } catch {}
};

const viewDetail = async (dispute) => {
  try {
    const res = await api.user.disputes.get(dispute.id);
    if (res.code === 200) {
      currentDispute.value = res.data;
      detailVisible.value = true;
    } else {
      ElMessage.error(res.message || '获取详情失败');
    }
  } catch {
    ElMessage.error('网络错误');
  }
};

const showReplyDialog = () => {
  replyForm.value = { content: '' };
  replyVisible.value = true;
};

const submitReply = async () => {
  if (!replyForm.value.content.trim()) {
    ElMessage.warning('请输入回复内容');
    return;
  }
  replyLoading.value = true;
  try {
    const res = await api.user.disputes.reply(currentDispute.value.id, replyForm.value);
    if (res.code === 200) {
      ElMessage.success('回复成功');
      replyVisible.value = false;
      detailVisible.value = false;
      fetchDisputes();
    } else {
      ElMessage.error(res.message || '回复失败');
    }
  } catch {
    ElMessage.error('网络错误');
  } finally {
    replyLoading.value = false;
  }
};

const showEvaluateDialog = () => {
  evaluateForm.value = { score: 5, remark: '' };
  evaluateVisible.value = true;
};

const submitEvaluate = async () => {
  if (!evaluateForm.value.score) {
    ElMessage.warning('请选择满意度评分');
    return;
  }
  evaluateLoading.value = true;
  try {
    const res = await api.user.disputes.satisfaction(currentDispute.value.id, evaluateForm.value);
    if (res.code === 200) {
      ElMessage.success('评价成功');
      evaluateVisible.value = false;
      detailVisible.value = false;
      fetchDisputes();
    } else {
      ElMessage.error(res.message || '评价失败');
    }
  } catch {
    ElMessage.error('网络错误');
  } finally {
    evaluateLoading.value = false;
  }
};

onMounted(() => {
  fetchDisputes();
  fetchStats();
});
</script>

<style scoped src="../../styles/pages/user-dispute-management.css"></style>
<style scoped>
.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--content-primary);
  margin: 0 0 8px;
}

.page-desc {
  color: var(--content-secondary);
  margin: 0;
}

.stats-cards {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.stat-item {
  flex: 1;
  background: var(--surface-card);
  border-radius: 12px;
  padding: 16px;
  text-align: center;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s;
}

.stat-item:hover,
.stat-item.active {
  border-color: var(--primary-color);
}

.stat-item.active {
  background: var(--color-primary-alpha-10);
}

.stat-num {
  display: block;
  font-size: 28px;
  font-weight: 600;
  color: var(--content-primary);
}

.stat-label {
  font-size: 14px;
  color: var(--content-secondary);
}

.dispute-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dispute-card {
  background: var(--surface-card);
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: box-shadow 0.2s;
  border: 1px solid var(--bg-muted);
}

.dispute-card:hover {
  box-shadow: var(--shadow-md);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.dispute-no {
  font-weight: 600;
  color: var(--content-primary);
}

.dispute-status {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
}

.status-pending { background: var(--color-warning-alpha-10); color: var(--color-warning); }
.status-assigned { background: var(--color-info-alpha-10); color: var(--color-info); }
.status-processing { background: var(--color-success-alpha-10); color: var(--color-success); }
.status-escalated { background: var(--color-danger-alpha-10); color: var(--color-danger); }
.status-resolved { background: oklch(55% 0.15 280 / 0.1); color: oklch(50% 0.18 280); }
.status-closed { background: var(--surface-section); color: var(--content-secondary); }
.status-cancelled { background: var(--surface-section); color: var(--text-muted); }

.card-body {
  margin-bottom: 12px;
}

.order-info,
.reason {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 14px;
}

.order-label,
.reason-label {
  color: var(--text-muted);
  min-width: 70px;
}

.order-no {
  color: var(--primary-color);
}

.item-title {
  color: var(--content-secondary);
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.reason-text {
  color: var(--text-primary);
}

.card-footer {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--text-muted);
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.loading-state,
.empty-state {
  text-align: center;
  padding: 60px 0;
  color: var(--text-muted);
}

.detail-content {
  padding: 0 8px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-subtle);
  margin-bottom: 20px;
}

.detail-no {
  font-size: 18px;
  font-weight: 600;
}

.detail-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0 0 12px;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 12px;
  color: var(--text-muted);
}

.info-value {
  font-size: 14px;
  color: var(--text-primary);
}

.description-box,
.result-box {
  background: var(--bg-muted);
  border-radius: 8px;
  padding: 12px;
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.6;
}

.mt-10 {
  margin-top: 8px;
}

.logs-box {
  background: var(--bg-muted);
  border-radius: 8px;
  padding: 12px;
  max-height: 200px;
  overflow-y: auto;
}

.log-item {
  display: flex;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px solid var(--border-subtle);
  font-size: 13px;
}

.log-item:last-child {
  border-bottom: none;
}

.log-time {
  color: var(--text-muted);
  min-width: 140px;
}

.log-content {
  color: var(--text-primary);
}

.satisfaction-box {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.satisfaction-remark {
  font-size: 14px;
  color: var(--content-secondary);
}

.dialog-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}
</style>