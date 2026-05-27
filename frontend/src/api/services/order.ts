import { get, post } from '../config/http';
import type { OrderInfo, CreateOrderRequest } from '../../types/api';

const order = {
  createOrder: (data: CreateOrderRequest) => post<OrderInfo>('/orders', data),
  getBuyerOrders: (params?: Record<string, any>) =>
    get<any>('/orders', { params }),
  getSellerOrders: (params?: Record<string, any>) =>
    get<any>('/orders/seller', { params }),
  getOrder: (id: number | string) => get<OrderInfo>(`/orders/${id}`),
  payOrder: (id: number | string, paymentMethod: string = 'OFFLINE') =>
    post<null>(`/orders/${id}/pay`, null, { params: { paymentMethod } }),
  cancelOrder: (id: number | string, reason: string = '用户主动取消') =>
    post<null>(`/orders/${id}/cancel`, { reason }),
  shipOrder: (id: number | string) => post<null>(`/orders/${id}/ship`),
  updateShippingInfo: (id: number | string, trackingNumber: string, shippingCompany: string) =>
    post<null>(`/orders/${id}/shipping`, null, {
      params: { trackingNumber, shippingCompany },
    }),
  confirmReceive: (id: number | string) => post<null>(`/orders/${id}/confirm-receive`),
  applyRefund: (id: number | string, data: { reason: string; description?: string; evidenceImages?: string[] }) =>
    post<null>(`/orders/${id}/refund`, data),
};

export default order;
