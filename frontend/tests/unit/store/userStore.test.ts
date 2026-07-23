import { describe, it, expect, vi, beforeEach } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';

// Mock API
const { mockLogin, mockRegister, mockLogout, mockGetCurrentUser, mockRefreshToken, mockUpdateProfile } = vi.hoisted(() => ({
  mockLogin: vi.fn(),
  mockRegister: vi.fn(),
  mockLogout: vi.fn(),
  mockGetCurrentUser: vi.fn(),
  mockRefreshToken: vi.fn(),
  mockUpdateProfile: vi.fn(),
}));

vi.mock('@/api', () => ({
  default: {
    auth: {
      login: mockLogin,
      register: mockRegister,
      logout: mockLogout,
      getCurrentUser: mockGetCurrentUser,
      refreshToken: mockRefreshToken,
    },
    user: {
      updateProfile: mockUpdateProfile,
    },
  },
}));

// Mock token functions
vi.mock('@/api/config/axios', () => ({
  setToken: vi.fn(),
  clearToken: vi.fn(),
  getToken: vi.fn().mockReturnValue('test-token'),
  isLoggedIn: vi.fn().mockReturnValue(false),
  clearCookies: vi.fn(),
  clearAuthState: vi.fn(),
}));

// Mock ErrorHandler
vi.mock('@/utils/error', () => ({
  ErrorHandler: {
    handle: vi.fn(),
  },
}));

