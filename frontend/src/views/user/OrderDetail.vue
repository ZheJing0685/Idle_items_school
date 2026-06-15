<template>
  <div class="order-detail-page">
    <PageHeader title="订单详情" subtitle="查看订单信息和纠纷状态" />

    <div class="detail-panel" v-loading="loading">
      <div v-if="order" class="detail-content">
        <div class="order-status-bar">
          <span class="status-badge" :class="statusClass">{{ statusText }}</span>
        </div>

        <div class="info-section">
          <h3 class="section-title">订单信息</h3>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">订单编号</span>
              <span class="info-value mono">#{{ order.orderNo }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">创建时间</span>
              <span class="info-value">{{ formatTime(order.createdAt) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">订单金额</span>
              <span class="info-value price">¥{{ formatPrice(order.price) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">订单状态</span>
              <span class="info-value">{{ statusText }}</span>
            </div>
          </div>
        </div>

        <div class="info-section">
          <h3 class="section-title">商品信息</h3>
          <div class="item-card" @click="$router.push(`/item/${order.itemId}`)">
            <img :src="order.itemImage || fallbackCover" :alt="order.itemTitle" class="item-image" loading="lazy" />
            <div class="item-details">
              <h4 class="item-title">{{ order.itemTitle }}</h4>
              <p class="item-price">¥{{ formatPrice(order.price) }}</p>
            </div>
            <div class="item-arrow">
              <ChevronRight :size="20" />
            </div>
          </div>
        </div>

        <div class="info-section" v-if="dispute">
          <h3 class="section-title">纠纷信息</h3>
          <div class="dispute-card" @click="$router.push('/user/disputes')">
            <div class="dispute-header">
              <span class="dispute-badge" :class="disputeStatusClass">{{ disputeStatusText }}</span>
              <span class="dispute-no">#{{ dispute.disputeNo }}</span>
            </div>
            <p class="dispute-reason">{{ dispute.reason }}</p>
            <div class="dispute-footer">
              <span class="dispute-time">{{ formatTime(dispute.createdAt) }}</span>
              <span class="dispute-link">查看详情 →</span>
            </div>
          </div>
        </div>

        <div class="info-section" v-if="dispute === null && canDispute !== null && canDispute.canDispute">
          <div class="action-card">
            <div class="action-text">
              <h4>遇到问题？</h4>
              <p>如果你对本次交易有任何疑问或不满，可以发起纠纷寻求帮助</p>
            </div>
            <el-button
              type="primary"
              size="large"
              class="action-btn-dispute"
              @click="handleCreateDispute"
            >
              申请纠纷
            </el-button>
          </div>
        </div>

        <div class="info-section" v-if="canDispute && !canDispute.canDispute && !dispute">
          <div class="no-dispute-card">
            <p class="no-dispute-text">{{ canDispute.reason || '当前订单状态不允许申请纠纷' }}</p>
          </div>
        </div>
      </div>

      <div v-else-if="!loading && !order" class="empty-state">
        <p>订单不存在或已删除</p>
        <el-button @click="$router.push('/user/orders')" class="back-btn">返回订单列表</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import api from '../../api';
import PageHeader from '../../components/user/PageHeader.vue';
import { ChevronRight } from 'lucide-vue-next';
import type { OrderDetail, OrderStatusValue } from '../../types/order';
import type { DisputeItem, DisputeStatus, CanDisputeResult } from '../../types/dispute';

const fallbackCover = 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=placeholder&image_size=square';

const route = useRoute();
const router = useRouter();

const loading = ref(true);
const order = ref<OrderDetail | null>(null);
const dispute = ref<DisputeItem | null>(null);
const canDispute = ref<CanDisputeResult | null>(null);

const statusTextMap: Record<OrderStatusValue, string> = {
  PENDING_PAYMENT: '待付款',
  PENDING_SHIPMENT: '待发货',
  SHIPPED: '已发货',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  REFUND_REQUESTED: '退款申请中',
  REFUNDED: '已退款',
};

const disputeStatusTextMap: Record<DisputeStatus, string> = {
  PENDING: '待处理',
  ASSIGNED: '已分配',
  PROCESSING: '处理中',
  ESCALATED: '已升级',
  RESOLVED: '已解决',
  CLOSED: '已关闭',
  CANCELLED: '已取消',
};

const statusClassMap: Record<string, string> = {
  PENDING_PAYMENT: 'status-pending',
  PENDING_SHIPMENT: 'status-processing',
  SHIPPED: 'status-shipped',
  COMPLETED: 'status-completed',
  CANCELLED: 'status-cancelled',
  REFUND_REQUESTED: 'status-refund',
  REFUNDED: 'status-refunded',
};

const disputeStatusClassMap: Record<DisputeStatus, string> = {
  PENDING: 'badge-warning',
  ASSIGNED: 'badge-info',
  PROCESSING: 'badge-primary',
  ESCALATED: 'badge-danger',
  RESOLVED: 'badge-success',
  CLOSED: 'badge-default',
  CANCELLED: 'badge-secondary',
};

const statusText = computed(() => (order.value ? (statusTextMap[order.value.orderStatus as OrderStatusValue] || '未知状态') : ''));
const statusClass = computed(() => (order.value ? (statusClassMap[order.value.orderStatus] || '') : ''));
const disputeStatusText = computed(() => (dispute.value ? (disputeStatusTextMap[dispute.value.disputeStatus] || '') : ''));
const disputeStatusClass = computed(() => (dispute.value ? (disputeStatusClassMap[dispute.value.disputeStatus] || '') : ''));

const formatTime = (time: string): string => {
  if (!time) return '';
  return new Date(time).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
};

const formatPrice = (price: number | string): string => {
  const numericPrice = Number(price || 0);
  return Number.isInteger(numericPrice) ? numericPrice.toString() : numericPrice.toFixed(2);
};

const handleCreateDispute = (): void => {
  router.push(`/user/create-dispute/${order.value!.id}`);
};

onMounted(async () => {
  const orderId = Number(route.params.orderId);
  if (!orderId) {
    loading.value = false;
    return;
  }

  try {
    const [orderRes, disputeRes, canDisputeRes] = await Promise.all([
      api.order.getOrder(orderId),
      (api.user.disputes.getByOrder(orderId) as Promise<any>).catch(() => null),
      (api.user.disputes.canDispute(orderId) as Promise<any>).catch(() => null),
    ]);

    order.value = (orderRes.data || orderRes) as OrderDetail;
    dispute.value = disputeRes?.data || null;
    canDispute.value = canDisputeRes?.data || null;
  } catch (error: unknown) {
    ElMessage.error(error instanceof Error ? error.message : '获取订单详情失败');
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.order-detail-page {
  max-width: 100%;
}

.detail-panel {
  min-height: 300px;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.order-status-bar {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-5);
  background: var(--bg-muted);
  border-radius: var(--radius-lg);
}

.status-badge {
  display: inline-block;
  padding: 6px 16px;
  border-radius: var(--radius-full);
  font-size: var(--text-sm);
  font-weight: 600;
}

.status-badge.status-pending { background: var(--color-warning-alpha-10); color: var(--color-warning); }
.status-badge.status-processing { background: var(--color-info-alpha-10); color: var(--color-info); }
.status-badge.status-shipped { background: var(--color-info-alpha-10); color: var(--color-info); }
.status-badge.status-completed { background: var(--color-success-alpha-10); color: var(--color-success); }
.status-badge.status-cancelled { background: var(--surface-ground); color: var(--content-tertiary); }
.status-badge.status-refund { background: var(--color-warning-alpha-10); color: var(--color-warning); }
.status-badge.status-refunded { background: var(--color-danger-alpha-10); color: var(--color-danger); }

.info-section {
  background: var(--bg-surface);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-subtle);
  padding: var(--space-5);
}

.section-title {
  font-size: var(--text-base);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--space-4) 0;
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--border-subtle);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-4);
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.info-label {
  font-size: var(--text-sm);
  color: var(--text-muted);
}

.info-value {
  font-size: var(--text-base);
  color: var(--text-primary);
  font-weight: 500;
}

.info-value.mono {
  font-family: monospace;
  font-size: var(--text-sm);
}

.info-value.price {
  color: var(--accent-color);
  font-weight: 700;
}

.item-card {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-3);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background 0.2s;
}

.item-card:hover {
  background: var(--bg-muted);
}

.item-image {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-md);
  object-fit: cover;
  background: var(--bg-muted);
}

.item-details {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: var(--text-base);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--space-1) 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-price {
  font-size: var(--text-lg);
  font-weight: 700;
  color: var(--accent-color);
  margin: 0;
}

.item-arrow {
  color: var(--text-muted);
  flex-shrink: 0;
}

.dispute-card {
  padding: var(--space-3);
  border-radius: var(--radius-md);
  background: var(--bg-muted);
  cursor: pointer;
  transition: background 0.2s;
}

.dispute-card:hover {
  background: var(--border-subtle);
}

.dispute-header {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-2);
}

.dispute-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: 500;
}

