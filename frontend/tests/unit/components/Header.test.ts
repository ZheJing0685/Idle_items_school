import { describe, it, expect, vi, beforeEach, beforeAll } from 'vitest';
import { mount } from '@vue/test-utils';
import { createRouter, createWebHistory } from 'vue-router';
import { setActivePinia, createPinia } from 'pinia';

// 使用 vi.hoisted 创建 mock
const { storeState, elMessageBoxConfirm, elMessageSuccess } = vi.hoisted(() => ({
  storeState: {
    isLoggedIn: false,
    user: null as any,
    logout: vi.fn(),
    getCurrentUser: vi.fn().mockResolvedValue(null),
  },
  elMessageBoxConfirm: vi.fn(),
  elMessageSuccess: vi.fn(),
}));

vi.mock('@element-plus/icons-vue', async (importOriginal) => {
  const actual = await importOriginal();
  return {
    ...actual,
    Search: { template: '<span class="icon-search"></span>' },
    ArrowDown: { template: '<span class="icon-arrow-down"></span>' },
    User: { template: '<span class="icon-user"></span>' },
    Edit: { template: '<span class="icon-edit"></span>' },
    Document: { template: '<span class="icon-document"></span>' },
    Setting: { template: '<span class="icon-setting"></span>' },
    SwitchButton: { template: '<span class="icon-switch"></span>' },
    House: { template: '<span class="icon-house"></span>' },
    Grid: { template: '<span class="icon-grid"></span>' },
    Plus: { template: '<span class="icon-plus"></span>' },
    List: { template: '<span class="icon-list"></span>' },
  };
});

vi.mock('element-plus', () => ({
  ElMessage: { success: elMessageSuccess, error: vi.fn(), warning: vi.fn(), info: vi.fn() },
  ElMessageBox: { confirm: elMessageBoxConfirm, alert: vi.fn() },
}));

vi.mock('@/store', () => ({
  userStore: () => storeState,
}));

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'Home', component: { template: '<div>Home</div>' } },
    { path: '/login', name: 'Login' },
    { path: '/register', name: 'Register' },
    { path: '/user/profile', name: 'UserProfile' },
    { path: '/items', name: 'Items' },
  ],
});

let Header: any;
beforeAll(async () => {
  const mod = await import('@/components/Header.vue');
  Header = mod.default;
});

beforeEach(() => {
  setActivePinia(createPinia());
  vi.clearAllMocks();
  storeState.isLoggedIn = false;
  storeState.user = null;
  storeState.logout.mockClear();
  storeState.getCurrentUser.mockClear();
  storeState.getCurrentUser.mockResolvedValue(null);
  elMessageBoxConfirm.mockReset();
  elMessageBoxConfirm.mockResolvedValue(true);
  elMessageSuccess.mockReset();
});

const mountHeader = (options = {}) => {
  return mount(Header, {
    global: {
      plugins: [router],
      stubs: {
        'el-menu': { template: '<div class="el-menu-stub"><slot /></div>' },
        'el-menu-item': { template: '<div class="el-menu-item"><slot /></div>', props: ['index'] },
        'el-dropdown': { template: '<div class="el-dropdown-stub"><slot /></div>', methods: { hide: vi.fn() } },
        'el-dropdown-menu': { template: '<ul><slot /></ul>' },
        'el-dropdown-item': { template: '<li class="dropdown-item"><slot /></li>' },
        'el-input': {
          template: '<div class="el-input-stub"><input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" /></div>',
          props: ['modelValue'],
          emits: ['update:modelValue'],
        },
        'el-avatar': { template: '<span class="el-avatar-stub"></span>' },
        'el-icon': { template: '<span class="el-icon-stub"><slot /></span>' },
        'el-button': { template: '<button @click="$emit(\'click\')"><slot /></button>' },
        'router-link': { template: '<a @click="$emit(\'click\')"><slot /></a>' },
      },
    },
    ...options,
  });
};

describe('Header Component (TypeScript)', () => {
  describe('Initial rendering', () => {
    it('renders header element', () => {
      const wrapper = mountHeader();
      expect(wrapper.find('header').exists()).toBe(true);
    });

    it('shows login text', () => {
      const wrapper = mountHeader();
      expect(wrapper.text()).toContain('登录');
    });

    it('renders logo section', () => {
      const wrapper = mountHeader();
      expect(wrapper.text()).toContain('闲置好物');
    });

    it('renders search input', () => {
      const wrapper = mountHeader();
      expect(wrapper.find('.el-input-stub').exists()).toBe(true);
    });
  });

  describe('User name display logic', () => {
    it('getUserName returns default for guest', () => {
      const wrapper = mountHeader();
      expect(wrapper.vm.getUserName()).toBe('用户');
    });

    it('getUserName returns nickname when available', () => {
      storeState.isLoggedIn = true;
      storeState.user = { id: 1, username: 'testuser', nickname: '昵称显示' };
      const wrapper = mountHeader();
      expect(wrapper.vm.getUserName()).toBe('昵称显示');
    });

    it('getUserName returns username when nickname is empty', () => {
      storeState.isLoggedIn = true;
      storeState.user = { id: 1, username: 'testuser', nickname: '' };
      const wrapper = mountHeader();
      expect(wrapper.vm.getUserName()).toBe('testuser');
    });

    it('getAvatarText returns first char of nickname', () => {
      storeState.isLoggedIn = true;
      storeState.user = { id: 1, username: 'test', nickname: '张' };
      const wrapper = mountHeader();
      expect(wrapper.vm.getAvatarText()).toBe('张');
    });
  });

  describe('Search functionality', () => {
    it('has searchKeyword state', () => {
      const wrapper = mountHeader();
      expect(wrapper.vm.searchKeyword).toBe('');
    });

    it('has handleSearch method', () => {
      const wrapper = mountHeader();
      expect(typeof wrapper.vm.handleSearch).toBe('function');
    });

    it('searchKeyword updates on input', async () => {
      const wrapper = mountHeader();
      const input = wrapper.find('input');
      if (input.exists()) {
        await input.setValue('关键词');
        expect(wrapper.vm.searchKeyword).toBe('关键词');
      }
    });
  });

  describe('Logged in state', () => {
    it('shows username when logged in', async () => {
      storeState.isLoggedIn = true;
      storeState.user = { id: 1, username: 'testuser', nickname: '测试用户', avatar: null };
      const wrapper = mountHeader();
      await wrapper.vm.$nextTick();
      expect(wrapper.text()).toContain('测试用户');
    });

    it('does not call getCurrentUser on mount', async () => {
      storeState.isLoggedIn = true;
      storeState.user = { id: 1 };
      mountHeader();
      await new Promise(r => setTimeout(r, 10));
      expect(storeState.getCurrentUser).not.toHaveBeenCalled();
    });
  });

  describe('Logout functionality', () => {
    it('has handleLogout method', () => {
      const wrapper = mountHeader();
      expect(typeof wrapper.vm.handleLogout).toBe('function');
    });

    it('calls logout when confirmed', async () => {
      storeState.isLoggedIn = true;
      storeState.user = { id: 1, username: 'testuser' };
      const wrapper = mountHeader();

      await wrapper.vm.handleLogout();

      expect(elMessageBoxConfirm).toHaveBeenCalled();
      expect(storeState.logout).toHaveBeenCalled();
      expect(elMessageSuccess).toHaveBeenCalled();
    });
  });
});
