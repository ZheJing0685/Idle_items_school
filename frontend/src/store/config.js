import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import configService from '../api/services/config.js';

export const useConfigStore = defineStore('config', () => {
  // 状态
  const configs = ref({});
  const loading = ref(false);
  const error = ref(null);
  const lastFetchTime = ref(null);

  // 缓存过期时间（1小时）
  const CACHE_TTL = 60 * 60 * 1000;

  // 计算属性
  const isCacheValid = computed(() => {
    if (!lastFetchTime.value) return false;
    return Date.now() - lastFetchTime.value < CACHE_TTL;
  });

  // 获取所有配置
  async function fetchAllConfigs(forceRefresh = false) {
    // 如果缓存有效且不强制刷新，直接返回缓存数据
    if (
      !forceRefresh &&
      isCacheValid.value &&
      Object.keys(configs.value).length > 0
    ) {
      return configs.value;
    }

    loading.value = true;
    error.value = null;

    try {
      const data = await configService.getAllConfigs();
      configs.value = data;
      lastFetchTime.value = Date.now();
      return data;
    } catch (err) {
      error.value = err.message || '获取配置数据失败';
      console.error('获取配置数据失败:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // 获取指定配置值
  async function fetchConfig(configKey) {
    // 如果已有缓存，直接返回
    if (configs.value[configKey] !== undefined) {
      return configs.value[configKey];
    }

    loading.value = true;
    error.value = null;

    try {
      const data = await configService.getConfig(configKey);
      configs.value[configKey] = data;
      return data;
    } catch (err) {
      error.value = err.message || `获取配置 ${configKey} 失败`;
      console.error(`获取配置 ${configKey} 失败:`, err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // 同步获取配置值（用于模板渲染）
  function getConfigSync(configKey, defaultValue = null) {
    const value = configs.value[configKey];
    return value !== undefined ? value : defaultValue;
  }

  // 获取配置值（字符串）
  function getConfigString(configKey, defaultValue = '') {
    const value = configs.value[configKey];
    return value !== undefined ? String(value) : defaultValue;
  }

  // 获取配置值（整数）
  function getConfigInt(configKey, defaultValue = 0) {
    const value = configs.value[configKey];
    if (value !== undefined) {
      const intValue = parseInt(value, 10);
      return isNaN(intValue) ? defaultValue : intValue;
    }
    return defaultValue;
  }

  // 获取配置值（浮点数）
  function getConfigFloat(configKey, defaultValue = 0.0) {
    const value = configs.value[configKey];
    if (value !== undefined) {
      const floatValue = parseFloat(value);
      return isNaN(floatValue) ? defaultValue : floatValue;
    }
    return defaultValue;
  }

  // 获取配置值（布尔）
  function getConfigBoolean(configKey, defaultValue = false) {
    const value = configs.value[configKey];
    if (value !== undefined) {
      if (typeof value === 'boolean') {
        return value;
      }
      return String(value).toLowerCase() === 'true';
    }
    return defaultValue;
  }

  // 获取指定分组的配置
  async function fetchConfigsByGroup(groupName) {
    loading.value = true;
    error.value = null;

    try {
      const data = await configService.getConfigsByGroup(groupName);
      // 将分组配置合并到主配置中
      Object.assign(configs.value, data);
      return data;
    } catch (err) {
      error.value = err.message || `获取配置分组 ${groupName} 失败`;
      console.error(`获取配置分组 ${groupName} 失败:`, err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // 清除配置缓存
  function clearCache() {
    configs.value = {};
    lastFetchTime.value = null;
  }

  // 强制刷新配置数据
  async function refreshConfigs() {
    return await fetchAllConfigs(true);
  }

  return {
    // 状态
    configs,
    loading,
    error,
    lastFetchTime,

    // 计算属性
    isCacheValid,

    // 方法
    fetchAllConfigs,
    fetchConfig,
    getConfigSync,
    getConfigString,
    getConfigInt,
    getConfigFloat,
    getConfigBoolean,
    fetchConfigsByGroup,
    clearCache,
    refreshConfigs,
  };
});
