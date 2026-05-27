package com.idleitems.school.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idleitems.school.dto.SubmitVerificationRequest;
import com.idleitems.school.entity.VerificationRecord;
import com.idleitems.school.service.FileService;
import com.idleitems.school.service.VerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VerificationController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("VerificationController 接口测试")
class VerificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FileService fileService;

    @MockitoBean
    private VerificationService verificationService;

    private SubmitVerificationRequest verificationRequest;

    @BeforeEach
    void setUp() {
        verificationRequest = new SubmitVerificationRequest();
        verificationRequest.setVerificationType("1");
        verificationRequest.setRealName("张三");
        verificationRequest.setIdCard("110101199001011234");
        verificationRequest.setIdCardFront("http://example.com/front.jpg");
        verificationRequest.setIdCardBack("http://example.com/back.jpg");
    }

    @Test
    @DisplayName("提交实名认证 - 成功")
    void testSubmitVerificationSuccess() throws Exception {
        VerificationRecord record = new VerificationRecord();
        record.setId(1L);
        when(verificationService.submit(eq(1L), any(SubmitVerificationRequest.class))).thenReturn(record);

        mockMvc.perform(post("/api/verification/submit")
                        .requestAttr("userId", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(verificationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("提交成功"));
    }

    @Test
    @DisplayName("提交实名认证 - 参数校验失败（认证类型为空）")
    void testSubmitVerificationValidationTypeBlank() throws Exception {
        SubmitVerificationRequest invalidRequest = new SubmitVerificationRequest();
        invalidRequest.setVerificationType("");
        invalidRequest.setRealName("张三");

        mockMvc.perform(post("/api/verification/submit")
                        .requestAttr("userId", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"));
    }

    @Test
    @DisplayName("提交实名认证 - 参数校验失败（姓名格式不正确）")
    void testSubmitVerificationValidationNameInvalid() throws Exception {
        SubmitVerificationRequest invalidRequest = new SubmitVerificationRequest();
        invalidRequest.setVerificationType("1");
        invalidRequest.setRealName("John");

        mockMvc.perform(post("/api/verification/submit")
                        .requestAttr("userId", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"));
    }

    @Test
    @DisplayName("提交实名认证 - 参数校验失败（认证类型无效）")
    void testSubmitVerificationValidationTypeInvalid() throws Exception {
        SubmitVerificationRequest invalidRequest = new SubmitVerificationRequest();
        invalidRequest.setVerificationType("5");
        invalidRequest.setRealName("张三");

        mockMvc.perform(post("/api/verification/submit")
                        .requestAttr("userId", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"));
    }

    @Test
    @DisplayName("获取认证状态 - 成功")
    void testGetVerificationStatusSuccess() throws Exception {
        when(verificationService.getStatus(1L))
                .thenReturn(Map.of("status", "PENDING", "message", "审核中"));

        mockMvc.perform(get("/api/verification/status")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取状态成功"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("重新提交认证 - 成功")
    void testResubmitVerificationSuccess() throws Exception {
        VerificationRecord record = new VerificationRecord();
        record.setId(1L);
        when(verificationService.submit(eq(1L), any(SubmitVerificationRequest.class))).thenReturn(record);

        mockMvc.perform(post("/api/verification/resubmit")
                        .requestAttr("userId", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(verificationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("重新提交成功"));
    }
}
