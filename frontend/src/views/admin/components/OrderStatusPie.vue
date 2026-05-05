<template>
  <div ref="chartRef" class="echarts-container"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue';
import * as echarts from 'echarts/core';
import { PieChart } from 'echarts/charts';
import {
  TooltipComponent,
  LegendComponent
} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';

echarts.use([
  PieChart,
  TooltipComponent,
  LegendComponent,
  CanvasRenderer
]);

const props = defineProps({
  data: {
    type: Object,
    default: () => ({})
  }
});

const chartRef = ref(null);
let chartInstance = null;

const statusMap = {
  pending_payment: { name: '待付款', color: '#f59e0b' },
  pending_shipment: { name: '待发货', color: '#3b82f6' },
  shipped: { name: '已发货', color: '#8b5cf6' },
  completed: { name: '已完成', color: '#22c55e' },
  cancelled: { name: '已取消', color: '#94a3b8' },
  refund_requested: { name: '退款中', color: '#ef4444' },
  refunded: { name: '已退款', color: '#ec4899' }
};

const initChart = () => {
  if (!chartRef.value) return;

  chartInstance = echarts.init(chartRef.value);
  updateChart();
};

const updateChart = () => {
  if (!chartInstance) return;

  const pieData = Object.entries(props.data)
    .filter(([key, value]) => value > 0)
    .map(([key, value]) => ({
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

<style scoped src="../../../styles/components/admin-charts.css"></style>
