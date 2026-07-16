<template>
  <div class="statistics">
    <div class="page-intro">
      <h2 class="section-title">数据统计</h2>
      <p class="section-desc">平台交易数据概览与趋势分析</p>
    </div>

    <div class="date-range-bar">
      <div class="date-range">
        <span class="date-label">统计周期：</span>
        <el-radio-group v-model="timeRange" @change="handleTimeRangeChange">
          <el-radio-button value="today">今日</el-radio-button>
          <el-radio-button value="week">本周</el-radio-button>
          <el-radio-button value="month">本月</el-radio-button>
        </el-radio-group>
        <span class="date-separator">或自定义：</span>
        <el-date-picker
          v-model="customDateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          @change="handleCustomDateChange"
        />
        <button class="btn btn-ghost" @click="handleRefresh" title="刷新">
          <RefreshCw :size="18" stroke-width="1.5" />
          刷新
        </button>
      </div>
    </div>

    <template v-if="loading">
      <div class="stats-grid">
        <div v-for="n in 4" :key="n" class="stat-card">
          <div class="skeleton skeleton-line skeleton-w40"></div>
          <div class="skeleton skeleton-stat-value"></div>
        </div>
      </div>
      <div class="charts-grid">
        <div class="chart-card chart-card-wide">
          <div class="chart-header">
            <div class="skeleton skeleton-line skeleton-w30"></div>
          </div>
          <div class="chart-body">
            <div class="skeleton skeleton-chart-area"></div>
          </div>
        </div>
        <div class="chart-card">
          <div class="chart-header">
            <div class="skeleton skeleton-line skeleton-w50"></div>
          </div>
          <div class="chart-body chart-body-center">
            <div class="skeleton skeleton-circle"></div>
          </div>
        </div>
      </div>
      <div class="chart-card chart-card-full">
        <div class="chart-header">
          <div class="skeleton skeleton-line skeleton-w25"></div>
        </div>
        <div class="chart-body">
          <div v-for="i in 5" :key="i" class="skeleton-table-row">
            <div class="skeleton skeleton-bar skeleton-w60"></div>
            <div class="skeleton skeleton-bar skeleton-w120"></div>
            <div class="skeleton skeleton-bar skeleton-w80"></div>
            <div class="skeleton skeleton-bar skeleton-w80"></div>
            <div class="skeleton skeleton-bar skeleton-w60"></div>
            <div class="skeleton skeleton-bar skeleton-w60"></div>
            <div class="skeleton skeleton-bar skeleton-w140"></div>
          </div>
        </div>
      </div>
    </template>

    <template v-if="!loading">
      <div class="stats-grid">
        <div class="stat-card stat-card-primary">
          <div class="stat-header">
            <span class="stat-label">总订单数</span>
          </div>
          <div class="stat-value">
            {{ formatNumber(dashboardData.totalOrders) }}
          </div>
        </div>
        <div class="stat-card stat-card-success">
          <div class="stat-header">
            <span class="stat-label">总交易额</span>
          </div>
          <div class="stat-value">
            ¥{{ formatNumber(dashboardData.totalAmount) }}
          </div>
        </div>
        <div class="stat-card stat-card-warning">
          <div class="stat-header">
            <span class="stat-label">待处理订单</span>
          </div>
          <div class="stat-value">
            {{ formatNumber(dashboardData.pendingOrders) }}
          </div>
        </div>
        <div class="stat-card stat-card-info">
          <div class="stat-header">
            <span class="stat-label">已完成订单</span>
          </div>
          <div class="stat-value">
            {{ formatNumber(dashboardData.completedOrders) }}
          </div>
        </div>
      </div>

      <div class="charts-grid">
        <div class="chart-card chart-card-wide">
          <div class="chart-header">
            <h3 class="chart-title">订单趋势</h3>
          </div>
          <div class="chart-body">
            <OrderTrendChart :data="dashboardData.orderTrend" />
          </div>
        </div>

        <div class="chart-card">
          <div class="chart-header">
            <h3 class="chart-title">订单状态分布</h3>
          </div>
          <div class="chart-body">
            <OrderStatusPie :data="dashboardData.orderStatusDistribution" />
          </div>
        </div>
      </div>

      <div class="chart-card chart-card-full">
        <div class="chart-header">
          <h3 class="chart-title">最近订单</h3>
        </div>
        <div class="chart-body">
          <el-table :data="dashboardData.recentOrders" style="width: 100%">
            <el-table-column prop="id" label="订单ID" width="80" />
            <el-table-column prop="orderNo" label="订单号" width="150" />
            <el-table-column prop="buyerName" label="买家" width="100" />
            <el-table-column prop="sellerName" label="卖家" width="100" />
            <el-table-column prop="amount" label="金额" width="100">
              <template #default="{ row }"> ¥{{ row.amount }} </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">{{
                  getStatusText(row.status)
                }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间">
              <template #default="{ row }">
                {{ formatDateTime(row.createdAt) }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import OrderTrendChart from './components/OrderTrendChart.vue';
import OrderStatusPie from './components/OrderStatusPie.vue';
import { RefreshCw } from 'lucide-vue-next';
import api from '../../api';
import { useDictStore } from '../../store/dict.js';

const dictStore = useDictStore();
const loading = ref(false);
const timeRange = ref('today');
const customDateRange = ref<any>(null);

const dashboardData = ref({
  totalOrders: 0,
  totalAmount: 0,
  pendingOrders: 0,
  completedOrders: 0,
  orderTrend: [],
  orderStatusDistribution: {},
  recentOrders: [],
});

const formatNumber = (num: number) => {
  if (num === null || num === undefined) return '0';
  return Number(num).toLocaleString();
};

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-';
  return new Date(dateTime).toLocaleString('zh-CN');
};

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    PENDING_PAYMENT: 'warning',
    PAID: 'primary',
    PENDING_SHIPMENT: 'primary',
    SHIPPED: 'info',
    DELIVERED: 'info',
    COMPLETED: 'success',
    CANCELLED: 'info',
    REFUND_REQUESTED: 'danger',
    REFUNDED: 'warning',
  };
  return (map[status] || 'info') as 'primary' | 'success' | 'warning' | 'info' | 'danger';
};

