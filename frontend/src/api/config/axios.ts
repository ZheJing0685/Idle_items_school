import axios, { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse, AxiosError } from 'axios';
import router from '../../router';
import { ErrorHandler } from '../../utils/error';
import type { ApiResponse } from '../../types/api';

const baseURL = '/api';

const instance: AxiosInstance = axios.create({
  baseURL,
  timeout: 15000,
  timeoutErrorMessage: '请求超时，请稍后重试',
});

let memoryToken = '';
let unauthorizedHandler: (() => void) | null = null;
let refreshSubscribers: Array<(token: string) => void> = [];
let isRefreshing = false;

export const setUnauthorizedHandler = (handler: () => void): void => {
  unauthorizedHandler = handler;
};

const initToken = (): void => {
  const stored = sessionStorage.getItem('access_token');
  if (stored) memoryToken = stored;
};

initToken();

instance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    if (memoryToken && config.headers) {
      config.headers.Authorization = `Bearer ${memoryToken}`;
    }
    return config;
  },
  (error: AxiosError) => Promise.reject(error),
);

const refreshAccessToken = async (): Promise<string | null> => {
  try {
    const refreshToken = sessionStorage.getItem('refresh_token');
    if (!refreshToken) throw new Error('no refresh token');
    const { default: api } = await import('../../api');
    const response: ApiResponse<{ token: string; refreshToken?: string }> = await api.auth.refreshToken(refreshToken);
    if (response.code === 200 && response.data?.token) {
      setToken(response.data.token);
      if (response.data.refreshToken) {
        sessionStorage.setItem('refresh_token', response.data.refreshToken);
      }
      return response.data.token;
    }
    throw new Error('refresh failed');
  } catch {
    clearToken();
    return null;
  }
};

const onRefreshed = (token: string): void => {
  refreshSubscribers.forEach((cb) => cb(token));
  refreshSubscribers = [];
};

const addRefreshSubscriber = (cb: (token: string) => void): void => {
  if (refreshSubscribers.length < 100) refreshSubscribers.push(cb);
};

instance.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    return response.data as any;
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };
    if (!originalRequest) return Promise.reject(error);

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve) => {
          addRefreshSubscriber((token: string) => {
            if (originalRequest.headers) {
              originalRequest.headers.Authorization = `Bearer ${token}`;
            }
            resolve(instance(originalRequest));
          });
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      const newToken = await refreshAccessToken();
      isRefreshing = false;

      if (newToken) {
        onRefreshed(newToken);
        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${newToken}`;
        }
        return instance(originalRequest);
      }

      clearToken();
      if (unauthorizedHandler) {
        unauthorizedHandler();
      } else {
        const { ElMessageBox } = await import('element-plus');
        ElMessageBox.alert('登录已过期，请重新登录', '提示', {
          confirmButtonText: '确定',
          callback: () => router.push('/login'),
        });
      }
      return Promise.reject(error);
    }

    if (error.response) {
      const { status, data } = error.response;
      const responseData = data as { message?: string } | undefined;
      switch (status) {
      case 403:
        ErrorHandler.showErrorMessage({ type: 'AUTHORIZATION_ERROR', message: responseData?.message || '权限不足' });
        break;
      case 404:
        ErrorHandler.showErrorMessage({ type: 'NOT_FOUND_ERROR', message: responseData?.message || '资源不存在' });
        break;
      case 500:
        ErrorHandler.showErrorMessage({ type: 'SERVER_ERROR', message: responseData?.message || '服务器错误，请稍后重试' });
        break;
      default:
        ErrorHandler.showErrorMessage({ type: 'CLIENT_ERROR', message: responseData?.message || '请求失败' });
        break;
      }
      return Promise.reject(data || { message: '请求失败' });
    }

    if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      ErrorHandler.showErrorMessage({ type: 'TIMEOUT_ERROR', message: '请求超时，请稍后重试' });
    } else if (error.message?.includes('Network Error')) {
      ErrorHandler.showErrorMessage({ type: 'NETWORK_ERROR', message: '网络连接失败，请检查网络' });
    } else {
      ErrorHandler.showErrorMessage({ type: 'UNKNOWN_ERROR', message: error.message || '网络异常，请稍后重试' });
    }
    return Promise.reject(error);
  },
);

export const setToken = (token: string): void => {
  memoryToken = token;
  sessionStorage.setItem('access_token', token);
};

export const getToken = (): string => memoryToken;

export const clearToken = (): void => {
  memoryToken = '';
  sessionStorage.removeItem('access_token');
  sessionStorage.removeItem('refresh_token');
};

export default instance;
