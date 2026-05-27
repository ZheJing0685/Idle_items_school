import { get, post } from '../config/http';
import type { DictType, DictItem } from '../../types/api';

const dictService = {
  getAllDicts() {
    return get<DictType[]>('/dicts/all');
  },

  getDictByType(typeCode: string) {
    return get<DictItem[]>(`/dicts/${typeCode}`);
  },

  getDictLabel(typeCode: string, itemValue: string) {
    return get<string>('/dicts/label', { params: { typeCode, itemValue } });
  },

  getDictOptions(typeCode: string) {
    return get<{ value: string; label: string }[]>(`/dicts/${typeCode}/options`);
  },

  clearDictCache() {
    return post<void>('/dicts/cache/clear');
  },

  reloadDictCache(typeCode: string) {
    return post<void>(`/dicts/${typeCode}/cache/reload`);
  },
};

export default dictService;
