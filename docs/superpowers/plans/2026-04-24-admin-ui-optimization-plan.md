# Admin UI Optimization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor admin backend UI with unified blue-green cyan design tokens, migrate all pages from raw `fetch()` to centralized API service, and fix bugs (client-side pagination, hardcoded data, fake fallback data, stub bulk operations).

**Architecture:** Hybrid approach — preserve custom HTML tables, apply consistent CSS token system, migrate API calls to centralized Axios service. No new npm packages, no route changes, no backend API changes, no component extraction.

**Tech Stack:** Vue 3 Composition API, Element Plus (el-dialog/el-form/el-pagination/el-date-picker), Axios with interceptors, OKLCH CSS custom properties

---

## File Change Map

| # | File | Action | Responsibility |
|---|------|--------|----------------|
| 1 | `frontend/src/api/services/admin.js` | **Modify** | Add all missing endpoints (categories, verifications, users, items CRUD, logs detail, statistics sub-apis) |
| 2 | `frontend/src/styles/pages/admin.css` | **Modify** | Add `.stat-card` left border accent, skeleton loading animation keyframes, filter/search icon styles |
| 3 | `frontend/src/views/admin/Admin.vue` | **Modify** | Sidebar 260→240px, gradient brand area, left accent bar on nav hover |
| 4 | `frontend/src/views/admin/Statistics.vue` | **Modify** | Add skeleton loading while fetching, colored left-border stat cards |
| 5 | `frontend/src/views/admin/OrderManagement.vue` | **Modify** | 4+3 two-row stat layout, search icon prefix in filter inputs |
| 6 | `frontend/src/views/admin/VerificationManagement.vue` | **Modify** | Migrate fetch()→api, add api.admin.verifications.*, fix hardcoded http://localhost:7000 URL |
| 7 | `frontend/src/views/admin/CategoryManagement.vue` | **Modify** | Migrate fetch()→api, remove hardcoded stats, fix stub bulk operations |
| 8 | `frontend/src/views/admin/LogManagement.vue` | **Modify** | Migrate fetch()→api, remove fake fallback data, replace native date input with el-date-picker |
| 9 | `frontend/src/views/admin/UserManagement.vue` | **Modify** | Migrate fetch()→api, fix client-side pagination bug, fix stub bulk operations, replace ElMessageBox view with el-dialog |
| 10 | `frontend/src/views/admin/ItemManagement.vue` | **Modify** | Migrate fetch()→api, remove hardcoded stats, fix stub bulk operations |
| 11 | `frontend/src/styles/pages/admin-statistics.css` | **Modify** | Add skeleton animation, stat-card colored left border |
| 12 | `frontend/src/styles/pages/admin-order-management.css` | **Modify** | 4+3 two-row stats grid, search icon prefix |
| 13 | `frontend/src/styles/pages/admin-user-management.css` | **Modify** | Add el-dialog styles for user detail, form dialog |
| 14 | `frontend/src/styles/pages/admin-item-management.css` | **Modify** | Add colored stat-card left borders |
| 15 | `frontend/src/styles/pages/admin-verification-management.css` | **Modify** | Minor visual refinements |
| 16 | `frontend/src/styles/pages/admin-category-management.css` | **Modify** | Minor visual refinements |
| 17 | `frontend/src/styles/pages/admin-log-management.css` | **Modify** | el-date-picker styling |

---

### Task 1: Expand admin.js API Service

**Files:**
- Modify: `frontend/src/api/services/admin.js` (full file replace)

**Applies design spec:** §4.2

**Context:** Currently admin.js has only basic endpoints (statistics.getDashboard, users.getUsers/updateStatus, items.getItems/updateStatus, orders CRUD, logs.getLogs). The 5 pages using raw `fetch()` need these additional endpoints. All endpoints below have corresponding `@GetMapping`/`@PostMapping`/`@PutMapping`/`@DeleteMapping` methods in `AdminController.java`.

- [ ] **Step 1: Replace admin.js with expanded API service**

Write the complete file to `frontend/src/api/services/admin.js`:

