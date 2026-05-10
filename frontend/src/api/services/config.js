import apiClient from '../config/axios.js';

const configService = {
  /**
   * 获取所有配置
   */
  async getAllConfigs() {
    try {
      const response = await apiClient.get('/configs');
      return response.data;
    } catch (error) {
      console.error('获取所有配置失败:', error);
      throw error;
    }
  },

  /**
   * 获取指定配置值
   */
  async getConfig(configKey) {
    try {
      const response = await apiClient.get(`/configs/${configKey}`);
      return response.data;
    } catch (error) {
      console.error(`获取配置 ${configKey} 失败:`, error);
      throw error;
    }
  },

  /**
   * 获取指定分组的配置
   */
  async getConfigsByGroup(groupName) {
    try {
      const response = await apiClient.get(`/configs/group/${groupName}`);
      return response.data;
    } catch (error) {
      console.error(`获取配置分组 ${groupName} 失败:`, error);
      throw error;
    }
  },

  /**
   * 保存或更新配置
   */
  async saveConfig(configKey, configValue, description) {
    try {
      const response = await apiClient.post('/configs', null, {
        params: { configKey, configValue, description },
      });
      return response.data;
    } catch (error) {
      console.error(`保存配置 ${configKey} 失败:`, error);
      throw error;
    }
  },

  /**
   * 删除配置
   */
  async deleteConfig(configKey) {
    try {
      await apiClient.delete(`/configs/${configKey}`);
    } catch (error) {
      console.error(`删除配置 ${configKey} 失败:`, error);
      throw error;
    }
  },

  /**
   * 清除配置缓存
   */
  async clearConfigCache() {
    try {
      await apiClient.post('/configs/cache/clear');
    } catch (error) {
      console.error('清除配置缓存失败:', error);
      throw error;
    }
  },

  /**
   * 重新加载配置缓存
   */
  async reloadConfigCache() {
    try {
      await apiClient.post('/configs/cache/reload');
    } catch (error) {
      console.error('重新加载配置缓存失败:', error);
      throw error;
    }
  },
};

export default configService;
