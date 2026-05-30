// 管理端订单状态模型 — 仅包含后端 Order.OrderStatus 枚举定义的 7 种状态
export const OrderStatus = {
  PENDING_PAYMENT: 'PENDING_PAYMENT',
  PENDING_SHIPMENT: 'PENDING_SHIPMENT',
  SHIPPED: 'SHIPPED',
  COMPLETED: 'COMPLETED',
  CANCELLED: 'CANCELLED',
  REFUND_REQUESTED: 'REFUND_REQUESTED',
  REFUNDED: 'REFUNDED',
} as const;

export type AdminOrderStatusValue = typeof OrderStatus[keyof typeof OrderStatus]

export const OrderStatusText: Record<AdminOrderStatusValue, string> = {
  [OrderStatus.PENDING_PAYMENT]: '待付款',
  [OrderStatus.PENDING_SHIPMENT]: '待发货',
  [OrderStatus.SHIPPED]: '已发货',
  [OrderStatus.COMPLETED]: '已完成',
  [OrderStatus.CANCELLED]: '已取消',
  [OrderStatus.REFUND_REQUESTED]: '退款申请中',
  [OrderStatus.REFUNDED]: '已退款',
};

export const OrderStatusColor: Record<AdminOrderStatusValue, string> = {
  [OrderStatus.PENDING_PAYMENT]: '#faad14',
  [OrderStatus.PENDING_SHIPMENT]: '#faad14',
  [OrderStatus.SHIPPED]: '#1890ff',
  [OrderStatus.COMPLETED]: '#52c41a',
  [OrderStatus.CANCELLED]: '#8c8c8c',
  [OrderStatus.REFUND_REQUESTED]: '#ff7a45',
  [OrderStatus.REFUNDED]: '#ff4d4f',
};

export type AdminRole = 'admin'

export interface AdminAction {
  key: string
  label: string
  tone: 'primary' | 'danger' | 'warning'
}

export interface SelectOption {
  label: string
  value: string
}

export interface PaymentOption {
  label: string
  value: string
}

export interface OrderLike {
  orderStatus?: string
  price?: number
  createdAt?: string
  paymentTime?: string
  shipTime?: string
  completeTime?: string
  refundTime?: string
  cancelReason?: string
  refundReason?: string
  [key: string]: any
}

export interface NormalizedAdminOrder {
  orderStatus: string
  price: number
  createdAt: string
  [key: string]: any
}

export const getOrderStatusText = (status: string): string => {
  return OrderStatusText[status as AdminOrderStatusValue] || '未知状态';
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
  return OrderStatusColor[status as AdminOrderStatusValue] || '#8c8c8c';
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

export const ADMIN_ORDER_PAYMENT_OPTIONS: PaymentOption[] = [
  { label: '线上支付', value: 'ONLINE' },
  { label: '线下支付', value: 'OFFLINE' },
  { label: '微信支付', value: 'WECHAT' },
  { label: '支付宝', value: 'ALIPAY' },
];

export const ADMIN_ORDER_STATUS_OPTIONS: SelectOption[] = [
  { label: '全部', value: 'ALL' },
  { label: '待付款', value: OrderStatus.PENDING_PAYMENT },
  { label: '待发货', value: OrderStatus.PENDING_SHIPMENT },
  { label: '已发货', value: OrderStatus.SHIPPED },
  { label: '已完成', value: OrderStatus.COMPLETED },
  { label: '已取消', value: OrderStatus.CANCELLED },
  { label: '退款申请中', value: OrderStatus.REFUND_REQUESTED },
  { label: '已退款', value: OrderStatus.REFUNDED },
];

export const canAdminApproveRefund = (status: string): boolean => {
  return status === OrderStatus.REFUND_REQUESTED;
};

export const getAdminOrderStatusText = (status: string): string => {
  return OrderStatusText[status as AdminOrderStatusValue] || '未知状态';
};

export const getAdminOrderStatusTime = (order: OrderLike | null): string => {
  if (!order) return '';
  const statusTimes: Record<string, string | undefined> = {
    [OrderStatus.PENDING_PAYMENT]: order.createdAt,
    [OrderStatus.PENDING_SHIPMENT]: order.paymentTime,
    [OrderStatus.SHIPPED]: order.shipTime,
    [OrderStatus.COMPLETED]: order.completeTime,
    [OrderStatus.CANCELLED]: order.createdAt,
    [OrderStatus.REFUND_REQUESTED]: order.createdAt,
    [OrderStatus.REFUNDED]: order.refundTime,
  };
  return statusTimes[order.orderStatus || ''] || order.createdAt || '';
};

export const getAdminPaymentText = (paymentMethod: string): string => {
  const paymentMap: Record<string, string> = {
    ONLINE: '线上支付',
    OFFLINE: '线下支付',
    WECHAT: '微信支付',
    ALIPAY: '支付宝',
  };
  return paymentMap[paymentMethod] || '未知支付方式';
};

export const normalizeAdminOrder = (order: OrderLike | null): NormalizedAdminOrder | null => {
  if (!order) return null;
  return {
    ...order,
    orderStatus: order.orderStatus || OrderStatus.PENDING_PAYMENT,
    price: order.price || 0,
    createdAt: order.createdAt || new Date().toISOString(),
  };
};

export const canAdminCancelOrder = (status: string): boolean => {
  return ([OrderStatus.PENDING_PAYMENT, OrderStatus.PENDING_SHIPMENT, OrderStatus.SHIPPED] as string[]).includes(status);
};

export const getAdminOrderActions = (status: string, _order?: OrderLike | null): AdminAction[] => {
  const actions: AdminAction[] = [];
  actions.push({ key: 'view', label: '查看详情', tone: 'primary' });

  if (canAdminCancelOrder(status)) {
    actions.push({ key: 'cancel', label: '取消订单', tone: 'danger' });
  }

  if (canAdminApproveRefund(status)) {
    actions.push({ key: 'approveRefund', label: '处理退款', tone: 'warning' });
  }

  return actions;
};

export const getAdminOrderStatusClass = (status: string): string => {
  const classes: Record<string, string> = {
    [OrderStatus.PENDING_PAYMENT]: 'admin-status-pending',
    [OrderStatus.PENDING_SHIPMENT]: 'admin-status-processing',
    [OrderStatus.SHIPPED]: 'admin-status-shipped',
    [OrderStatus.COMPLETED]: 'admin-status-completed',
    [OrderStatus.CANCELLED]: 'admin-status-cancelled',
    [OrderStatus.REFUND_REQUESTED]: 'admin-status-refunding',
    [OrderStatus.REFUNDED]: 'admin-status-refunded',
  };
  return classes[status] || 'admin-status-unknown';
};

export default {
  OrderStatus,
  OrderStatusText,
  OrderStatusColor,
  getOrderStatusText,
  getOrderStatusColor,
  getOrderStatusOptions,
  ADMIN_ORDER_PAYMENT_OPTIONS,
  ADMIN_ORDER_STATUS_OPTIONS,
  canAdminApproveRefund,
  canAdminCancelOrder,
  getAdminOrderActions,
  getAdminOrderStatusClass,
  getAdminOrderStatusText,
  getAdminOrderStatusTime,
  getAdminPaymentText,
  normalizeAdminOrder,
};
