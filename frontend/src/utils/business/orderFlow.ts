export const OrderStatus = {
  PENDING_PAYMENT: 'PENDING_PAYMENT',
  PENDING_SHIPMENT: 'PENDING_SHIPMENT',
  SHIPPED: 'SHIPPED',
  COMPLETED: 'COMPLETED',
  CANCELLED: 'CANCELLED',
  REFUND_REQUESTED: 'REFUND_REQUESTED',
  REFUNDED: 'REFUNDED',
} as const;

export type OrderStatusValue = typeof OrderStatus[keyof typeof OrderStatus]

export const OrderStatusText: Record<OrderStatusValue, string> = {
  [OrderStatus.PENDING_PAYMENT]: '待付款',
  [OrderStatus.PENDING_SHIPMENT]: '待发货',
  [OrderStatus.SHIPPED]: '已发货',
  [OrderStatus.COMPLETED]: '已完成',
  [OrderStatus.CANCELLED]: '已取消',
  [OrderStatus.REFUND_REQUESTED]: '退款申请中',
  [OrderStatus.REFUNDED]: '已退款',
};

export const OrderStatusColor: Record<OrderStatusValue, string> = {
  [OrderStatus.PENDING_PAYMENT]: '#faad14',
  [OrderStatus.PENDING_SHIPMENT]: '#faad14',
  [OrderStatus.SHIPPED]: '#1890ff',
  [OrderStatus.COMPLETED]: '#52c41a',
  [OrderStatus.CANCELLED]: '#8c8c8c',
  [OrderStatus.REFUND_REQUESTED]: '#ff7a45',
  [OrderStatus.REFUNDED]: '#ff4d4f',
};

export type OrderRole = 'buyer' | 'seller'

export interface OrderAction {
  key: string
  label: string
  type: 'primary' | 'danger' | 'warning' | 'info'
}

export interface StatusStep {
  key: OrderStatusValue
  title: string
}

export interface StatusProgress {
  steps: StatusStep[]
  current: number
}

export interface OrderHintMap {
  buyer?: Partial<Record<OrderStatusValue, string>>
  seller?: Partial<Record<OrderStatusValue, string>>
}

export interface SelectOption {
  label: string
  value: string
}

export interface OrderLike {
  orderStatus?: string
  price?: number
  createdAt?: string
  [key: string]: any
}

export interface NormalizedOrder {
  orderStatus: string
  price: number
  createdAt: string
  [key: string]: any
}

export const getOrderStatusText = (status: string): string => {
  return OrderStatusText[status as OrderStatusValue] || '未知状态';
};

const statusCSSVar: Record<string, string> = {
  PENDING_PAYMENT: '--order-status-pending',
  PENDING_SHIPMENT: '--order-status-pending',
  SHIPPED: '--order-status-shipped',
  COMPLETED: '--order-status-completed',
  CANCELLED: '--order-status-cancelled',
  REFUND_REQUESTED: '--order-status-refund-requested',
  REFUNDED: '--order-status-refunded',
};

export const getOrderStatusColor = (status: string): string => {
  const varName = statusCSSVar[status];
  if (varName && typeof document !== 'undefined') {
    return getComputedStyle(document.documentElement).getPropertyValue(varName).trim() || '#8c8c8c';
  }
  return OrderStatusColor[status as OrderStatusValue] || '#8c8c8c';
};

export const getOrderActions = (status: string, role: OrderRole): OrderAction[] => {
  const actions: OrderAction[] = [];

  if (role === 'buyer') {
    switch (status) {
    case OrderStatus.PENDING_PAYMENT:
      actions.push({ key: 'pay', label: '付款', type: 'primary' });
      actions.push({ key: 'cancel', label: '取消订单', type: 'danger' });
      break;
    case OrderStatus.PENDING_SHIPMENT:
    case OrderStatus.SHIPPED:
      actions.push({
        key: 'confirmReceive',
        label: '确认收货',
        type: 'primary',
      });
      actions.push({
        key: 'applyRefund',
        label: '申请退款',
        type: 'warning',
      });
      break;
    case OrderStatus.COMPLETED:
      actions.push({ key: 'review', label: '评价', type: 'primary' });
      break;
    }
  } else if (role === 'seller') {
    switch (status) {
    case OrderStatus.PENDING_SHIPMENT:
      actions.push({ key: 'ship', label: '确认发货', type: 'primary' });
      break;
    }
  }

  return actions;
};

