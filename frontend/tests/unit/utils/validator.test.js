import { describe, it, expect } from 'vitest'
import { validatePhone, validateEmail, validatePassword, validateUsername, validatePrice } from '../../../src/utils/validator'

describe('Validator Utils', () => {
  describe('validatePhone', () => {
    it('valid phone numbers', () => {
      expect(validatePhone('13800138000')).toBe(true)
      expect(validatePhone('15912345678')).toBe(true)
      expect(validatePhone('18611112222')).toBe(true)
    })

    it('invalid phone numbers', () => {
      expect(validatePhone('1234567890')).toBe(false)
      expect(validatePhone('138001380001')).toBe(false)
      expect(validatePhone('abc')).toBe(false)
      expect(validatePhone('')).toBe(false)
    })
  })

  describe('validateEmail', () => {
    it('valid emails', () => {
      expect(validateEmail('test@example.com')).toBe(true)
      expect(validateEmail('user.name@domain.co')).toBe(true)
    })

    it('invalid emails', () => {
      expect(validateEmail('invalid')).toBe(false)
      expect(validateEmail('@domain.com')).toBe(false)
      expect(validateEmail('')).toBe(false)
    })
  })

  describe('validatePassword', () => {
    it('valid passwords', () => {
      expect(validatePassword('Password@123')).toBe(true)
      expect(validatePassword('Abcdef1!')).toBe(true)
      expect(validatePassword('Test$1234')).toBe(true)
    })

    it('invalid passwords', () => {
      expect(validatePassword('short')).toBe(false)
      expect(validatePassword('nouppercase1!')).toBe(false)
      expect(validatePassword('NOLOWERCASE1!')).toBe(false)
      expect(validatePassword('NoNumber!')).toBe(false)
      expect(validatePassword('NoSpecial123')).toBe(false)
    })
  })

  describe('validateUsername', () => {
    it('valid usernames', () => {
      expect(validateUsername('abc')).toBe(true)
      expect(validateUsername('testuser')).toBe(true)
    })

    it('invalid usernames', () => {
      expect(validateUsername('ab')).toBe(false)
      expect(validateUsername('')).toBe(false)
    })
  })

  describe('validatePrice', () => {
    it('valid prices', () => {
      expect(validatePrice(100)).toBe(true)
      expect(validatePrice('50.5')).toBe(true)
    })

    it('invalid prices', () => {
      expect(validatePrice(0)).toBe(false)
      expect(validatePrice(-10)).toBe(false)
      expect(validatePrice(null)).toBe(false)
    })
  })
})
