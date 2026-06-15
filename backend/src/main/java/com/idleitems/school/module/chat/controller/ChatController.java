package com.idleitems.school.module.chat.controller;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.common.Result;
import com.idleitems.school.config.ApiPaths;
import com.idleitems.school.module.chat.dto.ChatDTO;
import com.idleitems.school.module.chat.entity.Chat;
import com.idleitems.school.module.chat.entity.ChatMessage;
import com.idleitems.school.module.chat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
@Tag(name = "聊天管理", description = "用户即时通讯相关接口")
@RestController
@RequestMapping(ApiPaths.Chat.BASE)
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    private static final int MAX_MESSAGE_LENGTH = 2000;

    @Operation(summary = "创建聊天会话", description = "用户与卖家创建新的聊天会话")
    @PostMapping
    public Result<Chat> createChat(
            @RequestAttribute("userId") Long userId,
            @RequestParam Long sellerId,
            @RequestParam Long itemId) {
        Chat chat = chatService.createChat(userId, sellerId, itemId);
        return Result.success("聊天会话创建成功", chat);
    }

    @Operation(summary = "获取用户聊天列表", description = "获取当前用户的所有聊天会话列表")
    @GetMapping
    public Result<java.util.List<ChatDTO>> getUserChats(
            @RequestAttribute("userId") Long userId) {
        java.util.List<ChatDTO> chats = chatService.getChatsByUserIdListWithUserInfo(userId);
        return Result.success(chats);
    }

    @Operation(summary = "获取聊天消息", description = "分页获取指定聊天会话的历史消息")
    @GetMapping("/{chatId}/messages")
    public Result<Page<ChatMessage>> getMessages(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long chatId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ChatMessage> messages = chatService.getMessagesByChatId(chatId, userId, pageable);
        // 异步标记消息为已读
        chatService.markMessagesAsReadAsync(chatId, userId);
        return Result.success(messages);
    }

    @Operation(summary = "发送消息", description = "向指定聊天会话发送消息并通过WebSocket推送")
    @PostMapping("/{chatId}/messages")
    public Result<ChatMessage> sendMessage(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long chatId,
            @RequestParam Long receiverId,
            @RequestParam String content,
            @RequestParam(required = false, defaultValue = "TEXT") ChatMessage.MessageType messageType) {
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "消息内容不能为空");
        }

        ChatMessage message = chatService.sendMessage(
                chatId, userId, receiverId, content, messageType
        );

        // 发送WebSocket消息给接收者
        messagingTemplate.convertAndSend(
                "/topic/chat/" + receiverId,
                message
        );

        // 发送WebSocket消息给发送者（确保发送者也能实时收到）
        messagingTemplate.convertAndSend(
                "/topic/chat/" + userId,
                message
        );

        return Result.success("消息发送成功", message);
    }

    @Operation(summary = "撤回消息", description = "撤回自己发送的消息（2分钟内且未读）")
    @PostMapping("/{chatId}/messages/{messageId}/recall")
    public Result<ChatMessage> recallMessage(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long chatId,
            @PathVariable Long messageId) {
        ChatMessage recalled = chatService.recallMessage(messageId, userId);

        // 通过WebSocket通知双方
        Chat chat = chatService.getChatsByUserId(userId, Pageable.unpaged())
                .getContent().stream()
                .filter(c -> c.getId().equals(chatId))
                .findFirst()
                .orElse(null);

        if (chat != null) {
            messagingTemplate.convertAndSend("/topic/chat/" + chat.getBuyerId(), recalled);
            messagingTemplate.convertAndSend("/topic/chat/" + chat.getSellerId(), recalled);
        }

        return Result.success("消息已撤回", recalled);
    }

    @Operation(summary = "标记消息已读", description = "手动标记指定会话的消息为已读")
    @PostMapping("/{chatId}/read")
    public Result<Void> markAsRead(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long chatId) {
        chatService.markMessagesAsRead(chatId, userId);
        return Result.success("已标记为已读", null);
    }

    @Operation(summary = "WebSocket发送消息", description = "通过WebSocket实时发送聊天消息")
    @MessageMapping("/chat/send")
    public void handleWebSocketMessage(@Payload ChatMessage message, Principal principal) {
        Long authenticatedUserId = getUserIdFromPrincipal(principal);

        if (!authenticatedUserId.equals(message.getSenderId())) {
            log.warn("WebSocket消息发送者身份不匹配: 认证用户={}, 消息发送者={}",
                    authenticatedUserId, message.getSenderId());
            throw new SecurityException("无权发送此消息：发送者身份不匹配");
        }

        if (message.getContent() == null || message.getContent().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "消息内容不能为空");
        }

        if (message.getContent().length() > MAX_MESSAGE_LENGTH) {
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                    "消息内容不能超过" + MAX_MESSAGE_LENGTH + "个字符");
        }

        ChatMessage savedMessage = chatService.sendMessage(
                message.getChatId(),
                authenticatedUserId,
                message.getReceiverId(),
                message.getContent(),
                message.getMessageType()
        );

        messagingTemplate.convertAndSend(
                "/topic/chat/" + message.getReceiverId(),
                savedMessage
        );

        messagingTemplate.convertAndSend(
                "/topic/chat/" + authenticatedUserId,
                savedMessage
        );

        log.debug("WebSocket消息已发送: chatId={}, senderId={}, receiverId={}",
                message.getChatId(), authenticatedUserId, message.getReceiverId());
    }

    private Long getUserIdFromPrincipal(Principal principal) {
        if (principal instanceof Authentication) {
            Authentication authentication = (Authentication) principal;
            String userIdStr = authentication.getName();
            try {
                return Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                log.error("无法解析用户ID: {}", userIdStr);
                throw new SecurityException("无效的用户身份信息");
            }
        }
        throw new SecurityException("无法获取用户身份信息");
    }
}