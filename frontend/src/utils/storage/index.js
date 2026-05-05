class Storage {
  constructor(namespace = 'app') {
    this.namespace = namespace;
  }

  get(key) {
    try {
      const item = localStorage.getItem(`${this.namespace}:${key}`);
      return item ? JSON.parse(item) : null;
    } catch (error) {
      console.error('Storage get error:', error);
      return null;
    }
  }

  set(key, value) {
    try {
      localStorage.setItem(`${this.namespace}:${key}`, JSON.stringify(value));
      return true;
    } catch (error) {
      console.error('Storage set error:', error);
      return false;
    }
  }

  remove(key) {
    try {
      localStorage.removeItem(`${this.namespace}:${key}`);
      return true;
    } catch (error) {
      console.error('Storage remove error:', error);
      return false;
    }
  }

  clear() {
    try {
      const keys = Object.keys(localStorage);
      keys.forEach(key => {
        if (key.startsWith(`${this.namespace}:`)) {
          localStorage.removeItem(key);
        }
      });
      return true;
    } catch (error) {
      console.error('Storage clear error:', error);
      return false;
    }
  }

  getAll() {
    try {
      const result = {};
      const keys = Object.keys(localStorage);
      keys.forEach(key => {
        if (key.startsWith(`${this.namespace}:`)) {
          const actualKey = key.replace(`${this.namespace}:`, '');
          result[actualKey] = this.get(actualKey);
        }
      });
      return result;
    } catch (error) {
      console.error('Storage getAll error:', error);
      return {};
    }
  }

  has(key) {
    return this.get(key) !== null;
  }

  setWithExpiry(key, value, ttl) {
    try {
      const item = {
        value,
        expiry: new Date().getTime() + ttl
      };
      return this.set(key, item);
    } catch (error) {
      console.error('Storage setWithExpiry error:', error);
      return false;
    }
  }

  getWithExpiry(key) {
    try {
      const item = this.get(key);
      if (!item) return null;
      
      if (new Date().getTime() > item.expiry) {
        this.remove(key);
        return null;
      }
      return item.value;
    } catch (error) {
      console.error('Storage getWithExpiry error:', error);
      return null;
    }
  }
}

const storage = (namespace) => new Storage(namespace);

export default storage;
