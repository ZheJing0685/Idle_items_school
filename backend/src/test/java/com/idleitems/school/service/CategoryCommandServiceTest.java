package com.idleitems.school.service;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.event.CategoryChangedEvent;
import com.idleitems.school.module.category.entity.Category;
import com.idleitems.school.module.category.entity.CategoryChangeLog;
import com.idleitems.school.module.category.repository.CategoryChangeLogRepository;
import com.idleitems.school.module.category.repository.CategoryRepository;
import com.idleitems.school.module.category.service.CategoryCommandService;
import com.idleitems.school.module.item.repository.ItemRepository;
import com.idleitems.school.shared.cache.CacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryCommandServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CategoryChangeLogRepository categoryChangeLogRepository;

    @Mock
    private CacheService cacheService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CategoryCommandService categoryCommandService;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("测试分类");
        testCategory.setParentId(null);
    }

    @Test
    void createCategory_WhenValidRequest_CreatesCategory() {
        when(categoryRepository.findByParentIdIsNull()).thenReturn(Collections.emptyList());
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Category result = categoryCommandService.createCategory(testCategory, 1L);

        assertNotNull(result);
        assertEquals("测试分类", result.getName());
        assertEquals(0, result.getSort());
        assertTrue(result.getStatus());
        assertEquals(1, result.getLevel());
        verify(categoryRepository, times(1)).save(any(Category.class));
        verify(cacheService, times(1)).delete("categories:all");
        verify(cacheService, times(1)).delete("categories:tree");
        verify(eventPublisher, times(1)).publishEvent(any(CategoryChangedEvent.class));
    }

    @Test
    void createCategory_WhenNameEmpty_ThrowsException() {
        testCategory.setName("  ");

        assertThrows(BusinessException.class, () -> categoryCommandService.createCategory(testCategory, 1L));
    }

    @Test
    void createCategory_WhenParentNotFound_ThrowsException() {
        testCategory.setParentId(999L);
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () ->
                categoryCommandService.createCategory(testCategory, 1L));
        assertTrue(ex.getMessage().contains("Parent not found"));
    }

    @Test
    void createCategory_WhenParentMaxLevelExceeded_ThrowsException() {
        testCategory.setParentId(1L);
        Category parent = new Category();
        parent.setId(1L);
        parent.setLevel(3);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parent));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                categoryCommandService.createCategory(testCategory, 1L));
        assertTrue(ex.getMessage().contains("Max 3 levels"));
    }

    @Test
    void createCategory_WhenDuplicateName_ThrowsException() {
        testCategory.setParentId(1L);
        Category parent = new Category();
        parent.setId(1L);
        parent.setLevel(1);
        Category sibling = new Category();
        sibling.setName("测试分类");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(categoryRepository.findByParentId(1L)).thenReturn(List.of(sibling));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                categoryCommandService.createCategory(testCategory, 1L));
        assertTrue(ex.getMessage().contains("Name already exists"));
    }

    @Test
    void createCategory_WhenDefaultSortAndStatus_AppliesDefaults() {
        testCategory.setSort(null);
        testCategory.setStatus(null);
        when(categoryRepository.findByParentIdIsNull()).thenReturn(Collections.emptyList());
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Category result = categoryCommandService.createCategory(testCategory, 1L);

        assertEquals(0, result.getSort());
        assertTrue(result.getStatus());
    }

    @Test
    void updateCategory_WhenNormalUpdate_UpdatesAllFields() throws Exception {
        Category existing = new Category();
        existing.setId(1L);
        existing.setName("旧名称");
        existing.setDescription("旧描述");
        existing.setIcon("旧图标");
        existing.setSort(1);
        existing.setKeywords("旧关键词");
        existing.setParentId(null);
        existing.setLevel(1);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Category updateData = new Category();
        updateData.setName("新名称");
        updateData.setDescription("新描述");
        updateData.setIcon("新图标");
        updateData.setSort(2);
        updateData.setKeywords("新关键词");

        Category result = categoryCommandService.updateCategory(1L, updateData, 1L);

        assertEquals("新名称", result.getName());
        assertEquals("新描述", result.getDescription());
        assertEquals("新图标", result.getIcon());
        assertEquals(2, result.getSort());
        assertEquals("新关键词", result.getKeywords());
        verify(categoryRepository, times(1)).save(any(Category.class));
        verify(cacheService, times(1)).delete("categories:all");
        verify(cacheService, times(1)).delete("categories:tree");
        verify(eventPublisher, times(1)).publishEvent(any(CategoryChangedEvent.class));
    }

    @Test
    void updateCategory_WhenChangeParent_UpdatesParentAndLevel() throws Exception {
        Category existing = new Category();
        existing.setId(1L);
        existing.setName("测试分类");
        existing.setParentId(null);
        existing.setLevel(1);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(existing));
        Category newParent = new Category();
        newParent.setId(2L);
        newParent.setLevel(2);
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newParent));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Category updateData = new Category();
        updateData.setParentId(2L);
        updateData.setSort(null);

        Category result = categoryCommandService.updateCategory(1L, updateData, 1L);

        assertEquals(2L, result.getParentId());
        assertEquals(3, result.getLevel());
    }

    @Test
    void updateCategory_WhenSelfParent_ThrowsException() {
        Category existing = new Category();
        existing.setId(1L);
        existing.setName("测试分类");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));

        Category updateData = new Category();
        updateData.setParentId(1L);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                categoryCommandService.updateCategory(1L, updateData, 1L));
        assertTrue(ex.getMessage().contains("Cannot set self as parent"));
    }

    @Test
    void updateCategory_WhenParentMaxLevel_ThrowsException() {
        Category existing = new Category();
        existing.setId(1L);
        existing.setName("测试分类");
        existing.setParentId(null);
        existing.setLevel(1);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));

        Category newParent = new Category();
        newParent.setId(2L);
        newParent.setLevel(3);
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newParent));

        Category updateData = new Category();
        updateData.setParentId(2L);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                categoryCommandService.updateCategory(1L, updateData, 1L));
        assertTrue(ex.getMessage().contains("Max 3 levels"));
    }

    @Test
    void updateCategory_WhenDuplicateName_ThrowsException() {
        Category existing = new Category();
        existing.setId(1L);
        existing.setName("旧名称");
        existing.setParentId(null);
        existing.setLevel(1);
        Category sibling = new Category();
        sibling.setId(2L);
        sibling.setName("新名称");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findByParentIdIsNull()).thenReturn(List.of(existing, sibling));

        Category updateData = new Category();
        updateData.setName("新名称");

        BusinessException ex = assertThrows(BusinessException.class, () ->
                categoryCommandService.updateCategory(1L, updateData, 1L));
        assertTrue(ex.getMessage().contains("Name already exists"));
    }

    @Test
    void updateCategory_WhenNoChanges_SavesWithoutChangeLog() {
        Category existing = new Category();
        existing.setId(1L);
        existing.setName("测试分类");
        existing.setParentId(null);
        existing.setLevel(1);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Category updateData = new Category();
        updateData.setSort(null);

        Category result = categoryCommandService.updateCategory(1L, updateData, 1L);

        assertNotNull(result);
        verify(categoryRepository, times(1)).save(any(Category.class));
        verify(categoryChangeLogRepository, never()).save(any(CategoryChangeLog.class));
        verify(cacheService, times(1)).delete("categories:all");
        verify(cacheService, times(1)).delete("categories:tree");
        verify(eventPublisher, times(1)).publishEvent(any(CategoryChangedEvent.class));
    }

    @Test
    void deleteCategory_WhenValid_DeletesCategory() {
        Category category = new Category();
        category.setId(1L);
        category.setName("测试分类");
        category.setParentId(null);
        category.setLevel(1);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findByParentId(1L)).thenReturn(Collections.emptyList());
        when(itemRepository.countByCategoryId(1L)).thenReturn(0L);

        categoryCommandService.deleteCategory(1L, 1L);

        verify(categoryRepository, times(1)).deleteById(1L);
        verify(cacheService, times(1)).delete("categories:all");
        verify(cacheService, times(1)).delete("categories:tree");
        verify(eventPublisher, times(1)).publishEvent(any(CategoryChangedEvent.class));
    }

    @Test
    void deleteCategory_WhenNotFound_ThrowsException() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> categoryCommandService.deleteCategory(999L, 1L));
    }

    @Test
    void deleteCategory_WhenHasChildren_ThrowsException() {
        Category category = new Category();
        category.setId(1L);
        category.setName("测试分类");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findByParentId(1L)).thenReturn(List.of(new Category()));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                categoryCommandService.deleteCategory(1L, 1L));
        assertTrue(ex.getMessage().contains("Has subcategories"));
    }

    @Test
    void deleteCategory_WhenHasItems_ThrowsException() {
        Category category = new Category();
        category.setId(1L);
        category.setName("测试分类");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findByParentId(1L)).thenReturn(Collections.emptyList());
        when(itemRepository.countByCategoryId(1L)).thenReturn(5L);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                categoryCommandService.deleteCategory(1L, 1L));
        assertTrue(ex.getMessage().contains("Has items"));
    }

    @Test
    void batchDeleteCategories_WhenPartialSuccess_ThrowsWithErrors() {
        Category cat1 = new Category();
        cat1.setId(1L);
        cat1.setName("分类1");
        Category cat2 = new Category();
        cat2.setId(2L);
        cat2.setName("分类2");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat1));
        when(categoryRepository.findByParentId(1L)).thenReturn(Collections.emptyList());
        when(itemRepository.countByCategoryId(1L)).thenReturn(0L);
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(cat2));
        when(categoryRepository.findByParentId(2L)).thenReturn(List.of(new Category()));
        when(categoryRepository.findById(3L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () ->
                categoryCommandService.batchDeleteCategories(List.of(1L, 2L, 3L), 1L));
        assertTrue(ex.getMessage().contains("has subcategories"));
        assertTrue(ex.getMessage().contains("not found"));

        verify(categoryRepository, times(1)).deleteById(1L);
        verify(cacheService, never()).delete(anyString());
        verify(eventPublisher, never()).publishEvent(any(CategoryChangedEvent.class));
    }

    @Test
    void batchDeleteCategories_WhenAllSucceed_DeletesAll() {
        Category cat1 = new Category();
        cat1.setId(1L);
        cat1.setName("分类1");
        Category cat2 = new Category();
        cat2.setId(2L);
        cat2.setName("分类2");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat1));
        when(categoryRepository.findByParentId(1L)).thenReturn(Collections.emptyList());
        when(itemRepository.countByCategoryId(1L)).thenReturn(0L);
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(cat2));
        when(categoryRepository.findByParentId(2L)).thenReturn(Collections.emptyList());
        when(itemRepository.countByCategoryId(2L)).thenReturn(0L);

        categoryCommandService.batchDeleteCategories(List.of(1L, 2L), 1L);

        verify(categoryRepository, times(1)).deleteById(1L);
        verify(categoryRepository, times(1)).deleteById(2L);
        verify(cacheService, times(1)).delete("categories:all");
        verify(cacheService, times(1)).delete("categories:tree");
        verify(eventPublisher, times(1)).publishEvent(any(CategoryChangedEvent.class));
    }

    @Test
    void toggleCategoryStatus_WhenValid_TogglesStatus() throws Exception {
        Category category = new Category();
        category.setId(1L);
        category.setName("测试分类");
        category.setStatus(true);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Category result = categoryCommandService.toggleCategoryStatus(1L, false, 1L);

        assertFalse(result.getStatus());
        verify(categoryRepository, times(1)).save(any(Category.class));
        verify(cacheService, times(1)).delete("categories:all");
        verify(cacheService, times(1)).delete("categories:tree");
        verify(eventPublisher, times(1)).publishEvent(any(CategoryChangedEvent.class));
    }

    @Test
    void toggleCategoryStatus_WhenNotFound_ThrowsException() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () ->
                categoryCommandService.toggleCategoryStatus(999L, false, 1L));
    }

    @Test
    void importCategories_WhenNormalCsv_ImportsSuccessfully() throws Exception {
        String csv = "序号,名称,上级分类ID,层级,排序,状态,图标,描述,关键词\n" +
                     "1,分类A,,1,1,启用,icon-a,描述A,关键词A\n" +
                     "2,分类B,1,2,2,ACTIVE,icon-b,描述B,关键词B\n";
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));
        when(categoryRepository.save(any(Category.class))).thenReturn(new Category());

        Map<String, Object> result = categoryCommandService.importCategories(file, 1L);

        assertEquals(2, result.get("successCount"));
        assertEquals(0, result.get("failCount"));
        verify(categoryRepository, times(2)).save(any(Category.class));
        verify(cacheService, times(1)).delete("categories:all");
        verify(cacheService, times(1)).delete("categories:tree");
        verify(eventPublisher, times(1)).publishEvent(any(CategoryChangedEvent.class));
    }

    @Test
    void importCategories_WhenEmptyFile_ThrowsException() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                categoryCommandService.importCategories(file, 1L));
        assertTrue(ex.getMessage().contains("Empty file"));
    }

    @Test
    void importCategories_WhenFieldParsingErrors_RecordsFailures() throws Exception {
        String csv = "序号,名称,上级分类ID,层级,排序,状态\n" +
                     "1,有效分类,,1,1,启用\n" +
                     "2,无效引用,abc\n" +
                     "3\n";
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));
        when(categoryRepository.save(any(Category.class))).thenReturn(new Category());

        Map<String, Object> result = categoryCommandService.importCategories(file, 1L);

        assertEquals(1, result.get("successCount"));
        assertEquals(2, result.get("failCount"));
        @SuppressWarnings("unchecked")
        List<String> errors = (List<String>) result.get("errors");
        assertTrue(errors.size() >= 2);
    }

    @Test
    void importCategories_WhenParseLevelChinese_HandlesChineseLevel() throws Exception {
        String csv = "分类ID,分类名称,上级分类ID,层级,排序,状态,图标,描述,关键词\n1,分类一,无,一级分类\n2,分类二,无,二级分类\n";
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));
        when(categoryRepository.save(any(Category.class))).thenReturn(new Category());

        Map<String, Object> result = categoryCommandService.importCategories(file, 1L);

        assertEquals(2, result.get("successCount"));
    }

    @Test
    void normalizeSortValues_WhenUnsorted_ReordersSiblings() {
        Category c1 = new Category();
        c1.setId(1L);
        c1.setSort(5);
        Category c2 = new Category();
        c2.setId(2L);
        c2.setSort(1);
        Category c3 = new Category();
        c3.setId(3L);
        c3.setSort(2);
        when(categoryRepository.findByParentIdIsNull()).thenReturn(List.of(c1, c2, c3));

        categoryCommandService.normalizeSortValues(null);

        verify(categoryRepository, times(3)).save(any(Category.class));
        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository, times(3)).save(captor.capture());
        List<Integer> sorts = captor.getAllValues().stream()
                .map(Category::getSort)
                .sorted()
                .toList();
        assertEquals(List.of(0, 1, 2), sorts);
    }

    @Test
    void normalizeSortValues_WhenAlreadyOrdered_NoChanges() {
        Category c1 = new Category();
        c1.setId(1L);
        c1.setSort(0);
        Category c2 = new Category();
        c2.setId(2L);
        c2.setSort(1);
        when(categoryRepository.findByParentIdIsNull()).thenReturn(List.of(c1, c2));

        categoryCommandService.normalizeSortValues(null);

        verify(categoryRepository, never()).save(any(Category.class));
    }
}
