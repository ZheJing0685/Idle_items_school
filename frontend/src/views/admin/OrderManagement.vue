<template>
  <div class="order-management">
    <div class="page-intro">
      <h2 class="section-title">订单管理</h2>
      <p class="section-desc">按统一状态机查看订单、处理退款审批和管理员取消</p>
    </div>

    <div class="stats-grid stats-grid-primary">
      <div
        v-for="card in primaryStatCards"
        :key="card.key"
        class="stat-card"
        :class="card.type"
      >
        <span class="stat-label">{{ card.label }}</span>
        <strong class="stat-value">{{ card.value }}</strong>
      </div>
    </div>
    <div class="stats-grid stats-grid-secondary">
      <div
        v-for="card in secondaryStatCards"
        :key="card.key"
        class="stat-card"
        :class="card.type"
      >
        <span class="stat-label">{{ card.label }}</span>
        <strong class="stat-value">{{ card.value }}</strong>
      </div>
    </div>

    <div class="content-card">
      <div class="toolbar">
        <div class="search-wrapper">
          <svg
            class="search-icon"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <circle cx="11" cy="11" r="8" />
            <path d="m21 21-4.35-4.35" />
          </svg>
          <input
            v-model="searchKeyword"
            class="search-input"
            type="text"
            placeholder="搜索订单号、物品名称或买家姓名"
            @keyup.enter="handleSearch"
          />
        </div>
        <select v-model="orderStatus" class="filter-select">
          <option
            v-for="option in ADMIN_ORDER_STATUS_OPTIONS"
            :key="option.value || 'all-status'"
            :value="option.value"
          >
            {{ option.label }}
          </option>
        </select>
        <select v-model="paymentMethod" class="filter-select">
          <option
            v-for="option in ADMIN_ORDER_PAYMENT_OPTIONS"
            :key="option.value || 'all-payment'"
            :value="option.value"
          >
            {{ option.label }}
          </option>
        </select>
        <button class="btn" @click="handleSearch">查询</button>
        <button class="btn btn-ghost" @click="handleReset">重置</button>
      </div>

      <div class="bulk-bar" v-if="selectedOrders.length">
        <span>已选择 {{ selectedOrders.length }} 项</span>
        <button class="btn btn-warning" @click="handleBulkCancel">
          批量取消
        </button>
      </div>

      <div class="table-wrap" v-loading="loading">
        <table class="order-table">
          <thead>
            <tr>
              <th>
                <input
                  type="checkbox"
                  :checked="isAllSelected"
                  @change="handleSelectAll"
                />
              </th>
              <th>订单号</th>
              <th>物品</th>
              <th>金额</th>
              <th>支付方式</th>
              <th>状态</th>
              <th>买家</th>
              <th>卖家</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in orders" :key="order.id">
              <td>
                <input
                  type="checkbox"
                  v-model="selectedOrders"
                  :value="order.id"
                  :disabled="!canAdminCancelOrder(order.orderStatus)"
                />
              </td>
              <td>
                <div class="mono">{{ order.orderNo }}</div>
                <div class="subtext">#{{ order.id }}</div>
              </td>
              <td>
                <div class="item-cell">
                  <img
                    :src="order.itemCover || fallbackCover"
                    :alt="order.itemTitle"
                    class="thumb"
                  />
                  <div>
                    <div>{{ order.itemTitle }}</div>
                    <div class="subtext">商品ID {{ order.itemId }}</div>
                  </div>
                </div>
              </td>
              <td class="price">¥{{ formatPrice(order.price) }}</td>
              <td>
                <div>{{ getAdminPaymentText(order.paymentMethod) }}</div>
                <div
                  :class="order.paymentTime ? 'status-paid' : 'status-unpaid'"
                  class="pay-flag"
                >
                  {{ order.paymentTime ? '已支付' : '未支付' }}
                </div>
              </td>
              <td>
                <span
                  class="badge"
                  :class="getAdminOrderStatusClass(order.orderStatus)"
                >
                  {{ getAdminOrderStatusText(order.orderStatus) }}
                </span>
                <div v-if="getStatusTime(order)" class="subtext">
                  {{ formatDateTime(getStatusTime(order)) }}
                </div>
              </td>
              <td>
                <div>{{ order.buyerName || `用户#${order.buyerId}` }}</div>
                <div class="subtext">{{ order.buyerPhone || '未填写' }}</div>
              </td>
              <td>卖家 #{{ order.sellerId }}</td>
              <td>{{ formatDateTime(order.createdAt) }}</td>
              <td>
                <div class="actions">
                  <button
                    v-for="action in getAdminOrderActions(order)"
                    :key="action.key"
                    class="action-btn"
                    :class="`action-btn-${action.tone}`"
                    @click="handleAction(action.key, order)"
                  >
                    {{ action.label }}
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="!loading && !orders.length">
              <td colspan="10" class="empty-cell">暂无符合条件的订单</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="footer-bar">
        <span
          >显示 {{ paginationStart }} - {{ paginationEnd }} 条，共
          {{ total }} 条</span
        >
        <div class="pager">
          <select
            v-model="pageSize"
            class="filter-select compact"
            @change="handleSizeChange"
          >
            <option :value="10">10 / 页</option>
            <option :value="20">20 / 页</option>
            <option :value="50">50 / 页</option>
          </select>
          <button
            class="btn btn-ghost"
            :disabled="page === 1"
            @click="changePage(page - 1)"
          >
            上一页
          </button>
          <span>{{ page }} / {{ totalPages }}</span>
          <button
            class="btn btn-ghost"
            :disabled="page >= totalPages"
            @click="changePage(page + 1)"
          >
            下一页
          </button>
        </div>
      </div>
    </div>

    <el-dialog v-model="detailDialogVisible" title="订单详情" width="680px">
      <div v-if="currentOrder" class="detail">
        <div class="detail-row">
          <span>订单号</span><strong>{{ currentOrder.orderNo }}</strong>
        </div>
        <div class="detail-row">
          <span>状态</span
          ><span
            class="badge"
            :class="getAdminOrderStatusClass(currentOrder.orderStatus)"
            >{{ getAdminOrderStatusText(currentOrder.orderStatus) }}</span
          >
        </div>
        <div class="detail-row">
          <span>物品</span><strong>{{ currentOrder.itemTitle }}</strong>
        </div>
        <div class="detail-row">
          <span>金额</span
          ><strong>¥{{ formatPrice(currentOrder.price) }}</strong>
        </div>
        <div class="detail-row">
          <span>支付方式</span
          ><span>{{ getAdminPaymentText(currentOrder.paymentMethod) }}</span>
        </div>
        <div class="detail-row">
          <span>买家</span
          ><span>{{
            currentOrder.buyerName || `用户#${currentOrder.buyerId}`
          }}</span>
        </div>
        <div class="detail-row">
          <span>电话</span
          ><span>{{ currentOrder.buyerPhone || '未填写' }}</span>
        </div>
        <div class="detail-row">
          <span>地址</span
          ><span>{{ currentOrder.buyerAddress || '未填写' }}</span>
        </div>
        <div v-if="currentOrder.cancelReason" class="remark">
          取消原因：{{ currentOrder.cancelReason }}
        </div>
        <div v-if="currentOrder.refundReason" class="remark">
          退款原因：{{ currentOrder.refundReason }}
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
          v-if="currentOrder && canAdminApproveRefund(currentOrder.orderStatus)"
          type="primary"
          @click="handleApproveRefund(currentOrder)"
          >审批退款</el-button
        >
        <el-button
          v-if="currentOrder && canAdminCancelOrder(currentOrder.orderStatus)"
          type="danger"
          @click="handleCancel(currentOrder)"
          >取消订单</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { userStore } from '../../store';
