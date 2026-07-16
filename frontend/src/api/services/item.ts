import { get, post, put, del } from '../config/http';
import requestManager from '../../utils/network/requestManager';
import { API_PATHS } from '../config/paths';
import type { ItemInfo, ItemSummary, CreateItemRequest, UpdateItemRequest, PageResponse } from '../../types/api';

const CACHE_EXPIRY = {
  SHORT: 2 * 60 * 1000,
  MEDIUM: 5 * 60 * 1000,
  LONG: 10 * 60 * 1000,
};

const item = {
  getItems: (params?: Record<string, unknown>) =>
    requestManager.request(
      API_PATHS.ITEM.LIST,
      () => get<PageResponse<ItemSummary>>(API_PATHS.ITEM.LIST, { params }),
      {
        useCache: true,
        useMerge: true,
        params,
        cacheExpiry: CACHE_EXPIRY.MEDIUM,
      },
    ),
  getHotItems: () =>
    requestManager.request(
      API_PATHS.ITEM.HOT,
      () => get<ItemSummary[]>(API_PATHS.ITEM.HOT),
      {
        useCache: true,
        useMerge: true,
        cacheExpiry: CACHE_EXPIRY.LONG,
      },
    ),
  getRecommendedItems: () =>
    requestManager.request(
      API_PATHS.ITEM.RECOMMENDED,
      () => get<ItemSummary[]>(API_PATHS.ITEM.RECOMMENDED),
      {
        useCache: false,
        useMerge: true,
        cacheExpiry: CACHE_EXPIRY.SHORT,
      },
    ),
  searchItems: (keyword: string, page?: number, size?: number, sortBy?: string) =>
    requestManager.request(
      API_PATHS.ITEM.SEARCH,
      () =>
        get<PageResponse<ItemSummary>>(API_PATHS.ITEM.SEARCH, {
          params: { keyword, page, size, sortBy },
        }),
      {
        useCache: true,
        useMerge: true,
        params: { keyword, page, size, sortBy },
        cacheExpiry: CACHE_EXPIRY.SHORT,
      },
    ),
  getItem: (id: number | string) =>
    requestManager.request(
      API_PATHS.ITEM.DETAIL(id),
      () => get<ItemInfo>(API_PATHS.ITEM.DETAIL(id)),
      {
        useCache: true,
        useMerge: true,
        params: { id },
        cacheExpiry: CACHE_EXPIRY.MEDIUM,
      },
    ),
  createItem: (data: CreateItemRequest) => post<ItemInfo>(API_PATHS.ITEM.CREATE, data),
  updateItem: (id: number | string, data: UpdateItemRequest) => put<ItemInfo>(API_PATHS.ITEM.UPDATE(id), data),
  offShelf: (id: number | string) => post<null>(API_PATHS.ITEM.OFF_SHELF(id)),
  onShelf: (id: number | string) => post<null>(API_PATHS.ITEM.ON_SHELF(id)),
  uploadImage: (data: FormData) => post<{ url: string }>(API_PATHS.ITEM.UPLOAD, data),
  uploadChunk: (data: FormData) => post<{ uploaded: boolean }>(API_PATHS.ITEM.UPLOAD_CHUNK, data),
  completeUpload: (data: { fileHash: string; uploadId: string; fileName: string; totalChunks: number }) =>
    post<{ url: string }>(API_PATHS.ITEM.UPLOAD_COMPLETE, data),
  checkUploadedChunks: (fileHash: string, uploadId: string) =>
    get<{ uploadedChunks: number[] }>(API_PATHS.ITEM.UPLOAD_CHECK, {
      params: { fileHash, uploadId },
    }),
  getRelatedItems: (id: number | string) =>
    requestManager.request(
      API_PATHS.ITEM.RELATED(id),
      () => get<{ similarItems: unknown[]; sellerItems: unknown[] }>(API_PATHS.ITEM.RELATED(id)),
      {
        useCache: true,
        useMerge: true,
        params: { id },
        cacheExpiry: CACHE_EXPIRY.SHORT,
      },
    ),
  getItemOrders: (id: number | string) => get<unknown[]>(API_PATHS.ITEM.ORDERS(id)),
  getItemActiveOrders: (id: number | string) => get<unknown[]>(API_PATHS.ITEM.ACTIVE_ORDERS(id)),
  deleteItem: (id: number | string) => del<null>(API_PATHS.ITEM.DETAIL(id)),
};

export default item;
