package com.idleitems.school.task;

import com.idleitems.school.module.item.entity.Item;
import com.idleitems.school.module.item.repository.ItemRepository;
import com.idleitems.school.shared.task.FavoriteCountSyncTask;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FavoriteCountSyncTask 收藏计数同步任务测试")
class FavoriteCountSyncTaskTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private FavoriteCountSyncTask favoriteCountSyncTask;

    private Item createItem(Long id, int favoriteCount) {
        Item item = new Item();
        item.setId(id);
        item.setFavoriteCount(favoriteCount);
        return item;
    }

    @Test
    @DisplayName("没有物品时同步任务直接结束")
    void syncFavoriteCounts_noItems_finishesImmediately() {
        when(itemRepository.findByStatus(eq(Item.ItemStatus.ON_SALE), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));

        favoriteCountSyncTask.syncFavoriteCounts();

        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    @DisplayName("同步时更新需要更新的物品")
    void syncFavoriteCounts_someItemsNeedUpdate_savesChanges() {
        Item item1 = createItem(1L, 5);
        Item item2 = createItem(2L, 10);
        when(itemRepository.findByStatus(eq(Item.ItemStatus.ON_SALE), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(item1, item2), PageRequest.of(0, 1000), 2));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("item:favorite:1")).thenReturn(20);
        when(valueOperations.get("item:favorite:2")).thenReturn(10);

        favoriteCountSyncTask.syncFavoriteCounts();

        assertEquals(20, item1.getFavoriteCount());
        assertEquals(10, item2.getFavoriteCount());
        verify(itemRepository).save(item1);
        verify(itemRepository, never()).save(item2);
    }

    @Test
    @DisplayName("所有物品计数一致时无需更新")
    void syncFavoriteCounts_allMatch_skipsSave() {
        Item item = createItem(1L, 42);
        when(itemRepository.findByStatus(eq(Item.ItemStatus.ON_SALE), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(item), PageRequest.of(0, 1000), 1));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("item:favorite:1")).thenReturn(42);

        favoriteCountSyncTask.syncFavoriteCounts();

        verify(itemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Redis中没有缓存数据时不更新")
    void syncFavoriteCounts_noCache_skipsItem() {
        Item item = createItem(1L, 5);
        when(itemRepository.findByStatus(eq(Item.ItemStatus.ON_SALE), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(item), PageRequest.of(0, 1000), 1));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("item:favorite:1")).thenReturn(null);

        favoriteCountSyncTask.syncFavoriteCounts();

        verify(itemRepository, never()).save(any());
    }

    @Test
    @DisplayName("异常被捕获不向外抛出")
    void syncFavoriteCounts_exception_caughtByTask() {
        when(itemRepository.findByStatus(eq(Item.ItemStatus.ON_SALE), any(PageRequest.class)))
                .thenThrow(new RuntimeException("数据库连接失败"));

        favoriteCountSyncTask.syncFavoriteCounts();

        verify(itemRepository, never()).save(any());
    }

    @Test
    @DisplayName("多批次分页处理")
    void syncFavoriteCounts_multipleBatches_processesAllPages() {
        List<Item> batch1 = IntStream.range(0, 1000)
                .mapToObj(i -> createItem((long) i, i)).toList();
        List<Item> batch2 = List.of(createItem(1000L, 1000));

        when(itemRepository.findByStatus(eq(Item.ItemStatus.ON_SALE), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(batch1, PageRequest.of(0, 1000), 1001))
                .thenReturn(new PageImpl<>(batch2, PageRequest.of(1, 1000), 1001));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        favoriteCountSyncTask.syncFavoriteCounts();

        verify(itemRepository, times(2)).findByStatus(eq(Item.ItemStatus.ON_SALE), any(PageRequest.class));
        verify(redisTemplate, times(1001)).opsForValue();
    }
}
