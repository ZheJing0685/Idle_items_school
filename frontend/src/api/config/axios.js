import axios from 'axios';
import router from '../../router';
import ErrorHandler from './errorHandler';

const baseURL = '/api';

const instance = axios.create({
  baseURL,
  timeout: 15000,
  timeoutErrorMessage: '请求超时，请稍后重试',
  withCredentials: true, // 允许携带cookie
});

let memoryToken = '';
let unauthorizedHandler = null;

export const setUnauthorizedHandler = (handler) => {
  unauthorizedHandler = handler;
};

// Cookie操作工具函数
const setCookie = (name, value, options = {}) => {
  let cookie = `${name}=${encodeURIComponent(value)}`;
  
  if (options.expires) {
    const date = new Date();
    date.setTime(date.getTime() + (options.expires * 24 * 60 * 60 * 1000));
    cookie += `; expires=${date.toUTCString()}`;
  }
  
  if (options.path) {
    cookie += `; path=${options.path}`;
  }
  
  if (options.domain) {
    cookie += `; domain=${options.domain}`;
  }
  
  if (options.secure) {
    cookie += '; secure';
  }
  
  document.cookie = cookie;
};

const getCookie = (name) => {
  const cookieName = `${name}=`;
  const decodedCookie = decodeURIComponent(document.cookie);
  const cookieArray = decodedCookie.split(';');
  
  for (let i = 0; i < cookieArray.length; i++) {
    let cookie = cookieArray[i];
    while (cookie.charAt(0) === ' ') {
      cookie = cookie.substring(1);
    }
    if (cookie.indexOf(cookieName) === 0) {
      return cookie.substring(cookieName.length, cookie.length);
    }
  }
  return '';
};

const deleteCookie = (name) => {
  document.cookie = `${name}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;`;
};

const initToken = () => {
  // 从cookie中获取 token
  const storedToken = getCookie('user:token');
  if (storedToken) {
    memoryToken = storedToken;
  }
};

initToken();

instance.interceptors.request.use(
  (config) => {
    if (memoryToken) {
      config.headers.Authorization = `Bearer ${memoryToken}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

instance.interceptors.response.use(
  (response) => {
    return response.data;
  },
  async (error) => {
    if (error.response) {
      const { status, data } = error.response;

      switch (status) {
        case 401:
          if (unauthorizedHandler) {
            unauthorizedHandler();
          } else {
            ErrorHandler.clearAuthStorage();
            import('element-plus').then(({ ElMessageBox }) => {
              ElMessageBox.alert('登录已过期，请重新登录', '提示', {
                confirmButtonText: '确定',
                callback: () => {
                  router.push('/login');
                },
              });
            });
          }
          break;
        case 403:
          ErrorHandler.showErrorMessage({
            type: 'AUTHORIZATION_ERROR',
            message: data?.message || '权限不足',
          });
          break;
        case 404:
          ErrorHandler.showErrorMessage({
            type: 'NOT_FOUND_ERROR',
            message: data?.message || '资源不存在',
          });
          break;
        case 500:
          ErrorHandler.showErrorMessage({
            type: 'SERVER_ERROR',
            message: data?.message || '服务器错误，请稍后重试',
          });
          break;
        default:
          ErrorHandler.showErrorMessage({
            type: 'CLIENT_ERROR',
            message: data?.message || '请求失败',
          });
          break;}

      return Promise.reject(data || error);
    }

    if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      ErrorHandler.showErrorMessage({
        type: 'TIMEOUT_ERROR',
        message: '请求超时，请稍后重试',
      });
    } else if (error.message?.includes('Network Error')) {
      ErrorHandler.showErrorMessage({
        type: 'NETWORK_ERROR',
        message: '网络连接失败，请检查网络',
      });
    } else {
      ErrorHandler.showErrorMessage({
        type: 'UNKNOWN_ERROR',
        message: error.message || '网络异常，请稍后重试',
      });
    }

    return Promise.reject(error);
  }
);

export const setToken = (token) => {
  memoryToken = token;
  setCookie('user:token', token, {
    expires: 7,
    path: '/',
    secure: window.location.protocol === 'https:'
  });
};

export const getToken = () => memoryToken;

export const clearToken = () => {
  memoryToken = '';
  deleteCookie('user:token');
  ErrorHandler.clearAuthStorage();
};

export default instance;
