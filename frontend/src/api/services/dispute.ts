import { get, post } from '../config/http';

interface DisputeListParams {
  page?: number
  size?: number
  status?: string
  [key: string]: unknown
}

const dispute = {
  list: (params?: DisputeListParams) =>
    get<unknown>('/disputes', { params }),
  get: (id: number | string) =>
    get<unknown>(`/disputes/${id}`),
  getByOrder: (orderId: number | string) =>
    get<unknown>(`/disputes/order/${orderId}`),
  canDispute: (orderId: number | string) =>
    get<unknown>(`/disputes/can-dispute/${orderId}`),
  create: (data: Record<string, unknown>) =>
    post<unknown>('/disputes', data),
  reply: (id: number | string, data: Record<string, unknown>) =>
    post<unknown>(`/disputes/${id}/reply`, data),
  satisfaction: (id: number | string, data: Record<string, unknown>) =>
    post<unknown>(`/disputes/${id}/satisfaction`, data),
};

export default dispute;
