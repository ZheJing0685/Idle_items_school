import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import api from '../../api';
import { clearAuthState } from '../../api/config/axios';
import { ErrorHandler } from '../../utils/error';
import type { UserInfo, RegisterRequest } from '../../types/api';

export const useUserStore = defineStore('user', () => {
  const user = ref<UserInfo | null>(null);
  const lastLoginTime = ref<string | null>(null);
  const rememberMe = ref(false);
  const loading = ref(false);
  const isLoggedIn = ref(false);
  const isAdmin = computed(() => user.value?.role === 'ADMIN');
  const isVerified = computed(() => user.value?.verified === true);
  let restorePromise: Promise<boolean> | null = null;

  const clearLocalState = () => {
    clearAuthState();
    user.value = null;
    lastLoginTime.value = null;
    isLoggedIn.value = false;
  };

  const login = async (username: string, password: string, remember?: boolean) => {
    loading.value = true;
    try {
      const response = await api.auth.login({ username, password });
      if (response.code === 200 && response.data) {
        // Cookie 已由后端自动设置，无需前端处理 token
        user.value = response.data.user;
        lastLoginTime.value = new Date().toISOString();
        rememberMe.value = remember || false;
        isLoggedIn.value = true;
        return response;
      }
      throw new Error(response.message || '登录失败');
    } catch (error: any) {
      ErrorHandler.handle(error, { silent: true });
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
        // Cookie 已由后端自动设置
        user.value = response.data.user;
        lastLoginTime.value = new Date().toISOString();
        isLoggedIn.value = true;
        return response;
      }
      throw new Error(response.message || '注册失败');
    } catch (error: any) {
      ErrorHandler.handle(error, { silent: true });
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const logout = async () => {
    try {
      // 调用后端登出接口，使 Token 失效并清除 HttpOnly Cookie
      await api.auth.logout();
    } catch {
      // 即使后端调用失败，也要清除本地状态
    } finally {
      clearLocalState();
    }
  };

  const getCurrentUser = async (): Promise<UserInfo | null> => {
    try {
      const response = await api.auth.getCurrentUser();
      if (response.code === 200) {
        user.value = response.data;
        isLoggedIn.value = true;
        return response.data;
      }
      return null;
    } catch (error: any) {
      const httpErr = error as { response?: { status?: number }; code?: number };
      if (httpErr?.response?.status === 401 || httpErr?.code === 401) {
        clearLocalState();
      }
      return null;
    }
  };

  const restoreSession = async (): Promise<boolean> => {
    if (restorePromise) return restorePromise;

    restorePromise = (async () => {
      const currentUser = await getCurrentUser();
      if (currentUser) return true;

      try {
        const refreshResponse = await api.auth.refreshToken();
        if (refreshResponse.code !== 200) {
          clearLocalState();
          return false;
        }

        const refreshedUser = await getCurrentUser();
        if (refreshedUser) return true;
      } catch {
        // Cookie 会话不可恢复，落回未登录态。
      }

      clearLocalState();
      return false;
    })();

    try {
      return await restorePromise;
    } finally {
      restorePromise = null;
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
    } catch (error: any) {
      ErrorHandler.handle(error, { silent: true });
      throw error;
    }
  };

  return {
    user, lastLoginTime, rememberMe, loading,
    isLoggedIn, isAdmin, isVerified,
    login, register, logout, getCurrentUser, restoreSession, updateProfile,
  };
});
