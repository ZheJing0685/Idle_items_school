<template>
  <div class="items-page">
    <PageHeader title="我的发布" subtitle="您发布的物品会显示在这里，方便您管理">
      <template #action>
        <el-button type="primary" @click="$router.push('/publish')">
          <el-icon><Plus /></el-icon>
          发布新物品
        </el-button>
      </template>
    </PageHeader>

    <FilterTabs v-model="statusFilter" :tabs="filterTabs" @change="handleStatusChange" />

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-grid">
      <div v-for="i in 6" :key="i" class="skeleton-card">
        <div class="skeleton-image"></div>
        <div class="skeleton-content">
          <div class="skeleton-title"></div>
          <div class="skeleton-price"></div>
          <div class="skeleton-meta"></div>
        </div>
      </div>
    </div>

    <!-- 错误提示 -->
    <el-alert v-else-if="error" :title="error" type="error" show-icon class="error-alert" />

    <!-- 空状态 -->
    <EmptyState v-else-if="items.length === 0" title="您还没有发布任何物品" description="发布闲置物品，让它们找到新主人">
      <template #action>
        <el-button type="primary" @click="$router.push('/publish')">去发布物品</el-button>
      </template>
    </EmptyState>

    <!-- 物品列表 -->
    <div v-else class="items-grid">
      <ItemCard
        v-for="item in items"
        :key="item.id"
        :id="item.id"
        :title="item.title"
        :price="item.price"
        :coverImage="item.coverImage"
        :status="item.status"
        :statusText="getStatusText(item.status)"
        :viewCount="item.viewCount"
        :time="formatDate(item.createdAt)"
        @click="goToItemDetail(item.id)"
      >
        <template #actions>
          <el-button type="primary" size="small" @click.stop="goToItemDetail(item.id)">查看详情</el-button>
          <el-button type="info" size="small" @click.stop="editItem(item.id)" :disabled="!canEdit(item)">编辑</el-button>
          <el-button v-if="canToggleShelf(item)" type="warning" size="small" @click.stop="toggleShelf(item)">
            {{ item.status === 'ON_SALE' ? '下架' : '上架' }}
          </el-button>
          <el-button v-if="canDelete(item)" type="danger" size="small" @click.stop="deleteItem(item.id)">删除</el-button>
        </template>
      </ItemCard>
    </div>

    <!-- 分页 -->
    <div v-if="!loading && !error && items.length > 0" class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[12, 24, 36]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import api from '../../api';
import { useDictStore } from '../../store/dict.js';
import PageHeader from '../../components/user/PageHeader.vue';
import FilterTabs from '../../components/user/FilterTabs.vue';
import ItemCard from '../../components/user/ItemCard.vue';
import EmptyState from '../../components/user/EmptyState.vue';

const router = useRouter();
const dictStore = useDictStore();

const items = ref([]);
const loading = ref(false);
const error = ref('');
const currentPage = ref(1);
const pageSize = ref(12);
const total = ref(0);
const statusFilter = ref('');

const filterTabs = [
  { value: '', label: '全部' },
  { value: 'ON_SALE', label: '在售' },
  { value: 'SOLD', label: '已售出' },
  { value: 'PENDING', label: '审核中' },
  { value: 'OFF_SHELF', label: '已下架' }
];

const loadItems = async () => {
  loading.value = true;
  error.value = '';
  try {
    const response = await api.user.getItems(
      statusFilter.value || undefined,
      currentPage.value,
      pageSize.value
    );
    if (response.code === 200) {
      items.value = response.data.content || [];
      total.value = response.data.totalElements || 0;
    } else {
      error.value = response.message || '加载物品失败';
    }
  } catch (err) {
    error.value = '网络错误，请稍后重试';
  } finally {
    loading.value = false;
  }
};

const editItem = (id) => {
  window.location.href = `/publish?edit=${id}`;
};

const deleteItem = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个物品吗？删除后无法恢复。', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    });
    items.value = items.value.filter((item) => item.id !== id);
    total.value--;
    ElMessage.success('删除成功');
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

const toggleShelf = async (item) => {
  try {
    const newStatus = item.status === 'ON_SALE' ? 'OFF_SHELF' : 'ON_SALE';
    const action = newStatus === 'ON_SALE' ? '上架' : '下架';
    await ElMessageBox.confirm(`确定要${action}这个物品吗？`, `${action}确认`, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info',
    });
    let response;
    if (newStatus === 'OFF_SHELF') {
      response = await api.item.offShelf(item.id);
    } else {
      response = await api.item.onShelf(item.id);
    }
    if (response.code === 200) {
      item.status = newStatus;
      ElMessage.success(`${action}成功`);
    } else {
      ElMessage.error(response.message || `${action}失败`);
    }
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('操作失败');
    }
  }
};

const goToItemDetail = (id) => {
  window.location.href = `/item/${id}`;
};

const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
};

const getStatusText = (status) => {
  return dictStore.getDictLabel('ITEM_STATUS', status);
};

const canEdit = (item) => {
  return !(item.status === 'SOLD');
};

const canToggleShelf = (item) => {
  return item.status !== 'PENDING';
};

const canDelete = (item) => {
  return !(item.status === 'SOLD');
};

const handleStatusChange = () => {
  currentPage.value = 1;
  loadItems();
};

const handleSizeChange = (size) => {
  pageSize.value = size;
  currentPage.value = 1;
  loadItems();
};

const handleCurrentChange = (page) => {
  currentPage.value = page;
  loadItems();
};

onMounted(async () => {
  await dictStore.preloadCommonDicts();
  loadItems();
});
</script>

<style scoped src="../../styles/pages/user-items.css"></style>
