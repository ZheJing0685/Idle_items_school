import instance from './axios';
import type { ApiResponse } from '../../types/api';

export function get<T = any>(url: string, config?: any): Promise<ApiResponse<T>> {
  return instance.get(url, config) as any;
}

export function post<T = any>(url: string, data?: any, config?: any): Promise<ApiResponse<T>> {
  return instance.post(url, data, config) as any;
}

export function put<T = any>(url: string, data?: any, config?: any): Promise<ApiResponse<T>> {
  return instance.put(url, data, config) as any;
}

export function del<T = any>(url: string, config?: any): Promise<ApiResponse<T>> {
  return instance.delete(url, config) as any;
}

export function getBlob(url: string, config?: any): Promise<Blob> {
  return instance.get(url, { ...config, responseType: 'blob' }) as any;
}
