# 后台管理统计仪表盘实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为后台管理系统创建数据统计仪表盘，展示订单趋势、状态分布，支持时间范围筛选

**Architecture:** 基于现有 Vue 3 + Element Plus 前端架构，新增 ECharts 5.x 图表库；后端新增 StatisticsController 提供统一的数据统计 API

**Tech Stack:** Vue 3.5+, Element Plus 2.13+, ECharts 5.x, Spring Boot 3.2+, Spring Data JPA

---

## 文件结构

### 后端新增文件
- `backend/src/main/java/com/idleitems/school/controller/admin/StatisticsController.java` - 交易统计 API
- `backend/src/main/java/com/idleitems/school/dto/statistics/DashboardResponse.java` - 仪表盘响应 DTO

### 前端修改文件
- `frontend/src/views/admin/Statistics.vue` - 主页面组件（重构）
- `frontend/src/api/index.js` - 添加 adminStatistics API

### 前端新增文件
- `frontend/src/views/admin/components/OrderTrendChart.vue` - 订单趋势图组件
- `frontend/src/views/admin/components/OrderStatusPie.vue` - 订单状态饼图组件

---

## Task 1: 创建后端 StatisticsController

**Files:**
- Create: `backend/src/main/java/com/idleitems/school/controller/admin/StatisticsController.java`
- Create: `backend/src/main/java/com/idleitems/school/dto/statistics/DashboardResponse.java`
- Test: `http://localhost:7000/api/admin/statistics/dashboard`

- [ ] **Step 1: 创建 DashboardResponse DTO**

```java
package com.idleitems.school.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private Long totalOrders;
    private BigDecimal totalAmount;
    private Long pendingOrders;
    private Long completedOrders;
    private List<OrderTrendItem> orderTrend;
    private Map<String, Long> orderStatusDistribution;
    private List<RecentOrder> recentOrders;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderTrendItem {
        private String date;
        private Long count;
        private BigDecimal amount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentOrder {
        private Long id;
        private String orderNo;
        private String buyerName;
        private String sellerName;
        private BigDecimal amount;
        private String status;
        private LocalDateTime createdAt;
    }
}
```

- [ ] **Step 2: 创建 StatisticsController**

