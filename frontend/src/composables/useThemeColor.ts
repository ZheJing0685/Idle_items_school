function getCSSVar(name: string): string {
  return getComputedStyle(document.documentElement).getPropertyValue(name).trim();
}

export function useThemeColor() {
  function chartColors(): string[] {
    const colors: string[] = [];
    for (let i = 1; i <= 7; i++) {
      colors.push(getCSSVar(`--chart-color-${i}`) || '#94a3b8');
    }
    return colors;
  }

  function trendColors(): { count: string; amount: string } {
    return {
      count: getCSSVar('--chart-trend-count') || '#6366f1',
      amount: getCSSVar('--chart-trend-amount') || '#22c55e',
    };
  }

  return { getCSSVar, chartColors, trendColors };
}
