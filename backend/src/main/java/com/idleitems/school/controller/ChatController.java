package com.idleitems.school.controller;

import com.idleitems.school.common.Result;
import com.idleitems.school.entity.Chat;
import com.idleitems.school.entity.ChatMessage;
import com.idleitems.school.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public Result<Page<Chat>> getUserChats(
            @RequestAttribute("userId") Long userId,
            @PageableDefault(size = 20, sort = "updatedAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<Chat> chats = chatService.getChatsByUserId(userId, pageable);
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
        
        // 发送WebSocket消息
        messagingTemplate.convertAndSend(
                "/topic/chat/" + receiverId,
                message
        );
        
        return Result.success("消息发送成功", message);
    }

    @MessageMapping("/chat/send")
    public void handleWebSocketMessage(@Payload ChatMessage message) {
        // 处理WebSocket消息
        ChatMessage savedMessage = chatService.sendMessage(
                message.getChatId(),
                message.getSenderId(),
                message.getReceiverId(),
                message.getContent(),
                message.getMessageType()
        );
        
        // 发送消息给接收者
        messagingTemplate.convertAndSend(
                "/topic/chat/" + message.getReceiverId(),
                savedMessage
        );
    }
}