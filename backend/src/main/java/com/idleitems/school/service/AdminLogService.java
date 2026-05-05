package com.idleitems.school.service;

import com.idleitems.school.entity.AdminLog;
import com.idleitems.school.repository.AdminLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class AdminLogService {

    private final AdminLogRepository adminLogRepository;
    private final ObjectMapper objectMapper;

    public void logOperation(Long adminId, String operation, String targetType, Long targetId, Map<String, Object> details, HttpServletRequest request) {
        AdminLog log = new AdminLog();
        log.setAdminId(adminId);
        log.setOperation(operation);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        
        try {
            log.setDetails(objectMapper.writeValueAsString(details));
        } catch (Exception e) {
            log.setDetails("Error serializing details: " + e.getMessage());
        }
        
        log.setIpAddress(request.getRemoteAddr());
        log.setUserAgent(request.getHeader("User-Agent"));
        
        adminLogRepository.save(log);
    }

    public Page<AdminLog> getAdminLogs(Pageable pageable) {
        return adminLogRepository.findAll(pageable);
    }

    public Page<AdminLog> getAdminLogsByAdminId(Long adminId, Pageable pageable) {
        return adminLogRepository.findByAdminId(adminId, pageable);
    }

    public Page<AdminLog> searchAdminLogs(String keyword, Pageable pageable) {
        return adminLogRepository.findByOperationContainingOrTargetTypeContaining(keyword, keyword, pageable);
    }

    public AdminLog getAdminLogById(Long id) {
        return adminLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("日志记录不存在"));
    }
}
