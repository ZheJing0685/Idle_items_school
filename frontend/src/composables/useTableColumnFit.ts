import { ref, onMounted, onUnmounted, nextTick } from 'vue';

interface TableColumnFitOptions {
  defaultMinWidth?: number
  defaultMaxWidth?: number
  defaultWidth?: number
  chineseCharWidth?: number
  englishCharWidth?: number
  numberWidth?: number
  padding?: number
  fitDelay?: number
}

interface ColumnConfig {
  minWidth: number
  maxWidth: number
  defaultWidth: number
}

export function useTableColumnFit(options: TableColumnFitOptions = {}) {
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

  const columnWidths = ref<Record<string, number>>({});
  const isFitting = ref(false);
  let fitTimer: ReturnType<typeof setTimeout> | null = null;

  const columnConfigs = ref<Record<string, ColumnConfig>>({
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

  const calculateTextWidth = (text: string): number => {
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
      } else if (/[\s\-.@#$%&*+]/.test(char)) {
        width += englishCharWidth * 0.6;
      } else {
        width += englishCharWidth * 0.8;
      }
    }

    return width + padding;
  };

  const getDisplayText = (prop: string, value: any): string => {
    switch (prop) {
    case 'role':
      return value === 'ADMIN' ? '管理员' : '学生';
    case 'status':
      if (
        typeof value === 'string' &&
          ['PENDING', 'ON_SALE', 'SOLD', 'OFF_SHELF', 'REJECTED'].includes(
            value,
          )
      ) {
        const statusMap: Record<string, string> = {
          PENDING: '待审核',
          ON_SALE: '在售',
          SOLD: '已售',
          OFF_SHELF: '下架',
          REJECTED: '驳回',
        };
        return statusMap[value] || value;
      }
      return value === 'ACTIVE' ? '活跃' : '禁用';
    case 'verified':
      return value ? '已认证' : '未认证';
    default:
      return value;
    }
  };

  const calculateColumnWidth = (prop: string, data: Record<string, any>[]): number => {
    const config: ColumnConfig = columnConfigs.value[prop] || {
      minWidth: defaultMinWidth,
      maxWidth: defaultMaxWidth,
      defaultWidth: defaultWidth,
    };

    if (!data || data.length === 0) {
      return config.defaultWidth;
    }

    let maxWidth = config.defaultWidth;

    for (const row of data) {
      let cellValue: any = row[prop];

      if (cellValue === null || cellValue === undefined) {
        continue;
      }

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
      config.maxWidth,
    );

    return Math.ceil(finalWidth);
  };

  const fitAllColumns = (data: Record<string, any>[]) => {
    if (!data) return;

    isFitting.value = true;

    nextTick(() => {
      const newWidths: Record<string, number> = {};

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

  const fitColumn = (prop: string, data: Record<string, any>[]): number | undefined => {
    if (!prop) return;

    const width = calculateColumnWidth(prop, data);
    columnWidths.value = {
      ...columnWidths.value,
      [prop]: width,
    };

    return width;
  };

  const getColumnWidth = (prop: string): number => {
    return (
      columnWidths.value[prop] ||
      columnConfigs.value[prop]?.defaultWidth ||
      defaultWidth
    );
  };

  const setColumnConfig = (prop: string, config: Partial<ColumnConfig>) => {
    columnConfigs.value = {
      ...columnConfigs.value,
      [prop]: {
        ...(columnConfigs.value[prop] || {}),
        ...config,
      } as ColumnConfig,
    };
  };

  const debouncedFit = (data: Record<string, any>[]) => {
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
      // window size changed, re-calculation would need original data
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

export function createColumnWidthCalculator(options: {
  minWidth?: number
  maxWidth?: number
  defaultWidth?: number
  chineseCharWidth?: number
  englishCharWidth?: number
  numberWidth?: number
  padding?: number
} = {}) {
  const {
    minWidth = 60,
    maxWidth = 400,
    defaultWidth = 120,
    chineseCharWidth = 16,
    englishCharWidth = 8,
    numberWidth = 10,
    padding = 20,
  } = options;

  const calculateTextWidth = (text: string): number => {
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
      } else if (/[\s\-.@#$%&*+]/.test(char)) {
        width += englishCharWidth * 0.6;
      } else {
        width += englishCharWidth * 0.8;
      }
    }

    return width + padding;
  };

  const calculateColumnWidth = (data: string[]): number => {
    if (!data || data.length === 0) {
      return defaultWidth;
    }

    let maxCalcWidth = defaultWidth;

    for (const item of data) {
      const textWidth = calculateTextWidth(String(item));
      maxCalcWidth = Math.max(maxCalcWidth, textWidth);
    }

    return Math.min(Math.max(maxCalcWidth, minWidth), maxWidth);
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
