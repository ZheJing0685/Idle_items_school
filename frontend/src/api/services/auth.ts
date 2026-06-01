import { get, post } from '../config/http';
import requestManager from '../../utils/network/requestManager';
import { API_PATHS } from '../config/paths';
import type { LoginRequest, LoginResponse, RegisterRequest, UserInfo } from '../../types/api';

const auth = {
  login: (data: LoginRequest) => post<LoginResponse>(API_PATHS.AUTH.LOGIN, data),
  register: (data: RegisterRequest) => post<LoginResponse>(API_PATHS.AUTH.REGISTER, data),
  getCurrentUser: () =>
    requestManager.request(
      API_PATHS.AUTH.ME,
      () => get<UserInfo>(API_PATHS.AUTH.ME),
      {
        useCache: true,
        useMerge: true,
      },
    ),
  refreshToken: () =>
    post<{ token: string; refreshToken?: string }>(API_PATHS.AUTH.REFRESH),
  forgotPassword: (email: string) =>
    post<void>(API_PATHS.AUTH.FORGOT_PASSWORD, { email }),
  verifyCode: (email: string, code: string) =>
    post<void>(API_PATHS.AUTH.VERIFY_CODE, { email, code }),
  resetPassword: (email: string, code: string, newPassword: string) =>
    post<void>(API_PATHS.AUTH.RESET_PASSWORD, { email, code, newPassword }),
  changePassword: (oldPassword: string, newPassword: string) =>
    post<void>(API_PATHS.AUTH.CHANGE_PASSWORD, { oldPassword, newPassword }),
  logout: () => post<void>(API_PATHS.AUTH.LOGOUT),
};

export default auth;
