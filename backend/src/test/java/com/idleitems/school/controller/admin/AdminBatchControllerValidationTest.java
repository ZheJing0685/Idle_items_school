package com.idleitems.school.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idleitems.school.module.admin.controller.AdminBatchController;
import com.idleitems.school.module.admin.service.AdminBatchService;
import com.idleitems.school.module.admin.service.AdminLogService;
import com.idleitems.school.module.system.service.DictService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminBatchController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AdminBatchController 参数校验测试")
class AdminBatchControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    @DisplayName("POST /api/admin/batch/items/approve - itemIds为空列表应返回400")
    void testBatchApproveItems_emptyItemIds() throws Exception {
        String jsonBody = "{\"itemIds\":[]}";

        mockMvc.perform(post("/api/admin/batch/items/approve")
                        .requestAttr("userId", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"))
                .andExpect(jsonPath("$.data.itemIds").value("物品ID列表不能为空"));
    }

    @Test
    @DisplayName("POST /api/admin/batch/items/reject - itemIds为空列表应返回400")
    void testBatchRejectItems_emptyItemIds() throws Exception {
        String jsonBody = "{\"itemIds\":[],\"reason\":\"测试原因\"}";

        mockMvc.perform(post("/api/admin/batch/items/reject")
                        .requestAttr("userId", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"))
                .andExpect(jsonPath("$.data.itemIds").value("物品ID列表不能为空"));
    }

    @Test
    @DisplayName("POST /api/admin/batch/items/reject - reason超过500字符应返回400")
    void testBatchRejectItems_reasonTooLong() throws Exception {
        String longReason = "a".repeat(501);
        String jsonBody = "{\"itemIds\":[1,2],\"reason\":\"" + longReason + "\"}";

        mockMvc.perform(post("/api/admin/batch/items/reject")
                        .requestAttr("userId", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"))
                .andExpect(jsonPath("$.data.reason").exists());
    }

    @Test
    @DisplayName("POST /api/admin/batch/items/off-shelf - itemIds为空列表应返回400")
    void testBatchOffShelfItems_emptyItemIds() throws Exception {
        String jsonBody = "{\"itemIds\":[],\"reason\":\"测试原因\"}";

        mockMvc.perform(post("/api/admin/batch/items/off-shelf")
                        .requestAttr("userId", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"))
                .andExpect(jsonPath("$.data.itemIds").value("物品ID列表不能为空"));
    }

    @Test
    @DisplayName("POST /api/admin/batch/items/off-shelf - reason超过500字符应返回400")
    void testBatchOffShelfItems_reasonTooLong() throws Exception {
        String longReason = "b".repeat(501);
        String jsonBody = "{\"itemIds\":[1],\"reason\":\"" + longReason + "\"}";

        mockMvc.perform(post("/api/admin/batch/items/off-shelf")
                        .requestAttr("userId", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"))
                .andExpect(jsonPath("$.data.reason").exists());
    }

    @Test
    @DisplayName("POST /api/admin/batch/users/status - userIds为空列表应返回400")
    void testBatchUpdateUserStatus_emptyUserIds() throws Exception {
        String jsonBody = "{\"userIds\":[],\"status\":\"DISABLED\"}";

        mockMvc.perform(post("/api/admin/batch/users/status")
                        .requestAttr("userId", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"))
                .andExpect(jsonPath("$.data.userIds").value("用户ID列表不能为空"));
    }

    @Test
    @DisplayName("POST /api/admin/batch/users/status - status为null应返回400")
    void testBatchUpdateUserStatus_nullStatus() throws Exception {
        String jsonBody = "{\"userIds\":[1],\"status\":null}";

        mockMvc.perform(post("/api/admin/batch/users/status")
                        .requestAttr("userId", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"));
    }

    @Test
    @DisplayName("POST /api/admin/batch/orders/cancel - orderIds为空列表应返回400")
    void testBatchCancelOrders_emptyOrderIds() throws Exception {
        String jsonBody = "{\"orderIds\":[],\"reason\":\"测试取消\"}";

        mockMvc.perform(post("/api/admin/batch/orders/cancel")
                        .requestAttr("userId", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"))
                .andExpect(jsonPath("$.data.orderIds").value("订单ID列表不能为空"));
    }

    @Test
    @DisplayName("POST /api/admin/batch/orders/cancel - reason超过500字符应返回400")
    void testBatchCancelOrders_reasonTooLong() throws Exception {
        String longReason = "c".repeat(501);
        String jsonBody = "{\"orderIds\":[1],\"reason\":\"" + longReason + "\"}";

        mockMvc.perform(post("/api/admin/batch/orders/cancel")
                        .requestAttr("userId", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"));
    }

    @Test
    @DisplayName("POST /api/admin/batch/users/delete - userIds为空列表应返回400")
    void testBatchDeleteUsers_emptyUserIds() throws Exception {
        String jsonBody = "{\"userIds\":[]}";

        mockMvc.perform(post("/api/admin/batch/users/delete")
                        .requestAttr("userId", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"))
                .andExpect(jsonPath("$.data.userIds").value("用户ID列表不能为空"));
    }
}
