import instance from '../config/axios';

const order = {
  createOrder: (data) => instance.post('/orders', data),
  getBuyerOrders: (params) => instance.get('/orders', { params }),
  getSellerOrders: (params) => instance.get('/orders/seller', { params }),
  getOrder: (id) => instance.get(`/orders/${id}`),
  payOrder: (id, paymentMethod = 'OFFLINE') =>
    instance.post(`/orders/${id}/pay`, null, {
      params: { paymentMethod },
    }),
  cancelOrder: (id, reason = '用户主动取消') =>
    instance.post(`/orders/${id}/cancel`, { reason }),
  shipOrder: (id) => instance.post(`/orders/${id}/ship`),
  updateShippingInfo: (id, trackingNumber, shippingCompany) =>
    instance.post(`/orders/${id}/shipping`, null, {
      params: { trackingNumber, shippingCompany },
    }),
  confirmReceive: (id) => instance.post(`/orders/${id}/confirm-receive`),
  applyRefund: (id, data) => instance.post(`/orders/${id}/refund`, data),
};

export default order;
