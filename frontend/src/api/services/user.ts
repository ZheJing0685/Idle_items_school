import { get, put } from '../config/http';
import { API_PATHS } from '../config/paths';
import type { UserInfo } from '../../types/api';

const user = {
  getItems: (status?: string, page?: number, size?: number) =>
    get<any[]>('/items/user', { params: { status, page, size } }),
  getProfile: () => get<UserInfo>(API_PATHS.USER.PROFILE),
  updateProfile: (data: Partial<UserInfo>) => put<UserInfo>(API_PATHS.USER.UPDATE, data),
  getStats: () => get<{ totalItems: number; totalSales: number; totalPurchases: number; rating: number }>(API_PATHS.USER.STATS),
};

export default user;
