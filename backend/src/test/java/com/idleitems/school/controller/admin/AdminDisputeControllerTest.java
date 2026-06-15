package com.idleitems.school.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idleitems.school.module.admin.controller.AdminDisputeController;
import com.idleitems.school.module.dispute.entity.Dispute;
import com.idleitems.school.module.dispute.service.DisputeCommandService;
import com.idleitems.school.module.dispute.service.DisputeQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminDisputeController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AdminDisputeController 纠纷管理接口测试")
class AdminDisputeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DisputeCommandService disputeCommandService;

    @MockitoBean
    private DisputeQueryService disputeQueryService;

    private Dispute buildDispute() {
        Dispute dispute = new Dispute();
        dispute.setId(1L);
        dispute.setDisputeNo("DSP-001");
        dispute.setOrderId(10L);
        dispute.setApplicantId(2L);
        dispute.setRespondentId(3L);
        dispute.setReason("商品与描述不符");
        dispute.setDescription("收到的商品有明显瑕疵");
        dispute.setDisputeStatus(Dispute.DisputeStatus.PENDING);
        dispute.setDisputeType(1);
        dispute.setPriority(1);
        dispute.setIsUrgent(false);
        dispute.setIsEscalated(false);
        dispute.setCreatedAt(LocalDateTime.of(2026, 5, 20, 10, 0));
        return dispute;
    }

    @Test
    @DisplayName("测试获取纠纷列表 - 成功")
    void testGetDisputes() throws Exception {
        Dispute dispute = buildDispute();
        when(disputeQueryService.getAllDisputes(any(), any()))
                .thenReturn(new PageImpl<>(List.of(dispute), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/admin/disputes")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].disputeNo").value("DSP-001"));
    }

    @Test
    @DisplayName("测试获取纠纷列表 - 按状态筛选")
    void testGetDisputesByStatus() throws Exception {
        when(disputeQueryService.getAllDisputes(eq(Dispute.DisputeStatus.PENDING), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        mockMvc.perform(get("/api/admin/disputes")
                        .param("status", "PENDING")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试获取纠纷详情 - 成功")
    void testGetDispute() throws Exception {
        Dispute dispute = buildDispute();
        when(disputeQueryService.getDisputeById(1L, 0L)).thenReturn(dispute);

        mockMvc.perform(get("/api/admin/disputes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.disputeNo").value("DSP-001"))
                .andExpect(jsonPath("$.data.reason").value("商品与描述不符"));
    }

    @Test
    @DisplayName("测试创建纠纷 - 成功")
    void testCreateDispute() throws Exception {
        Dispute dispute = buildDispute();
        when(disputeCommandService.createDispute(
                eq(2L), eq(10L), eq(1), eq("商品与描述不符"),
                any(), any(), any(), any()))
                .thenReturn(dispute);

        mockMvc.perform(post("/api/admin/disputes")
                        .requestAttr("userId", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"applicantId\":2,\"orderId\":10,\"disputeType\":1,\"reason\":\"商品与描述不符\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("纠纷已创建"))
                .andExpect(jsonPath("$.data.disputeNo").value("DSP-001"));
    }

    @Test
    @DisplayName("测试分配纠纷 - 成功")
    void testAssignDispute() throws Exception {
        Dispute dispute = buildDispute();
        dispute.setDisputeStatus(Dispute.DisputeStatus.ASSIGNED);
        dispute.setHandlerId(5L);
        dispute.setAssignTime(LocalDateTime.now());

        when(disputeCommandService.assignDispute(1L, 5L, 2)).thenReturn(dispute);

        mockMvc.perform(post("/api/admin/disputes/1/assign")
                        .requestAttr("userId", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"handlerId\":5,\"priority\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("纠纷已分配"));
    }

    @Test
    @DisplayName("测试开始处理纠纷 - 成功")
    void testStartProcess() throws Exception {
        Dispute dispute = buildDispute();
        dispute.setDisputeStatus(Dispute.DisputeStatus.PROCESSING);
        dispute.setStartProcessTime(LocalDateTime.now());

        when(disputeCommandService.startProcess(1L, 99L)).thenReturn(dispute);

        mockMvc.perform(post("/api/admin/disputes/1/start")
                        .requestAttr("userId", 99L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("开始处理"));
    }

    @Test
    @DisplayName("测试处理纠纷 - 成功")
    void testHandleDispute() throws Exception {
        Dispute dispute = buildDispute();
        dispute.setDisputeStatus(Dispute.DisputeStatus.RESOLVED);
        dispute.setResult("同意退款");
        dispute.setActualRefundAmount(BigDecimal.valueOf(50));

        when(disputeCommandService.handleDispute(
                eq(1L), eq(99L), eq("同意退款"), eq("REFUND"),
                any(BigDecimal.class), any()))
                .thenReturn(dispute);

        mockMvc.perform(post("/api/admin/disputes/1/handle")
                        .requestAttr("userId", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"result\":\"同意退款\",\"action\":\"REFUND\",\"actualRefundAmount\":50,\"processRemark\":\"核实属实\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("纠纷已处理"));
    }

    @Test
    @DisplayName("测试升级纠纷 - 成功")
    void testEscalateDispute() throws Exception {
        Dispute dispute = buildDispute();
        dispute.setDisputeStatus(Dispute.DisputeStatus.ESCALATED);
        dispute.setIsEscalated(true);
        dispute.setEscalatedTo(10L);
        dispute.setEscalatedReason("需要高级管理员处理");

        when(disputeCommandService.escalateDispute(1L, 10L, "需要高级管理员处理")).thenReturn(dispute);

        mockMvc.perform(post("/api/admin/disputes/1/escalate")
                        .requestAttr("userId", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"escalatedTo\":10,\"reason\":\"需要高级管理员处理\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("纠纷已升级"));
    }

    @Test
    @DisplayName("测试标记紧急纠纷 - 成功")
    void testMarkAsUrgent() throws Exception {
        Dispute dispute = buildDispute();
        dispute.setIsUrgent(true);

        when(disputeCommandService.markAsUrgent(1L, true)).thenReturn(dispute);

        mockMvc.perform(post("/api/admin/disputes/1/urgent")
                        .requestAttr("userId", 99L)
                        .param("urgent", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("已标记为紧急"));
    }

    @Test
    @DisplayName("测试关闭纠纷 - 成功")
    void testCloseDispute() throws Exception {
        Dispute dispute = buildDispute();
        dispute.setDisputeStatus(Dispute.DisputeStatus.CLOSED);
        dispute.setCloseType(2);
        dispute.setCloseTime(LocalDateTime.now());

        when(disputeCommandService.closeDispute(1L, 99L, 2, "管理员关闭")).thenReturn(dispute);

        mockMvc.perform(post("/api/admin/disputes/1/close")
                        .requestAttr("userId", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"closeType\":2,\"reason\":\"管理员关闭\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("纠纷已关闭"));
    }

    @Test
    @DisplayName("测试获取纠纷统计 - 成功")
    void testGetDisputeStats() throws Exception {
        when(disputeQueryService.getDisputeStats()).thenReturn(Map.of(
                "total", 50L,
                "pending", 10L,
                "processing", 15L,
                "resolved", 25L
        ));

        mockMvc.perform(get("/api/admin/disputes/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(50))
                .andExpect(jsonPath("$.data.pending").value(10));
    }
}
