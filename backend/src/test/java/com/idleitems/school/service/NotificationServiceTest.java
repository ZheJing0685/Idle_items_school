package com.idleitems.school.service;

import com.idleitems.school.entity.Notification;
import com.idleitems.school.repository.NotificationRepository;
import com.idleitems.school.service.impl.NotificationServiceImpl;
import com.idleitems.school.service.impl.WebSocketNotificationSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService 单元测试")
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private WebSocketNotificationSender webSocketSender;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testNotification = new Notification();
        testNotification.setId(1L);
        testNotification.setUserId(1L);
        testNotification.setNotificationType(Notification.NotificationType.SYSTEM.getCode());
        testNotification.setTitle("测试通知标题");
        testNotification.setContent("测试通知内容");
        testNotification.setRelatedId(null);
        testNotification.setRelatedType(null);
        testNotification.setIsRead(false);
        testNotification.setIsDeleted(false);
    }

    @Test
    @DisplayName("测试创建通知")
    void testCreateNotification() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        Notification result = notificationService.createNotification(
                1L,
                Notification.NotificationType.SYSTEM.getCode(),
                "测试通知标题",
                "测试通知内容",
                null,
                null
        );

        assertNotNull(result);
        assertEquals("测试通知标题", result.getTitle());
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(webSocketSender, times(1)).sendAsync(eq(1L), any(Notification.class));
    }

    @Test
    @DisplayName("测试获取用户通知列表")
    void testGetNotifications() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> page = new PageImpl<>(Arrays.asList(testNotification), pageable, 1);
        when(notificationRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(1L, pageable))
            .thenReturn(page);

        Page<Notification> result = notificationService.getNotifications(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("测试通知标题", result.getContent().get(0).getTitle());
        verify(notificationRepository, times(1)).findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(1L, pageable);
    }

    @Test
    @DisplayName("测试获取未读通知数量")
    void testGetUnreadCount() {
        when(notificationRepository.countByUserIdAndIsReadFalseAndIsDeletedFalse(1L)).thenReturn(5L);

        long count = notificationService.getUnreadCount(1L);

        assertEquals(5L, count);
        verify(notificationRepository, times(1)).countByUserIdAndIsReadFalseAndIsDeletedFalse(1L);
    }

    @Test
    @DisplayName("测试标记单条通知为已读")
    void testMarkAsRead() {
        when(notificationRepository.markAsRead(anyLong(), anyLong(), any())).thenReturn(1);

        notificationService.markAsRead(1L, 1L);

        verify(notificationRepository, times(1)).markAsRead(anyLong(), anyLong(), any());
    }

    @Test
    @DisplayName("测试标记所有通知为已读")
    void testMarkAllAsRead() {
        when(notificationRepository.markAllAsRead(anyLong(), any())).thenReturn(5);

        notificationService.markAllAsRead(1L);

        verify(notificationRepository, times(1)).markAllAsRead(anyLong(), any());
    }

    @Test
    @DisplayName("测试删除通知")
    void testDeleteNotification() {
        when(notificationRepository.softDelete(anyLong(), anyLong())).thenReturn(1);

        notificationService.deleteNotification(1L, 1L);

        verify(notificationRepository, times(1)).softDelete(anyLong(), anyLong());
    }

    @Test
    @DisplayName("测试创建订单通知")
    void testCreateOrderNotification() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        notificationService.createOrderNotification(1L, "订单标题", "订单内容", 100L);

        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(webSocketSender, times(1)).sendAsync(eq(1L), any(Notification.class));
    }

    @Test
    @DisplayName("测试创建系统通知")
    void testCreateSystemNotification() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        notificationService.createSystemNotification(1L, "系统标题", "系统内容");

        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(webSocketSender, times(1)).sendAsync(eq(1L), any(Notification.class));
    }

    @Test
    @DisplayName("测试批量创建通知")
    void testBatchCreateNotification() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        notificationService.batchCreateNotification(
                1L,
                Notification.NotificationType.ORDER.getCode(),
                "批量标题",
                "批量内容",
                200L,
                "ORDER"
        );

        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(webSocketSender, times(1)).sendAsync(eq(1L), any(Notification.class));
    }

    @Test
    @DisplayName("测试清理过期通知")
    void testCleanupOldNotifications() {
        when(notificationRepository.hardDeleteOldReadNotifications(any())).thenReturn(10);

        notificationService.cleanupOldNotifications();

        verify(notificationRepository, times(1)).hardDeleteOldReadNotifications(any());
    }
}
