package com.idleitems.school.service;

import com.idleitems.school.module.item.entity.Item;
import com.idleitems.school.module.item.repository.ItemRepository;
import com.idleitems.school.module.item.service.ItemAdminService;
import com.idleitems.school.module.order.repository.OrderRepository;
import com.idleitems.school.shared.cache.CacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemAdminServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private ItemAdminService itemAdminService;

    private Item testItem;

    @BeforeEach
    void setUp() {
        testItem = new Item();
        testItem.setId(1L);
        testItem.setUserId(10L);
        testItem.setTitle("测试物品");
        testItem.setStatus(Item.ItemStatus.PENDING);
    }

    @Test
    void approveItem_withValidId_approvesItem() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Item result = itemAdminService.approveItem(1L);

        assertNotNull(result);
        assertEquals(Item.ItemStatus.ON_SALE, result.getStatus());
        verify(itemRepository, times(1)).findById(1L);
        verify(itemRepository, times(1)).save(any(Item.class));
        verify(cacheService, times(1)).delete(CacheService.getItemKey(1L));
        verify(cacheService, times(1)).deletePattern("item:list:*");
        verify(cacheService, times(1)).deletePattern("item:hot");
    }

    @Test
    void approveItem_whenNotFound_throwsException() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            itemAdminService.approveItem(999L);
        });

        verify(itemRepository, times(1)).findById(999L);
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void rejectItem_withValidIdAndReason_rejectsItem() {
        String reason = "图片不清晰";

        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Item result = itemAdminService.rejectItem(1L, reason);

        assertNotNull(result);
        assertEquals(Item.ItemStatus.REJECTED, result.getStatus());
        assertEquals("图片不清晰", result.getRejectReason());
        verify(itemRepository, times(1)).findById(1L);
        verify(itemRepository, times(1)).save(any(Item.class));
        verify(cacheService, times(1)).delete(CacheService.getItemKey(1L));
    }

    @Test
    void rejectItem_whenNotFound_throwsException() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            itemAdminService.rejectItem(999L, "违规内容");
        });

        verify(itemRepository, times(1)).findById(999L);
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void forceOffShelfItem_withValidIdAndReason_offShelvesItem() {
        String reason = "违规商品";
        testItem.setStatus(Item.ItemStatus.ON_SALE);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Item result = itemAdminService.forceOffShelfItem(1L, reason);

        assertNotNull(result);
        assertEquals(Item.ItemStatus.OFF_SHELF, result.getStatus());
        assertEquals("违规商品", result.getRejectReason());
        verify(itemRepository, times(1)).findById(1L);
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void existsOrderByItemId_whenExists_returnsTrue() {
        when(orderRepository.existsByItemId(1L)).thenReturn(true);

        boolean result = itemAdminService.existsOrderByItemId(1L);

        assertTrue(result);
        verify(orderRepository, times(1)).existsByItemId(1L);
    }

    @Test
    void existsOrderByItemId_whenNotExists_returnsFalse() {
        when(orderRepository.existsByItemId(1L)).thenReturn(false);

        boolean result = itemAdminService.existsOrderByItemId(1L);

        assertFalse(result);
        verify(orderRepository, times(1)).existsByItemId(1L);
    }

    @Test
    void deleteItemById_withValidId_deletesItem() {
        itemAdminService.deleteItemById(1L);

        verify(itemRepository, times(1)).deleteById(1L);
        verify(cacheService, times(1)).delete(CacheService.getItemKey(1L));
        verify(cacheService, times(1)).deletePattern("item:list:*");
        verify(cacheService, times(1)).deletePattern("item:hot");
    }

    @Test
    void getAdminItems_withStatus_returnsFilteredPage() {
        Page<Item> expectedPage = new PageImpl<>(List.of(testItem));
        when(itemRepository.findByStatus(Item.ItemStatus.PENDING, Pageable.unpaged())).thenReturn(expectedPage);

        Page<Item> result = itemAdminService.getAdminItems(Pageable.unpaged(), Item.ItemStatus.PENDING);

        assertEquals(1, result.getTotalElements());
        verify(itemRepository, times(1)).findByStatus(Item.ItemStatus.PENDING, Pageable.unpaged());
        verify(itemRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getAdminItems_withoutStatus_returnsAllPage() {
        Page<Item> expectedPage = new PageImpl<>(List.of(testItem));
        when(itemRepository.findAll(any(Pageable.class))).thenReturn(expectedPage);

        Page<Item> result = itemAdminService.getAdminItems(Pageable.unpaged(), null);

        assertEquals(1, result.getTotalElements());
        verify(itemRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void countItems_returnsCount() {
        when(itemRepository.count()).thenReturn(42L);

        long result = itemAdminService.countItems();

        assertEquals(42L, result);
        verify(itemRepository, times(1)).count();
    }

    @Test
    void countItemsByStatus_returnsCount() {
        when(itemRepository.countByStatus(Item.ItemStatus.PENDING)).thenReturn(5L);

        long result = itemAdminService.countItemsByStatus(Item.ItemStatus.PENDING);

        assertEquals(5L, result);
        verify(itemRepository, times(1)).countByStatus(Item.ItemStatus.PENDING);
    }
}
