<template>
  <div class="category-management">
    <div class="page-intro">
      <h2 class="section-title">分类管理</h2>
      <p class="section-desc">管理平台物品分类，支持多级分类结构</p>
    </div>

    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon stat-icon-total">
          <List :size="24" />
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.total }}</span>
          <span class="stat-label">分类总数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-active">
          <Clock :size="24" />
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.active }}</span>
          <span class="stat-label">活跃分类</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-level1">
          <Layers :size="24" />
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.level1 }}</span>
          <span class="stat-label">一级分类</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-level2">
          <Grid :size="24" />
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.level2 }}</span>
          <span class="stat-label">二级分类</span>
        </div>
      </div>
    </div>

    <div class="main-content">
      <div class="tree-panel">
        <div class="tree-header">
          <h3 class="tree-title">分类结构</h3>
          <div class="tree-actions">
            <button
              class="btn btn-icon"
              @click="handleExpandAll"
              :title="allExpanded ? '全部收起' : '全部展开'"
              :aria-label="allExpanded ? '全部收起' : '全部展开'"
            >
              <ChevronUp v-if="allExpanded" :size="16" />
              <ChevronDown v-else :size="16" />
            </button>
            <button
              class="btn btn-icon"
              @click="handleRefreshTree"
              title="刷新"
              aria-label="刷新"
            >
              <RefreshCw :size="16" />
            </button>
          </div>
        </div>

        <div class="tree-search">
          <Search :size="16" class="search-icon" />
          <input
            v-model="treeFilterText"
            type="text"
            placeholder="搜索分类..."
            class="search-input"
          />
        </div>

        <div class="tree-toolbar">
          <button class="btn btn-primary btn-sm" @click="handleAddRoot">
            <Plus :size="16" />
            添加一级分类
          </button>
          <div class="tree-batch" v-if="selectedTreeKeys.length > 0">
            <span class="batch-count">{{ selectedTreeKeys.length }} 项</span>
            <button class="btn btn-success btn-xs" @click="handleBatchEnable">
              启用
            </button>
            <button class="btn btn-warning btn-xs" @click="handleBatchDisable">
              禁用
            </button>
            <button class="btn btn-danger btn-xs" @click="handleBatchDelete">
              删除
            </button>
          </div>
        </div>

        <div class="tree-body">
          <el-tree
            ref="treeRef"
            :data="filteredTreeData"
            :props="treeProps"
            node-key="id"
            :default-expand-all="allExpanded"
            :expand-on-click-node="false"
            :filter-node-method="filterTreeNode"
            :highlight-current="true"
            show-checkbox
            check-strictly
            @node-click="handleNodeClick"
            @check-change="handleCheckChange"
          >
            <template #default="{ data }">
              <div class="tree-node" :class="{ 'is-disabled': !data.status }">
                <div class="tree-node-content">
                  <span class="tree-node-icon" v-if="data.icon">
                    <img :src="data.icon" :alt="data.name" />
                  </span>
                  <span class="tree-node-name">{{ data.name }}</span>
                  <span class="tree-node-count">{{ data.itemCount || 0 }}</span>
                  <span
                    class="tree-node-status"
                    :class="data.status ? 'status-active' : 'status-inactive'"
                  >
                    {{ data.status ? '启用' : '禁用' }}
                  </span>
                </div>
                <div class="tree-node-actions" @click.stop>
                  <button
                    class="tree-action-btn"
                    @click="handleEditNode(data)"
                    title="编辑"
                    aria-label="编辑"
                  >
                    <Edit3 :size="14" />
                  </button>
                  <button
                    v-if="data.level === 1"
                    class="tree-action-btn tree-action-success"
                    @click="handleAddChild(data)"
                    title="添加子分类"
                    aria-label="添加子分类"
                  >
                    <Plus :size="14" />
                  </button>
                  <button
                    class="tree-action-btn tree-action-danger"
                    @click="handleDeleteNode(data)"
                    title="删除"
                    aria-label="删除"
                  >
                    <Trash2 :size="14" />
                  </button>
                </div>
              </div>
            </template>
          </el-tree>

          <div class="tree-empty" v-if="filteredTreeData.length === 0">
            <Package :size="48" />
            <span>暂无分类数据</span>
          </div>
        </div>
      </div>

      <div class="detail-panel">
        <template v-if="panelMode === 'empty'">
          <div class="detail-empty">
            <Table :size="48" />
            <h4>请选择分类查看详情</h4>
            <p>点击左侧分类树中的节点，即可在此处查看分类详情</p>
          </div>
        </template>

        <template v-if="panelMode === 'detail' && currentCategory">
          <div class="detail-header">
            <div class="detail-header-left">
              <div class="detail-icon" v-if="currentCategory.icon">
                <img :src="currentCategory.icon" :alt="currentCategory.name" />
              </div>
              <div class="detail-icon detail-icon-placeholder" v-else>
                <Package :size="32" />
              </div>
              <div class="detail-title-group">
                <h3 class="detail-name">{{ currentCategory.name }}</h3>
                <div class="detail-badges">
                  <span
                    class="badge"
                    :class="
                      currentCategory.level === 1
                        ? 'badge-primary'
                        : 'badge-info'
                    "
                  >
                    {{ currentCategory.level }}级分类
                  </span>
                  <span
                    class="badge"
                    :class="
                      currentCategory.status ? 'badge-success' : 'badge-danger'
                    "
                  >
                    {{ currentCategory.status ? '启用' : '禁用' }}
                  </span>
                </div>
              </div>
            </div>
            <div class="detail-header-right">
              <button class="btn btn-primary btn-sm" @click="handleEditCurrent">
                <Edit3 :size="14" />
                编辑
              </button>
            </div>
          </div>

          <div class="detail-body">
            <div class="detail-section">
              <h4 class="detail-section-title">基本信息</h4>
              <div class="info-grid">
                <div class="info-item">
                  <span class="info-label">分类名称</span>
                  <span class="info-value">{{ currentCategory.name }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">分类级别</span>
                  <span class="info-value">{{ currentCategory.level }}级</span>
                </div>
                <div class="info-item">
                  <span class="info-label">父分类</span>
                  <span class="info-value">{{
                    currentCategory.parentName || '无（一级分类）'
                  }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">排序权重</span>
                  <span class="info-value">{{ currentCategory.sort }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">物品数量</span>
                  <span class="info-value">{{
                    currentCategory.itemCount || 0
                  }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">状态</span>
                  <span
                    class="info-value"
                    :class="
                      currentCategory.status
                        ? 'status-active'
                        : 'status-inactive'
                    "
                  >
                    {{ currentCategory.status ? '启用' : '禁用' }}
                  </span>
                </div>
              </div>
            </div>

            <div class="detail-section" v-if="currentCategory.description">
              <h4 class="detail-section-title">分类描述</h4>
              <div class="desc-content">{{ currentCategory.description }}</div>
            </div>

            <div class="detail-section">
              <h4 class="detail-section-title">时间信息</h4>
              <div class="info-grid">
                <div class="info-item">
                  <span class="info-label">创建时间</span>
                  <span class="info-value">{{
                    formatDateTime(currentCategory.createdAt)
                  }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">更新时间</span>
                  <span class="info-value">{{
                    formatDateTime(currentCategory.updatedAt)
                  }}</span>
                </div>
              </div>
            </div>

            <div class="detail-section" v-if="currentCategory.path">
              <h4 class="detail-section-title">分类路径</h4>
              <div class="path-content">{{ currentCategory.path }}</div>
            </div>
          </div>
        </template>

        <template
          v-if="(panelMode === 'edit' || panelMode === 'create') && editForm"
        >
          <div class="edit-header">
            <h3 class="edit-title">
              {{ panelMode === 'edit' ? '编辑分类' : '新增分类' }}
            </h3>
            <button class="btn btn-ghost btn-sm" @click="handleCancelEdit">
              <X :size="16" />
              取消
            </button>
          </div>

          <div class="edit-body">
            <el-form
              :model="editForm"
              label-width="100px"
              class="category-form"
            >
              <el-form-item label="分类名称" required>
                <el-input
                  v-model="editForm.name"
                  maxlength="50"
                  placeholder="请输入分类名称"
                />
              </el-form-item>

              <el-form-item label="父分类">
                <el-select
                  v-model="editForm.parentId"
                  placeholder="选择父分类"
                  style="width: 100%"
                >
                  <el-option value="0" label="一级分类" />
                  <el-option
                    v-for="cat in level1Categories"
                    :key="cat.id"
                    :label="cat.name"
                    :value="cat.id"
                  />
                </el-select>
              </el-form-item>

              <el-form-item label="排序">
                <el-input-number
                  v-model="editForm.sort"
                  :min="1"
                  :max="999"
                  :step="1"
                />
              </el-form-item>

              <el-form-item label="状态">
                <el-switch
                  v-model="editForm.status"
                  :active-value="1"
                  :inactive-value="0"
                />
              </el-form-item>

              <el-form-item label="分类图标">
                <el-upload
                  class="icon-upload"
                  action="/api/upload"
                  :show-file-list="false"
                  :on-success="handleIconSuccess"
                  :before-upload="beforeIconUpload"
                >
                  <div class="upload-area" v-if="!editForm.icon">
                    <Upload :size="24" />
                    <span>点击上传图标</span>
                  </div>
                  <div class="icon-preview" v-else>
                    <img :src="editForm.icon" :alt="editForm.name" />
                    <button
                      type="button"
                      class="remove-btn"
                      @click.stop="editForm.icon = ''"
                    >
                      <XCircle :size="16" />
                    </button>
                  </div>
                </el-upload>
              </el-form-item>

              <el-form-item label="分类描述">
                <el-input
                  v-model="editForm.description"
                  type="textarea"
                  :rows="3"
                  maxlength="200"
                  show-word-limit
                  placeholder="请输入分类描述"
                />
              </el-form-item>

              <el-form-item>
                <div class="edit-actions">
                  <button class="btn btn-ghost" @click="handleCancelEdit">
                    取消
                  </button>
                  <button
                    class="btn btn-primary"
                    @click="handleSave"
                    :disabled="saving"
                  >
                    {{ saving ? '保存中...' : '保存' }}
                  </button>
                </div>
              </el-form-item>
            </el-form>
          </div>
        </template>
      </div>
    </div>

    <div class="content-card tabs-section">
      <el-tabs v-model="activeTab" class="management-tabs">
        <el-tab-pane label="分类管理" name="management">
          <div class="tab-toolbar">
            <div class="tab-filters">
              <select
                v-model="tableStatus"
                class="filter-select"
                @change="fetchTableCategories"
              >
                <option value="">全部状态</option>
                <option value="1">启用</option>
                <option value="0">禁用</option>
              </select>
            </div>
            <div class="tab-actions">
              <button class="btn btn-ghost btn-sm" @click="handleExport">
                <Download :size="16" />
                导出
              </button>
              <button class="btn btn-ghost btn-sm" @click="triggerImport">
                <Upload :size="16" />
                导入
              </button>
              <input
                ref="importInputRef"
                type="file"
                accept=".csv"
                style="display: none"
                @change="handleImportFile"
              />
            </div>
          </div>

          <div class="table-wrapper">
            <el-table
              :data="tableCategories"
              style="width: 100%"
              @selection-change="handleSelectionChange"
              row-key="id"
            >
              <el-table-column type="selection" width="50" />
              <el-table-column label="分类名称" min-width="160">
                <template #default="{ row }">
                  <div class="category-cell">
                    <span
                      class="category-name"
                      :style="{ paddingLeft: (row.level - 1) * 20 + 'px' }"
                    >
                      <span v-if="row.level > 1" class="level-indicator"></span>
                      {{ row.name }}
                    </span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="级别" width="80">
                <template #default="{ row }">
                  <span class="badge" :class="row.level === 1 ? 'badge-primary' : 'badge-info'">
                    {{ row.level }}级
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="父分类" width="120">
                <template #default="{ row }">
                  <span class="parent-name">{{ row.parentName || '无' }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="sort" label="排序" width="80" />
              <el-table-column label="物品数" width="80">
                <template #default="{ row }">
                  <span class="item-count">{{ row.itemCount || 0 }}</span>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="80">
                <template #default="{ row }">
                  <span class="status-tag" :class="row.status ? 'status-active' : 'status-inactive'">
                    {{ row.status ? '启用' : '禁用' }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="创建时间" min-width="120">
                <template #default="{ row }">
                  <span class="create-time">{{ formatDate(row.createdAt) }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div class="table-footer" v-if="selectedCategories.length > 0">
            <div class="selection-info">
              已选择 <strong>{{ selectedCategories.length }}</strong> 项
            </div>
            <div class="bulk-actions">
              <button class="btn btn-sm btn-success" @click="handleBulkEnable">
                批量启用
              </button>
              <button class="btn btn-sm btn-warning" @click="handleBulkDisable">
                批量禁用
              </button>
              <button class="btn btn-sm btn-danger" @click="handleBulkDelete">
                批量删除
              </button>
            </div>
          </div>

          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="tablePage"
              v-model:page-size="tablePageSize"
              :total="tableTotal"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleTableSizeChange"
              @current-change="fetchTableCategories"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="变更日志" name="changelog">
          <div class="table-wrapper">
            <table class="data-table">
              <thead>
                <tr>
                  <th>操作类型</th>
                  <th>分类名称</th>
                  <th>操作人</th>
                  <th>操作时间</th>
                  <th>详情</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="log in changeLogs" :key="log.id" class="table-row">
                  <td>
                    <span
                      class="badge"
                      :class="getLogTypeClass(log.operationType)"
                    >
                      {{ log.operationType }}
                    </span>
                  </td>
                  <td>{{ log.categoryName }}</td>
                  <td>{{ log.operatorName }}</td>
                  <td>{{ formatDateTime(log.createdAt) }}</td>
                  <td>
                    <span class="log-detail">{{ log.detail || '-' }}</span>
                  </td>
                </tr>
                <tr v-if="changeLogs.length === 0">
                  <td colspan="5" class="empty-cell">暂无变更日志</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="pagination-wrapper">
            <div class="pagination-info">共 {{ logTotal }} 条记录</div>
            <div class="pagination-controls">
              <div class="pagination-buttons">
                <button
                  class="page-btn"
                  :disabled="logPage === 1"
                  @click="
                    logPage--;
                    fetchChangeLogs();
                  "
                >
                <ChevronLeft :size="16" />
              </button>
              <span class="page-indicator"
                >{{ logPage }} / {{ logTotalPages }}</span
              >
              <button
                class="page-btn"
                :disabled="logPage >= logTotalPages"
                @click="
                  logPage++;
                  fetchChangeLogs();
                "
              >
                <ChevronRight :size="16" />
                </button>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, nextTick } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import api from '../../api';
import { List, Clock, Layers, Grid, ChevronUp, ChevronDown, RefreshCw, Search, Plus, Edit3, Trash2, X, Upload, XCircle, Package, Table, Download, ChevronLeft, ChevronRight } from 'lucide-vue-next';

const stats = ref({ total: 0, active: 0, level1: 0, level2: 0 });
const allCategories = ref<any[]>([]);
const level1Categories = ref<any[]>([]);
const categoryTree = ref<any[]>([]);
const treeRef = ref<any>(null);
const treeFilterText = ref('');
const allExpanded = ref(true);
const selectedTreeKeys = ref([]);

const currentCategory = ref<any>(null);
const panelMode = ref('empty');
const editForm = ref<any>(null);
const saving = ref(false);

const activeTab = ref('management');
const tableCategories = ref<any[]>([]);
const tableStatus = ref('');
const tablePage = ref(1);
const tablePageSize = ref(20);
const tableTotal = ref(0);
const selectedCategories = ref<any[]>([]);
const importInputRef = ref<HTMLInputElement | null>(null);

const changeLogs = ref<any[]>([]);
const logPage = ref(1);
const logPageSize = ref(20);
const logTotal = ref(0);

const treeProps = { children: 'children', label: 'name' };

const tableTotalPages = computed(
  () => Math.ceil(tableTotal.value / tablePageSize.value) || 1
);
const logTotalPages = computed(
  () => Math.ceil(logTotal.value / logPageSize.value) || 1
);
const isAllSelected = computed(
  () =>
    tableCategories.value.length > 0 &&
    selectedCategories.value.length === tableCategories.value.length
);

const filteredTreeData = computed(() => {
  if (!treeFilterText.value) return categoryTree.value;
  return categoryTree.value;
});

watch(treeFilterText, (val: string) => {
  treeRef.value?.filter(val);
});

const buildTree = (list: any[]) => {
  const map: Record<string, any> = {};
  const roots: any[] = [];
  list.forEach((item: any) => {
    map[item.id] = { ...item, children: [] as any[] };
  });
  list.forEach((item: any) => {
    if (
      item.parentId &&
      item.parentId !== '0' &&
      item.parentId !== 0 &&
      map[item.parentId]
    ) {
      map[item.parentId].children.push(map[item.id]);
    } else {
      roots.push(map[item.id]);
    }
  });
  roots.forEach((root: any) => {
    if (root.children.length === 0) delete root.children;
  });
  return roots;
};

const filterTreeNode = (value: string, data: any) => {
  if (!value) return true;
  return data.name.includes(value);
};

const formatDate = (dateString: string) => {
  if (!dateString) return '-';
  return new Date(dateString).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
};

const formatDateTime = (dateString: string) => {
  if (!dateString) return '-';
  return new Date(dateString).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
};

const getLogTypeClass = (type: string) => {
  const map: Record<string, string> = {
    '创建': 'badge-success',
    '编辑': 'badge-primary',
    '删除': 'badge-danger',
    '启用': 'badge-success',
    '禁用': 'badge-warning',
    '导入': 'badge-info',
  };
  return map[type] || 'badge-info';
};

const fetchStats = async () => {
  try {
    const res = await api.admin.categories.getCategoryStats();
    if (res.code === 200) {
      stats.value = res.data || { total: 0, active: 0, level1: 0, level2: 0 };
    }
  } catch {
    stats.value = { total: 0, active: 0, level1: 0, level2: 0 };
  }
};

const fetchAllCategories = async () => {
  try {
    const res = await api.admin.categories.getCategories({ size: 9999 });
    if (res.code === 200) {
      const list = res.data.content || res.data || [];
      allCategories.value = list;
      categoryTree.value = buildTree(list);
      level1Categories.value = list.filter((c: any) => c.level === 1);
    }
  } catch {
    ElMessage.error('获取分类数据失败');
  }
};

const fetchTableCategories = async () => {
  try {
    const params: Record<string, any> = { page: tablePage.value, size: tablePageSize.value };
    if (tableStatus.value) params.status = tableStatus.value;
    const res = await api.admin.categories.getCategories(params);
    if (res.code === 200) {
      tableCategories.value = res.data.content || [];
      tableTotal.value = res.data.totalElements || 0;
    }
  } catch {
    ElMessage.error('获取分类列表失败');
  }
};

const fetchChangeLogs = async () => {
  try {
    const params = { page: logPage.value, size: logPageSize.value };
    const res = await api.admin.categories.getChangeLogs(params);
    if (res.code === 200) {
      changeLogs.value = res.data.content || res.data || [];
      logTotal.value = res.data.totalElements || changeLogs.value.length || 0;
    }
  } catch {
    changeLogs.value = [];
  }
};

const handleExpandAll = () => {
  allExpanded.value = !allExpanded.value;
  nextTick(() => {
    const nodes = (treeRef.value?.store?.root?.childNodes || []) as any[];
    const toggleAll = (nodeList: any[], expand: boolean) => {
      nodeList.forEach((node: any) => {
        node.expanded = expand;
        if (node.childNodes?.length) toggleAll(node.childNodes, expand);
      });
    };
    toggleAll(nodes as any[], allExpanded.value);
  });
};

const handleRefreshTree = () => {
  fetchAllCategories();
  fetchStats();
};

const handleNodeClick = (data: any) => {
  currentCategory.value = { ...data };
  panelMode.value = 'detail';
};

const handleCheckChange = () => {
  selectedTreeKeys.value = treeRef.value?.getCheckedKeys() || [];
};

const handleAddRoot = () => {
  editForm.value = {
    name: '',
    parentId: '0',
    sort: 1,
    status: 1,
    icon: '',
    description: '',
  };
  panelMode.value = 'create';
};

const handleAddChild = (parent: any) => {
  editForm.value = {
    name: '',
    parentId: parent.id,
    sort: 1,
    status: 1,
    icon: '',
    description: '',
  };
  panelMode.value = 'create';
};

const handleEditNode = (data: any) => {
  editForm.value = { ...data, status: data.status ? 1 : 0 };
  panelMode.value = 'edit';
};

const handleEditCurrent = () => {
  if (!currentCategory.value) return;
  editForm.value = {
    ...currentCategory.value,
    status: currentCategory.value.status ? 1 : 0,
  };
  panelMode.value = 'edit';
};

const handleCancelEdit = () => {
  if (currentCategory.value) {
    panelMode.value = 'detail';
  } else {
    panelMode.value = 'empty';
  }
  editForm.value = null;
};

const handleSave = async () => {
  if (!editForm.value.name?.trim()) {
    ElMessage.warning('请输入分类名称');
    return;
  }
  saving.value = true;
  try {
    const isEdit = panelMode.value === 'edit';
    const res = isEdit
      ? await api.admin.categories.updateCategory(editForm.value.id, editForm.value)
      : await api.admin.categories.createCategory(editForm.value);
    if (res.code === 200) {
      ElMessage.success(isEdit ? '分类已更新' : '分类已添加');
      const savedData = res.data || editForm.value;
      currentCategory.value = { ...savedData };
      panelMode.value = 'detail';
      editForm.value = null;
      fetchAllCategories();
      fetchTableCategories();
      fetchStats();
    } else {
      ElMessage.error(res.message || '操作失败');
    }
  } catch {
    ElMessage.error('网络错误');
  } finally {
    saving.value = false;
  }
};

const handleDeleteNode = (data: any) => {
  if (data.itemCount > 0) {
    ElMessage.warning('该分类下有物品，无法删除');
    return;
  }
  ElMessageBox.confirm(`确定删除分类「${data.name}」？`, '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      try {
        const res = await api.admin.categories.deleteCategory(data.id);
        if (res.code === 200) {
          ElMessage.success('分类已删除');
          if (currentCategory.value?.id === data.id) {
            currentCategory.value = null;
            panelMode.value = 'empty';
          }
          fetchAllCategories();
          fetchTableCategories();
          fetchStats();
        } else {
          ElMessage.error(res.message || '操作失败');
        }
      } catch {
        ElMessage.error('网络错误');
      }
    })
    .catch(() => {});
};

const handleSelectAll = (e: any) => {
  selectedCategories.value = e.target.checked
    ? tableCategories.value.map((c) => c.id)
    : [];
};

const handleTableSizeChange = () => {
  tablePage.value = 1;
  fetchTableCategories();
};

const handleSelectionChange = (selection: any[]) => {
  selectedCategories.value = selection.map((item: any) => item.id);
};

const handleBatchEnable = async () => {
  ElMessageBox.confirm(
    `批量启用 ${selectedTreeKeys.value.length} 个分类？`,
    '确认'
  )
    .then(async () => {
      try {
        const res = await api.admin.categories.batchEnable(
          selectedTreeKeys.value
        );
        if (res.code === 200) {
          ElMessage.success('操作成功');
          selectedTreeKeys.value = [];
          treeRef.value?.setCheckedKeys([]);
          fetchAllCategories();
          fetchTableCategories();
          fetchStats();
        } else {
          ElMessage.error(res.message || '操作失败');
        }
      } catch {
        ElMessage.error('网络错误');
      }
    })
    .catch(() => {});
};

const handleBatchDisable = async () => {
  ElMessageBox.confirm(
    `批量禁用 ${selectedTreeKeys.value.length} 个分类？`,
    '确认'
  )
    .then(async () => {
      try {
        const res = await api.admin.categories.batchDisable(
          selectedTreeKeys.value
        );
        if (res.code === 200) {
          ElMessage.success('操作成功');
          selectedTreeKeys.value = [];
          treeRef.value?.setCheckedKeys([]);
          fetchAllCategories();
          fetchTableCategories();
          fetchStats();
        } else {
          ElMessage.error(res.message || '操作失败');
        }
      } catch {
        ElMessage.error('网络错误');
      }
    })
    .catch(() => {});
};

const handleBatchDelete = async () => {
  ElMessageBox.confirm(
    `批量删除 ${selectedTreeKeys.value.length} 个分类？此操作不可恢复。`,
    '确认删除',
    {
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        const res = await api.admin.categories.batchDelete(
          selectedTreeKeys.value
        );
        if (res.code === 200) {
          ElMessage.success('操作成功');
          selectedTreeKeys.value = [];
          treeRef.value?.setCheckedKeys([]);
          currentCategory.value = null;
          panelMode.value = 'empty';
          fetchAllCategories();
          fetchTableCategories();
          fetchStats();
        } else {
          ElMessage.error(res.message || '操作失败');
        }
      } catch {
        ElMessage.error('网络错误');
      }
    })
    .catch(() => {});
};

const handleBulkEnable = async () => {
  ElMessageBox.confirm(
    `批量启用 ${selectedCategories.value.length} 个分类？`,
    '确认'
  )
    .then(async () => {
      try {
        const res = await api.admin.categories.batchEnable(
          selectedCategories.value
        );
        if (res.code === 200) {
          ElMessage.success('操作成功');
        } else {
          ElMessage.error(res.message || '操作失败');
        }
      } catch {
        ElMessage.error('网络错误');
      }
      selectedCategories.value = [];
      fetchTableCategories();
      fetchAllCategories();
      fetchStats();
    })
    .catch(() => {});
};

const handleBulkDisable = async () => {
  ElMessageBox.confirm(
    `批量禁用 ${selectedCategories.value.length} 个分类？`,
    '确认'
  )
    .then(async () => {
      try {
        const res = await api.admin.categories.batchDisable(
          selectedCategories.value
        );
        if (res.code === 200) {
          ElMessage.success('操作成功');
        } else {
          ElMessage.error(res.message || '操作失败');
        }
      } catch {
        ElMessage.error('网络错误');
      }
      selectedCategories.value = [];
      fetchTableCategories();
      fetchAllCategories();
      fetchStats();
    })
    .catch(() => {});
};

const handleBulkDelete = async () => {
  ElMessageBox.confirm(
    `批量删除 ${selectedCategories.value.length} 个分类？`,
    '确认'
  )
    .then(async () => {
      try {
        const res = await api.admin.categories.batchDelete(
          selectedCategories.value
        );
        if (res.code === 200) {
          ElMessage.success('操作成功');
        } else {
          ElMessage.error(res.message || '操作失败');
        }
      } catch {
        ElMessage.error('网络错误');
      }
      selectedCategories.value = [];
      fetchTableCategories();
      fetchAllCategories();
      fetchStats();
    })
    .catch(() => {});
};

const handleExport = async () => {
  try {
    const blob = await api.admin.categories.exportCategories();
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `分类数据_${new Date().toISOString().slice(0, 10)}.csv`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
    ElMessage.success('导出成功');
  } catch {
    ElMessage.error('导出失败');
  }
};

const triggerImport = () => {
  importInputRef.value?.click();
};

const handleImportFile = async (event: any) => {
  const file = event.target.files?.[0];
  if (!file) return;
  if (!file.name.endsWith('.csv')) {
    ElMessage.error('请选择 CSV 文件');
    event.target.value = '';
    return;
  }
  try {
    const formData = new FormData();
    formData.append('file', file);
    const res = await api.admin.categories.importCategories(formData);
    if (res.code === 200) {
      ElMessage.success('导入成功');
      fetchAllCategories();
      fetchTableCategories();
      fetchStats();
    } else {
      ElMessage.error(res.message || '导入失败');
    }
  } catch {
    ElMessage.error('导入失败');
  }
  event.target.value = '';
};

const handleIconSuccess = (response: any) => {
  if (response.code === 200) {
    editForm.value.icon = response.data.url;
  }
};

const beforeIconUpload = (file: any) => {
  const isJpgOrPng =
    file.type === 'image/jpeg' ||
    file.type === 'image/png' ||
    file.type === 'image/webp';
  if (!isJpgOrPng) {
    ElMessage.error('只能上传 JPG、PNG 或 WebP 格式的图片！');
  }
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB！');
  }
  return isJpgOrPng && isLt2M;
};

onMounted(() => {
  fetchStats();
  fetchAllCategories();
  fetchTableCategories();
  fetchChangeLogs();
});
</script>

<style scoped src="../../styles/pages/admin-category-management.css"></style>
