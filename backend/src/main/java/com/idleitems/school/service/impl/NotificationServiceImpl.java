package com.idleitems.school.service.impl;

import com.idleitems.school.entity.Notification;
import com.idleitems.school.repository.NotificationRepository;
import com.idleitems.school.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public Notification createNotification(Long userId, Integer type, String title, String content, Long relatedId, String relatedType) {
        log.info("创建通知，用户ID: {}, 类型: {}, 标题: {}", userId, type, title);

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setNotificationType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setRelatedType(relatedType);
        notification.setIsRead(false);
        notification.setIsDeleted(false);

        Notification saved = notificationRepository.save(notification);

        // 通过WebSocket推送通知
        sendWebSocketNotification(userId, saved);

        return saved;
    }

    @Override
    public Page<Notification> getNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalseAndIsDeletedFalse(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        notificationRepository.markAsRead(notificationId, userId, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        notificationRepository.softDelete(notificationId, userId);
    }

    @Override
    public void createOrderNotification(Long userId, String title, String content, Long orderId) {
        createNotification(userId, Notification.NotificationType.ORDER.getCode(), title, content, orderId, "ORDER");
    }

    @Override
    public void createSystemNotification(Long userId, String title, String content) {
        createNotification(userId, Notification.NotificationType.SYSTEM.getCode(), title, content, null, null);
    }

    @Override
    @Transactional
    public void batchCreateNotification(Long userId, Integer type, String title, String content, Long relatedId, String relatedType) {
        createNotification(userId, type, title, content, relatedId, relatedType);
    }

    /**
     * 通过WebSocket推送通知
     */
    private void sendWebSocketNotification(Long userId, Notification notification) {
        sendWebSocketNotificationWithRetry(userId, notification, 3);
    }
    
    /**
     * 带重试的WebSocket通知推送
     */
    private void sendWebSocketNotificationWithRetry(Long userId, Notification notification, int maxRetries) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("id", notification.getId());
                payload.put("title", notification.getTitle());
                payload.put("content", notification.getContent());
                payload.put("type", notification.getNotificationType());
                payload.put("relatedId", notification.getRelatedId());
                payload.put("relatedType", notification.getRelatedType());
                payload.put("createdAt", notification.getCreatedAt());

                messagingTemplate.convertAndSend("/topic/notifications/" + userId, payload);
                log.debug("WebSocket通知已发送，用户ID: {}, 尝试次数: {}", userId, attempt);
                return; // 发送成功，退出重试循环
            } catch (Exception e) {
                log.warn("WebSocket通知发送失败，尝试次数: {}/{}, 错误: {}", attempt, maxRetries, e.getMessage());
                if (attempt == maxRetries) {
                    log.error("WebSocket通知发送最终失败，用户ID: {}, 通知ID: {}", userId, notification.getId());
                } else {
                    // 等待一段时间后重试
                    try {
                        Thread.sleep(1000L * attempt); // 递增等待时间
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("重试等待被中断");
                        return;
                    }
                }
            }
        }
    }
}
