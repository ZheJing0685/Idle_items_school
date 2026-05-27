package com.idleitems.school.controller;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.entity.Notification;
import com.idleitems.school.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("NotificationController 接口测试")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testNotification = new Notification();
        testNotification.setId(1L);
        testNotification.setUserId(1L);
        testNotification.setNotificationType(1);
        testNotification.setTitle("系统通知");
        testNotification.setContent("您的物品已通过审核");
        testNotification.setIsRead(false);
        testNotification.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("获取通知列表 - 成功")
    void testGetNotificationsSuccess() throws Exception {
        Page<Notification> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0);
        when(notificationService.getNotifications(eq(1L), any())).thenReturn(emptyPage);

        mockMvc.perform(get("/api/notifications")
                        .requestAttr("userId", 1L)
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取未读通知数量 - 成功")
    void testGetUnreadCountSuccess() throws Exception {
        when(notificationService.getUnreadCount(1L)).thenReturn(5L);

        mockMvc.perform(get("/api/notifications/unread-count")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.count").value(5));
    }

    @Test
    @DisplayName("标记通知为已读 - 成功")
    void testMarkAsReadSuccess() throws Exception {
        doNothing().when(notificationService).markAsRead(1L, 1L);

        mockMvc.perform(post("/api/notifications/1/read")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("已标记为已读"));
    }

    @Test
    @DisplayName("标记通知为已读 - 通知不存在")
    void testMarkAsReadNotFound() throws Exception {
        doNothing().when(notificationService).markAsRead(999L, 1L);

        mockMvc.perform(post("/api/notifications/999/read")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("全部标记为已读 - 成功")
    void testMarkAllAsReadSuccess() throws Exception {
        doNothing().when(notificationService).markAllAsRead(1L);

        mockMvc.perform(post("/api/notifications/read-all")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("已全部标记为已读"));
    }

    @Test
    @DisplayName("删除通知 - 成功")
    void testDeleteNotificationSuccess() throws Exception {
        doNothing().when(notificationService).deleteNotification(1L, 1L);

        mockMvc.perform(delete("/api/notifications/1")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("已删除"));
    }

    @Test
    @DisplayName("删除通知 - 通知不存在")
    void testDeleteNotificationNotFound() throws Exception {
        doNothing().when(notificationService).deleteNotification(999L, 1L);

        mockMvc.perform(delete("/api/notifications/999")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
