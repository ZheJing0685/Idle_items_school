package com.idleitems.school.service;

import com.idleitems.school.module.item.repository.ItemRepository;
import com.idleitems.school.module.item.service.ViewCountService;
import com.idleitems.school.shared.cache.CacheService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ViewCountServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CacheService cacheService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private ViewCountService viewCountService;

    @Test
    void increment_WhenRedisWorks_BuffersCount() {
        Long itemId = 1L;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(1L);

        viewCountService.increment(itemId);

        verify(valueOperations).increment("view:buffer:1");
        verify(cacheService).delete(CacheService.getItemKey(itemId));
        verify(cacheService, never()).delete(CacheService.getHotItemsKey());
        verify(itemRepository, never()).incrementViewCountBy(anyLong(), anyInt());
    }

    @Test
    void increment_WhenRedisFails_FallsBackToDirectDbWrite() {
        Long itemId = 1L;
        when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("Redis down"));

        viewCountService.increment(itemId);

        verify(itemRepository).incrementViewCount(itemId);
        verify(cacheService).delete(CacheService.getItemKey(itemId));
    }

    @Test
    void increment_WhenBothRedisAndDbFail_DoesNotPropagate() {
        Long itemId = 1L;
        when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("Redis down"));
        doThrow(new RuntimeException("DB error")).when(itemRepository).incrementViewCount(itemId);

        assertDoesNotThrow(() -> viewCountService.increment(itemId));
    }

    @Test
    void increment_WhenCountReachesThreshold_FlushesToDb() {
        Long itemId = 1L;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(10L);
        when(valueOperations.get(anyString())).thenReturn("10");

        viewCountService.increment(itemId);

        verify(itemRepository).incrementViewCountBy(eq(itemId), eq(10));
    }
}
