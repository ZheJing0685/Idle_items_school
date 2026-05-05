import instance from '../config/axios';
import requestManager from '../../utils/network/requestManager';
import { API_PATHS } from '../config/paths';

const category = {
  getCategories: () =>
    requestManager.request(API_PATHS.CATEGORY.LIST, () => instance.get(API_PATHS.CATEGORY.LIST), {
      useCache: true,
      useMerge: true,
    }),
  getCategoryTree: () =>
    requestManager.request(API_PATHS.CATEGORY.TREE, () => instance.get(API_PATHS.CATEGORY.TREE), {
      useCache: true,
      useMerge: true,
    }),
};

export default category;
