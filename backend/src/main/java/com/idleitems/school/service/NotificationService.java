package com.idleitems.school.service;

import com.idleitems.school.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface NotificationService {

    /**
     * 创建通知
     */
    Notification createNotification(Long userId, Integer type, String title, String content, Long relatedId, String relatedType);

    /**
     * 获取用户通知列表
     */
    Page<Notification> getNotifications(Long userId, Pageable pageable);

    /**
     * 获取用户未读通知数量
     */
    long getUnreadCount(Long userId);

    /**
     * 标记单条通知为已读
     */
    void markAsRead(Long notificationId, Long userId);

    /**
     * 标记所有通知为已读
     */
    void markAllAsRead(Long userId);

    /**
     * 删除通知
     */
    void deleteNotification(Long notificationId, Long userId);

    /**
     * 创建订单通知
     */
    void createOrderNotification(Long userId, String title, String content, Long orderId);

    /**
     * 创建系统通知
     */
    void createSystemNotification(Long userId, String title, String content);

    /**
     * 批量创建通知
     */
    void batchCreateNotification(Long userId, Integer type, String title, String content, Long relatedId, String relatedType);
}