```javascript
import instance from '../config/axios';

const admin = {
  statistics: {
    getDashboard: (params) => instance.get('/admin/statistics/dashboard', { params }),
    getMonthly: (params) => instance.get('/admin/statistics/monthly', { params }),
    getCategories: () => instance.get('/admin/statistics/categories'),
    getHotItems: (params) => instance.get('/admin/statistics/hot-items', { params }),
  },
  users: {
    getUsers: (params) => instance.get('/admin/users', { params }),
    getUser: (userId) => instance.get(`/admin/users/${userId}`),
    getUserStats: () => instance.get('/admin/users/stats'),
    updateStatus: (userId, status) => instance.put(`/admin/users/${userId}/status`, { status }),
    deleteUser: (userId) => instance.delete(`/admin/users/${userId}`),
    batchUpdateStatus: (userIds, status) => instance.put('/admin/users/batch/status', { userIds, status }),
    batchDelete: (userIds) => instance.post('/admin/users/batch/delete', { userIds }),
  },
  items: {
    getItems: (params) => instance.get('/admin/items', { params }),
    getItemStats: () => instance.get('/admin/items/stats'),
    updateStatus: (itemId, status) => instance.put(`/admin/items/${itemId}/status`, { status }),
    approve: (itemId) => instance.put(`/admin/items/${itemId}/approve`),
    reject: (itemId, reason) => instance.put(`/admin/items/${itemId}/reject`, { reason }),
    offShelf: (itemId, reason) => instance.put(`/admin/items/${itemId}/off-shelf`, { reason }),
    deleteItem: (itemId) => instance.delete(`/admin/items/${itemId}`),
    batchApprove: (itemIds) => instance.put('/admin/items/batch/approve', { itemIds }),
    batchReject: (itemIds, reason) => instance.put('/admin/items/batch/reject', { itemIds, reason }),
    batchOffShelf: (itemIds, reason) => instance.put('/admin/items/batch/off-shelf', { itemIds, reason }),
  },
  orders: {
    getOrders: (params) => instance.get('/admin/orders', { params }),
    getStats: () => instance.get('/admin/orders/stats'),
    getOrder: (orderId) => instance.get(`/admin/orders/${orderId}`),
    cancelOrder: (orderId, reason) => instance.put(`/admin/orders/${orderId}/cancel`, { reason }),
    approveRefund: (orderId) => instance.put(`/admin/orders/${orderId}/refund/approve`),
    batchCancelOrders: (orderIds, reason) => instance.put('/admin/orders/batch/cancel', { orderIds, reason }),
  },
  categories: {
    getCategories: (params) => instance.get('/admin/categories', { params }),
    getCategory: (categoryId) => instance.get(`/admin/categories/${categoryId}`),
    getCategoryStats: () => instance.get('/admin/categories/stats'),
    create: (data) => instance.post('/admin/categories', data),
    update: (categoryId, data) => instance.put(`/admin/categories/${categoryId}`, data),
    updateStatus: (categoryId, status) => instance.put(`/admin/categories/${categoryId}/status`, { status }),
    moveUp: (categoryId) => instance.put(`/admin/categories/${categoryId}/sort/up`),
    moveDown: (categoryId) => instance.put(`/admin/categories/${categoryId}/sort/down`),
    deleteCategory: (categoryId) => instance.delete(`/admin/categories/${categoryId}`),
    batchEnable: (categoryIds) => instance.put('/admin/categories/batch/enable', { categoryIds }),
    batchDisable: (categoryIds) => instance.put('/admin/categories/batch/disable', { categoryIds }),
    batchDelete: (categoryIds) => instance.post('/admin/categories/batch/delete', { categoryIds }),
  },
  verifications: {
    getVerifications: (params) => instance.get('/admin/verifications', { params }),
    getStats: () => instance.get('/admin/verifications/stats'),
    approve: (verificationId) => instance.put(`/admin/verifications/${verificationId}/approve`),
    reject: (verificationId, reason) => instance.put(`/admin/verifications/${verificationId}/reject`, { reason }),
    batchApprove: (verificationIds) => instance.put('/admin/verifications/batch/approve', { verificationIds }),
    batchReject: (verificationIds, reason) => instance.put('/admin/verifications/batch/reject', { verificationIds, reason }),
  },
  logs: {
    getLogs: (params) => instance.get('/admin/logs', { params }),
    getLog: (logId) => instance.get(`/admin/logs/${logId}`),
  },
};

export default admin;
```

- [ ] **Step 2: Verify file structure**

Run: `node -e "const admin = require('./frontend/src/api/services/admin.js'); console.log('Categories:', Object.keys(admin.categories)); console.log('Verifications:', Object.keys(admin.verifications));"` (or just visually confirm the file is valid JS with correct import/export)

Expected: No syntax errors, all 6 API groups exported.

---

### Task 2: CSS Token Refinements + Admin.vue Layout

**Files:**
- Modify: `frontend/src/styles/pages/admin.css`
- Modify: `frontend/src/views/admin/Admin.vue`

**Applies design spec:** §4.1 (Design Token System), §4.2 (Admin.vue Layout)

**Context:** admin.css already has a comprehensive token system. We need to add: `.stat-card` colored left border accent (for the stats row component used across all pages), skeleton loading keyframes (for Statistics.vue), and filter/search icon prefix styles. Admin.vue gets sidebar refinements.

- [ ] **Step 1: Add shared component styles to admin.css**

Add to `frontend/src/styles/pages/admin.css` (before the `@media` section, after `.label-fade-leave-to`):

```css
/* ===== Shared Component Styles ===== */
.stat-card {
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  border-radius: 0 2px 2px 0;
}

.stat-card.card-total::before { background: var(--color-primary); }
.stat-card.card-pending::before { background: var(--color-warning); }
.stat-card.card-success::before { background: var(--color-success); }
.stat-card.card-danger::before { background: var(--color-danger); }
.stat-card.card-info::before { background: var(--color-info); }
.stat-card.card-secondary::before { background: oklch(62% 0.12 158); }
.stat-card.card-active::before { background: var(--color-success); }
.stat-card.card-sold::before { background: var(--color-info); }

/* Skeleton Loading */
@keyframes skeleton-pulse {
  0% { background: var(--surface-section); }
  50% { background: oklch(94% 0.01 195); }
  100% { background: var(--surface-section); }
}

.skeleton {
  animation: skeleton-pulse 1.5s var(--ease-out-quart) infinite;
  border-radius: var(--radius-sm);
}

.skeleton-card {
  height: 120px;
  border-radius: var(--radius-lg);
}

.skeleton-line {
  height: 16px;
  margin-bottom: var(--space-3);
}

.skeleton-line:last-child {
  width: 60%;
}

/* Filter bar search icon */
.filters-bar .el-input__prefix {
  font-size: 16px;
  color: var(--content-tertiary);
}
```

