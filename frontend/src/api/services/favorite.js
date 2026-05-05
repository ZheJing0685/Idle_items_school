import instance from '../config/axios';

const favorite = {
  getFavorites: (page, size) => instance.get('/favorites', { params: { page, size } }),
  addFavorite: (itemId) => instance.post(`/favorites/${itemId}`),
  removeFavorite: (itemId) => instance.delete(`/favorites/${itemId}`),
  checkFavorite: (itemId) => instance.get(`/favorites/${itemId}/status`),
};

export default favorite;
