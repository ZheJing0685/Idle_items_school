<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue';

const props = defineProps({
  column: {
    type: Object,
    required: true,
  },
  tableData: {
    type: Array,
    default: () => [],
  },
  minWidth: {
    type: Number,
    default: 60,
  },
  maxWidth: {
    type: Number,
    default: 400,
  },
  defaultWidth: {
    type: Number,
    default: 120,
  },
  fitOnMounted: {
    type: Boolean,
    default: true,
  },
});

const emit = defineEmits(['width-calculated']);

const columnWidth = ref(props.column.width || props.defaultWidth);
const isAutoCalculating = ref(false);

const chineseCharWidth = 16;
const englishCharWidth = 8;
const numberWidth = 10;
const padding = 20;

const calculateTextWidth = (text) => {
  if (!text || typeof text !== 'string') {
    return 0;
  }

  let width = 0;
  for (const char of text) {
    const code = char.charCodeAt(0);
    if (code >= 0x4e00 && code <= 0x9fa5) {
      width += chineseCharWidth;
    } else if (/[a-zA-Z]/.test(char)) {
      width += englishCharWidth;
    } else if (/[0-9]/.test(char)) {
      width += numberWidth;
    } else if (/[\s\-\_\.\@\#\$\%\&\*\+]/.test(char)) {
      width += englishCharWidth * 0.6;
    } else {
      width += englishCharWidth * 0.8;
    }
  }

  return width + padding;
};

const calculateColumnWidth = () => {
  const prop = props.column.prop;

  if (
    !prop ||
    props.column.type === 'selection' ||
    props.column.type === 'index'
  ) {
    return columnWidth.value;
  }

  if (!props.tableData || props.tableData.length === 0) {
    return columnWidth.value;
  }

  let maxWidth = props.defaultWidth;

  for (const row of props.tableData) {
    let cellValue = row[prop];

    if (cellValue === null || cellValue === undefined) {
      continue;
    }

    if (typeof cellValue === 'boolean') {
      cellValue = cellValue ? '是' : '否';
    } else if (typeof cellValue === 'number') {
      cellValue = cellValue.toString();
    } else {
      cellValue = String(cellValue);
    }

    const textWidth = calculateTextWidth(cellValue);
    maxWidth = Math.max(maxWidth, textWidth);
  }

  const finalWidth = Math.min(
    Math.max(maxWidth, props.minWidth),
    props.maxWidth
  );
  columnWidth.value = Math.ceil(finalWidth);

  return columnWidth.value;
};

const fitColumnWidth = () => {
  if (isAutoCalculating.value) return;

  isAutoCalculating.value = true;

  nextTick(() => {
    const width = calculateColumnWidth();
    emit('width-calculated', {
      prop: props.column.prop,
      width: width,
    });
    isAutoCalculating.value = false;
  });
};

watch(
  () => props.tableData,
  () => {
    if (props.fitOnMounted) {
      fitColumnWidth();
    }
  },
  { deep: true }
);

watch(
  () => props.column.width,
  (newWidth) => {
    if (newWidth && !isAutoCalculating.value) {
      columnWidth.value = newWidth;
    }
  }
);

onMounted(() => {
  if (props.fitOnMounted) {
    fitColumnWidth();
  }
});

const columnStyle = computed(() => ({
  width: `${columnWidth.value}px`,
  minWidth: `${props.minWidth}px`,
  maxWidth: `${props.maxWidth}px`,
}));

defineExpose({
  fitColumnWidth,
  columnWidth,
});
</script>

<template>
  <el-table-column
    v-bind="column"
    :width="columnStyle.width"
    :min-width="columnStyle.minWidth"
  >
    <template v-for="(index, name) in $slots" #[name]="slotData">
      <slot :name="name" v-bind="slotData || {}"></slot>
    </template>
  </el-table-column>
</template>
