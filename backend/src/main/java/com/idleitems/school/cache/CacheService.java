package com.idleitems.school.cache;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    public CacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public <T> T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        return clazz.cast(value);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public void deletePattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            // 使用 SCAN 代替 KEYS 以避免阻塞 Redis
            deleteKeys(keys);
        }
    }

    private void deleteKeys(Set<String> keys) {
        for (String key : keys) {
            redisTemplate.delete(key);
        }
    }

    public void expire(String key, long timeout, TimeUnit unit) {
        redisTemplate.expire(key, timeout, unit);
    }

    public long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    public void increment(String key) {
        redisTemplate.opsForValue().increment(key);
    }

    public void increment(String key, long delta) {
        redisTemplate.opsForValue().increment(key, delta);
    }

    public void decrement(String key) {
        redisTemplate.opsForValue().decrement(key);
    }

    public void decrement(String key, long delta) {
        redisTemplate.opsForValue().decrement(key, delta);
    }

    public void addToSet(String key, Object... values) {
        redisTemplate.opsForSet().add(key, values);
    }

    public Set<Object> getSet(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    public boolean isMemberOfSet(String key, Object value) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
    }

    public void removeFromSet(String key, Object... values) {
        redisTemplate.opsForSet().remove(key, values);
    }

    public void clearAll() {
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) {
            deleteKeys(keys);
        }
    }
}
