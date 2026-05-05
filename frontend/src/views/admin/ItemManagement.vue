<template>
  <div class="item-management">
    <div class="page-intro">
      <h2 class="section-title">物品管理</h2>
      <p class="section-desc">审核和管理平台发布的闲置物品，处理违规内容</p>
    </div>

    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon stat-icon-total">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.5"
          >
            <path
              d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"
            />
          </svg>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.total }}</span>
          <span class="stat-label">物品总数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-pending">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.5"
          >
            <circle cx="12" cy="12" r="10" />
            <path d="M12 6v6l4 2" />
          </svg>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.pending }}</span>
          <span class="stat-label">待审核</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-onsale">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.5"
          >
            <path d="M12 2v20M17 5H9.5a3.5 3.5 0 000 7h5a3.5 3.5 0 010 7H6" />
          </svg>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.onSale }}</span>
          <span class="stat-label">在售</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-sold">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.5"
          >
            <path d="M9 12l2 2 4-4" />
            <circle cx="12" cy="12" r="10" />
          </svg>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.sold }}</span>
          <span class="stat-label">已售出</span>
        </div>
      </div>
    </div>

    <div class="content-card">
      <div class="card-header">
        <div class="header-left">
          <h3 class="card-title">物品列表</h3>
          <span class="data-range">共 {{ total }} 条记录</span>
        </div>
        <div class="header-actions">
          <button class="btn btn-ghost" @click="handleExport">
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
            导出
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
            placeholder="搜索物品标题、描述..."
            class="search-input"
            @keyup.enter="handleSearch"
          />
        </div>
        <div class="filter-selects">
          <select v-model="itemStatus" class="filter-select">
            <option value="">全部状态</option>
            <option value="DRAFT">草稿</option>
            <option value="PENDING">待审核</option>
            <option value="ON_SALE">在售</option>
            <option value="SOLD">已售</option>
            <option value="OFF_SHELF">已下架</option>
            <option value="REJECTED">已驳回</option>
          </select>
          <select v-model="categoryId" class="filter-select">
            <option value="">全部分类</option>
            <option v-for="cat in categories" :key="cat.id" :value="cat.id">
              {{ cat.name }}
            </option>
          </select>
          <select v-model="itemCondition" class="filter-select">
            <option value="">全部成色</option>
            <option value="NEW">全新</option>
            <option value="LIKE_NEW">几乎全新</option>
            <option value="GOOD">良好</option>
            <option value="FAIR">一般</option>
            <option value="POOR">较差</option>
          </select>
          <select v-model="bargainAllowed" class="filter-select">
            <option value="">议价状态</option>
            <option value="true">可议价</option>
            <option value="false">一口价</option>
          </select>
          <button class="btn btn-ghost btn-sm" @click="handleReset">
            重置
          </button>
        </div>
      </div>

      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th class="col-checkbox">
                <input
                  type="checkbox"
                  @change="handleSelectAll"
                  :checked="isAllSelected"
                />
              </th>
              <th class="col-image">物品</th>
              <th class="col-price">价格</th>
              <th class="col-condition">成色</th>
              <th class="col-delivery">配送</th>
              <th class="col-bargain">议价</th>
              <th class="col-category">分类</th>
              <th class="col-status">状态</th>
              <th class="col-stats">浏览/收藏</th>
              <th class="col-seller">发布者</th>
              <th class="col-date">发布时间</th>
              <th class="col-actions">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in items" :key="item.id" class="table-row">
              <td class="col-checkbox">
                <input
                  type="checkbox"
                  v-model="selectedItems"
                  :value="item.id"
                />
              </td>
              <td class="col-image">
                <div class="item-cell">
                  <div class="item-image">
                    <img
                      :src="getFirstImage(item)"
                      :alt="item.title"
                    />
                    <span v-if="item.isRecommended" class="recommend-badge"
                      >推荐</span
                    >
                  </div>
                  <div class="item-info">
                    <span class="item-title">{{ item.title }}</span>
                    <span class="item-desc">{{
                      truncateText(item.description, 30)
                    }}</span>
                    <div class="item-tags" v-if="item.tags">
                      <span
                        class="tag"
                        v-for="tag in item.tags.split(',').slice(0, 2)"
                        :key="tag"
                        >{{ tag }}</span
                      >
                    </div>
                  </div>
                </div>
              </td>
              <td class="col-price">
                <span class="price-value">¥{{ item.price }}</span>
                <span
                  class="price-original"
                  v-if="item.originalPrice && item.originalPrice > item.price"
                  >¥{{ item.originalPrice }}</span
                >
                <span
                  class="bargain-hint"
                  v-if="item.isBargainAllowed && item.minPrice"
                  >可至¥{{ item.minPrice }}</span
                >
              </td>
              <td class="col-condition">
                <span
                  class="badge"
                  :class="getConditionClass(item.itemCondition)"
                >
                  {{ getConditionText(item.itemCondition) }}
                </span>
              </td>
              <td class="col-delivery">
                <span class="delivery-text">{{
                  getDeliveryText(item.deliveryMethod)
                }}</span>
              </td>
              <td class="col-bargain">
                <span
                  class="badge"
                  :class="
                    item.isBargainAllowed ? 'badge-success' : 'badge-default'
                  "
                >
                  {{ item.isBargainAllowed ? '可议价' : '一口价' }}
                </span>
              </td>
              <td class="col-category">
                <span class="badge badge-category">{{
                  item.categoryName || '未分类'
                }}</span>
              </td>
              <td class="col-status">
                <span class="badge" :class="getStatusClass(item.status)">
                  {{ getStatusText(item.status) }}
                </span>
                <span
                  class="reject-reason"
                  v-if="item.status === 'REJECTED' && item.rejectReason"
                  :title="item.rejectReason"
                >
                  {{ truncateText(item.rejectReason, 15) }}
                </span>
              </td>
              <td class="col-stats">
                <div class="stats-mini">
                  <span class="stat-item" title="浏览">
                    <svg
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="1.5"
                    >
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                      <circle cx="12" cy="12" r="3" />
                    </svg>
                    {{ item.viewCount || 0 }}
                  </span>
                  <span class="stat-item" title="收藏">
                    <svg
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="1.5"
                    >
                      <path
                        d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"
                      />
                    </svg>
                    {{ item.favoriteCount || 0 }}
                  </span>
                </div>
              </td>
              <td class="col-seller">
                <div class="seller-cell">
                  <span class="seller-name">{{ item.sellerNickname || '-' }}</span>
                  <span class="seller-id" v-if="item.sellerVerified"
                    >已认证</span
                  >
                </div>
              </td>
              <td class="col-date">
                <div class="date-cell">
                  <span class="date-main">{{
                    formatDate(item.publishTime || item.createdAt)
                  }}</span>
                  <span class="date-sub" v-if="item.publishTime">发布</span>
                </div>
              </td>
              <td class="col-actions">
                <div class="action-group">
                  <button
                    class="action-btn"
                    @click="handleView(item)"
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
                  <button
                    v-if="item.status === 'PENDING'"
                    class="action-btn action-success"
                    @click="handleApprove(item)"
                    title="通过"
                  >
                    <svg
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="1.5"
                    >
                      <path d="M9 12l2 2 4-4" />
                      <circle cx="12" cy="12" r="10" />
                    </svg>
                  </button>
                  <button
                    v-if="item.status === 'PENDING'"
                    class="action-btn action-danger"
                    @click="handleReject(item)"
                    title="驳回"
                  >
                    <svg
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="1.5"
                    >
                      <circle cx="12" cy="12" r="10" />
                      <path d="M15 9l-6 6M9 9l6 6" />
                    </svg>
                  </button>
                  <button
                    v-if="item.status === 'ON_SALE'"
                    class="action-btn action-warning"
                    @click="handleTakeDown(item)"
                    title="下架"
                  >
                    <svg
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="1.5"
                    >
                      <path d="M19 12H5M12 19l-7-7 7-7" />
                    </svg>
                  </button>
                  <button
                    v-if="item.status === 'OFF_SHELF'"
                    class="action-btn action-success"
                    @click="handleReList(item)"
                    title="重新上架"
                  >
                    <svg
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="1.5"
                    >
                      <path d="M5 12h14M12 5l7 7-7 7" />
                    </svg>
                  </button>
                  <button
                    class="action-btn action-danger"
                    @click="handleDelete(item)"
                    title="删除"
                  >
                    <svg
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="1.5"
                    >
                      <path
                        d="M3 6h18M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"
                      />
                    </svg>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="table-footer" v-if="selectedItems.length > 0">
        <div class="selection-info">
          已选择 <strong>{{ selectedItems.length }}</strong> 项
        </div>
        <div class="bulk-actions">
          <button class="btn btn-sm btn-warning" @click="handleBulkTakeDown">
            批量下架
          </button>
          <button class="btn btn-sm btn-danger" @click="handleBulkDelete">
            批量删除
          </button>
        </div>
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
            <option :value="10">10 条/页</option>
            <option :value="20">20 条/页</option>
            <option :value="50">50 条/页</option>
          </select>
          <div class="pagination-buttons">
            <button
              class="page-btn"
              :disabled="page === 1"
              @click="
                page--;
                fetchItems();
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
                fetchItems();
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

    <el-dialog
      v-model="detailDialogVisible"
      title="物品详情"
      width="800px"
      class="item-detail-dialog"
    >
      <div class="item-detail" v-if="currentItem">
        <div class="detail-gallery">
          <div class="main-image">
            <img
              :src="getFirstImage(currentItem)"
              :alt="currentItem.title"
            />
          </div>
          <div
            class="image-list"
            v-if="parseImages(currentItem.images).length"
          >
            <img
              v-for="(img, idx) in parseImages(currentItem.images)"
              :key="idx"
              :src="img"
              :class="{ active: idx === 0 }"
              @click="currentItem.coverImage = img"
            />
          </div>
        </div>
        <div class="detail-info">
          <div class="detail-header">
            <h3 class="detail-title">{{ currentItem.title }}</h3>
            <span
              class="badge"
              :class="getStatusClass(currentItem.status)"
              >{{ getStatusText(currentItem.status) }}</span
            >
          </div>
          <div class="detail-price">
            <span class="current-price">¥{{ currentItem.price }}</span>
            <span class="original-price" v-if="currentItem.originalPrice"
              >原价 ¥{{ currentItem.originalPrice }}</span
            >
            <span class="bargain-tag" v-if="currentItem.isBargainAllowed"
              >可议价</span
            >
          </div>
          <div class="detail-meta">
            <div class="meta-row">
              <span class="meta-label">成色</span>
              <span class="meta-value">{{
                getConditionText(currentItem.itemCondition)
              }}</span>
            </div>
            <div class="meta-row">
              <span class="meta-label">分类</span>
              <span class="meta-value">{{
                currentItem.categoryName || '未分类'
              }}</span>
            </div>
            <div class="meta-row">
              <span class="meta-label">配送方式</span>
              <span class="meta-value">{{
                getDeliveryText(currentItem.deliveryMethod)
              }}</span>
            </div>
            <div class="meta-row" v-if="currentItem.location">
              <span class="meta-label">交易地点</span>
              <span class="meta-value">{{ currentItem.location }}</span>
            </div>
            <div class="meta-row" v-if="currentItem.brand">
              <span class="meta-label">品牌</span>
              <span class="meta-value">{{ currentItem.brand }}</span>
            </div>
            <div class="meta-row" v-if="currentItem.purchaseDate">
              <span class="meta-label">购买日期</span>
              <span class="meta-value">{{ currentItem.purchaseDate }}</span>
            </div>
            <div class="meta-row" v-if="currentItem.warrantyInfo">
              <span class="meta-label">保修信息</span>
              <span class="meta-value">{{ currentItem.warrantyInfo }}</span>
            </div>
          </div>
          <div class="detail-stats">
            <div class="stat-item">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.5"
              >
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                <circle cx="12" cy="12" r="3" />
              </svg>
              <span>{{ currentItem.viewCount || 0 }} 浏览</span>
            </div>
            <div class="stat-item">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.5"
              >
                <path
                  d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"
                />
              </svg>
              <span>{{ currentItem.favoriteCount || 0 }} 收藏</span>
            </div>
          </div>
          <div class="detail-desc">
            <h4>物品描述</h4>
            <p>{{ currentItem.description || '暂无描述' }}</p>
          </div>
          <div class="detail-tags" v-if="currentItem.tags">
            <span
              class="tag"
              v-for="tag in currentItem.tags.split(',')"
              :key="tag"
              >{{ tag }}</span
            >
          </div>
          <div class="detail-seller">
            <span class="seller-avatar">{{
              currentItem.sellerNickname?.charAt(0) || '用'
            }}</span>
            <div class="seller-info">
              <span class="seller-name">{{ currentItem.sellerNickname }}</span>
              <span class="seller-school" v-if="currentItem.sellerVerified">
                已认证
              </span>
              <span class="seller-school" v-else>
                未认证
              </span>
            </div>
          </div>
          <div class="detail-reject" v-if="currentItem.rejectReason">
            <h4>驳回原因</h4>
            <p>{{ currentItem.rejectReason }}</p>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button
            v-if="currentItem?.status === 'PENDING'"
            type="success"
            @click="
              handleApprove(currentItem);
              detailDialogVisible = false;
            "
            >通过</el-button
          >
          <el-button
            v-if="currentItem?.status === 'PENDING'"
            type="danger"
            @click="
              handleReject(currentItem);
              detailDialogVisible = false;
            "
            >驳回</el-button
          >
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import api from '../../api';

