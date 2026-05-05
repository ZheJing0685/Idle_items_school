import instance from '../config/axios';
import requestManager from '../../utils/network/requestManager';
import { API_PATHS } from '../config/paths';

const auth = {
  login: (data) => instance.post(API_PATHS.AUTH.LOGIN, data),
  register: (data) => instance.post(API_PATHS.AUTH.REGISTER, data),
  getCurrentUser: () =>
    requestManager.request(API_PATHS.AUTH.ME, () => instance.get(API_PATHS.AUTH.ME), {
      useCache: true,
      useMerge: true,
    }),
  refreshToken: (data) => instance.post(API_PATHS.AUTH.REFRESH, data),
};

export default auth;
