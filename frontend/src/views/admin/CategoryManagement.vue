<template>
  <div class="category-management">
    <div class="page-intro">
      <h2 class="section-title">分类管理</h2>
      <p class="section-desc">管理平台物品分类，支持多级分类结构</p>
    </div>

    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon stat-icon-total">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
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
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
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
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
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
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
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

    <div class="main-content">
      <div class="tree-panel">
        <div class="tree-header">
          <h3 class="tree-title">分类结构</h3>
          <div class="tree-actions">
            <button class="btn btn-icon" @click="handleExpandAll" :title="allExpanded ? '全部收起' : '全部展开'">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path v-if="allExpanded" d="M7 15l5-5 5 5" />
                <path v-else d="M7 9l5 5 5-5" />
              </svg>
            </button>
            <button class="btn btn-icon" @click="handleRefreshTree" title="刷新">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M21.5 2v6h-6M2.5 22v-6h6" />
                <path d="M2 12A10 10 0 1 0 22 12" />
              </svg>
            </button>
          </div>
        </div>

        <div class="tree-search">
          <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8" />
            <path d="M21 21l-4.35-4.35" />
          </svg>
          <input
            v-model="treeFilterText"
            type="text"
            placeholder="搜索分类..."
            class="search-input"
          />
        </div>

        <div class="tree-toolbar">
          <button class="btn btn-primary btn-sm" @click="handleAddRoot">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M12 5v14M5 12h14" />
            </svg>
            添加一级分类
          </button>
          <div class="tree-batch" v-if="selectedTreeKeys.length > 0">
            <span class="batch-count">{{ selectedTreeKeys.length }} 项</span>
            <button class="btn btn-success btn-xs" @click="handleBatchEnable">启用</button>
            <button class="btn btn-warning btn-xs" @click="handleBatchDisable">禁用</button>
            <button class="btn btn-danger btn-xs" @click="handleBatchDelete">删除</button>
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
            <template #default="{ node, data }">
              <div class="tree-node" :class="{ 'is-disabled': !data.status }">
                <div class="tree-node-content">
                  <span class="tree-node-icon" v-if="data.icon">
                    <img :src="data.icon" :alt="data.name" />
                  </span>
                  <span class="tree-node-name">{{ data.name }}</span>
                  <span class="tree-node-count">{{ data.itemCount || 0 }}</span>
                  <span class="tree-node-status" :class="data.status ? 'status-active' : 'status-inactive'">
                    {{ data.status ? '启用' : '禁用' }}
                  </span>
                </div>
                <div class="tree-node-actions" @click.stop>
                  <button class="tree-action-btn" @click="handleEditNode(data)" title="编辑">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                      <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7" />
                      <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z" />
                    </svg>
                  </button>
                  <button
                    v-if="data.level === 1"
                    class="tree-action-btn tree-action-success"
                    @click="handleAddChild(data)"
                    title="添加子分类"
                  >
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                      <path d="M12 5v14M5 12h14" />
                    </svg>
                  </button>
                  <button class="tree-action-btn tree-action-danger" @click="handleDeleteNode(data)" title="删除">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                      <path d="M3 6h18M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2" />
                    </svg>
                  </button>
                </div>
              </div>
            </template>
          </el-tree>

          <div class="tree-empty" v-if="filteredTreeData.length === 0">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
            </svg>
            <span>暂无分类数据</span>
          </div>
        </div>
      </div>

      <div class="detail-panel">
        <template v-if="panelMode === 'empty'">
          <div class="detail-empty">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
              <path d="M3 9h18M9 21V9" />
            </svg>
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
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
                </svg>
              </div>
              <div class="detail-title-group">
                <h3 class="detail-name">{{ currentCategory.name }}</h3>
                <div class="detail-badges">
                  <span class="badge" :class="currentCategory.level === 1 ? 'badge-primary' : 'badge-info'">
                    {{ currentCategory.level }}级分类
                  </span>
                  <span class="badge" :class="currentCategory.status ? 'badge-success' : 'badge-danger'">
                    {{ currentCategory.status ? '启用' : '禁用' }}
                  </span>
                </div>
              </div>
            </div>
            <div class="detail-header-right">
              <button class="btn btn-primary btn-sm" @click="handleEditCurrent">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7" />
                  <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z" />
                </svg>
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
                  <span class="info-value">{{ currentCategory.parentName || '无（一级分类）' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">排序权重</span>
                  <span class="info-value">{{ currentCategory.sort }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">物品数量</span>
                  <span class="info-value">{{ currentCategory.itemCount || 0 }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">状态</span>
                  <span class="info-value" :class="currentCategory.status ? 'status-active' : 'status-inactive'">
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
                  <span class="info-value">{{ formatDateTime(currentCategory.createdAt) }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">更新时间</span>
                  <span class="info-value">{{ formatDateTime(currentCategory.updatedAt) }}</span>
                </div>
              </div>
            </div>

            <div class="detail-section" v-if="currentCategory.path">
              <h4 class="detail-section-title">分类路径</h4>
              <div class="path-content">{{ currentCategory.path }}</div>
            </div>
          </div>
        </template>

        <template v-if="(panelMode === 'edit' || panelMode === 'create') && editForm">
          <div class="edit-header">
            <h3 class="edit-title">{{ panelMode === 'edit' ? '编辑分类' : '新增分类' }}</h3>
            <button class="btn btn-ghost btn-sm" @click="handleCancelEdit">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M18 6L6 18M6 6l12 12" />
              </svg>
              取消
            </button>
          </div>

          <div class="edit-body">
            <el-form :model="editForm" label-width="100px" class="category-form">
              <el-form-item label="分类名称" required>
                <el-input v-model="editForm.name" maxlength="50" placeholder="请输入分类名称" />
              </el-form-item>

              <el-form-item label="父分类">
                <el-select v-model="editForm.parentId" placeholder="选择父分类" style="width: 100%">
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
                <el-input-number v-model="editForm.sort" :min="1" :max="999" :step="1" />
              </el-form-item>

              <el-form-item label="状态">
                <el-switch v-model="editForm.status" :active-value="1" :inactive-value="0" />
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
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                      <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4M7 10l5 5 5-5M12 15V3" />
                    </svg>
                    <span>点击上传图标</span>
                  </div>
                  <div class="icon-preview" v-else>
                    <img :src="editForm.icon" :alt="editForm.name" />
                    <button type="button" class="remove-btn" @click.stop="editForm.icon = ''">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                        <circle cx="12" cy="12" r="10" />
                        <path d="M15 9l-6 6M9 9l6 6" />
                      </svg>
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
                  <button class="btn btn-ghost" @click="handleCancelEdit">取消</button>
                  <button class="btn btn-primary" @click="handleSave" :disabled="saving">
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
              <select v-model="tableStatus" class="filter-select" @change="fetchTableCategories">
                <option value="">全部状态</option>
                <option value="1">启用</option>
                <option value="0">禁用</option>
              </select>
            </div>
            <div class="tab-actions">
              <button class="btn btn-ghost btn-sm" @click="handleExport">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4M7 10l5 5 5-5M12 15V3" />
                </svg>
                导出
              </button>
              <button class="btn btn-ghost btn-sm" @click="triggerImport">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4M17 8l-5-5-5 5M12 3v12" />
                </svg>
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
            <table class="data-table">
              <thead>
                <tr>
                  <th class="col-checkbox">
                    <input type="checkbox" @change="handleSelectAll" :checked="isAllSelected" />
                  </th>
                  <th class="col-name">分类名称</th>
                  <th class="col-level">级别</th>
                  <th class="col-parent">父分类</th>
                  <th class="col-sort">排序</th>
                  <th class="col-count">物品数</th>
                  <th class="col-status">状态</th>
                  <th class="col-date">创建时间</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="category in tableCategories" :key="category.id" class="table-row">
                  <td class="col-checkbox">
                    <input type="checkbox" v-model="selectedCategories" :value="category.id" />
                  </td>
                  <td class="col-name">
                    <div class="category-cell">
                      <span class="category-name" :style="{ paddingLeft: (category.level - 1) * 20 + 'px' }">
                        <span v-if="category.level > 1" class="level-indicator"></span>
                        {{ category.name }}
                      </span>
                    </div>
                  </td>
                  <td class="col-level">
                    <span class="badge" :class="category.level === 1 ? 'badge-primary' : 'badge-info'">
                      {{ category.level }}级
                    </span>
                  </td>
                  <td class="col-parent">
                    <span class="parent-name">{{ category.parentName || '无' }}</span>
                  </td>
                  <td class="col-sort">
                    <span class="sort-value">{{ category.sort }}</span>
                  </td>
                  <td class="col-count">
                    <span class="item-count">{{ category.itemCount || 0 }}</span>
                  </td>
                  <td class="col-status">
                    <span class="status-tag" :class="category.status ? 'status-active' : 'status-inactive'">
                      {{ category.status ? '启用' : '禁用' }}
                    </span>
                  </td>
                  <td class="col-date">
                    <span class="create-time">{{ formatDate(category.createdAt) }}</span>
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
              <button class="btn btn-sm btn-success" @click="handleBulkEnable">批量启用</button>
              <button class="btn btn-sm btn-warning" @click="handleBulkDisable">批量禁用</button>
              <button class="btn btn-sm btn-danger" @click="handleBulkDelete">批量删除</button>
            </div>
          </div>

          <div class="pagination-wrapper">
            <div class="pagination-info">
              显示 {{ (tablePage - 1) * tablePageSize + 1 }} - {{ Math.min(tablePage * tablePageSize, tableTotal) }} 条，共 {{ tableTotal }} 条
            </div>
            <div class="pagination-controls">
              <select v-model="tablePageSize" class="page-size-select" @change="handleTableSizeChange">
                <option :value="10">10 条/页</option>
                <option :value="20">20 条/页</option>
                <option :value="50">50 条/页</option>
              </select>
              <div class="pagination-buttons">
                <button class="page-btn" :disabled="tablePage === 1" @click="tablePage--; fetchTableCategories()">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M15 18l-6-6 6-6" />
                  </svg>
                </button>
                <span class="page-indicator">{{ tablePage }} / {{ tableTotalPages }}</span>
                <button class="page-btn" :disabled="tablePage >= tableTotalPages" @click="tablePage++; fetchTableCategories()">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M9 18l6-6-6-6" />
                  </svg>
                </button>
              </div>
            </div>
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
                    <span class="badge" :class="getLogTypeClass(log.operationType)">
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
            <div class="pagination-info">
              共 {{ logTotal }} 条记录
            </div>
            <div class="pagination-controls">
              <div class="pagination-buttons">
                <button class="page-btn" :disabled="logPage === 1" @click="logPage--; fetchChangeLogs()">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M15 18l-6-6 6-6" />
                  </svg>
                </button>
                <span class="page-indicator">{{ logPage }} / {{ logTotalPages }}</span>
                <button class="page-btn" :disabled="logPage >= logTotalPages" @click="logPage++; fetchChangeLogs()">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M9 18l6-6-6-6" />
                  </svg>
                </button>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, nextTick } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import api from '../../api';

const stats = ref({ total: 0, active: 0, level1: 0, level2: 0 });
const allCategories = ref([]);
const level1Categories = ref([]);
const categoryTree = ref([]);
const treeRef = ref(null);
const treeFilterText = ref('');
const allExpanded = ref(true);
const selectedTreeKeys = ref([]);

const currentCategory = ref(null);
const panelMode = ref('empty');
const editForm = ref(null);
const saving = ref(false);

const activeTab = ref('management');
const tableCategories = ref([]);
const tableStatus = ref('');
const tablePage = ref(1);
const tablePageSize = ref(20);
const tableTotal = ref(0);
const selectedCategories = ref([]);
const importInputRef = ref(null);

const changeLogs = ref([]);
const logPage = ref(1);
const logPageSize = ref(20);
const logTotal = ref(0);

const treeProps = { children: 'children', label: 'name' };

const tableTotalPages = computed(() => Math.ceil(tableTotal.value / tablePageSize.value) || 1);
const logTotalPages = computed(() => Math.ceil(logTotal.value / logPageSize.value) || 1);
const isAllSelected = computed(
  () => tableCategories.value.length > 0 && selectedCategories.value.length === tableCategories.value.length
);

const filteredTreeData = computed(() => {
  if (!treeFilterText.value) return categoryTree.value;
  return categoryTree.value;
});

watch(treeFilterText, (val) => {
  treeRef.value?.filter(val);
});

const buildTree = (list) => {
  const map = {};
  const roots = [];
  list.forEach((item) => {
    map[item.id] = { ...item, children: [] };
  });
  list.forEach((item) => {
    if (item.parentId && item.parentId !== '0' && item.parentId !== 0 && map[item.parentId]) {
      map[item.parentId].children.push(map[item.id]);
    } else {
      roots.push(map[item.id]);
    }
  });
  roots.forEach((root) => {
    if (root.children.length === 0) delete root.children;
  });
  return roots;
};

const filterTreeNode = (value, data) => {
  if (!value) return true;
  return data.name.includes(value);
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

const getLogTypeClass = (type) => {
  const map = {
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
      level1Categories.value = list.filter((c) => c.level === 1);
    }
  } catch {
    ElMessage.error('获取分类数据失败');
  }
};

const fetchTableCategories = async () => {
  try {
    const params = { page: tablePage.value, size: tablePageSize.value };
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
    const nodes = treeRef.value?.store?.root?.childNodes || [];
    const toggleAll = (nodeList, expand) => {
      nodeList.forEach((node) => {
        node.expanded = expand;
        if (node.childNodes?.length) toggleAll(node.childNodes, expand);
      });
    };
    toggleAll(nodes, allExpanded.value);
  });
};

const handleRefreshTree = () => {
  fetchAllCategories();
  fetchStats();
};

const handleNodeClick = (data) => {
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

const handleAddChild = (parent) => {
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

const handleEditNode = (data) => {
  editForm.value = { ...data, status: data.status ? 1 : 0 };
  panelMode.value = 'edit';
};

const handleEditCurrent = () => {
  if (!currentCategory.value) return;
  editForm.value = { ...currentCategory.value, status: currentCategory.value.status ? 1 : 0 };
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
      ? await api.admin.categories.update(editForm.value.id, editForm.value)
      : await api.admin.categories.create(editForm.value);
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

const handleDeleteNode = (data) => {
  if (data.itemCount > 0) {
    ElMessage.warning('该分类下有物品，无法删除');
    return;
  }
  ElMessageBox.confirm(`确定删除分类「${data.name}」？`, '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
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
  }).catch(() => {});
};

const handleSelectAll = (e) => {
  selectedCategories.value = e.target.checked ? tableCategories.value.map((c) => c.id) : [];
};

const handleTableSizeChange = () => {
  tablePage.value = 1;
  fetchTableCategories();
};

const handleBatchEnable = async () => {
  ElMessageBox.confirm(`批量启用 ${selectedTreeKeys.value.length} 个分类？`, '确认').then(async () => {
    try {
      const res = await api.admin.categories.batchEnable(selectedTreeKeys.value);
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
  }).catch(() => {});
};

const handleBatchDisable = async () => {
  ElMessageBox.confirm(`批量禁用 ${selectedTreeKeys.value.length} 个分类？`, '确认').then(async () => {
    try {
      const res = await api.admin.categories.batchDisable(selectedTreeKeys.value);
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
  }).catch(() => {});
};

const handleBatchDelete = async () => {
  ElMessageBox.confirm(`批量删除 ${selectedTreeKeys.value.length} 个分类？此操作不可恢复。`, '确认删除', {
    type: 'warning',
  }).then(async () => {
    try {
      const res = await api.admin.categories.batchDelete(selectedTreeKeys.value);
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
  }).catch(() => {});
};

const handleBulkEnable = async () => {
  ElMessageBox.confirm(`批量启用 ${selectedCategories.value.length} 个分类？`, '确认').then(async () => {
    try {
      const res = await api.admin.categories.batchEnable(selectedCategories.value);
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
  }).catch(() => {});
};

const handleBulkDisable = async () => {
  ElMessageBox.confirm(`批量禁用 ${selectedCategories.value.length} 个分类？`, '确认').then(async () => {
    try {
      const res = await api.admin.categories.batchDisable(selectedCategories.value);
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
  }).catch(() => {});
};

const handleBulkDelete = async () => {
  ElMessageBox.confirm(`批量删除 ${selectedCategories.value.length} 个分类？`, '确认').then(async () => {
    try {
      const res = await api.admin.categories.batchDelete(selectedCategories.value);
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
  }).catch(() => {});
};

const handleExport = async () => {
  try {
    const res = await api.admin.categories.exportCategories();
    const blob = new Blob([res], { type: 'text/csv;charset=utf-8;' });
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

const handleImportFile = async (event) => {
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

const handleIconSuccess = (response) => {
  if (response.code === 200) {
    editForm.value.icon = response.data.url;
  }
};

const beforeIconUpload = (file) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/webp';
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
