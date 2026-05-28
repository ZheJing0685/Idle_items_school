import { describe, it, expect } from 'vitest'
import { API_PATHS } from '@/api/config/paths'

describe('API路径配置', () => {
  describe('AUTH路径', () => {
    it('应该包含登录路径', () => {
      expect(API_PATHS.AUTH.LOGIN).toBe('/auth/login')
    })

    it('应该包含注册路径', () => {
      expect(API_PATHS.AUTH.REGISTER).toBe('/auth/register')
    })

    it('应该包含获取用户信息路径', () => {
      expect(API_PATHS.AUTH.ME).toBe('/auth/me')
    })

    it('应该包含刷新token路径', () => {
      expect(API_PATHS.AUTH.REFRESH).toBe('/auth/refresh')
    })

    it('应该包含忘记密码路径', () => {
      expect(API_PATHS.AUTH.FORGOT_PASSWORD).toBe('/auth/forgot-password')
    })

    it('应该包含验证代码路径', () => {
      expect(API_PATHS.AUTH.VERIFY_CODE).toBe('/auth/verify-code')
    })

    it('应该包含重置密码路径', () => {
      expect(API_PATHS.AUTH.RESET_PASSWORD).toBe('/auth/reset-password')
    })

    it('应该包含修改密码路径', () => {
      expect(API_PATHS.AUTH.CHANGE_PASSWORD).toBe('/auth/change-password')
    })

    it('应该包含登出路径', () => {
      expect(API_PATHS.AUTH.LOGOUT).toBe('/auth/logout')
    })
  })

  describe('ITEM路径', () => {
    it('应该包含物品列表路径', () => {
      expect(API_PATHS.ITEM.LIST).toBe('/items')
    })

    it('应该包含热门物品路径', () => {
      expect(API_PATHS.ITEM.HOT).toBe('/items/hot')
    })

    it('应该包含搜索路径', () => {
      expect(API_PATHS.ITEM.SEARCH).toBe('/items/search')
    })

    it('应该包含物品详情路径函数', () => {
      expect(API_PATHS.ITEM.DETAIL(1)).toBe('/items/1')
      expect(API_PATHS.ITEM.DETAIL('abc')).toBe('/items/abc')
    })

    it('应该包含创建物品路径', () => {
      expect(API_PATHS.ITEM.CREATE).toBe('/items')
    })

    it('应该包含更新物品路径函数', () => {
      expect(API_PATHS.ITEM.UPDATE(1)).toBe('/items/1')
    })

    it('应该包含下架路径函数', () => {
      expect(API_PATHS.ITEM.OFF_SHELF(1)).toBe('/items/1/off-shelf')
    })

    it('应该包含上架路径函数', () => {
      expect(API_PATHS.ITEM.ON_SHELF(1)).toBe('/items/1/on-shelf')
    })

    it('应该包含上传路径', () => {
      expect(API_PATHS.ITEM.UPLOAD).toBe('/items/upload')
      expect(API_PATHS.ITEM.UPLOAD_CHUNK).toBe('/items/upload/chunk')
      expect(API_PATHS.ITEM.UPLOAD_COMPLETE).toBe('/items/upload/complete')
      expect(API_PATHS.ITEM.UPLOAD_CHECK).toBe('/items/upload/check')
    })

    it('应该包含订单路径函数', () => {
      expect(API_PATHS.ITEM.ORDERS(1)).toBe('/items/1/orders')
      expect(API_PATHS.ITEM.ACTIVE_ORDERS(1)).toBe('/items/1/active-orders')
    })
  })

  describe('CATEGORY路径', () => {
    it('应该包含分类列表路径', () => {
      expect(API_PATHS.CATEGORY.LIST).toBe('/categories')
    })

    it('应该包含分类树路径', () => {
      expect(API_PATHS.CATEGORY.TREE).toBe('/categories/tree')
    })

    it('应该包含分类搜索路径', () => {
      expect(API_PATHS.CATEGORY.SEARCH).toBe('/categories/search')
    })

    it('应该包含反馈路径', () => {
      expect(API_PATHS.CATEGORY.FEEDBACK).toBe('/categories/feedback')
      expect(API_PATHS.CATEGORY.MY_FEEDBACK).toBe('/categories/feedback/my')
    })
  })

  describe('FAVORITE路径', () => {
    it('应该包含收藏列表路径', () => {
      expect(API_PATHS.FAVORITE.LIST).toBe('/favorites')
    })

    it('应该包含添加收藏路径', () => {
      expect(API_PATHS.FAVORITE.ADD).toBe('/favorites')
    })

    it('应该包含移除收藏路径函数', () => {
      expect(API_PATHS.FAVORITE.REMOVE(1)).toBe('/favorites/1')
    })
  })

  describe('ORDER路径', () => {
    it('应该包含创建订单路径', () => {
      expect(API_PATHS.ORDER.CREATE).toBe('/orders')
    })

    it('应该包含订单列表路径', () => {
      expect(API_PATHS.ORDER.LIST).toBe('/orders')
    })

    it('应该包含订单详情路径函数', () => {
      expect(API_PATHS.ORDER.DETAIL(1)).toBe('/orders/1')
    })

    it('应该包含取消订单路径函数', () => {
      expect(API_PATHS.ORDER.CANCEL(1)).toBe('/orders/1/cancel')
    })

    it('应该包含支付路径函数', () => {
      expect(API_PATHS.ORDER.PAY(1)).toBe('/orders/1/pay')
    })

    it('应该包含发货路径函数', () => {
      expect(API_PATHS.ORDER.SHIP(1)).toBe('/orders/1/ship')
    })

    it('应该包含确认收货路径函数', () => {
      expect(API_PATHS.ORDER.CONFIRM(1)).toBe('/orders/1/confirm-receive')
    })

    it('应该包含退款路径函数', () => {
      expect(API_PATHS.ORDER.REFUND(1)).toBe('/orders/1/refund')
    })
  })

  describe('REVIEW路径', () => {
    it('应该包含创建评价路径', () => {
      expect(API_PATHS.REVIEW.CREATE).toBe('/reviews')
    })

    it('应该包含物品评价列表路径函数', () => {
      expect(API_PATHS.REVIEW.LIST(1)).toBe('/reviews/item/1')
    })

    it('应该包含用户评价路径', () => {
      expect(API_PATHS.REVIEW.USER).toBe('/reviews/user')
    })
  })

  describe('USER路径', () => {
    it('应该包含用户资料路径', () => {
      expect(API_PATHS.USER.PROFILE).toBe('/user/profile')
      expect(API_PATHS.USER.UPDATE).toBe('/user/profile')
    })

    it('应该包含实名认证路径', () => {
      expect(API_PATHS.USER.VERIFICATION).toBe('/user/verification')
    })

    it('应该包含用户统计路径', () => {
      expect(API_PATHS.USER.STATS).toBe('/user/stats')
    })
  })

  describe('ADMIN路径', () => {
    it('应该包含管理后台路径', () => {
      expect(API_PATHS.ADMIN.DASHBOARD).toBe('/admin/dashboard')
      expect(API_PATHS.ADMIN.USERS).toBe('/admin/users')
      expect(API_PATHS.ADMIN.CREATE_USER).toBe('/admin/users')
    })

    it('应该包含用户管理路径函数', () => {
      expect(API_PATHS.ADMIN.UPDATE_USER(1)).toBe('/admin/users/1')
      expect(API_PATHS.ADMIN.DELETE_USER(1)).toBe('/admin/users/1')
    })

    it('应该包含导出路径', () => {
      expect(API_PATHS.ADMIN.EXPORT_USERS).toBe('/admin/users/export')
      expect(API_PATHS.ADMIN.EXPORT_ITEMS).toBe('/admin/items/export')
      expect(API_PATHS.ADMIN.EXPORT_LOGS).toBe('/admin/logs/export')
    })

    it('应该包含批量删除路径', () => {
      expect(API_PATHS.ADMIN.BATCH_DELETE_USERS).toBe('/admin/batch/users/delete')
    })

    it('应该包含管理后台其他路径', () => {
      expect(API_PATHS.ADMIN.ITEMS).toBe('/admin/items')
      expect(API_PATHS.ADMIN.ORDERS).toBe('/admin/orders')
      expect(API_PATHS.ADMIN.CATEGORIES).toBe('/admin/categories')
      expect(API_PATHS.ADMIN.STATISTICS).toBe('/admin/statistics')
      expect(API_PATHS.ADMIN.LOGS).toBe('/admin/logs')
    })

    it('应该包含争议管理路径', () => {
      expect(API_PATHS.ADMIN.DISPUTES).toBe('/admin/disputes')
      expect(API_PATHS.ADMIN.DISPUTE_STATS).toBe('/admin/disputes/stats')
      expect(API_PATHS.ADMIN.HANDLE_DISPUTE(1)).toBe('/admin/disputes/1/handle')
    })
  })

  describe('路径常量类型检查', () => {
    it('应该确保所有路径都是字符串或函数', () => {
      // 检查所有路径是否为字符串
      expect(typeof API_PATHS.AUTH.LOGIN).toBe('string')
      expect(typeof API_PATHS.AUTH.REGISTER).toBe('string')
      expect(typeof API_PATHS.ITEM.LIST).toBe('string')
      expect(typeof API_PATHS.CATEGORY.LIST).toBe('string')
      expect(typeof API_PATHS.FAVORITE.LIST).toBe('string')
      expect(typeof API_PATHS.ORDER.CREATE).toBe('string')
      expect(typeof API_PATHS.REVIEW.CREATE).toBe('string')
      expect(typeof API_PATHS.USER.PROFILE).toBe('string')
      expect(typeof API_PATHS.ADMIN.DASHBOARD).toBe('string')
      
      // 检查动态路径是否为函数
      expect(typeof API_PATHS.ITEM.DETAIL).toBe('function')
      expect(typeof API_PATHS.ITEM.UPDATE).toBe('function')
      expect(typeof API_PATHS.ITEM.OFF_SHELF).toBe('function')
      expect(typeof API_PATHS.ITEM.ON_SHELF).toBe('function')
      expect(typeof API_PATHS.ITEM.ORDERS).toBe('function')
      expect(typeof API_PATHS.ITEM.ACTIVE_ORDERS).toBe('function')
      expect(typeof API_PATHS.FAVORITE.REMOVE).toBe('function')
      expect(typeof API_PATHS.ORDER.DETAIL).toBe('function')
      expect(typeof API_PATHS.ORDER.CANCEL).toBe('function')
      expect(typeof API_PATHS.ORDER.PAY).toBe('function')
      expect(typeof API_PATHS.ORDER.SHIP).toBe('function')
      expect(typeof API_PATHS.ORDER.CONFIRM).toBe('function')
      expect(typeof API_PATHS.ORDER.REFUND).toBe('function')
      expect(typeof API_PATHS.REVIEW.LIST).toBe('function')
      expect(typeof API_PATHS.ADMIN.UPDATE_USER).toBe('function')
      expect(typeof API_PATHS.ADMIN.DELETE_USER).toBe('function')
      expect(typeof API_PATHS.ADMIN.HANDLE_DISPUTE).toBe('function')
    })
  })
})