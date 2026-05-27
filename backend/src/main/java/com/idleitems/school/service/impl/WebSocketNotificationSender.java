package com.idleitems.school.service.impl;

import com.idleitems.school.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketNotificationSender {

    private final SimpMessagingTemplate messagingTemplate;

    @Async("notificationExecutor")
    public void sendAsync(Long userId, Notification notification) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("id", notification.getId());
            payload.put("notificationType", notification.getNotificationType());
            payload.put("title", notification.getTitle());
            payload.put("content", notification.getContent());
            payload.put("relatedId", notification.getRelatedId());
            payload.put("relatedType", notification.getRelatedType());
            payload.put("isRead", false);
            payload.put("createdAt", notification.getCreatedAt() != null
                    ? notification.getCreatedAt().toString()
                    : null);

            messagingTemplate.convertAndSend("/topic/notifications/" + userId, payload);
            log.debug("WebSocket通知已异步发送，用户ID: {}", userId);
        } catch (Exception e) {
            log.error("WebSocket通知发送失败，用户ID: {}, 通知ID: {}, 错误: {}", userId, notification.getId(), e.getMessage());
        }
    }
}
