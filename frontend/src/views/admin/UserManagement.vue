<template>
  <div class="user-management">
    <div class="page-intro">
      <h2 class="section-title">用户概览</h2>
      <p class="section-desc">管理平台用户账户，审核认证状态，设置权限</p>
    </div>

    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon stat-icon-users">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.5"
          >
            <circle cx="12" cy="8" r="4" />
            <path d="M4 21v-2a4 4 0 014-4h8a4 4 0 014 4v2" />
          </svg>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.total }}</span>
          <span class="stat-label">总用户数</span>
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
          <span class="stat-label">活跃用户</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-verified">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.5"
          >
            <path d="M9 12l2 2 4-4" />
            <circle cx="12" cy="12" r="9" />
          </svg>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.verified }}</span>
          <span class="stat-label">已认证</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-new">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.5"
          >
            <path d="M12 5v14M5 12h14" />
          </svg>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.newThisWeek }}</span>
          <span class="stat-label">本周新增</span>
        </div>
      </div>
    </div>

    <div class="content-card">
      <div class="card-header">
        <div class="header-left">
          <h3 class="card-title">用户列表</h3>
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
          <button class="btn btn-primary" @click="handleAdd">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
            >
              <path d="M12 5v14M5 12h14" />
            </svg>
            添加用户
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
            placeholder="搜索用户名、昵称、邮箱或手机号..."
            class="search-input"
            @keyup.enter="handleSearch"
          />
        </div>
        <div class="filter-selects">
          <select
            v-model="userRole"
            class="filter-select"
            @change="handleSearch"
          >
            <option value="">全部角色</option>
            <option value="STUDENT">学生</option>
            <option value="ADMIN">管理员</option>
          </select>
          <select
            v-model="userStatus"
            class="filter-select"
            @change="handleSearch"
          >
            <option value="">全部状态</option>
            <option value="ACTIVE">活跃</option>
            <option value="DISABLED">禁用</option>
          </select>
          <select
            v-model="userVerified"
            class="filter-select"
            @change="handleSearch"
          >
            <option value="">认证状态</option>
            <option value="true">已认证</option>
            <option value="false">未认证</option>
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
              <th class="col-user">用户</th>
              <th class="col-school">学校/学号</th>
              <th class="col-role">角色</th>
              <th class="col-status">状态</th>
              <th class="col-verified">认证</th>
              <th class="col-credit">信用</th>
              <th class="col-stats">交易统计</th>
              <th class="col-login">最后登录</th>
              <th class="col-actions">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in users" :key="user.id" class="table-row">
              <td class="col-checkbox">
                <input
                  type="checkbox"
                  v-model="selectedUsers"
                  :value="user.id"
                />
              </td>
              <td class="col-user">
                <div class="user-cell">
                  <el-avatar :size="36" :src="user.avatar">
                    {{ getAvatarText(user) }}
                  </el-avatar>
                  <div class="user-info">
                    <span class="user-name">{{
                      user.nickname || user.username
                    }}</span>
                    <span class="user-email">{{
                      user.email || '未设置邮箱'
                    }}</span>
                  </div>
                </div>
              </td>
              <td class="col-school">
                <div class="school-cell">
                  <span class="school-name">{{ user.schoolName || '-' }}</span>
                  <span class="student-id" v-if="user.studentId"
                    >学号: {{ user.studentId }}</span
                  >
                </div>
              </td>
              <td class="col-role">
                <span
                  class="badge"
                  :class="
                    user.role === 'ADMIN' ? 'badge-admin' : 'badge-student'
                  "
                >
                  {{ user.role === 'ADMIN' ? '管理员' : '学生' }}
                </span>
              </td>
              <td class="col-status">
                <span
                  class="badge"
                  :class="
                    user.status === 'ACTIVE' ? 'badge-success' : 'badge-danger'
                  "
                >
                  {{ user.status === 'ACTIVE' ? '活跃' : '禁用' }}
                </span>
              </td>
              <td class="col-verified">
                <span
                  class="badge"
                  :class="user.verified ? 'badge-success' : 'badge-warning'"
                >
                  {{ user.verified ? '已认证' : '未认证' }}
                </span>
              </td>
              <td class="col-credit">
                <el-progress
                  :percentage="user.creditScore || 100"
                  :color="getScoreColor(user.creditScore)"
                  :show-text="false"
                  class="credit-progress"
                />
                <span class="credit-value">{{ user.creditScore || 100 }}</span>
              </td>
              <td class="col-stats">
                <div class="stats-mini">
                  <span class="mini-stat" title="售出">{{
                    user.totalSales || 0
                  }}</span>
                  <span class="mini-sep">/</span>
                  <span class="mini-stat" title="购买">{{
                    user.totalPurchases || 0
                  }}</span>
                </div>
              </td>
              <td class="col-login">
                <div class="login-info">
                  <span class="login-time">{{
                    formatDate(user.lastLoginTime)
                  }}</span>
                  <span class="login-ip" v-if="user.lastLoginIp">{{
                    user.lastLoginIp
                  }}</span>
                </div>
              </td>
              <td class="col-actions">
                <div class="action-group">
                  <button
                    class="action-btn"
                    @click="handleView(user)"
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
                    @click="handleEdit(user)"
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
                    class="action-btn"
                    :class="
                      user.status === 'ACTIVE'
                        ? 'action-danger'
                        : 'action-success'
                    "
                    @click="handleToggleStatus(user)"
                    :title="user.status === 'ACTIVE' ? '禁用' : '启用'"
                  >
                    <svg
                      v-if="user.status === 'ACTIVE'"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="1.5"
                    >
                      <circle cx="12" cy="12" r="10" />
                      <path d="M4.93 4.93l14.14 14.14" />
                    </svg>
                    <svg
                      v-else
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
                    class="action-btn action-danger"
                    @click="handleDelete(user)"
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

      <div class="table-footer" v-if="selectedUsers.length > 0">
        <div class="selection-info">
          已选择 <strong>{{ selectedUsers.length }}</strong> 项
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
                fetchUsers();
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
                fetchUsers();
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
      v-model="dialogVisible"
      title="用户详情"
      width="520px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <div class="user-detail" v-if="currentUser">
        <div class="detail-avatar">
          <el-avatar :size="64" :src="currentUser.avatar">
            {{ getAvatarText(currentUser) }}
          </el-avatar>
          <div class="detail-name">
            {{ currentUser.nickname || currentUser.username }}
          </div>
          <div class="detail-email">
            {{ currentUser.email || '未设置邮箱' }}
          </div>
        </div>
        <div class="detail-grid">
          <div class="detail-item">
            <span class="detail-label">用户名</span>
            <span class="detail-value">{{ currentUser.username || '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">手机号</span>
            <span class="detail-value">{{ currentUser.phone || '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">学校</span>
            <span class="detail-value">{{
              currentUser.schoolName || '-'
            }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">学号</span>
            <span class="detail-value">{{ currentUser.studentId || '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">性别</span>
            <span class="detail-value">{{
              currentUser.gender === 1
                ? '男'
                : currentUser.gender === 2
                  ? '女'
                  : '未知'
            }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">信用评分</span>
            <span class="detail-value">
              <el-progress
                :percentage="currentUser.creditScore || 100"
                :color="getScoreColor(currentUser.creditScore)"
                :width="36"
                type="circle"
                :stroke-width="4"
              />
              <span style="margin-left: 6px; font-weight: 600">{{
                currentUser.creditScore || 100
              }}</span>
            </span>
          </div>
          <div class="detail-item detail-full">
            <span class="detail-label">交易统计</span>
            <span class="detail-value"
              >累计交易 {{ currentUser.totalTransactions || 0 }} 次 · 售出
              {{ currentUser.totalSales || 0 }} 件 · 购买
              {{ currentUser.totalPurchases || 0 }} 件</span
            >
          </div>
          <div class="detail-item">
            <span class="detail-label">角色</span>
            <span class="detail-value">
              <span
                class="badge"
                :class="
                  currentUser.role === 'ADMIN' ? 'badge-admin' : 'badge-student'
                "
              >
                {{ currentUser.role === 'ADMIN' ? '管理员' : '学生' }}
              </span>
            </span>
          </div>
          <div class="detail-item">
            <span class="detail-label">状态</span>
            <span class="detail-value">
              <span
                class="badge"
                :class="
                  currentUser.status === 'ACTIVE'
                    ? 'badge-success'
                    : 'badge-danger'
                "
              >
                {{ currentUser.status === 'ACTIVE' ? '活跃' : '禁用' }}
              </span>
            </span>
          </div>
          <div class="detail-item">
            <span class="detail-label">认证</span>
            <span class="detail-value">
              <span
                class="badge"
                :class="
                  currentUser.verified ? 'badge-success' : 'badge-warning'
                "
              >
                {{ currentUser.verified ? '已认证' : '未认证' }}
              </span>
            </span>
          </div>
          <div class="detail-item">
            <span class="detail-label">注册时间</span>
            <span class="detail-value">{{
              formatDateTime(currentUser.createdAt)
            }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">最后登录</span>
            <span class="detail-value">{{
              formatDateTime(currentUser.lastLoginTime)
            }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 编辑用户对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑用户"
      width="520px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" disabled />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editForm.role" placeholder="请选择角色">
            <el-option label="学生" value="STUDENT" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editForm.status" placeholder="请选择状态">
            <el-option label="活跃" value="ACTIVE" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="学号">
          <el-input v-model="editForm.studentId" placeholder="请输入学号" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="editForm.gender" placeholder="请选择性别" clearable>
            <el-option label="男" :value="1" />
            <el-option label="女" :value="2" />
            <el-option label="未知" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="学校">
          <el-input v-model="editForm.schoolName" placeholder="请输入学校" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="editForm.bio" type="textarea" :rows="3" placeholder="请输入简介" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="editLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 添加用户对话框 -->
    <el-dialog
      v-model="addDialogVisible"
      title="添加用户"
      width="520px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form :model="addForm" label-width="80px">
        <el-form-item label="用户名" required>
          <el-input v-model="addForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱" required>
          <el-input v-model="addForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="密码" required>
          <el-input v-model="addForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="addForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="addForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="addForm.role" placeholder="请选择角色">
            <el-option label="学生" value="STUDENT" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="addForm.status" placeholder="请选择状态">
            <el-option label="活跃" value="ACTIVE" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="学号">
          <el-input v-model="addForm.studentId" placeholder="请输入学号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAdd" :loading="addLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import api from '../../api';

const searchKeyword = ref('');
const userRole = ref('');
const userStatus = ref('');
const userVerified = ref('');
const users = ref([]);
const selectedUsers = ref([]);
const page = ref(1);
const pageSize = ref(20);
const total = ref(0);
const dialogVisible = ref(false);
const currentUser = ref(null);

// 编辑用户相关
const editDialogVisible = ref(false);
const editForm = ref({});
const editLoading = ref(false);

// 添加用户相关
const addDialogVisible = ref(false);
const addForm = ref({});
const addLoading = ref(false);

const stats = ref({
  total: 0,
  active: 0,
  verified: 0,
  newThisWeek: 0,
});

const totalPages = computed(() => Math.ceil(total.value / pageSize.value) || 1);
const isAllSelected = computed(
  () =>
    users.value.length > 0 && selectedUsers.value.length === users.value.length
);

const getAvatarText = (user) => {
  if (user.nickname && user.nickname.length > 0) return user.nickname.charAt(0);
  if (user.username && user.username.length > 0) return user.username.charAt(0);
  return '用';
};

const getScoreColor = (score) => {
  if (score >= 80) return '#67C23A';
  if (score >= 60) return '#E6A23C';
  return '#F56C6C';
};

const formatDate = (dateString) => {
  if (!dateString) return '-';
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
};

const formatDateTime = (dateString) => {
  if (!dateString) return '-';
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
};

const fetchUsers = async () => {
  try {
    const params = {};
    params.page = page.value;
    params.size = pageSize.value;
    if (searchKeyword.value) params.keyword = searchKeyword.value;
    if (userRole.value) params.role = userRole.value;
    if (userStatus.value) params.status = userStatus.value;
    if (userVerified.value !== '') params.verified = userVerified.value;

    const response = await api.admin.users.getUsers(params);
    if (response.code === 200) {
      let userList = response.data.content || [];

      if (searchKeyword.value) {
        const keyword = searchKeyword.value.toLowerCase();
        userList = userList.filter(
          (user) =>
            (user.username && user.username.toLowerCase().includes(keyword)) ||
            (user.nickname && user.nickname.toLowerCase().includes(keyword)) ||
            (user.email && user.email.toLowerCase().includes(keyword))
        );
      }

      if (userVerified.value !== '') {
        const verifiedValue = userVerified.value === 'true';
        userList = userList.filter((user) => user.verified === verifiedValue);
      }

      users.value = userList;
      total.value = response.data.totalElements;
    } else {
      ElMessage.error(response.message || '获取用户列表失败');
    }
  } catch (error) {
    ElMessage.error('网络错误，请稍后重试');
    users.value = [];
    total.value = 0;
  }
};

const fetchStats = async () => {
  try {
    const response = await api.admin.users.getUserStats();
    if (response.code === 200) {
      stats.value = response.data;
    }
  } catch (error) {
    stats.value = { total: 0, active: 0, verified: 0, newThisWeek: 0 };
  }
};

const handleSearch = () => {
  page.value = 1;
  fetchUsers();
};

const handleReset = () => {
  searchKeyword.value = '';
  userRole.value = '';
  userStatus.value = '';
  userVerified.value = '';
  page.value = 1;
  fetchUsers();
};

const handleSelectAll = (e) => {
  if (e.target.checked) {
    selectedUsers.value = users.value.map((u) => u.id);
  } else {
    selectedUsers.value = [];
  }
};

const handleSizeChange = () => {
  page.value = 1;
  fetchUsers();
};

const handleView = (user) => {
  currentUser.value = user;
  dialogVisible.value = true;
};

const handleEdit = (user) => {
  editForm.value = {
    id: user.id,
    username: user.username,
    email: user.email || '',
    phone: user.phone || '',
    nickname: user.nickname || '',
    role: user.role,
    status: user.status,
    studentId: user.studentId || '',
    gender: user.gender,
    bio: user.bio || '',
    schoolName: user.schoolName || '',
  };
  editDialogVisible.value = true;
};

const handleToggleStatus = async (user) => {
  const action = user.status === 'ACTIVE' ? '禁用' : '启用';
  try {
    await ElMessageBox.confirm(
      `确定要${action}用户 ${user.username} 吗？`,
      `确认${action}`,
      {
        type: action === '启用' ? 'success' : 'warning',
      }
    );
    const newStatus = user.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE';
    const response = await api.admin.users.updateStatus(user.id, newStatus);
    if (response.code === 200) {
      user.status = newStatus;
      ElMessage.success(`用户已${action}`);
    } else {
      ElMessage.error(response.message || `${action}失败`);
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失败`);
    }
  }
};

const handleDelete = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 ${user.username} 吗？此操作不可恢复。`,
      '危险操作',
      {
        type: 'error',
      }
    );
    const response = await api.admin.users.deleteUser(user.id);
    if (response.code === 200) {
      users.value = users.value.filter((u) => u.id !== user.id);
      total.value--;
      ElMessage.success('用户已删除');
    } else {
      ElMessage.error(response.message || '删除失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

const handleBulkEnable = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要启用 ${selectedUsers.value.length} 个用户吗？`,
      '确认批量启用',
      {
        type: 'success',
      }
    );
    const response = await api.admin.users.batchUpdateStatus(
      selectedUsers.value,
      'ACTIVE'
    );
    if (response.code === 200) {
      ElMessage.success(`已启用 ${selectedUsers.value.length} 个用户`);
      selectedUsers.value = [];
      fetchUsers();
    } else {
      ElMessage.error(response.message || '批量启用失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量启用失败');
    }
  }
};

const handleBulkDisable = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要禁用 ${selectedUsers.value.length} 个用户吗？`,
      '确认批量禁用',
      {
        type: 'warning',
      }
    );
    const response = await api.admin.users.batchUpdateStatus(
      selectedUsers.value,
      'DISABLED'
    );
    if (response.code === 200) {
      ElMessage.success(`已禁用 ${selectedUsers.value.length} 个用户`);
      selectedUsers.value = [];
      fetchUsers();
    } else {
      ElMessage.error(response.message || '批量禁用失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量禁用失败');
    }
  }
};

const handleBulkDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除 ${selectedUsers.value.length} 个用户吗？此操作不可恢复。`,
      '危险操作',
      {
        type: 'error',
      }
    );
    const response = await api.admin.users.batchDelete(selectedUsers.value);
    if (response.code === 200) {
      ElMessage.success(`已删除 ${selectedUsers.value.length} 个用户`);
      selectedUsers.value = [];
      fetchUsers();
    } else {
      ElMessage.error(response.message || '批量删除失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败');
    }
  }
};

const handleExport = async () => {
  try {
    const params = {};
    if (searchKeyword.value) params.keyword = searchKeyword.value;
    if (userRole.value) params.role = userRole.value;
    if (userStatus.value) params.status = userStatus.value;

    const response = await api.admin.users.exportUsers(params);
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `users_${new Date().getTime()}.csv`);
    document.body.appendChild(link);
    link.click();
    link.remove();
    ElMessage.success('导出成功');
  } catch (error) {
    ElMessage.error('导出失败');
  }
};

const handleAdd = () => {
  addForm.value = {
    username: '',
    email: '',
    password: '',
    phone: '',
    nickname: '',
    role: 'STUDENT',
    status: 'ACTIVE',
    studentId: '',
  };
  addDialogVisible.value = true;
};

const submitEdit = async () => {
  if (!editForm.value.email) {
    ElMessage.warning('请输入邮箱');
    return;
  }
  editLoading.value = true;
  try {
    const response = await api.admin.users.updateUser(editForm.value.id, {
      email: editForm.value.email,
      phone: editForm.value.phone,
      nickname: editForm.value.nickname,
      role: editForm.value.role,
      status: editForm.value.status,
      studentId: editForm.value.studentId,
      gender: editForm.value.gender,
      bio: editForm.value.bio,
      schoolName: editForm.value.schoolName,
    });
    if (response.code === 200) {
      ElMessage.success('更新成功');
      editDialogVisible.value = false;
      fetchUsers();
    } else {
      ElMessage.error(response.message || '更新失败');
    }
  } catch (error) {
    ElMessage.error('更新失败');
  } finally {
    editLoading.value = false;
  }
};

const submitAdd = async () => {
  if (!addForm.value.username) {
    ElMessage.warning('请输入用户名');
    return;
  }
  if (!addForm.value.email) {
    ElMessage.warning('请输入邮箱');
    return;
  }
  if (!addForm.value.password) {
    ElMessage.warning('请输入密码');
    return;
  }
  if (addForm.value.password.length < 6) {
    ElMessage.warning('密码长度不能少于6位');
    return;
  }
  addLoading.value = true;
  try {
    const response = await api.admin.users.createUser(addForm.value);
    if (response.code === 200) {
      ElMessage.success('创建成功');
      addDialogVisible.value = false;
      fetchUsers();
      fetchStats();
    } else {
      ElMessage.error(response.message || '创建失败');
    }
  } catch (error) {
    ElMessage.error('创建失败');
  } finally {
    addLoading.value = false;
  }
};

onMounted(() => {
  fetchUsers();
  fetchStats();
});
</script>

<style scoped src="../../styles/pages/admin-user-management.css"></style>
