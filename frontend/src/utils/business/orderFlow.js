export const OrderStatus = {
  PENDING_PAYMENT: 'PENDING_PAYMENT',
  PENDING_SHIPMENT: 'PENDING_SHIPMENT',
  SHIPPED: 'SHIPPED',
  COMPLETED: 'COMPLETED',
  CANCELLED: 'CANCELLED',
  REFUND_REQUESTED: 'REFUND_REQUESTED',
  REFUNDED: 'REFUNDED',
};

export const OrderStatusText = {
  [OrderStatus.PENDING_PAYMENT]: '待付款',
  [OrderStatus.PENDING_SHIPMENT]: '待发货',
  [OrderStatus.SHIPPED]: '已发货',
  [OrderStatus.COMPLETED]: '已完成',
  [OrderStatus.CANCELLED]: '已取消',
  [OrderStatus.REFUND_REQUESTED]: '退款申请中',
  [OrderStatus.REFUNDED]: '已退款',
};

export const OrderStatusColor = {
  [OrderStatus.PENDING_PAYMENT]: '#faad14',
  [OrderStatus.PENDING_SHIPMENT]: '#faad14',
  [OrderStatus.SHIPPED]: '#1890ff',
  [OrderStatus.COMPLETED]: '#52c41a',
  [OrderStatus.CANCELLED]: '#8c8c8c',
  [OrderStatus.REFUND_REQUESTED]: '#ff7a45',
  [OrderStatus.REFUNDED]: '#ff4d4f',
};

export const getOrderStatusText = (status) => {
  return OrderStatusText[status] || '未知状态';
};

export const getOrderStatusColor = (status) => {
  return OrderStatusColor[status] || '#8c8c8c';
};

export const getOrderActions = (status, role) => {
  const actions = [];

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

export const getOrderStatusProgress = (status) => {
  const steps = [
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

export const getOrderHint = (status, role) => {
  const hints = {
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

  return hints[role]?.[status] || '';
};

export const normalizeOrder = (order) => {
  if (!order) return null;
  return {
    ...order,
    orderStatus: order.orderStatus || OrderStatus.PENDING_PAYMENT,
    price: order.price || 0,
    createdAt: order.createdAt || new Date().toISOString(),
  };
};

export const sanitizeOrderStatus = (status) => {
  return Object.values(OrderStatus).includes(status)
    ? status
    : OrderStatus.PENDING_PAYMENT;
};

export const sanitizeOrderView = (view) => {
  return ['buyer', 'seller'].includes(view) ? view : 'buyer';
};

export const getOrderStatusOptions = (role = 'buyer') => {
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

export const getOrderStatusClass = (status) => {
  const classes = {
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