import api from '../../api';
import {
  ADMIN_ORDER_PAYMENT_OPTIONS,
  ADMIN_ORDER_STATUS_OPTIONS,
  canAdminApproveRefund,
  canAdminCancelOrder,
  getAdminOrderActions,
  getAdminOrderStatusClass,
  getAdminOrderStatusText,
  getAdminOrderStatusTime,
  getAdminPaymentText,
  normalizeAdminOrder,
} from '../../utils/business/adminOrderFlow';

const fallbackCover =
  'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=placeholder&image_size=square';
const store = userStore();
const searchKeyword = ref('');
const orderStatus = ref('');
const paymentMethod = ref('');
const loading = ref(false);
const orders = ref([]);
const selectedOrders = ref([]);
const page = ref(1);
const pageSize = ref(20);
const total = ref(0);
const detailDialogVisible = ref(false);
const currentOrder = ref(null);
const stats = ref({
  total: 0,
  pendingPayment: 0,
  pendingShipment: 0,
  shipped: 0,
  refundRequested: 0,
  completed: 0,
  amount: 0,
});

const totalPages = computed(() =>
  Math.max(1, Math.ceil(total.value / pageSize.value))
);
const paginationStart = computed(() =>
  total.value ? (page.value - 1) * pageSize.value + 1 : 0
);
const paginationEnd = computed(() =>
  Math.min(page.value * pageSize.value, total.value)
);
const isAllSelected = computed(
  () =>
    orders.value.filter((o) => canAdminCancelOrder(o.orderStatus)).length > 0 &&
    selectedOrders.value.length ===
      orders.value.filter((o) => canAdminCancelOrder(o.orderStatus)).length
);
const primaryStatCards = computed(() => [
  {
    key: 'total',
    label: '订单总数',
    type: 'card-total',
    value: stats.value.total,
  },
  {
    key: 'pendingPayment',
    label: '待支付',
    type: 'card-pending',
    value: stats.value.pendingPayment,
  },
  {
    key: 'pendingShipment',
    label: '待发货',
    type: 'card-pending',
    value: stats.value.pendingShipment,
  },
  {
    key: 'shipped',
    label: '待收货',
    type: 'card-info',
    value: stats.value.shipped,
  },
]);
const secondaryStatCards = computed(() => [
  {
    key: 'refundRequested',
    label: '退款中',
    type: 'card-danger',
    value: stats.value.refundRequested,
  },
  {
    key: 'completed',
    label: '已完成',
    type: 'card-success',
    value: stats.value.completed,
  },
  {
    key: 'amount',
    label: '成交总额',
    type: 'card-secondary',
    value: `¥${Number(stats.value.amount || 0).toLocaleString()}`,
  },
]);