describe('User Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  describe('初始状态', () => {
    it('should export useUserStore', async () => {
      const { useUserStore } = await import('@/store/modules/user');
      expect(typeof useUserStore).toBe('function');
    });

    it('should have initial state', async () => {
      const { useUserStore } = await import('@/store/modules/user');
      const store = useUserStore();
      expect(store.user).toBeNull();
      expect(store.loading).toBe(false);
      expect(store.isLoggedIn).toBeDefined();
    });

    it('should have computed properties', async () => {
      const { useUserStore } = await import('@/store/modules/user');
      const store = useUserStore();
      expect(store.isAdmin).toBeDefined();
      expect(store.isVerified).toBeDefined();
    });
  });

  describe('login', () => {
    it('should have login method', async () => {
      const { useUserStore } = await import('@/store/modules/user');
      const store = useUserStore();
      expect(typeof store.login).toBe('function');
    });

    it('should call API login', async () => {
      mockLogin.mockResolvedValue({
        code: 200,
        data: { token: 'test-token', refreshToken: 'refresh-token', user: { id: 1 } },
      });

      const { useUserStore } = await import('@/store/modules/user');
      const store = useUserStore();
      await store.login('testuser', 'password123');

      expect(mockLogin).toHaveBeenCalledWith({ username: 'testuser', password: 'password123' });
    });

    it('should set user after login', async () => {
      mockLogin.mockResolvedValue({
        code: 200,
        data: { token: 'test-token', refreshToken: 'refresh-token', user: { id: 1, username: 'testuser' } },
      });

      const { useUserStore } = await import('@/store/modules/user');
      const store = useUserStore();
      await store.login('testuser', 'password123');

      expect(store.user).toEqual({ id: 1, username: 'testuser' });
      expect(store.isLoggedIn).toBe(true);
    });

    it('should return null on login failure', async () => {
      mockLogin.mockRejectedValue(new Error('登录失败'));

      const { useUserStore } = await import('@/store/modules/user');
      const store = useUserStore();

      const result = await store.login('testuser', 'wrongpassword');
      expect(result).toBeNull();
    });
  });

  describe('register', () => {
    it('should have register method', async () => {
      const { useUserStore } = await import('@/store/modules/user');
      const store = useUserStore();
      expect(typeof store.register).toBe('function');
    });

    it('should call API register', async () => {
      mockRegister.mockResolvedValue({
        code: 200,
        data: { token: 'test-token', refreshToken: 'refresh-token', user: { id: 1 } },
      });

      const { useUserStore } = await import('@/store/modules/user');
      const store = useUserStore();
      const userData = { username: 'newuser', password: 'Password@123', email: 'test@test.com', phone: '13800138000' };
      await store.register(userData);

      expect(mockRegister).toHaveBeenCalledWith(userData);
    });
  });

  describe('logout', () => {
    it('should have logout method', async () => {
      const { useUserStore } = await import('@/store/modules/user');
      const store = useUserStore();
      expect(typeof store.logout).toBe('function');
    });

    it('should clear user state on logout', async () => {
      mockLogout.mockResolvedValue(null);

      const { useUserStore } = await import('@/store/modules/user');
      const store = useUserStore();
      store.user = { id: 1, username: 'testuser' } as any;
      store.isLoggedIn = true;

      await store.logout();

      expect(store.user).toBeNull();
      expect(store.isLoggedIn).toBe(false);
    });
  });

  describe('getCurrentUser', () => {
    it('should have getCurrentUser method', async () => {
      const { useUserStore } = await import('@/store/modules/user');
      const store = useUserStore();
      expect(typeof store.getCurrentUser).toBe('function');
    });

    it('should fetch current user', async () => {
      mockGetCurrentUser.mockResolvedValue({
        code: 200,
        data: { id: 1, username: 'testuser' },
      });

      const { useUserStore } = await import('@/store/modules/user');
      const store = useUserStore();
      const result = await store.getCurrentUser();

      expect(result).toEqual({ id: 1, username: 'testuser' });
      expect(store.user).toEqual({ id: 1, username: 'testuser' });
    });

    it('should restore session from server current user after refresh', async () => {
      mockGetCurrentUser.mockResolvedValue({
        code: 200,
        data: { id: 1, username: 'testuser' },
      });

      const { useUserStore } = await import('@/store/modules/user');
      const store = useUserStore();

      const result = await store.restoreSession();

      expect(result).toBe(true);
      expect(mockGetCurrentUser).toHaveBeenCalled();
      expect(store.user).toEqual({ id: 1, username: 'testuser' });
      expect(store.isLoggedIn).toBe(true);
    });

    it('should refresh cookie session before restoring user when current user is unauthorized', async () => {
      mockGetCurrentUser
        .mockRejectedValueOnce({ response: { status: 401 } })
        .mockResolvedValueOnce({
          code: 200,
          data: { id: 1, username: 'testuser' },
        });
      mockRefreshToken.mockResolvedValue({ code: 200, data: {} });

      const { useUserStore } = await import('@/store/modules/user');
      const store = useUserStore();

      const result = await store.restoreSession();

      expect(result).toBe(true);
      expect(mockRefreshToken).toHaveBeenCalledWith();
      expect(mockGetCurrentUser).toHaveBeenCalledTimes(2);
      expect(store.user).toEqual({ id: 1, username: 'testuser' });
      expect(store.isLoggedIn).toBe(true);
    });

    it('should clear login state when session restore fails', async () => {
      mockGetCurrentUser.mockRejectedValue({ response: { status: 401 } });
      mockRefreshToken.mockRejectedValue({ response: { status: 401 } });

      const { useUserStore } = await import('@/store/modules/user');
      const store = useUserStore();
      store.user = { id: 1, username: 'testuser' } as any;
      store.isLoggedIn = true;

      const result = await store.restoreSession();

      expect(result).toBe(false);
      expect(store.user).toBeNull();
      expect(store.isLoggedIn).toBe(false);
    });

    it('should have getCurrentUser method', async () => {
      const { useUserStore } = await import('@/store/modules/user');
      const store = useUserStore();
      expect(typeof store.getCurrentUser).toBe('function');
    });
  });

  describe('updateProfile', () => {
    it('should have updateProfile method', async () => {
      const { useUserStore } = await import('@/store/modules/user');
      const store = useUserStore();
      expect(typeof store.updateProfile).toBe('function');
    });

    it('should update user profile', async () => {
      mockUpdateProfile.mockResolvedValue({
        code: 200,
        data: { nickname: '新昵称' },
      });

      const { useUserStore } = await import('@/store/modules/user');
      const store = useUserStore();
      store.user = { id: 1, username: 'testuser', nickname: '旧昵称' } as any;

      await store.updateProfile({ nickname: '新昵称' });

      expect(mockUpdateProfile).toHaveBeenCalledWith({ nickname: '新昵称' });
    });
  });

  describe('loading state', () => {
    it('should set loading during login', async () => {
      let resolveLogin: any;
      mockLogin.mockImplementation(() => new Promise(resolve => { resolveLogin = resolve; }));

      const { useUserStore } = await import('@/store/modules/user');
      const store = useUserStore();

      const loginPromise = store.login('testuser', 'password123');
      expect(store.loading).toBe(true);

      resolveLogin({ code: 200, data: { token: 'test', refreshToken: 'refresh', user: {} } });
      await loginPromise;

      expect(store.loading).toBe(false);
    });
  });
});
