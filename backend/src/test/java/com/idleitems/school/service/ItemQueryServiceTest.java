package com.idleitems.school.service;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.entity.Item;
import com.idleitems.school.repository.ItemRepository;
import com.idleitems.school.repository.ReviewRepository;
import com.idleitems.school.repository.UserRepository;
import com.idleitems.school.cache.CacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemQueryServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private CacheService cacheService;

    @Mock
    private ViewCountService viewCountService;

    @InjectMocks
    private ItemQueryService itemQueryService;

    private Item testItem;

    @BeforeEach
    void setUp() {
        testItem = new Item();
        testItem.setId(1L);
        testItem.setTitle("测试物品");
        testItem.setDescription("测试描述");
        testItem.setPrice(new BigDecimal("99.99"));
        testItem.setStatus(Item.ItemStatus.ON_SALE);
        testItem.setViewCount(10);
        testItem.setFavoriteCount(5);
        testItem.setUserId(1L);
    }

    @Test
    void getItemById_WhenItemExists_ReturnsItem() {
        when(cacheService.get(anyString())).thenReturn(null);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemRepository.countByUserId(anyLong())).thenReturn(1L);
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        Item result = itemQueryService.getItemById(1L);

        assertNotNull(result);
        assertEquals("测试物品", result.getTitle());
        verify(itemRepository, times(1)).findById(1L);
    }

    @Test
    void getItemById_WhenItemNotExists_ThrowsException() {
        when(cacheService.get(anyString())).thenReturn(null);
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> {
            itemQueryService.getItemById(999L);
        });
    }

    @Test
    void getItemById_WhenCached_ReturnsCachedItem() {
        when(cacheService.get(anyString())).thenReturn(testItem);

        Item result = itemQueryService.getItemById(1L);

        assertNotNull(result);
        assertEquals("测试物品", result.getTitle());
        verify(itemRepository, never()).findById(anyLong());
    }

    @Test
    void getSellerItemCount_WhenCached_ReturnsCachedCount() {
        when(cacheService.get(anyString())).thenReturn(5);

        int result = itemQueryService.getSellerItemCount(1L);

        assertEquals(5, result);
        verify(itemRepository, never()).countByUserId(anyLong());
    }

    @Test
    void getSellerItemCount_WhenNotCached_ReturnsFromRepository() {
        when(cacheService.get(anyString())).thenReturn(null);
        when(itemRepository.countByUserId(1L)).thenReturn(3L);

        int result = itemQueryService.getSellerItemCount(1L);

        assertEquals(3, result);
        verify(itemRepository, times(1)).countByUserId(1L);
        verify(cacheService, times(1)).set(anyString(), anyInt(), anyLong(), any());
    }
}
