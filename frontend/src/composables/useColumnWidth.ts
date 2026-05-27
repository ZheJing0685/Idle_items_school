interface ColumnConfig {
  minWidth: number
  maxWidth: number
  defaultWidth: number
}

interface WidthConfig {
  minWidth: number
  maxWidth: number
  defaultWidth: number
  chineseCharWidth: number
  englishCharWidth: number
  numberWidth: number
  padding: number
  headerHeight: number
}

export function useColumnWidth() {
  const defaultConfig: WidthConfig = {
    minWidth: 60,
    maxWidth: 400,
    defaultWidth: 120,
    chineseCharWidth: 16,
    englishCharWidth: 8,
    numberWidth: 10,
    padding: 20,
    headerHeight: 40,
  };

  const columnConfigs: Record<string, ColumnConfig> = {
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
    operation: { minWidth: 200, maxWidth: 300, defaultWidth: 220 },
  };

  const calculateTextWidth = (text: string, config: WidthConfig = defaultConfig): number => {
    if (!text || typeof text !== 'string') {
      return config.defaultWidth;
    }

    let width = 0;
    for (const char of text) {
      const code = char.charCodeAt(0);
      if (code >= 0x4e00 && code <= 0x9fa5) {
        width += config.chineseCharWidth;
      } else if (/[a-zA-Z]/.test(char)) {
        width += config.englishCharWidth;
      } else if (/[0-9]/.test(char)) {
        width += config.numberWidth;
      } else if (/[\s\-_.@]/.test(char)) {
        width += config.englishCharWidth * 0.6;
      } else {
        width += config.englishCharWidth * 0.8;
      }
    }

    return width + config.padding;
  };

  const calculateColumnWidth = (columnName: string, data: Record<string, any>[], config: WidthConfig = defaultConfig): number => {
    const columnConfig: ColumnConfig = columnConfigs[columnName] || {
      minWidth: config.minWidth,
      maxWidth: config.maxWidth,
      defaultWidth: config.defaultWidth,
    };

    if (!data || data.length === 0) {
      return columnConfig.defaultWidth;
    }

    let maxWidth = columnConfig.defaultWidth;

    for (const row of data) {
      let cellValue: any = row[columnName];

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

      const textWidth = calculateTextWidth(cellValue, config);
      maxWidth = Math.max(maxWidth, textWidth);
    }

    const finalWidth = Math.min(
      Math.max(maxWidth, columnConfig.minWidth),
      columnConfig.maxWidth,
    );

    return Math.ceil(finalWidth);
  };

  const fitColumnsWidth = (columns: { prop?: string; field?: string }[], data: Record<string, any>[], config: WidthConfig = defaultConfig): Record<string, number> => {
    const result: Record<string, number> = {};

    for (const column of columns) {
      const columnName = column.prop || column.field;

      if (
        !columnName ||
        columnName === 'selection' ||
        columnName === 'operation'
      ) {
        continue;
      }

      result[columnName] = calculateColumnWidth(columnName, data, config);
    }

    return result;
  };

  const getColumnWidth = (columnName: string, calculatedWidths: Record<string, number>): number => {
    return (
      calculatedWidths[columnName] ||
      columnConfigs[columnName]?.defaultWidth ||
      defaultConfig.defaultWidth
    );
  };

  return {
    defaultConfig,
    columnConfigs,
    calculateTextWidth,
    calculateColumnWidth,
    fitColumnsWidth,
    getColumnWidth,
  };
}
