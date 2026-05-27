<template>
  <div class="user-management">
    <div class="page-intro">
      <h2 class="section-title">用户概览</h2>
      <p class="section-desc">管理平台用户账户，审核认证状态，设置权限</p>
    </div>

    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon stat-icon-users">
          <User :size="24" />
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.total }}</span>
          <span class="stat-label">总用户数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-active">
          <Clock :size="24" />
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.active }}</span>
          <span class="stat-label">活跃用户</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-verified">
          <CheckCircle :size="24" />
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.verified }}</span>
          <span class="stat-label">已认证</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon stat-icon-new">
          <Plus :size="24" />
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
            <Download :size="16" />
            导出
          </button>
          <button class="btn btn-primary" @click="handleAdd">
            <Plus :size="16" />
            添加用户
          </button>
        </div>
      </div>

      <div class="filters-bar">
        <div class="filter-search">
          <Search :size="16" class="search-icon" />
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索用户名、昵称、邮箱或手机号..."
            class="search-input"
            @keyup.enter="handleSearch"
          />
        </div>
        <div class="filter-selects">
          <el-select v-model="userRole" placeholder="全部角色" @change="handleSearch" class="filter-select">
            <el-option label="全部角色" value="" />
            <el-option label="学生" value="STUDENT" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
          <el-select v-model="userStatus" placeholder="全部状态" @change="handleSearch" class="filter-select">
            <el-option label="全部状态" value="" />
            <el-option label="活跃" value="ACTIVE" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
          <el-select v-model="userVerified" placeholder="认证状态" @change="handleSearch" class="filter-select">
            <el-option label="认证状态" value="" />
            <el-option label="已认证" value="true" />
            <el-option label="未认证" value="false" />
          </el-select>
          <button class="btn btn-ghost btn-sm" @click="handleReset">
            重置
          </button>
        </div>
      </div>

      <el-table
        :data="users"
        row-key="id"
        stripe
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column label="用户" min-width="200">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="36" :src="row.avatar">
                {{ getAvatarText(row) }}
              </el-avatar>
              <div class="user-info">
                <span class="user-name">{{ row.nickname || row.username }}</span>
                <span class="user-email">{{ row.email || '未设置邮箱' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="学校/学号" min-width="160">
          <template #default="{ row }">
            <div class="school-cell">
              <span class="school-name">{{ row.schoolName || '-' }}</span>
              <span class="student-id" v-if="row.studentId">学号: {{ row.studentId }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="角色" width="90">
          <template #default="{ row }">
            <span class="badge" :class="row.role === 'ADMIN' ? 'badge-admin' : 'badge-student'">
              {{ row.role === 'ADMIN' ? '管理员' : '学生' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <span class="badge" :class="row.status === 'ACTIVE' ? 'badge-success' : 'badge-danger'">
              {{ row.status === 'ACTIVE' ? '活跃' : '禁用' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="认证" width="80">
          <template #default="{ row }">
            <span class="badge" :class="row.verified ? 'badge-success' : 'badge-warning'">
              {{ row.verified ? '已认证' : '未认证' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="信用" width="120">
          <template #default="{ row }">
            <el-progress
              :percentage="row.creditScore || 100"
              :color="getScoreColor(row.creditScore)"
              :show-text="false"
              class="credit-progress"
            />
            <span class="credit-value">{{ row.creditScore || 100 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="交易统计" width="100">
          <template #default="{ row }">
            <div class="stats-mini">
              <span class="mini-stat" title="售出">{{ row.totalSales || 0 }}</span>
              <span class="mini-sep">/</span>
              <span class="mini-stat" title="购买">{{ row.totalPurchases || 0 }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="最后登录" width="130">
          <template #default="{ row }">
            <div class="login-info">
              <span class="login-time">{{ formatDate(row.lastLoginTime) }}</span>
              <span class="login-ip" v-if="row.lastLoginIp">{{ row.lastLoginIp }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <div class="action-group">
              <button class="action-btn" @click="handleView(row)" title="查看详情" aria-label="查看详情">
                <Eye :size="16" />
              </button>
              <button class="action-btn" @click="handleEdit(row)" title="编辑" aria-label="编辑">
                <Edit3 :size="16" />
              </button>
              <button
                class="action-btn"
                :class="row.status === 'ACTIVE' ? 'action-danger' : 'action-success'"
                @click="handleToggleStatus(row)"
                :title="row.status === 'ACTIVE' ? '禁用' : '启用'"
                :aria-label="row.status === 'ACTIVE' ? '禁用' : '启用'"
              >
                <Ban v-if="row.status === 'ACTIVE'" :size="16" />
                <CheckCircle v-else :size="16" />
              </button>
              <button class="action-btn action-danger" @click="handleDelete(row)" title="删除" aria-label="删除">
                <Trash2 :size="16" />
              </button>
            </div>
          </template>
        </el-table-column>
      </el-table>

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
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
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

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import api from '../../api';
import { User, Clock, CheckCircle, Plus, Download, Search, Eye, Edit3, Ban, Trash2 } from 'lucide-vue-next';

const searchKeyword = ref('');
const userRole = ref('');
const userStatus = ref('');
const userVerified = ref('');
const users = ref<any[]>([]);
const selectedUsers = ref<any[]>([]);
const page = ref(1);
const pageSize = ref(20);
const total = ref(0);
const dialogVisible = ref(false);
const currentUser = ref<any>(null);

// 编辑用户相关
const editDialogVisible = ref(false);
const editForm = ref<{ id?: number; username?: string; email?: string; password?: string; phone?: string; nickname?: string; role?: string; status?: string; studentId?: string; gender?: number; schoolName?: string; bio?: string }>({});
const editLoading = ref(false);

// 添加用户相关
const addDialogVisible = ref(false);
const addForm = ref<{ username?: string; email?: string; password?: string; phone?: string; nickname?: string; role?: string; status?: string; studentId?: string }>({});
const addLoading = ref(false);

const stats = ref({
  total: 0,
  active: 0,
  verified: 0,
  newThisWeek: 0,
});


const getAvatarText = (user: any) => {
  if (user.nickname && user.nickname.length > 0) return user.nickname.charAt(0);
  if (user.username && user.username.length > 0) return user.username.charAt(0);
  return '用';
};

const getScoreColor = (score: number) => {
  if (score >= 80) return 'var(--color-success)';
  if (score >= 60) return 'var(--color-warning)';
  return 'var(--color-danger)';
};

const formatDate = (dateString: string) => {
  if (!dateString) return '-';
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
};

const formatDateTime = (dateString: string) => {
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
    const params: Record<string, any> = {};
    params.page = page.value;
    params.size = pageSize.value;
    if (searchKeyword.value) params.keyword = searchKeyword.value;
    if (userRole.value) params.role = userRole.value;
    if (userStatus.value) params.status = userStatus.value;
    if (userVerified.value !== '') params.verified = userVerified.value;

    const response = await api.admin.users.getUsers(params);
    if (response.code === 200) {
      let userList: any[] = response.data.content || [];

      if (searchKeyword.value) {
        const keyword = searchKeyword.value.toLowerCase();
        userList = userList.filter(
          (user: any) =>
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

const handleSelectionChange = (selection: any[]) => {
  selectedUsers.value = selection.map((u) => u.id);
};

const handleSortChange = () => {
  // 排序逻辑可在此扩展
};

const handleCurrentChange = (val: number) => {
  page.value = val;
  fetchUsers();
};

const handleSizeChange = () => {
  page.value = 1;
  fetchUsers();
};

const handleView = (user: any) => {
  currentUser.value = user;
  dialogVisible.value = true;
};

const handleEdit = (user: any) => {
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

const handleToggleStatus = async (user: any) => {
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

const handleDelete = async (user: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 ${user.username} 吗？此操作不可恢复。`,
      '危险操作',
      {
        type: 'error',
      }
    );
    const response = await api.admin.users.deleteUsers([user.id]);
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
    const params: Record<string, any> = {};
    if (searchKeyword.value) params.keyword = searchKeyword.value;
    if (userRole.value) params.role = userRole.value;
    if (userStatus.value) params.status = userStatus.value;

    const blob = await api.admin.users.exportUsers(params);
    const url = window.URL.createObjectURL(blob);
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
    const response = await api.admin.users.updateUser(editForm.value.id!, {
      email: editForm.value.email,
      phone: editForm.value.phone,
      nickname: editForm.value.nickname,
      role: editForm.value.role,
      status: editForm.value.status,
      studentId: editForm.value.studentId,
      gender: editForm.value.gender,
      bio: editForm.value.bio,
      schoolName: editForm.value.schoolName,
    } as any);
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
    const response = await api.admin.users.createUser(addForm.value as any);
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
