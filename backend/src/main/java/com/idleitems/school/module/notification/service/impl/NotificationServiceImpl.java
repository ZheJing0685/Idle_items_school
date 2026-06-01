package com.idleitems.school.module.notification.service.impl;

import com.idleitems.school.module.notification.entity.Notification;
import com.idleitems.school.module.notification.repository.NotificationRepository;
import com.idleitems.school.module.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
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
    private final WebSocketNotificationSender webSocketSender;

    private static final int RETENTION_DAYS = 90;

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

        webSocketSender.sendAsync(userId, saved);

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
     * 定期清理过期通知（每天凌晨3点执行）
     * 删除超过90天的已读通知，释放数据库空间
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupOldNotifications() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(RETENTION_DAYS);
        int deletedCount = notificationRepository.hardDeleteOldReadNotifications(threshold);
        if (deletedCount > 0) {
            log.info("清理过期通知完成: 删除了{}条超过{}天的已读通知", deletedCount, RETENTION_DAYS);
        }
    }
}
