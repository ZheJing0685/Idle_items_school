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
      <div class="filters-bar">
        <div class="filter-search">
          <Search class="search-icon" :size="16" />
          <input
            v-model="searchKeyword"
            class="search-input"
            type="text"
            placeholder="搜索订单号、物品名称或买家姓名"
            @keyup.enter="handleSearch"
          />
        </div>
        <div class="filter-selects">
          <el-select v-model="orderStatus" placeholder="全部状态" clearable>
            <el-option
              v-for="option in ADMIN_ORDER_STATUS_OPTIONS"
              :key="option.value || 'all-status'"
              :value="option.value"
              :label="option.label"
            />
          </el-select>
          <el-select v-model="paymentMethod" placeholder="全部支付方式" clearable>
            <el-option
              v-for="option in ADMIN_ORDER_PAYMENT_OPTIONS"
              :key="option.value || 'all-payment'"
              :value="option.value"
              :label="option.label"
            />
          </el-select>
        </div>
        <div class="filter-actions">
          <button class="btn btn-ghost btn-sm" @click="handleReset">重置</button>
        </div>
      </div>

      <div class="bulk-bar" v-if="selectedOrders.length">
        <span>已选择 {{ selectedOrders.length }} 项</span>
        <button class="btn btn-warning" @click="handleBulkCancel" :disabled="bulkLoading">
          {{ bulkLoading ? '处理中...' : '批量取消' }}
        </button>
      </div>

      <el-table
        :data="orders"
        row-key="id"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        stripe
        empty-text="暂无符合条件的订单"
      >
        <el-table-column type="selection" width="50" :selectable="(row: any) => canAdminCancelOrder(row.orderStatus)" />
        <el-table-column label="订单号" width="200">
          <template #default="{ row }">
            <div class="mono">{{ row.orderNo }}</div>
            <div class="subtext">#{{ row.id }}</div>
          </template>
        </el-table-column>
        <el-table-column label="物品" min-width="200">
          <template #default="{ row }">
            <div class="item-cell">
              <img :src="row.itemCover || fallbackCover" :alt="row.itemTitle" class="thumb" loading="lazy" />
              <div>
                <div>{{ row.itemTitle }}</div>
                <div class="subtext">商品ID {{ row.itemId }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="100">
          <template #default="{ row }">
            <span class="price">¥{{ formatPrice(row.price) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="支付方式" width="120">
          <template #default="{ row }">
            <div>{{ getAdminPaymentText(row.paymentMethod) }}</div>
            <div :class="row.paymentTime ? 'status-paid' : 'status-unpaid'" class="pay-flag">
              {{ row.paymentTime ? '已支付' : '未支付' }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <span class="badge" :class="getAdminOrderStatusClass(row.orderStatus)">
              {{ getAdminOrderStatusText(row.orderStatus) }}
            </span>
            <div v-if="getStatusTime(row)" class="subtext">{{ formatDateTime(getStatusTime(row)) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="买家" width="150">
          <template #default="{ row }">
            <div>{{ row.buyerName || `用户#${row.buyerId}` }}</div>
            <div class="subtext">{{ row.buyerPhone || '未填写' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="卖家" width="100">
          <template #default="{ row }">
            卖家 #{{ row.sellerId }}
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="actions">
              <button v-for="action in getAdminOrderActions(row)" :key="action.key"
                class="action-btn" :class="`action-btn-${action.tone}`"
                @click="handleAction(action.key, row)">
                {{ action.label }}
              </button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="footer-bar">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="changePage"
          @size-change="handleSizeChange"
        />
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

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { userStore } from '../../store';
import api from '../../api';
import { Search } from 'lucide-vue-next';
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
const orders = ref<any[]>([]);
const selectedOrders = ref<any[]>([]);
const page = ref(1);
const pageSize = ref(20);
const total = ref(0);
const detailDialogVisible = ref(false);
const currentOrder = ref<any>(null);
const bulkLoading = ref(false);
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

const formatPrice = (price: number) => {
  const value = Number(price || 0);
  return Number.isInteger(value) ? `${value}` : value.toFixed(2);
};
const formatDateTime = (value: string) =>
  value ? new Date(value).toLocaleString() : '';
const getStatusTime = (order: any) => getAdminOrderStatusTime(order);

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

const fetchOrderDetail = async (orderId: number) => {
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
const changePage = async (nextPage: number) => {
  page.value = nextPage;
  await fetchOrders();
};
const handleSelectionChange = (selection: any[]) => {
  selectedOrders.value = selection.map((order: any) => order.id);
};

const handleView = async (order: any) => {
  try {
    currentOrder.value = await fetchOrderDetail(order.id);
    detailDialogVisible.value = true;
  } catch (error) {
    ElMessage.error(error.message || '获取详情失败');
  }
};

const handleApproveRefund = async (order: any) => {
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

const handleCancel = async (order: any) => {
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
    bulkLoading.value = true;
    await api.admin.orders.batchCancel(cancellableIds, value);
    selectedOrders.value = [];
    ElMessage.success(`已批量取消 ${cancellableIds.length} 个订单`);
    await refreshData();
  } catch (error) {
    if (error !== 'cancel' && error !== 'close')
      ElMessage.error(error.message || '批量取消失败');
  } finally {
    bulkLoading.value = false;
  }
};

const handleAction = async (actionKey: string, order: any) => {
  if (actionKey === 'view') return handleView(order);
  if (actionKey === 'approveRefund') return handleApproveRefund(order);
  if (actionKey === 'cancel') return handleCancel(order);
};

onMounted(async () => {
  await refreshData();
});
</script>

<style src="../../styles/components/admin-filters.css"></style>
<style scoped src="../../styles/pages/admin-order-management.css"></style>
