<template>
  <div class="orders-page">
    <PageHeader :title="'我的订单'" :subtitle="pageSubtitle">
      <template #action>
        <div class="view-switch">
          <button
            class="view-btn"
            :class="{ active: currentView === 'buyer' }"
            @click="setView('buyer')"
          >
            我买到的
          </button>
          <button
            class="view-btn"
            :class="{ active: currentView === 'seller' }"
            @click="setView('seller')"
          >
            我卖出的
          </button>
        </div>
      </template>
    </PageHeader>

    <FilterTabs v-model="currentTab" :tabs="tabs" @change="handleTabChange" />

    <div class="orders-panel" v-loading="loading">
      <div class="orders-list" v-if="orders.length > 0">
        <div v-for="order in orders" :key="order.id" class="order-card">
          <div class="order-header">
            <div class="order-info">
              <span class="order-number">订单号：{{ order.orderNo }}</span>
              <span class="order-time">{{ formatTime(order.createdAt) }}</span>
            </div>
            <span class="order-status" :class="getOrderStatusClass(order.orderStatus)">
              {{ getOrderStatusText(order.orderStatus) }}
            </span>
          </div>

          <div class="order-body">
            <div class="order-item" @click="$router.push(`/item/${order.itemId}`)">
              <img
                :src="order.itemCover || fallbackCover"
                :alt="order.itemTitle"
                class="item-image"
              />
              <div class="item-details">
                <h4 class="item-title">{{ order.itemTitle }}</h4>
                <p class="item-meta">{{ currentViewLabel }}</p>
              </div>
            </div>

            <div class="order-price">
              <span class="price-label">金额</span>
              <span class="price-value">¥{{ formatPrice(order.price) }}</span>
            </div>

            <div class="order-actions">
              <p v-if="getOrderHint(order)" class="order-hint">
                {{ getOrderHint(order) }}
              </p>
              <div class="action-buttons">
                <el-button
                  v-for="action in getActions(order)"
                  :key="action.key"
                  :type="action.type"
                  size="small"
                  :class="(action as any).className"
                  @click="handleAction(action.key, order)"
                >
                  {{ action.label }}
                </el-button>
                <el-button
                  size="small"
                  plain
                  @click="viewDetail(order)"
                  class="detail-btn"
                >
                  查看商品
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <EmptyState
        v-else-if="!loading"
        :title="emptyTitle"
        :description="emptyDesc"
      >
        <template #action>
          <router-link to="/items" class="empty-action-btn">浏览物品</router-link>
        </template>
      </EmptyState>
    </div>

    <div class="pagination-wrap" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        layout="prev, pager, next"
        :total="total"
        @current-change="handlePageChange"
        class="custom-pagination"
      />
    </div>

    <el-dialog
      v-model="showReviewDialog"
      title="评价商品"
      width="480px"
      class="review-dialog"
    >
      <div class="review-form">
        <div class="review-item-info">
          <img
            :src="currentReviewOrder?.itemCover || fallbackCover"
            class="review-item-image"
          />
          <span class="review-item-title">
            {{ currentReviewOrder?.itemTitle }}
          </span>
        </div>
        <div class="rating-section">
          <span class="rating-label">商品评分</span>
          <div class="rating-stars">
            <Star
              v-for="i in 5"
              :key="i"
              :size="32"
              :fill="i <= reviewRating ? 'var(--accent-color)' : 'var(--border-default)'"
              :color="i <= reviewRating ? 'var(--accent-color)' : 'var(--border-default)'"
              class="star-icon"
              @click="reviewRating = i"
            />
          </div>
        </div>
        <el-input
          v-model="reviewContent"
          type="textarea"
          :rows="4"
          placeholder="分享你的购买体验..."
          maxlength="200"
          show-word-limit
        />
      </div>
      <template #footer>
        <el-button @click="showReviewDialog = false">取消</el-button>
        <el-button type="primary" @click="submitReview" class="submit-review-btn">
          提交评价
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import api from '../api';
import { Star } from 'lucide-vue-next';
import {
  getOrderActions,
  getOrderHint as buildOrderHint,
  getOrderStatusClass,
  getOrderStatusOptions,
  getOrderStatusText,
  normalizeOrder,
  sanitizeOrderStatus,
  sanitizeOrderView,
} from '../utils/business/orderFlow';
import PageHeader from '../components/user/PageHeader.vue';
import FilterTabs from '../components/user/FilterTabs.vue';
import EmptyState from '../components/user/EmptyState.vue';

const fallbackCover =
  'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=placeholder&image_size=square';

const route = useRoute();
const router = useRouter();

const currentView = ref(sanitizeOrderView(route.query.view as string));
const currentTab = ref(
  sanitizeOrderStatus(route.query.status as string)
);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const orders = ref<any[]>([]);
const loading = ref(false);
const showReviewDialog = ref(false);
const currentReviewOrder = ref<any>(null);
const reviewRating = ref(5);
const reviewContent = ref('');

const pageSubtitle = computed(() =>
  currentView.value === 'buyer' ? '跟进支付、收货与评价' : '处理发货与成交进度'
);

const currentViewLabel = computed(() =>
  currentView.value === 'buyer' ? '买家视角' : '卖家视角'
);

const tabs = computed(() =>
  getOrderStatusOptions(currentView.value).map((tab) => ({
    ...tab,
    count: tab.value === 'ALL' ? total.value : undefined,
  }))
);

const emptyTitle = computed(() =>
  currentView.value === 'buyer' ? '暂无买到的订单' : '暂无卖出的订单'
);

const emptyDesc = computed(() =>
  currentView.value === 'buyer'
    ? '你还没有相关订单，去挑一件心仪的闲置吧'
    : '暂时还没有成交订单，商品卖出后会在这里出现'
);

