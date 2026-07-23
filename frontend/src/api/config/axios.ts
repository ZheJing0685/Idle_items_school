import axios, { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse, AxiosError } from 'axios';
import { ElMessageBox } from 'element-plus';
import router from '../../router';
import { ErrorHandler } from '../../utils/error';
import type { ApiResponse } from '../../types/api';

const baseURL = import.meta.env.VITE_API_BASE_URL || '/api';

const instance: AxiosInstance = axios.create({
  baseURL,
  timeout: 15000,
  timeoutErrorMessage: '请求超时，请稍后重试',
  withCredentials: true, // 关键：自动携带 Cookie
});

const refreshInstance: AxiosInstance = axios.create({
  baseURL,
  timeout: 15000,
  withCredentials: true,
});

let unauthorizedHandler: (() => void) | null = null;

export const setUnauthorizedHandler = (handler: () => void): void => {
  unauthorizedHandler = handler;
};

instance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // Token 由 HttpOnly Cookie 自动携带，无需手动设置 Authorization
    return config;
  },
  (error: AxiosError) => Promise.reject(error),
);

instance.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    return response.data as unknown as AxiosResponse<ApiResponse>;
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };
    if (!originalRequest) return Promise.reject(error);

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      // 尝试通过 refresh 接口自动续期，Refresh Token 由 HttpOnly Cookie 自动携带。
      try {
        const response = await refreshInstance.post<ApiResponse>('/auth/refresh');
        if (response.data.code === 200) {
          return instance(originalRequest);
        }
      } catch {
        // 刷新失败，继续走登出逻辑
      }

      clearAuthState();
      if (unauthorizedHandler) {
        unauthorizedHandler();
      } else {
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

/** 
 * 清除客户端认证相关的所有本地状态。
 * 注意：HttpOnly Cookie 无法从 JS 端清除，由后端 /auth/logout 接口负责。
 */
export function clearAuthState(): void {
  // 清除重定向路径缓存
  localStorage.removeItem('redirectPath');

  // 清除应用命名空间下的所有缓存数据
  try {
    const keys = Object.keys(localStorage);
    const namespace = 'app';
    keys.forEach((key) => {
      if (key.startsWith(`${namespace}:`)) {
        localStorage.removeItem(key);
      }
    });
  } catch {
    // localStorage 可能在隐私模式下不可用
  }

  // 清除 sessionStorage 中的所有数据
  try {
    sessionStorage.clear();
  } catch {
    // sessionStorage 可能在隐私模式下不可用
  }
}

// 兼容旧调用：认证状态现在只以 /auth/me 的服务端校验结果为准。
export const clearCookies = clearAuthState;

export default instance;
