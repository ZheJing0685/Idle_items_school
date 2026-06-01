package com.idleitems.school.module.notification.controller;

import com.idleitems.school.common.Result;
import com.idleitems.school.module.notification.entity.Notification;
import com.idleitems.school.config.ApiPaths;
import com.idleitems.school.module.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "通知管理", description = "用户消息通知相关接口")
@RestController
@RequestMapping(ApiPaths.Notification.BASE)
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "获取通知列表", description = "分页获取当前用户的消息通知列表")
    @GetMapping
    public Result<Page<Notification>> getNotifications(
            @RequestAttribute("userId") Long userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return Result.success(notificationService.getNotifications(userId, pageable));
    }

    @Operation(summary = "获取未读通知数量", description = "获取当前用户的未读通知数量")
    @GetMapping("/unread-count")
    public Result<Map<String, Long>> getUnreadCount(@RequestAttribute("userId") Long userId) {
        long count = notificationService.getUnreadCount(userId);
        return Result.success(Map.of("count", count));
    }

    @Operation(summary = "标记通知为已读", description = "将指定通知标记为已读")
    @PostMapping("/{id}/read")
    public Result<Void> markAsRead(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        notificationService.markAsRead(id, userId);
        return Result.success("已标记为已读", null);
    }

    @Operation(summary = "全部标记为已读", description = "将当前用户所有通知标记为已读")
    @PostMapping("/read-all")
    public Result<Void> markAllAsRead(@RequestAttribute("userId") Long userId) {
        notificationService.markAllAsRead(userId);
        return Result.success("已全部标记为已读", null);
    }

    @Operation(summary = "删除通知", description = "删除指定通知")
    @DeleteMapping("/{id}")
    public Result<Void> deleteNotification(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        notificationService.deleteNotification(id, userId);
        return Result.success("已删除", null);
    }
}