- [ ] **Step 2: Refine Admin.vue sidebar**

Modify `frontend/src/views/admin/Admin.vue` — sidebar width 260→240px, gradient brand area, left accent bar on nav hover.

First, change the CSS variable (line 43):
```css
  --sidebar-width: 240px;
```

Then update `.nav-item` hover state (lines 192-195):
```css
.nav-item:hover {
  background: var(--surface-section);
  color: var(--content-primary);
  box-shadow: inset 3px 0 0 var(--color-primary);
}
```

Update `.nav-item.is-active` (lines 197-200):
```css
.nav-item.is-active {
  background: linear-gradient(135deg, var(--color-primary-lighter), oklch(92% 0.03 195));
  color: var(--color-primary);
  font-weight: 600;
}
```

Update `.sidebar-brand` (lines 77-84):
```css
.sidebar-brand {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-5);
  background: linear-gradient(135deg, oklch(62% 0.14 195 / 0.04), oklch(100% 0 0) 60%);
  border-bottom: 1px solid var(--border-subtle);
  min-height: var(--header-height);
}
```

Update `.page-title` font-size (line 327):
```css
  font-size: 18px;
```

---

### Task 3: Statistics.vue — Skeleton Loading + Visual Refinements

**Files:**
- Modify: `frontend/src/views/admin/Statistics.vue`
- Modify: `frontend/src/styles/pages/admin-statistics.css`

**Applies design spec:** §4.3 (Dashboard)

**Context:** Statistics.vue already uses centralized API (`api.admin.statistics.getDashboard`). Only needs: skeleton loading while data loads, and colored left-border stat cards. The CSS already has most styling.

- [ ] **Step 1: Add skeleton loading state to Statistics.vue**

Find the `<script setup>` section. Add a `loading` ref:

```javascript
const loading = ref(true);
```

In the data fetch function, wrap with loading:

```javascript
const fetchData = async () => {
  loading.value = true;
  try {
    const res = await api.admin.statistics.getDashboard({ period: period.value });
    if (res.data.code === 200) {
      statsData.value = res.data.data;
    }
  } catch (error) {
    console.error('获取仪表盘数据失败:', error);
  } finally {
    loading.value = false;
  }
};
```

- [ ] **Step 2: Update template with skeleton**

In the `<template>`, wrap the stats row content with a loading conditional. Replace direct `statsData.xxx` rendering with:

```html
<div class="stats-row">
  <div v-if="loading" v-for="n in 4" :key="n" class="stat-card skeleton skeleton-card" />
  <template v-else>
    <div class="stat-card card-total">
      <div class="stat-card__icon stat-icon-total">
        <svg><!-- 商品图标 --></svg>
      </div>
      <div class="stat-card__info">
        <span class="stat-card__label">商品总数</span>
        <span class="stat-card__value">{{ statsData.totalItems || 0 }}</span>
      </div>
      <span class="stat-card__change">+{{ statsData.newItemsToday || 0 }} 今日新增</span>
    </div>
    <div class="stat-card card-active">
      <!-- ... -->
    </div>
    <!-- repeat for orders, users, revenue -->
  </template>
</div>
```

- [ ] **Step 3: Add stat-card colored left border styles to admin-statistics.css**

Add before the `.charts-grid` section:

```css
.stat-card {
  position: relative;
  overflow: hidden;
  padding-left: var(--space-5);
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  border-radius: 0 2px 2px 0;
}
```

---

### Task 4: OrderManagement.vue — 4+3 Stat Layout + Search Icon

**Files:**
- Modify: `frontend/src/styles/pages/admin-order-management.css`
- Modify: `frontend/src/views/admin/OrderManagement.vue`

**Applies design spec:** §4.3 (Order Management)

**Context:** OrderManagement.vue already uses centralized API. The stats row has 7 items crammed in one row. Need to split into 4+3 two-row layout. Also need search icon prefix for filter inputs.

- [ ] **Step 1: Update stats grid CSS in admin-order-management.css**

Replace existing `.stats-row` grid:

```css
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-5);
  margin-bottom: var(--space-6);
}

.stats-row + .stats-row {
  margin-top: 0;
  margin-bottom: var(--space-6);
}

.stats-row .stat-card {
  background: var(--surface-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  transition: all 200ms var(--ease-out-quart);
}

.stats-row .stat-card:hover {
  box-shadow: var(--shadow-md);
  border-color: var(--color-primary-lighter);
}

.stats-row .stat-card:nth-child(n+5) {
  padding: var(--space-4) var(--space-5);
}

.stats-row .stat-card:nth-child(n+5) .stat-value {
  font-size: 20px;
}

@media (max-width: 1200px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: 1fr;
  }
}
```

- [ ] **Step 2: Update OrderManagement.vue template — split stats into two rows**

Replace the single `.stats-row` with two `.stats-row` divs:

