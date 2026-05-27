interface ExpiryItem<T = any> {
  value: T
  expiry: number
}

class Storage {
  private namespace: string;

  constructor(namespace: string = 'app') {
    this.namespace = namespace;
  }

  get<T = any>(key: string): T | null {
    try {
      const item = localStorage.getItem(`${this.namespace}:${key}`);
      return item ? JSON.parse(item) as T : null;
    } catch (error) {
      console.error('Storage get error:', error);
      return null;
    }
  }

  set(key: string, value: any): boolean {
    try {
      localStorage.setItem(`${this.namespace}:${key}`, JSON.stringify(value));
      return true;
    } catch (error) {
      console.error('Storage set error:', error);
      return false;
    }
  }

  remove(key: string): boolean {
    try {
      localStorage.removeItem(`${this.namespace}:${key}`);
      return true;
    } catch (error) {
      console.error('Storage remove error:', error);
      return false;
    }
  }

  clear(): boolean {
    try {
      const keys = Object.keys(localStorage);
      keys.forEach((key) => {
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

  getAll(): Record<string, any> {
    try {
      const result: Record<string, any> = {};
      const keys = Object.keys(localStorage);
      keys.forEach((key) => {
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

  has(key: string): boolean {
    return this.get(key) !== null;
  }

  setWithExpiry<T = any>(key: string, value: T, ttl: number): boolean {
    try {
      const item: ExpiryItem<T> = {
        value,
        expiry: new Date().getTime() + ttl,
      };
      return this.set(key, item);
    } catch (error) {
      console.error('Storage setWithExpiry error:', error);
      return false;
    }
  }

  getWithExpiry<T = any>(key: string): T | null {
    try {
      const item = this.get<ExpiryItem<T>>(key);
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

const storage = (namespace?: string) => new Storage(namespace);

export default storage;
