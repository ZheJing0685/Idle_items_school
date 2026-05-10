import apiClient from '../config/axios.js';

const dictService = {
  /**
   * 获取所有字典数据
   */
  async getAllDicts() {
    try {
      const response = await apiClient.get('/dicts/all');
      return response.data;
    } catch (error) {
      console.error('获取所有字典数据失败:', error);
      throw error;
    }
  },

  /**
   * 获取指定类型的字典数据
   */
  async getDictByType(typeCode) {
    try {
      const response = await apiClient.get(`/dicts/${typeCode}`);
      return response.data;
    } catch (error) {
      console.error(`获取字典类型 ${typeCode} 失败:`, error);
      throw error;
    }
  },

  /**
   * 获取字典项标签
   */
  async getDictLabel(typeCode, itemValue) {
    try {
      const response = await apiClient.get('/dicts/label', {
        params: { typeCode, itemValue },
      });
      return response.data;
    } catch (error) {
      console.error(`获取字典标签失败 (${typeCode}:${itemValue}):`, error);
      throw error;
    }
  },

  /**
   * 获取字典选项列表（用于下拉框）
   */
  async getDictOptions(typeCode) {
    try {
      const response = await apiClient.get(`/dicts/${typeCode}/options`);
      return response.data;
    } catch (error) {
      console.error(`获取字典选项失败 (${typeCode}):`, error);
      throw error;
    }
  },

  /**
   * 清除字典缓存
   */
  async clearDictCache() {
    try {
      await apiClient.post('/dicts/cache/clear');
    } catch (error) {
      console.error('清除字典缓存失败:', error);
      throw error;
    }
  },

  /**
   * 重新加载指定类型的字典缓存
   */
  async reloadDictCache(typeCode) {
    try {
      await apiClient.post(`/dicts/${typeCode}/cache/reload`);
    } catch (error) {
      console.error(`重新加载字典缓存失败 (${typeCode}):`, error);
      throw error;
    }
  },
};

export default dictService;
