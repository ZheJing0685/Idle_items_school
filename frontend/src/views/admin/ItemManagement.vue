<template>
  <div class="item-management">
    <div class="page-intro">
      <h2 class="section-title">物品管理</h2>
      <p class="section-desc">审核和管理平台发布的闲置物品，处理违规内容</p>
    </div>

    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon stat-icon-total">
          <Package :size="24" />
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.total }}</span>
          <span class="stat-label">物品总数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-pending">
          <Clock :size="24" />
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.pending }}</span>
          <span class="stat-label">待审核</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-onsale">
          <DollarSign :size="24" />
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.onSale }}</span>
          <span class="stat-label">在售</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-sold">
          <CheckCircle :size="24" />
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
            placeholder="搜索物品标题、描述..."
            class="search-input"
            @keyup.enter="handleSearch"
          />
        </div>
        <div class="filter-selects">
          <el-select v-model="itemStatus" placeholder="全部状态" clearable>
            <el-option value="DRAFT" label="草稿" />
            <el-option value="PENDING" label="待审核" />
            <el-option value="ON_SALE" label="在售" />
            <el-option value="SOLD" label="已售" />
            <el-option value="OFF_SHELF" label="已下架" />
            <el-option value="REJECTED" label="已驳回" />
          </el-select>
          <el-select v-model="categoryId" placeholder="全部分类" clearable>
            <el-option v-for="cat in categories" :key="cat.id" :value="cat.id" :label="cat.name" />
          </el-select>
          <el-select v-model="itemCondition" placeholder="全部成色" clearable>
            <el-option value="NEW" label="全新" />
            <el-option value="LIKE_NEW" label="几乎全新" />
            <el-option value="GOOD" label="良好" />
            <el-option value="FAIR" label="一般" />
            <el-option value="POOR" label="较差" />
          </el-select>
          <el-select v-model="bargainAllowed" placeholder="议价状态" clearable>
            <el-option value="true" label="可议价" />
            <el-option value="false" label="一口价" />
          </el-select>
        </div>
        <div class="filter-actions">
          <button class="btn btn-ghost btn-sm" @click="handleReset">重置</button>
        </div>
      </div>

      <el-table
        :data="items"
        row-key="id"
        @selection-change="handleSelectionChange"
        stripe
        empty-text="暂无物品数据"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column label="物品" min-width="200">
          <template #default="{ row }">
            <div class="item-cell">
              <div class="item-image">
                <img :src="getFirstImage(row)" :alt="row.title" />
                <span v-if="row.isRecommended" class="recommend-badge">推荐</span>
              </div>
              <div class="item-info">
                <span class="item-title">{{ row.title }}</span>
                <span class="item-desc">{{ truncateText(row.description, 30) }}</span>
                <div class="item-tags" v-if="row.tags">
                  <span class="tag" v-for="tag in row.tags.split(',').slice(0, 2)" :key="tag">{{ tag }}</span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="120">
          <template #default="{ row }">
            <span class="price-value">¥{{ row.price }}</span>
            <span class="price-original" v-if="row.originalPrice && row.originalPrice > row.price">¥{{ row.originalPrice }}</span>
            <span class="bargain-hint" v-if="row.isBargainAllowed && row.minPrice">可至¥{{ row.minPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column label="成色" width="90">
          <template #default="{ row }">
            <span class="badge" :class="getConditionClass(row.itemCondition)">
              {{ getConditionText(row.itemCondition) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="配送" width="80">
          <template #default="{ row }">
            <span class="delivery-text">{{ getDeliveryText(row.deliveryMethod) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="议价" width="75">
          <template #default="{ row }">
            <span class="badge" :class="row.isBargainAllowed ? 'badge-success' : 'badge-default'">
              {{ row.isBargainAllowed ? '可议价' : '一口价' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="100">
          <template #default="{ row }">
            <span class="badge badge-category">{{ row.categoryName || '未分类' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <span class="badge" :class="getStatusClass(row.status)">
              {{ getStatusText(row.status) }}
            </span>
            <span class="reject-reason" v-if="row.status === 'REJECTED' && row.rejectReason" :title="row.rejectReason">
              {{ truncateText(row.rejectReason, 15) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="浏览/收藏" width="110">
          <template #default="{ row }">
            <div class="stats-mini">
              <span class="stat-item" title="浏览">
                <Eye :size="14" />
                {{ row.viewCount || 0 }}
              </span>
              <span class="stat-item" title="收藏">
                <Heart :size="14" />
                {{ row.favoriteCount || 0 }}
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="发布者" width="120">
          <template #default="{ row }">
            <div class="seller-cell">
              <span class="seller-name">{{ row.sellerNickname || '-' }}</span>
              <span class="seller-id" v-if="row.sellerVerified">已认证</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="105">
          <template #default="{ row }">
            <div class="date-cell">
              <span class="date-main">{{ formatDate(row.publishTime || row.createdAt) }}</span>
              <span class="date-sub" v-if="row.publishTime">发布</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="210" fixed="right">
          <template #default="{ row }">
            <div class="action-group">
              <button class="action-btn" @click="handleView(row)" title="查看详情" aria-label="查看详情">
                <Eye :size="16" />
              </button>
              <button v-if="row.status === 'PENDING'" class="action-btn action-success" @click="handleApprove(row)" title="通过" aria-label="通过">
                <CheckCircle :size="16" />
              </button>
              <button v-if="row.status === 'PENDING'" class="action-btn action-danger" @click="handleReject(row)" title="驳回" aria-label="驳回">
                <XCircle :size="16" />
              </button>
              <button v-if="row.status === 'ON_SALE'" class="action-btn action-warning" @click="handleTakeDown(row)" title="下架" aria-label="下架">
                <ArrowLeft :size="16" />
              </button>
              <button v-if="row.status === 'OFF_SHELF'" class="action-btn action-success" @click="handleReList(row)" title="重新上架" aria-label="重新上架">
                <ArrowUp :size="16" />
              </button>
              <button class="action-btn action-danger" @click="handleDelete(row)" title="删除" aria-label="删除">
                <Trash2 :size="16" />
              </button>
            </div>
          </template>
        </el-table-column>
      </el-table>

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
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="fetchItems"
          @size-change="handleSizeChange"
        />
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
            <img :src="getFirstImage(currentItem)" :alt="currentItem.title" />
          </div>
          <div class="image-list" v-if="parseImages(currentItem.images).length">
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
            <span class="badge" :class="getStatusClass(currentItem.status)">{{
              getStatusText(currentItem.status)
            }}</span>
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
              <Eye :size="16" />
              <span>{{ currentItem.viewCount || 0 }} 浏览</span>
            </div>
            <div class="stat-item">
              <Heart :size="16" />
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
              <span class="seller-school" v-else> 未认证 </span>
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

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import api from '../../api';
import { useDictStore } from '../../store/dict.js';
import { Package, Clock, DollarSign, CheckCircle, Download, Search, Eye, Heart, XCircle, ArrowLeft, ArrowUp, Trash2 } from 'lucide-vue-next';

const dictStore = useDictStore();

const searchKeyword = ref('');
const itemStatus = ref('');
const categoryId = ref('');
const itemCondition = ref('');
const bargainAllowed = ref('');
const items = ref<any[]>([]);
const selectedItems = ref<any[]>([]);
const categories = ref<any[]>([]);
const page = ref(1);
const pageSize = ref(20);
const total = ref(0);
const detailDialogVisible = ref(false);
const currentItem = ref<any>(null);

const stats = ref({ total: 0, pending: 0, onSale: 0, sold: 0 });
const totalPages = computed(() => Math.ceil(total.value / pageSize.value) || 1);

const getConditionClass = (condition: string) => {
  const map: Record<string, string> = {
    NEW: 'badge-success',
    LIKE_NEW: 'badge-info',
    GOOD: 'badge-primary',
    FAIR: 'badge-warning',
    POOR: 'badge-default',
  };
  return map[condition] || 'badge-default';
};

const getConditionText = (condition: string) => {
  return dictStore.getDictLabel('ITEM_CONDITION', condition);
};

const getDeliveryText = (method: string) => {
  return dictStore.getDictLabel('DELIVERY_METHOD', method);
};

const getStatusClass = (status: string) => {
  const map: Record<string, string> = {
    DRAFT: 'badge-default',
    PENDING: 'badge-warning',
    ON_SALE: 'badge-success',
    SOLD: 'badge-info',
    OFF_SHELF: 'badge-default',
    REJECTED: 'badge-danger',
  };
  return map[status] || 'badge-default';
};

const getStatusText = (status: string) => {
  return dictStore.getDictLabel('ITEM_STATUS', status);
};

const truncateText = (text: string, length: number) => {
  if (!text) return '';
  return text.length > length ? text.slice(0, length) + '...' : text;
};

const formatDate = (dateString: string) => {
  if (!dateString) return '-';
  return new Date(dateString).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
};

const fetchItems = async () => {
  try {
    const params: Record<string, any> = {};
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
const handleSelectionChange = (selection: any[]) => {
  selectedItems.value = selection.map((item: any) => item.id);
};

const handleView = (item: any) => {
  currentItem.value = { ...item, images: item.images || [] };
  detailDialogVisible.value = true;
};

const parseImages = (images: any) => {
  if (!images) return [];
  if (Array.isArray(images)) return images;
  try {
    const parsed = JSON.parse(images);
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
};

const getFirstImage = (item: any) => {
  if (item.coverImage) return item.coverImage;
  const images = parseImages(item.images);
  return images.length > 0 ? images[0] : '/placeholder.png';
};

const handleApprove = (item: any) => {
  ElMessageBox.confirm(`通过物品 "${item.title}"？`, '确认')
    .then(async () => {
      try {
        const res = await api.admin.items.approveItem(item.id);
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

const handleReject = (item: any) => {
  ElMessageBox.prompt('请输入驳回原因', '驳回物品', {
    inputValidator: (v) => !!v || '原因不能为空',
  })
    .then(async ({ value }) => {
      try {
        const res = await api.admin.items.rejectItem(item.id, value);
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

const handleTakeDown = (item: any) => {
  ElMessageBox.confirm(`下架物品 "${item.title}"？`, '确认')
    .then(async () => {
      try {
        const res = await api.admin.items.offShelfItem(item.id, '');
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

const handleReList = (item: any) => {
  ElMessageBox.confirm(`重新上架物品 "${item.title}"？`, '确认')
    .then(async () => {
      try {
        const res = await api.admin.items.approveItem(item.id);
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

const handleDelete = (item: any) => {
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
      } catch (err) {
        // 处理后端返回的业务错误
        if (err && err.message) {
          ElMessage.error(err.message);
        } else {
          ElMessage.error('操作失败');
        }
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
        const res = await api.admin.items.batchOffShelf(selectedItems.value, '');
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
        const succeeded = results.filter(
          (r) => r.status === 'fulfilled'
        ).length;
        ElMessage.success(`已删除 ${succeeded} 个物品`);
        selectedItems.value = [];
        fetchItems();
      } catch {
        ElMessage.error('网络错误');
      }
    })
    .catch(() => {});
};

const handleExport = async () => {
  try {
    const params: Record<string, any> = {};
    if (searchKeyword.value) params.keyword = searchKeyword.value;
    if (itemStatus.value) params.status = itemStatus.value;
    if (categoryId.value) params.categoryId = categoryId.value;

    const blob = await api.admin.items.exportItems(params);
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `items_${new Date().getTime()}.csv`);
    document.body.appendChild(link);
    link.click();
    link.remove();
    ElMessage.success('导出成功');
  } catch (error) {
    ElMessage.error('导出失败');
  }
};

onMounted(async () => {
  // 加载字典数据
  await dictStore.preloadCommonDicts();
  fetchItems();
  fetchCategories();
  fetchStats();
});
</script>

<style src="../../styles/components/admin-filters.css"></style>
<style scoped src="../../styles/pages/admin-item-management.css"></style>
