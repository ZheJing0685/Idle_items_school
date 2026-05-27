import { get, post } from '../config/http';
import type { ReviewInfo, CreateReviewRequest } from '../../types/api';

const review = {
  getReviewsByItem: (itemId: number | string) =>
    get<ReviewInfo[]>(`/reviews/item/${itemId}`),
  createReview: (orderId: number | string, data: CreateReviewRequest) =>
    post<ReviewInfo>(`/reviews/order/${orderId}`, data),
};

export default review;
