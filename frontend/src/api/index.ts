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
import notification from './services/notification';
import dict from './services/dict';
import config from './services/config';
import { setUnauthorizedHandler } from './config/axios';

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
  notification,
  dict,
  config,
  setUnauthorizedHandler,
  clearCache: (url: string, params?: Record<string, unknown>) => requestManager.clearCache(url, params),
  clearAllCache: () => requestManager.clearAllCache(),
};

export default api;