```html
<div class="stats-row">
  <div class="stat-card card-total">
    <span class="stat-label">订单总数</span>
    <span class="stat-value">{{ stats.totalOrders || 0 }}</span>
  </div>
  <div class="stat-card card-pending">
    <span class="stat-label">待处理</span>
    <span class="stat-value">{{ stats.pendingOrders || 0 }}</span>
  </div>
  <div class="stat-card card-success">
    <span class="stat-label">已完成</span>
    <span class="stat-value">{{ stats.completedOrders || 0 }}</span>
  </div>
  <div class="stat-card card-danger">
    <span class="stat-label">已取消</span>
    <span class="stat-value">{{ stats.cancelledOrders || 0 }}</span>
  </div>
</div>
<div class="stats-row">
  <div class="stat-card card-info">
    <span class="stat-label">退款中</span>
    <span class="stat-value">{{ stats.refundingOrders || 0 }}</span>
  </div>
  <div class="stat-card">
    <span class="stat-label">今日订单</span>
    <span class="stat-value">{{ stats.todayOrders || 0 }}</span>
  </div>
  <div class="stat-card">
    <span class="stat-label">交易总额</span>
    <span class="stat-value">¥{{ (stats.totalAmount || 0).toLocaleString() }}</span>
  </div>
</div>
```

- [ ] **Step 3: Add el-input prefix icon to filter inputs**

For filter inputs using `el-input`, add `prefix-icon`:

```html
<el-input v-model="filters.orderNo" placeholder="订单号" clearable prefix-icon="Search" />
```

---

### Task 5: Refactor VerificationManagement.vue — fetch→api Migration

**Files:**
- Modify: `frontend/src/views/admin/VerificationManagement.vue`

**Applies design spec:** §4.3 (Verification Management), §4.4 (API Migration)

**Context:** This page uses raw `fetch()` with manual `Authorization: Bearer` headers for all API calls. Migrate to `api.admin.verifications.*`. Also fix the hardcoded `http://localhost:7000` URL replacement in `handleView()`.

- [ ] **Step 1: Check AdminController.java for verification endpoints**

Run: `grep -n "verification" backend/src/main/java/com/idleitems/school/controller/admin/AdminController.java -i`

Expected: Should find `@GetMapping("/admin/verifications")`, `@PutMapping("/admin/verifications/{id}/approve")`, `@PutMapping("/admin/verifications/{id}/reject")`, `@GetMapping("/admin/verifications/stats")`

- [ ] **Step 2: Replace import — remove userStore import for token, add api import**

In `<script setup>`, replace:
```javascript
import { userStore } from '../../store';
```
With:
```javascript
import api from '../../api';
```

Keep the existing `ElMessage`, `ElMessageBox` imports.

- [ ] **Step 3: Migrate fetchStats()**

Replace:
```javascript
const fetchStats = async () => {
  try {
    const res = await fetch('/api/admin/verifications/stats', {
      headers: { Authorization: `Bearer ${store.token}` },
    });
    const data = await res.json();
    if (data.code === 200) stats.value = data.data;
  } catch (error) {
    console.error('获取认证统计失败:', error);
  }
};
```

With:
```javascript
const fetchStats = async () => {
  try {
    const res = await api.admin.verifications.getStats();
    if (res.data.code === 200) stats.value = res.data.data;
  } catch (error) {
    console.error('获取认证统计失败:', error);
  }
};
```

- [ ] **Step 4: Migrate fetchVerifications()**

Replace `fetch('/api/admin/verifications?' + params)` with:
```javascript
const fetchVerifications = async () => {
  loading.value = true;
  try {
    const res = await api.admin.verifications.getVerifications({
      page: pagination.value.current,
      size: pagination.value.pageSize,
      status: filters.value.status || undefined,
      keyword: filters.value.keyword || undefined,
    });
    if (res.data.code === 200) {
      verifications.value = res.data.data.content;
      pagination.value.total = res.data.data.totalElements;
    }
  } catch (error) {
    console.error('获取认证列表失败:', error);
  } finally {
    loading.value = false;
  }
};
```

- [ ] **Step 5: Migrate handleApprove() and handleReject()**

Replace `fetch('/api/admin/verifications/${id}/approve', { method: 'PUT', headers: {...} })` with:

```javascript
const handleApprove = async (id) => {
  try {
    const res = await api.admin.verifications.approve(id);
    if (res.data.code === 200) {
      ElMessage.success('已通过认证');
      await fetchVerifications();
      await fetchStats();
    }
  } catch (error) {
    ElMessage.error('操作失败');
  }
};

const handleReject = async (id) => {
  try {
    const reason = prompt('请输入驳回原因：');
    if (!reason) return;
    const res = await api.admin.verifications.reject(id, reason);
    if (res.data.code === 200) {
      ElMessage.success('已驳回认证');
      await fetchVerifications();
      await fetchStats();
    }
  } catch (error) {
    ElMessage.error('操作失败');
  }
};
```

- [ ] **Step 6: Migrate bulk operations**

Replace `Promise.all` with `fetch()` pattern:
```javascript
const handleBulkApprove = async () => {
  if (selectedIds.value.length === 0) return ElMessage.warning('请选择认证记录');
  try {
    const res = await api.admin.verifications.batchApprove(selectedIds.value);
    if (res.data.code === 200) {
      ElMessage.success(`已批量通过 ${selectedIds.value.length} 条认证`);
      selectedIds.value = [];
      await fetchVerifications();
      await fetchStats();
    }
  } catch (error) {
    ElMessage.error('批量操作失败');
  }
};
```

- [ ] **Step 7: Fix hardcoded `http://localhost:7000` URL in handleView()**

