package com.idleitems.school.service;

import com.idleitems.school.dto.SubmitVerificationRequest;
import com.idleitems.school.entity.VerificationRecord;

import java.util.Map;

public interface VerificationService {

    VerificationRecord submit(Long userId, SubmitVerificationRequest request);

    Map<String, Object> getStatus(Long userId);
}
