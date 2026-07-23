package com.idleitems.school.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idleitems.school.module.admin.controller.AdminCategoryController;
import com.idleitems.school.module.category.service.CategoryCommandService;
import com.idleitems.school.module.category.service.CategoryFeedbackService;
import com.idleitems.school.module.category.service.CategoryQueryService;
import com.idleitems.school.module.category.entity.Category;
import com.idleitems.school.module.item.repository.ItemRepository;
import com.idleitems.school.module.category.repository.CategoryRepository;
import com.idleitems.school.module.admin.service.AdminLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminCategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AdminCategoryController 参数校验测试")
class AdminCategoryControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryRepository categoryRepository;

    @MockitoBean
    private ItemRepository itemRepository;

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
        Category cat = new Category();
        cat.setId(1L);
        cat.setName("电子产品");
        cat.setDescription("数码产品");
        cat.setParentId(null);
        cat.setSort(1);
        cat.setStatus(true);
        cat.setLevel(1);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));
        when(categoryRepository.findByParentId(1L)).thenReturn(Collections.emptyList());

        Page<Category> emptyPage = new PageImpl<>(Collections.emptyList());
        when(itemRepository.countByCategoryIds(any())).thenReturn(0L);
    }

    @Test
    @DisplayName("POST /api/admin/categories/{id}/status - body中缺少status字段应返回400")
    void testUpdateCategoryStatus_missingStatus() throws Exception {
        String jsonBody = "{}";

        mockMvc.perform(post("/api/admin/categories/1/status")
                        .requestAttr("userId", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/admin/categories/{id}/status - status为null应返回400")
    void testUpdateCategoryStatus_nullStatus() throws Exception {
        String jsonBody = "{\"status\":null}";

        mockMvc.perform(post("/api/admin/categories/1/status")
                        .requestAttr("userId", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/admin/categories/batch/enable - ids为空列表应返回400")
    void testBatchEnableCategories_emptyIds() throws Exception {
        String jsonBody = "{\"ids\":[]}";

        mockMvc.perform(post("/api/admin/categories/batch/enable")
                        .requestAttr("userId", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"))
                .andExpect(jsonPath("$.data.ids").value("ID列表不能为空"));
    }

    @Test
    @DisplayName("POST /api/admin/categories/batch/disable - ids为空列表应返回400")
    void testBatchDisableCategories_emptyIds() throws Exception {
        String jsonBody = "{\"ids\":[]}";

        mockMvc.perform(post("/api/admin/categories/batch/disable")
                        .requestAttr("userId", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"))
                .andExpect(jsonPath("$.data.ids").value("ID列表不能为空"));
    }

    @Test
    @DisplayName("DELETE /api/admin/categories/batch - ids为空列表应返回400")
    void testBatchDeleteCategories_emptyIds() throws Exception {
        String jsonBody = "{\"ids\":[]}";

        mockMvc.perform(delete("/api/admin/categories/batch")
                        .requestAttr("userId", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"))
                .andExpect(jsonPath("$.data.ids").value("ID列表不能为空"));
    }
}