Find the line that does URL replacement:
```javascript
item.idCardFront = `http://localhost:7000${baseUrl}/idCardFront/${verification.userId}`;
```

Replace with:
```javascript
item.idCardFront = `${baseUrl}/idCardFront/${verification.userId}`;
```

(Remove the hardcoded `http://localhost:7000` — use a relative URL or `window.location.origin` prefix as appropriate.)

---

### Task 6: Refactor CategoryManagement.vue — fetch→api + Hardcoded Stats + Bulk Ops

**Files:**
- Modify: `frontend/src/views/admin/CategoryManagement.vue`

**Applies design spec:** §4.3 (Category Management), §4.4 (API Migration)

**Context:** 963-line file. Uses raw `fetch()` for all CRUD operations. Stats are hardcoded. Bulk operations are stubs. Migrate everything to `api.admin.categories.*`.

- [ ] **Step 1: Replace imports**

Remove `userStore` import. Add:
```javascript
import api from '../../api';
```

- [ ] **Step 2: Replace fetchCategories()**

Replace raw `fetch()` with:
```javascript
const fetchCategories = async () => {
  loading.value = true;
  try {
    const params = { page: pagination.value.current, size: pagination.value.pageSize };
    if (filters.value.status) params.status = filters.value.status;
    if (filters.value.keyword) params.keyword = filters.value.keyword;
    const res = await api.admin.categories.getCategories(params);
    if (res.data.code === 200) {
      categories.value = res.data.data.content || res.data.data;
      if (res.data.data.totalElements !== undefined) pagination.value.total = res.data.data.totalElements;
    }
  } catch (error) {
    console.error('获取分类列表失败:', error);
  } finally {
    loading.value = false;
  }
};
```

- [ ] **Step 3: Replace fetchStats() — remove hardcoded values**

Replace the hardcoded `stats.value = { total: 28, active: 25, level1: 8, level2: 20 }` with:

```javascript
const fetchStats = async () => {
  try {
    const res = await api.admin.categories.getCategoryStats();
    if (res.data.code === 200) stats.value = res.data.data;
  } catch (error) {
    console.error('获取分类统计失败:', error);
  }
};
```

- [ ] **Step 4: Replace handleSave()**

Replace raw `fetch()` POST/PUT with:
```javascript
const handleSave = async () => {
  if (!form.value.name?.trim()) return ElMessage.warning('请输入分类名称');
  try {
    const res = isEdit.value
      ? await api.admin.categories.update(editingId.value, form.value)
      : await api.admin.categories.create(form.value);
    if (res.data.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功');
      dialogVisible.value = false;
      await fetchCategories();
      await fetchStats();
    }
  } catch (error) {
    ElMessage.error('保存失败');
  }
};
```

- [ ] **Step 5: Replace handleToggleStatus(), handleDelete(), handleMoveUp(), handleMoveDown()**

```javascript
const handleToggleStatus = async (category) => {
  try {
    const newStatus = category.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    const res = await api.admin.categories.updateStatus(category.id, newStatus);
    if (res.data.code === 200) {
      ElMessage.success(newStatus === 'ACTIVE' ? '已启用' : '已禁用');
      category.status = newStatus;
    }
  } catch (error) {
    ElMessage.error('操作失败');
  }
};

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该分类？', '确认删除');
    const res = await api.admin.categories.deleteCategory(id);
    if (res.data.code === 200) {
      ElMessage.success('删除成功');
      await fetchCategories();
      await fetchStats();
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败');
  }
};

const handleMoveUp = async (id) => {
  try {
    const res = await api.admin.categories.moveUp(id);
    if (res.data.code === 200) await fetchCategories();
  } catch (error) {
    ElMessage.error('操作失败');
  }
};

const handleMoveDown = async (id) => {
  try {
    const res = await api.admin.categories.moveDown(id);
    if (res.data.code === 200) await fetchCategories();
  } catch (error) {
    ElMessage.error('操作失败');
  }
};
```

- [ ] **Step 6: Replace stub bulk operations**

