import { get, post, del } from '../config/http';
import type { FavoriteInfo } from '../../types/api';

const favorite = {
  getFavorites: (page?: number, size?: number) =>
    get<{ content: FavoriteInfo[]; totalElements: number }>('/favorites', { params: { page, size } }),
  addFavorite: (itemId: number | string) => post<null>(`/favorites/${itemId}`),
  removeFavorite: (itemId: number | string) => del<null>(`/favorites/${itemId}`),
  checkFavorite: (itemId: number | string) => get<{ isFavorited: boolean }>(`/favorites/${itemId}/status`),
};

export default favorite;
