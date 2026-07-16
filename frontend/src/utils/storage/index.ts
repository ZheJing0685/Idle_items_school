import { logger } from '../logger';

interface ExpiryItem<T = unknown> {
  value: T
  expiry: number
}

class Storage {
  private namespace: string;

  constructor(namespace: string = 'app') {
    this.namespace = namespace;
  }

  get<T = unknown>(key: string): T | null {
    try {
      const item = localStorage.getItem(`${this.namespace}:${key}`);
      return item ? JSON.parse(item) as T : null;
    } catch (error) {
      logger.error('Storage get error:', error);
      return null;
    }
  }

  set(key: string, value: unknown): boolean {
    try {
      localStorage.setItem(`${this.namespace}:${key}`, JSON.stringify(value));
      return true;
    } catch (error) {
      logger.error('Storage set error:', error);
      return false;
    }
  }

  remove(key: string): boolean {
    try {
      localStorage.removeItem(`${this.namespace}:${key}`);
      return true;
    } catch (error) {
      logger.error('Storage remove error:', error);
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
      logger.error('Storage clear error:', error);
      return false;
    }
  }

  getAll(): Record<string, unknown> {
    try {
      const result: Record<string, unknown> = {};
      const keys = Object.keys(localStorage);
      keys.forEach((key) => {
        if (key.startsWith(`${this.namespace}:`)) {
          const actualKey = key.replace(`${this.namespace}:`, '');
          result[actualKey] = this.get(actualKey);
        }
      });
      return result;
    } catch (error) {
      logger.error('Storage getAll error:', error);
      return {};
    }
  }

  has(key: string): boolean {
    return this.get(key) !== null;
  }

  setWithExpiry<T = unknown>(key: string, value: T, ttl: number): boolean {
    try {
      const item: ExpiryItem<T> = {
        value,
        expiry: new Date().getTime() + ttl,
      };
      return this.set(key, item);
    } catch (error) {
      logger.error('Storage setWithExpiry error:', error);
      return false;
    }
  }

  getWithExpiry<T = unknown>(key: string): T | null {
    try {
      const item = this.get<ExpiryItem<T>>(key);
      if (!item) return null;

      if (new Date().getTime() > item.expiry) {
        this.remove(key);
        return null;
      }
      return item.value;
    } catch (error) {
      logger.error('Storage getWithExpiry error:', error);
      return null;
    }
  }
}

const storage = (namespace?: string) => new Storage(namespace);

export default storage;
