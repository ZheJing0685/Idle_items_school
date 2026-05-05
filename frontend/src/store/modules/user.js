import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { ElMessageBox } from 'element-plus';
import api from '../../api';
import storage from '../../utils/storage';
import { ErrorHandler } from '../../utils/error';

const storageInstance = storage('user');

const COOKIE_CHECK_INTERVAL = 30000;

const getCookieValue = (name) => {
  const cookieName = `${name}=`;
  const cookies = document.cookie.split(';');
  for (let i = 0; i < cookies.length; i++) {
    let cookie = cookies[i].trim();
    if (cookie.startsWith(cookieName)) {
      return decodeURIComponent(cookie.substring(cookieName.length));
    }
  }
  return '';
};

export const useUserStore = defineStore('user', () => {
  const token = ref(storageInstance.get('token') || '');
  const refreshToken = ref(storageInstance.get('refreshToken') || '');
  const user = ref(storageInstance.get('user'));
  const lastLoginTime = ref(storageInstance.get('lastLoginTime'));
  const rememberMe = ref(storageInstance.get('rememberMe') || false);
  const loading = ref(false);
  const isLoggedIn = computed(() => !!token.value);
  const isAdmin = computed(() => user.value?.role === 'ADMIN' || user.value?.role === 'SUPER_ADMIN');
  const isVerified = computed(() => user.value?.verified === true);

  let cookieCheckTimer = null;
  let isForceLoggingOut = false;

  const clearAuthData = () => {
    token.value = '';
    refreshToken.value = '';
    user.value = null;
    lastLoginTime.value = null;

    api.clearToken();
    storageInstance.remove('token');
    storageInstance.remove('refreshToken');
    storageInstance.remove('user');
    storageInstance.remove('lastLoginTime');
  };

  const checkCookieExpiry = () => {
    if (!token.value) return;

    if (getCookieValue('user:token')) return;

    if (storageInstance.get('token')) {
      api.setToken(token.value);
      return;
    }

    forceLogout();
  };

  const startCookieCheck = () => {
    stopCookieCheck();
    cookieCheckTimer = setInterval(checkCookieExpiry, COOKIE_CHECK_INTERVAL);
  };

  const stopCookieCheck = () => {
    if (cookieCheckTimer) {
      clearInterval(cookieCheckTimer);
      cookieCheckTimer = null;
    }
  };

  const forceLogout = () => {
    if (isForceLoggingOut) return;
    isForceLoggingOut = true;

    clearAuthData();
    stopCookieCheck();

    if (!rememberMe.value) {
      storageInstance.remove('rememberMe');
    }

    ElMessageBox.alert('登录已过期，请重新登录', '提示', {
      confirmButtonText: '确定',
      callback: () => {
        isForceLoggingOut = false;
        window.location.href = '/login';
      },
    });
  };

  const handleVisibilityChange = () => {
    if (document.visibilityState !== 'visible') return;
    if (isForceLoggingOut) return;
    if (!isLoggedIn.value) return;

    checkCookieExpiry();
  };

  document.addEventListener('visibilitychange', handleVisibilityChange);

  const setToken = (newToken) => {
    token.value = newToken;
    storageInstance.set('token', newToken);
    api.setToken(newToken);
  };

  const setRefreshToken = (newRefreshToken) => {
    refreshToken.value = newRefreshToken;
    storageInstance.set('refreshToken', newRefreshToken);
  };

  const login = async (username, password, remember) => {
    loading.value = true;
    try {
      const credentials = {
        username,
        password
      };
      const response = await api.auth.login(credentials);
      if (response.code === 200 && response.data) {
        const { token: newToken, refreshToken: newRefreshToken, user: userInfo } = response.data;
        setToken(newToken);
        setRefreshToken(newRefreshToken);
        user.value = userInfo;
        lastLoginTime.value = new Date().toISOString();
        rememberMe.value = remember || false;

        storageInstance.set('token', newToken);
        storageInstance.set('refreshToken', newRefreshToken);
        storageInstance.set('user', userInfo);
        storageInstance.set('lastLoginTime', lastLoginTime.value);
        storageInstance.set('rememberMe', rememberMe.value);

        startCookieCheck();

        return response;
      }
      throw new Error(response.message || '登录失败');
    } catch (error) {
      ErrorHandler.handle(error);
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const register = async (userData) => {
    loading.value = true;
    try {
      const response = await api.auth.register(userData);
      if (response.code === 200) {
        return response;
      }
      throw new Error(response.message || '注册失败');
    } catch (error) {
      ErrorHandler.handle(error);
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const logout = () => {
    clearAuthData();
    stopCookieCheck();

    if (!rememberMe.value) {
      storageInstance.remove('rememberMe');
    }
  };

  const getCurrentUser = async () => {
    if (!token.value) return null;
    try {
      const response = await api.auth.getCurrentUser();
      if (response.code === 200) {
        user.value = response.data;
        storageInstance.set('user', response.data);
        return response.data;
      }
      return null;
    } catch (error) {
      if (error.code === 401) {
        logout();
      }
      return null;
    }
  };

  const updateProfile = async (profileData) => {
    try {
      const response = await api.user.updateProfile(profileData);
      if (response.code === 200) {
        user.value = { ...user.value, ...response.data };
        storageInstance.set('user', user.value);
        return response;
      }
      throw new Error(response.message || '更新失败');
    } catch (error) {
      ErrorHandler.handle(error);
      throw error;
    }
  };

  api.setUnauthorizedHandler(forceLogout);

  if (token.value) {
    startCookieCheck();
  }

  return {
    token,
    refreshToken,
    user,
    lastLoginTime,
    rememberMe,
    loading,
    isLoggedIn,
    isAdmin,
    isVerified,
    setToken,
    setRefreshToken,
    login,
    register,
    logout,
    getCurrentUser,
    updateProfile,
  };
});
