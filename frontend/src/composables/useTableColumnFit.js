import { ref, watch, onMounted, onUnmounted, nextTick } from 'vue';

export function useTableColumnFit(options = {}) {
  const {
    defaultMinWidth = 60,
    defaultMaxWidth = 400,
    defaultWidth = 120,
    chineseCharWidth = 16,
    englishCharWidth = 8,
    numberWidth = 10,
    padding = 20,
    fitDelay = 100,
  } = options;

  const columnWidths = ref({});
  const isFitting = ref(false);
  let fitTimer = null;

  const columnConfigs = ref({
    id: { minWidth: 60, maxWidth: 100, defaultWidth: 80 },
    username: { minWidth: 100, maxWidth: 200, defaultWidth: 140 },
    nickname: { minWidth: 80, maxWidth: 160, defaultWidth: 120 },
    email: { minWidth: 180, maxWidth: 300, defaultWidth: 220 },
    phone: { minWidth: 110, maxWidth: 150, defaultWidth: 120 },
    role: { minWidth: 70, maxWidth: 100, defaultWidth: 80 },
    status: { minWidth: 70, maxWidth: 100, defaultWidth: 80 },
    verified: { minWidth: 70, maxWidth: 100, defaultWidth: 80 },
    createdAt: { minWidth: 150, maxWidth: 200, defaultWidth: 180 },
    updatedAt: { minWidth: 150, maxWidth: 200, defaultWidth: 180 },
    itemTitle: { minWidth: 150, maxWidth: 350, defaultWidth: 200 },
    itemPrice: { minWidth: 80, maxWidth: 120, defaultWidth: 100 },
    orderNo: { minWidth: 150, maxWidth: 220, defaultWidth: 180 },
    totalAmount: { minWidth: 100, maxWidth: 150, defaultWidth: 120 },
    realName: { minWidth: 80, maxWidth: 150, defaultWidth: 100 },
    idCard: { minWidth: 150, maxWidth: 200, defaultWidth: 180 },
    categoryName: { minWidth: 80, maxWidth: 150, defaultWidth: 100 },
    itemCount: { minWidth: 70, maxWidth: 100, defaultWidth: 80 },
  });

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

  const getDisplayText = (prop, value) => {
    switch (prop) {
      case 'role':
        return value === 'ADMIN' ? '管理员' : '学生';
      case 'status':
        // 物品状态映射
        if (
          typeof value === 'string' &&
          ['PENDING', 'ON_SALE', 'SOLD', 'OFF_SHELF', 'REJECTED'].includes(
            value
          )
        ) {
          const statusMap = {
            PENDING: '待审核',
            ON_SALE: '在售',
            SOLD: '已售',
            OFF_SHELF: '下架',
            REJECTED: '驳回',
          };
          return statusMap[value] || value;
        }
        // 用户状态映射
        return value === 'ACTIVE' ? '活跃' : '禁用';
      case 'verified':
        return value ? '已认证' : '未认证';
      default:
        return value;
    }
  };

  const calculateColumnWidth = (prop, data) => {
    const config = columnConfigs.value[prop] || {
      minWidth: defaultMinWidth,
      maxWidth: defaultMaxWidth,
      defaultWidth: defaultWidth,
    };

    if (!data || data.length === 0) {
      return config.defaultWidth;
    }

    let maxWidth = config.defaultWidth;

    for (const row of data) {
      let cellValue = row[prop];

      if (cellValue === null || cellValue === undefined) {
        continue;
      }

      // 获取显示文本
      cellValue = getDisplayText(prop, cellValue);

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
      Math.max(maxWidth, config.minWidth),
      config.maxWidth
    );

    return Math.ceil(finalWidth);
  };

  const fitAllColumns = (data) => {
    if (!data) return;

    isFitting.value = true;

    nextTick(() => {
      const newWidths = {};

      for (const prop of Object.keys(columnConfigs.value)) {
        newWidths[prop] = calculateColumnWidth(prop, data);
      }

      for (const row of data) {
        if (row && typeof row === 'object') {
          for (const key of Object.keys(row)) {
            if (!(key in newWidths)) {
              newWidths[key] = calculateColumnWidth(key, data);
            }
          }
        }
      }

      columnWidths.value = newWidths;
      isFitting.value = false;
    });
  };

  const fitColumn = (prop, data) => {
    if (!prop) return;

    const width = calculateColumnWidth(prop, data);
    columnWidths.value = {
      ...columnWidths.value,
      [prop]: width,
    };

    return width;
  };

  const getColumnWidth = (prop) => {
    return (
      columnWidths.value[prop] ||
      columnConfigs.value[prop]?.defaultWidth ||
      defaultWidth
    );
  };

  const setColumnConfig = (prop, config) => {
    columnConfigs.value = {
      ...columnConfigs.value,
      [prop]: {
        ...columnConfigs.value[prop],
        ...config,
      },
    };
  };

  const debouncedFit = (data) => {
    if (fitTimer) {
      clearTimeout(fitTimer);
    }

    fitTimer = setTimeout(() => {
      fitAllColumns(data);
    }, fitDelay);
  };

  const handleResize = () => {
    if (fitTimer) {
      clearTimeout(fitTimer);
    }

    fitTimer = setTimeout(() => {
      // 窗口大小变化时，我们需要重新计算列宽
      // 但是我们没有保存原始数据，所以这里暂时不做处理
      // 实际使用时，应该在组件中监听窗口大小变化并调用fitAllColumns
    }, fitDelay);
  };

  onMounted(() => {
    window.addEventListener('resize', handleResize);
  });

  onUnmounted(() => {
    window.removeEventListener('resize', handleResize);
    if (fitTimer) {
      clearTimeout(fitTimer);
    }
  });

  return {
    columnWidths,
    isFitting,
    columnConfigs,
    fitAllColumns,
    fitColumn,
    getColumnWidth,
    setColumnConfig,
    debouncedFit,
    calculateTextWidth,
    calculateColumnWidth,
  };
}

export function createColumnWidthCalculator(options = {}) {
  const {
    minWidth = 60,
    maxWidth = 400,
    defaultWidth = 120,
    chineseCharWidth = 16,
    englishCharWidth = 8,
    numberWidth = 10,
    padding = 20,
  } = options;

  const calculateTextWidth = (text) => {
    if (!text || typeof text !== 'string') {
      return defaultWidth;
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

  const calculateColumnWidth = (data) => {
    if (!data || data.length === 0) {
      return defaultWidth;
    }

    let maxWidth = defaultWidth;

    for (const item of data) {
      const textWidth = calculateTextWidth(String(item));
      maxWidth = Math.max(maxWidth, textWidth);
    }

    return Math.min(Math.max(maxWidth, minWidth), maxWidth);
  };

  return {
    minWidth,
    maxWidth,
    defaultWidth,
    calculateTextWidth,
    calculateColumnWidth,
  };
}

export default {
  useTableColumnFit,
  createColumnWidthCalculator,
};
