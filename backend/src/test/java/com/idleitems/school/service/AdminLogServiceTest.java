package com.idleitems.school.service;

import com.idleitems.school.module.admin.entity.AdminLog;
import com.idleitems.school.module.admin.repository.AdminLogRepository;
import com.idleitems.school.module.admin.service.AdminLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("管理员日志服务测试")
class AdminLogServiceTest {

    @Mock
    private AdminLogRepository adminLogRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AdminLogService adminLogService;

    private AdminLog testLog;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testLog = new AdminLog();
        testLog.setId(1L);
        testLog.setAdminId(1L);
        testLog.setOperation("DELETE_ITEM");
        testLog.setTargetType("ITEM");
        testLog.setTargetId(100L);
        testLog.setDetails("{\"itemId\": 100}");
        testLog.setCreatedAt(LocalDateTime.now());
        testLog.setIpAddress("127.0.0.1");
        testLog.setUserAgent("Mozilla/5.0");

        pageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("记录操作日志 - 成功")
    void logOperation_WhenValidInput_SavesLog() throws Exception {
        when(request.getHeader("X-Real-IP")).thenReturn("192.168.1.1");
        when(request.getHeader("User-Agent")).thenReturn("TestAgent");
        when(objectMapper.writeValueAsString(anyMap())).thenReturn("{\"key\":\"value\"}");

        Map<String, Object> details = new HashMap<>();
        details.put("itemId", 100L);
        adminLogService.logOperation(1L, "DELETE_ITEM", "ITEM", 100L, details, request);

        verify(adminLogRepository).save(any(AdminLog.class));
    }

    @Test
    @DisplayName("记录操作日志 - 序列化失败时记录错误信息")
    void logOperation_WhenSerializationFails_RecordsError() throws Exception {
        when(request.getHeader("X-Real-IP")).thenReturn("192.168.1.1");
        when(request.getHeader("User-Agent")).thenReturn("TestAgent");
        when(objectMapper.writeValueAsString(anyMap())).thenThrow(new RuntimeException("Serialization error"));

        Map<String, Object> details = new HashMap<>();
        adminLogService.logOperation(1L, "DELETE_ITEM", "ITEM", 100L, details, request);

        verify(adminLogRepository).save(argThat(log ->
            log.getDetails().contains("Error serializing details")));
    }

    @Test
    @DisplayName("记录操作日志 - X-Real-IP为空时使用RemoteAddr")
    void logOperation_whenXRealIpNull_usesRemoteAddr() throws Exception {
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("TestAgent");
        when(objectMapper.writeValueAsString(anyMap())).thenReturn("{}");

        adminLogService.logOperation(1L, "TEST", "ITEM", 1L, Map.of(), request);

        ArgumentCaptor<AdminLog> captor = ArgumentCaptor.forClass(AdminLog.class);
        verify(adminLogRepository).save(captor.capture());
        assertEquals("10.0.0.1", captor.getValue().getIpAddress());
    }

    @Test
    @DisplayName("记录操作日志 - X-Real-IP为unknown时使用RemoteAddr")
    void logOperation_whenXRealIpUnknown_usesRemoteAddr() throws Exception {
        when(request.getHeader("X-Real-IP")).thenReturn("unknown");
        when(request.getRemoteAddr()).thenReturn("10.0.0.2");
        when(request.getHeader("User-Agent")).thenReturn("TestAgent");
        when(objectMapper.writeValueAsString(anyMap())).thenReturn("{}");

        adminLogService.logOperation(1L, "TEST", "ITEM", 1L, Map.of(), request);

        ArgumentCaptor<AdminLog> captor = ArgumentCaptor.forClass(AdminLog.class);
        verify(adminLogRepository).save(captor.capture());
        assertEquals("10.0.0.2", captor.getValue().getIpAddress());
    }

    @Test
    @DisplayName("记录操作日志 - X-Real-IP为空字符串时使用RemoteAddr")
    void logOperation_whenXRealIpEmpty_usesRemoteAddr() throws Exception {
        when(request.getHeader("X-Real-IP")).thenReturn("");
        when(request.getRemoteAddr()).thenReturn("10.0.0.3");
        when(request.getHeader("User-Agent")).thenReturn("TestAgent");
        when(objectMapper.writeValueAsString(anyMap())).thenReturn("{}");

        adminLogService.logOperation(1L, "TEST", "ITEM", 1L, Map.of(), request);

        ArgumentCaptor<AdminLog> captor = ArgumentCaptor.forClass(AdminLog.class);
        verify(adminLogRepository).save(captor.capture());
        assertEquals("10.0.0.3", captor.getValue().getIpAddress());
    }

