package com.idleitems.school.service;

import com.idleitems.school.dto.analytics.LogAnalysisResponse;
import com.idleitems.school.repository.AdminLogRepository;
import com.idleitems.school.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LogAnalysisServiceTest {

    @Mock
    private AdminLogRepository adminLogRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LogAnalysisService logAnalysisService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetLogAnalysis() {
        // 准备测试数据
        when(adminLogRepository.count()).thenReturn(100L);
        when(adminLogRepository.countByCreatedAtAfter(any())).thenReturn(10L);
        when(adminLogRepository.countGroupByOperation()).thenReturn(new ArrayList<>());
        when(adminLogRepository.countGroupByTargetType()).thenReturn(new ArrayList<>());
        when(adminLogRepository.countByCreatedAtBetween(any(), any())).thenReturn(5L);
        when(adminLogRepository.findTop5AdminsByOperationCount()).thenReturn(new ArrayList<>());

        // 执行测试
        LogAnalysisResponse response = logAnalysisService.getLogAnalysis();

        // 验证结果
        assertNotNull(response);
        assertEquals(100L, response.getTotalOperations());
        verify(adminLogRepository, times(1)).count();
    }
}