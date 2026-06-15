/**
 * 物品相关共享工具函数
 * 集中管理成色文本、相对时间等非分类工具
 */

const COLOR_PALETTE = ['#dce8f7', '#f5edd6', '#d8f0e0', '#e8d8f0', '#f0e0d0', '#e0e8d8'];

/** 基于 categoryId 的确定性颜色选择（使用取模运算均匀分配） */
export function getCategoryColorById(categoryId: number): string {
  return COLOR_PALETTE[Math.abs(categoryId) % COLOR_PALETTE.length];
}

/** 获取物品成色文本 */
export function getConditionText(condition: string): string {
  const map: Record<string, string> = {
    NEW: '全新',
    LIKE_NEW: '九五新',
    GOOD: '九成新',
    FAIR: '八成新',
    POOR: '七成新',
  };
  return map[condition] || condition || '未知';
}

/** 获取交易方式文本 */
export function getDeliveryText(method: string): string {
  const map: Record<string, string> = {
    LOCAL_DELIVERY: '面交',
    HOME_DELIVERY: '上门',
    EXPRESS: '快递',
    MAIL: '邮寄',
  };
  return map[method] || method || '面交';
}

/** 相对时间文本 */
export function getTimeAgo(dateStr: string): string {
  if (!dateStr) return '刚刚';
  const date = new Date(dateStr);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);
  if (minutes < 60) return `${minutes}分钟前`;
  if (hours < 24) return `${hours}小时前`;
  return `${days}天前`;
}
