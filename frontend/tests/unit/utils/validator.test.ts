import { describe, it, expect } from 'vitest'
import {
  validateEmail,
  validatePhone,
  validatePassword,
  validateUsername,
  validateNickname,
  validatePrice,
  validateTitle,
  validateDescription,
  validateArray,
  validateRequired,
  validateMinLength,
  validateMaxLength
} from '@/utils/validator'

describe('Validator Utils', () => {
  describe('validateEmail', () => {
    it('should return true for valid email', () => {
      expect(validateEmail('test@example.com')).toBe(true)
      expect(validateEmail('user.name@domain.com')).toBe(true)
      expect(validateEmail('user+tag@example.com')).toBe(true)
    })

    it('should return false for invalid email', () => {
      expect(validateEmail('invalid-email')).toBe(false)
      expect(validateEmail('test@')).toBe(false)
      expect(validateEmail('@example.com')).toBe(false)
      expect(validateEmail('')).toBe(false)
      expect(validateEmail('test @example.com')).toBe(false)
    })
  })

  describe('validatePhone', () => {
    it('should return true for valid phone', () => {
      expect(validatePhone('13800138000')).toBe(true)
      expect(validatePhone('15912345678')).toBe(true)
      expect(validatePhone('18800001111')).toBe(true)
    })

    it('should return false for invalid phone', () => {
      expect(validatePhone('12345678901')).toBe(false)
      expect(validatePhone('1380013800')).toBe(false)
      expect(validatePhone('abc')).toBe(false)
      expect(validatePhone('')).toBe(false)
    })
  })

  describe('validatePassword', () => {
    it('should return true for valid password', () => {
      expect(validatePassword('Password@123')).toBe(true)
      expect(validatePassword('MyPass@456')).toBe(true)
      expect(validatePassword('Test@2024!')).toBe(true)
    })

    it('should return false for weak password', () => {
      expect(validatePassword('12345678')).toBe(false) // no uppercase, lowercase, special
      expect(validatePassword('password')).toBe(false) // no uppercase, number, special
      expect(validatePassword('Pass@1')).toBe(false) // too short
      expect(validatePassword('')).toBe(false) // empty
      expect(validatePassword('Password123')).toBe(false) // no special char
    })
  })

  describe('validateUsername', () => {
    it('should return true for valid username', () => {
      expect(validateUsername('testuser')).toBe(true)
      expect(validateUsername('user123')).toBe(true)
      expect(validateUsername('abc')).toBe(true)
    })

    it('should return false for invalid username', () => {
      expect(validateUsername('ab')).toBe(false) // too short
      expect(validateUsername('')).toBe(false) // empty
      expect(validateUsername('a'.repeat(21))).toBe(false) // too long
    })
  })

  describe('validateNickname', () => {
    it('should return true for valid nickname', () => {
      expect(validateNickname('测试用户')).toBe(true)
      expect(validateNickname('ab')).toBe(true)
    })

    it('should return false for invalid nickname', () => {
      expect(validateNickname('a')).toBe(false) // too short
      expect(validateNickname('')).toBe(false) // empty
      expect(validateNickname('a'.repeat(21))).toBe(false) // too long
    })
  })

  describe('validatePrice', () => {
    it('should return true for valid price', () => {
      expect(validatePrice(100)).toBe(true)
      expect(validatePrice(99.99)).toBe(true)
      expect(validatePrice('50')).toBe(true)
    })

    it('should return false for invalid price', () => {
      expect(validatePrice(0)).toBe(false)
      expect(validatePrice(-10)).toBe(false)
      expect(validatePrice(1000000)).toBe(false) // too high
      expect(validatePrice(null)).toBe(false)
      expect(validatePrice(undefined)).toBe(false)
    })
  })

  describe('validateTitle', () => {
    it('should return true for valid title', () => {
      expect(validateTitle('测试物品标题')).toBe(true)
      expect(validateTitle('abc')).toBe(true)
    })

    it('should return false for invalid title', () => {
      expect(validateTitle('ab')).toBe(false) // too short
      expect(validateTitle('')).toBe(false) // empty
      expect(validateTitle('a'.repeat(51))).toBe(false) // too long
    })
  })

  describe('validateDescription', () => {
    it('should return true for valid description', () => {
      expect(validateDescription('这是一个有效的描述内容')).toBe(true)
      expect(validateDescription('a'.repeat(10))).toBe(true)
    })

    it('should return false for invalid description', () => {
      expect(validateDescription('short')).toBe(false) // too short
      expect(validateDescription('')).toBe(false) // empty
      expect(validateDescription('a'.repeat(1001))).toBe(false) // too long
    })
  })

  describe('validateArray', () => {
    it('should return true for valid array', () => {
      expect(validateArray([1, 2, 3])).toBe(true)
      expect(validateArray([1, 2, 3], 1, 5)).toBe(true)
      expect(validateArray([], 0, 5)).toBe(true)
    })

    it('should return false for invalid array', () => {
      expect(validateArray('not-array' as any)).toBe(false)
      expect(validateArray([1, 2], 3)).toBe(false) // too few
      expect(validateArray([1, 2, 3, 4, 5], 1, 3)).toBe(false) // too many
    })
  })

  describe('validateRequired', () => {
    it('should return true for valid values', () => {
      expect(validateRequired('test')).toBe(true)
      expect(validateRequired(123)).toBe(true)
      expect(validateRequired([1])).toBe(true)
      expect(validateRequired(true)).toBe(true)
    })

    it('should return false for empty values', () => {
      expect(validateRequired(null)).toBe(false)
      expect(validateRequired(undefined)).toBe(false)
      expect(validateRequired('')).toBe(false)
      expect(validateRequired('  ')).toBe(false)
      expect(validateRequired([])).toBe(false)
    })
  })

  describe('validateMinLength', () => {
    it('should return true when length >= min', () => {
      expect(validateMinLength('test', 3)).toBe(true)
      expect(validateMinLength('test', 4)).toBe(true)
      expect(validateMinLength([1, 2, 3], 2)).toBe(true)
    })

    it('should return false when length < min', () => {
      expect(validateMinLength('test', 5)).toBe(false)
      expect(validateMinLength('', 1)).toBe(false)
      expect(validateMinLength([1], 2)).toBe(false)
    })
  })

  describe('validateMaxLength', () => {
    it('should return true when length <= max', () => {
      expect(validateMaxLength('test', 5)).toBe(true)
      expect(validateMaxLength('test', 4)).toBe(true)
      expect(validateMaxLength([1, 2, 3], 4)).toBe(true)
    })

    it('should return false when length > max', () => {
      expect(validateMaxLength('test', 3)).toBe(false)
      expect(validateMaxLength([1, 2, 3], 2)).toBe(false)
    })

    it('should return true for null/undefined', () => {
      expect(validateMaxLength(null, 5)).toBe(true)
      expect(validateMaxLength(undefined, 5)).toBe(true)
    })
  })
})
