<template>
  <div ref="chartRef" class="echarts-container"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, computed } from 'vue';
import * as echarts from 'echarts/core';
import { PieChart } from 'echarts/charts';
import { TooltipComponent, LegendComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { useDictStore } from '../../../store/dict.js';

echarts.use([PieChart, TooltipComponent, LegendComponent, CanvasRenderer]);

const props = defineProps({
  data: {
    type: Object,
    default: () => ({}),
  },
});

const dictStore = useDictStore();
const chartRef = ref(null);
let chartInstance = null;

// 从字典获取状态映射
const statusMap = computed(() => {
  const map = {};
  const orderStatuses = dictStore.getDictOptions('ORDER_STATUS');
  const colors = [
    '#f59e0b',
    '#3b82f6',
    '#8b5cf6',
    '#22c55e',
    '#94a3b8',
    '#ef4444',
    '#ec4899',
  ];

  orderStatuses.forEach((status, index) => {
    map[status.value] = {
      name: status.label,
      color: colors[index % colors.length],
    };
  });

  return map;
});

// statusMap 已在上面定义为计算属性

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
      name: statusMap.value[key]?.name || key,
      value: value,
      itemStyle: { color: statusMap.value[key]?.color || '#94a3b8' },
    }));

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)',
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      data: pieData.map((item) => item.name),
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
          borderWidth: 2,
        },
        label: {
          show: false,
          position: 'center',
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold',
          },
        },
        labelLine: {
          show: false,
        },
        data: pieData,
      },
    ],
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
