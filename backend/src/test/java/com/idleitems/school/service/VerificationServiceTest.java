package com.idleitems.school.service;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.dto.SubmitVerificationRequest;
import com.idleitems.school.entity.User;
import com.idleitems.school.entity.VerificationRecord;
import com.idleitems.school.repository.UserRepository;
import com.idleitems.school.repository.VerificationRecordRepository;
import com.idleitems.school.service.impl.VerificationServiceImpl;
import com.idleitems.school.util.DataEncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("VerificationService 单元测试")
class VerificationServiceTest {

    @Mock
    private VerificationRecordRepository verificationRecordRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DataEncryptionUtil dataEncryptionUtil;

    @InjectMocks
    private VerificationServiceImpl verificationService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setVerified(false);
    }

    @Test
    @DisplayName("测试提交身份证认证")
    void testSubmitIdCardVerification() {
        SubmitVerificationRequest req = buildIdCardRequest();
        when(userRepository.existsById(1L)).thenReturn(true);
        when(verificationRecordRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(verificationRecordRepository.save(any(VerificationRecord.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        VerificationRecord result = verificationService.submit(1L, req);

        assertNotNull(result);
        assertEquals(VerificationRecord.Type.ID_CARD, result.getType());
        assertEquals(VerificationRecord.Status.PENDING, result.getStatus());
        assertEquals("张三", result.getRealName());
        verify(verificationRecordRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("测试提交学生证认证")
    void testSubmitStudentCardVerification() {
        SubmitVerificationRequest req = new SubmitVerificationRequest();
        req.setVerificationType("2");
        req.setRealName("李四");
        req.setStudentId("2021001");
        req.setSchool("某某大学");
        req.setStudentCard("http://img/student.jpg");

        when(userRepository.existsById(1L)).thenReturn(true);
        when(verificationRecordRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(verificationRecordRepository.save(any(VerificationRecord.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        VerificationRecord result = verificationService.submit(1L, req);

        assertEquals(VerificationRecord.Type.STUDENT_CARD, result.getType());
        assertEquals("2021001", result.getStudentId());
    }

    @Test
    @DisplayName("测试提交认证-用户不存在")
    void testSubmitVerificationUserNotFound() {
        SubmitVerificationRequest req = buildIdCardRequest();
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThrows(BusinessException.class, () ->
                verificationService.submit(999L, req));
        verify(verificationRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("测试提交认证-无效认证类型")
    void testSubmitInvalidType() {
        SubmitVerificationRequest req = new SubmitVerificationRequest();
        req.setVerificationType("99");
        req.setRealName("张三");

        when(userRepository.existsById(1L)).thenReturn(true);

        assertThrows(BusinessException.class, () ->
                verificationService.submit(1L, req));
        verify(verificationRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("测试获取认证状态-已审核通过")
    void testGetStatusApproved() {
        VerificationRecord record = new VerificationRecord();
        record.setUserId(1L);
        record.setStatus(VerificationRecord.Status.APPROVED);

        when(verificationRecordRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(record));

        Map<String, Object> status = verificationService.getStatus(1L);

        assertEquals("approved", status.get("status"));
        assertEquals("已通过", status.get("message"));
    }

    @Test
    @DisplayName("测试获取认证状态-未认证")
    void testGetStatusUnverified() {
        when(verificationRecordRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());

        Map<String, Object> status = verificationService.getStatus(1L);

        assertEquals("unverified", status.get("status"));
    }

    @Test
    @DisplayName("测试获取认证状态-审核中")
    void testGetStatusPending() {
        VerificationRecord record = new VerificationRecord();
        record.setUserId(1L);
        record.setStatus(VerificationRecord.Status.PENDING);

        when(verificationRecordRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(record));

        Map<String, Object> status = verificationService.getStatus(1L);

        assertEquals("pending", status.get("status"));
        assertEquals("审核中", status.get("message"));
    }

    @Test
    @DisplayName("测试重新提交创建新记录")
    void testResubmitCreatesNewRecord() {
        SubmitVerificationRequest req = buildIdCardRequest();
        VerificationRecord existing = new VerificationRecord();
        existing.setUserId(1L);
        existing.setStatus(VerificationRecord.Status.REJECTED);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(verificationRecordRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(existing));
        when(verificationRecordRepository.save(any(VerificationRecord.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        VerificationRecord result = verificationService.submit(1L, req);

        assertEquals(VerificationRecord.Status.PENDING, result.getStatus());
        verify(verificationRecordRepository, times(1)).save(any(VerificationRecord.class));
    }

    private SubmitVerificationRequest buildIdCardRequest() {
        SubmitVerificationRequest req = new SubmitVerificationRequest();
        req.setVerificationType("1");
        req.setRealName("张三");
        req.setIdCard("110101199901011232"); // 有效的18位身份证号（含正确校验位）
        req.setIdCardFront("http://img/front.jpg");
        req.setIdCardBack("http://img/back.jpg");
        return req;
    }
}
