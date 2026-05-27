import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import api from '../../api';
import { setToken, clearToken, getToken } from '../../api/config/axios';
import { ErrorHandler } from '../../utils/error';
import type { UserInfo, RegisterRequest } from '../../types/api';

export const useUserStore = defineStore('user', () => {
  const refreshToken = ref('');
  const user = ref<UserInfo | null>(null);
  const lastLoginTime = ref<string | null>(null);
  const rememberMe = ref(false);
  const loading = ref(false);
  const isLoggedIn = ref(!!getToken());
  const isAdmin = computed(() => user.value?.role === 'ADMIN');
  const isVerified = computed(() => user.value?.verified === true);

  const setRefreshToken = (newRefreshToken: string) => {
    refreshToken.value = newRefreshToken;
    if (newRefreshToken) {
      sessionStorage.setItem('refresh_token', newRefreshToken);
    } else {
      sessionStorage.removeItem('refresh_token');
    }
  };

  const login = async (username: string, password: string, remember?: boolean) => {
    loading.value = true;
    try {
      const response = await api.auth.login({ username, password });
      if (response.code === 200 && response.data) {
        const { token: newToken, refreshToken: newRefreshToken, user: userInfo } = response.data;
        setToken(newToken);
        setRefreshToken(newRefreshToken);
        user.value = userInfo;
        lastLoginTime.value = new Date().toISOString();
        rememberMe.value = remember || false;
        isLoggedIn.value = true;
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

  const register = async (userData: RegisterRequest) => {
    loading.value = true;
    try {
      const response = await api.auth.register(userData);
      if (response.code === 200 && response.data) {
        // 注册成功后自动登录，保存Token和用户信息
        const { token: newToken, refreshToken: newRefreshToken, user: userInfo } = response.data;
        setToken(newToken);
        setRefreshToken(newRefreshToken);
        user.value = userInfo;
        lastLoginTime.value = new Date().toISOString();
        isLoggedIn.value = true;
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

  const logout = async () => {
    try {
      // 调用后端logout接口，将当前Token加入黑名单
      if (getToken()) {
        await api.auth.logout();
      }
    } catch {
      // 即使后端调用失败，也要清除本地状态
    } finally {
      clearToken();
      refreshToken.value = '';
      user.value = null;
      lastLoginTime.value = null;
      isLoggedIn.value = false;
    }
  };

  const getCurrentUser = async (): Promise<UserInfo | null> => {
    if (!getToken()) return null;
    try {
      const response = await api.auth.getCurrentUser();
      if (response.code === 200) {
        user.value = response.data;
        return response.data;
      }
      return null;
    } catch (error: any) {
      // 401错误时登出（axios拦截器会弹框提示）
      if (error?.response?.status === 401 || error?.code === 401) {
        await logout();
      }
      return null;
    }
  };

  const updateProfile = async (profileData: Partial<UserInfo>) => {
    try {
      const response = await api.user.updateProfile(profileData);
      if (response.code === 200) {
        user.value = { ...user.value, ...response.data } as UserInfo;
        return response;
      }
      throw new Error(response.message || '更新失败');
    } catch (error) {
      ErrorHandler.handle(error);
      throw error;
    }
  };

  return {
    refreshToken, user, lastLoginTime, rememberMe, loading,
    isLoggedIn, isAdmin, isVerified,
    setRefreshToken, login, register, logout, getCurrentUser, updateProfile,
  };
});
