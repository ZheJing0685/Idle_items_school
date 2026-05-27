package com.idleitems.school.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.SetOperations;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CacheService 单元测试")
class CacheServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private SetOperations<String, Object> setOperations;

    @InjectMocks
    private CacheService cacheService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }

    @Test
    @DisplayName("测试设置缓存（带过期时间）")
    void testSetWithTimeout() {
        cacheService.set("testKey", "testValue", 60, TimeUnit.SECONDS);
        verify(valueOperations).set("testKey", "testValue", 60, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("测试设置缓存（无过期时间）")
    void testSetWithoutTimeout() {
        cacheService.set("testKey", "testValue");
        verify(valueOperations).set("testKey", "testValue");
    }

    @Test
    @DisplayName("测试获取缓存（带类型）")
    void testGetWithType() {
        when(valueOperations.get("testKey")).thenReturn("testValue");
        String result = cacheService.get("testKey", String.class);
        assertEquals("testValue", result);
    }

    @Test
    @DisplayName("测试获取缓存（无类型）")
    void testGetWithoutType() {
        when(valueOperations.get("testKey")).thenReturn("testValue");
        Object result = cacheService.get("testKey");
        assertEquals("testValue", result);
    }

    @Test
    @DisplayName("测试检查键是否存在")
    void testExists() {
        when(redisTemplate.hasKey("testKey")).thenReturn(true);
        assertTrue(cacheService.exists("testKey"));
    }

    @Test
    @DisplayName("测试删除缓存")
    void testDelete() {
        cacheService.delete("testKey");
        verify(redisTemplate).delete("testKey");
    }

    @Test
    @DisplayName("测试设置过期时间")
    void testExpire() {
        cacheService.expire("testKey", 60, TimeUnit.SECONDS);
        verify(redisTemplate).expire("testKey", 60, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("测试获取过期时间")
    void testGetExpire() {
        when(redisTemplate.getExpire("testKey", TimeUnit.SECONDS)).thenReturn(60L);
        long expire = cacheService.getExpire("testKey");
        assertEquals(60L, expire);
    }

    @Test
    @DisplayName("测试递增操作")
    void testIncrement() {
        cacheService.increment("testKey");
        verify(valueOperations).increment("testKey");
    }

    @Test
    @DisplayName("测试递增指定值")
    void testIncrementWithDelta() {
        cacheService.increment("testKey", 5);
        verify(valueOperations).increment("testKey", 5);
    }

    @Test
    @DisplayName("测试递减操作")
    void testDecrement() {
        cacheService.decrement("testKey");
        verify(valueOperations).decrement("testKey");
    }

    @Test
    @DisplayName("测试添加到集合")
    void testAddToSet() {
        cacheService.addToSet("testSet", "value1", "value2");
        verify(setOperations).add("testSet", "value1", "value2");
    }

    @Test
    @DisplayName("测试获取集合成员")
    void testGetSet() {
        Set<Object> expectedSet = Set.of("value1", "value2");
        when(setOperations.members("testSet")).thenReturn(expectedSet);
        Set<Object> result = cacheService.getSet("testSet");
        assertEquals(expectedSet, result);
    }

    @Test
    @DisplayName("测试检查集合成员")
    void testIsMemberOfSet() {
        when(setOperations.isMember("testSet", "value1")).thenReturn(true);
        assertTrue(cacheService.isMemberOfSet("testSet", "value1"));
    }

    @Test
    @DisplayName("测试生成物品缓存键")
    void testGetItemKey() {
        String key = CacheService.getItemKey(1L);
        assertEquals("item:1:detail", key);
    }

    @Test
    @DisplayName("测试生成用户缓存键")
    void testGetUserKey() {
        String key = CacheService.getUserKey(1L);
        assertEquals("user:1:detail", key);
    }

    @Test
    @DisplayName("测试生成分类缓存键")
    void testGetCategoryKey() {
        String key = CacheService.getCategoryKey(1L);
        assertEquals("category:1:detail", key);
    }

    @Test
    @DisplayName("测试生成订单缓存键")
    void testGetOrderKey() {
        String key = CacheService.getOrderKey(1L);
        assertEquals("order:1:detail", key);
    }
}