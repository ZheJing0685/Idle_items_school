package com.idleitems.school.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idleitems.school.dto.SubmitFeedbackRequest;
import com.idleitems.school.entity.CategoryFeedback;
import com.idleitems.school.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("CategoryController 接口测试")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    private SubmitFeedbackRequest feedbackRequest;

    @BeforeEach
    void setUp() {
        feedbackRequest = new SubmitFeedbackRequest();
        feedbackRequest.setFeedbackType("MISSING");
        feedbackRequest.setDescription("缺少电子产品分类");
        feedbackRequest.setCategoryId(1L);
    }

    @Test
    @DisplayName("获取分类列表 - 成功")
    void testGetCategoriesSuccess() throws Exception {
        List<Map<String, Object>> categories = List.of(
                Map.of("id", 1, "name", "电子产品", "children", Collections.emptyList())
        );
        when(categoryService.getAllCategories()).thenReturn(categories);

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取分类树 - 成功")
    void testGetCategoryTreeSuccess() throws Exception {
        List<Map<String, Object>> tree = List.of(
                Map.of("id", 1, "name", "电子产品", "children", Collections.emptyList())
        );
        when(categoryService.getCategoryTree()).thenReturn(tree);

        mockMvc.perform(get("/api/categories/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("搜索分类 - 成功")
    void testSearchCategoriesSuccess() throws Exception {
        List<Map<String, Object>> results = List.of(
                Map.of("id", 1, "name", "电子产品")
        );
        when(categoryService.searchCategories("电子")).thenReturn(results);

        mockMvc.perform(get("/api/categories/search")
                        .param("keyword", "电子"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("提交分类反馈 - 成功")
    void testSubmitFeedbackSuccess() throws Exception {
        doNothing().when(categoryService).submitFeedback(eq(1L), any(), eq(1L), anyString());

        mockMvc.perform(post("/api/categories/feedback")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(feedbackRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("反馈提交成功"));
    }

    @Test
    @DisplayName("提交分类反馈 - 参数校验失败（反馈类型为空）")
    void testSubmitFeedbackValidationTypeBlank() throws Exception {
        SubmitFeedbackRequest invalidRequest = new SubmitFeedbackRequest();
        invalidRequest.setFeedbackType("");
        invalidRequest.setDescription("缺少电子产品分类");

        mockMvc.perform(post("/api/categories/feedback")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"));
    }

    @Test
    @DisplayName("提交分类反馈 - 参数校验失败（描述为空）")
    void testSubmitFeedbackValidationDescriptionBlank() throws Exception {
        SubmitFeedbackRequest invalidRequest = new SubmitFeedbackRequest();
        invalidRequest.setFeedbackType("MISSING");
        invalidRequest.setDescription("");

        mockMvc.perform(post("/api/categories/feedback")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"));
    }

    @Test
    @DisplayName("获取我的反馈 - 成功")
    void testGetMyFeedbacksSuccess() throws Exception {
        Page<CategoryFeedback> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(categoryService.getMyFeedbacks(eq(1L), any())).thenReturn(emptyPage);

        mockMvc.perform(get("/api/categories/feedback/my")
                        .requestAttr("userId", 1L)
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
