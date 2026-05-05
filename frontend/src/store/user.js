import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import api, { setToken, clearToken } from '../api';
import storage from '../utils/storage';
import ErrorHandler from '../utils/errorHandler';

const userStore = defineStore('user', () => {
  // 存储配置
  const storageType = 'persistent'; // 'persistent' | 'session' | 'cookie'
  const storageInstance = storage.getStorage(storageType);

  // 状态
  const token = ref(storageInstance.get('token') || '');
  const refreshToken = ref(storageInstance.get('refreshToken') || '');
  const user = ref(storageInstance.get('user') || null);
  const loading = ref(false);
  const lastLoginTime = ref(storageInstance.get('lastLoginTime') || null);
  const rememberMe = ref(storageInstance.get('rememberMe') || false);

  // 计算属性
  const isLoggedIn = computed(() => !!token.value);
  const isAdmin = computed(() => {
    if (!user.value || !user.value.role) return false;
    const role = user.value.role.toUpperCase();
    return role === 'ADMIN' || role === 'ROLE_ADMIN';
  });
  const loginDuration = computed(() => {
    if (!lastLoginTime.value) return 0;
    return Date.now() - new Date(lastLoginTime.value).getTime();
  });

  // 方法
  const login = async (username, password, remember = false) => {
    loading.value = true;
    try {
      const response = await api.auth.login({ username, password });
      token.value = response.data.token;
      refreshToken.value = response.data.refreshToken;
      user.value = response.data.user;
      lastLoginTime.value = new Date().toISOString();
      rememberMe.value = remember;

      // 存储数据
      setToken(token.value);
      storageInstance.set('refreshToken', refreshToken.value);
      storageInstance.set('user', user.value);
      storageInstance.set('lastLoginTime', lastLoginTime.value);
      storageInstance.set('rememberMe', rememberMe.value);

      return response;
    } finally {
      loading.value = false;
    }
  };

  const register = async (userData) => {
    loading.value = true;
    try {
      const response = await api.auth.register(userData);
      token.value = response.data.token;
      refreshToken.value = response.data.refreshToken;
      user.value = response.data.user;
      lastLoginTime.value = new Date().toISOString();

      // 存储数据
      setToken(token.value);
      storageInstance.set('refreshToken', refreshToken.value);
      storageInstance.set('user', user.value);
      storageInstance.set('lastLoginTime', lastLoginTime.value);

      return response;
    } finally {
      loading.value = false;
    }
  };

  const logout = () => {
    token.value = '';
    refreshToken.value = '';
    user.value = null;
    lastLoginTime.value = null;

    clearToken();
    storageInstance.remove('refreshToken');
    storageInstance.remove('user');
    storageInstance.remove('lastLoginTime');

    if (!rememberMe.value) {
      storageInstance.remove('rememberMe');
    }
  };

  const getCurrentUser = async () => {
    if (!token.value) return null;
    try {
      const response = await api.auth.getCurrentUser();
      user.value = response.data;
      storageInstance.set('user', user.value);
      return response.data;
    } catch (error) {
      // 处理错误
      const classifiedError = ErrorHandler.classifyError(error);

      // 只有在认证错误时才清除登录状态
      if (classifiedError.type === 'AUTHENTICATION_ERROR') {
        logout();
      }

      throw error;
    }
  };

  const checkTokenExpiry = () => {
    // 检查token是否过期
    // 实际项目中应该解析JWT token来检查过期时间
    // 这里简化处理，假设token 24小时过期
    if (!lastLoginTime.value) return true;
    const duration = loginDuration.value;
    return duration > 24 * 60 * 60 * 1000; // 24小时
  };

  const refreshTokenFn = async () => {
    try {
      const response = await api.auth.refreshToken({
        refreshToken: refreshToken.value,
      });
      token.value = response.data.token;
      lastLoginTime.value = new Date().toISOString();
      setToken(token.value);
      storageInstance.set('lastLoginTime', lastLoginTime.value);
      return token.value;
    } catch (error) {
      console.error('令牌刷新失败:', error);
      // 刷新失败时清除登录状态
      logout();
      throw error;
    }
  };

  return {
    // 状态
    token,
    refreshToken,
    user,
    loading,
    lastLoginTime,
    rememberMe,

    // 计算属性
    isLoggedIn,
    isAdmin,
    loginDuration,

    // 方法
    login,
    register,
    logout,
    getCurrentUser,
    checkTokenExpiry,
    refreshToken: refreshTokenFn,
  };
});

export default userStore;
