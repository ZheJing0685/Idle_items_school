package com.idleitems.school.service;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.module.category.entity.Category;
import com.idleitems.school.module.category.repository.CategoryRepository;
import com.idleitems.school.module.item.dto.ItemSummaryDTO;
import com.idleitems.school.module.item.entity.Item;
import com.idleitems.school.module.item.repository.ItemRepository;
import com.idleitems.school.module.item.service.ItemQueryService;
import com.idleitems.school.module.item.service.ViewCountService;
import com.idleitems.school.module.order.repository.ReviewRepository;
import com.idleitems.school.module.user.entity.User;
import com.idleitems.school.module.user.repository.UserRepository;
import com.idleitems.school.shared.cache.CacheService;
import com.idleitems.school.util.ItemDTOConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ItemDTOConverter dtoConverter;

    @InjectMocks
    private ItemQueryService itemQueryService;

    private Item testItem;
    private List<Object[]> emptyCount;
    private List<Object[]> singleCount;

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

        emptyCount = new ArrayList<>();
        singleCount = new ArrayList<>();
        singleCount.add(new Object[]{1L, 5L});
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

    @Test
    void getItemById_withSellerInfo_EnrichesItem() {
        when(cacheService.get(anyString())).thenReturn(null);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemRepository.countByUserId(1L)).thenReturn(7L);

        User seller = new User();
        seller.setId(1L);
        seller.setNickname("卖家昵称");
        seller.setUsername("seller123");
        seller.setVerified(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(dtoConverter.getAverageRating(1L)).thenReturn(BigDecimal.valueOf(4.5));

        Item result = itemQueryService.getItemById(1L);

        assertEquals("卖家昵称", result.getSellerNickname());
        assertTrue(result.isSellerVerified());
        assertEquals(7, result.getSellerItemsCount());
        assertEquals(4.5, result.getSellerRating(), 0.001);
    }

    @Test
    void getItemById_withSellerInfoNullNickname_usesUsername() {
        when(cacheService.get(anyString())).thenReturn(null);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemRepository.countByUserId(1L)).thenReturn(3L);

        User seller = new User();
        seller.setId(1L);
        seller.setNickname(null);
        seller.setUsername("fallbackUser");
        seller.setVerified(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(seller));

        Item result = itemQueryService.getItemById(1L);

        assertEquals("fallbackUser", result.getSellerNickname());
        assertFalse(result.isSellerVerified());
    }

    @Test
    void getItemById_withSellerInfoNullVerified_defaultsFalse() {
        when(cacheService.get(anyString())).thenReturn(null);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemRepository.countByUserId(1L)).thenReturn(2L);

        User seller = new User();
        seller.setId(1L);
        seller.setNickname("seller");
        seller.setUsername("seller");
        seller.setVerified(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(seller));

        Item result = itemQueryService.getItemById(1L);

        assertFalse(result.isSellerVerified());
    }

    @Test
    void getItemById_withSellerInfoNullRating_defaultsZero() {
        when(cacheService.get(anyString())).thenReturn(null);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemRepository.countByUserId(1L)).thenReturn(2L);

        User seller = new User();
        seller.setId(1L);
        seller.setNickname("seller");
        seller.setUsername("seller");
        seller.setVerified(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(dtoConverter.getAverageRating(1L)).thenReturn(null);

        Item result = itemQueryService.getItemById(1L);

        assertEquals(0.0, result.getSellerRating(), 0.001);
    }

    @Test
    void getItems_withCachedResult() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ItemSummaryDTO> cachedPage = new PageImpl<>(List.of(), pageable, 0);
        when(cacheService.get(anyString())).thenReturn(cachedPage);

        Page<ItemSummaryDTO> result = itemQueryService.getItems(1, 20, null, "createdAt", null, null, null);

        assertNotNull(result);
        verify(itemRepository, never()).findByFilters(any(), any(), any(), any(), any(), any());
    }

    @Test
    void getItems_withInvalidCategoryIdFormat() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Item> itemsPage = new PageImpl<>(List.of(), pageable, 0);
        when(cacheService.get(anyString())).thenReturn(null);

        when(itemRepository.findByFilters(eq(Item.ItemStatus.ON_SALE), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(itemsPage);

        Page<ItemSummaryDTO> result = itemQueryService.getItems(1, 20, "abc", "createdAt", null, null, null);

        assertNotNull(result);
        verify(itemRepository).findByFilters(eq(Item.ItemStatus.ON_SALE), isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void getItems_withCategoryId() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Item> itemsPage = new PageImpl<>(List.of(testItem), pageable, 1);
        when(cacheService.get(anyString())).thenReturn(null);

        Category category = new Category();
        category.setId(100L);
        category.setParentId(10L);
        when(categoryRepository.findById(100L)).thenReturn(Optional.of(category));

        when(itemRepository.findByFilters(eq(Item.ItemStatus.ON_SALE), eq(100L), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(itemsPage);

        User stubUser = new User();
        stubUser.setId(1L);
        stubUser.setNickname("u");
        stubUser.setUsername("u");
        when(userRepository.findAllById(anySet())).thenReturn(List.of(stubUser));
        when(itemRepository.countByUserIds(anyList())).thenReturn(singleCount);
        when(dtoConverter.toSummaryDTOList(anyList(), anyMap(), anyMap())).thenReturn(List.of());

        Page<ItemSummaryDTO> result = itemQueryService.getItems(1, 20, "100", "createdAt", null, null, null);

        assertNotNull(result);
        verify(categoryRepository).findById(100L);
    }

    @Test
    void getItems_withParentCategory_queriesSubCategories() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Item> itemsPage = new PageImpl<>(List.of(testItem), pageable, 1);
        when(cacheService.get(anyString())).thenReturn(null);

        Category parentCategory = new Category();
        parentCategory.setId(10L);
        parentCategory.setParentId(null);
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(parentCategory));

        Category sub = new Category();
        sub.setId(20L);
        sub.setParentId(10L);
        when(categoryRepository.findByParentId(10L)).thenReturn(List.of(sub));

        when(itemRepository.findByCategoryIdsAndFilters(eq(Item.ItemStatus.ON_SALE), anyList(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(itemsPage);

        User stubUser = new User();
        stubUser.setId(1L);
        stubUser.setNickname("u");
        stubUser.setUsername("u");
        when(userRepository.findAllById(anySet())).thenReturn(List.of(stubUser));
        when(itemRepository.countByUserIds(anyList())).thenReturn(singleCount);
        when(dtoConverter.toSummaryDTOList(anyList(), anyMap(), anyMap())).thenReturn(List.of());

        Page<ItemSummaryDTO> result = itemQueryService.getItems(1, 20, "10", "createdAt", null, null, null);

        assertNotNull(result);
        verify(categoryRepository).findByParentId(10L);
        verify(itemRepository).findByCategoryIdsAndFilters(eq(Item.ItemStatus.ON_SALE), anyList(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void getItems_withConditionAndDeliveryMethod() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Item> itemsPage = new PageImpl<>(List.of(testItem), pageable, 1);
        when(cacheService.get(anyString())).thenReturn(null);
        when(itemRepository.findByFilters(eq(Item.ItemStatus.ON_SALE), isNull(), eq(Item.ItemCondition.NEW), eq("1"), isNull(), any(Pageable.class)))
                .thenReturn(itemsPage);
        User stubUser = new User();
        stubUser.setId(1L);
        stubUser.setNickname("u");
        stubUser.setUsername("u");
        when(userRepository.findAllById(anySet())).thenReturn(List.of(stubUser));
        when(itemRepository.countByUserIds(anyList())).thenReturn(singleCount);
        when(dtoConverter.toSummaryDTOList(anyList(), anyMap(), anyMap())).thenReturn(List.of());

        Page<ItemSummaryDTO> result = itemQueryService.getItems(1, 20, null, "createdAt", "NEW", "1", null);

        assertNotNull(result);
        verify(itemRepository).findByFilters(eq(Item.ItemStatus.ON_SALE), isNull(), eq(Item.ItemCondition.NEW), eq("1"), isNull(), any(Pageable.class));
    }

    @Test
    void getItems_withInvalidCondition() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Item> itemsPage = new PageImpl<>(List.of(), pageable, 0);
        when(cacheService.get(anyString())).thenReturn(null);
        when(itemRepository.findByFilters(eq(Item.ItemStatus.ON_SALE), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(itemsPage);

        Page<ItemSummaryDTO> result = itemQueryService.getItems(1, 20, null, "createdAt", "INVALID_CONDITION", null, null);

        assertNotNull(result);
        verify(itemRepository).findByFilters(eq(Item.ItemStatus.ON_SALE), isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void getUserItems_withStatus() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Item> expectedPage = new PageImpl<>(List.of(testItem));
        when(itemRepository.findByUserIdAndStatus(1L, Item.ItemStatus.ON_SALE, pageable)).thenReturn(expectedPage);

        Page<Item> result = itemQueryService.getUserItems(1L, Item.ItemStatus.ON_SALE, 1, 20);

        assertEquals(1, result.getContent().size());
        verify(itemRepository).findByUserIdAndStatus(1L, Item.ItemStatus.ON_SALE, pageable);
        verify(itemRepository, never()).findByUserId(anyLong(), any());
    }

    @Test
    void getUserItems_withoutStatus() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Item> expectedPage = new PageImpl<>(List.of(testItem));
        when(itemRepository.findByUserId(1L, pageable)).thenReturn(expectedPage);

        Page<Item> result = itemQueryService.getUserItems(1L, null, 1, 20);

        assertEquals(1, result.getContent().size());
        verify(itemRepository).findByUserId(1L, pageable);
        verify(itemRepository, never()).findByUserIdAndStatus(anyLong(), any(), any());
    }

    @Test
    void searchItems_withKeyword() {
        String keyword = "test";
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Item> itemsPage = new PageImpl<>(List.of(testItem), pageable, 1);
        when(itemRepository.searchByKeyword(keyword, Item.ItemStatus.ON_SALE, pageable)).thenReturn(itemsPage);

        User stubUser = new User();
        stubUser.setId(1L);
        stubUser.setNickname("u");
        stubUser.setUsername("u");
        when(userRepository.findAllById(anySet())).thenReturn(List.of(stubUser));
        List<Object[]> countForSearch = new ArrayList<>();
        countForSearch.add(new Object[]{1L, 3L});
        when(itemRepository.countByUserIds(anyList())).thenReturn(countForSearch);
        when(dtoConverter.toSummaryDTOList(anyList(), anyMap(), anyMap())).thenReturn(List.of());

        Page<ItemSummaryDTO> result = itemQueryService.searchItems(keyword, 1, 20, "createdAt");

        assertNotNull(result);
        verify(itemRepository).searchByKeyword(keyword, Item.ItemStatus.ON_SALE, pageable);
    }

    @Test
    void searchItems_withEmptyKeyword() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Item> itemsPage = new PageImpl<>(List.of(), pageable, 0);
        when(itemRepository.searchByKeyword("", Item.ItemStatus.ON_SALE, pageable)).thenReturn(itemsPage);

        Page<ItemSummaryDTO> result = itemQueryService.searchItems("", 1, 20, "createdAt");

        assertNotNull(result);
        assertEquals(0, result.getContent().size());
    }

    @Test
    void getHotItems_withCachedNonEmpty() {
        List<ItemSummaryDTO> cachedList = List.of(ItemSummaryDTO.builder().id(1L).title("hot").build());
        when(cacheService.get(anyString())).thenReturn(cachedList);

        List<ItemSummaryDTO> result = itemQueryService.getHotItems();

        assertEquals(1, result.size());
        assertEquals("hot", result.get(0).getTitle());
        verify(itemRepository, never()).findByStatusAndCreatedAtBetween(any(), any(), any());
    }

    @Test
    void getHotItems_withCachedEmpty_refetches() {
        when(cacheService.get(anyString())).thenReturn(List.of());

        Item hotItem = new Item();
        hotItem.setId(1L);
        hotItem.setTitle("热门物品");
        hotItem.setUserId(1L);
        hotItem.setCategoryId(1L);
        hotItem.setCreatedAt(LocalDateTime.now());
        hotItem.setViewCount(100);
        hotItem.setFavoriteCount(10);
        hotItem.setCondition(Item.ItemCondition.NEW);
        when(itemRepository.findByStatusAndCreatedAtBetween(eq(Item.ItemStatus.ON_SALE), any(), any()))
                .thenReturn(List.of(hotItem));

        User stubUser = new User();
        stubUser.setId(1L);
        stubUser.setNickname("seller");
        stubUser.setUsername("seller");
        when(userRepository.findAllById(anySet())).thenReturn(List.of(stubUser));
        when(itemRepository.countByUserIds(anyList())).thenReturn(singleCount);
        when(dtoConverter.toSummaryDTOList(anyList(), anyMap(), anyMap())).thenReturn(List.of(
                ItemSummaryDTO.builder().id(1L).title("热门物品").build()
        ));

        List<ItemSummaryDTO> result = itemQueryService.getHotItems();

        assertEquals(1, result.size());
        verify(itemRepository).findByStatusAndCreatedAtBetween(eq(Item.ItemStatus.ON_SALE), any(), any());
    }

    @Test
    void getSellerItemCounts_batch_withCachedAndUncached() {
        when(cacheService.get(CacheService.getSellerItemCountKey(1L))).thenReturn(5);
        when(cacheService.get(CacheService.getSellerItemCountKey(2L))).thenReturn(null);

        List<Object[]> batchCount = new ArrayList<>();
        batchCount.add(new Object[]{2L, 3L});
        when(itemRepository.countByUserIds(List.of(2L))).thenReturn(batchCount);

        Map<Long, Integer> result = itemQueryService.getSellerItemCounts(List.of(1L, 2L));

        assertEquals(5, result.get(1L));
        assertEquals(3, result.get(2L));
        verify(itemRepository).countByUserIds(List.of(2L));
    }

    @Test
    void getSellerItemCounts_batch_fillsAbsentWithZero() {
        when(cacheService.get(anyString())).thenReturn(null);
        when(itemRepository.countByUserIds(List.of(1L))).thenReturn(emptyCount);

        Map<Long, Integer> result = itemQueryService.getSellerItemCounts(List.of(1L));

        assertEquals(0, result.get(1L));
    }

    @Test
    void createPageable_withPriceAsc() {
        Pageable result = itemQueryService.createPageable(1, 20, "priceAsc");

        assertEquals(Sort.by(Sort.Direction.ASC, "price"), result.getSort());
    }

    @Test
    void createPageable_withPriceDesc() {
        Pageable result = itemQueryService.createPageable(1, 20, "priceDesc");

        assertEquals(Sort.by(Sort.Direction.DESC, "price"), result.getSort());
    }

    @Test
    void createPageable_withViewCount() {
        Pageable result = itemQueryService.createPageable(1, 20, "viewCount");

        assertEquals(Sort.by(Sort.Direction.DESC, "viewCount"), result.getSort());
    }

    @Test
    void createPageable_withFavoriteCount() {
        Pageable result = itemQueryService.createPageable(1, 20, "favoriteCount");

        assertEquals(Sort.by(Sort.Direction.DESC, "favoriteCount"), result.getSort());
    }

    @Test
    void createPageable_withDefaultSort() {
        Pageable result = itemQueryService.createPageable(1, 20, "unknown");

        assertEquals(Sort.by(Sort.Direction.DESC, "createdAt"), result.getSort());
    }

    @Test
    void getUserItems_withPageSize() {
        Pageable pageable = PageRequest.of(2, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Item> expectedPage = new PageImpl<>(List.of(), pageable, 0);
        when(itemRepository.findByUserId(1L, pageable)).thenReturn(expectedPage);

        Page<Item> result = itemQueryService.getUserItems(1L, null, 3, 10);

        assertNotNull(result);
        assertEquals(pageable, result.getPageable());
    }
}