```java
package com.idleitems.school.controller.admin;

import com.idleitems.school.annotation.RequireRole;
import com.idleitems.school.common.Result;
import com.idleitems.school.dto.statistics.DashboardResponse;
import com.idleitems.school.entity.Order;
import com.idleitems.school.entity.User;
import com.idleitems.school.repository.OrderRepository;
import com.idleitems.school.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/admin/statistics")
@RequiredArgsConstructor
@RequireRole(value = {User.Role.ADMIN}, message = "需要管理员权限")
public class StatisticsController {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @GetMapping("/dashboard")
    public Result<DashboardResponse> getDashboard(
            @RequestParam(defaultValue = "today") String timeRange,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDateTime[] dateRange = calculateDateRange(timeRange, startDate, endDate);
        LocalDateTime start = dateRange[0];
        LocalDateTime end = dateRange[1];

        List<Order> ordersInRange = orderRepository.findByCreatedAtBetween(start, end);
        List<Order> allOrders = orderRepository.findAll();

        long totalOrders = allOrders.size();
        long pendingOrders = orderRepository.countByOrderStatus(Order.OrderStatus.PENDING);
        long completedOrders = orderRepository.countByOrderStatus(Order.OrderStatus.COMPLETED);
        BigDecimal totalAmount = orderRepository.sumCompletedOrderAmount();
        if (totalAmount == null) {
            totalAmount = BigDecimal.ZERO;
        }

        List<DashboardResponse.OrderTrendItem> orderTrend = generateOrderTrend(start, end);

        Map<String, Long> statusDistribution = new HashMap<>();
        statusDistribution.put("pending", orderRepository.countByOrderStatus(Order.OrderStatus.PENDING));
        statusDistribution.put("processing", orderRepository.countByOrderStatus(Order.OrderStatus.PROCESSING));
        statusDistribution.put("completed", orderRepository.countByOrderStatus(Order.OrderStatus.COMPLETED));
        statusDistribution.put("cancelled", orderRepository.countByOrderStatus(Order.OrderStatus.CANCELLED));

        List<DashboardResponse.RecentOrder> recentOrders = allOrders.stream()
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .limit(10)
                .map(order -> {
                    String buyerName = userRepository.findById(order.getBuyerId())
                            .map(u -> u.getNickname() != null ? u.getNickname() : u.getUsername())
                            .orElse("未知");
                    String sellerName = userRepository.findById(order.getSellerId())
                            .map(u -> u.getNickname() != null ? u.getNickname() : u.getUsername())
                            .orElse("未知");
                    return DashboardResponse.RecentOrder.builder()
                            .id(order.getId())
                            .orderNo(order.getOrderNo())
                            .buyerName(buyerName)
                            .sellerName(sellerName)
                            .amount(order.getTotalAmount())
                            .status(order.getOrderStatus().name())
                            .createdAt(order.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        DashboardResponse response = DashboardResponse.builder()
                .totalOrders(totalOrders)
                .totalAmount(totalAmount)
                .pendingOrders(pendingOrders)
                .completedOrders(completedOrders)
                .orderTrend(orderTrend)
                .orderStatusDistribution(statusDistribution)
                .recentOrders(recentOrders)
                .build();

        return Result.success(response);
    }

    private LocalDateTime[] calculateDateRange(String timeRange, LocalDate startDate, LocalDate endDate) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start;

        switch (timeRange) {
            case "week":
                start = end.minusWeeks(1).withHour(0).withMinute(0).withSecond(0);
                break;
            case "month":
                start = end.minusMonths(1).withHour(0).withMinute(0).withSecond(0);
                break;
            case "custom":
                start = startDate.atStartOfDay();
                end = endDate.atTime(LocalTime.MAX);
                break;
            case "today":
            default:
                start = end.withHour(0).withMinute(0).withSecond(0);
                break;
        }

        return new LocalDateTime[]{start, end};
    }

    private List<DashboardResponse.OrderTrendItem> generateOrderTrend(LocalDateTime start, LocalDateTime end) {
        List<DashboardResponse.OrderTrendItem> trend = new ArrayList<>();
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate());
        int maxDays = (int) Math.min(daysBetween, 30);

        for (int i = maxDays; i >= 0; i--) {
            LocalDate date = end.minusDays(i).toLocalDate();
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(LocalTime.MAX);

            List<Order> dayOrders = orderRepository.findByCreatedAtBetween(dayStart, dayEnd);
            BigDecimal dayAmount = dayOrders.stream()
                    .filter(o -> o.getOrderStatus() == Order.OrderStatus.COMPLETED)
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            trend.add(DashboardResponse.OrderTrendItem.builder()
                    .date(date.toString())
                    .count((long) dayOrders.size())
                    .amount(dayAmount)
                    .build());
        }

        return trend;
    }
}
```

- [ ] **Step 3: 在 OrderRepository 中添加必要方法**

修改: `backend/src/main/java/com/idleitems/school/repository/OrderRepository.java`

添加方法：
```java
List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
```

- [ ] **Step 4: 重启后端并测试 API**

运行命令: `mvn spring-boot:run` (终端 6)
访问: `http://localhost:7000/api/admin/statistics/dashboard?timeRange=today`
预期结果: 返回包含 totalOrders, totalAmount, pendingOrders, completedOrders, orderTrend, orderStatusDistribution, recentOrders 的 JSON

- [ ] **Step 5: 提交代码**

```bash
git add backend/src/main/java/com/idleitems/school/controller/admin/StatisticsController.java backend/src/main/java/com/idleitems/school/dto/statistics/DashboardResponse.java backend/src/main/java/com/idleitems/school/repository/OrderRepository.java
git commit -m "feat: 添加交易统计仪表盘 API"
```

---

## Task 2: 安装 ECharts 依赖

**Files:**
- Modify: `frontend/package.json`

- [ ] **Step 1: 安装 ECharts 依赖**

运行命令: `cd frontend && npm install echarts vue-echarts --save`
预期结果: 依赖安装成功，package.json 中新增 echarts 和 vue-echarts

- [ ] **Step 2: 提交代码**

```bash
git add frontend/package.json frontend/package-lock.json
git commit -m "chore: 添加 echarts 依赖"
```

---

## Task 3: 重构前端 Statistics.vue

**Files:**
- Modify: `frontend/src/views/admin/Statistics.vue`
- Modify: `frontend/src/api/index.js`

