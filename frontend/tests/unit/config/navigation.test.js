import { describe, it, expect } from 'vitest'
import { iconMap, navigationConfig, userMenuConfig, getNavigationItems, getUserMenuItems } from '../../../src/config/navigation'

describe('Navigation Config', () => {
  describe('iconMap', () => {
    it('应该包含所有必要的图标', () => {
      expect(iconMap.home).toBeDefined()
      expect(iconMap.grid).toBeDefined()
      expect(iconMap.user).toBeDefined()
      expect(iconMap.box).toBeDefined()
      expect(iconMap.heart).toBeDefined()
      expect(iconMap.message).toBeDefined()
      expect(iconMap.bell).toBeDefined()
    })
  })

  describe('navigationConfig', () => {
    it('应该有 guest 导航', () => {
      expect(navigationConfig.guest).toBeDefined()
      expect(navigationConfig.guest.length).toBeGreaterThan(0)
    })

    it('应该有 user 导航', () => {
      expect(navigationConfig.user).toBeDefined()
      expect(navigationConfig.user.length).toBeGreaterThan(0)
    })

    it('应该有 admin 导航', () => {
      expect(navigationConfig.admin).toBeDefined()
      expect(navigationConfig.admin.length).toBeGreaterThan(0)
    })
  })

  describe('userMenuConfig', () => {
    it('应该有菜单项', () => {
      expect(userMenuConfig.items).toBeDefined()
      expect(userMenuConfig.items.length).toBeGreaterThan(0)
    })

    it('应该有管理员菜单项', () => {
      expect(userMenuConfig.adminItem).toBeDefined()
      expect(userMenuConfig.adminItem.path).toBe('/admin')
    })
  })

  describe('getNavigationItems', () => {
    it('应该返回 guest 导航', () => {
      const items = getNavigationItems('guest')
      expect(items).toEqual(navigationConfig.guest)
    })

    it('应该返回 user 导航', () => {
      const items = getNavigationItems('user')
      expect(items).toEqual(navigationConfig.user)
    })

    it('应该返回 admin 导航', () => {
      const items = getNavigationItems('admin')
      expect(items).toEqual(navigationConfig.admin)
    })

    it('未知角色应该返回 guest 导航', () => {
      const items = getNavigationItems('unknown')
      expect(items).toEqual(navigationConfig.guest)
    })
  })

  describe('getUserMenuItems', () => {
    it('普通用户应该没有管理后台菜单', () => {
      const items = getUserMenuItems(false)
      expect(items.find(i => i.path === '/admin')).toBeUndefined()
    })

    it('管理员应该有管理后台菜单', () => {
      const items = getUserMenuItems(true)
      expect(items.find(i => i.path === '/admin')).toBeDefined()
    })
  })
})
