import instance from './axios';
import type { ApiResponse } from '../../types/api';

// 拦截器已将 response.data 直接返回，instance.get/post 返回的就是 ApiResponse
export function get<T = unknown>(url: string, config?: Record<string, unknown>): Promise<ApiResponse<T>> {
  return instance.get(url, config) as unknown as Promise<ApiResponse<T>>;
}

export function post<T = unknown>(url: string, data?: unknown, config?: Record<string, unknown>): Promise<ApiResponse<T>> {
  return instance.post(url, data, config) as unknown as Promise<ApiResponse<T>>;
}

export function put<T = unknown>(url: string, data?: unknown, config?: Record<string, unknown>): Promise<ApiResponse<T>> {
  return instance.put(url, data, config) as unknown as Promise<ApiResponse<T>>;
}

export function del<T = unknown>(url: string, config?: Record<string, unknown>): Promise<ApiResponse<T>> {
  return instance.delete(url, config) as unknown as Promise<ApiResponse<T>>;
}

export function getBlob(url: string, config?: Record<string, unknown>): Promise<Blob> {
  return instance.get(url, { ...config, responseType: 'blob' }) as unknown as Promise<Blob>;
}