const getStatusText = (status: string) => {
  return dictStore.getDictLabel('ORDER_STATUS', status);
};

const fetchDashboard = async () => {
  loading.value = true;
  try {
    const params: Record<string, unknown> = { timeRange: timeRange.value };
    if (timeRange.value === 'custom' && customDateRange.value) {
      params.startDate = customDateRange.value[0].toISOString().split('T')[0];
      params.endDate = customDateRange.value[1].toISOString().split('T')[0];
    }

    const response = await api.admin.statistics.getDashboard(params);
    if (response.code === 200) {
      dashboardData.value = response.data as any;
    } else {
      ElMessage.error(response.message || '获取统计数据失败');
    }
  } catch (error: any) {
    console.error('Error fetching dashboard:', error);
    if (!error.response) {
      ElMessage.error('网络连接失败，请检查网络');
    }
  } finally {
    loading.value = false;
  }
};

const handleTimeRangeChange = () => {
  if (timeRange.value !== 'custom') {
    fetchDashboard();
  }
};

const handleCustomDateChange = () => {
  if (customDateRange.value && customDateRange.value.length === 2) {
    timeRange.value = 'custom';
    fetchDashboard();
  }
};

const handleRefresh = () => {
  fetchDashboard();
  ElMessage.success('已刷新');
};

onMounted(async () => {
  // 加载字典数据
  await dictStore.preloadCommonDicts();
  fetchDashboard();
});
</script>

<style scoped src="../../styles/pages/admin-statistics.css"></style>