const formatPrice = (price) => {
  const value = Number(price || 0);
  return Number.isInteger(value) ? `${value}` : value.toFixed(2);
};
const formatDateTime = (value) =>
  value ? new Date(value).toLocaleString() : '';
const getStatusTime = (order) => getAdminOrderStatusTime(order);

const fetchOrders = async () => {
  loading.value = true;
  try {
    const params = {
      page: page.value,
      size: pageSize.value,
      keyword: searchKeyword.value.trim() || undefined,
      status:
        orderStatus.value && orderStatus.value !== 'ALL'
          ? orderStatus.value
          : undefined,
      paymentMethod: paymentMethod.value || undefined,
    };
    const response = await api.admin.orders.getOrders(params);
    orders.value = (response.data.content || []).map(normalizeAdminOrder);
    total.value = response.data.totalElements || 0;
    selectedOrders.value = selectedOrders.value.filter((id) =>
      orders.value.some(
        (order) => order.id === id && canAdminCancelOrder(order.orderStatus)
      )
    );
  } catch (error) {
    ElMessage.error(error.message || '获取订单失败');
  } finally {
    loading.value = false;
  }
};

const fetchStats = async () => {
  const response = await api.admin.orders.getStats();
  stats.value = { ...stats.value, ...response.data };
};

const fetchOrderDetail = async (orderId) => {
  const response = await api.admin.orders.getOrder(orderId);
  return normalizeAdminOrder(response.data);
};

const refreshData = async () => {
  try {
    await Promise.all([fetchOrders(), fetchStats()]);
  } catch (error) {
    ElMessage.error(error.message || '刷新数据失败');
  }
};

const handleSearch = async () => {
  page.value = 1;
  await fetchOrders();
};
const handleReset = async () => {
  searchKeyword.value = '';
  orderStatus.value = '';
  paymentMethod.value = '';
  page.value = 1;
  await fetchOrders();
};
const handleSizeChange = async () => {
  page.value = 1;
  await fetchOrders();
};
const changePage = async (nextPage) => {
  page.value = nextPage;
  await fetchOrders();
};
const handleSelectAll = (event) => {
  selectedOrders.value = event.target.checked
    ? orders.value
        .filter((o) => canAdminCancelOrder(o.orderStatus))
        .map((order) => order.id)
    : [];
};

const handleView = async (order) => {
  try {
    currentOrder.value = await fetchOrderDetail(order.id);
    detailDialogVisible.value = true;
  } catch (error) {
    ElMessage.error(error.message || '获取详情失败');
  }
};

const handleApproveRefund = async (order) => {
  try {
    await ElMessageBox.confirm(
      `确认审批订单 ${order.orderNo} 的退款申请？`,
      '审批退款',
      { type: 'warning' }
    );
    const response = await api.admin.orders.approveRefund(order.id);
    if (currentOrder.value?.id === order.id)
      currentOrder.value = normalizeAdminOrder(response.data);
    ElMessage.success('退款已审批');
    await refreshData();
  } catch (error) {
    if (error !== 'cancel' && error !== 'close')
      ElMessage.error(error.message || '退款审批失败');
  }
};

const handleCancel = async (order) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入取消原因', '取消订单', {
      inputValidator: (input) => !!input || '原因不能为空',
    });
    const response = await api.admin.orders.cancelOrder(order.id, value);
    if (currentOrder.value?.id === order.id)
      currentOrder.value = normalizeAdminOrder(response.data);
    ElMessage.success('订单已取消');
    await refreshData();
  } catch (error) {
    if (error !== 'cancel' && error !== 'close')
      ElMessage.error(error.message || '取消失败');
  }
};

const handleBulkCancel = async () => {
  const cancellableIds = orders.value
    .filter(
      (order) =>
        selectedOrders.value.includes(order.id) &&
        canAdminCancelOrder(order.orderStatus)
    )
    .map((order) => order.id);
  if (!cancellableIds.length) {
    ElMessage.warning('请选择可取消的订单');
    return;
  }
  try {
    const { value } = await ElMessageBox.prompt(
      `将批量取消 ${cancellableIds.length} 个订单，请填写原因`,
      '批量取消订单',
      { inputValidator: (input) => !!input || '原因不能为空' }
    );
    await api.admin.orders.batchCancelOrders(cancellableIds, value);
    selectedOrders.value = [];
    ElMessage.success('批量取消成功');
    await refreshData();
  } catch (error) {
    if (error !== 'cancel' && error !== 'close')
      ElMessage.error(error.message || '批量取消失败');
  }
};

const handleAction = async (actionKey, order) => {
  if (actionKey === 'view') return handleView(order);
  if (actionKey === 'approveRefund') return handleApproveRefund(order);
  if (actionKey === 'cancel') return handleCancel(order);
};

onMounted(async () => {
  await refreshData();
});
</script>

<style scoped src="../../styles/pages/admin-order-management.css"></style>
