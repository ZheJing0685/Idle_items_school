import { describe, it, expect, vi, beforeEach } from 'vitest'
import configService from '../../../src/api/services/config'

describe('Config Service', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getAllConfigs 应该导出函数', () => {
    expect(typeof configService.getAllConfigs).toBe('function')
  })

  it('getConfig 应该导出函数', () => {
    expect(typeof configService.getConfig).toBe('function')
  })

  it('getConfigsByGroup 应该导出函数', () => {
    expect(typeof configService.getConfigsByGroup).toBe('function')
  })

  it('saveConfig 应该导出函数', () => {
    expect(typeof configService.saveConfig).toBe('function')
  })

  it('deleteConfig 应该导出函数', () => {
    expect(typeof configService.deleteConfig).toBe('function')
  })

  it('clearConfigCache 应该导出函数', () => {
    expect(typeof configService.clearConfigCache).toBe('function')
  })

  it('reloadConfigCache 应该导出函数', () => {
    expect(typeof configService.reloadConfigCache).toBe('function')
  })
})
