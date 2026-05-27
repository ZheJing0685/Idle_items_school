package com.idleitems.school.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.dto.CreateDisputeRequest;
import com.idleitems.school.dto.ReplyDisputeRequest;
import com.idleitems.school.dto.SatisfactionRequest;
import com.idleitems.school.entity.Dispute;
import com.idleitems.school.service.DisputeService;
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

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DisputeController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("DisputeController 接口测试")
class DisputeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DisputeService disputeService;

    private Dispute testDispute;
    private CreateDisputeRequest createRequest;
    private ReplyDisputeRequest replyRequest;
    private SatisfactionRequest satisfactionRequest;

    @BeforeEach
    void setUp() {
        testDispute = new Dispute();
        testDispute.setId(1L);
        testDispute.setDisputeNo("DSP20250101001");
        testDispute.setOrderId(1L);
        testDispute.setApplicantId(1L);
        testDispute.setRespondentId(2L);
        testDispute.setReason("商品与描述不符");
        testDispute.setDisputeStatus(Dispute.DisputeStatus.PENDING);

        createRequest = new CreateDisputeRequest();
        createRequest.setOrderId(1L);
        createRequest.setReason("商品与描述不符");
        createRequest.setDescription("收到的商品与卖家描述不一致");

        replyRequest = new ReplyDisputeRequest();
        replyRequest.setContent("我已经收到了相关证据");

        satisfactionRequest = new SatisfactionRequest();
        satisfactionRequest.setScore(4);
        satisfactionRequest.setRemark("处理速度较快");
    }

    @Test
    @DisplayName("创建纠纷 - 成功")
    void testCreateDisputeSuccess() throws Exception {
        when(disputeService.createDispute(eq(1L), eq(1L), any(), eq("商品与描述不符"),
                eq("收到的商品与卖家描述不一致"), any(), any(), any())).thenReturn(testDispute);

        mockMvc.perform(post("/api/disputes")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("纠纷已提交"));
    }

    @Test
    @DisplayName("创建纠纷 - 参数校验失败（订单ID为空）")
    void testCreateDisputeValidationOrderIdNull() throws Exception {
        CreateDisputeRequest invalidRequest = new CreateDisputeRequest();
        invalidRequest.setOrderId(null);
        invalidRequest.setReason("商品与描述不符");

        mockMvc.perform(post("/api/disputes")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"));
    }

    @Test
    @DisplayName("创建纠纷 - 参数校验失败（原因为空）")
    void testCreateDisputeValidationReasonBlank() throws Exception {
        CreateDisputeRequest invalidRequest = new CreateDisputeRequest();
        invalidRequest.setOrderId(1L);
        invalidRequest.setReason("");

        mockMvc.perform(post("/api/disputes")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"));
    }

    @Test
    @DisplayName("获取我的纠纷列表 - 成功")
    void testGetMyDisputesSuccess() throws Exception {
        Page<Dispute> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(disputeService.getMyDisputes(eq(1L), eq(null), any())).thenReturn(emptyPage);

        mockMvc.perform(get("/api/disputes")
                        .requestAttr("userId", 1L)
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取纠纷详情 - 成功")
    void testGetDisputeSuccess() throws Exception {
        when(disputeService.getDisputeById(1L, 1L)).thenReturn(testDispute);

        mockMvc.perform(get("/api/disputes/1")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.reason").value("商品与描述不符"));
    }

    @Test
    @DisplayName("获取纠纷详情 - 纠纷不存在")
    void testGetDisputeNotFound() throws Exception {
        when(disputeService.getDisputeById(999L, 1L))
                .thenThrow(new BusinessException(ErrorCode.NOT_FOUND, "纠纷不存在"));

        mockMvc.perform(get("/api/disputes/999")
                        .requestAttr("userId", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("回复纠纷 - 成功")
    void testReplyDisputeSuccess() throws Exception {
        when(disputeService.replyDispute(eq(1L), eq(1L), anyString())).thenReturn(testDispute);

        mockMvc.perform(post("/api/disputes/1/reply")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(replyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("回复成功"));
    }

    @Test
    @DisplayName("回复纠纷 - 参数校验失败（内容为空）")
    void testReplyDisputeValidationContentBlank() throws Exception {
        ReplyDisputeRequest invalidRequest = new ReplyDisputeRequest();
        invalidRequest.setContent("");

        mockMvc.perform(post("/api/disputes/1/reply")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"));
    }

    @Test
    @DisplayName("提交纠纷满意度评价 - 成功")
    void testSubmitSatisfactionSuccess() throws Exception {
        when(disputeService.submitSatisfaction(eq(1L), eq(1L), eq(4), anyString())).thenReturn(testDispute);

        mockMvc.perform(post("/api/disputes/1/satisfaction")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(satisfactionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("评价提交成功"));
    }

    @Test
    @DisplayName("提交满意度评价 - 参数校验失败（评分为空）")
    void testSubmitSatisfactionValidationScoreNull() throws Exception {
        SatisfactionRequest invalidRequest = new SatisfactionRequest();
        invalidRequest.setScore(null);

        mockMvc.perform(post("/api/disputes/1/satisfaction")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"));
    }

    @Test
    @DisplayName("提交满意度评价 - 参数校验失败（评分超出范围）")
    void testSubmitSatisfactionValidationScoreOutOfRange() throws Exception {
        SatisfactionRequest invalidRequest = new SatisfactionRequest();
        invalidRequest.setScore(10);

        mockMvc.perform(post("/api/disputes/1/satisfaction")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"));
    }

    @Test
    @DisplayName("获取纠纷统计 - 成功")
    void testGetDisputeStatsSuccess() throws Exception {
        when(disputeService.getDisputeStats()).thenReturn(Map.of("total", 10, "pending", 3));

        mockMvc.perform(get("/api/disputes/stats")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(10));
    }

    @Test
    @DisplayName("检查是否可以发起纠纷 - 成功")
    void testCanCreateDisputeSuccess() throws Exception {
        when(disputeService.canCreateDispute(1L, 1L)).thenReturn(Map.of("canDispute", true));

        mockMvc.perform(get("/api/disputes/orders/1/can-dispute")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.canDispute").value(true));
    }

    @Test
    @DisplayName("获取订单活跃纠纷 - 成功")
    void testGetActiveDisputeSuccess() throws Exception {
        when(disputeService.getActiveDisputeByOrder(1L, 1L)).thenReturn(testDispute);

        mockMvc.perform(get("/api/disputes/orders/1/dispute")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
