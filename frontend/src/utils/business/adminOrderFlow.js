export const OrderStatus = {
  PENDING_PAYMENT: 'PENDING_PAYMENT',
  PAID: 'PAID',
  PENDING_SHIPMENT: 'PENDING_SHIPMENT',
  SHIPPED: 'SHIPPED',
  DELIVERED: 'DELIVERED',
  COMPLETED: 'COMPLETED',
  CANCELLED: 'CANCELLED',
  REFUNDED: 'REFUNDED',
};

export const OrderStatusText = {
  [OrderStatus.PENDING_PAYMENT]: '待付款',
  [OrderStatus.PAID]: '已付款',
  [OrderStatus.PENDING_SHIPMENT]: '待发货',
  [OrderStatus.SHIPPED]: '已发货',
  [OrderStatus.DELIVERED]: '已送达',
  [OrderStatus.COMPLETED]: '已完成',
  [OrderStatus.CANCELLED]: '已取消',
  [OrderStatus.REFUNDED]: '已退款',
};

export const OrderStatusColor = {
  [OrderStatus.PENDING_PAYMENT]: '#faad14',
  [OrderStatus.PAID]: '#1890ff',
  [OrderStatus.PENDING_SHIPMENT]: '#faad14',
  [OrderStatus.SHIPPED]: '#1890ff',
  [OrderStatus.DELIVERED]: '#1890ff',
  [OrderStatus.COMPLETED]: '#52c41a',
  [OrderStatus.CANCELLED]: '#8c8c8c',
  [OrderStatus.REFUNDED]: '#ff4d4f',
};

export const getOrderStatusText = (status) => {
  return OrderStatusText[status] || '未知状态';
};

export const getOrderStatusColor = (status) => {
  return OrderStatusColor[status] || '#8c8c8c';
};

export const getOrderStatusOptions = (role = 'buyer') => {
  return [
    { label: '全部', value: 'ALL' },
    { label: '待付款', value: OrderStatus.PENDING_PAYMENT },
    { label: '已付款', value: OrderStatus.PAID },
    { label: '待发货', value: OrderStatus.PENDING_SHIPMENT },
    { label: '已发货', value: OrderStatus.SHIPPED },
    { label: '已完成', value: OrderStatus.COMPLETED },
    { label: '已取消', value: OrderStatus.CANCELLED },
    { label: '已退款', value: OrderStatus.REFUNDED },
  ];
};

export const getOrderActions = (status, role) => {
  const actions = [];

  if (role === 'admin') {
    actions.push({ name: '查看详情', type: 'primary' });
    if (status === OrderStatus.PENDING_PAYMENT) {
      actions.push({ name: '取消订单', type: 'danger' });
    }
    if (
      status === OrderStatus.PAID ||
      status === OrderStatus.PENDING_SHIPMENT
    ) {
      actions.push({ name: '标记发货', type: 'primary' });
    }
  }

  return actions;
};

export const ADMIN_ORDER_PAYMENT_OPTIONS = [
  { label: '在线支付', value: 'ONLINE' },
  { label: '线下支付', value: 'OFFLINE' },
  { label: '微信支付', value: 'WECHAT' },
  { label: '支付宝', value: 'ALIPAY' },
];

export const ADMIN_ORDER_STATUS_OPTIONS = [
  { label: '全部', value: 'ALL' },
  { label: '待付款', value: OrderStatus.PENDING_PAYMENT },
  { label: '已付款', value: OrderStatus.PAID },
  { label: '待发货', value: OrderStatus.PENDING_SHIPMENT },
  { label: '已发货', value: OrderStatus.SHIPPED },
  { label: '已完成', value: OrderStatus.COMPLETED },
  { label: '已取消', value: OrderStatus.CANCELLED },
  { label: '已退款', value: OrderStatus.REFUNDED },
];

export const canAdminApproveRefund = (status) => {
  return (
    status === OrderStatus.PAID ||
    status === OrderStatus.PENDING_SHIPMENT ||
    status === OrderStatus.SHIPPED
  );
};

export const getAdminOrderStatusText = (status) => {
  return OrderStatusText[status] || '未知状态';
};

export const getAdminOrderStatusTime = (order) => {
  if (!order) return '';
  const statusTimes = {
    [OrderStatus.PENDING_PAYMENT]: order.createdAt,
    [OrderStatus.PAID]: order.paidAt,
    [OrderStatus.PENDING_SHIPMENT]: order.paidAt,
    [OrderStatus.SHIPPED]: order.shippedAt,
    [OrderStatus.DELIVERED]: order.deliveredAt,
    [OrderStatus.COMPLETED]: order.completedAt,
    [OrderStatus.CANCELLED]: order.cancelledAt,
    [OrderStatus.REFUNDED]: order.refundedAt,
  };
  return statusTimes[order.orderStatus] || order.createdAt;
};

export const getAdminPaymentText = (paymentMethod) => {
  const paymentMap = {
    ONLINE: '在线支付',
    OFFLINE: '线下支付',
    WECHAT: '微信支付',
    ALIPAY: '支付宝',
  };
  return paymentMap[paymentMethod] || '未知支付方式';
};

export const normalizeAdminOrder = (order) => {
  if (!order) return null;
  return {
    ...order,
    orderStatus: order.orderStatus || OrderStatus.PENDING_PAYMENT,
    price: order.price || 0,
    createdAt: order.createdAt || new Date().toISOString(),
  };
};

export const canAdminCancelOrder = (status) => {
  return status === OrderStatus.PENDING_PAYMENT || status === OrderStatus.PAID;
};

export const getAdminOrderActions = (status, order) => {
  const actions = [];

  actions.push({ key: 'view', label: '查看详情', tone: 'primary' });

  if (canAdminCancelOrder(status)) {
    actions.push({ key: 'cancel', label: '取消订单', tone: 'danger' });
  }

  if (status === OrderStatus.PAID || status === OrderStatus.PENDING_SHIPMENT) {
    actions.push({ key: 'ship', label: '标记发货', tone: 'primary' });
  }

  if (canAdminApproveRefund(status)) {
    actions.push({ key: 'approveRefund', label: '处理退款', tone: 'warning' });
  }

  return actions;
};

export const getAdminOrderStatusClass = (status) => {
  const classes = {
    [OrderStatus.PENDING_PAYMENT]: 'admin-status-pending',
    [OrderStatus.PAID]: 'admin-status-paid',
    [OrderStatus.PENDING_SHIPMENT]: 'admin-status-processing',
    [OrderStatus.SHIPPED]: 'admin-status-shipped',
    [OrderStatus.DELIVERED]: 'admin-status-delivered',
    [OrderStatus.COMPLETED]: 'admin-status-completed',
    [OrderStatus.CANCELLED]: 'admin-status-cancelled',
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
  getOrderActions,
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