export const getOrderStatusProgress = (status: string): StatusProgress => {
  const terminatedSteps: Record<string, StatusStep[]> = {
    [OrderStatus.CANCELLED]: [{ key: OrderStatus.CANCELLED, title: '已取消' }],
    [OrderStatus.REFUND_REQUESTED]: [{ key: OrderStatus.REFUND_REQUESTED, title: '退款申请中' }],
    [OrderStatus.REFUNDED]: [{ key: OrderStatus.REFUNDED, title: '已退款' }],
  };

  if (terminatedSteps[status]) {
    return { steps: terminatedSteps[status], current: 0 };
  }

  const steps: StatusStep[] = [
    { key: OrderStatus.PENDING_PAYMENT, title: '待付款' },
    { key: OrderStatus.PENDING_SHIPMENT, title: '待发货' },
    { key: OrderStatus.SHIPPED, title: '已发货' },
    { key: OrderStatus.COMPLETED, title: '已完成' },
  ];

  const currentIndex = steps.findIndex((step) => step.key === status);
  return {
    steps,
    current: currentIndex === -1 ? 0 : currentIndex,
  };
};

export const getOrderHint = (status: string, role: OrderRole): string => {
  const hints: OrderHintMap = {
    buyer: {
      [OrderStatus.PENDING_PAYMENT]: '请及时付款，超时订单将自动取消',
      [OrderStatus.PENDING_SHIPMENT]: '卖家准备中，请等待发货',
      [OrderStatus.SHIPPED]: '请在收到商品后点击确认收货',
      [OrderStatus.REFUND_REQUESTED]: '退款申请已提交，请等待处理',
    },
    seller: {
      [OrderStatus.PENDING_SHIPMENT]: '买家已付款，请尽快确认发货',
    },
  };

  return hints[role]?.[status as OrderStatusValue] || '';
};

export const normalizeOrder = (order: OrderLike | null): NormalizedOrder | null => {
  if (!order) return null;
  return {
    ...order,
    orderStatus: order.orderStatus || OrderStatus.PENDING_PAYMENT,
    price: order.price || 0,
    createdAt: order.createdAt || new Date().toISOString(),
  };
};

export const sanitizeOrderStatus = (status: string): string => {
  return (Object.values(OrderStatus) as string[]).includes(status)
    ? status
    : OrderStatus.PENDING_PAYMENT;
};

export const sanitizeOrderView = (view: string): OrderRole => {
  return (['buyer', 'seller'] as OrderRole[]).includes(view as OrderRole) ? view as OrderRole : 'buyer';
};

export const getOrderStatusOptions = (_role?: string): SelectOption[] => {
  return [
    { label: '全部', value: 'ALL' },
    { label: '待付款', value: OrderStatus.PENDING_PAYMENT },
    { label: '待发货', value: OrderStatus.PENDING_SHIPMENT },
    { label: '已发货', value: OrderStatus.SHIPPED },
    { label: '已完成', value: OrderStatus.COMPLETED },
    { label: '已取消', value: OrderStatus.CANCELLED },
    { label: '退款申请中', value: OrderStatus.REFUND_REQUESTED },
    { label: '已退款', value: OrderStatus.REFUNDED },
  ];
};

export const getOrderStatusClass = (status: string): string => {
  const classes: Record<string, string> = {
    [OrderStatus.PENDING_PAYMENT]: 'status-pending',
    [OrderStatus.PENDING_SHIPMENT]: 'status-processing',
    [OrderStatus.SHIPPED]: 'status-shipped',
    [OrderStatus.COMPLETED]: 'status-completed',
    [OrderStatus.CANCELLED]: 'status-cancelled',
    [OrderStatus.REFUND_REQUESTED]: 'status-refund-requested',
    [OrderStatus.REFUNDED]: 'status-refunded',
  };
  return classes[status] || 'status-unknown';
};

export default {
  OrderStatus,
  OrderStatusText,
  OrderStatusColor,
  getOrderStatusText,
  getOrderStatusColor,
  getOrderActions,
  getOrderStatusProgress,
  getOrderHint,
  normalizeOrder,
  sanitizeOrderStatus,
  sanitizeOrderView,
  getOrderStatusOptions,
  getOrderStatusClass,
};
