import requestManager from '../utils/network/requestManager';
import auth from './services/auth';
import item from './services/item';
import category from './services/category';
import favorite from './services/favorite';
import user from './services/user';
import verification from './services/verification';
import review from './services/review';
import order from './services/order';
import admin from './services/admin';
import chat from './services/chat';
import {
  setToken,
  getToken,
  clearToken,
  setUnauthorizedHandler,
} from './config/axios';

const api = {
  auth,
  item,
  category,
  favorite,
  user,
  verification,
  review,
  order,
  admin,
  chat,
  setToken,
  getToken,
  clearToken,
  setUnauthorizedHandler,
  clearCache: (url, params) => requestManager.clearCache(url, params),
  clearAllCache: () => requestManager.clearAllCache(),
};

export default api;
