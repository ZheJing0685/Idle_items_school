import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import configService from '../api/services/config';

interface ConfigRecord {
  [configKey: string]: any
}

export const useConfigStore = defineStore('config', () => {
  const configs = ref<ConfigRecord>({});
  const loading = ref(false);
  const error = ref<string | null>(null);
  const lastFetchTime = ref<number | null>(null);

  const CACHE_TTL = 60 * 60 * 1000;

  const isCacheValid = computed(() => {
    if (!lastFetchTime.value) return false;
    return Date.now() - lastFetchTime.value < CACHE_TTL;
  });

  async function fetchAllConfigs(forceRefresh = false) {
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
      const response: any = await configService.getAllConfigs();
      configs.value = response.data;
      lastFetchTime.value = Date.now();
      return configs.value;
    } catch (err: any) {
      error.value = err.message || '获取配置数据失败';
      console.error('获取配置数据失败:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function fetchConfig(configKey: string) {
    if (configs.value[configKey] !== undefined) {
      return configs.value[configKey];
    }

    loading.value = true;
    error.value = null;

    try {
      const response: any = await configService.getConfig(configKey);
      configs.value[configKey] = response.data;
      return response.data;
    } catch (err: any) {
      error.value = err.message || `获取配置 ${configKey} 失败`;
      console.error(`获取配置 ${configKey} 失败:`, err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  function getConfigSync(configKey: string, defaultValue: any = null) {
    const value = configs.value[configKey];
    return value !== undefined ? value : defaultValue;
  }

  function getConfigString(configKey: string, defaultValue = '') {
    const value = configs.value[configKey];
    return value !== undefined ? String(value) : defaultValue;
  }

  function getConfigInt(configKey: string, defaultValue = 0) {
    const value = configs.value[configKey];
    if (value !== undefined) {
      const intValue = parseInt(value, 10);
      return isNaN(intValue) ? defaultValue : intValue;
    }
    return defaultValue;
  }

  function getConfigFloat(configKey: string, defaultValue = 0.0) {
    const value = configs.value[configKey];
    if (value !== undefined) {
      const floatValue = parseFloat(value);
      return isNaN(floatValue) ? defaultValue : floatValue;
    }
    return defaultValue;
  }

  function getConfigBoolean(configKey: string, defaultValue = false) {
    const value = configs.value[configKey];
    if (value !== undefined) {
      if (typeof value === 'boolean') {
        return value;
      }
      return String(value).toLowerCase() === 'true';
    }
    return defaultValue;
  }

  async function fetchConfigsByGroup(groupName: string) {
    loading.value = true;
    error.value = null;

    try {
      const response: any = await configService.getConfigsByGroup(groupName);
      Object.assign(configs.value, response.data);
      return response.data;
    } catch (err: any) {
      error.value = err.message || `获取配置分组 ${groupName} 失败`;
      console.error(`获取配置分组 ${groupName} 失败:`, err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  function clearCache() {
    configs.value = {};
    lastFetchTime.value = null;
  }

  async function refreshConfigs() {
    return await fetchAllConfigs(true);
  }

  return {
    configs,
    loading,
    error,
    lastFetchTime,
    isCacheValid,
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
