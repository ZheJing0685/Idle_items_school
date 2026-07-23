package com.idleitems.school.controller.admin;

import com.idleitems.school.module.admin.controller.AdminBatchController;
import com.idleitems.school.module.admin.service.AdminBatchService;
import com.idleitems.school.module.admin.service.AdminLogService;
import com.idleitems.school.module.system.service.DictService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminBatchController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
@DisplayName("AdminBatchController 批量操作接口测试")
class AdminBatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminBatchService adminBatchService;

    @MockitoBean
    private AdminLogService adminLogService;

    @MockitoBean
    private DictService dictService;

    @BeforeEach
    void setUp() {
        doNothing().when(adminBatchService).batchApproveItems(any());
        doNothing().when(adminBatchService).batchRejectItems(any(), any());
        doNothing().when(adminBatchService).batchOffShelfItems(any(), any());
        doNothing().when(adminBatchService).batchUpdateUserStatus(any(), any());
        doNothing().when(adminBatchService).batchCancelOrders(any(), any(), any());
        doNothing().when(adminBatchService).batchDeleteUsers(any());
    }

    @Test
    @DisplayName("测试批量审核通过物品")
    void testBatchApproveItems() throws Exception {
        mockMvc.perform(post("/api/admin/batch/items/approve")
                        .requestAttr("userId", 99L)
                        .contentType("application/json")
                        .content("{\"itemIds\": [1]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试批量驳回物品")
    void testBatchRejectItems() throws Exception {
        mockMvc.perform(post("/api/admin/batch/items/reject")
                        .requestAttr("userId", 99L)
                        .contentType("application/json")
                        .content("{\"itemIds\": [1], \"reason\": \"违规\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试批量下架物品")
    void testBatchOffShelfItems() throws Exception {
        mockMvc.perform(post("/api/admin/batch/items/off-shelf")
                        .requestAttr("userId", 99L)
                        .contentType("application/json")
                        .content("{\"itemIds\": [1], \"reason\": \"下架\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试批量更新用户状态")
    void testBatchUpdateUserStatus() throws Exception {
        mockMvc.perform(post("/api/admin/batch/users/status")
                        .requestAttr("userId", 99L)
                        .contentType("application/json")
                        .content("{\"userIds\": [1], \"status\": \"ACTIVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试批量取消订单")
    void testBatchCancelOrders() throws Exception {
        mockMvc.perform(post("/api/admin/batch/orders/cancel")
                        .requestAttr("userId", 99L)
                        .contentType("application/json")
                        .content("{\"orderIds\": [1], \"reason\": \"管理员取消\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
