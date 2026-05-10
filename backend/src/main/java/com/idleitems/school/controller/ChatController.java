package com.idleitems.school.controller;

import com.idleitems.school.common.Result;
import com.idleitems.school.dto.ChatDTO;
import com.idleitems.school.entity.Chat;
import com.idleitems.school.entity.ChatMessage;
import com.idleitems.school.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping
    public Result<Chat> createChat(
            @RequestAttribute("userId") Long userId,
            @RequestParam Long sellerId,
            @RequestParam Long itemId) {
        Chat chat = chatService.createChat(userId, sellerId, itemId);
        return Result.success("聊天会话创建成功", chat);
    }

    @GetMapping
    public Result<List<ChatDTO>> getUserChats(
            @RequestAttribute("userId") Long userId) {
        List<ChatDTO> chats = chatService.getChatsByUserIdListWithUserInfo(userId);
        return Result.success(chats);
    }

    @GetMapping("/{chatId}/messages")
    public Result<Page<ChatMessage>> getMessages(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long chatId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ChatMessage> messages = chatService.getMessagesByChatId(chatId, userId, pageable);
        return Result.success(messages);
    }

    @PostMapping("/{chatId}/messages")
    public Result<ChatMessage> sendMessage(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long chatId,
            @RequestParam Long receiverId,
            @RequestParam String content) {
        ChatMessage message = chatService.sendMessage(
                chatId, userId, receiverId, content, ChatMessage.MessageType.TEXT
        );
        
        // 发送WebSocket消息给接收者
        messagingTemplate.convertAndSend(
                "/topic/chat/" + receiverId,
                message
        );
        
        // 发送WebSocket消息给发送者（确认）
        messagingTemplate.convertAndSend(
                "/topic/chat/" + userId,
                message
        );
        
        return Result.success("消息发送成功", message);
    }

    @MessageMapping("/chat/send")
    public void handleWebSocketMessage(@Payload ChatMessage message, Principal principal) {
        // 从WebSocket会话获取认证用户ID
        Long authenticatedUserId = getUserIdFromPrincipal(principal);
        
        // 验证发送者身份
        if (!authenticatedUserId.equals(message.getSenderId())) {
            log.warn("WebSocket消息发送者身份不匹配: 认证用户={}, 消息发送者={}", 
                    authenticatedUserId, message.getSenderId());
            throw new SecurityException("无权发送此消息：发送者身份不匹配");
        }
        
        // 验证消息内容
        if (message.getContent() == null || message.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
        
        // 处理WebSocket消息
        ChatMessage savedMessage = chatService.sendMessage(
                message.getChatId(),
                authenticatedUserId,
                message.getReceiverId(),
                message.getContent(),
                message.getMessageType()
        );
        
        // 发送消息给接收者
        messagingTemplate.convertAndSend(
                "/topic/chat/" + message.getReceiverId(),
                savedMessage
        );
        
        // 发送确认消息给发送者
        messagingTemplate.convertAndSend(
                "/topic/chat/" + authenticatedUserId,
                savedMessage
        );
        
        log.debug("WebSocket消息已发送: chatId={}, senderId={}, receiverId={}", 
                message.getChatId(), authenticatedUserId, message.getReceiverId());
    }
    
    /**
     * 从Principal中获取用户ID
     *
     * @param principal WebSocket会话中的Principal
     * @return 用户ID
     */
    private Long getUserIdFromPrincipal(Principal principal) {
        if (principal instanceof Authentication) {
            Authentication authentication = (Authentication) principal;
            // 从Authentication的name中获取用户ID（在JWT过滤器中设置的）
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