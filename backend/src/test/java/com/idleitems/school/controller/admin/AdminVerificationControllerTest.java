package com.idleitems.school.controller.admin;

import com.idleitems.school.aspect.PermissionAspect;
import com.idleitems.school.dto.VerificationRecordDTO;
import com.idleitems.school.entity.User;
import com.idleitems.school.entity.VerificationRecord;
import com.idleitems.school.repository.UserRepository;
import com.idleitems.school.repository.VerificationRecordRepository;
import com.idleitems.school.service.VerificationService;
import com.idleitems.school.service.AdminLogService;
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

@WebMvcTest(AdminVerificationController.class)
@AutoConfigureMockMvc(addFilters = false)
@EnableAspectJAutoProxy
@Import(PermissionAspect.class)
@DisplayName("AdminVerificationController 实名认证管理接口测试")
@SuppressWarnings("unchecked")
class AdminVerificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VerificationRecordRepository verificationRecordRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private VerificationService verificationService;

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
    @DisplayName("测试获取认证列表")
    void testGetVerifications() throws Exception {
        VerificationRecord record = buildVerificationRecord();
        when(verificationRecordRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(record), PageRequest.of(0, 10), 1));
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(new User()));

        mockMvc.perform(get("/api/admin/verifications")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试获取认证统计")
    void testGetVerificationStats() throws Exception {
        when(verificationRecordRepository.count()).thenReturn(100L);
        when(verificationRecordRepository.countByStatus(VerificationRecord.Status.PENDING)).thenReturn(10L);
        when(verificationRecordRepository.countByStatus(VerificationRecord.Status.APPROVED)).thenReturn(80L);
        when(verificationRecordRepository.countByStatus(VerificationRecord.Status.REJECTED)).thenReturn(10L);
        when(verificationRecordRepository.countByCreatedAtAfter(any(LocalDateTime.class))).thenReturn(5L);

        mockMvc.perform(get("/api/admin/verifications/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(100));
    }

    @Test
    @DisplayName("测试通过认证")
    void testApproveVerification() throws Exception {
        VerificationRecord record = buildVerificationRecord();
        User user = new User();
        user.setId(1L);
        user.setVerified(false);

        when(verificationRecordRepository.findById(1L)).thenReturn(Optional.of(record));
        when(verificationRecordRepository.save(any(VerificationRecord.class))).thenReturn(record);
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/admin/verifications/1/approve")
                        .requestAttr("userId", 99L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试拒绝认证")
    void testRejectVerification() throws Exception {
        VerificationRecord record = buildVerificationRecord();
        User user = new User();
        user.setId(1L);

        when(verificationRecordRepository.findById(1L)).thenReturn(Optional.of(record));
        when(verificationRecordRepository.save(any(VerificationRecord.class))).thenReturn(record);
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/admin/verifications/1/reject")
                        .requestAttr("userId", 99L)
                        .param("reason", "证件照片模糊"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试批量通过认证")
    void testBatchApproveVerifications() throws Exception {
        VerificationRecord record = buildVerificationRecord();
        User user = new User();
        user.setId(1L);
        user.setVerified(false);

        when(verificationRecordRepository.findAllById(any(List.class)))
                .thenReturn(List.of(record));
        when(verificationRecordRepository.saveAll(any(List.class)))
                .thenReturn(List.of(record));
        when(userRepository.findAllById(any(List.class))).thenReturn(List.of(user));
        when(userRepository.saveAll(any(List.class))).thenReturn(List.of(user));

        mockMvc.perform(post("/api/admin/verifications/batch/approve")
                        .requestAttr("userId", 99L)
                        .contentType("application/json")
                        .content("[1]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试批量拒绝认证")
    void testBatchRejectVerifications() throws Exception {
        VerificationRecord record = buildVerificationRecord();
        User user = new User();
        user.setId(1L);

        when(verificationRecordRepository.findAllById(any(List.class)))
                .thenReturn(List.of(record));
        when(verificationRecordRepository.saveAll(any(List.class)))
                .thenReturn(List.of(record));
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/admin/verifications/batch/reject")
                        .requestAttr("userId", 99L)
                        .contentType("application/json")
                        .content("{\"verificationIds\":[1],\"reason\":\"照片模糊\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    private VerificationRecord buildVerificationRecord() {
        VerificationRecord record = new VerificationRecord();
        record.setId(1L);
        record.setUserId(1L);
        record.setRealName("张三");
        record.setIdCard("110101199901011234");
        record.setType(VerificationRecord.Type.ID_CARD);
        record.setStatus(VerificationRecord.Status.PENDING);
        record.setCreatedAt(LocalDateTime.now());
        return record;
    }
}
