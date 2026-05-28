import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import { userStore } from '../store';
import { ElMessage } from 'element-plus';

function isValidRedirectPath(path: string): boolean {
  if (!path) return false;
  return path.startsWith('/') && !path.startsWith('//');
}

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: {
      requiresAuth: false,
      title: '首页',
    },
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: {
      requiresAuth: false,
      title: '登录',
    },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: {
      requiresAuth: false,
      title: '注册',
    },
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('../views/ForgotPassword.vue'),
    meta: {
      requiresAuth: false,
      title: '忘记密码',
    },
  },
  {
    path: '/items',
    name: 'Items',
    component: () => import('../views/Items.vue'),
    meta: {
      requiresAuth: false,
      title: '浏览闲置',
    },
  },
  {
    path: '/item/:id',
    name: 'ItemDetail',
    component: () => import('../views/ItemDetail.vue'),
    meta: {
      requiresAuth: false,
      title: '商品详情',
    },
  },
  {
    path: '/orders',
    redirect: '/user/orders',
  },
  {
    path: '/user',
    name: 'UserCenter',
    component: () => import('../views/UserCenter.vue'),
    meta: {
      requiresAuth: true,
      title: '个人中心',
    },
    children: [
      {
        path: 'profile',
        name: 'UserProfile',
        component: () => import('../views/user/Profile.vue'),
        meta: {
          requiresAuth: true,
          title: '个人信息',
        },
      },
      {
        path: 'items',
        name: 'UserItems',
        component: () => import('../views/user/Items.vue'),
        meta: {
          requiresAuth: true,
          title: '我的发布',
        },
      },
      {
        path: 'favorites',
        name: 'UserFavorites',
        component: () => import('../views/user/Favorites.vue'),
        meta: {
          requiresAuth: true,
          title: '我的收藏',
        },
      },
      {
        path: 'verification',
        name: 'UserVerification',
        component: () => import('../views/user/Verification.vue'),
        meta: {
          requiresAuth: true,
          title: '实名认证',
        },
      },
      {
        path: 'feedback',
        name: 'MyFeedbacks',
        component: () => import('../views/user/MyFeedbacks.vue'),
        meta: {
          requiresAuth: true,
          title: '我的反馈',
        },
      },
      {
        path: 'chat',
        name: 'UserChat',
        component: () => import('../views/user/Chat.vue'),
        meta: {
          requiresAuth: true,
          title: '消息中心',
        },
      },
      {
        path: 'notifications',
        name: 'UserNotifications',
        component: () => import('../views/user/Notifications.vue'),
        meta: {
          requiresAuth: true,
          title: '消息通知',
        },
      },
      {
        path: 'orders',
        name: 'UserOrders',
        component: () => import('../views/OrderList.vue'),
        meta: {
          requiresAuth: true,
          title: '我的订单',
        },
      },
      {
        path: 'change-password',
        name: 'ChangePassword',
        component: () => import('../views/user/ChangePassword.vue'),
        meta: {
          requiresAuth: true,
          title: '修改密码',
        },
      },
    ],
  },
  {
    path: '/feedback',
    name: 'CategoryFeedback',
    component: () => import('../views/CategoryFeedback.vue'),
    meta: {
      requiresAuth: true,
      title: '分类反馈',
    },
  },
  {
    path: '/publish',
    name: 'Publish',
    component: () => import('../views/Publish.vue'),
    meta: {
      requiresAuth: true,
      title: '发布闲置',
    },
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../views/admin/Admin.vue'),
    meta: {
      requiresAuth: true,
      requiresAdmin: true,
      title: '管理后台',
    },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('../views/admin/Dashboard.vue'),
        meta: {
          requiresAuth: true,
          requiresAdmin: true,
          title: '控制台',
        },
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('../views/admin/UserManagement.vue'),
        meta: {
          requiresAuth: true,
          requiresAdmin: true,
          title: '用户管理',
        },
      },
      {
        path: 'items',
        name: 'ItemManagement',
        component: () => import('../views/admin/ItemManagement.vue'),
        meta: {
          requiresAuth: true,
          requiresAdmin: true,
          title: '物品管理',
        },
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('../views/admin/Statistics.vue'),
        meta: {
          requiresAuth: true,
          requiresAdmin: true,
          title: '统计分析',
        },
      },
      {
        path: 'categories',
        name: 'CategoryManagement',
        component: () => import('../views/admin/CategoryManagement.vue'),
        meta: {
          requiresAuth: true,
          requiresAdmin: true,
          title: '分类管理',
        },
      },
      {
        path: 'category-feedbacks',
        name: 'CategoryFeedbackManagement',
        component: () => import('../views/admin/CategoryFeedbackManagement.vue'),
        meta: {
          requiresAuth: true,
          requiresAdmin: true,
          title: '分类反馈管理',
        },
      },
      {
        path: 'orders',
        name: 'OrderManagement',
        component: () => import('../views/admin/OrderManagement.vue'),
        meta: {
          requiresAuth: true,
          requiresAdmin: true,
          title: '订单管理',
        },
      },
      {
        path: 'verification',
        name: 'VerificationManagement',
        component: () => import('../views/admin/VerificationManagement.vue'),
        meta: {
          requiresAuth: true,
          requiresAdmin: true,
          title: '实名认证审核',
        },
      },
      {
        path: 'logs',
        name: 'LogManagement',
        component: () => import('../views/admin/LogManagement.vue'),
        meta: {
          requiresAuth: true,
          requiresAdmin: true,
          title: '操作日志管理',
        },
      },
      {
        path: 'disputes',
        name: 'DisputeManagement',
        component: () => import('../views/admin/DisputeManagement.vue'),
        meta: {
          requiresAuth: true,
          requiresAdmin: true,
          title: '纠纷管理',
        },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue'),
    meta: {
      requiresAuth: false,
      title: '页面不存在',
    },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(async (to, from, next) => {
  const brandName = '闲置物品交易平台';
  const pageTitle = to.meta.title as string;
  document.title = pageTitle ? `${pageTitle} - ${brandName}` : brandName;
  const store = userStore();

  if (to.matched.some((record) => record.meta.requiresAuth)) {
    if (!store.isLoggedIn) {
      const redirectPath = to.fullPath;
      if (isValidRedirectPath(redirectPath)) {
        localStorage.setItem('redirectPath', redirectPath);
      }
      next('/login');
    } else {
      if (to.matched.some((record) => record.meta.requiresAdmin)) {
        try {
          if (!store.user) {
            await store.getCurrentUser();
          }
        } catch (error) {
          console.error('获取用户信息失败', error);
          store.logout();
          next('/login');
          return;
        }
        if (!store.isAdmin) {
          ElMessage.error('无权限访问管理后台');
          next('/');
        } else {
          next();
        }
      } else {
        if (!store.user) {
          try {
            await store.getCurrentUser();
          } catch (error) {
            console.error('获取用户信息失败', error);
            store.logout();
            next('/login');
            return;
          }
        }
        next();
      }
    }
  } else {
    next();
  }
});

router.afterEach((_to, _from) => {
  window.scrollTo({ top: 0, behavior: 'smooth' });
});

export default router;
