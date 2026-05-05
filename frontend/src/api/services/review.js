import instance from '../config/axios';

const review = {
  getReviewsByItem: (itemId) => instance.get(`/reviews/item/${itemId}`),
  createReview: (orderId, data) => instance.post(`/reviews/order/${orderId}`, data),
};

export default review;