const formatTime = (time: string) => {
  if (!time) return '';
  return new Date(time).toLocaleDateString();
};

const formatPrice = (price: number) => {
  const numericPrice = Number(price || 0);
  return Number.isInteger(numericPrice)
    ? numericPrice.toString()
    : numericPrice.toFixed(2);
};

const syncQuery = async () => {
  const query: Record<string, string> = {};

  if (currentView.value === 'seller') {
    query.view = 'seller';
  }

  if (currentTab.value !== 'ALL') {
    query.status = currentTab.value;
  }

  await router.replace({ path: '/user/orders', query });
};

const loadOrders = async () => {
  loading.value = true;

  try {
    const params: Record<string, any> = {
      page: currentPage.value,
      size: pageSize.value,
    };

    if (currentTab.value !== 'ALL') {
      params.status = currentTab.value;
    }

    const response =
      currentView.value === 'seller'
        ? await api.order.getSellerOrders(params)
        : await api.order.getBuyerOrders(params);

    const pageData = response.data || {};
    orders.value = (pageData.content || []).map(normalizeOrder);
    total.value =
      pageData.totalElements ?? pageData.total ?? orders.value.length;
  } catch (error) {
    orders.value = [];
    total.value = 0;
    ElMessage.error(error.message || '获取订单失败');
  } finally {
    loading.value = false;
  }
};

const refreshOrders = async () => {
  await loadOrders();
};

const setView = async (view: string) => {
  const nextView = sanitizeOrderView(view);
  if (currentView.value === nextView) return;

  currentView.value = nextView;
  currentTab.value = 'ALL';
  currentPage.value = 1;
  await syncQuery();
  await loadOrders();
};

const handleTabChange = async (value: string) => {
  await setTab(value);
};

const setTab = async (status: string) => {
  const nextStatus = sanitizeOrderStatus(status);
  if (currentTab.value === nextStatus) return;

  currentTab.value = nextStatus;
  currentPage.value = 1;
  await syncQuery();
  await loadOrders();
};

const handlePay = async (order: any) => {
  try {
    await ElMessageBox.confirm('确认支付该订单？', '提示', { type: 'warning' });
    await api.order.payOrder(order.id);
    ElMessage.success('支付成功');
    await refreshOrders();
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error.message || '支付失败');
    }
  }
};

const handleCancel = async (order: any) => {
  try {
    await ElMessageBox.confirm('确认取消该订单？', '提示', { type: 'warning' });
    await api.order.cancelOrder(order.id, '用户主动取消');
    ElMessage.success('订单已取消');
    await refreshOrders();
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error.message || '取消失败');
    }
  }
};

const handleShip = async (order: any) => {
  try {
    await ElMessageBox.confirm('确认已准备好发货？', '提示', {
      type: 'warning',
    });
    await api.order.shipOrder(order.id);
    ElMessage.success('发货成功');
    await refreshOrders();
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error.message || '发货失败');
    }
  }
};

const handleConfirmReceive = async (order: any) => {
  try {
    await ElMessageBox.confirm('确认已收到货物？', '提示', { type: 'warning' });
    await api.order.confirmReceive(order.id);
    ElMessage.success('已确认收货');
    await refreshOrders();
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error.message || '确认收货失败');
    }
  }
};

const handleApplyRefund = async (order: any) => {
  try {
    const { value: reason } = await ElMessageBox.prompt(
      '请输入退款原因',
      '申请退款',
      {
        confirmButtonText: '提交申请',
        cancelButtonText: '取消',
        inputPlaceholder: '请描述退款原因...',
        inputValidator: (val) => (val && val.trim() ? true : '请输入退款原因'),
        inputType: 'textarea',
        type: 'warning',
      }
    );

    await api.order.applyRefund(order.id, { reason: reason.trim() });
    ElMessage.success('退款申请已提交');
    await refreshOrders();
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error.message || '退款申请失败');
    }
  }
};

const handleReview = (order: any) => {
  if (order.reviewed) {
    ElMessage.info('该订单已评价');
    return;
  }

  currentReviewOrder.value = order;
  reviewRating.value = 5;
  reviewContent.value = '';
  showReviewDialog.value = true;
};

const submitReview = async () => {
  if (!currentReviewOrder.value) return;

  if (!reviewContent.value.trim()) {
    ElMessage.warning('请输入评价内容');
    return;
  }

  try {
    await api.review.createReview(currentReviewOrder.value.id, {
      itemId: currentReviewOrder.value.itemId,
      rating: reviewRating.value,
      content: reviewContent.value.trim(),
      isAnonymous: false,
    });
    ElMessage.success('评价成功');
    showReviewDialog.value = false;
    await refreshOrders();
  } catch (error) {
    ElMessage.error(error.message || '评价失败');
  }
};

const handleAction = async (actionKey: string, order: any) => {
  switch (actionKey) {
    case 'pay':
      await handlePay(order);
      break;
    case 'cancel':
      await handleCancel(order);
      break;
    case 'ship':
      await handleShip(order);
      break;
    case 'confirmReceive':
      await handleConfirmReceive(order);
      break;
    case 'applyRefund':
      await handleApplyRefund(order);
      break;
    case 'review':
      handleReview(order);
      break;
    default:
      break;
  }
};

const getActions = (order: any) =>
  getOrderActions(order.orderStatus, currentView.value);

const getOrderHint = (order: any) =>
  buildOrderHint(order.orderStatus, currentView.value);

const viewDetail = (order: any) => {
  router.push(`/item/${order.itemId}`);
};

const handlePageChange = async (page: number) => {
  currentPage.value = page;
  await loadOrders();
};

onMounted(async () => {
  await syncQuery();
  await loadOrders();
});
</script>

<style scoped src="../styles/pages/orders.css"></style>
