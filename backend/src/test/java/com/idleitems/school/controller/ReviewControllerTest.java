package com.idleitems.school.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.module.order.dto.CreateReviewRequest;
import com.idleitems.school.module.order.entity.Review;
import com.idleitems.school.module.order.service.ReviewService;
import com.idleitems.school.module.order.controller.ReviewController;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ReviewController 接口测试")
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewService reviewService;

    private Review testReview;
    private CreateReviewRequest createRequest;

    @BeforeEach
    void setUp() {
        testReview = new Review();
        testReview.setId(1L);
        testReview.setOrderId(1L);
        testReview.setReviewerId(1L);
        testReview.setReviewedUserId(2L);
        testReview.setItemId(1L);
        testReview.setRating(5);
        testReview.setContent("卖家服务很好，物品质量也不错");
        testReview.setIsAnonymous(false);
        testReview.setCreatedAt(LocalDateTime.now());

        createRequest = new CreateReviewRequest();
        createRequest.setRating(5);
        createRequest.setContent("卖家服务很好");
    }

    @Test
    @DisplayName("创建评价 - 成功")
    void testCreateReviewSuccess() throws Exception {
        when(reviewService.createReview(eq(1L), eq(1L), any(CreateReviewRequest.class))).thenReturn(testReview);

        mockMvc.perform(post("/api/reviews/order/1")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("评价成功"))
                .andExpect(jsonPath("$.data.rating").value(5));
    }

    @Test
    @DisplayName("创建评价 - 参数校验失败（评分为空）")
    void testCreateReviewValidationRatingNull() throws Exception {
        CreateReviewRequest invalidRequest = new CreateReviewRequest();
        invalidRequest.setRating(null);
        invalidRequest.setContent("评价内容");

        mockMvc.perform(post("/api/reviews/order/1")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"));
    }

    @Test
    @DisplayName("创建评价 - 参数校验失败（评分超出范围）")
    void testCreateReviewValidationRatingOutOfRange() throws Exception {
        CreateReviewRequest invalidRequest = new CreateReviewRequest();
        invalidRequest.setRating(10);
        invalidRequest.setContent("评价内容");

        mockMvc.perform(post("/api/reviews/order/1")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"));
    }

    @Test
    @DisplayName("创建评价 - 重复评价")
    void testCreateReviewDuplicate() throws Exception {
        when(reviewService.createReview(eq(1L), eq(1L), any(CreateReviewRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.CONFLICT, "您已评价过该订单"));

        mockMvc.perform(post("/api/reviews/order/1")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409));
    }

    @Test
    @DisplayName("获取用户评价 - 成功")
    void testGetUserReviewsSuccess() throws Exception {
        Page<Review> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(reviewService.getReviewsByUserId(eq(1L), any())).thenReturn(emptyPage);

        mockMvc.perform(get("/api/reviews/user/1")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取商品评价 - 成功")
    void testGetItemReviewsSuccess() throws Exception {
        Page<Review> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(reviewService.getReviewsByItemId(eq(1L), any())).thenReturn(emptyPage);

        mockMvc.perform(get("/api/reviews/item/1")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取用户评价统计 - 成功")
    void testGetUserReviewStatsSuccess() throws Exception {
        when(reviewService.getAverageRatingByUserId(1L)).thenReturn(new BigDecimal("4.5"));
        when(reviewService.getReviewCountByUserId(1L)).thenReturn(10L);

        mockMvc.perform(get("/api/reviews/user/1/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.averageRating").value(4.5))
                .andExpect(jsonPath("$.data.reviewCount").value(10));
    }

    @Test
    @DisplayName("获取商品评价统计 - 成功")
    void testGetItemReviewStatsSuccess() throws Exception {
        when(reviewService.getAverageRatingByItemId(1L)).thenReturn(new BigDecimal("4.8"));
        when(reviewService.getReviewCountByItemId(1L)).thenReturn(5L);

        mockMvc.perform(get("/api/reviews/item/1/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.averageRating").value(4.8))
                .andExpect(jsonPath("$.data.reviewCount").value(5));
    }
}
