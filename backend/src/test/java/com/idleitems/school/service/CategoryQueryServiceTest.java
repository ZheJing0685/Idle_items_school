package com.idleitems.school.service;

import com.idleitems.school.module.category.entity.Category;
import com.idleitems.school.module.category.entity.CategoryChangeLog;
import com.idleitems.school.module.category.entity.CategoryFeedback;
import com.idleitems.school.module.category.repository.CategoryChangeLogRepository;
import com.idleitems.school.module.category.repository.CategoryFeedbackRepository;
import com.idleitems.school.module.category.repository.CategoryRepository;
import com.idleitems.school.module.category.service.CategoryQueryService;
import com.idleitems.school.module.item.repository.ItemRepository;
import com.idleitems.school.shared.cache.CacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryQueryServiceTest {

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

    @InjectMocks
    private CategoryQueryService categoryQueryService;

    private Category category1;
    private Category category2;
    private Category child1;

    @BeforeEach
    void setUp() {
        category1 = new Category();
        category1.setId(1L);
        category1.setName("电子产品");
        category1.setParentId(null);
        category1.setLevel(1);
        category1.setSort(1);
        category1.setStatus(true);
        category1.setIcon("icon-electronics");
        category1.setKeywords("电子,数码,科技");

        category2 = new Category();
        category2.setId(2L);
        category2.setName("家具家电");
        category2.setParentId(null);
        category2.setLevel(1);
        category2.setSort(2);
        category2.setStatus(true);
        category2.setIcon("icon-furniture");
        category2.setKeywords("家具,家电,生活");

        child1 = new Category();
        child1.setId(3L);
        child1.setName("手机");
        child1.setParentId(1L);
        child1.setLevel(2);
        child1.setSort(1);
        child1.setStatus(true);
        child1.setIcon("icon-phone");
        child1.setKeywords("手机,移动电话");
    }

    @Test
    void getAllCategories_WhenCached_ReturnsCached() {
        List<Map<String, Object>> cachedResult = new ArrayList<>();
        Map<String, Object> cachedMap = new HashMap<>();
        cachedMap.put("id", 1L);
        cachedMap.put("name", "缓存数据");
        cachedResult.add(cachedMap);
        @SuppressWarnings("unchecked")
        Object cachedObj = (Object) cachedResult;
        when(cacheService.get("categories:all")).thenReturn(cachedObj);

        List<Map<String, Object>> result = categoryQueryService.getAllCategories();

        assertEquals(1, result.size());
        assertEquals("缓存数据", result.get(0).get("name"));
        verify(categoryRepository, never()).findAll();
    }

    @Test
    void getAllCategories_WhenNotCached_BuildsFromDatabase() {
        when(cacheService.get("categories:all")).thenReturn(null);
        when(categoryRepository.findAll()).thenReturn(List.of(category1, category2, child1));
        when(itemRepository.countByCategoryIdsGrouped(anyList())).thenReturn(List.<Object[]>of(
                new Object[]{1L, 10L},
                new Object[]{2L, 5L},
                new Object[]{3L, 3L}
        ));

        List<Map<String, Object>> result = categoryQueryService.getAllCategories();

        assertEquals(3, result.size());
        verify(cacheService, times(1)).set(eq("categories:all"), anyList(), eq(3600L), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getCategoryTree_WhenCached_ReturnsCached() {
        List<Map<String, Object>> cachedTree = new ArrayList<>();
        Map<String, Object> node = new HashMap<>();
        node.put("id", 1L);
        node.put("name", "缓存树");
        cachedTree.add(node);
        when(cacheService.get("categories:tree")).thenReturn((Object) cachedTree);

        List<Map<String, Object>> result = categoryQueryService.getCategoryTree();

        assertEquals(1, result.size());
        assertEquals("缓存树", result.get(0).get("name"));
    }

    @Test
    void getCategoryTree_WhenNotCached_BuildsTree() {
        when(cacheService.get("categories:tree")).thenReturn(null);
        when(categoryRepository.findAll()).thenReturn(List.of(category1, category2, child1));
        when(itemRepository.countByCategoryIdsGrouped(anyList())).thenReturn(List.<Object[]>of(
                new Object[]{1L, 10L},
                new Object[]{2L, 5L},
                new Object[]{3L, 3L}
        ));

        List<Map<String, Object>> result = categoryQueryService.getCategoryTree();

        assertEquals(2, result.size());
        Map<String, Object> firstRoot = result.stream()
                .filter(m -> m.get("id").equals(1L))
                .findFirst().orElse(null);
        assertNotNull(firstRoot);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> children = (List<Map<String, Object>>) firstRoot.get("children");
        assertEquals(1, children.size());
        assertEquals("手机", children.get(0).get("name"));
        verify(cacheService, times(1)).set(eq("categories:tree"), anyList(), eq(3600L), any());
    }

    @Test
    void getChildren_WhenNormal_ReturnsChildren() {
        when(categoryRepository.findByParentId(1L)).thenReturn(List.of(child1));
        when(categoryRepository.findAll()).thenReturn(List.of(category1, category2, child1));
        when(itemRepository.countByCategoryIdsGrouped(anyList())).thenReturn(List.<Object[]>of(
                new Object[]{3L, 3L}
        ));

        List<Map<String, Object>> result = categoryQueryService.getChildren(1L);

        assertEquals(1, result.size());
        assertEquals("手机", result.get(0).get("name"));
    }

    @Test
    void getChildren_WhenEmpty_ReturnsEmptyList() {
        when(categoryRepository.findByParentId(999L)).thenReturn(Collections.emptyList());

        List<Map<String, Object>> result = categoryQueryService.getChildren(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getChildren_WhenMultiple_SortsBySortThenName() {
        Category childB = new Category();
        childB.setId(4L);
        childB.setName("B类");
        childB.setParentId(1L);
        childB.setSort(1);
        Category childA = new Category();
        childA.setId(5L);
        childA.setName("A类");
        childA.setParentId(1L);
        childA.setSort(1);
        Category childC = new Category();
        childC.setId(6L);
        childC.setName("C类");
        childC.setParentId(1L);
        childC.setSort(2);

        when(categoryRepository.findByParentId(1L)).thenReturn(List.of(childB, childA, childC));
        when(categoryRepository.findAll()).thenReturn(List.of(childA, childB, childC));
        when(itemRepository.countByCategoryIdsGrouped(anyList())).thenReturn(List.<Object[]>of(
                new Object[]{4L, 0L},
                new Object[]{5L, 0L},
                new Object[]{6L, 0L}
        ));

        List<Map<String, Object>> result = categoryQueryService.getChildren(1L);

        assertEquals(3, result.size());
        assertEquals("A类", result.get(0).get("name"));
        assertEquals("B类", result.get(1).get("name"));
        assertEquals("C类", result.get(2).get("name"));
    }

    @Test
    void suggestCategories_FiltersByPrefix() {
        Category cat = new Category();
        cat.setId(1L);
        cat.setName("电子产品");
        cat.setParentId(null);
        cat.setLevel(1);
        when(categoryRepository.searchByKeyword("电子")).thenReturn(List.of(cat));

        List<Map<String, Object>> result = categoryQueryService.suggestCategories("电子");

        assertEquals(1, result.size());
        assertEquals("电子产品", result.get(0).get("name"));
    }

    @Test
    void suggestCategories_LimitsToFive() {
        List<Category> manyResults = new ArrayList<>();
        for (long i = 1; i <= 7; i++) {
            Category c = new Category();
            c.setId(i);
            c.setName("分类" + i);
            c.setParentId(null);
            c.setLevel(1);
            manyResults.add(c);
        }
        when(categoryRepository.searchByKeyword("分类")).thenReturn(manyResults);

        List<Map<String, Object>> result = categoryQueryService.suggestCategories("分类");

        assertEquals(5, result.size());
    }

    @Test
    void recommendCategories_WhenMatches_ReturnsScoredResults() {
        when(categoryRepository.findByStatus(true)).thenReturn(List.of(category1, category2, child1));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));

        List<Map<String, Object>> result = categoryQueryService.recommendCategories("生活 手机", 10);

        assertFalse(result.isEmpty());
        Map<String, Object> first = result.get(0);
        assertTrue((Double) first.get("score") > 0);
    }

    @Test
    void recommendCategories_WhenNoMatches_ReturnsEmpty() {
        when(categoryRepository.findByStatus(true)).thenReturn(List.of(category1, category2, child1));

        List<Map<String, Object>> result = categoryQueryService.recommendCategories("xyzabc", 5);

        assertTrue(result.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getHotCategories_WhenCached_ReturnsCached() {
        List<Map<String, Object>> cachedResult = new ArrayList<>();
        Map<String, Object> cached = new HashMap<>();
        cached.put("id", 1L);
        cached.put("itemCount", 100L);
        cachedResult.add(cached);
        when(cacheService.get("categories:hot")).thenReturn((Object) cachedResult);

        List<Map<String, Object>> result = categoryQueryService.getHotCategories(10);

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).get("itemCount"));
    }

    @Test
    void getHotCategories_WhenNotCached_BuildsFromDatabase() {
        when(cacheService.get("categories:hot")).thenReturn(null);
        when(categoryRepository.findByStatus(true)).thenReturn(List.of(category1, category2, child1));
        when(itemRepository.countItemsByCategory()).thenReturn(List.<Object[]>of(
                new Object[]{1L, 100L},
                new Object[]{2L, 50L},
                new Object[]{3L, 30L}
        ));

        List<Map<String, Object>> result = categoryQueryService.getHotCategories(10);

        assertEquals(3, result.size());
        assertTrue((Long) result.get(0).get("itemCount") >= (Long) result.get(1).get("itemCount"));
        verify(cacheService, times(1)).set(eq("categories:hot"), anyList(), eq(300L), any());
    }

    @Test
    void getBreadcrumb_WhenChain_BuildsCorrectOrder() {
        Category l3 = new Category();
        l3.setId(3L);
        l3.setName("三级");
        l3.setParentId(2L);
        l3.setLevel(3);
        Category l2 = new Category();
        l2.setId(2L);
        l2.setName("二级");
        l2.setParentId(1L);
        l2.setLevel(2);
        Category l1 = new Category();
        l1.setId(1L);
        l1.setName("一级");
        l1.setParentId(null);
        l1.setLevel(1);

        when(categoryRepository.findById(3L)).thenReturn(Optional.of(l3));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(l2));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(l1));

        List<Map<String, Object>> result = categoryQueryService.getBreadcrumb(3L);

        assertEquals(3, result.size());
        assertEquals("一级", result.get(0).get("name"));
        assertEquals("二级", result.get(1).get("name"));
        assertEquals("三级", result.get(2).get("name"));
    }

    @Test
    void getBreadcrumb_WhenRoot_ReturnsSingle() {
        Category root = new Category();
        root.setId(1L);
        root.setName("根分类");
        root.setParentId(null);
        root.setLevel(1);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(root));

        List<Map<String, Object>> result = categoryQueryService.getBreadcrumb(1L);

        assertEquals(1, result.size());
        assertEquals("根分类", result.get(0).get("name"));
    }

    @Test
    void searchCategories_WhenNameMatches_ReturnsResults() {
        when(categoryRepository.findAll()).thenReturn(List.of(category1, category2, child1));

        List<Map<String, Object>> result = categoryQueryService.searchCategories("电子");

        assertEquals(1, result.size());
        assertEquals("电子产品", result.get(0).get("name"));
    }

    @Test
    void searchCategories_WhenKeywordsMatches_ReturnsResults() {
        when(categoryRepository.findAll()).thenReturn(List.of(category1, category2, child1));

        List<Map<String, Object>> result = categoryQueryService.searchCategories("移动电话");

        assertEquals(1, result.size());
        assertEquals("手机", result.get(0).get("name"));
    }

    @Test
    void searchCategories_WhenNoMatch_ReturnsEmpty() {
        when(categoryRepository.findAll()).thenReturn(List.of(category1, category2, child1));

        List<Map<String, Object>> result = categoryQueryService.searchCategories("不存在的关键词");

        assertTrue(result.isEmpty());
    }

    @Test
    void getCategoryStats_ReturnsCounts() {
        Category l1 = new Category();
        l1.setId(1L);
        l1.setName("一级");
        l1.setLevel(1);
        l1.setStatus(true);
        Category l2 = new Category();
        l2.setId(2L);
        l2.setName("二级");
        l2.setLevel(2);
        l2.setParentId(1L);
        l2.setStatus(true);

        when(categoryRepository.count()).thenReturn(2L);
        when(categoryRepository.findAll()).thenReturn(List.of(l1, l2));
        when(itemRepository.countByCategoryIds(anyList())).thenReturn(15L);
        when(categoryFeedbackRepository.countByStatus(CategoryFeedback.FeedbackStatus.PENDING)).thenReturn(3L);

        Map<String, Object> stats = categoryQueryService.getCategoryStats();

        assertEquals(2L, stats.get("total"));
        assertEquals(2L, stats.get("active"));
        assertEquals(1L, stats.get("level1"));
        assertEquals(1L, stats.get("level2"));
        assertEquals(3L, stats.get("pendingFeedbacks"));
        assertNotNull(stats.get("categoryItemCounts"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getCategoryChangeLogs_WithCategoryId_FiltersByCategory() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CategoryChangeLog> mockPage = mock(Page.class);
        when(categoryChangeLogRepository.findByCategoryIdOrderByCreatedAtDesc(1L, pageable)).thenReturn(mockPage);

        Page<CategoryChangeLog> result = categoryQueryService.getCategoryChangeLogs(1L, pageable);

        assertSame(mockPage, result);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getCategoryChangeLogs_WithoutCategoryId_ReturnsAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CategoryChangeLog> mockPage = mock(Page.class);
        when(categoryChangeLogRepository.findAllByOrderByCreatedAtDesc(pageable)).thenReturn(mockPage);

        Page<CategoryChangeLog> result = categoryQueryService.getCategoryChangeLogs(null, pageable);

        assertSame(mockPage, result);
    }

    @Test
    void exportCategories_ReturnsCsvFormat() {
        when(categoryRepository.findAll()).thenReturn(List.of(category1));

        String csv = categoryQueryService.exportCategories();

        assertTrue(csv.contains("分类ID,分类名称,上级分类ID,层级,排序,状态,图标,描述,关键词"));
        assertTrue(csv.contains("电子产品"));
        assertTrue(csv.contains("icon-electronics"));
    }

    @Test
    void exportCategories_WhenSpecialChars_ProperlyEscapes() {
        Category c = new Category();
        c.setId(1L);
        c.setName("带,逗号");
        c.setDescription("带\"引号\"");
        c.setKeywords("换行\n文本");
        c.setLevel(1);
        c.setSort(0);
        c.setStatus(true);
        when(categoryRepository.findAll()).thenReturn(List.of(c));

        String csv = categoryQueryService.exportCategories();

        assertTrue(csv.contains("\"带,逗号\""));
        assertTrue(csv.contains("\"带\"\"引号\"\"\""));
        assertTrue(csv.contains("\"换行\n文本\""));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAllFeedbacks_WithStatus_FiltersByStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CategoryFeedback> mockPage = mock(Page.class);
        when(categoryFeedbackRepository.findByStatus(CategoryFeedback.FeedbackStatus.PENDING, pageable)).thenReturn(mockPage);

        Page<CategoryFeedback> result = categoryQueryService.getAllFeedbacks("PENDING", pageable);

        assertSame(mockPage, result);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAllFeedbacks_WithoutStatus_ReturnsAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CategoryFeedback> mockPage = mock(Page.class);
        when(categoryFeedbackRepository.findAllByOrderByCreatedAtDesc(pageable)).thenReturn(mockPage);

        Page<CategoryFeedback> result = categoryQueryService.getAllFeedbacks(null, pageable);

        assertSame(mockPage, result);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getMyFeedbacks_DelegatesToRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CategoryFeedback> mockPage = mock(Page.class);
        when(categoryFeedbackRepository.findByUserIdOrderByCreatedAtDesc(1L, pageable)).thenReturn(mockPage);

        Page<CategoryFeedback> result = categoryQueryService.getMyFeedbacks(1L, pageable);

        assertSame(mockPage, result);
    }
}
