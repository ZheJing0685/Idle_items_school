<template>
  <div class="category-management">
    <div class="page-intro">
      <h2 class="section-title">分类管理</h2>
      <p class="section-desc">管理平台物品分类，支持多级分类结构</p>
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
            <path d="M4 6h16M4 10h16M4 14h16M4 18h16" />
          </svg>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.total }}</span>
          <span class="stat-label">分类总数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-active">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.5"
          >
            <circle cx="12" cy="12" r="9" />
            <path d="M12 7v5l3 3" />
          </svg>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.active }}</span>
          <span class="stat-label">活跃分类</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-level1">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.5"
          >
            <path d="M12 2L2 7l10 5 10-5-10-5z" />
            <path d="M2 17l10 5 10-5" />
            <path d="M2 12l10 5 10-5" />
          </svg>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.level1 }}</span>
          <span class="stat-label">一级分类</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-level2">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.5"
          >
            <path d="M3 12s0-2 2-2 2 2 2 2-2 2-2 2" />
            <path d="M13 12s0-2 2-2 2 2 2 2-2 2-2 2" />
            <path d="M3 6s0-2 2-2 2 2 2 2-2 2-2 2" />
            <path d="M13 6s0-2 2-2 2 2 2 2-2 2-2 2" />
            <path d="M3 18s0-2 2-2 2 2 2 2-2 2-2 2" />
            <path d="M13 18s0-2 2-2 2 2 2 2-2 2-2 2" />
          </svg>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.level2 }}</span>
          <span class="stat-label">二级分类</span>
        </div>
      </div>
    </div>

    <div class="content-card">
      <div class="card-header">
        <div class="header-left">
          <h3 class="card-title">分类列表</h3>
          <span class="data-range">共 {{ total }} 条记录</span>
        </div>
        <div class="header-actions">
          <button class="btn btn-primary" @click="handleAdd">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
            >
              <path d="M12 5v14M5 12h14" />
            </svg>
            添加分类
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
            placeholder="搜索分类名称..."
            class="search-input"
            @keyup.enter="handleSearch"
          />
        </div>
        <div class="filter-selects">
          <select v-model="parentId" class="filter-select">
            <option value="">全部父分类</option>
            <option value="0">一级分类</option>
            <option
              v-for="cat in level1Categories"
              :key="cat.id"
              :value="cat.id"
            >
              {{ cat.name }}
            </option>
          </select>
          <select v-model="categoryStatus" class="filter-select">
            <option value="">全部状态</option>
            <option value="1">启用</option>
            <option value="0">禁用</option>
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
              <th class="col-name">分类名称</th>
              <th class="col-level">级别</th>
              <th class="col-parent">父分类</th>
              <th class="col-icon">图标</th>
              <th class="col-sort">排序</th>
              <th class="col-count">物品数</th>
              <th class="col-status">状态</th>
              <th class="col-date">创建时间</th>
              <th class="col-actions">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="category in categories"
              :key="category.id"
              class="table-row"
            >
              <td class="col-checkbox">
                <input
                  type="checkbox"
                  v-model="selectedCategories"
                  :value="category.id"
                />
              </td>
              <td class="col-name">
                <div class="category-cell">
                  <span
                    class="category-name"
                    :style="{ paddingLeft: (category.level - 1) * 20 + 'px' }"
                  >
                    <span
                      v-if="category.level > 1"
                      class="level-indicator"
                    ></span>
                    {{ category.name }}
                  </span>
                  <span class="category-desc" v-if="category.description">{{
                    truncateText(category.description, 30)
                  }}</span>
                </div>
              </td>
              <td class="col-level">
                <span class="badge" :class="getLevelClass(category.level)">
                  {{ category.level }}级
                </span>
              </td>
              <td class="col-parent">
                <span class="parent-name">{{
                  category.parentName || '无'
                }}</span>
              </td>
              <td class="col-icon">
                <div class="icon-preview" v-if="category.icon">
                  <img :src="category.icon" :alt="category.name" />
                </div>
                <span class="no-icon" v-else>无</span>
              </td>
              <td class="col-sort">
                <div class="sort-controls">
                  <button
                    class="sort-btn"
                    @click="handleMoveUp(category)"
                    :disabled="category.sort <= 1"
                  >
                    <svg
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="1.5"
                    >
                      <path d="M18 15l-6-6-6 6" />
                    </svg>
                  </button>
                  <span class="sort-value">{{ category.sort }}</span>
                  <button class="sort-btn" @click="handleMoveDown(category)">
                    <svg
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="1.5"
                    >
                      <path d="M6 9l6 6 6-6" />
                    </svg>
                  </button>
                </div>
              </td>
              <td class="col-count">
                <span class="item-count">{{ category.itemCount || 0 }}</span>
              </td>
              <td class="col-status">
                <el-switch
                  v-model="category.status"
                  active-color="#10b981"
                  inactive-color="#d1d5db"
                  @change="handleToggleStatus(category)"
                />
              </td>
              <td class="col-date">
                <span class="create-time">{{
                  formatDate(category.createdAt)
                }}</span>
              </td>
              <td class="col-actions">
                <div class="action-group">
                  <button
                    class="action-btn"
                    @click="handleView(category)"
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
                    class="action-btn"
                    @click="handleEdit(category)"
                    title="编辑"
                  >
                    <svg
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="1.5"
                    >
                      <path
                        d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"
                      />
                      <path
                        d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"
                      />
                    </svg>
                  </button>
                  <button
                    v-if="category.level === 1"
                    class="action-btn action-success"
                    @click="handleAddSubCategory(category)"
                    title="添加子分类"
                  >
                    <svg
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="1.5"
                    >
                      <path d="M12 5v14M5 12h14" />
                    </svg>
                  </button>
                  <button
                    class="action-btn action-danger"
                    @click="handleDelete(category)"
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
                fetchCategories();
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
                fetchCategories();
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
      title="分类详情"
      width="600px"
      class="category-detail-dialog"
    >
      <div class="category-detail" v-if="currentCategory">
        <div class="detail-section">
          <h4 class="section-title">基本信息</h4>
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
                currentCategory.parentName || '无'
              }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">排序</span>
              <span class="info-value">{{ currentCategory.sort }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">状态</span>
              <span
                class="info-value"
                :class="
                  currentCategory.status ? 'status-active' : 'status-inactive'
                "
              >
                {{ currentCategory.status ? '启用' : '禁用' }}
              </span>
            </div>
            <div class="info-item">
              <span class="info-label">物品数量</span>
              <span class="info-value">{{
                currentCategory.itemCount || 0
              }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section" v-if="currentCategory.icon">
          <h4 class="section-title">分类图标</h4>
          <div class="icon-display">
            <img :src="currentCategory.icon" :alt="currentCategory.name" />
          </div>
        </div>

        <div class="detail-section" v-if="currentCategory.description">
          <h4 class="section-title">分类描述</h4>
          <div class="desc-content">{{ currentCategory.description }}</div>
        </div>

        <div class="detail-section">
          <h4 class="section-title">时间信息</h4>
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
          <h4 class="section-title">分类路径</h4>
          <div class="path-content">{{ currentCategory.path }}</div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="formDialogVisible"
      :title="isEdit ? '编辑分类' : '添加分类'"
      width="600px"
      class="category-form-dialog"
    >
      <el-form :model="form" label-width="100px" class="category-form">
        <el-form-item label="分类名称" required>
          <el-input
            v-model="form.name"
            maxlength="50"
            placeholder="请输入分类名称"
          />
        </el-form-item>

        <el-form-item label="父分类">
          <el-select v-model="form.parentId" placeholder="选择父分类">
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
            v-model="form.sort"
            :min="1"
            :max="999"
            :step="1"
            placeholder="排序序号"
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-switch
            v-model="form.status"
            active-color="#10b981"
            inactive-color="#d1d5db"
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
            <div class="upload-area" v-if="!form.icon">
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
              <span>点击上传图标</span>
            </div>
            <div class="icon-preview" v-else>
              <img :src="form.icon" :alt="form.name" />
              <button type="button" class="remove-btn" @click="form.icon = ''">
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
            </div>
          </el-upload>
        </el-form-item>

        <el-form-item label="分类描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="请输入分类描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="formDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSave" :loading="saving"
            >保存</el-button
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
const parentId = ref('');
const categoryStatus = ref('');
const categories = ref([]);
const level1Categories = ref([]);
const selectedCategories = ref([]);
const page = ref(1);
const pageSize = ref(20);
const total = ref(0);
const detailDialogVisible = ref(false);
const formDialogVisible = ref(false);
const currentCategory = ref(null);
const form = ref({});
const isEdit = ref(false);
const saving = ref(false);

const stats = ref({ total: 0, active: 0, level1: 0, level2: 0 });
const totalPages = computed(() => Math.ceil(total.value / pageSize.value) || 1);
const isAllSelected = computed(
  () =>
    categories.value.length > 0 &&
    selectedCategories.value.length === categories.value.length
);

const getLevelClass = (level) => {
  return level === 1 ? 'badge-primary' : 'badge-info';
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

const formatDateTime = (dateString) => {
  if (!dateString) return '-';
  return new Date(dateString).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
};

const fetchCategories = async () => {
  try {
    const params = { page: page.value, size: pageSize.value };
    if (searchKeyword.value) params.keyword = searchKeyword.value;
    if (parentId.value) params.parentId = parentId.value;
    if (categoryStatus.value) params.status = categoryStatus.value;

    const res = await api.admin.categories.getCategories(params);
    if (res.code === 200) {
      categories.value = res.data.content || [];
      total.value = res.data.totalElements || 0;
    }
  } catch {
    ElMessage.error('网络错误');
  }
};

const fetchLevel1Categories = async () => {
  try {
    const res = await api.admin.categories.getCategories({ level: 1 });
    if (res.code === 200) {
      level1Categories.value = res.data || [];
    }
  } catch {
    ElMessage.error('网络错误');
  }
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

const handleSearch = () => {
  page.value = 1;
  fetchCategories();
};
const handleReset = () => {
  searchKeyword.value = '';
  parentId.value = '';
  categoryStatus.value = '';
  page.value = 1;
  fetchCategories();
};
const handleSizeChange = () => {
  page.value = 1;
  fetchCategories();
};
const handleSelectAll = (e) => {
  selectedCategories.value = e.target.checked
    ? categories.value.map((c) => c.id)
    : [];
};

const handleView = (category) => {
  currentCategory.value = { ...category };
  detailDialogVisible.value = true;
};

const handleAdd = () => {
  form.value = {
    name: '',
    parentId: '0',
    sort: 1,
    status: 1,
    icon: '',
    description: '',
  };
  isEdit.value = false;
  formDialogVisible.value = true;
};

const handleAddSubCategory = (parent) => {
  form.value = {
    name: '',
    parentId: parent.id,
    sort: 1,
    status: 1,
    icon: '',
    description: '',
  };
  isEdit.value = false;
  formDialogVisible.value = true;
};

const handleEdit = (category) => {
  form.value = { ...category };
  isEdit.value = true;
  formDialogVisible.value = true;
};

const handleSave = async () => {
  if (!form.value.name) {
    ElMessage.warning('请输入分类名称');
    return;
  }

  saving.value = true;
  try {
    const res = isEdit.value
      ? await api.admin.categories.update(form.value.id, form.value)
      : await api.admin.categories.create(form.value);

    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '分类已更新' : '分类已添加');
      formDialogVisible.value = false;
      fetchCategories();
      fetchLevel1Categories();
    } else {
      ElMessage.error(res.message || '操作失败');
    }
  } catch {
    ElMessage.error('网络错误');
  } finally {
    saving.value = false;
  }
};

const handleToggleStatus = async (category) => {
  try {
    const res = await api.admin.categories.updateStatus(category.id, category.status);
    if (res.code !== 200) {
      category.status = !category.status;
      ElMessage.error(res.message || '操作失败');
    }
  } catch {
    category.status = !category.status;
    ElMessage.error('网络错误');
  }
};

const handleDelete = (category) => {
  if (category.itemCount > 0) {
    ElMessage.warning('该分类下有物品，无法删除');
    return;
  }

  ElMessageBox.confirm(`确定删除分类 ${category.name}？`, '确认')
    .then(async () => {
      try {
        const res = await api.admin.categories.deleteCategory(category.id);
        if (res.code === 200) {
          ElMessage.success('分类已删除');
          fetchCategories();
          fetchLevel1Categories();
        } else {
          ElMessage.error(res.message || '操作失败');
        }
      } catch {
        ElMessage.error('网络错误');
      }
    })
    .catch(() => {});
};

const handleMoveUp = async (category) => {
  try {
    const res = await api.admin.categories.moveUp(category.id);
    if (res.code === 200) {
      ElMessage.success('排序已更新');
      fetchCategories();
    } else {
      ElMessage.error(res.message || '操作失败');
    }
  } catch {
    ElMessage.error('网络错误');
  }
};

const handleMoveDown = async (category) => {
  try {
    const res = await api.admin.categories.moveDown(category.id);
    if (res.code === 200) {
      ElMessage.success('排序已更新');
      fetchCategories();
    } else {
      ElMessage.error(res.message || '操作失败');
    }
  } catch {
    ElMessage.error('网络错误');
  }
};

const handleBulkEnable = async () => {
  ElMessageBox.confirm(
    `批量启用 ${selectedCategories.value.length} 个分类？`,
    '确认'
  )
    .then(async () => {
      try {
        const res = await api.admin.categories.batchEnable(selectedCategories.value);
        if (res.code === 200) {
          ElMessage.success(`已启用 ${selectedCategories.value.length} 个分类`);
        } else {
          ElMessage.error(res.message || '操作失败');
        }
      } catch {
        ElMessage.error('网络错误');
      }
      selectedCategories.value = [];
      fetchCategories();
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
        const res = await api.admin.categories.batchDisable(selectedCategories.value);
        if (res.code === 200) {
          ElMessage.success(`已禁用 ${selectedCategories.value.length} 个分类`);
        } else {
          ElMessage.error(res.message || '操作失败');
        }
      } catch {
        ElMessage.error('网络错误');
      }
      selectedCategories.value = [];
      fetchCategories();
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
        const res = await api.admin.categories.batchDelete(selectedCategories.value);
        if (res.code === 200) {
          ElMessage.success(`已删除 ${selectedCategories.value.length} 个分类`);
        } else {
          ElMessage.error(res.message || '操作失败');
        }
      } catch {
        ElMessage.error('网络错误');
      }
      selectedCategories.value = [];
      fetchCategories();
      fetchLevel1Categories();
    })
    .catch(() => {});
};

const handleIconSuccess = (response) => {
  if (response.code === 200) {
    form.value.icon = response.data.url;
  }
};

const beforeIconUpload = (file) => {
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
  fetchCategories();
  fetchLevel1Categories();
  fetchStats();
});
</script>

<style scoped src="../../styles/pages/admin-category-management.css"></style>
