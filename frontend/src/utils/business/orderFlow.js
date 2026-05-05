export const OrderStatus = {
  PENDING_PAYMENT: 'PENDING_PAYMENT',
  PAID: 'PAID',
  PENDING_SHIPMENT: 'PENDING_SHIPMENT',
  SHIPPED: 'SHIPPED',
  DELIVERED: 'DELIVERED',
  COMPLETED: 'COMPLETED',
  CANCELLED: 'CANCELLED',
  REFUNDED: 'REFUNDED'
};

export const OrderStatusText = {
  [OrderStatus.PENDING_PAYMENT]: '待付款',
  [OrderStatus.PAID]: '已付款',
  [OrderStatus.PENDING_SHIPMENT]: '待发货',
  [OrderStatus.SHIPPED]: '已发货',
  [OrderStatus.DELIVERED]: '已送达',
  [OrderStatus.COMPLETED]: '已完成',
  [OrderStatus.CANCELLED]: '已取消',
  [OrderStatus.REFUNDED]: '已退款'
};

export const OrderStatusColor = {
  [OrderStatus.PENDING_PAYMENT]: '#faad14',
  [OrderStatus.PAID]: '#1890ff',
  [OrderStatus.PENDING_SHIPMENT]: '#faad14',
  [OrderStatus.SHIPPED]: '#1890ff',
  [OrderStatus.DELIVERED]: '#1890ff',
  [OrderStatus.COMPLETED]: '#52c41a',
  [OrderStatus.CANCELLED]: '#8c8c8c',
  [OrderStatus.REFUNDED]: '#ff4d4f'
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
        actions.push({ name: '付款', type: 'primary' });
        actions.push({ name: '取消订单', type: 'danger' });
        break;
      case OrderStatus.SHIPPED:
        actions.push({ name: '确认收货', type: 'primary' });
        break;
      case OrderStatus.COMPLETED:
        actions.push({ name: '评价', type: 'primary' });
        break;
    }
  } else if (role === 'seller') {
    switch (status) {
      case OrderStatus.PAID:
      case OrderStatus.PENDING_SHIPMENT:
        actions.push({ name: '发货', type: 'primary' });
        break;
      case OrderStatus.SHIPPED:
        actions.push({ name: '查看物流', type: 'info' });
        break;
    }
  }
  
  return actions;
};

export const getOrderStatusProgress = (status) => {
  const steps = [
    { key: OrderStatus.PENDING_PAYMENT, title: '待付款' },
    { key: OrderStatus.PAID, title: '已付款' },
    { key: OrderStatus.PENDING_SHIPMENT, title: '待发货' },
    { key: OrderStatus.SHIPPED, title: '已发货' },
    { key: OrderStatus.DELIVERED, title: '已送达' },
    { key: OrderStatus.COMPLETED, title: '已完成' }
  ];
  
  const currentIndex = steps.findIndex(step => step.key === status);
  return {
    steps,
    current: currentIndex === -1 ? 0 : currentIndex
  };
};

export const getOrderHint = (status, role) => {
  const hints = {
    buyer: {
      [OrderStatus.PENDING_PAYMENT]: '请及时付款，超时订单将自动取消',
      [OrderStatus.SHIPPED]: '商品已发货，请注意查收',
      [OrderStatus.DELIVERED]: '商品已送达，请确认收货'
    },
    seller: {
      [OrderStatus.PAID]: '买家已付款，请及时发货',
      [OrderStatus.PENDING_SHIPMENT]: '请尽快发货，避免影响买家体验'
    }
  };
  
  return hints[role]?.[status] || '';
};

export const normalizeOrder = (order) => {
  if (!order) return null;
  return {
    ...order,
    orderStatus: order.orderStatus || OrderStatus.PENDING_PAYMENT,
    price: order.price || 0,
    createdAt: order.createdAt || new Date().toISOString()
  };
};

export const sanitizeOrderStatus = (status) => {
  return Object.values(OrderStatus).includes(status) ? status : OrderStatus.PENDING_PAYMENT;
};

export const sanitizeOrderView = (view) => {
  return ['buyer', 'seller'].includes(view) ? view : 'buyer';
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
    { label: '已退款', value: OrderStatus.REFUNDED }
  ];
};

export const getOrderStatusClass = (status) => {
  const classes = {
    [OrderStatus.PENDING_PAYMENT]: 'status-pending',
    [OrderStatus.PAID]: 'status-paid',
    [OrderStatus.PENDING_SHIPMENT]: 'status-processing',
    [OrderStatus.SHIPPED]: 'status-shipped',
    [OrderStatus.DELIVERED]: 'status-delivered',
    [OrderStatus.COMPLETED]: 'status-completed',
    [OrderStatus.CANCELLED]: 'status-cancelled',
    [OrderStatus.REFUNDED]: 'status-refunded'
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
  getOrderStatusClass
};