    @Test
    @DisplayName("记录操作日志 - 无UserAgent")
    void logOperation_whenUserAgentNull_savesNull() throws Exception {
        when(request.getHeader("X-Real-IP")).thenReturn("192.168.1.1");
        when(request.getHeader("User-Agent")).thenReturn(null);
        when(objectMapper.writeValueAsString(anyMap())).thenReturn("{}");

        adminLogService.logOperation(1L, "TEST", "ITEM", 1L, Map.of(), request);

        ArgumentCaptor<AdminLog> captor = ArgumentCaptor.forClass(AdminLog.class);
        verify(adminLogRepository).save(captor.capture());
        assertNull(captor.getValue().getUserAgent());
    }

    @Test
    @DisplayName("获取所有管理员日志")
    void getAdminLogs_ReturnsPage() {
        Page<AdminLog> page = new PageImpl<>(List.of(testLog));
        when(adminLogRepository.findAll(pageable)).thenReturn(page);

        Page<AdminLog> result = adminLogService.getAdminLogs(pageable);

        assertEquals(1, result.getContent().size());
        verify(adminLogRepository).findAll(pageable);
    }

    @Test
    @DisplayName("根据管理员ID获取日志")
    void getAdminLogsByAdminId_ReturnsFilteredLogs() {
        Page<AdminLog> page = new PageImpl<>(List.of(testLog));
        when(adminLogRepository.findByAdminId(1L, pageable)).thenReturn(page);

        Page<AdminLog> result = adminLogService.getAdminLogsByAdminId(1L, pageable);

        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getAdminId());
    }

    @Test
    @DisplayName("搜索管理员日志")
    void searchAdminLogs_ReturnsMatchingLogs() {
        Page<AdminLog> page = new PageImpl<>(List.of(testLog));
        when(adminLogRepository.findByOperationContainingOrTargetTypeContaining("DELETE", "DELETE", pageable))
                .thenReturn(page);

        Page<AdminLog> result = adminLogService.searchAdminLogs("DELETE", pageable);

        assertEquals(1, result.getContent().size());
        assertEquals("DELETE_ITEM", result.getContent().get(0).getOperation());
    }

    @Test
    @DisplayName("根据ID获取日志 - 成功")
    void getAdminLogById_WhenExists_ReturnsLog() {
        when(adminLogRepository.findById(1L)).thenReturn(Optional.of(testLog));

        AdminLog result = adminLogService.getAdminLogById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("DELETE_ITEM", result.getOperation());
    }

    @Test
    @DisplayName("根据ID获取日志 - 不存在时抛出异常")
    void getAdminLogById_WhenNotExists_ThrowsException() {
        when(adminLogRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            adminLogService.getAdminLogById(999L);
        });
    }

    @Test
    @DisplayName("根据筛选条件获取日志")
    void getAdminLogsByFilters_ReturnsFilteredLogs() {
        Page<AdminLog> page = new PageImpl<>(List.of(testLog));
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();

        when(adminLogRepository.findByFilters("DELETE", 1L, "ITEM", startDate, endDate, pageable))
                .thenReturn(page);

        Page<AdminLog> result = adminLogService.getAdminLogsByFilters(
                "DELETE", 1L, "ITEM", startDate, endDate, pageable);

        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("根据筛选条件获取日志 - 全空参数")
    void getAdminLogsByFilters_withNullParams_returnsResults() {
        Page<AdminLog> page = new PageImpl<>(List.of(testLog));
        when(adminLogRepository.findByFilters(null, null, null, null, null, pageable))
                .thenReturn(page);

        Page<AdminLog> result = adminLogService.getAdminLogsByFilters(null, null, null, null, null, pageable);

        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("获取导出日志")
    void getAdminLogsForExport_ReturnsLogs() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();

        when(adminLogRepository.findAllByFilters(null, null, null, startDate, endDate))
                .thenReturn(List.of(testLog));

        List<AdminLog> result = adminLogService.getAdminLogsForExport(null, null, null, startDate, endDate);

        assertEquals(1, result.size());
        verify(adminLogRepository).findAllByFilters(null, null, null, startDate, endDate);
    }

    @Test
    @DisplayName("获取导出日志 - 带全部筛选参数")
    void getAdminLogsForExport_withAllFilters_returnsLogs() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();
        when(adminLogRepository.findAllByFilters("keyword", 1L, "ITEM", startDate, endDate))
                .thenReturn(List.of(testLog));

        List<AdminLog> result = adminLogService.getAdminLogsForExport("keyword", 1L, "ITEM", startDate, endDate);

        assertEquals(1, result.size());
        assertEquals("ITEM", result.get(0).getTargetType());
    }
}
