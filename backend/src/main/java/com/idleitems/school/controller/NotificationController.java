package com.idleitems.school.controller;

import com.idleitems.school.common.Result;
import com.idleitems.school.entity.Notification;
import com.idleitems.school.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public Result<Page<Notification>> getNotifications(
            @RequestAttribute("userId") Long userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return Result.success(notificationService.getNotifications(userId, pageable));
    }

    @GetMapping("/unread-count")
    public Result<Map<String, Long>> getUnreadCount(@RequestAttribute("userId") Long userId) {
        long count = notificationService.getUnreadCount(userId);
        return Result.success(Map.of("count", count));
    }

    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        notificationService.markAsRead(id, userId);
        return Result.success("已标记为已读", null);
    }

    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(@RequestAttribute("userId") Long userId) {
        notificationService.markAllAsRead(userId);
        return Result.success("已全部标记为已读", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteNotification(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        notificationService.deleteNotification(id, userId);
        return Result.success("已删除", null);
    }
}