const searchKeyword = ref('');
const itemStatus = ref('');
const categoryId = ref('');
const itemCondition = ref('');
const bargainAllowed = ref('');
const items = ref([]);
const selectedItems = ref([]);
const categories = ref([]);
const page = ref(1);
const pageSize = ref(20);
const total = ref(0);
const detailDialogVisible = ref(false);
const currentItem = ref(null);

const stats = ref({ total: 0, pending: 0, onSale: 0, sold: 0 });
const totalPages = computed(() => Math.ceil(total.value / pageSize.value) || 1);
const isAllSelected = computed(
  () =>
    items.value.length > 0 && selectedItems.value.length === items.value.length
);

const getConditionClass = (condition) => {
  const map = {
    NEW: 'badge-success',
    LIKE_NEW: 'badge-info',
    GOOD: 'badge-primary',
    FAIR: 'badge-warning',
    POOR: 'badge-default',
  };
  return map[condition] || 'badge-default';
};

const getConditionText = (condition) => {
  const map = {
    NEW: '全新',
    LIKE_NEW: '几乎全新',
    GOOD: '良好',
    FAIR: '一般',
    POOR: '较差',
  };
  return map[condition] || condition;
};

const getDeliveryText = (method) => {
  const map = { 1: '自提', 2: '邮寄', 3: '送货上门' };
  return map[method] || '自提';
};

