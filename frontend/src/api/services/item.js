import instance from '../config/axios';
import requestManager from '../../utils/network/requestManager';
import { API_PATHS } from '../config/paths';

// 缓存时间配置（毫秒）
const CACHE_EXPIRY = {
  SHORT: 2 * 60 * 1000, // 2分钟
  MEDIUM: 5 * 60 * 1000, // 5分钟
  LONG: 10 * 60 * 1000, // 10分钟
};

const item = {
  getItems: (params) =>
    requestManager.request(API_PATHS.ITEM.LIST, () => instance.get(API_PATHS.ITEM.LIST, { params }), {
      useCache: true,
      useMerge: true,
      params,
      cacheExpiry: CACHE_EXPIRY.MEDIUM,
    }),
  getHotItems: () =>
    requestManager.request(API_PATHS.ITEM.HOT, () => instance.get(API_PATHS.ITEM.HOT), {
      useCache: true,
      useMerge: true,
      cacheExpiry: CACHE_EXPIRY.LONG,
    }),
  searchItems: (keyword, page, size, sortBy) =>
    requestManager.request(
      API_PATHS.ITEM.SEARCH,
      () =>
        instance.get(API_PATHS.ITEM.SEARCH, {
          params: { keyword, page, size, sortBy },
        }),
      {
        useCache: true,
        useMerge: true,
        params: { keyword, page, size, sortBy },
        cacheExpiry: CACHE_EXPIRY.SHORT,
      }
    ),
  getItem: (id) =>
    requestManager.request(API_PATHS.ITEM.DETAIL(id), () => instance.get(API_PATHS.ITEM.DETAIL(id)), {
      useCache: true,
      useMerge: true,
      params: { id },
      cacheExpiry: CACHE_EXPIRY.MEDIUM,
    }),
  createItem: (data) => instance.post(API_PATHS.ITEM.CREATE, data),
  updateItem: (id, data) => instance.put(API_PATHS.ITEM.UPDATE(id), data),
  offShelf: (id) => instance.put(API_PATHS.ITEM.OFF_SHELF(id)),
  uploadImage: (data) => instance.post(API_PATHS.ITEM.UPLOAD, data),
  uploadChunk: (data) => instance.post(API_PATHS.ITEM.UPLOAD_CHUNK, data),
  completeUpload: (data) => instance.post(API_PATHS.ITEM.UPLOAD_COMPLETE, data),
  checkUploadedChunks: (fileHash, uploadId) =>
    instance.get(API_PATHS.ITEM.UPLOAD_CHECK, { params: { fileHash, uploadId } }),
};

export default item;
