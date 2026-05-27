import { get, post } from '../config/http';
import requestManager from '../../utils/network/requestManager';
import { API_PATHS } from '../config/paths';
import type { CategoryInfo } from '../../types/api';

const CACHE_EXPIRY = 5 * 60 * 1000;

const category = {
  getCategories: () =>
    requestManager.request(
      API_PATHS.CATEGORY.LIST,
      () => get<CategoryInfo[]>(API_PATHS.CATEGORY.LIST),
      {
        useCache: true,
        useMerge: true,
        cacheExpiry: CACHE_EXPIRY,
      },
    ),
  getCategoryTree: () =>
    requestManager.request(
      API_PATHS.CATEGORY.TREE,
      () => get<CategoryInfo[]>(API_PATHS.CATEGORY.TREE),
      {
        useCache: true,
        useMerge: true,
        cacheExpiry: CACHE_EXPIRY,
      },
    ),
  searchCategories: (keyword: string) =>
    get<CategoryInfo[]>(API_PATHS.CATEGORY.SEARCH, { params: { keyword } }),
  submitFeedback: (data: { categoryName: string; reason: string }) =>
    post<null>(API_PATHS.CATEGORY.FEEDBACK, data),
  getMyFeedbacks: (params?: Record<string, any>) =>
    get<any[]>(API_PATHS.CATEGORY.MY_FEEDBACK, { params }),
};

export default category;
