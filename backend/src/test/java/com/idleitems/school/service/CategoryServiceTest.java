package com.idleitems.school.service;

import com.idleitems.school.cache.CacheService;
import com.idleitems.school.common.BusinessException;
import com.idleitems.school.entity.Category;
import com.idleitems.school.entity.CategoryChangeLog;
import com.idleitems.school.entity.CategoryFeedback;
import com.idleitems.school.repository.CategoryChangeLogRepository;
import com.idleitems.school.repository.CategoryFeedbackRepository;
import com.idleitems.school.repository.CategoryRepository;
import com.idleitems.school.repository.ItemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CategoryFeedbackRepository categoryFeedbackRepository;

    @Mock
    private CategoryChangeLogRepository categoryChangeLogRepository;

    @Mock
    private CacheService cacheService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CategoryService categoryService;

    @Captor
    private ArgumentCaptor<Category> categoryCaptor;

    private Category rootCategory;
    private Category childCategory1;
    private Category childCategory2;

    @BeforeEach
    void setUp() {
        rootCategory = new Category();
        rootCategory.setId(1L);
        rootCategory.setName("电子产品");
        rootCategory.setDescription("电子类商品");
        rootCategory.setParentId(null);
        rootCategory.setLevel(1);
        rootCategory.setSort(1);
        rootCategory.setStatus(true);
        rootCategory.setIcon("electronics-icon");

        childCategory1 = new Category();
        childCategory1.setId(2L);
        childCategory1.setName("手机");
        childCategory1.setDescription("手机及相关配件");
        childCategory1.setParentId(1L);
        childCategory1.setLevel(2);
        childCategory1.setSort(1);
        childCategory1.setStatus(true);
        childCategory1.setIcon("phone-icon");

        childCategory2 = new Category();
        childCategory2.setId(3L);
        childCategory2.setName("电脑");
        childCategory2.setDescription("电脑及笔记本");
        childCategory2.setParentId(1L);
        childCategory2.setLevel(2);
        childCategory2.setSort(2);
        childCategory2.setStatus(true);
        childCategory2.setIcon("computer-icon");
    }

    @Test
    void getAllCategories_FromCache_ReturnsCachedData() {
        List<Map<String, Object>> cachedData = List.of(Map.of("id", 1, "name", "电子产品"));
        when(cacheService.get("categories:all")).thenReturn(cachedData);

        List<Map<String, Object>> result = categoryService.getAllCategories();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("电子产品", result.get(0).get("name"));
        verify(cacheService, times(1)).get("categories:all");
        verify(categoryRepository, never()).findAll();
    }

    @SuppressWarnings("unchecked")
    @Test
    void getAllCategories_NoCache_QueriesDatabase() {
        when(cacheService.get("categories:all")).thenReturn(null);
        when(categoryRepository.findAll()).thenReturn(List.of(rootCategory, childCategory1));
        when(itemRepository.countByCategoryIdsGrouped(anyList())).thenReturn(List.of(
                new Object[]{1L, 3L}, new Object[]{2L, 2L}
        ));

        List<Map<String, Object>> result = categoryService.getAllCategories();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("电子产品", result.get(0).get("name"));
        assertEquals("手机", result.get(1).get("name"));
        verify(cacheService, times(1)).get("categories:all");
        verify(categoryRepository, times(1)).findAll();
        verify(cacheService, times(1)).set(eq("categories:all"), anyList(), eq(1800L), eq(TimeUnit.SECONDS));
    }

    @Test
    void getCategoryTree_FromCache_ReturnsCachedTree() {
        List<Map<String, Object>> cachedTree = List.of(Map.of("id", 1, "name", "电子产品", "children", List.of()));
        when(cacheService.get("categories:tree")).thenReturn(cachedTree);

        List<Map<String, Object>> result = categoryService.getCategoryTree();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("电子产品", result.get(0).get("name"));
        verify(cacheService, times(1)).get("categories:tree");
        verify(categoryRepository, never()).findAll();
    }

    @SuppressWarnings("unchecked")
    @Test
    void getCategoryTree_BuildsTreeCorrectly() {
        Category parent = new Category();
        parent.setId(1L);
        parent.setName("电子产品");
        parent.setParentId(null);
        parent.setLevel(1);
        parent.setIcon("el-icon");

        Category child = new Category();
        child.setId(2L);
        child.setName("手机");
        child.setParentId(1L);
        child.setLevel(2);

        when(cacheService.get("categories:tree")).thenReturn(null);
        when(categoryRepository.findAll()).thenReturn(List.of(parent, child));
        when(itemRepository.countByCategoryIdsGrouped(anyList())).thenReturn(List.of(
                new Object[]{1L, 2L}, new Object[]{2L, 1L}
        ));

        List<Map<String, Object>> result = categoryService.getCategoryTree();

        assertNotNull(result);
        assertEquals(1, result.size());
        Map<String, Object> root = result.get(0);
        assertEquals("电子产品", root.get("name"));
        assertNotNull(root.get("children"));
        List<Map<String, Object>> children = (List<Map<String, Object>>) root.get("children");
        assertEquals(1, children.size());
        assertEquals("手机", children.get(0).get("name"));
        verify(cacheService, times(1)).set(eq("categories:tree"), anyList(), eq(1800L), eq(TimeUnit.SECONDS));
    }

    @Test
    void searchCategories_ByKeyword_ReturnsMatching() {
        Category cat1 = new Category();
        cat1.setId(1L);
        cat1.setName("电子产品");
        cat1.setIcon("el-icon");
        cat1.setKeywords("数码,电子");
        cat1.setLevel(1);

        Category cat2 = new Category();
        cat2.setId(2L);
        cat2.setName("服装");
        cat2.setIcon("clothes-icon");
        cat2.setKeywords("衣服,时尚");
        cat2.setLevel(1);

        when(categoryRepository.findAll()).thenReturn(List.of(cat1, cat2));

        List<Map<String, Object>> result = categoryService.searchCategories("电子");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("电子产品", result.get(0).get("name"));
    }

    @Test
    void searchCategories_ByKeywordInKeywords_ReturnsMatching() {
        Category cat = new Category();
        cat.setId(1L);
        cat.setName("服饰专区");
        cat.setIcon("icon");
        cat.setKeywords("时尚,潮流,服饰");
        cat.setLevel(1);

        when(categoryRepository.findAll()).thenReturn(List.of(cat));

        List<Map<String, Object>> result = categoryService.searchCategories("时尚");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("服饰专区", result.get(0).get("name"));
    }

    @Test
    void createCategory_WithEmptyName_ThrowsException() {
        Category category = new Category();
        category.setName("");
        category.setParentId(null);

        assertThrows(BusinessException.class, () ->
                categoryService.createCategory(category, 1L));
    }

    @Test
    void createCategory_WithNullName_ThrowsException() {
        Category category = new Category();
        category.setName(null);

        assertThrows(BusinessException.class, () ->
                categoryService.createCategory(category, 1L));
    }

    @Test
    void createCategory_WithValidData_SavesAndClearsCache() {
        Category input = new Category();
        input.setName("新分类");
        input.setDescription("新分类描述");
        input.setParentId(null);
        input.setSort(null);
        input.setStatus(null);

        when(categoryRepository.findByParentIdIsNull()).thenReturn(List.of());
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        Category result = categoryService.createCategory(input, 1L);

        assertNotNull(result);
        assertEquals("新分类", result.getName());
        assertEquals(Integer.valueOf(1), result.getLevel());
        assertEquals(Integer.valueOf(0), result.getSort());
        assertTrue(result.getStatus());
        verify(categoryRepository, times(1)).save(any(Category.class));
        verify(categoryChangeLogRepository, times(1)).save(any(CategoryChangeLog.class));
        verify(cacheService, times(1)).delete("categories:all");
        verify(cacheService, times(1)).delete("categories:tree");
    }

    @Test
    void createCategory_WithDuplicateName_ThrowsException() {
        Category input = new Category();
        input.setName("电子产品");
        input.setParentId(null);

        Category existing = new Category();
        existing.setId(1L);
        existing.setName("电子产品");

        when(categoryRepository.findByParentIdIsNull()).thenReturn(List.of(existing));

        assertThrows(BusinessException.class, () ->
                categoryService.createCategory(input, 1L));
    }

    @Test
    void createCategory_WithParent_UpdatesLevel() {
        Category parent = new Category();
        parent.setId(5L);
        parent.setName("父分类");
        parent.setLevel(2);

        Category input = new Category();
        input.setName("子分类");
        input.setParentId(5L);

        when(categoryRepository.findById(5L)).thenReturn(Optional.of(parent));
        when(categoryRepository.findByParentId(5L)).thenReturn(List.of());
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category saved = invocation.getArgument(0);
            saved.setId(20L);
            return saved;
        });

        Category result = categoryService.createCategory(input, 1L);

        assertEquals(Integer.valueOf(3), result.getLevel());
    }

    @Test
    void deleteCategory_WithChildren_ThrowsException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(rootCategory));
        when(categoryRepository.findByParentId(1L)).thenReturn(List.of(childCategory1));

        assertThrows(BusinessException.class, () ->
                categoryService.deleteCategory(1L, 1L));

        verify(categoryRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteCategory_WithItems_ThrowsException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(rootCategory));
        when(categoryRepository.findByParentId(1L)).thenReturn(List.of());
        when(itemRepository.countByCategoryId(1L)).thenReturn(5L);

        assertThrows(BusinessException.class, () ->
                categoryService.deleteCategory(1L, 1L));

        verify(categoryRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteCategory_WithoutChildrenOrItems_DeletesSuccessfully() {
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(childCategory2));
        when(categoryRepository.findByParentId(3L)).thenReturn(List.of());
        when(itemRepository.countByCategoryId(3L)).thenReturn(0L);
        doNothing().when(categoryRepository).deleteById(3L);

        categoryService.deleteCategory(3L, 1L);

        verify(categoryRepository, times(1)).deleteById(3L);
        verify(categoryChangeLogRepository, times(1)).save(any(CategoryChangeLog.class));
        verify(cacheService, times(1)).delete("categories:all");
        verify(cacheService, times(1)).delete("categories:tree");
    }

    @Test
    void getCategoryStats_ReturnsStats() {
        Category inactiveCat = new Category();
        inactiveCat.setId(4L);
        inactiveCat.setName("停用分类");
        inactiveCat.setParentId(null);
        inactiveCat.setLevel(1);
        inactiveCat.setStatus(false);
        inactiveCat.setSort(2);

        Category level2Cat = new Category();
        level2Cat.setId(5L);
        level2Cat.setName("二级分类");
        level2Cat.setParentId(1L);
        level2Cat.setLevel(2);
        level2Cat.setStatus(true);

        when(categoryRepository.count()).thenReturn(4L);
        when(categoryRepository.findAll()).thenReturn(List.of(rootCategory, childCategory1, inactiveCat, level2Cat));
        when(itemRepository.countByCategoryIds(anyList())).thenReturn(8L);
        when(categoryFeedbackRepository.countByStatus(CategoryFeedback.FeedbackStatus.PENDING)).thenReturn(2L);

        Map<String, Object> stats = categoryService.getCategoryStats();

        assertNotNull(stats);
        assertEquals(4L, stats.get("total"));
        assertEquals(3L, stats.get("active"));
        assertEquals(2L, stats.get("level1"));
        assertEquals(2L, stats.get("level2"));
        assertEquals(2L, stats.get("pendingFeedbacks"));
        assertNotNull(stats.get("categoryItemCounts"));
    }

    @Test
    void getCategoryStats_categoryItemCounts_ContainsLevel1Categories() {
        when(categoryRepository.count()).thenReturn(1L);
        when(categoryRepository.findAll()).thenReturn(List.of(rootCategory));
        when(itemRepository.countByCategoryIds(anyList())).thenReturn(8L);
        when(categoryFeedbackRepository.countByStatus(CategoryFeedback.FeedbackStatus.PENDING)).thenReturn(0L);

        Map<String, Object> stats = categoryService.getCategoryStats();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> counts = (List<Map<String, Object>>) stats.get("categoryItemCounts");
        assertEquals(1, counts.size());
        assertEquals("电子产品", counts.get(0).get("name"));
        assertEquals(8L, counts.get(0).get("itemCount"));
    }
}