- [ ] **Step 1: 添加 adminStatistics API**

修改: `frontend/src/api/index.js`

在文件末尾添加：
```javascript
const adminStatistics = {
  getDashboard: (params) =>
    instance.get('/admin/statistics/dashboard', { params }),
};

api.adminStatistics = adminStatistics;
```

- [ ] **Step 2: 重构 Statistics.vue 主页面**

修改: `frontend/src/views/admin/Statistics.vue`

将整个文件内容替换为（保持原有样式结构，只修改 script 和 template）:

```vue
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
          <el-radio-button label="today">今日</el-radio-button>
          <el-radio-button label="week">本周</el-radio-button>
          <el-radio-button label="month">本月</el-radio-button>
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
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M21.5 2v6h-6M2.5 22v-6h6" />
            <path d="M2 12A10 10 0 1 0 22 12" />
          </svg>
          刷新
        </button>
      </div>
    </div>

    <div class="loading-container" v-if="loading">
      <div class="loading-spinner"></div>
      <span class="loading-text">加载中...</span>
    </div>

    <template v-if="!loading">
      <div class="stats-grid">
        <div class="stat-card stat-card-primary">
          <div class="stat-header">
            <span class="stat-label">总订单数</span>
          </div>
          <div class="stat-value">{{ formatNumber(dashboardData.totalOrders) }}</div>
        </div>
        <div class="stat-card stat-card-success">
          <div class="stat-header">
            <span class="stat-label">总交易额</span>
          </div>
          <div class="stat-value">¥{{ formatNumber(dashboardData.totalAmount) }}</div>
        </div>
        <div class="stat-card stat-card-warning">
          <div class="stat-header">
            <span class="stat-label">待处理订单</span>
          </div>
          <div class="stat-value">{{ formatNumber(dashboardData.pendingOrders) }}</div>
        </div>
        <div class="stat-card stat-card-info">
          <div class="stat-header">
            <span class="stat-label">已完成订单</span>
          </div>
          <div class="stat-value">{{ formatNumber(dashboardData.completedOrders) }}</div>
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
              <template #default="{ row }">
                ¥{{ row.amount }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
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

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import OrderTrendChart from './components/OrderTrendChart.vue';
import OrderStatusPie from './components/OrderStatusPie.vue';
import api from '../../api';

const loading = ref(false);
const timeRange = ref('today');
const customDateRange = ref(null);

const dashboardData = ref({
  totalOrders: 0,
  totalAmount: 0,
  pendingOrders: 0,
  completedOrders: 0,
  orderTrend: [],
  orderStatusDistribution: {},
  recentOrders: []
});

const formatNumber = (num) => {
  if (num === null || num === undefined) return '0';
  return Number(num).toLocaleString();
};

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-';
  return new Date(dateTime).toLocaleString('zh-CN');
};

const getStatusType = (status) => {
  const map = {
    'PENDING': 'warning',
    'PROCESSING': 'primary',
    'COMPLETED': 'success',
    'CANCELLED': 'info'
  };
  return map[status] || 'info';
};

const getStatusText = (status) => {
  const map = {
    'PENDING': '待处理',
    'PROCESSING': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  };
  return map[status] || status;
};

const fetchDashboard = async () => {
  loading.value = true;
  try {
    const params = { timeRange: timeRange.value };
    if (timeRange.value === 'custom' && customDateRange.value) {
      params.startDate = customDateRange.value[0].toISOString().split('T')[0];
      params.endDate = customDateRange.value[1].toISOString().split('T')[0];
    }

    const response = await api.adminStatistics.getDashboard(params);
    if (response.code === 200) {
      dashboardData.value = response.data;
    } else {
      ElMessage.error(response.message || '获取统计数据失败');
    }
  } catch (error) {
    console.error('Error fetching dashboard:', error);
    ElMessage.error('网络错误，请稍后重试');
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

onMounted(() => {
  fetchDashboard();
});
</script>
```

- [ ] **Step 3: 提交代码**

```bash
git add frontend/src/views/admin/Statistics.vue frontend/src/api/index.js
git commit -m "refactor: 重构统计页面为交易仪表盘"
```

---

## Task 4: 创建 ECharts 图表组件

**Files:**
- Create: `frontend/src/views/admin/components/OrderTrendChart.vue`
- Create: `frontend/src/views/admin/components/OrderStatusPie.vue`