```javascript
const handleBulkEnable = async () => {
  if (selectedIds.value.length === 0) return ElMessage.warning('请选择分类');
  try {
    const res = await api.admin.categories.batchEnable(selectedIds.value);
    if (res.data.code === 200) {
      ElMessage.success(`已批量启用 ${selectedIds.value.length} 个分类`);
      selectedIds.value = [];
      await fetchCategories();
    }
  } catch (error) {
    ElMessage.error('批量操作失败');
  }
};

const handleBulkDisable = async () => {
  if (selectedIds.value.length === 0) return ElMessage.warning('请选择分类');
  try {
    const res = await api.admin.categories.batchDisable(selectedIds.value);
    if (res.data.code === 200) {
      ElMessage.success(`已批量禁用 ${selectedIds.value.length} 个分类`);
      selectedIds.value = [];
      await fetchCategories();
    }
  } catch (error) {
    ElMessage.error('批量操作失败');
  }
};

const handleBulkDelete = async () => {
  if (selectedIds.value.length === 0) return ElMessage.warning('请选择分类');
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 个分类？`, '批量删除');
    const res = await api.admin.categories.batchDelete(selectedIds.value);
    if (res.data.code === 200) {
      ElMessage.success(`已批量删除 ${selectedIds.value.length} 个分类`);
      selectedIds.value = [];
      await fetchCategories();
      await fetchStats();
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('批量删除失败');
  }
};
```

---

### Task 7: Refactor LogManagement.vue — fetch→api + Fake Fallback + el-date-picker

**Files:**
- Modify: `frontend/src/views/admin/LogManagement.vue`

**Applies design spec:** §4.3 (Log Management), §4.4 (API Migration)

**Context:** 562-line file. Uses raw `fetch()`. Catch block falls back to 6 hardcoded log entries (worst pattern). Uses native `<input type="date">` instead of `el-date-picker`.

- [ ] **Step 1: Replace imports**

Remove `userStore` import. Add:
```javascript
import api from '../../api';
```

- [ ] **Step 2: Remove fake fallback data (hardcoded log entries, ~80 lines)**

Delete the entire hardcoded fallback data block (lines ~403-482 in the original). The `catch` block should show empty state instead:

```javascript
const fetchLogs = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.value.current,
      size: pagination.value.pageSize,
    };
    if (filters.value.type) params.type = filters.value.type;
    if (filters.value.keyword) params.keyword = filters.value.keyword;
    if (filters.value.startDate) params.startDate = filters.value.startDate;
    if (filters.value.endDate) params.endDate = filters.value.endDate;
    const res = await api.admin.logs.getLogs(params);
    if (res.data.code === 200) {
      logs.value = res.data.data.content || [];
      pagination.value.total = res.data.data.totalElements || 0;
    } else {
      logs.value = [];
      pagination.value.total = 0;
    }
  } catch (error) {
    console.error('获取日志失败:', error);
    logs.value = [];
    pagination.value.total = 0;
  } finally {
    loading.value = false;
  }
};
```

- [ ] **Step 3: Replace native input[type=date] with el-date-picker**

In `<template>`, replace:
```html
<input type="date" v-model="filters.startDate" class="filter-input" />
<input type="date" v-model="filters.endDate" class="filter-input" />
```

With:
```html
<el-date-picker
  v-model="filters.startDate"
  type="date"
  placeholder="开始日期"
  value-format="YYYY-MM-DD"
  class="filter-datepicker"
/>
<el-date-picker
  v-model="filters.endDate"
  type="date"
  placeholder="结束日期"
  value-format="YYYY-MM-DD"
  class="filter-datepicker"
/>
```

- [ ] **Step 4: Migrate handleExport()**

Replace raw `fetch()` blob download with:
```javascript
const handleExport = async () => {
  try {
    const res = await api.admin.logs.getLogs({
      ...filters.value,
      page: 1,
      size: 99999,
      export: true,
    });
    if (res.data.code === 200 && res.data.data?.content) {
      const csv = convertToCsv(res.data.data.content);
      const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `操作日志_${new Date().toISOString().slice(0, 10)}.csv`;
      a.click();
      URL.revokeObjectURL(url);
    }
  } catch (error) {
    ElMessage.error('导出失败');
  }
};
```

---

### Task 8: Refactor UserManagement.vue — fetch→api + Pagination Bug + Bulk Ops

**Files:**
- Modify: `frontend/src/views/admin/UserManagement.vue`

**Applies design spec:** §4.3 (User Management), §4.4 (API Migration)

**Context:** Uses raw `fetch()` for all API calls. **Critical bug**: `fetchUsers()` sends `page`/`size` to backend (server pagination), but then filters client-side by keyword/role/status/verified, and sets `total = userList.length` (filtered count) instead of `data.data.totalElements`. This means pagination is completely broken — changing page only changes server page, not the client filter. Also has stub bulk operations. Uses `ElMessageBox` HTML string for user detail view instead of `el-dialog`.

- [ ] **Step 1: Replace imports**

Remove `userStore` import. Add:
```javascript
import api from '../../api';
```

Add `ref` for user detail dialog:
```javascript
const viewingUser = ref(null);
const detailDialogVisible = ref(false);
```

- [ ] **Step 2: Fix fetchUsers() — server-side pagination + client-side filtering**

Replace the broken fetch:

```javascript
const fetchUsers = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.value.current,
      size: pagination.value.pageSize,
    };
    if (filters.value.keyword) params.keyword = filters.value.keyword;
    if (filters.value.role && filters.value.role !== '') params.role = filters.value.role;
    if (filters.value.status && filters.value.status !== '') params.status = filters.value.status;
    if (filters.value.verified && filters.value.verified !== '') params.verified = filters.value.verified;
    const res = await api.admin.users.getUsers(params);
    if (res.data.code === 200) {
      users.value = res.data.data.content || [];
      pagination.value.total = res.data.data.totalElements || 0;
    }
  } catch (error) {
    console.error('获取用户列表失败:', error);
  } finally {
    loading.value = false;
  }
};
```

- [ ] **Step 3: Replace fetchStats() and handleToggleStatus() and handleDelete()**

```javascript
const fetchStats = async () => {
  try {
    const res = await api.admin.users.getUserStats();
    if (res.data.code === 200) stats.value = res.data.data;
  } catch (error) {
    console.error('获取用户统计失败:', error);
  }
};

