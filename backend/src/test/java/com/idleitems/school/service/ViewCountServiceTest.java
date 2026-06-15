package com.idleitems.school.service;

import com.idleitems.school.module.item.repository.ItemRepository;
import com.idleitems.school.module.item.service.ViewCountService;
import com.idleitems.school.shared.cache.CacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ViewCountServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private ViewCountService viewCountService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void increment_WhenValidItemId_IncrementsCount() {
        Long itemId = 1L;

        viewCountService.increment(itemId);

        verify(itemRepository, times(1)).incrementViewCount(itemId);
    }

    @Test
    void increment_WhenValidItemId_DeletesItemCache() {
        Long itemId = 1L;

        viewCountService.increment(itemId);

        verify(cacheService, times(1)).delete(CacheService.getItemKey(itemId));
    }

    @Test
    void increment_WhenValidItemId_DeletesHotItemsCache() {
        Long itemId = 1L;

        viewCountService.increment(itemId);

        verify(cacheService, times(1)).delete(CacheService.getHotItemsKey());
    }

    @Test
    void increment_WhenRepositoryThrowsException_DoesNotPropagate() {
        Long itemId = 1L;
        doThrow(new RuntimeException("DB error")).when(itemRepository).incrementViewCount(itemId);

        assertDoesNotThrow(() -> viewCountService.increment(itemId));

        verify(itemRepository, times(1)).incrementViewCount(itemId);
    }

    @Test
    void increment_WhenCacheDeleteThrowsException_DoesNotPropagate() {
        Long itemId = 1L;
        doThrow(new RuntimeException("Redis error")).when(cacheService).delete(anyString());

        assertDoesNotThrow(() -> viewCountService.increment(itemId));

        verify(itemRepository, times(1)).incrementViewCount(itemId);
    }

    @Test
    void increment_CallsBothCacheDeletes() {
        Long itemId = 1L;

        viewCountService.increment(itemId);

        verify(cacheService, times(2)).delete(anyString());
        verify(cacheService).delete(CacheService.getItemKey(itemId));
        verify(cacheService).delete(CacheService.getHotItemsKey());
    }
}
