class RequestManager {
  constructor() {
    this.cache = new Map();
    this.requests = new Map();
    this.cacheSize = 100;
    this.defaultCacheExpiry = 5 * 60 * 1000; // 默认缓存过期时间：5分钟
  }

  async request(url, requestFn, options = {}) {
    const { useCache = false, useMerge = false, params, cacheKey, cacheExpiry = this.defaultCacheExpiry } = options;
    const key = cacheKey || this.generateKey(url, params);

    if (useCache) {
      const cachedData = this.getCache(key);
      if (cachedData && !this.isCacheExpired(cachedData)) {
        return cachedData.data;
      }
    }

    if (useMerge && this.requests.has(key)) {
      return this.requests.get(key);
    }

    const promise = requestFn().then(response => {
      this.requests.delete(key);
      if (useCache) {
        this.setCache(key, response, cacheExpiry);
      }
      return response;
    }).catch(error => {
      this.requests.delete(key);
      throw error;
    });

    if (useMerge) {
      this.requests.set(key, promise);
    }

    return promise;
  }

  /**
   * 批量请求处理，避免重复请求
   * @param {Array} requests - 请求配置数组
   * @returns {Promise<Array>} - 请求结果数组
   */
  async batchRequest(requests) {
    const results = [];
    const requestMap = new Map();

    // 首先收集所有请求，避免重复
    for (const req of requests) {
      const { url, params, requestFn, useCache = false, useMerge = true, cacheKey, cacheExpiry } = req;
      const key = cacheKey || this.generateKey(url, params);

      if (!requestMap.has(key)) {
        requestMap.set(key, this.request(url, requestFn, { useCache, useMerge, params, cacheKey, cacheExpiry }));
      }
    }

    // 执行所有唯一请求
    for (const [key, promise] of requestMap) {
      try {
        results.push(await promise);
      } catch (error) {
        results.push(error);
      }
    }

    return results;
  }

  /**
   * 取消所有未完成的请求
   */
  cancelAllRequests() {
    this.requests.clear();
  }

  /**
   * 取消特定请求
   * @param {string} url - 请求URL
   * @param {object} params - 请求参数
   */
  cancelRequest(url, params) {
    const key = this.generateKey(url, params);
    this.requests.delete(key);
  }

  generateKey(url, params) {
    if (!params) return url;
    const sortedParams = Object.keys(params)
      .sort()
      .map(key => `${key}=${params[key]}`)
      .join('&');
    return `${url}?${sortedParams}`;
  }

  setCache(key, value, expiry = this.defaultCacheExpiry) {
    if (this.cache.size >= this.cacheSize) {
      this.evictOldestCache();
    }
    this.cache.set(key, {
      data: value,
      timestamp: Date.now(),
      expiry: expiry
    });
  }

  getCache(key) {
    return this.cache.get(key);
  }

  isCacheExpired(cachedData) {
    return Date.now() - cachedData.timestamp > cachedData.expiry;
  }

  evictOldestCache() {
    let oldestKey = null;
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

  clearCache(url, params) {
    const key = this.generateKey(url, params);
    this.cache.delete(key);
  }

  clearAllCache() {
    this.cache.clear();
  }

  clearCacheByPattern(pattern) {
    for (const key of this.cache.keys()) {
      if (key.includes(pattern)) {
        this.cache.delete(key);
      }
    }
  }

  getCacheSize() {
    return this.cache.size;
  }

  setCacheSize(size) {
    this.cacheSize = size;
    while (this.cache.size > size) {
      this.evictOldestCache();
    }
  }

  setDefaultCacheExpiry(expiry) {
    this.defaultCacheExpiry = expiry;
  }

  getDefaultCacheExpiry() {
    return this.defaultCacheExpiry;
  }
}

const requestManager = new RequestManager();

export default requestManager;
