<template>
  <div class="dashboard">
    <div class="page-intro">
      <h2 class="section-title">控制台</h2>
      <p class="section-desc">平台运营数据概览与快速操作入口</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card card-total">
        <div class="stat-header">
          <span class="stat-label">总用户数</span>
          <div class="stat-icon">
            <Users :size="20" stroke-width="1.5" />
          </div>
        </div>
        <div class="stat-value">{{ formatNumber(stats.totalUsers) }}</div>
        <div class="stat-change positive">
          <span>+{{ stats.newUsersToday }}</span> 今日新增
        </div>
      </div>

      <div class="stat-card card-secondary">
        <div class="stat-header">
          <span class="stat-label">总物品数</span>
          <div class="stat-icon">
            <Package :size="20" stroke-width="1.5" />
          </div>
        </div>
        <div class="stat-value">{{ formatNumber(stats.totalItems) }}</div>
        <div class="stat-change positive">
          <span>+{{ stats.newItemsToday }}</span> 今日发布
        </div>
      </div>

      <div class="stat-card card-success">
        <div class="stat-header">
          <span class="stat-label">总订单数</span>
          <div class="stat-icon">
            <ClipboardList :size="20" stroke-width="1.5" />
          </div>
        </div>
        <div class="stat-value">{{ formatNumber(stats.totalOrders) }}</div>
        <div class="stat-change positive">
          <span>+{{ stats.newOrdersToday }}</span> 今日新增
        </div>
      </div>

      <div class="stat-card card-info">
        <div class="stat-header">
          <span class="stat-label">总交易额</span>
          <div class="stat-icon">
            <DollarSign :size="20" stroke-width="1.5" />
          </div>
        </div>
        <div class="stat-value">¥{{ formatNumber(stats.totalAmount) }}</div>
        <div class="stat-change positive">
          <span>+¥{{ formatNumber(stats.amountToday) }}</span> 今日交易
        </div>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <h3 class="section-subtitle">快捷操作</h3>
      <div class="actions-grid">
        <router-link to="/admin/users" class="action-card">
          <div class="action-icon">
            <Users :size="20" stroke-width="1.5" />
          </div>
          <div class="action-content">
            <h4 class="action-title">用户管理</h4>
            <p class="action-desc">管理平台用户账号与权限</p>
          </div>
          <ChevronRight class="action-arrow" :size="20" stroke-width="1.5" />
        </router-link>

        <router-link to="/admin/verification" class="action-card">
          <div class="action-icon">
            <CheckCircle :size="20" stroke-width="1.5" />
          </div>
          <div class="action-content">
            <h4 class="action-title">实名认证</h4>
            <p class="action-desc">审核用户实名认证申请</p>
          </div>
          <ChevronRight class="action-arrow" :size="20" stroke-width="1.5" />
        </router-link>

        <router-link to="/admin/items" class="action-card">
          <div class="action-icon">
            <Package :size="20" stroke-width="1.5" />
          </div>
          <div class="action-content">
            <h4 class="action-title">物品管理</h4>
            <p class="action-desc">审核与管理平台物品信息</p>
          </div>
          <ChevronRight class="action-arrow" :size="20" stroke-width="1.5" />
        </router-link>

        <router-link to="/admin/categories" class="action-card">
          <div class="action-icon">
            <Menu :size="20" stroke-width="1.5" />
          </div>
          <div class="action-content">
            <h4 class="action-title">分类管理</h4>
            <p class="action-desc">管理物品分类与标签</p>
          </div>
          <ChevronRight class="action-arrow" :size="20" stroke-width="1.5" />
        </router-link>

        <router-link to="/admin/category-feedbacks" class="action-card">
          <div class="action-icon">
            <MessageSquare :size="20" stroke-width="1.5" />
          </div>
          <div class="action-content">
            <h4 class="action-title">分类反馈</h4>
            <p class="action-desc">查看用户分类反馈建议</p>
          </div>
          <ChevronRight class="action-arrow" :size="20" stroke-width="1.5" />
        </router-link>

        <router-link to="/admin/orders" class="action-card">
          <div class="action-icon">
            <ClipboardList :size="20" stroke-width="1.5" />
          </div>
          <div class="action-content">
            <h4 class="action-title">订单管理</h4>
            <p class="action-desc">查看与管理平台订单</p>
          </div>
          <ChevronRight class="action-arrow" :size="20" stroke-width="1.5" />
        </router-link>

        <router-link to="/admin/statistics" class="action-card">
          <div class="action-icon">
            <TrendingUp :size="20" stroke-width="1.5" />
          </div>
          <div class="action-content">
            <h4 class="action-title">数据统计</h4>
            <p class="action-desc">查看平台运营数据分析</p>
          </div>
          <ChevronRight class="action-arrow" :size="20" stroke-width="1.5" />
        </router-link>

        <router-link to="/admin/logs" class="action-card">
          <div class="action-icon">
            <FileText :size="20" stroke-width="1.5" />
          </div>
          <div class="action-content">
            <h4 class="action-title">操作日志</h4>
            <p class="action-desc">查看系统操作日志记录</p>
          </div>
          <ChevronRight class="action-arrow" :size="20" stroke-width="1.5" />
        </router-link>
      </div>
    </div>

    <!-- 待办事项与最近活动 -->
    <div class="dashboard-bottom">
      <!-- 待办事项 -->
      <div class="todo-section">
        <h3 class="section-subtitle">待办事项</h3>
        <div v-if="todoLoading" class="loading-placeholder">加载中...</div>
        <div v-else-if="todoItems.length === 0" class="loading-placeholder">暂无待办事项</div>
        <div v-else class="todo-list">
          <div v-for="item in todoItems" :key="item.id" class="todo-item">
            <div class="todo-priority" :class="item.priority"></div>
            <div class="todo-content">
              <h4 class="todo-title">{{ item.title }}</h4>
              <p class="todo-desc">{{ item.description }}</p>
              <span class="todo-time">{{ item.time }}</span>
            </div>
            <div class="todo-count" :class="item.type">{{ item.count }}</div>
          </div>
        </div>
      </div>

      <!-- 最近活动 -->
      <div class="activity-section">
        <h3 class="section-subtitle">最近活动</h3>
        <div v-if="activityLoading" class="loading-placeholder">加载中...</div>
        <div v-else-if="recentActivities.length === 0" class="loading-placeholder">暂无最新活动</div>
        <div v-else class="activity-list">
          <div
            v-for="activity in recentActivities"
            :key="activity.id"
            class="activity-item"
          >
            <div class="activity-icon" :class="activity.type">
              <Users v-if="activity.type === 'user'" :size="20" stroke-width="1.5" />
              <Package v-else-if="activity.type === 'item'" :size="20" stroke-width="1.5" />
              <ClipboardList v-else-if="activity.type === 'order'" :size="20" stroke-width="1.5" />
              <FileText v-else :size="20" stroke-width="1.5" />
            </div>
            <div class="activity-content">
              <p class="activity-text">{{ activity.text }}</p>
              <span class="activity-time">{{ activity.time }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import api from '../../api';
import { Users, Package, ClipboardList, DollarSign, CheckCircle, ChevronRight, Menu, MessageSquare, TrendingUp, FileText } from 'lucide-vue-next';

const loading = ref(false);
const todoLoading = ref(false);
const activityLoading = ref(false);

const stats = ref({
  totalUsers: 0,
  newUsersToday: 0,
  totalItems: 0,
  newItemsToday: 0,
  totalOrders: 0,
  newOrdersToday: 0,
  totalAmount: 0,
  amountToday: 0,
});

// 待办事项数据（从 API 加载）
const todoItems = ref<Array<{ id: number; title: string; description: string; time: string; count: number; priority: string; type: string }>>([]);

// 最近活动数据（从 API 加载）
const recentActivities = ref<Array<{ id: number; type: string; text: string; time: string }>>([]);

const formatNumber = (num: number) => {
  if (num === null || num === undefined) return '0';
  return Number(num).toLocaleString();
};

const fetchStats = async () => {
  loading.value = true;
  try {
    const [userStatsRes, itemStatsRes, orderStatsRes] = await Promise.all([
      api.admin.users.getUserStats(),
      api.admin.items.getItemStats(),
      api.admin.orders.getStats(),
    ]);

    if (userStatsRes.code === 200) {
      stats.value.totalUsers = userStatsRes.data.total || 0;
      stats.value.newUsersToday = userStatsRes.data.todayNew || 0;
    }

    if (itemStatsRes.code === 200) {
      stats.value.totalItems = itemStatsRes.data.total || 0;
      stats.value.newItemsToday = itemStatsRes.data.todayNew || 0;
    }

    if (orderStatsRes.code === 200) {
      stats.value.totalOrders = orderStatsRes.data.total || 0;
      stats.value.newOrdersToday = orderStatsRes.data.todayNew || 0;
      stats.value.totalAmount = orderStatsRes.data.totalAmount || 0;
      stats.value.amountToday = orderStatsRes.data.todayAmount || 0;
    }
  } catch (error) {
    console.error('Error fetching stats:', error);
    ElMessage.error('获取统计数据失败');
  } finally {
    loading.value = false;
  }
};

/** 加载 Dashboard 数据（todo, activities） */
const fetchDashboardData = async () => {
  todoLoading.value = true;
  activityLoading.value = true;
  try {
    const res = await api.admin.statistics.getDashboard();
    if (res.code === 200 && res.data) {
      if (res.data.todoItems) {
        todoItems.value = res.data.todoItems;
      }
      if (res.data.recentActivities) {
        recentActivities.value = res.data.recentActivities;
      }
    }
  } catch {
    // 网络异常，显示降级提示
    todoItems.value = [];
    recentActivities.value = [];
  } finally {
    todoLoading.value = false;
    activityLoading.value = false;
  }
};

onMounted(() => {
  fetchStats();
  fetchDashboardData();
});
</script>

<style scoped src="../../styles/pages/dashboard.css"></style>
