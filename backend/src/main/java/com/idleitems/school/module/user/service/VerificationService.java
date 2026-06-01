package com.idleitems.school.module.user.service;

import com.idleitems.school.module.user.dto.SubmitVerificationRequest;
import com.idleitems.school.module.user.entity.VerificationRecord;

import java.util.Map;

public interface VerificationService {

    VerificationRecord submit(Long userId, SubmitVerificationRequest request);

    Map<String, Object> getStatus(Long userId);

    /**
     * 解密身份证号（用于管理端展示）
     */
    String decryptIdCard(String encryptedIdCard);

    /**
     * 脱敏身份证号（用于前端展示）
     */
    String maskIdCard(String idCard);
}
