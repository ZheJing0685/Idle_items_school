import { get, post } from '../config/http';

interface DisputeListParams {
  page?: number
  size?: number
  status?: string
  [key: string]: unknown
}

const dispute = {
  list: (params?: DisputeListParams) =>
    get<any>('/disputes', { params }),
  get: (id: number | string) =>
    get<any>(`/disputes/${id}`),
  getByOrder: (orderId: number | string) =>
    get<any>(`/disputes/order/${orderId}`),
  canDispute: (orderId: number | string) =>
    get<any>(`/disputes/can-dispute/${orderId}`),
  create: (data: Record<string, unknown>) =>
    post<any>('/disputes', data),
  reply: (id: number | string, data: Record<string, unknown>) =>
    post<any>(`/disputes/${id}/reply`, data),
  satisfaction: (id: number | string, data: Record<string, unknown>) =>
    post<any>(`/disputes/${id}/satisfaction`, data),
};

export default dispute;
