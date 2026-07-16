import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import dictService from '../api/services/dict';
import { logger } from '@/utils/logger';
import type { DictItem } from '../types/api';

interface DictRecord {
  [typeCode: string]: DictItem[]
}

export const useDictStore = defineStore('dict', () => {
  const dicts = ref<DictRecord>({});
  const loading = ref(false);
  const error = ref<string | null>(null);
  const lastFetchTime = ref<number | null>(null);

  const CACHE_TTL = 24 * 60 * 60 * 1000;

  const isCacheValid = computed(() => {
    if (!lastFetchTime.value) return false;
    return Date.now() - lastFetchTime.value < CACHE_TTL;
  });

  async function fetchAllDicts(forceRefresh = false) {
    if (
      !forceRefresh &&
      isCacheValid.value &&
      Object.keys(dicts.value).length > 0
    ) {
      return dicts.value;
    }

    loading.value = true;
    error.value = null;

    try {
      const response: any = await dictService.getAllDicts();
      dicts.value = response.data;
      lastFetchTime.value = Date.now();
      return dicts.value;
    } catch (err: any) {
      error.value = (err as Error).message || '获取字典数据失败';
      logger.error('获取字典数据失败:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function fetchDictByType(typeCode: string) {
    if (dicts.value[typeCode]) {
      return dicts.value[typeCode];
    }

    loading.value = true;
    error.value = null;

    try {
      const response: any = await dictService.getDictByType(typeCode);
      dicts.value[typeCode] = response.data;
      return response.data;
    } catch (err: any) {
      error.value = (err as Error).message || `获取字典类型 ${typeCode} 失败`;
      logger.error(`获取字典类型 ${typeCode} 失败:`, err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  function getDictLabel(typeCode: string, itemValue: string) {
    if (!dicts.value[typeCode] || !itemValue) {
      return itemValue || '';
    }

    const items = dicts.value[typeCode];
    const item = items.find((i) => i.value === itemValue);
    return item ? item.label : itemValue;
  }

  function getDictLabelSync(typeCode: string, itemValue: string) {
    const items = dicts.value[typeCode];
    if (!items) return itemValue;
    const item = items.find((i) => i.value === itemValue);
    return item ? item.label : itemValue;
  }

  function getDictOptions(typeCode: string) {
    if (!dicts.value[typeCode]) {
      return [];
    }

    return dicts.value[typeCode].map((item) => ({
      value: item.value,
      label: item.label,
    }));
  }

  function getDictCssClass(typeCode: string, itemValue: string) {
    if (!dicts.value[typeCode] || !itemValue) {
      return '';
    }

    const items = dicts.value[typeCode];
    const item = items.find((i) => i.value === itemValue);
    return item ? item.cssClass || '' : '';
  }

  function clearCache() {
    dicts.value = {};
    lastFetchTime.value = null;
  }

  async function refreshDicts() {
    return await fetchAllDicts(true);
  }

  async function preloadCommonDicts() {
    const commonTypes = [
      'ITEM_CONDITION',
      'DELIVERY_METHOD',
      'ITEM_STATUS',
      'ORDER_STATUS',
      'CONTACT_TYPE',
    ];

    const promises = commonTypes.map((type) => fetchDictByType(type));
    await Promise.allSettled(promises);
  }

  return {
    dicts,
    loading,
    error,
    lastFetchTime,
    isCacheValid,
    fetchAllDicts,
    fetchDictByType,
    getDictLabel,
    getDictLabelSync,
    getDictOptions,
    getDictCssClass,
    clearCache,
    refreshDicts,
    preloadCommonDicts,
  };
});