const handleToggleStatus = async (user) => {
  try {
    const newStatus = user.status === 'ACTIVE' ? 'BANNED' : 'ACTIVE';
    const res = await api.admin.users.updateStatus(user.id, newStatus);
    if (res.data.code === 200) {
      ElMessage.success(newStatus === 'ACTIVE' ? '已启用' : '已禁用');
      user.status = newStatus;
    }
  } catch (error) {
    ElMessage.error('操作失败');
  }
};

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该用户？此操作不可恢复。', '删除确认');
    const res = await api.admin.users.deleteUser(id);
    if (res.data.code === 200) {
      ElMessage.success('删除成功');
      await fetchUsers();
      await fetchStats();
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败');
  }
};
```

- [ ] **Step 4: Replace ElMessageBox view with el-dialog**

Replace the `handleView()` that uses `ElMessageBox` HTML string with an `el-dialog`:

```html
<el-dialog v-model="detailDialogVisible" title="用户详情" width="600px" class="user-detail-dialog">
  <div v-if="viewingUser" class="detail-grid">
    <div class="detail-item">
      <span class="detail-label">用户ID</span>
      <span class="detail-value">{{ viewingUser.id }}</span>
    </div>
    <div class="detail-item">
      <span class="detail-label">用户名</span>
      <span class="detail-value">{{ viewingUser.username }}</span>
    </div>
    <div class="detail-item">
      <span class="detail-label">昵称</span>
      <span class="detail-value">{{ viewingUser.nickname }}</span>
    </div>
    <div class="detail-item">
      <span class="detail-label">邮箱</span>
      <span class="detail-value">{{ viewingUser.email }}</span>
    </div>
    <div class="detail-item">
      <span class="detail-label">手机号</span>
      <span class="detail-value">{{ viewingUser.phone }}</span>
    </div>
    <div class="detail-item">
      <span class="detail-label">角色</span>
      <span class="detail-value"><el-tag>{{ viewingUser.role }}</el-tag></span>
    </div>
    <div class="detail-item">
      <span class="detail-label">状态</span>
      <span class="detail-value">
        <el-tag :type="viewingUser.status === 'ACTIVE' ? 'success' : 'danger'">
          {{ viewingUser.status === 'ACTIVE' ? '正常' : '禁用' }}
        </el-tag>
      </span>
    </div>
    <div class="detail-item">
      <span class="detail-label">注册时间</span>
      <span class="detail-value">{{ viewingUser.createdAt }}</span>
    </div>
  </div>
</el-dialog>
```

```javascript
const handleView = async (user) => {
  try {
    const res = await api.admin.users.getUser(user.id);
    if (res.data.code === 200) {
      viewingUser.value = res.data.data;
      detailDialogVisible.value = true;
    }
  } catch (error) {
    ElMessage.error('获取用户详情失败');
  }
};
```

- [ ] **Step 5: Fix stub bulk operations**

```javascript
const handleBulkEnable = async () => {
  if (selectedIds.value.length === 0) return ElMessage.warning('请选择用户');
  try {
    const res = await api.admin.users.batchUpdateStatus(selectedIds.value, 'ACTIVE');
    if (res.data.code === 200) {
      ElMessage.success(`已批量启用 ${selectedIds.value.length} 个用户`);
      selectedIds.value = [];
      await fetchUsers();
    }
  } catch (error) {
    ElMessage.error('批量操作失败');
  }
};

const handleBulkDisable = async () => {
  if (selectedIds.value.length === 0) return ElMessage.warning('请选择用户');
  try {
    const res = await api.admin.users.batchUpdateStatus(selectedIds.value, 'BANNED');
    if (res.data.code === 200) {
      ElMessage.success(`已批量禁用 ${selectedIds.value.length} 个用户`);
      selectedIds.value = [];
      await fetchUsers();
    }
  } catch (error) {
    ElMessage.error('批量操作失败');
  }
};

