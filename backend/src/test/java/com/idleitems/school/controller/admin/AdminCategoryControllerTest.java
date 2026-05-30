package com.idleitems.school.controller.admin;

import com.idleitems.school.aspect.PermissionAspect;
import com.idleitems.school.dto.CategoryDTO;
import com.idleitems.school.entity.Category;
import com.idleitems.school.entity.User;
import com.idleitems.school.repository.CategoryRepository;
import com.idleitems.school.repository.ItemRepository;
import com.idleitems.school.repository.UserRepository;
import com.idleitems.school.service.AdminLogService;
import com.idleitems.school.service.CategoryCommandService;
import com.idleitems.school.service.CategoryFeedbackService;
import com.idleitems.school.service.CategoryQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminCategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@EnableAspectJAutoProxy
@Import(PermissionAspect.class)
@DisplayName("AdminCategoryController 分类管理接口测试")
class AdminCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryRepository categoryRepository;

    @MockitoBean
    private ItemRepository itemRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private CategoryCommandService categoryCommandService;

    @MockitoBean
    private CategoryFeedbackService categoryFeedbackService;

    @MockitoBean
    private CategoryQueryService categoryQueryService;

    @MockitoBean
    private AdminLogService adminLogService;

    @BeforeEach
    void setUp() {
        User adminUser = new User();
        adminUser.setId(99L);
        adminUser.setRole(User.Role.ADMIN);
        when(userRepository.findById(99L)).thenReturn(Optional.of(adminUser));
    }

    @Test
    @DisplayName("测试获取分类列表")
    @SuppressWarnings("unchecked")
    void testGetCategories() throws Exception {
        Category category = buildCategory();
        when(categoryRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(category), PageRequest.of(0, 10), 1));
        when(categoryRepository.findByParentId(any(Long.class))).thenReturn(List.of());
        when(itemRepository.countByCategoryIds(any(List.class))).thenReturn(5L);

        mockMvc.perform(get("/api/admin/categories")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试获取分类统计")
    void testGetCategoryStats() throws Exception {
        when(categoryQueryService.getCategoryStats()).thenReturn(Map.of(
                "total", 20L,
                "active", 18L
        ));

        mockMvc.perform(get("/api/admin/categories/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(20));
    }

    @Test
    @DisplayName("测试创建分类")
    void testCreateCategory() throws Exception {
        Category category = buildCategory();
        when(categoryCommandService.createCategory(any(Category.class), any(Long.class))).thenReturn(category);

        mockMvc.perform(post("/api/admin/categories")
                        .requestAttr("userId", 99L)
                        .contentType("application/json")
                        .content("{\"name\":\"测试分类\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试更新分类")
    void testUpdateCategory() throws Exception {
        Category category = buildCategory();
        when(categoryCommandService.updateCategory(any(Long.class), any(Category.class), any(Long.class)))
                .thenReturn(category);

        mockMvc.perform(put("/api/admin/categories/1")
                        .requestAttr("userId", 99L)
                        .contentType("application/json")
                        .content("{\"name\":\"更新后的分类\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试删除分类")
    void testDeleteCategory() throws Exception {
        mockMvc.perform(delete("/api/admin/categories/1")
                        .requestAttr("userId", 99L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试获取分类反馈列表")
    void testGetCategoryFeedbacks() throws Exception {
        when(categoryFeedbackService.getAllFeedbacks(any(String.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        mockMvc.perform(get("/api/admin/categories/feedback")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试批量启用分类")
    void testBatchEnableCategories() throws Exception {
        Category category = buildCategory();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        mockMvc.perform(post("/api/admin/categories/batch/enable")
                        .requestAttr("userId", 99L)
                        .contentType("application/json")
                        .content("[1]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    private Category buildCategory() {
        Category category = new Category();
        category.setId(1L);
        category.setName("测试分类");
        category.setDescription("测试描述");
        category.setParentId(0L);
        category.setLevel(1);
        category.setSort(1);
        category.setStatus(true);
        category.setCreatedAt(LocalDateTime.now());
        return category;
    }
}