.dispute-badge.badge-warning { background: var(--color-warning-alpha-10); color: var(--color-warning); }
.dispute-badge.badge-info { background: var(--color-info-alpha-10); color: var(--color-info); }
.dispute-badge.badge-primary { background: var(--color-primary-alpha-10); color: var(--color-primary); }
.dispute-badge.badge-danger { background: var(--color-danger-alpha-10); color: var(--color-danger); }
.dispute-badge.badge-success { background: var(--color-success-alpha-10); color: var(--color-success); }
.dispute-badge.badge-default { background: var(--surface-ground); color: var(--content-tertiary); }
.dispute-badge.badge-secondary { background: var(--surface-ground); color: var(--content-tertiary); }

.dispute-no {
  font-family: monospace;
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.dispute-reason {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin: 0 0 var(--space-2) 0;
}

.dispute-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dispute-time {
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.dispute-link {
  font-size: var(--text-sm);
  color: var(--primary-color);
  font-weight: 500;
}

.action-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
}

.action-text h4 {
  font-size: var(--text-base);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--space-1) 0;
}

.action-text p {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin: 0;
}

.action-btn-dispute {
  flex-shrink: 0;
}

.no-dispute-card {
  padding: var(--space-4);
  text-align: center;
}

.no-dispute-text {
  font-size: var(--text-sm);
  color: var(--text-muted);
  margin: 0;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-12) 0;
  color: var(--text-muted);
}

.back-btn {
  margin-top: var(--space-2);
}

@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }

  .action-card {
    flex-direction: column;
    text-align: center;
  }
}
</style>
