<template>
  <div class="orders-page">
    <div class="page-header">
      <div class="container">
        <div class="header-content">
          <h1 class="page-title">我的订单</h1>
          <p class="page-subtitle">{{ pageSubtitle }}</p>
        </div>
      </div>
    </div>

    <div class="container">
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

      <div class="orders-tabs">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          class="tab-btn"
          :class="{ active: currentTab === tab.value }"
          @click="setTab(tab.value)"
        >
          {{ tab.label }}
          <span class="tab-count" v-if="tab.count !== undefined">
            {{ tab.count }}
          </span>
        </button>
      </div>

      <div class="orders-panel" v-loading="loading">
        <div class="orders-list" v-if="orders.length > 0">
          <div v-for="order in orders" :key="order.id" class="order-card">
            <div class="order-header">
              <div class="order-info">
                <span class="order-number">订单号：{{ order.orderNo }}</span>
                <span class="order-time">{{ formatTime(order.createdAt) }}</span>
              </div>
              <span
                class="order-status"
                :class="getOrderStatusClass(order.orderStatus)"
              >
                {{ getOrderStatusText(order.orderStatus) }}
              </span>
            </div>

            <div class="order-body">
              <div
                class="order-item"
                @click="$router.push(`/item/${order.itemId}`)"
              >
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
                    :class="action.className"
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

        <div class="empty-state" v-else-if="!loading">
          <div class="empty-icon">
            <svg
              width="64"
              height="64"
              viewBox="0 0 24 24"
              fill="none"
              stroke="var(--text-muted)"
              stroke-width="1.5"
            >
              <path
                d="M9 5H7C5.89543 5 5 5.89543 5 7V19C5 20.1046 5.89543 21 7 21H17C18.1046 21 19 20.1046 19 19V7C19 5.89543 18.1046 5 17 5H15"
              />
              <path
                d="M9 5C9 3.89543 9.89543 3 11 3H13C14.1046 3 15 3.89543 15 5C15 6.10457 14.1046 7 13 7H11C9.89543 7 9 6.10457 9 5Z"
              />
            </svg>
          </div>
          <h3 class="empty-title">{{ emptyTitle }}</h3>
          <p class="empty-desc">{{ emptyDesc }}</p>
          <router-link to="/items" class="empty-action">浏览物品</router-link>
        </div>
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
            <svg
              v-for="i in 5"
              :key="i"
              width="32"
              height="32"
              viewBox="0 0 24 24"
              :fill="
                i <= reviewRating
                  ? 'var(--accent-color)'
                  : 'var(--border-default)'
              "
              class="star-icon"
              @click="reviewRating = i"
            >
              <path
                d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"
              />
            </svg>
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
        <el-button
          type="primary"
          @click="submitReview"
          class="submit-review-btn"
        >
          提交评价
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import api from '../api';
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

const fallbackCover =
  'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=placeholder&image_size=square';

const route = useRoute();
const router = useRouter();

const currentView = ref(sanitizeOrderView(route.query.view));
const currentTab = ref(sanitizeOrderStatus(route.query.status, currentView.value));
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const orders = ref([]);
const loading = ref(false);
const showReviewDialog = ref(false);
const currentReviewOrder = ref(null);
const reviewRating = ref(5);
const reviewContent = ref('');

const pageSubtitle = computed(() =>
  currentView.value === 'buyer'
    ? '跟进支付、收货与评价'
    : '处理发货与成交进度'
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

const formatTime = (time) => {
  if (!time) return '';
  return new Date(time).toLocaleDateString();
};

const formatPrice = (price) => {
  const numericPrice = Number(price || 0);
  return Number.isInteger(numericPrice)
    ? numericPrice.toString()
    : numericPrice.toFixed(2);
};

const syncQuery = async () => {
  const query = {};

  if (currentView.value === 'seller') {
    query.view = 'seller';
  }

  if (currentTab.value !== 'ALL') {
    query.status = currentTab.value;
  }

  await router.replace({ path: '/orders', query });
};

const loadOrders = async () => {
  loading.value = true;

  try {
    const params = {
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
    total.value = pageData.totalElements ?? pageData.total ?? orders.value.length;
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

const setView = async (view) => {
  const nextView = sanitizeOrderView(view);
  if (currentView.value === nextView) return;

  currentView.value = nextView;
  currentTab.value = 'ALL';
  currentPage.value = 1;
  await syncQuery();
  await loadOrders();
};

const setTab = async (status) => {
  const nextStatus = sanitizeOrderStatus(status, currentView.value);
  if (currentTab.value === nextStatus) return;

  currentTab.value = nextStatus;
  currentPage.value = 1;
  await syncQuery();
  await loadOrders();
};

const handlePay = async (order) => {
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

const handleCancel = async (order) => {
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

const handleShip = async (order) => {
  try {
    await ElMessageBox.confirm('确认已准备好发货？', '提示', { type: 'warning' });
    await api.order.shipOrder(order.id);
    ElMessage.success('发货成功');
    await refreshOrders();
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error.message || '发货失败');
    }
  }
};

const handleConfirmReceive = async (order) => {
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

const handleReview = (order) => {
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

const handleAction = async (actionKey, order) => {
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
    case 'review':
      handleReview(order);
      break;
    default:
      break;
  }
};

const getActions = (order) => getOrderActions(order, currentView.value);

const getOrderHint = (order) => buildOrderHint(order, currentView.value);

const viewDetail = (order) => {
  router.push(`/item/${order.itemId}`);
};

const handlePageChange = async (page) => {
  currentPage.value = page;
  await loadOrders();
};

onMounted(async () => {
  await syncQuery();
  await loadOrders();
});
</script>

<style scoped src="../styles/pages/orders.css"></style>