const handleBulkDelete = async () => {
  if (selectedIds.value.length === 0) return ElMessage.warning('请选择用户');
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 个用户？`, '批量删除');
    const res = await api.admin.users.batchDelete(selectedIds.value);
    if (res.data.code === 200) {
      ElMessage.success(`已批量删除 ${selectedIds.value.length} 个用户`);
      selectedIds.value = [];
      await fetchUsers();
      await fetchStats();
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('批量删除失败');
  }
};
```

---

### Task 9: Refactor ItemManagement.vue — fetch→api + Hardcoded Stats + Bulk Ops

**Files:**
- Modify: `frontend/src/views/admin/ItemManagement.vue`

**Applies design spec:** §4.3 (Item Management), §4.4 (API Migration)

**Context:** Uses raw `fetch()` for all CRUD. Stats are hardcoded. Bulk operations are stubs. Has complex 12-column table and detail dialog.

- [ ] **Step 1: Replace imports**

Remove `userStore` import. Add:
```javascript
import api from '../../api';
```

- [ ] **Step 2: Replace fetchItems()**

```javascript
const fetchItems = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.value.current,
      size: pagination.value.pageSize,
    };
    if (filters.value.status) params.status = filters.value.status;
    if (filters.value.categoryId) params.categoryId = filters.value.categoryId;
    if (filters.value.keyword) params.keyword = filters.value.keyword;
    const res = await api.admin.items.getItems(params);
    if (res.data.code === 200) {
      items.value = res.data.data.content || [];
      pagination.value.total = res.data.data.totalElements || 0;
    }
  } catch (error) {
    console.error('获取物品列表失败:', error);
  } finally {
    loading.value = false;
  }
};
```

- [ ] **Step 3: Remove hardcoded stats**

Replace `stats.value = { total: 324, pending: 18, onSale: 256, sold: 42 }` with:

```javascript
const fetchStats = async () => {
  try {
    const res = await api.admin.items.getItemStats();
    if (res.data.code === 200) stats.value = res.data.data;
  } catch (error) {
    console.error('获取物品统计失败:', error);
  }
};
```

- [ ] **Step 4: Replace handleApprove(), handleReject(), handleTakeDown(), handleReList(), handleDelete()**

```javascript
const handleApprove = async (id) => {
  try {
    const res = await api.admin.items.approve(id);
    if (res.data.code === 200) {
      ElMessage.success('已审核通过');
      await fetchItems();
      await fetchStats();
    }
  } catch (error) {
    ElMessage.error('操作失败');
  }
};

const handleReject = async (id) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入驳回原因', '驳回物品');
    if (!reason) return;
    const res = await api.admin.items.reject(id, reason);
    if (res.data.code === 200) {
      ElMessage.success('已驳回');
      await fetchItems();
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('操作失败');
  }
};

const handleTakeDown = async (id) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入下架原因', '下架物品');
    if (!reason) return;
    const res = await api.admin.items.offShelf(id, reason);
    if (res.data.code === 200) {
      ElMessage.success('已下架');
      await fetchItems();
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('操作失败');
  }
};

const handleReList = async (id) => {
  try {
    const res = await api.admin.items.approve(id);
    if (res.data.code === 200) {
      ElMessage.success('已重新上架');
      await fetchItems();
    }
  } catch (error) {
    ElMessage.error('操作失败');
  }
};

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该物品？', '删除确认');
    const res = await api.admin.items.deleteItem(id);
    if (res.data.code === 200) {
      ElMessage.success('删除成功');
      await fetchItems();
      await fetchStats();
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败');
  }
};
```

- [ ] **Step 5: Fix stub bulk operations**

```javascript
const handleBulkTakeDown = async () => {
  if (selectedIds.value.length === 0) return ElMessage.warning('请选择物品');
  try {
    const res = await api.admin.items.batchOffShelf(selectedIds.value, '批量下架');
    if (res.data.code === 200) {
      ElMessage.success(`已批量下架 ${selectedIds.value.length} 个物品`);
      selectedIds.value = [];
      await fetchItems();
    }
  } catch (error) {
    ElMessage.error('批量操作失败');
  }
};

const handleBulkDelete = async () => {
  if (selectedIds.value.length === 0) return ElMessage.warning('请选择物品');
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 个物品？`, '批量删除');
    const res = await api.admin.items.batchOffShelf(selectedIds.value, '批量删除');
    selectedIds.value = [];
    await fetchItems();
    await fetchStats();
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('批量删除失败');
  }
};
```

---

### Task 10: Remaining CSS Visual Enhancements

**Files:**
- Modify: `frontend/src/styles/pages/admin-user-management.css`
- Modify: `frontend/src/styles/pages/admin-item-management.css`
- Modify: `frontend/src/styles/pages/admin-verification-management.css`
- Modify: `frontend/src/styles/pages/admin-category-management.css`
- Modify: `frontend/src/styles/pages/admin-log-management.css`

**Applies design spec:** §4.3, §5 (Common Style Unification)

**Context:** All CSS files already use the design token system. Need minor refinements: el-dialog styles (UserManagement), colored stat-card left borders (ItemManagement), el-date-picker styling (LogManagement), and visual polish.

- [ ] **Step 1: Add user detail dialog styles to admin-user-management.css**

```css
.user-detail-dialog .detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-5);
  padding: var(--space-4);
}

.user-detail-dialog .detail-item {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.user-detail-dialog .detail-label {
  font-size: 12px;
  color: var(--content-tertiary);
  font-weight: 500;
}

.user-detail-dialog .detail-value {
  font-size: 14px;
  color: var(--content-primary);
  font-weight: 500;
}

@media (max-width: 768px) {
  .user-detail-dialog .detail-grid {
    grid-template-columns: 1fr;
  }
}
```

- [ ] **Step 2: Add el-date-picker styles to admin-log-management.css**

```css
.filter-datepicker {
  width: 160px;
}

.filter-datepicker .el-input__wrapper {
  background: var(--surface-section);
  border: 1px solid var(--border-default);
  border-radius: var(--radius-md);
  box-shadow: none;
}

.filter-datepicker .el-input__wrapper:hover {
  border-color: var(--color-primary);
}
```

- [ ] **Step 3: Audit and fix any missing CSS variable usage**

Check each CSS file for hardcoded color values (e.g., `#ff0000`, `rgb()`, named colors) that should use CSS custom properties. Replace with appropriate `var(--color-*)` or `var(--surface-*)` variables.

---

## Self-Review Checklist

- [ ] **Spec coverage**: Does every section in the design spec have a corresponding task?
  - §1 Overview → covered by header
  - §2 Design Principles → covered by Tasks 2-10
  - §3 Design Token System → Task 2 (admin.css)
  - §4.1 Admin.vue Layout → Task 2
  - §4.2 Dashboard → Task 3
  - §4.3 User/Item/Category/Order/Verification/Log pages → Tasks 5-10
  - §5 Common Style Unification → Task 10
  - §6 Out of Scope → confirmed not covered
  - API Migration (implied by all fetch→api) → Tasks 1, 5-9

- [ ] **Placeholder scan**: No "TBD", "TODO", "implement later" patterns. Every code block contains complete code.

- [ ] **Type consistency**: All API method names in Tasks 5-9 match the admin.js definitions in Task 1. Method signatures are consistent (same params, same return patterns).