const getStatusClass = (status) => {
  const map = {
    DRAFT: 'badge-default',
    PENDING: 'badge-warning',
    ON_SALE: 'badge-success',
    SOLD: 'badge-info',
    OFF_SHELF: 'badge-default',
    REJECTED: 'badge-danger',
  };
  return map[status] || 'badge-default';
};

const getStatusText = (status) => {
  const map = {
    DRAFT: '草稿',
    PENDING: '待审核',
    ON_SALE: '在售',
    SOLD: '已售',
    OFF_SHELF: '已下架',
    REJECTED: '已驳回',
  };
  return map[status] || status;
};

const truncateText = (text, length) => {
  if (!text) return '';
  return text.length > length ? text.slice(0, length) + '...' : text;
};

const formatDate = (dateString) => {
  if (!dateString) return '-';
  return new Date(dateString).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
};

const fetchItems = async () => {
  try {
    const params = {};
    params.page = page.value;
    params.size = pageSize.value;
    if (searchKeyword.value) params.keyword = searchKeyword.value;
    if (itemStatus.value) params.status = itemStatus.value;
    if (categoryId.value) params.categoryId = categoryId.value;
    if (itemCondition.value) params.condition = itemCondition.value;
    if (bargainAllowed.value) params.bargainAllowed = bargainAllowed.value;

    const res = await api.admin.items.getItems(params);
    if (res.code === 200) {
      items.value = res.data.content || [];
      total.value = res.data.totalElements || 0;
    }
  } catch {
    ElMessage.error('网络错误');
  }
};

