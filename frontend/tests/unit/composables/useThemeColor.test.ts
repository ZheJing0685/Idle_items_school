import { describe, it, expect, vi, beforeEach } from 'vitest';

// Mock getComputedStyle
const getComputedStyleMock = vi.fn().mockImplementation(() => ({
  getPropertyValue: vi.fn().mockReturnValue('#6366f1'),
}));

Object.defineProperty(window, 'getComputedStyle', { value: getComputedStyleMock });

describe('useThemeColor Composable', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should export useThemeColor function', async () => {
    const { useThemeColor } = await import('@/composables/useThemeColor');
    expect(typeof useThemeColor).toBe('function');
  });

  it('should return getCSSVar, chartColors, trendColors', async () => {
    const { useThemeColor } = await import('@/composables/useThemeColor');
    const result = useThemeColor();
    expect(typeof result.getCSSVar).toBe('function');
    expect(typeof result.chartColors).toBe('function');
    expect(typeof result.trendColors).toBe('function');
  });

  it('should get CSS variable value', async () => {
    const { useThemeColor } = await import('@/composables/useThemeColor');
    const { getCSSVar } = useThemeColor();
    const value = getCSSVar('--test-var');
    expect(value).toBeDefined();
    expect(typeof value).toBe('string');
  });

  it('should return chart colors array', async () => {
    const { useThemeColor } = await import('@/composables/useThemeColor');
    const { chartColors } = useThemeColor();
    const colors = chartColors();
    expect(Array.isArray(colors)).toBe(true);
    expect(colors.length).toBe(7);
  });

  it('should return trend colors object', async () => {
    const { useThemeColor } = await import('@/composables/useThemeColor');
    const { trendColors } = useThemeColor();
    const trend = trendColors();
    expect(trend).toBeDefined();
    expect(trend.count).toBeDefined();
    expect(trend.amount).toBeDefined();
    expect(typeof trend.count).toBe('string');
    expect(typeof trend.amount).toBe('string');
  });

  it('should return default color if CSS var not found', async () => {
    getComputedStyleMock.mockReturnValue({
      getPropertyValue: vi.fn().mockReturnValue(''),
    });
    const { useThemeColor } = await import('@/composables/useThemeColor');
    const { chartColors } = useThemeColor();
    const colors = chartColors();
    expect(colors.every(c => c === '#94a3b8')).toBe(true);
  });
});
