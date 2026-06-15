package com.idleitems.school.controller.admin;

import com.idleitems.school.module.admin.controller.AdminLogController;
import com.idleitems.school.module.admin.entity.AdminLog;
import com.idleitems.school.module.admin.service.AdminLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminLogController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AdminLogController 操作日志接口测试")
class AdminLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminLogService adminLogService;

    @Test
    @DisplayName("测试获取操作日志列表")
    void getAdminLogs_returnsPagedLogs() throws Exception {
        AdminLog log = buildAdminLog();
        when(adminLogService.getAdminLogsByFilters(
                isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(log)));

        mockMvc.perform(get("/api/admin/logs")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].operation").value("管理员取消订单"));
    }

    @Test
    @DisplayName("测试根据关键词搜索操作日志")
    void getAdminLogs_withKeyword_filtersByKeyword() throws Exception {
        AdminLog log = buildAdminLog();
        when(adminLogService.getAdminLogsByFilters(
                eq("取消"), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(log)));

        mockMvc.perform(get("/api/admin/logs")
                        .param("keyword", "取消"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    @DisplayName("测试根据管理员ID查询操作日志")
    void getAdminLogs_withAdminId_filtersByAdmin() throws Exception {
        AdminLog log = buildAdminLog();
        when(adminLogService.getAdminLogsByFilters(
                isNull(), eq(99L), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(log)));

        mockMvc.perform(get("/api/admin/logs")
                        .param("adminId", "99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试带所有搜索条件的组合筛选")
    void getAdminLogs_withAllFilters_returnsFilteredLogs() throws Exception {
        AdminLog log = buildAdminLog();
        when(adminLogService.getAdminLogsByFilters(
                eq("取消"), eq(99L), eq("ORDER"),
                any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(log)));

        mockMvc.perform(get("/api/admin/logs")
                        .param("keyword", "取消")
                        .param("adminId", "99")
                        .param("targetType", "ORDER")
                        .param("startDate", "2026-01-01T00:00:00")
                        .param("endDate", "2026-12-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].targetType").value("ORDER"));
    }

    @Test
    @DisplayName("测试根据targetType筛选操作日志")
    void getAdminLogs_withTargetType_filtersByType() throws Exception {
        AdminLog log = buildAdminLog();
        when(adminLogService.getAdminLogsByFilters(
                isNull(), isNull(), eq("ORDER"), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(log)));

        mockMvc.perform(get("/api/admin/logs")
                        .param("targetType", "ORDER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试获取日志详情成功")
    void getAdminLog_found_returnsLog() throws Exception {
        AdminLog adminLog = buildAdminLog();
        when(adminLogService.getAdminLogById(1L)).thenReturn(adminLog);

        mockMvc.perform(get("/api/admin/logs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.operation").value("管理员取消订单"))
                .andExpect(jsonPath("$.data.targetType").value("ORDER"));
    }

    @Test
    @DisplayName("测试获取不存在的日志详情返回错误")
    void getAdminLog_notFound_returnsNullData() throws Exception {
        when(adminLogService.getAdminLogById(999L))
                .thenThrow(new IllegalArgumentException("日志记录不存在"));

        mockMvc.perform(get("/api/admin/logs/999"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("测试导出操作日志为CSV")
    void exportLogs_returnsCsvFile() throws Exception {
        AdminLog log = buildAdminLog();
        when(adminLogService.getAdminLogsForExport(
                isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of(log));

        mockMvc.perform(get("/api/admin/logs/export"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv;charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", containsString("attachment;filename=logs_")))
                .andExpect(content().string(containsString("管理员取消订单")))
                .andExpect(content().string(containsString("ORDER")))
                .andExpect(content().string(containsString("ID,操作人ID")));
    }

    @Test
    @DisplayName("测试导出操作日志带筛选条件")
    void exportLogs_withFilters_returnsFilteredCsv() throws Exception {
        AdminLog log = buildAdminLog();
        when(adminLogService.getAdminLogsForExport(
                eq("取消"), eq(99L), eq("ORDER"),
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(log));

        mockMvc.perform(get("/api/admin/logs/export")
                        .param("keyword", "取消")
                        .param("adminId", "99")
                        .param("targetType", "ORDER")
                        .param("startDate", "2026-01-01T00:00:00")
                        .param("endDate", "2026-12-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("管理员取消订单")));
    }

    private AdminLog buildAdminLog() {
        AdminLog adminLog = new AdminLog();
        adminLog.setId(1L);
        adminLog.setAdminId(99L);
        adminLog.setOperation("管理员取消订单");
        adminLog.setTargetType("ORDER");
        adminLog.setTargetId(1L);
        adminLog.setIpAddress("127.0.0.1");
        adminLog.setDetails("{\"reason\": \"item sold out\"}");
        adminLog.setCreatedAt(LocalDateTime.of(2026, 5, 7, 10, 30, 0));
        return adminLog;
    }
}
