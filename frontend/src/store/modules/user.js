import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import api from '../../api';
import { setToken, clearToken, getToken } from '../../api/config/axios';
import storage from '../../utils/storage';
import { ErrorHandler } from '../../utils/error';

const storageInstance = storage('user');

export const useUserStore = defineStore('user', () => {
  const refreshToken = ref(storageInstance.get('refreshToken') || '');
  const user = ref(storageInstance.get('user'));
  const lastLoginTime = ref(storageInstance.get('lastLoginTime'));
  const rememberMe = ref(storageInstance.get('rememberMe') || false);
  const loading = ref(false);
  const isLoggedIn = ref(!!getToken());
  const isAdmin = computed(() => user.value?.role === 'ADMIN' || user.value?.role === 'SUPER_ADMIN');
  const isVerified = computed(() => user.value?.verified === true);

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
        isLoggedIn.value = true;

        storageInstance.set('refreshToken', newRefreshToken);
        storageInstance.set('user', userInfo);
        storageInstance.set('lastLoginTime', lastLoginTime.value);
        storageInstance.set('rememberMe', rememberMe.value);

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
    clearToken();
    refreshToken.value = '';
    user.value = null;
    lastLoginTime.value = null;
    isLoggedIn.value = false;

    storageInstance.remove('refreshToken');
    storageInstance.remove('user');
    storageInstance.remove('lastLoginTime');

    if (!rememberMe.value) {
      storageInstance.remove('rememberMe');
    }
  };

  const getCurrentUser = async () => {
    if (!getToken()) return null;
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

  return {
    refreshToken,
    user,
    lastLoginTime,
    rememberMe,
    loading,
    isLoggedIn,
    isAdmin,
    isVerified,
    setRefreshToken,
    login,
    register,
    logout,
    getCurrentUser,
    updateProfile,
  };
});
