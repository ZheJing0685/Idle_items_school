<template>
  <div ref="chartRef" class="echarts-container"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue';
import type { PropType } from 'vue';
import * as echarts from 'echarts/core';
import { LineChart } from 'echarts/charts';
import {
  TooltipComponent,
  LegendComponent,
  GridComponent,
} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { useThemeColor } from '../../../composables/useThemeColor';

echarts.use([
  LineChart,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  CanvasRenderer,
]);

interface TrendItem {
  date: string
  count: number
  amount: number
}

const props = defineProps({
  data: {
    type: Array as PropType<TrendItem[]>,
    default: () => [],
  },
});

const chartRef = ref<HTMLDivElement | null>(null);
let chartInstance: echarts.ECharts | null = null;
const { trendColors } = useThemeColor();

const initChart = () => {
  if (!chartRef.value) return;

  chartInstance = echarts.init(chartRef.value);
  updateChart();
};

const updateChart = () => {
  if (!chartInstance) return;

  const dates = props.data.map((item) => item.date);
  const orderCounts = props.data.map((item) => item.count);
  const amounts = props.data.map((item) => item.amount);

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' },
    },
    legend: {
      data: ['订单数', '交易额'],
      bottom: 0,
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '10%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: false,
    },
    yAxis: [
      {
        type: 'value',
        name: '订单数',
        position: 'left',
      },
      {
        type: 'value',
        name: '交易额(元)',
        position: 'right',
        axisLabel: {
          formatter: (value: number) => '¥' + value,
        },
      },
    ],
    series: [
      {
        name: '订单数',
        type: 'line',
        smooth: true,
        data: orderCounts,
        itemStyle: { color: trendColors().count },
      },
      {
        name: '交易额',
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        data: amounts,
        itemStyle: { color: trendColors().amount },
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