const fetchCategories = async () => {
  try {
    const res = await api.admin.categories.getCategories({ page: 1, size: 50 });
    if (res.code === 200) {
      categories.value = res.data.content || [];
    }
  } catch {
    categories.value = [];
  }
};

const fetchStats = async () => {
  try {
    const res = await api.admin.items.getItemStats();
    if (res.code === 200) {
      stats.value = res.data;
    }
  } catch {
    stats.value = { total: 0, pending: 0, onSale: 0, sold: 0 };
  }
};

const handleSearch = () => {
  page.value = 1;
  fetchItems();
};
const handleReset = () => {
  searchKeyword.value = '';
  itemStatus.value = '';
  categoryId.value = '';
  itemCondition.value = '';
  bargainAllowed.value = '';
  page.value = 1;
  fetchItems();
};
const handleSizeChange = () => {
  page.value = 1;
  fetchItems();
};
const handleSelectAll = (e) => {
  selectedItems.value = e.target.checked ? items.value.map((i) => i.id) : [];
};

const handleView = (item) => {
  currentItem.value = { ...item, images: item.images || [] };
  detailDialogVisible.value = true;
};

const parseImages = (images) => {
  if (!images) return [];
  if (Array.isArray(images)) return images;
  try {
    const parsed = JSON.parse(images);
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
};

const getFirstImage = (item) => {
  if (item.coverImage) return item.coverImage;
  const images = parseImages(item.images);
  return images.length > 0 ? images[0] : '/placeholder.png';
};

const handleApprove = (item) => {
  ElMessageBox.confirm(`通过物品 "${item.title}"？`, '确认')
    .then(async () => {
      try {
        const res = await api.admin.items.approve(item.id);
        if (res.code === 200) {
          item.status = 'ON_SALE';
          ElMessage.success('已通过');
        } else {
          ElMessage.error(res.message || '操作失败');
        }
      } catch {
        ElMessage.error('网络错误');
      }
    })
    .catch(() => {});
};

const handleReject = (item) => {
  ElMessageBox.prompt('请输入驳回原因', '驳回物品', {
    inputValidator: (v) => !!v || '原因不能为空',
  })
    .then(async ({ value }) => {
      try {
        const res = await api.admin.items.reject(item.id, value);
        if (res.code === 200) {
          item.status = 'REJECTED';
          item.rejectReason = value;
          ElMessage.success('已驳回');
        } else {
          ElMessage.error(res.message || '操作失败');
        }
      } catch {
        ElMessage.error('网络错误');
      }
    })
    .catch(() => {});
};

const handleTakeDown = (item) => {
  ElMessageBox.confirm(`下架物品 "${item.title}"？`, '确认')
    .then(async () => {
      try {
        const res = await api.admin.items.offShelf(item.id);
        if (res.code === 200) {
          item.status = 'OFF_SHELF';
          ElMessage.success('已下架');
        } else {
          ElMessage.error(res.message || '操作失败');
        }
      } catch {
        ElMessage.error('网络错误');
      }
    })
    .catch(() => {});
};

const handleReList = (item) => {
  ElMessageBox.confirm(`重新上架物品 "${item.title}"？`, '确认')
    .then(async () => {
      try {
        const res = await api.admin.items.approve(item.id);
        if (res.code === 200) {
          item.status = 'ON_SALE';
          ElMessage.success('已重新上架');
        } else {
          ElMessage.error(res.message || '操作失败');
        }
      } catch {
        ElMessage.error('网络错误');
      }
    })
    .catch(() => {});
};

const handleDelete = (item) => {
  ElMessageBox.confirm(
    `删除物品 "${item.title}"？此操作不可恢复。`,
    '危险操作',
    { type: 'error' }
  )
    .then(async () => {
      try {
        const res = await api.admin.items.deleteItem(item.id);
        if (res.code === 200) {
          items.value = items.value.filter((i) => i.id !== item.id);
          total.value--;
          ElMessage.success('已删除');
        } else {
          ElMessage.error(res.message || '操作失败');
        }
      } catch {
        ElMessage.error('网络错误');
      }
    })
    .catch(() => {});
};

const handleBulkTakeDown = () => {
  ElMessageBox.confirm(
    `下架选中的 ${selectedItems.value.length} 个物品？`,
    '确认'
  )
    .then(async () => {
      try {
        const res = await api.admin.items.batchOffShelf(selectedItems.value);
        if (res.code === 200) {
          ElMessage.success(`已下架 ${selectedItems.value.length} 个物品`);
          selectedItems.value = [];
          fetchItems();
        } else {
          ElMessage.error(res.message || '操作失败');
        }
      } catch {
        ElMessage.error('网络错误');
      }
    })
    .catch(() => {});
};

const handleBulkDelete = () => {
  ElMessageBox.confirm(
    `删除选中的 ${selectedItems.value.length} 个物品？此操作不可恢复。`,
    '危险操作',
    { type: 'error' }
  )
    .then(async () => {
      try {
        const results = await Promise.allSettled(
          selectedItems.value.map((id) => api.admin.items.deleteItem(id))
        );
        const succeeded = results.filter(r => r.status === 'fulfilled').length;
        ElMessage.success(`已删除 ${succeeded} 个物品`);
        selectedItems.value = [];
        fetchItems();
      } catch {
        ElMessage.error('网络错误');
      }
    })
    .catch(() => {});
};

const handleExport = () => ElMessage.info('导出功能开发中');

onMounted(() => {
  fetchItems();
  fetchCategories();
  fetchStats();
});
</script>

<style scoped src="../../styles/pages/admin-item-management.css"></style>
