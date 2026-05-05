import { createRouter, createWebHistory } from 'vue-router';
import { userStore } from '../store';
import { ElMessage } from 'element-plus';

const router = createRouter({
  history: createWebHistory(),
  routes: [
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
      name: 'OrderList',
      component: () => import('../views/OrderList.vue'),
      meta: {
        requiresAuth: true,
        title: '我的订单',
      },
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
      ],
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
  ],
});

router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title || '闲置物品交易平台';

  const store = userStore();

  // 检查是否需要认证
  if (to.matched.some((record) => record.meta.requiresAuth)) {
    if (!store.isLoggedIn) {
      // 保存当前路径，登录后重定向
      localStorage.setItem('redirectPath', to.fullPath);
      next('/login');
    } else {
      // 检查是否需要管理员权限
      if (to.matched.some((record) => record.meta.requiresAdmin)) {
        // 管理员路由始终刷新用户信息，验证 token 和角色是否仍有效
        try {
          await store.getCurrentUser();
        } catch (error) {
          console.error('获取用户信息失败', error);
        }

        if (!store.isAdmin) {
          ElMessage.error('无权限访问管理后台');
          next('/');
        } else {
          next();
        }
      } else {
        // 普通认证路由，尝试获取用户信息（仅在缺失时）
        try {
          if (!store.user || !store.user.role) {
            await store.getCurrentUser();
          }
        } catch (error) {
          console.error('获取用户信息失败', error);
        }
        next();
      }
    }
  } else {
    next();
  }
});

// 路由导航完成后滚动到页面顶部
router.afterEach((to, from) => {
  // 滚动到顶部
  window.scrollTo({
    top: 0,
    behavior: 'smooth', // 平滑滚动
  });
});

export default router;
