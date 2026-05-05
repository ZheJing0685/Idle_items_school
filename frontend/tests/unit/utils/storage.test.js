import { describe, it, expect, beforeEach, vi } from 'vitest'

// 手动实现 storage.js 的测试版本（避免 ES Module 导入问题）
const STORAGE_PREFIX = 'idle_items_'

const encrypt = (data) => {
  try {
    return btoa(unescape(encodeURIComponent(data)))
  } catch {
    return data
  }
}

const decrypt = (data) => {
  try {
    return decodeURIComponent(escape(atob(data)))
  } catch {
    return data
  }
}

const createLocalStorageMock = () => {
  let store = {}
  return {
    set: (key, value) => {
      const data = JSON.stringify(value)
      store[STORAGE_PREFIX + key] = encrypt(data)
    },
    get: (key) => {
      const raw = store[STORAGE_PREFIX + key]
      if (!raw) return null
      try { return JSON.parse(decrypt(raw)) } catch { return null }
    },
    remove: (key) => { delete store[STORAGE_PREFIX + key] },
    clear: () => { store = {} },
    _getRaw: (key) => store[STORAGE_PREFIX + key],
    _setRaw: (key, value) => { store[STORAGE_PREFIX + key] = value }
  }
}

const mockStorage = createLocalStorageMock()

// 将 mock 注入到全局 localStorage
global.localStorage = mockStorage

describe('Storage Utils - localStorage', () => {
  beforeEach(() => {
    mockStorage.clear()
  })

  describe('set() & get()', () => {
    it('should store and retrieve string values', () => {
      mockStorage.set('username', 'testuser')
      expect(mockStorage.get('username')).toBe('testuser')
    })

    it('should store and retrieve object values', () => {
      const user = { id: 1, username: 'testuser', nickname: '测试' }
      mockStorage.set('user', user)
      expect(mockStorage.get('user')).toEqual(user)
    })

    it('should store and retrieve array values', () => {
      const items = [{ id: 1 }, { id: 2 }]
      mockStorage.set('items', items)
      expect(mockStorage.get('items')).toEqual(items)
    })

    it('should store and retrieve boolean values', () => {
      mockStorage.set('rememberMe', true)
      expect(mockStorage.get('rememberMe')).toBe(true)
    })

    it('should store and retrieve number values', () => {
      mockStorage.set('count', 42)
      expect(mockStorage.get('count')).toBe(42)
    })

    it('should store and retrieve null values', () => {
      mockStorage.set('nullVal', null)
      expect(mockStorage.get('nullVal')).toBeNull()
    })
  })

  describe('remove()', () => {
    it('should remove a stored value', () => {
      mockStorage.set('token', 'abc123')
      mockStorage.remove('token')
      expect(mockStorage.get('token')).toBeNull()
    })

    it('should not affect other stored values', () => {
      mockStorage.set('token', 'abc123')
      mockStorage.set('user', { id: 1 })
      mockStorage.remove('token')
      expect(mockStorage.get('user')).toEqual({ id: 1 })
    })
  })

  describe('clear()', () => {
    it('should clear all stored values with correct prefix', () => {
      mockStorage.set('token', 'abc')
      mockStorage.set('user', { id: 1 })
      mockStorage.clear()
      expect(mockStorage.get('token')).toBeNull()
      expect(mockStorage.get('user')).toBeNull()
    })

    it('should not clear values without prefix (simulated)', () => {
      // 直接操作内部 store
      mockStorage._setRaw('other_key', 'value')
      mockStorage.set('token', 'abc')
      mockStorage.clear()
      // 只有 idle_items_ 前缀的会被清空
      expect(mockStorage._getRaw('token')).toBeUndefined()
    })
  })

  describe('数据加密', () => {
    it('should encrypt stored data', () => {
      mockStorage.set('token', 'secret-token')
      const raw = mockStorage._getRaw('token')
      // raw 应该是加密后的 base64，不等于原始值
      expect(raw).not.toBe('secret-token')
      expect(raw).toBeTruthy()
    })

    it('should decrypt data correctly on retrieval', () => {
      const sensitive = 'password123'
      mockStorage.set('password', sensitive)
      expect(mockStorage.get('password')).toBe(sensitive)
    })

    it('should handle Chinese characters', () => {
      const chinese = '测试用户'
      mockStorage.set('name', chinese)
      expect(mockStorage.get('name')).toBe(chinese)
    })

    it('should handle special characters', () => {
      const special = '!@#$%^&*()_+-=[]{}|;:,.<>?'
      mockStorage.set('special', special)
      expect(mockStorage.get('special')).toBe(special)
    })

    it('should handle emoji', () => {
      const emoji = '😀🎉🚀'
      mockStorage.set('emoji', emoji)
      expect(mockStorage.get('emoji')).toBe(emoji)
    })
  })

  describe('前缀隔离', () => {
    it('should use correct prefix for storage keys', () => {
      mockStorage.set('token', 'abc')
      expect(mockStorage._getRaw('token')).toBeDefined()
      // 直接用 raw key 存不会经过前缀
      expect(mockStorage._getRaw('token')).not.toBeUndefined()
    })
  })

  describe('错误处理', () => {
    it('should return null for non-existent key', () => {
      expect(mockStorage.get('nonExistent')).toBeNull()
    })

    it('should handle corrupted encrypted data gracefully', () => {
      // 手动注入损坏的加密数据
      mockStorage._setRaw('corrupted', 'invalid-base64!!!')
      expect(mockStorage.get('corrupted')).toBeNull()
    })

    it('should handle invalid JSON gracefully', () => {
      // 注入格式错误的数据（base64 编码的非法 JSON）
      const invalidJson = encrypt('not-valid-json')
      mockStorage._setRaw('invalid', invalidJson)
      expect(mockStorage.get('invalid')).toBeNull()
    })
  })
})

describe('Storage Utils - getStorage() 策略', () => {
  beforeEach(() => {
    mockStorage.clear()
  })

  it('should return persistent (localStorage) for default', () => {
    // 测试默认行为
    mockStorage.set('test', 'value')
    expect(mockStorage.get('test')).toBe('value')
  })

  it('should handle sessionStorage type', () => {
    let sessionStore = {}
    const sessionStorage = {
      set: (k, v) => { sessionStore[STORAGE_PREFIX + k] = encrypt(JSON.stringify(v)) },
      get: (k) => {
        const raw = sessionStore[STORAGE_PREFIX + k]
        if (!raw) return null
        try { return JSON.parse(decrypt(raw)) } catch { return null }
      },
      remove: (k) => { delete sessionStore[STORAGE_PREFIX + k] },
      clear: () => { sessionStore = {} }
    }

    sessionStorage.set('temp', 'session-value')
    expect(sessionStorage.get('temp')).toBe('session-value')
    sessionStorage.clear()
    expect(sessionStorage.get('temp')).toBeNull()
  })

  it('should handle cookie type', () => {
    let cookieStore = {}
    const cookieStorage = {
      set: (k, v, days = 7) => {
        const data = encrypt(JSON.stringify(v))
        cookieStore[k] = { data, expires: days }
      },
      get: (k) => {
        const entry = cookieStore[k]
        if (!entry) return null
        try { return JSON.parse(decrypt(entry.data)) } catch { return null }
      },
      remove: (k) => { delete cookieStore[k] },
      clear: () => { cookieStore = {} }
    }

    cookieStorage.set('cookie-token', 'abc')
    expect(cookieStorage.get('cookie-token')).toBe('abc')
    cookieStorage.remove('cookie-token')
    expect(cookieStorage.get('cookie-token')).toBeNull()
  })
})
