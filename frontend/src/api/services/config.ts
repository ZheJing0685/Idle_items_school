import { get, post, del } from '../config/http';
import type { SystemConfig } from '../../types/api';

const configService = {
  getAllConfigs() {
    return get<SystemConfig[]>('/configs');
  },

  getConfig(configKey: string) {
    return get<SystemConfig>(`/configs/${configKey}`);
  },

  getConfigsByGroup(groupName: string) {
    return get<SystemConfig[]>(`/configs/group/${groupName}`);
  },

  saveConfig(configKey: string, configValue: string, description?: string) {
    return post<SystemConfig>('/configs', null, { params: { configKey, configValue, description } });
  },

  deleteConfig(configKey: string) {
    return del<void>(`/configs/${configKey}`);
  },

  clearConfigCache() {
    return post<void>('/configs/cache/clear');
  },

  reloadConfigCache() {
    return post<void>('/configs/cache/reload');
  },
};

export default configService;
