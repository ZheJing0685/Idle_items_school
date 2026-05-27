interface CacheEntry<T = any> {
  data: T
  timestamp: number
  expiry: number
}

interface RequestOptions {
  useCache?: boolean
  useMerge?: boolean
  params?: Record<string, any>
  cacheKey?: string
  cacheExpiry?: number
}

interface BatchRequest {
  url: string
  requestFn: () => Promise<any>
  useCache?: boolean
  useMerge?: boolean
  params?: Record<string, any>
  cacheKey?: string
  cacheExpiry?: number
}

class RequestManager {
  private cache: Map<string, CacheEntry>;
  private requests: Map<string, Promise<any>>;
  private cacheSize: number;
  private defaultCacheExpiry: number;

  constructor() {
    this.cache = new Map();
    this.requests = new Map();
    this.cacheSize = 100;
    this.defaultCacheExpiry = 5 * 60 * 1000;
  }

  async request<T = any>(
    url: string,
    requestFn: () => Promise<T>,
    options: RequestOptions = {},
  ): Promise<T> {
    const {
      useCache = false,
      useMerge = false,
      params,
      cacheKey,
      cacheExpiry = this.defaultCacheExpiry,
    } = options;
    const key = cacheKey || this.generateKey(url, params);

    if (useCache) {
      const cachedData = this.getCache(key);
      if (cachedData && !this.isCacheExpired(cachedData)) {
        return cachedData.data as T;
      }
    }

    if (useMerge && this.requests.has(key)) {
      return this.requests.get(key)! as Promise<T>;
    }

    const promise = requestFn()
      .then((response: T) => {
        this.requests.delete(key);
        if (useCache) {
          this.setCache(key, response, cacheExpiry);
        }
        return response;
      })
      .catch((error: any) => {
        this.requests.delete(key);
        throw error;
      });

    if (useMerge) {
      this.requests.set(key, promise);
    }

    return promise;
  }

  async batchRequest(requests: BatchRequest[]): Promise<any[]> {
    const results: any[] = [];
    const requestMap = new Map<string, Promise<any>>();

    for (const req of requests) {
      const {
        url,
        params,
        requestFn,
        useCache = false,
        useMerge = true,
        cacheKey,
        cacheExpiry,
      } = req;
      const key = cacheKey || this.generateKey(url, params);

      if (!requestMap.has(key)) {
        requestMap.set(
          key,
          this.request(url, requestFn, {
            useCache,
            useMerge,
            params,
            cacheKey,
            cacheExpiry,
          }),
        );
      }
    }

    for (const [, promise] of requestMap) {
      try {
        results.push(await promise);
      } catch (error) {
        results.push(error);
      }
    }

    return results;
  }

  cancelAllRequests(): void {
    this.requests.clear();
  }

  cancelRequest(url: string, params?: Record<string, any>): void {
    const key = this.generateKey(url, params);
    this.requests.delete(key);
  }

  generateKey(url: string, params?: Record<string, any>): string {
    if (!params) return url;
    const sortedParams = Object.keys(params)
      .sort()
      .map((key) => `${key}=${params[key]}`)
      .join('&');
    return `${url}?${sortedParams}`;
  }

  setCache(key: string, value: any, expiry: number = this.defaultCacheExpiry): void {
    if (this.cache.size >= this.cacheSize) {
      this.evictOldestCache();
    }
    this.cache.set(key, {
      data: value,
      timestamp: Date.now(),
      expiry,
    });
  }

  getCache(key: string): CacheEntry | undefined {
    return this.cache.get(key);
  }

  isCacheExpired(cachedData: CacheEntry): boolean {
    return Date.now() - cachedData.timestamp > cachedData.expiry;
  }

  evictOldestCache(): void {
    let oldestKey: string | null = null;
    let oldestTimestamp = Infinity;

    for (const [key, value] of this.cache.entries()) {
      if (value.timestamp < oldestTimestamp) {
        oldestKey = key;
        oldestTimestamp = value.timestamp;
      }
    }

    if (oldestKey) {
      this.cache.delete(oldestKey);
    }
  }

  clearCache(url: string, params?: Record<string, any>): void {
    const key = this.generateKey(url, params);
    this.cache.delete(key);
  }

  clearAllCache(): void {
    this.cache.clear();
  }

  clearCacheByPattern(pattern: string): void {
    for (const key of this.cache.keys()) {
      if (key.includes(pattern)) {
        this.cache.delete(key);
      }
    }
  }

  getCacheSize(): number {
    return this.cache.size;
  }

  setCacheSize(size: number): void {
    this.cacheSize = size;
    while (this.cache.size > size) {
      this.evictOldestCache();
    }
  }

  setDefaultCacheExpiry(expiry: number): void {
    this.defaultCacheExpiry = expiry;
  }

  getDefaultCacheExpiry(): number {
    return this.defaultCacheExpiry;
  }
}

const requestManager = new RequestManager();

export default requestManager;
