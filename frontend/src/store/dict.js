import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import dictService from '../api/services/dict.js';

export const useDictStore = defineStore('dict', () => {
  // 状态
  const dicts = ref({});
  const loading = ref(false);
  const error = ref(null);
  const lastFetchTime = ref(null);

  // 缓存过期时间（24小时）
  const CACHE_TTL = 24 * 60 * 60 * 1000;

  // 计算属性
  const isCacheValid = computed(() => {
    if (!lastFetchTime.value) return false;
    return Date.now() - lastFetchTime.value < CACHE_TTL;
  });

  // 获取所有字典数据
  async function fetchAllDicts(forceRefresh = false) {
    // 如果缓存有效且不强制刷新，直接返回缓存数据
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
      const data = await dictService.getAllDicts();
      dicts.value = data;
      lastFetchTime.value = Date.now();
      return data;
    } catch (err) {
      error.value = err.message || '获取字典数据失败';
      console.error('获取字典数据失败:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // 获取指定类型的字典数据
  async function fetchDictByType(typeCode) {
    // 如果已有缓存，直接返回
    if (dicts.value[typeCode]) {
      return dicts.value[typeCode];
    }

    loading.value = true;
    error.value = null;

    try {
      const data = await dictService.getDictByType(typeCode);
      dicts.value[typeCode] = data;
      return data;
    } catch (err) {
      error.value = err.message || `获取字典类型 ${typeCode} 失败`;
      console.error(`获取字典类型 ${typeCode} 失败:`, err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // 获取字典项标签
  function getDictLabel(typeCode, itemValue) {
    if (!dicts.value[typeCode] || !itemValue) {
      return itemValue || '';
    }

    const items = dicts.value[typeCode];
    const item = items.find((i) => i.value === itemValue);
    return item ? item.label : itemValue;
  }

  // 同步获取字典项标签（用于模板渲染）
  function getDictLabelSync(typeCode, itemValue) {
    if (!dicts.value[typeCode] || !itemValue) {
      return itemValue || '';
    }

    const items = dicts.value[typeCode];
    const item = items.find((i) => i.value === itemValue);
    return item ? item.label : itemValue;
  }

  // 获取字典选项列表（用于下拉框）
  function getDictOptions(typeCode) {
    if (!dicts.value[typeCode]) {
      return [];
    }

    return dicts.value[typeCode].map((item) => ({
      value: item.value,
      label: item.label,
    }));
  }

  // 获取字典项的CSS类
  function getDictCssClass(typeCode, itemValue) {
    if (!dicts.value[typeCode] || !itemValue) {
      return '';
    }

    const items = dicts.value[typeCode];
    const item = items.find((i) => i.value === itemValue);
    return item ? item.cssClass || '' : '';
  }

  // 清除字典缓存
  function clearCache() {
    dicts.value = {};
    lastFetchTime.value = null;
  }

  // 强制刷新字典数据
  async function refreshDicts() {
    return await fetchAllDicts(true);
  }

  // 预加载常用字典类型
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
    // 状态
    dicts,
    loading,
    error,
    lastFetchTime,

    // 计算属性
    isCacheValid,

    // 方法
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
