package com.idleitems.school.service;

import com.idleitems.school.module.category.entity.Category;
import com.idleitems.school.module.category.repository.CategoryRepository;
import com.idleitems.school.module.item.entity.Item;
import com.idleitems.school.module.item.repository.ItemRepository;
import com.idleitems.school.module.item.service.impl.DefaultItemSearchProvider;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultItemSearchProviderTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private DefaultItemSearchProvider searchProvider;

    private Item item1;
    private Item item2;
    private Item item3;
    private Category parentCategory;
    private Category childCategory1;
    private Category childCategory2;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);

        item1 = new Item();
        item1.setId(1L);
        item1.setTitle("苹果手机 iPhone 14");
        item1.setDescription("全新苹果手机");
        item1.setCategoryId(101L);
        item1.setStatus(Item.ItemStatus.ON_SALE);
        item1.setCondition(Item.ItemCondition.NEW);
        item1.setDeliveryMethod("快递");
        item1.setPrice(BigDecimal.valueOf(5000));

        item2 = new Item();
        item2.setId(2L);
        item2.setTitle("苹果电脑 MacBook Pro");
        item2.setDescription("二手苹果电脑");
        item2.setCategoryId(101L);
        item2.setStatus(Item.ItemStatus.ON_SALE);
        item2.setCondition(Item.ItemCondition.GOOD);
        item2.setDeliveryMethod("自取");
        item2.setPrice(BigDecimal.valueOf(8000));

        item3 = new Item();
        item3.setId(3L);
        item3.setTitle("安卓手机");
        item3.setDescription("华为手机");
        item3.setCategoryId(102L);
        item3.setStatus(Item.ItemStatus.ON_SALE);
        item3.setCondition(Item.ItemCondition.LIKE_NEW);
        item3.setDeliveryMethod("快递");
        item3.setPrice(BigDecimal.valueOf(3000));

        parentCategory = new Category();
        parentCategory.setId(100L);
        parentCategory.setName("电子产品");
        parentCategory.setParentId(null);
        parentCategory.setStatus(true);

        childCategory1 = new Category();
        childCategory1.setId(101L);
        childCategory1.setName("手机");
        childCategory1.setParentId(100L);
        childCategory1.setStatus(true);

        childCategory2 = new Category();
        childCategory2.setId(102L);
        childCategory2.setName("电脑");
        childCategory2.setParentId(100L);
        childCategory2.setStatus(true);
    }

    @Test
    void searchItemIds_withKeyword_returnsResults() {
        Page<Item> itemPage = new PageImpl<>(List.of(item1, item2));
        when(itemRepository.searchByKeyword("苹果", Item.ItemStatus.ON_SALE, pageable)).thenReturn(itemPage);

        Page<Long> result = searchProvider.searchItemIds("苹果", null, null, pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().contains(1L));
        assertTrue(result.getContent().contains(2L));
        verify(itemRepository, times(1)).searchByKeyword("苹果", Item.ItemStatus.ON_SALE, pageable);
    }

    @Test
    void searchItemIds_withoutKeyword_returnsResults() {
        Page<Item> itemPage = new PageImpl<>(List.of(item1, item2, item3));
        when(itemRepository.findByFilters(Item.ItemStatus.ON_SALE, null, null, null, null, pageable)).thenReturn(itemPage);

        Page<Long> result = searchProvider.searchItemIds(null, null, null, pageable);

        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        verify(itemRepository, times(1)).findByFilters(Item.ItemStatus.ON_SALE, null, null, null, null, pageable);
    }

    @Test
    void searchItemIds_withKeywordAndConditionFilter_returnsFilteredResults() {
        Page<Item> itemPage = new PageImpl<>(List.of(item1, item2, item3));
        when(itemRepository.searchByKeyword("苹果", Item.ItemStatus.ON_SALE, pageable)).thenReturn(itemPage);

        Map<String, Object> filters = Map.of("condition", "NEW");
        Page<Long> result = searchProvider.searchItemIds("苹果", null, filters, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(Long.valueOf(1L), result.getContent().get(0));
        verify(itemRepository, times(1)).searchByKeyword("苹果", Item.ItemStatus.ON_SALE, pageable);
    }

    @Test
    void searchItemIds_withKeywordAndDeliveryMethodFilter_returnsFilteredResults() {
        Page<Item> itemPage = new PageImpl<>(List.of(item1, item2, item3));
        when(itemRepository.searchByKeyword("苹果", Item.ItemStatus.ON_SALE, pageable)).thenReturn(itemPage);

        Map<String, Object> filters = Map.of("deliveryMethod", "自取");
        Page<Long> result = searchProvider.searchItemIds("苹果", null, filters, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(Long.valueOf(2L), result.getContent().get(0));
        verify(itemRepository, times(1)).searchByKeyword("苹果", Item.ItemStatus.ON_SALE, pageable);
    }

    @Test
    void searchItemIds_withParentCategory_returnsResultsWithSubCategories() {
        when(categoryRepository.findById(100L)).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.findByParentId(100L)).thenReturn(List.of(childCategory1, childCategory2));

        Page<Item> itemPage = new PageImpl<>(List.of(item1, item2, item3));
        when(itemRepository.findByCategoryIdsAndFilters(
                Item.ItemStatus.ON_SALE, List.of(100L, 101L, 102L), null, null, null, pageable))
                .thenReturn(itemPage);

        Page<Long> result = searchProvider.searchItemIds(null, 100L, null, pageable);

        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        verify(categoryRepository, times(1)).findById(100L);
        verify(categoryRepository, times(1)).findByParentId(100L);
        verify(itemRepository, times(1)).findByCategoryIdsAndFilters(
                Item.ItemStatus.ON_SALE, List.of(100L, 101L, 102L), null, null, null, pageable);
    }

    @Test
    void searchItemIds_withLeafCategory_returnsResults() {
        Category leafCategory = new Category();
        leafCategory.setId(101L);
        leafCategory.setName("手机");
        leafCategory.setParentId(100L);
        leafCategory.setStatus(true);

        when(categoryRepository.findById(101L)).thenReturn(Optional.of(leafCategory));

        Page<Item> itemPage = new PageImpl<>(List.of(item1, item2));
        when(itemRepository.findByFilters(Item.ItemStatus.ON_SALE, 101L, null, null, null, pageable))
                .thenReturn(itemPage);

        Page<Long> result = searchProvider.searchItemIds(null, 101L, null, pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(categoryRepository, times(1)).findById(101L);
        verify(itemRepository, times(1)).findByFilters(Item.ItemStatus.ON_SALE, 101L, null, null, null, pageable);
    }

    @Test
    void searchItemIds_withoutKeywordAndFilters_returnsAllItems() {
        Page<Item> itemPage = new PageImpl<>(List.of(item1, item2, item3));
        when(itemRepository.findByFilters(Item.ItemStatus.ON_SALE, null, null, null, null, pageable)).thenReturn(itemPage);

        Page<Long> result = searchProvider.searchItemIds(null, null, null, pageable);

        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        verify(itemRepository, times(1)).findByFilters(Item.ItemStatus.ON_SALE, null, null, null, null, pageable);
    }

    @Test
    void searchItemIds_withKeywordAndCategoryAndFilters_returnsFilteredIds() {
        Page<Item> itemPage = new PageImpl<>(List.of(item1, item2, item3));
        when(itemRepository.searchByKeyword("苹果", Item.ItemStatus.ON_SALE, pageable)).thenReturn(itemPage);

        Category leafCategory = new Category();
        leafCategory.setId(101L);
        leafCategory.setName("手机");
        leafCategory.setParentId(100L);
        when(categoryRepository.findById(101L)).thenReturn(Optional.of(leafCategory));

        Map<String, Object> filters = Map.of("condition", "NEW", "deliveryMethod", "快递");
        Page<Long> result = searchProvider.searchItemIds("苹果", 101L, filters, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(Long.valueOf(1L), result.getContent().get(0));
        verify(itemRepository, times(1)).searchByKeyword("苹果", Item.ItemStatus.ON_SALE, pageable);
        verify(categoryRepository, times(1)).findById(101L);
    }

    @Test
    void searchItemIds_withInvalidFilterValue_ignoresInvalidCondition() {
        Page<Item> itemPage = new PageImpl<>(List.of(item1, item2, item3));
        when(itemRepository.searchByKeyword("苹果", Item.ItemStatus.ON_SALE, pageable)).thenReturn(itemPage);

        Map<String, Object> filters = Map.of("condition", "INVALID_CONDITION");
        Page<Long> result = searchProvider.searchItemIds("苹果", null, filters, pageable);

        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
    }

    @Test
    void indexItem_doesNothing() {
        searchProvider.indexItem(item1);

        verifyNoInteractions(itemRepository, categoryRepository);
    }

    @Test
    void removeFromIndex_doesNothing() {
        searchProvider.removeFromIndex(1L);

        verifyNoInteractions(itemRepository, categoryRepository);
    }
}