- [ ] **Step 1: 创建 OrderTrendChart 组件**

创建: `frontend/src/views/admin/components/OrderTrendChart.vue`

```vue
<template>
  <div ref="chartRef" class="echarts-container"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue';
import * as echarts from 'echarts';

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  }
});

const chartRef = ref(null);
let chartInstance = null;

const initChart = () => {
  if (!chartRef.value) return;

  chartInstance = echarts.init(chartRef.value);
  updateChart();
};

const updateChart = () => {
  if (!chartInstance) return;

  const dates = props.data.map(item => item.date);
  const orderCounts = props.data.map(item => item.count);
  const amounts = props.data.map(item => item.amount);

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['订单数', '交易额'],
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: false
    },
    yAxis: [
      {
        type: 'value',
        name: '订单数',
        position: 'left'
      },
      {
        type: 'value',
        name: '交易额(元)',
        position: 'right',
        axisLabel: {
          formatter: (value) => '¥' + value
        }
      }
    ],
    series: [
      {
        name: '订单数',
        type: 'line',
        smooth: true,
        data: orderCounts,
        itemStyle: { color: '#6366f1' }
      },
      {
        name: '交易额',
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        data: amounts,
        itemStyle: { color: '#22c55e' }
      }
    ]
  };

  chartInstance.setOption(option);
};

const handleResize = () => {
  chartInstance?.resize();
};

watch(() => props.data, updateChart, { deep: true });

onMounted(() => {
  initChart();
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  chartInstance?.dispose();
});
</script>

<style scoped>
.echarts-container {
  width: 100%;
  height: 280px;
}
</style>
```

- [ ] **Step 2: 创建 OrderStatusPie 组件**

创建: `frontend/src/views/admin/components/OrderStatusPie.vue`

```vue
<template>
  <div ref="chartRef" class="echarts-container"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue';
import * as echarts from 'echarts';

const props = defineProps({
  data: {
    type: Object,
    default: () => ({})
  }
});

const chartRef = ref(null);
let chartInstance = null;

const statusMap = {
  pending: { name: '待处理', color: '#f59e0b' },
  processing: { name: '进行中', color: '#3b82f6' },
  completed: { name: '已完成', color: '#22c55e' },
  cancelled: { name: '已取消', color: '#94a3b8' }
};

const initChart = () => {
  if (!chartRef.value) return;

  chartInstance = echarts.init(chartRef.value);
  updateChart();
};

const updateChart = () => {
  if (!chartInstance) return;

  const pieData = Object.entries(props.data).map(([key, value]) => ({
    name: statusMap[key]?.name || key,
    value: value,
    itemStyle: { color: statusMap[key]?.color || '#94a3b8' }
  }));

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      data: pieData.map(item => item.name)
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: pieData
      }
    ]
  };

  chartInstance.setOption(option);
};

const handleResize = () => {
  chartInstance?.resize();
};

watch(() => props.data, updateChart, { deep: true });

onMounted(() => {
  initChart();
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  chartInstance?.dispose();
});
</script>

<style scoped>
.echarts-container {
  width: 100%;
  height: 280px;
}
</style>
```

- [ ] **Step 3: 提交代码**

```bash
git add frontend/src/views/admin/components/OrderTrendChart.vue frontend/src/views/admin/components/OrderStatusPie.vue
git commit -m "feat: 添加订单趋势和状态图表组件"
```

---

## Task 5: 验证完整功能

**Verification:**

- [ ] 访问 `http://localhost:5173/admin/statistics` 验证页面加载
- [ ] 验证统计卡片显示总订单数、总交易额、待处理订单、已完成订单
- [ ] 验证订单趋势折线图正确显示
- [ ] 验证订单状态饼图正确显示
- [ ] 点击"今日/本周/本月"筛选按钮，验证数据更新
- [ ] 验证最近订单表格正确显示

---

## 验收清单

- [ ] 后端 API `/api/admin/statistics/dashboard` 正常返回数据
- [ ] 前端统计页面正常加载，显示4个统计卡片
- [ ] 订单趋势折线图正常显示（支持今日/本周/本月切换）
- [ ] 订单状态饼图正常显示
- [ ] 最近订单表格正确显示最新10条订单
- [ ] 页面在不同屏幕宽度下正常显示
