package com.idleitems.school.controller.admin;

import com.idleitems.school.aspect.PermissionAspect;
import com.idleitems.school.entity.AdminLog;
import com.idleitems.school.entity.User;
import com.idleitems.school.repository.UserRepository;
import com.idleitems.school.service.AdminLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminLogController.class)
@AutoConfigureMockMvc(addFilters = false)
@EnableAspectJAutoProxy
@Import(PermissionAspect.class)
@DisplayName("AdminLogController 操作日志接口测试")
class AdminLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminLogService adminLogService;

    @MockBean
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        User adminUser = new User();
        adminUser.setId(99L);
        adminUser.setRole(User.Role.ADMIN);
        when(userRepository.findById(99L)).thenReturn(Optional.of(adminUser));
    }

    @Test
    @DisplayName("测试获取操作日志列表")
    void testGetAdminLogs() throws Exception {
        AdminLog adminLog = buildAdminLog();
        when(adminLogService.getAdminLogs(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(adminLog), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/admin/logs")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试根据关键词搜索操作日志")
    void testSearchAdminLogs() throws Exception {
        AdminLog adminLog = buildAdminLog();
        when(adminLogService.searchAdminLogs(any(String.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(adminLog), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/admin/logs")
                        .param("keyword", "取消订单"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试根据管理员ID查询操作日志")
    void testGetAdminLogsByAdminId() throws Exception {
        AdminLog adminLog = buildAdminLog();
        when(adminLogService.getAdminLogsByAdminId(any(Long.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(adminLog), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/admin/logs")
                        .param("adminId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试获取操作日志详情")
    void testGetAdminLog() throws Exception {
        AdminLog adminLog = buildAdminLog();
        when(adminLogService.getAdminLogById(1L)).thenReturn(adminLog);

        mockMvc.perform(get("/api/admin/logs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    private AdminLog buildAdminLog() {
        AdminLog adminLog = new AdminLog();
        adminLog.setId(1L);
        adminLog.setAdminId(99L);
        adminLog.setOperation("管理员取消订单");
        adminLog.setTargetType("ORDER");
        adminLog.setTargetId(1L);
        adminLog.setCreatedAt(LocalDateTime.now());
        return adminLog;
    }
}
