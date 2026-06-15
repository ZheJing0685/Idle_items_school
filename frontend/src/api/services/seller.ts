import { get } from '../config/http';
import type { SellerProfile, ReviewItem, ItemSummary, PageResponse } from '../../types/api';

const seller = {
  getProfile: (id: number) => get<SellerProfile>(`/user/${id}/profile`),
  getItems: (id: number, page = 1, size = 12) =>
    get<PageResponse<ItemSummary>>(`/user/${id}/items`, { params: { page, size } }),
  getReviews: (id: number, page = 1, size = 10) =>
    get<PageResponse<ReviewItem>>(`/user/${id}/reviews`, { params: { page, size } }),
};

export default seller;
